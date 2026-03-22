package OOP.Project.Backend.dto;

import lombok.Data;

// JSON Android sends when a player joins a room:
// {
//   "roomCode": "ABC123",
//   "playerName": "Sara",
//   "funFact": "I have visited 30 countries"
// }
@Data
public class JoinRoomRequest {
    private String roomCode;
    private String playerName;
    private String funFact;
}