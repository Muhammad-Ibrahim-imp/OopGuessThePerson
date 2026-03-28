package OOP.Project.Backend.controller;

import OOP.Project.Backend.dto.AnswerRequest;
import OOP.Project.Backend.model.Question;
import OOP.Project.Backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    // Host starts game — no minimum player requirement
    @MessageMapping("/game/{roomCode}/start")
    public void startGame(@DestinationVariable String roomCode) {
        Question firstQuestion = gameService.startGame(roomCode, messagingTemplate);
        if (firstQuestion != null) {
            messagingTemplate.convertAndSend(
                    "/topic/game/" + roomCode + "/question",
                    firstQuestion
            );
        }
    }

    // Player submits answer with their answer time
    @MessageMapping("/game/{roomCode}/answer")
    public void submitAnswer(
            @DestinationVariable String roomCode,
            @Payload AnswerRequest request) {

        boolean allAnswered = gameService.processAnswer(
                roomCode,
                request.getPlayerId(),
                request.getAnsweredPlayerId(),
                request.getAnswerTimeMs()
        );

        if (allAnswered) {
            // All answered before timer — cancel timer and advance immediately
            messagingTemplate.convertAndSend(
                    "/topic/game/" + roomCode + "/scores",
                    gameService.getRankedScores(roomCode)
            );
            gameService.advanceToNextQuestion(roomCode, messagingTemplate);
        }
    }
}