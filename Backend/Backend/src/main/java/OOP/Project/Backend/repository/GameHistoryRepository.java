package OOP.Project.Backend.repository;

import OOP.Project.Backend.model.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {
    // Returns last 5 games for a user, newest first
    List<GameHistory> findTop5ByUserIdOrderByPlayedAtDesc(Long userId);
}