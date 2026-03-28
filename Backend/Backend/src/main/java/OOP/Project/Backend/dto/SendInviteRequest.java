package OOP.Project.Backend.dto;

import lombok.Data;

@Data
public class SendInviteRequest {
    private Long friendUserId;  // who to invite
    private String roomCode;    // which room
}