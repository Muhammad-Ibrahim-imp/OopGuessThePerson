package OOP.Project.Backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*; //jakarta.persistence = tools to connect your Java classes with a database
import lombok.Data; //@Data is a shortcut annotation in Lombok that automatically generates:

// ✅ Getters for all fields
// ✅ Setters for all fields
// ✅ toString() method
// ✅ equals() and hashCode()
// ✅ Required constructor

import java.util.ArrayList;
import java.util.List;

// @Entity → Hibernate creates a "rooms" table in MySQL for this class
// @Data   → Lombok generates all getters, setters, equals, hashCode, toString
// Note: Spring Boot 4 uses jakarta.persistence (NOT javax.persistence)
//       this changed in Spring Boot 3+ and jakarta is already correct here

@Entity
@Data
@Table(name= "rooms")
public class Room {
    // @Id = this is the primary key column in MySQL
    // IDENTITY = MySQL auto_increment (1, 2, 3, 4...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The 6-character code players type to join, e.g. "ABC123"
    // unique=true → MySQL enforces that no two rooms share the same code
    // nullable=false → MySQL rejects empty values
    @Column(unique = true, nullable = false, length = 6)
    private String roomCode;

    // Name of whoever created and hosts this room
    private String hostName;

    // "status" is a reserved keyword in MySQL so we wrap it in backticks
    // EnumType.STRING → stores "WAITING" as text, not 0/1/2
    @Enumerated(EnumType.STRING)
    @Column(name = "`status`")
    private RoomStatus status = RoomStatus.WAITING;

    // Tracks which question (0-based index) is currently being shown
    private int currentQuestionIndex = 0;

    // Maximum number of questions host wants to play
    // 0 means play all (one per player)
    private int maxQuestions = 0;

    // Timer duration in seconds for each question (set by host)
    private int questionTimerSeconds = 10

    // Tracks which player indices have already been used as question subjects
    // Stored as comma-separated string e.g. "0,2,4"
    // This ensures questions are never repeated
    @Column(length = 1000)
    private String usedQuestionIndices = "";

    // One Room → Many Players relationship
    // mappedBy="room"     → the Player class owns this via its "room" field
    // CascadeType.ALL     → deleting a room also deletes all its players
    // FetchType.EAGER     → always load the players list when loading a room
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    // @JsonManagedReference marks this as the "parent" side of the relationship
    // Jackson will serialize this side normally
    //Prevents infinite recursions
    private List<Player> players = new ArrayList<>();

    // Three possible states for a room
    public enum RoomStatus {
        WAITING,        // players are joining in the lobby
        IN_PROGRESS,    // game is actively running
        FINISHED        // game has ended, scores are final
    }
}