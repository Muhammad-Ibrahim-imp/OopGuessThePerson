package OOP.Project.Backend.dto;

import lombok.Data;

// Sent when host creates a room
// Now includes game configuration settings
@Data
public class CreateRoomRequest {
    private String hostName;
    private int maxQuestions;         // 0 = no limit (one question per player)
    private int questionTimerSeconds; // seconds per question (minimum 5)
}