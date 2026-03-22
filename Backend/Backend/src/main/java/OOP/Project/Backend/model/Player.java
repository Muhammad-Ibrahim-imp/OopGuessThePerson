package OOP.Project.Backend.model;

import jakarta.persistence.*;
import lombok.Data;

// @Entity → Hibernate creates a "players" table in MySQL
@Entity
@Data
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Player's real name — shown as answer options in the MCQ
    @Column(nullable = false)
    private String name;

    // The interesting fact they entered when joining the room
    // This becomes the statement in the MCQ question
    // length=500 → creates a VARCHAR(500) column in MySQL
    @Column(nullable = false, length = 500)
    private String funFact;

    // Starts at 0, increases by 1000 for each correct answer
    private int score = 0;

    // Tracks whether this player has answered the current question
    // Reset to false at the start of every new question
    private boolean hasAnswered = false;

    // Many Players → One Room
    // FetchType.LAZY → only load the Room data when we explicitly need it
    // @JoinColumn    → creates a "room_id" foreign key column in the players table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
}