package OOP.Project.Frontend.data.network

import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

/*
 * WebSocketManager manages the persistent WebSocket connection
 * between the Android app and the Spring Boot backend.
 *
 * WHAT IS WEBSOCKET?
 * HTTP is like sending a letter — you send a request, wait, get a response.
 * WebSocket is like a phone call — the connection stays open and BOTH sides
 * can send messages to each other at any time.
 *
 * WHY DO WE NEED WEBSOCKET FOR THIS GAME?
 * During the game, the server needs to PUSH events to all players:
 *   - "Here is the next question" → sent to all players simultaneously
 *   - "Here are the updated scores" → sent when everyone answers
 *   - "Game is over" → sent when all questions are done
 *
 * With HTTP alone, the app would have to constantly ask "anything new?"
 * every second (polling) which is wasteful. WebSocket lets the server
 * push data instantly when something happens.
 *
 * WHAT IS STOMP?
 * STOMP (Simple Text Oriented Messaging Protocol) is a protocol that
 * runs ON TOP of WebSocket. It adds pub/sub (publish/subscribe) features.
 *
 * PUB/SUB MODEL:
 *   - Server PUBLISHES messages to a TOPIC (like a radio station)
 *   - Clients SUBSCRIBE to topics they care about (like tuning a radio)
 *   - All subscribers receive the message simultaneously
 *
 * Example:
 *   Server publishes → /topic/game/ABC123/question → { statement: "...", options: [...] }
 *   All 4 players subscribed to that topic receive the question at the same time
 *
 * WHAT IS RxJava / CompositeDisposable?
 * The Stomp library uses RxJava for its reactive streaming.
 * When you subscribe to a topic, you get back a "Disposable" —
 * a handle to that subscription that lets you cancel it later.
 * CompositeDisposable holds all your Disposables in one container
 * so you can cancel ALL subscriptions with one call: disposables.clear()
 *
 * @param serverUrl the base URL of your Spring Boot server
 *                  e.g. "http://10.0.2.2:8080"
 */
class WebSocketManager(private val serverUrl: String) {

    /*
     * StompClient is the main object from the StompProtocolAndroid library.
     * It handles:
     *   - Opening the WebSocket connection
     *   - Sending STOMP frames (messages)
     *   - Subscribing to topics
     *   - Heartbeating to keep the connection alive
     *
     * "lateinit" means we promise to initialize it before using it.
     * We can't initialize it in the constructor because we first need
     * to build the WebSocket URL (which we do in connect()).
     */
    private lateinit var stompClient: StompClient

    /*
     * CompositeDisposable is like a bag that holds all active subscriptions.
     *
     * When you call stompClient.topic("/topic/...").subscribe(...),
     * you get back a Disposable representing that subscription.
     * We add it to this bag with disposables.add(sub).
     *
     * When the player leaves the game, we call disposables.clear()
     * which cancels every subscription at once — preventing memory leaks
     * and stopping the app from receiving messages it no longer needs.
     */
    private val disposables = CompositeDisposable()

    /*
     * Opens the WebSocket connection to the Spring Boot server.
     *
     * Must be called BEFORE subscribe() or send().
     *
     * HOW THE URL IS BUILT:
     * serverUrl = "http://10.0.2.2:8080"
     * After replace: "ws://10.0.2.2:8080"
     * After + "/ws/websocket": "ws://10.0.2.2:8080/ws/websocket"
     *
     * WHY /ws/websocket?
     * Spring Boot's WebSocket endpoint is registered at "/ws" with SockJS.
     * SockJS adds "/websocket" to establish a native WebSocket connection.
     *
     * Stomp.over(ConnectionProvider.OKHTTP, wsUrl)
     *   Creates a StompClient that uses OkHttp as the underlying
     *   WebSocket transport. OkHttp is the same library Retrofit uses.
     *
     * stompClient.connect()
     *   Actually opens the TCP connection to the server.
     *   After this, the connection stays open until disconnect() is called.
     */
    fun connect() {
        val wsUrl = serverUrl.replace("http", "ws") + "/ws/websocket"
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)
        stompClient.connect()
    }

    /*
     * Subscribes to a server topic and receives messages from it.
     *
     * HOW IT WORKS:
     * 1. stompClient.topic(topic) creates an Observable (RxJava stream)
     *    that emits a new value every time the server publishes to that topic
     * 2. .subscribe { message -> ... } sets up a listener
     *    The lambda runs every time a new message arrives
     * 3. message.payload contains the raw JSON string sent by the server
     * 4. We pass this JSON string to onMessage so the caller can parse it
     *
     * @param topic     the STOMP topic path to subscribe to
     *                  e.g. "/topic/game/ABC123/question"
     * @param onMessage callback lambda called with the raw JSON string
     *                  every time the server publishes to this topic
     *
     * EXAMPLE USAGE:
     * wsManager.subscribe("/topic/game/ABC123/question") { json ->
     *     val question = gson.fromJson(json, QuestionDto::class.java)
     *     _currentQuestion.value = question
     * }
     */
    fun subscribe(topic: String, onMessage: (String) -> Unit) {
        val subscription = stompClient.topic(topic)
            .subscribe { message ->
                onMessage(message.payload) // payload is the raw JSON string
            }
        // Add to our bag so we can cancel it later
        disposables.add(subscription)
    }

    /*
     * Sends a message TO the server over the WebSocket connection.
     *
     * Used for:
     *   - Starting the game: send("/app/game/ABC123/start", "")
     *   - Submitting an answer: send("/app/game/ABC123/answer", jsonBody)
     *
     * @param destination the STOMP destination on the server
     *                    Must match @MessageMapping in GameController.java
     *                    e.g. "/app/game/ABC123/answer"
     * @param body        the message body as a JSON string
     *                    e.g. {"playerId":1,"answeredPlayerId":"Sara"}
     *
     * .subscribe() at the end is required by RxJava —
     * without it, the send operation would never actually execute.
     * RxJava is "lazy" — nothing happens until someone subscribes.
     */
    fun send(destination: String, body: String) {
        stompClient.send(destination, body).subscribe()
    }

    /*
     * Closes the WebSocket connection and cancels all subscriptions.
     *
     * WHEN TO CALL THIS:
     * Called from GameViewModel.onCleared() which runs automatically
     * when the user permanently leaves the game (presses back, closes app).
     *
     * WHY IS CLEANUP IMPORTANT?
     * Without cleanup:
     *   - Old subscriptions keep receiving messages (memory leak)
     *   - The WebSocket connection stays open consuming battery
     *   - Callbacks might fire on destroyed UI causing crashes
     *
     * disposables.clear()
     *   Cancels every subscription stored in our CompositeDisposable bag.
     *   The server can still publish to those topics, but we stop receiving.
     *
     * ::stompClient.isInitialized
     *   The :: operator gets a reference to the property.
     *   isInitialized checks if a lateinit var has been assigned a value.
     *   We check this because disconnect() might be called before connect()
     *   in edge cases (app crashes, user backs out before connecting).
     */
    fun disconnect() {
        disposables.clear()
        if (::stompClient.isInitialized) {
            stompClient.disconnect()
        }
    }
}