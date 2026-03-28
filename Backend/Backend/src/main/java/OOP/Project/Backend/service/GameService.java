package OOP.Project.Backend.service;

import OOP.Project.Backend.model.*;
import OOP.Project.Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;

    // Tracks when each question started (roomCode → start time in ms)
    // Used to calculate how long each player took to answer
    private final Map<String, Long> questionStartTimes = new ConcurrentHashMap<>();

    // Tracks scheduled timer tasks (roomCode → timer thread)
    private final Map<String, Timer> questionTimers = new ConcurrentHashMap<>();

    public Question startGame(String roomCode, SimpMessagingTemplate template) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getPlayers().isEmpty()) {
            throw new RuntimeException("Cannot start with no players");
        }

        // Validate host has set their fun fact
        room.getPlayers().stream()
                .filter(p -> p.getName().equals(room.getHostName()))
                .findFirst()
                .ifPresent(host -> {
                    if (host.getFunFact().equals("Host's fun fact pending")) {
                        throw new RuntimeException("Host must set their fun fact before starting");
                    }
                });

        room.setStatus(Room.RoomStatus.IN_PROGRESS);
        room.setUsedQuestionIndices(""); // reset used indices
        roomRepository.save(room);

        // Determine total number of questions
        // maxQuestions = 0 means one question per player (no limit)
        int totalPlayers = room.getPlayers().size();
        int totalQuestions = room.getMaxQuestions() > 0
                ? Math.min(room.getMaxQuestions(), totalPlayers)
                : totalPlayers;

        // Build and return the first randomly selected question
        Question first = buildNextQuestion(room, totalQuestions);
        if (first != null) {
            // Record when this question started
            questionStartTimes.put(roomCode, System.currentTimeMillis());
            scheduleTimer(roomCode, room.getQuestionTimerSeconds(), template);
        }
        return first;
    }

    // Builds the next question by randomly picking an unused player's fact
    public Question buildNextQuestion(Room room, int totalQuestions) {
        List<Player> players = room.getPlayers();

        // Parse which indices have already been used
        Set<Integer> usedIndices = parseUsedIndices(room.getUsedQuestionIndices());

        // Find all available (unused) player indices
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (!usedIndices.contains(i)) {
                availableIndices.add(i);
            }
        }

        // No more unused players — game should end
        if (availableIndices.isEmpty()) return null;

        // How many questions have been asked so far
        int questionsAsked = usedIndices.size();

        // Stop if we've reached the question limit
        if (questionsAsked >= totalQuestions) return null;

        // Pick a RANDOM available index — this is the key change
        // so questions are never predictable or sequential
        Collections.shuffle(availableIndices);
        int chosenIndex = availableIndices.get(0);

        // Mark this index as used
        usedIndices.add(chosenIndex);
        room.setUsedQuestionIndices(
                usedIndices.stream().map(String::valueOf).collect(Collectors.joining(","))
        );
        roomRepository.save(room);

        Player factOwner = players.get(chosenIndex);

        // All other player names as wrong options
        List<String> otherNames = players.stream()
                .filter(p -> !p.getId().equals(factOwner.getId()))
                .map(Player::getName)
                .collect(Collectors.toList());

        Collections.shuffle(otherNames);
        List<String> options = new ArrayList<>(
                otherNames.subList(0, Math.min(3, otherNames.size()))
        );
        options.add(factOwner.getName());
        Collections.shuffle(options);

        Question question = new Question();
        question.setStatement(factOwner.getFunFact());
        question.setOptions(options);
        question.setCorrectAnswer(factOwner.getName());
        question.setQuestionIndex(questionsAsked + 1); // 1-based for display
        question.setTotalQuestions(totalQuestions);
        question.setTimerSeconds(room.getQuestionTimerSeconds());
        question.setFactOwnerPlayerId(factOwner.getId());

        return question;
    }

    // Called when a player submits their answer
    // Returns true when ALL players have answered
    public boolean processAnswer(String roomCode, Long playerId, String answeredName, long answerTimeMs) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        // Ignore duplicate answers from the same player
        if (player.isHasAnswered()) return false;

        // Calculate score based on correctness AND speed
        long questionStart = questionStartTimes.getOrDefault(roomCode, System.currentTimeMillis());
        long timeTakenMs = System.currentTimeMillis() - questionStart;
        int timerMs = room.getQuestionTimerSeconds() * 1000;

        if (answeredName.equals(getCorrectAnswerForCurrentQuestion(room))) {
            // Time-based scoring:
            // Full points (1000) for instant answer
            // Minimum points (100) for answering at the last second
            // Linear interpolation between these two values
            double timeRatio = 1.0 - ((double) timeTakenMs / timerMs);
            timeRatio = Math.max(0, Math.min(1, timeRatio)); // clamp between 0 and 1
            int points = (int) (100 + (900 * timeRatio));    // range: 100 to 1000
            player.setScore(player.getScore() + points);
        }

        player.setHasAnswered(true);
        player.setAnswerTimeMs(timeTakenMs);
        playerRepository.save(player);

        return room.getPlayers().stream().allMatch(Player::isHasAnswered);
    }

    // Get the correct answer for the current question without rebuilding it
    private String getCorrectAnswerForCurrentQuestion(Room room) {
        Set<Integer> usedIndices = parseUsedIndices(room.getUsedQuestionIndices());
        if (usedIndices.isEmpty()) return "";
        // The last added index is the current question's fact owner
       // int lastIndex = usedIndices.stream().mapToInt(i -> i).max().orElse(0);
        // We stored them in order so the most recently used is the current
        List<Integer> orderedUsed = new ArrayList<>(usedIndices);
        int currentOwnerIndex = orderedUsed.get(orderedUsed.size() - 1);
        return room.getPlayers().get(currentOwnerIndex).getName();
    }

    // Advances to the next question or ends the game
    public void advanceToNextQuestion(String roomCode, SimpMessagingTemplate template) {
        // Cancel any running timer for this room
        cancelTimer(roomCode);

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Give 0 score to players who didn't answer in time
        room.getPlayers().forEach(p -> {
            if (!p.isHasAnswered()) {
                p.setAnswerTimeMs(-1); // -1 indicates timeout
            }
            p.setHasAnswered(false); // reset for next question
            playerRepository.save(p);
        });

        // Determine total questions for this game
        int totalPlayers = room.getPlayers().size();
        int totalQuestions = room.getMaxQuestions() > 0
                ? Math.min(room.getMaxQuestions(), totalPlayers)
                : totalPlayers;

        // Try to build the next question
        Question next = buildNextQuestion(room, totalQuestions);

        if (next == null) {
            // No more questions — end the game
            room.setStatus(Room.RoomStatus.FINISHED);
            roomRepository.save(room);
            template.convertAndSend(
                    "/topic/game/" + roomCode + "/finished",
                    getRankedScores(roomCode)
            );
        } else {
            // Record the start time of this new question
            questionStartTimes.put(roomCode, System.currentTimeMillis());
            template.convertAndSend("/topic/game/" + roomCode + "/question", next);
            scheduleTimer(roomCode, room.getQuestionTimerSeconds(), template);
        }
    }

    // Returns scores with proper rank assignment (equal scores = same rank)
    public List<Map<String, Object>> getRankedScores(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Sort players by score descending
        List<Player> sorted = room.getPlayers().stream()
                .sorted(Comparator.comparingInt(Player::getScore).reversed())
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;

        for (int i = 0; i < sorted.size(); i++) {
            Player p = sorted.get(i);

            // If same score as previous player → same rank
            if (i > 0 && sorted.get(i).getScore() == sorted.get(i - 1).getScore()) {
                rank = result.isEmpty() ? 1 : (int) result.get(i - 1).get("rank");
            } else {
                rank = i + 1; // rank is 1-based position
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("rank", rank);
            entry.put("name", p.getName());
            entry.put("score", p.getScore());
            result.add(entry);
        }

        return result;
    }

    // Schedules auto-advance when timer runs out
    private void scheduleTimer(String roomCode, int seconds, SimpMessagingTemplate template) {
        Timer timer = new Timer();
        questionTimers.put(roomCode, timer);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Push scores then advance — same flow as when everyone answers
                template.convertAndSend(
                        "/topic/game/" + roomCode + "/scores",
                        getRankedScores(roomCode)
                );
                advanceToNextQuestion(roomCode, template);
            }
        }, seconds * 1000L); // convert seconds to milliseconds
    }

    // Cancels the timer if it exists (called when all players answer before time is up)
    private void cancelTimer(String roomCode) {
        Timer timer = questionTimers.remove(roomCode);
        if (timer != null) timer.cancel();
    }

    // Parses "0,2,4" into a Set {0, 2, 4}
    private Set<Integer> parseUsedIndices(String usedIndices) {
        Set<Integer> result = new LinkedHashSet<>(); // LinkedHashSet preserves insertion order
        if (usedIndices == null || usedIndices.isEmpty()) return result;
        for (String s : usedIndices.split(",")) {
            try { result.add(Integer.parseInt(s.trim())); }
            catch (NumberFormatException ignored) {}
        }
        return result;
    }
}