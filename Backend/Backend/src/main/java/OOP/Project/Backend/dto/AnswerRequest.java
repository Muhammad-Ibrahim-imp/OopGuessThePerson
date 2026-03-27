package OOP.Project.Backend.dto;

import lombok.Data;

// Sent over WebSocket when a player submits an answer:
// { "playerId": 3, "answeredPlayerId": "Sara" }
@Data
public class AnswerRequest {
    private Long playerId;                  // ID of the player who is answering
    private String answeredPlayerId;        // the name they selected as their answer
    private long answerTimeMs;              // how many milliseconds the player took to answe
}