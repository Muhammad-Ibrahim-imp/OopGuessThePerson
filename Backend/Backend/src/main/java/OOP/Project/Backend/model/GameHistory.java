package OOP.Project.Backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "game_history")
public class GameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Room code of the game played
    private String roomCode;

    // Final score achieved in this game
    private int score;

    // Final rank/position in this game
    private int rank;

    // Total players in the game
    private int totalPlayers;

    // How many questions were in this game
    private int totalQuestions;

    // How many questions were answered correctly
    private int correctAnswers;

    private LocalDateTime playedAt = LocalDateTime.now();
}