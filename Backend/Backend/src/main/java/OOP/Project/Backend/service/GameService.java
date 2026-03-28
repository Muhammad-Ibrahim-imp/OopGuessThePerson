package OOP.Project.Backend.service;

import OOP.Project.Backend.model.*;
import OOP.Project.Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    // Called when the host taps "Start Game"
    // Changes status to IN_PROGRESS and returns the first question
    public Question startGame(String roomCode, SimpMessagingTemplate template) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(()-> new RuntimeException("Room not found"));

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

        // Change from WAITING → IN_PROGRESS and persist to MySQL
        room.setStatus(Room.RoomStatus.IN_PROGRESS);
        room.setUsedQuestionIndices(""); // reset used indices // smjh nhin ayee
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

    public Question buildQuestion(Room room, int questionIndex) {
        List<Player> players = room.getPlayers();

        // Safety check — if index is out of bounds, game should have ended
        if(questionIndex>=players.size()) return null;

        // The player whose fun fact becomes the question statement
        Player factOwner = players.get(questionIndex);

        // All other player names become the wrong answer options
        List<String> otherNames = players.stream()
                .filter(p -> !p.getId().equals(factOwner.getId()))
                .map(Player::getName)
                .collect(Collectors.toList());

        // Shuffle wrong options and take at most 3
        Collections.shuffle(otherNames);
        List<String> options = new ArrayList<>(
                otherNames.subList(0,Math.min(3,otherNames.size()))
        );

        // Add the correct answer then shuffle again
        // so the correct answer is not always in the same position
        options.add(factOwner.getName());
        Collections.shuffle(options);

        // Build and return the Question object
        Question question = new Question();
        question.setStatement(factOwner.getFunFact());
        question.setOptions(options);
        question.setCorrectAnswer(factOwner.getName());
        question.setQuestionIndex(questionIndex);
        question.setTotalQuestions(players.size());

        return question;
    }

    // Called when a player submits an answer.
    // Returns true if ALL players have now answered — signals time to move on.
    public boolean processAnswer(String roomCode, Long playerId, String answeredName) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(()->new RuntimeException("Room not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(()-> new RuntimeException("Player not found"));

        // Get the current question to check the answer against
        Question current = buildQuestion(room, room.getCurrentQuestionIndex());

        // Award 1000 points for a correct answer
        if (answeredName.equals(current.getCorrectAnswer())) {
            player.setScore(player.getScore()+1000);
        }

        // Mark this player as done for this round
        player.setHasAnswered(true);
        playerRepository.save(player);

        // allMatch returns true only when EVERY player has answered
        return room.getPlayers().stream().allMatch(Player::isHasAnswered);
    }

    // Moves to the next question, or ends the game if all questions are done
    public void advanceToNextQuestion(String roomCode, SimpMessagingTemplate template) { //SimpMessagingTemplate a Spring class used to send messages from the backend to clients over WebSockets (STOMP protocol).
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(()-> new RuntimeException("Room not found"));

        int nextIndex = room.getCurrentQuestionIndex() + 1;
        room.setCurrentQuestionIndex(nextIndex);

        // Reset hasAnswered for all players before the next question
        room.getPlayers().forEach(p->{
            p.setHasAnswered(false);
            playerRepository.save(p);
        });

        if (nextIndex >= room.getPlayers().size()) {
            // No more questions — game is over
            room.setStatus(Room.RoomStatus.FINISHED);
            roomRepository.save(room);

            // Push the final leaderboard to all players
            template.convertAndSend(  //convertAndSend() is a method of SimpMessagingTemplate used to send a message (Java object) to a WebSocket topic after converting it to JSON.
                    //syntax : template.convertAndSend(destination, payload);
                    //convert → Java object → JSON
                    //send    → push to clients via WebSocket
                    //→ destination is the topic/channel to send the message to, and payload is the data (Java object) that gets converted to JSON and sent.
                    "/topic/game/" + roomCode + "/finished",
                    getScores(roomCode)
            );
        } else {

            // Push the next question to all players
            roomRepository.save(room);
            Question next = buildQuestion(room, nextIndex);
            template.convertAndSend(
                    "/topic/game" + roomCode + "/question",
                    next
            );
        }
    }
    // Returns all players sorted by score (highest first)
    public List<Map<String, Object>> getScores(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(()-> new RuntimeException("Room not found"));

        return room.getPlayers().stream()
                .sorted(Comparator.comparingInt(Player::getScore).reversed())
                .map(p->(Map<String, Object>) (Map<?, ?>) Map.of("name", p.getName(), "score", p.getScore()))
                .collect(Collectors.toList());
        // Map.of returns a strict generic type incompatible with Map<String, Object>.
        // Using (Map<?, ?>) → (Map<String, Object>) casts it to the expected type safely.
    }
}