package OOP.Project.Backend.controller;

import OOP.Project.Backend.dto.CreateRoomRequest;
import OOP.Project.Backend.dto.JoinRoomRequest;
import OOP.Project.Backend.model.Room;
import OOP.Project.Backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// @RestController → handles HTTP requests and returns JSON automatically
// @RequestMapping → all endpoints here start with /api/rooms
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor //Lombok annotation @RequiredArgsConstructor generates exactly one constructor.
//It creates a constructor including all final fields and fields annotated with @NonNull.
//Fields that are not final or @NonNull are ignored.
public class RoomController {

    private final RoomService roomService;

    // POST /api/rooms/create
    // Android sends: { "hostName": "Ali" }
    // Returns: the full Room object as JSON with the generated room code
    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(@RequestBody CreateRoomRequest request) {
        Room room = roomService.createRoom(request.getHostName());
        return ResponseEntity.ok(room);
        // ResponseEntity represents an HTTP response including body, status, and headers.
        // ok() → sets HTTP status 200.
        // room → response body.
    }

    // POST /api/rooms/join
    // Android sends: { "roomCode": "ABC123", "playerName": "Sara", "funFact": "..." }
    // Returns: the updated Room with the new player included
    @PostMapping("/join")
    public ResponseEntity<Room> joinRoom(@RequestBody JoinRoomRequest request) {
        Room room = roomService.joinRoom(
                request.getRoomCode(),
                request.getPlayerName(),
                request.getFunFact()
        );
        return ResponseEntity.ok(room);
    }

    // GET /api/rooms/ABC123
    // Returns the current state of the room (player list, status, etc.)
    // @PathVariable extracts "ABC123" from the URL
    @GetMapping("/{code}")
    public ResponseEntity<Room> getRoom(@PathVariable String code) {
        return ResponseEntity.ok(roomService.getRoomByCode(code));
    }
}