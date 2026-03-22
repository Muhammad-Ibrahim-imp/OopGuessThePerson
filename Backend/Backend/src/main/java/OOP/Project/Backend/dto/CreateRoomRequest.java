package OOP.Project.Backend.dto;

import lombok.Data;

// DTO = Data Transfer Object
// Represents the exact JSON shape Android sends when creating a room:
// { "hostName": "Ali" }
@Data
public class CreateRoomRequest {
    private String hostName;
}