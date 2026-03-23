package OOP.Project.Backend.controller;

import OOP.Project.Backend.dto.AnswerRequest;
import OOP.Project.Backend.model.Question;
import OOP.Project.Backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

// @Controller (not @RestController) because WebSocket handlers work differently
// They don't return HTTP responses — they push messages to topics
@Controller
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    // SimpMessagingTemplate lets the SERVER push messages to clients
    // without the client having requested anything — this is the magic of WebSocket
    private final SimpMessagingTemplate messagingTemplate;

    // Android sends to /app/game/{roomCode}/start
    // @MessageMapping listens for this destination
    // @DestinationVariable extracts {roomCode} from the destination string

    //@MessageMapping in WebSockets is conceptually similar to @RequestMapping in HTTP,
    // as both map incoming client requests/messages to handler methods,
    // but WebSockets are event-driven and support server push.
    @MessageMapping("/game/{roomCode}/start") //Curly braces means: “There will be some value here, and I want to capture it as roomCode.”
    public void startGame(@DestinationVariable String roomCode) {
        Question firstQuestion = gameService.startGame(roomCode);

        // Push the first question to ALL players in this room simultaneously
        // Everyone subscribed to /topic/game/ABC123/question receives this
        messagingTemplate.convertAndSend(
                "/topic/game/" + roomCode + "/question",
                firstQuestion
        );
    }

    // Android sends to /app/game/{roomCode}/answer
    // @Payload extracts the message body and converts JSON → AnswerRequest
    @MessageMapping("/game/{roomCode}/answer") // Equivalent to @RequestMapping (and its shortcuts-[@GetMapping @PostMapping @PutMapping @DeleteMapping]) in HTTP
    public void submitAnswer(
            @DestinationVariable String roomCode, // Equivalent to @PathVariable in HTTP
            //@Payload binds the body of a WebSocket message to a method parameter and converts it from JSON into a Java object
            @Payload AnswerRequest request // Equivalent to @RequestBody in HTTP
            ) {

        // Process the answer, update score if correct
        boolean allAnswered = gameService.processAnswer(
                roomCode,
                request.getPlayerId(),
                request.getAnsweredPlayerId()
        );

        // Only advance when ALL players have answered
        if(allAnswered) {
            // Push updated scores to all players
            messagingTemplate.convertAndSend(
                    "/topic/game/" + roomCode + "/scores",
                    gameService.getScores(roomCode)
            );

            // Move to next question or end game
            gameService.advanceToNextQuestion(roomCode, messagingTemplate);
        }
    }
}