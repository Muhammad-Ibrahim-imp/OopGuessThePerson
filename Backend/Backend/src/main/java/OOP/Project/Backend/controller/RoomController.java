package OOP.Project.Backend.controller;

import OOP.Project.Backend.dto.*;
import OOP.Project.Backend.model.Room;
import OOP.Project.Backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // POST /api/rooms/create
    // Body now includes maxQuestions and questionTimerSeconds
    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(@RequestBody CreateRoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    // POST /api/rooms/join
    @PostMapping("/join")
    public ResponseEntity<Room> joinRoom(@RequestBody JoinRoomRequest request) {
        return ResponseEntity.ok(roomService.joinRoom(
                request.getRoomCode(),
                request.getPlayerName(),
                request.getFunFact()
        ));
    }

    // PUT /api/rooms/{code}/host-fact
    // Host sets their own fun fact before game starts
    @PutMapping("/{code}/host-fact")
    public ResponseEntity<Room> setHostFunFact(
            @PathVariable String code,
            @RequestBody JoinRoomRequest request) {
        return ResponseEntity.ok(roomService.setHostFunFact(
                code,
                request.getPlayerName(), // host name for verification
                request.getFunFact()
        ));
    }

    // DELETE /api/rooms/{code}/kick
    // Host kicks a player from the lobby
    @DeleteMapping("/{code}/kick")
    public ResponseEntity<Room> kickPlayer(
            @PathVariable String code,
            @RequestBody KickPlayerRequest request) {
        return ResponseEntity.ok(roomService.kickPlayer(
                code,
                request.getHostName(),
                request.getPlayerId()
        ));
    }

    // GET /api/rooms/{code}
    @GetMapping("/{code}")
    public ResponseEntity<Room> getRoom(@PathVariable String code) {
        return ResponseEntity.ok(roomService.getRoomByCode(code));
    }
}