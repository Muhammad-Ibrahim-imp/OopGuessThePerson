package OOP.Project.Backend.service;

import OOP.Project.Backend.model.*;
import OOP.Project.Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final RoomInviteRepository roomInviteRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Send a friend request from one user to another
    public FriendRequest sendFriendRequest(Long senderId, String receiverUsername) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + receiverUsername));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("Cannot send friend request to yourself");
        }

        // Check if already friends
        boolean alreadyFriends = sender.getFriends().stream()
                .anyMatch(f -> f.getId().equals(receiver.getId()));
        if (alreadyFriends) {
            throw new RuntimeException("Already friends");
        }

        // Check if request already sent
        if (friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiver.getId())) {
            throw new RuntimeException("Friend request already sent");
        }

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        FriendRequest saved = friendRequestRepository.save(request);

        // Push real-time notification to receiver
        messagingTemplate.convertAndSendToUser(
                receiverUsername,
                "/queue/friend-requests",
                Map.of(
                        "type", "FRIEND_REQUEST",
                        "from", sender.getUsername(),
                        "requestId", saved.getId()
                )
        );

        return saved;
    }

    // Accept a pending friend request
    public void acceptFriendRequest(Long requestId, Long receiverId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new RuntimeException("Not authorized");
        }

        request.setStatus(FriendRequest.RequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        // Add each other as friends (bidirectional)
        User sender = request.getSender();
        User receiver = request.getReceiver();

        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        userRepository.save(sender);
        userRepository.save(receiver);
    }

    // Decline a pending friend request
    public void declineFriendRequest(Long requestId, Long receiverId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new RuntimeException("Not authorized");
        }

        request.setStatus(FriendRequest.RequestStatus.DECLINED);
        friendRequestRepository.save(request);
    }

    // Get all pending incoming friend requests for a user
    public List<Map<String, Object>> getPendingRequests(Long userId) {
        return friendRequestRepository
                .findByReceiverIdAndStatus(userId, FriendRequest.RequestStatus.PENDING)
                .stream()
                .map(r -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("requestId", r.getId());
                    map.put("fromUsername", r.getSender().getUsername());
                    map.put("sentAt", r.getSentAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Get a user's full friends list
    public List<Map<String, Object>> getFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getFriends().stream()
                .map(f -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("userId", f.getId());
                    map.put("username", f.getUsername());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Host sends a room invite to a friend
    public RoomInvite sendRoomInvite(Long hostUserId, Long friendUserId, String roomCode) {
        User host = userRepository.findById(hostUserId)
                .orElseThrow(() -> new RuntimeException("Host not found"));

        User friend = userRepository.findById(friendUserId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        // Only friends can be invited
        boolean areFriends = host.getFriends().stream()
                .anyMatch(f -> f.getId().equals(friendUserId));
        if (!areFriends) {
            throw new RuntimeException("Can only invite friends");
        }

        RoomInvite invite = new RoomInvite();
        invite.setSender(host);
        invite.setReceiver(friend);
        invite.setRoomCode(roomCode);
        RoomInvite saved = roomInviteRepository.save(invite);

        // Push real-time invite notification to the friend
        messagingTemplate.convertAndSendToUser(
                friend.getUsername(),
                "/queue/invites",
                Map.of(
                        "type", "ROOM_INVITE",
                        "from", host.getUsername(),
                        "roomCode", roomCode,
                        "inviteId", saved.getId()
                )
        );

        return saved;
    }

    // Get all pending room invites for a user
    public List<Map<String, Object>> getPendingInvites(Long userId) {
        return roomInviteRepository
                .findByReceiverIdAndStatus(userId, RoomInvite.InviteStatus.PENDING)
                .stream()
                .map(i -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("inviteId", i.getId());
                    map.put("fromUsername", i.getSender().getUsername());
                    map.put("roomCode", i.getRoomCode());
                    map.put("sentAt", i.getSentAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Accept a room invite
    public void acceptRoomInvite(Long inviteId, Long userId) {
        RoomInvite invite = roomInviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!invite.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        invite.setStatus(RoomInvite.InviteStatus.ACCEPTED);
        roomInviteRepository.save(invite);
    }

    // Decline a room invite
    public void declineRoomInvite(Long inviteId, Long userId) {
        RoomInvite invite = roomInviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!invite.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        invite.setStatus(RoomInvite.InviteStatus.DECLINED);
        roomInviteRepository.save(invite);
    }
}