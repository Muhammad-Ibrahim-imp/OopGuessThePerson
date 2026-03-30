package OOP.Project.Backend.service;

import OOP.Project.Backend.model.*;
import OOP.Project.Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameHistoryService {

    private final GameHistoryRepository gameHistoryRepository;
    private final UserRepository userRepository;

    // Called at the end of each game to save results for registered players
    public void recordGameResults(Room room, List<Map<String, Object>> rankedScores) {
        int totalPlayers = room.getPlayers().size();
        int totalQuestions = parseUsedIndices(room.getUsedQuestionIndices()).size();

        for (Player player : room.getPlayers()) {
            // Only record history for players linked to a registered user account
            if (player.getUser() == null) continue;

            // Find this player's rank and score from the ranked scores list
            Optional<Map<String, Object>> playerScore = rankedScores.stream()
                    .filter(s -> s.get("name").equals(player.getName()))
                    .findFirst();

            if (playerScore.isEmpty()) continue;

            int rank = (int) playerScore.get().get("rank");
            int score = (int) playerScore.get().get("score");

            GameHistory history = new GameHistory();
            history.setUser(player.getUser());
            history.setRoomCode(room.getRoomCode());
            history.setScore(score);
            history.setRank(rank);
            history.setTotalPlayers(totalPlayers);
            history.setTotalQuestions(totalQuestions);
            history.setCorrectAnswers(player.getCorrectAnswers());

            gameHistoryRepository.save(history);

            // Keep only the last 5 game history entries per user
           // trimHistoryToFive(player.getUser().getId());
        }
    }

    // Deletes oldest entries beyond the 5-game limit
   // private void trimHistoryToFive(Long userId) {
     //   List<GameHistory> all = gameHistoryRepository
       //         .findTop5ByUserIdOrderByPlayedAtDesc(userId);

        // Get ALL history for this user (not just top 5)
        // then delete any beyond the 5th entry
        //userRepository.findById(userId).ifPresent(user -> {
          //  List<GameHistory> allHistory = user.getGameHistory();
            //if (allHistory.size() > 5) {
                // Sort oldest first and delete extras
              //  allHistory.sort(Comparator.comparing(GameHistory::getPlayedAt));
                //List<GameHistory> toDelete = allHistory.subList(0, allHistory.size() - 5);
              //  gameHistoryRepository.deleteAll(toDelete);
         //   }
      //  });
  //  }

    // Get last 5 games for a user
    public List<Map<String, Object>> getHistory(Long userId) {
        return gameHistoryRepository
                .findTop5ByUserIdOrderByPlayedAtDesc(userId)
                .stream()
                .map(h -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("roomCode", h.getRoomCode());
                    map.put("score", h.getScore());
                    map.put("rank", h.getRank());
                    map.put("totalPlayers", h.getTotalPlayers());
                    map.put("totalQuestions", h.getTotalQuestions());
                    map.put("correctAnswers", h.getCorrectAnswers());
                    map.put("playedAt", h.getPlayedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private Set<Integer> parseUsedIndices(String usedIndices) {
        Set<Integer> result = new LinkedHashSet<>();
        if (usedIndices == null || usedIndices.isEmpty()) return result;
        for (String s : usedIndices.split(",")) {
            try { result.add(Integer.parseInt(s.trim())); }
            catch (NumberFormatException ignored) {}
        }
        return result;
    }
}
