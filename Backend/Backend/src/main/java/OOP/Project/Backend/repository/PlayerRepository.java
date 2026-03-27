package OOP.Project.Backend.repository;

import OOP.Project.Backend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository<Player, Long> means:
// - we are working with Player objects
// - the primary key type is Long
// JpaRepository's built-in findById() and save() are all we need here
public interface PlayerRepository extends JpaRepository<Player, Long> {
    // Find all players in a specific room by room ID
    List<Player> findByRoomId(Long roomId);

    // Check if a player with this name already exists in this room
    // Used to prevent duplicate name entries
    boolean existsByRoomIdAndName(Long roomId, String name);
}