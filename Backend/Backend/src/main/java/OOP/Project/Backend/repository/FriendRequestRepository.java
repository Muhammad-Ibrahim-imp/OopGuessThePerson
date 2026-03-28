package OOP.Project.Backend.repository;

import OOP.Project.Backend.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequest.RequestStatus status);

    List<FriendRequest> findBySenderIdAndStatus(Long senderId, FriendRequest.RequestStatus status);

    // Check if a request already exists between two users
    Optional<FriendRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
}