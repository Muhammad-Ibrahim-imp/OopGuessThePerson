package OOP.Project.Frontend.data.repository

import OOP.Project.Frontend.data.dto.*
import OOP.Project.Frontend.data.network.ApiServiceInstance
import OOP.Project.Frontend.data.network.WebSocketManager

/*
 * GameRepository is the SINGLE SOURCE OF TRUTH for all data in the app.
 *
 * WHAT IS THE REPOSITORY PATTERN?
 * In Clean Architecture, the Repository sits between the ViewModel
 * and the data sources (network, database, cache).
 *
 * WITHOUT Repository pattern:
 *   ViewModel → directly calls Retrofit → directly calls WebSocket
 *   Problem: ViewModel knows too much about HOW data is fetched.
 *            If you change the API, you must update the ViewModel.
 *
 * WITH Repository pattern:
 *   ViewModel → Repository → (Retrofit OR WebSocket OR Database)
 *   Benefit: ViewModel only knows WHAT data it needs, not HOW to get it.
 *            If you change the API, you only update the Repository.
 *            The ViewModel never changes.
 *
 * ANALOGY:
 * You (ViewModel) want pizza.
 * You call the restaurant (Repository): "I want a pizza".
 * The restaurant handles everything — ordering ingredients, cooking, delivery.
 * You don't care if they changed their oven or supplier.
 * You just get your pizza.
 *
 * This separation also makes testing easier — you can replace the
 * Repository with a fake one that returns test data without needing
 * a real server.
 */
class GameRepository {

    /*
     * ApiServiceInstance.instance gives us the Retrofit-generated
     * ApiService implementation.
     *
     * "by lazy" in ApiServiceInstance means it is only created once
     * (the first time it is accessed) and reused after that.
     */
    private val api = ApiServiceInstance.instance

    /*
     * One WebSocketManager instance per GameRepository.
     * Manages the single WebSocket connection for the whole game session.
     *
     * The URL must match your Spring Boot server.
     * 10.0.2.2 = your PC's localhost as seen from the Android emulator.
     */
    private val wsManager = WebSocketManager("http://10.0.2.2:8080")

    // ════════════════════════════════════════════════════════════
    // REST API METHODS
    // These correspond to HTTP calls defined in ApiService.kt
    // ════════════════════════════════════════════════════════════

    /*
     * Asks the backend to create a new game room.
     *
     * "suspend" means this function must be called from inside
     * a coroutine (e.g. viewModelScope.launch { ... }).
     * It suspends (pauses) the coroutine while waiting for the
     * HTTP response, without blocking the main UI thread.
     *
     * @param hostName  the name of whoever is creating the room
     * @return RoomDto  the newly created room with its generated room code
     *
     * WHAT HAPPENS INTERNALLY:
     * 1. We create a CreateRoomRequest with the hostName
     * 2. Retrofit converts it to JSON: { "hostName": "Ibrahim" }
     * 3. Retrofit sends POST http://10.0.2.2:8080/api/rooms/create
     * 4. Spring Boot creates the room, saves to MySQL, returns room JSON
     * 5. Gson converts the response JSON to a RoomDto object
     * 6. We return that RoomDto to whoever called this function
     */
    suspend fun createRoom(hostName: String): RoomDto {
        return api.createRoom(CreateRoomRequest(hostName))
    }

    /*
     * Asks the backend to add this player to an existing room.
     *
     * @param roomCode    the 6-character code the host shared
     * @param playerName  the player's real name
     * @param funFact     their interesting fact (becomes MCQ statement)
     * @return RoomDto    the updated room now including this new player
     *
     * If roomCode doesn't exist, the backend throws an exception
     * which becomes an Exception here — caught in the ViewModel.
     */
    suspend fun joinRoom(roomCode: String, playerName: String, funFact: String): RoomDto {
        return api.joinRoom(JoinRoomRequest(roomCode, playerName, funFact))
    }

    /*
     * Fetches the latest state of a room.
     *
     * @param roomCode  the room to fetch
     * @return RoomDto  current room state including all players and scores
     *
     * Useful for refreshing the lobby player list.
     */
    suspend fun getRoom(roomCode: String): RoomDto {
        return api.getRoom(roomCode)
    }

    // ════════════════════════════════════════════════════════════
    // WEBSOCKET METHODS
    // These delegate to WebSocketManager
    // ════════════════════════════════════════════════════════════

    /*
     * Opens the WebSocket connection to the server.
     *
     * Must be called once after joining/creating a room,
     * before any subscribe() or send() calls.
     *
     * This is a regular function (not suspend) because
     * connecting a WebSocket doesn't block — it just
     * initiates the connection asynchronously.
     */
    fun connectToRoom() {
        wsManager.connect()
    }

    /*
     * Subscribes to a STOMP topic to receive real-time messages.
     *
     * The Repository simply delegates to WebSocketManager here.
     * This delegation means:
     *   - ViewModel doesn't need to know WebSocketManager exists
     *   - If we switch WebSocket libraries, only this file changes
     *
     * @param topic     the topic path e.g. "/topic/game/ABC123/question"
     * @param onMessage lambda called with raw JSON every time server publishes
     */
    fun subscribe(topic: String, onMessage: (String) -> Unit) {
        wsManager.subscribe(topic, onMessage)
    }

    /*
     * Sends a message to the server over WebSocket.
     *
     * Used for:
     *   - Host starting the game: send("/app/game/CODE/start", "")
     *   - Players answering: send("/app/game/CODE/answer", jsonString)
     *
     * @param destination  STOMP destination matching @MessageMapping in backend
     * @param body         JSON string payload to send
     */
    fun send(destination: String, body: String) {
        wsManager.send(destination, body)
    }

    /*
     * Closes the WebSocket connection and cancels all subscriptions.
     *
     * Called from GameViewModel.onCleared() when the user exits.
     * Prevents memory leaks and unnecessary battery drain.
     */
    fun disconnect() {
        wsManager.disconnect()
    }
}