package OOP.Project.Backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    // Stored as bcrypt hash — never plain text
    @Column(nullable = false)
    private String password;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Friends are stored as a list of user IDs this user has befriended
    // Bidirectional: if A friends B, B also friends A
    @ManyToMany
    @JoinTable(
            name = "friendships",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private List<User> friends = new ArrayList<>();

    // Last 5 game history entries for this user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("playedAt DESC")
    private List<GameHistory> gameHistory = new ArrayList<>();
}