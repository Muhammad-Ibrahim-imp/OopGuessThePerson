package OOP.Project.Backend.model;

import lombok.Data;
import java.util.List;

// NOT an @Entity — this class is never saved to MySQL
// It is built on the fly from players' fun facts
// and sent over WebSocket to all players
@Data
public class Question {
    // The fun fact being shown, e.g. "I have visited 30 countries"
    private String statement;

    // Four player names as answer choices ["Ali", "Sara", "Ahmed", "Bilal"]
    private List<String> options;

    // The name of whoever owns this fun fact — the correct answer
    private String correctAnswer;

    // Which question this is (0-based), used for progress display
    private int questionIndex;

    // Total number of questions = total number of players
    private int totalQuestions;

    private int timerSeconds;        // how many seconds players have to answer
    private Long factOwnerPlayerId;  // ID of the player whose fact this is
}