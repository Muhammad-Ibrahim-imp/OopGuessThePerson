package OOP.Project.Backend.repository;

import OOP.Project.Backend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<Player, Long> means:
// - we are working with Player objects
// - the primary key type is Long
// JpaRepository's built-in findById() and save() are all we need here
public interface PlayerRepository extends JpaRepository<Player, Long> {
}