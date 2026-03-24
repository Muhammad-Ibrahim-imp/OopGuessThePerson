package OOP.Project.Backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

// @ControllerAdvice intercepts exceptions thrown anywhere in your controllers
// and converts them into proper HTTP responses instead of ugly 500 crashes
@ControllerAdvice
public class GlobalExceptionHandler {
    // Handles "Room not found", "Game already started", "Player not found" etc.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String,String>> handleRuntimeException(RuntimeException ex) {

        Map<String,String> errorResponse = new HashMap<>();

        // "error" tells the client what category of problem it is
        errorResponse.put("error", "Bad Request");

        // "message" tells the client exactly what went wrong
        // This comes directly from your throw new RuntimeException("Room not found: " + roomCode)
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    /*
    // Separate handler specifically for "not found" cases
    // You can throw this from your service when a room or player doesn't exist
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(IllegalArgumentException ex) {

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());

        // 404 NOT_FOUND = the resource they asked for does not exist
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }*/
}