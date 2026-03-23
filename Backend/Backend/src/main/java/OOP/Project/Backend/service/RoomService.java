package OOP.Project.Backend.service;

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
    public Room createRoom(String hostName) {

        // UUID.randomUUID() generates something like "a3f9k2b1-4c2d-..."
        // We strip dashes, take first 6 characters, uppercase them → "A3F9K2"
        String roomCode = UUID.randomUUID()
                .toString()
                .replace("-","")
                .substring(0, 6)
                .toUpperCase();

        Room room = new Room();
        room.setRoomCode(roomCode);
        room.setHostName(hostName);
        room.setStatus(Room.RoomStatus.WAITING);

        // save() runs INSERT INTO rooms (...) VALUES (...)
        return roomRepository.save(room);
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
        // Create the player and link them to this room
        Player player = new Player();
        player.setName(playerName);
        player.setFunFact(funFact);
        player.setRoom(room); // this sets the room_id foreign key in MySQL

        playerRepository.save(player);

        // Reload the room so the returned object includes the new player
        return roomRepository.findByRoomCode(roomCode).get();
    }

    // Fetches a room by its code
    public Room getRoomByCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode)
                .orElseThrow(()-> new RuntimeException("Room not found: " + roomCode));
    }
}