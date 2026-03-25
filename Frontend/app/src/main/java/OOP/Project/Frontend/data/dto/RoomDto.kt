package OOP.Project.Frontend.data.dto

/*
 * DTO = Data Transfer Object
 *
 * This file contains ALL the data classes that represent JSON
 * going TO and FROM the Spring Boot backend.
 *
 * Think of DTOs as "contracts" between your Android app and backend.
 * If the backend sends { "hostName": "Ali" }, you need a data class
 * with a field called hostName — the names MUST match exactly.
 *
 * Gson (the JSON library) reads the field names and maps them
 * automatically. If names don't match, the field will be null.
 *
 * There are 3 categories of DTOs in this file:
 *   1. Request DTOs  — data we SEND to the backend (HTTP body)
 *   2. Response DTOs — data we RECEIVE from the backend (HTTP response)
 *   3. WebSocket DTOs — data received over WebSocket during the game
 */

// ════════════════════════════════════════════════════════════════
// 1. REQUEST DTOs — sent TO the backend
// ════════════════════════════════════════════════════════════════

/*
 * Sent when the host creates a new game room.
 *
 * The Android app sends this as a JSON body in a POST request:
 * POST /api/rooms/create
 * Body: { "hostName": "Ibrahim" }
 *
 * The backend reads this and creates a Room with Ibrahim as host.
 *
 * Why a data class?
 * Kotlin data classes automatically get equals(), hashCode(),
 * toString(), and copy() methods — perfect for simple data holders.
 */
data class CreateRoomRequest(
    val hostName: String  // name of whoever is creating and hosting the room
)

/*
 * Sent when a player joins an existing room.
 *
 * POST /api/rooms/join
 * Body: {
 *   "roomCode": "ABC123",
 *   "playerName": "Sara",
 *   "funFact": "I have visited 30 countries"
 * }
 *
 * The roomCode tells the backend WHICH room to join.
 * The funFact becomes the MCQ question statement later in the game.
 */
data class JoinRoomRequest(
    val roomCode: String,    // 6-character code the host shared
    val playerName: String,  // player's real name — shown as MCQ options
    val funFact: String      // interesting fact — becomes the question statement
)

/*
 * Sent over WebSocket when a player taps an answer button.
 *
 * WebSocket destination: /app/game/{roomCode}/answer
 * Body: { "playerId": 3, "answeredPlayerId": "Sara" }
 *
 * playerId identifies WHO is answering.
 * answeredPlayerId is the NAME they selected as their answer.
 *
 * Note: This is sent as JSON over WebSocket, not HTTP.
 * Gson converts this data class to JSON string before sending.
 */
data class AnswerRequest(
    val playerId: Long,           // the ID of the player who is answering
    val answeredPlayerId: String  // the name they chose as the answer
)

// ════════════════════════════════════════════════════════════════
// 2. RESPONSE DTOs — received FROM the backend
// ════════════════════════════════════════════════════════════════

/*
 * Represents one player inside a room.
 *
 * This is a nested object inside RoomDto.players list.
 * The backend sends an array of these inside the room response.
 *
 * Example JSON for one player:
 * {
 *   "id": 1,
 *   "name": "Sara",
 *   "funFact": "I have visited 30 countries",
 *   "score": 0,
 *   "hasAnswered": false
 * }
 */
data class PlayerDto(
    val id: Long,            // unique ID assigned by MySQL auto-increment
    val name: String,        // player's real name
    val funFact: String,     // their interesting fact
    val score: Int,          // current score — starts at 0, +1000 per correct answer
    val hasAnswered: Boolean // whether they answered the current question
)

/*
 * Represents a full game room with all its players.
 *
 * This is what we receive when we:
 *   - POST /api/rooms/create  → creates and returns a room
 *   - POST /api/rooms/join    → joins and returns the updated room
 *   - GET  /api/rooms/{code}  → fetches the current room state
 *
 * Example JSON:
 * {
 *   "id": 2,
 *   "roomCode": "5CA321",
 *   "hostName": "Ibrahim",
 *   "status": "WAITING",
 *   "players": [ {...}, {...} ],
 *   "currentQuestionIndex": 0
 * }
 */
data class RoomDto(
    val id: Long,                        // room's database ID
    val roomCode: String,                // 6-character code players use to join
    val hostName: String,                // name of whoever created the room
    val status: String,                  // "WAITING", "IN_PROGRESS", or "FINISHED"
    val players: List<PlayerDto>,        // all players currently in this room
    val currentQuestionIndex: Int        // which question (0-based) is currently active
)

// ════════════════════════════════════════════════════════════════
// 3. WEBSOCKET DTOs — received over WebSocket during gameplay
// ════════════════════════════════════════════════════════════════

/*
 * Represents one MCQ question pushed by the server over WebSocket.
 *
 * When the host starts the game, the server builds questions from
 * players' fun facts and pushes them one at a time to all players.
 *
 * WebSocket topic: /topic/game/{roomCode}/question
 *
 * Example JSON received:
 * {
 *   "statement": "I have visited 30 countries",
 *   "options": ["Ali", "Sara", "Ahmed", "Bilal"],
 *   "correctAnswer": "Sara",
 *   "questionIndex": 0,
 *   "totalQuestions": 3
 * }
 *
 * The Android app shows:
 *   - statement as the question card text
 *   - options as the 4 colored answer buttons
 *   - correctAnswer is used internally only (not shown before answering)
 *   - questionIndex + totalQuestions for the progress indicator "Q1/3"
 */
data class QuestionDto(
    val statement: String,      // the fun fact shown as the question
    val options: List<String>,  // 4 player names as answer choices (shuffled)
    val correctAnswer: String,  // the name of whoever wrote this fact
    val questionIndex: Int,     // 0-based index of this question
    val totalQuestions: Int     // total number of questions in this game
)

/*
 * Represents one player's entry in the score list.
 *
 * After each question, the server pushes an updated score list.
 * At the end of the game, the server pushes the final score list.
 *
 * WebSocket topics:
 *   /topic/game/{roomCode}/scores   → after each question
 *   /topic/game/{roomCode}/finished → at the end of the game
 *
 * Example JSON (it's a list of these):
 * [
 *   { "name": "Sara", "score": 2000 },
 *   { "name": "Ahmed", "score": 1000 },
 *   { "name": "Bilal", "score": 0 }
 * ]
 *
 * The list is sorted by score descending — highest score first.
 * The backend does the sorting so Android just displays in order.
 */
data class ScoreDto(
    val name: String,  // player's name
    val score: Int     // their total score at this point
)