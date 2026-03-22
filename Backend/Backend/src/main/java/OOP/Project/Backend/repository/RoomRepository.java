package OOP.Project.Backend.repository;

//JpaRepository → gives ready-made database operations
//Optional → safely handles missing data (no null crashes)
import OOP.Project.Backend.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository gives you free database methods without writing SQL:
// save(room)         → INSERT or UPDATE
// findById(id)       → SELECT WHERE id = ?
// findAll()          → SELECT all rooms
// deleteById(id)     → DELETE WHERE id = ?
//
// Spring reads the method name below and auto-generates this SQL:
// SELECT * FROM rooms WHERE room_code = ?
//
// Optional<Room> forces you to handle the case where room is not found
// JpaRepository<Room, Long> means:
// - we are working with Room objects
// - the primary key type is Long
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomCode(String roomCode);
}

