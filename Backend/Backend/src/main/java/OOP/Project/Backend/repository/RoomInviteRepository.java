package OOP.Project.Backend.repository;

import OOP.Project.Backend.model.RoomInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomInviteRepository extends JpaRepository<RoomInvite, Long> {
    // All pending invites for a user
    List<RoomInvite> findByReceiverIdAndStatus(Long receiverId, RoomInvite.InviteStatus status);
}