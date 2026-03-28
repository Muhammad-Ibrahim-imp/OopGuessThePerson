package OOP.Project.Backend.service;

import OOP.Project.Backend.dto.CreateRoomRequest;
import OOP.Project.Backend.model.*;
import OOP.Project.Backend.repository.PlayerRepository;
import OOP.Project.Backend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

// @Service marks this as a business logic class.
// Spring creates one instance and injects it wherever needed.
// @RequiredArgsConstructor (Lombok) generates a constructor for all final fields,
// which is how Spring injects the repositories (Dependency Injection).
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;

    // Creates a new game room with a random 6-character code
    public Room createRoom(CreateRoomRequest request) {

        // UUID.randomUUID() generates something like "a3f9k2b1-4c2d-..."
        // We strip dashes, take first 6 characters, uppercase them → "A3F9K2"
        String roomCode = UUID.randomUUID()
                .toString()
                .replace("-","")
                .substring(0, 6)
                .toUpperCase();

        // Validate timer — minimum 5 seconds, default 30 if not provided
        int timer = request.getQuestionTimerSeconds();
        if (timer < 5) timer = 30;

        Room room = new Room();
        room.setRoomCode(roomCode);
        room.setHostName(request.getHostName());
        room.setStatus(Room.RoomStatus.WAITING);
        room.setMaxQuestions(request.getMaxQuestions());
        room.setQuestionTimerSeconds(timer);

        Room savedRoom = roomRepository.save(room);

        // Host is also a player — they play the game too
        // Their fun fact will be used as one of the questions
        Player hostPlayer = new Player();
        hostPlayer.setName(request.getHostName());
        // Host's fun fact will be set separately when they join as a player
        // For now set a placeholder — host must update this before starting
        hostPlayer.setFunFact("Host's fun fact pending");
        hostPlayer.setRoom(savedRoom);
        playerRepository.save(hostPlayer);

        return roomRepository.findByRoomCode(roomCode).get();
    }

    // Adds a new player to an existing room
    public Room joinRoom(String roomCode, String playerName, String funFact) {

        // Find the room or throw an error if it does not exist
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(()->new RuntimeException("Room not found: " + roomCode));

        // Prevent joining a game that has already started
        if(room.getStatus() != Room.RoomStatus.WAITING) {
            throw new RuntimeException("Game has already started - cannot join");
        }

        // Prevent duplicate player names in the same room
        if (playerRepository.existsByRoomIdAndName(room.getId(), playerName)) {
            throw new RuntimeException("Name '" + playerName + "' is already taken in this room");
        }

        // Host name is reserved — other players cannot use it
        if (playerName.equalsIgnoreCase(room.getHostName())) {
            throw new RuntimeException("Name '" + playerName + "' is reserved for the host");
        }

        // Create the player and link them to this room
        Player player = new Player();
        player.setName(playerName);
        player.setFunFact(funFact);
        player.setRoom(room); // this sets the room_id foreign key in MySQL

        playerRepository.save(player);

        // Reload the room so the returned object includes the new player
        return roomRepository.findByRoomCode(roomCode).get();
    }

    // Allows host to update their own fun fact before the game starts
    public Room setHostFunFact(String roomCode, String hostName, String funFact) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomCode));

        if (!room.getHostName().equals(hostName)) {
            throw new RuntimeException("Only the host can update their fun fact");
        }

        // Find the host player entry and update their fun fact
        room.getPlayers().stream()
                .filter(p -> p.getName().equals(hostName))
                .findFirst()
                .ifPresent(hostPlayer -> {
                    hostPlayer.setFunFact(funFact);
                    playerRepository.save(hostPlayer);
                });

        return roomRepository.findByRoomCode(roomCode).get();
    }

    // Host kicks a player before the game starts
    public Room kickPlayer(String roomCode, String hostName, Long playerId) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomCode));

        if (!room.getHostName().equals(hostName)) {
            throw new RuntimeException("Only the host can kick players");
        }

        if (room.getStatus() != Room.RoomStatus.WAITING) {
            throw new RuntimeException("Cannot kick players after game has started");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Host cannot kick themselves
        if (player.getName().equals(hostName)) {
            throw new RuntimeException("Host cannot kick themselves");
        }

        playerRepository.delete(player);
        return roomRepository.findByRoomCode(roomCode).get();
    }

    // Fetches a room by its code
    public Room getRoomByCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode)
                .orElseThrow(()-> new RuntimeException("Room not found: " + roomCode));
    }
}