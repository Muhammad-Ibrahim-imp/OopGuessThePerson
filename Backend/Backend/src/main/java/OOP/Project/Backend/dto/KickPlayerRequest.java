package OOP.Project.Backend.dto;

import lombok.Data;

// Sent by host to kick a player before game starts
@Data
public class KickPlayerRequest {
    private Long playerId;   // ID of the player to remove
    private String hostName; // must match room's hostName to verify identity
}
