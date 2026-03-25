// Defines the package location for this file within the data/network layer.
package OOP.Project.Frontend.data.network

// Imports the Data Transfer Objects (DTOs) used for request and response mapping.
import OOP.Project.Frontend.data.dto.CreateRoomRequest
import OOP.Project.Frontend.data.dto.JoinRoomRequest
import OOP.Project.Frontend.data.dto.RoomDto
// Imports Retrofit annotations like @POST, @GET, @Body, and @Path.
import retrofit2.http.*

/*
 * ApiService defines ALL the HTTP endpoints your app communicates with.
 *
 * WHAT IS AN INTERFACE?
 * An interface defines WHAT methods exist but not HOW they work.
 * Here we define what API calls exist — Retrofit generates
 * the actual implementation at runtime using reflection.
 *
 * HOW RETROFIT READS THIS:
 * When you call RetrofitClient.retrofit.create(ApiService::class.java),
 * Retrofit scans every method and its annotations, then generates
 * a class that actually performs those HTTP calls.
 *
 * WHAT IS "suspend"?
 * suspend marks a function as a COROUTINE function.
 * It means the function can be paused and resumed without blocking.
 * Instead of freezing the UI while waiting for a network response,
 * the coroutine pauses, lets the UI keep running, then resumes
 * when the response arrives.
 *
 * You MUST call suspend functions from inside a coroutine
 * (like viewModelScope.launch { ... }).
 */

/**
 * Interface defining the HTTP endpoints for communicating with the Spring Boot backend.
 */
interface ApiService {

    /*
    * Creates a new game room.
    *
    * @POST("api/rooms/create")
    *   Sends an HTTP POST request to:
    *   http://10.0.2.2:8080/api/rooms/create
    *
    * @Body request: CreateRoomRequest
    *   @Body tells Retrofit to convert the CreateRoomRequest object
    *   to JSON and send it in the request body.
    *   { "hostName": "Ibrahim" }
    *
    * : RoomDto
    *   The return type tells Retrofit what to convert the response to.
    *   Retrofit receives JSON → Gson converts it to a RoomDto object.
    */
    // Sends a POST request to create a room; converts the Request object into a JSON body.
    @POST("api/room/create")
    suspend fun createRoom(@Body request: CreateRoomRequest): RoomDto

    /*
     * Joins an existing room with a player's name and fun fact.
     *
     * @POST("api/rooms/join")
     *   POST to http://10.0.2.2:8080/api/rooms/join
     *
     * @Body request: JoinRoomRequest
     *   Sends: { "roomCode": "ABC123", "playerName": "Sara", "funFact": "..." }
     *
     * Returns the updated RoomDto with the new player added.
     */

    // Sends a POST request to join a room; the 'suspend' keyword allows non-blocking network calls.
    @POST("api/room/join")
    suspend fun joinRoom(@Body request: JoinRoomRequest): RoomDto

    /*
     * Fetches the current state of a room.
     *
     * @GET("api/rooms/{code}")
     *   Sends a GET request. {code} is a URL placeholder.
     *
     * @Path("code") code: String
     *   @Path replaces {code} in the URL with the actual value passed in.
     *   If code = "ABC123", the URL becomes:
     *   http://10.0.2.2:8080/api/rooms/ABC123
     *
     * No @Body needed for GET requests — the data is in the URL itself.
     */
    // Sends a GET request where '{code}' is a dynamic variable in the URL path.
    @GET("api/room/{code}")
    suspend fun getRoom(@Path("code") code: String): RoomDto
}

/*
 * ApiServiceInstance provides a globally accessible singleton
 * instance of ApiService.
 *
 * WHY NOT CREATE ApiService DIRECTLY?
 * Retrofit.create() generates the implementation class using
 * reflection — this is somewhat expensive. Creating it once
 * and reusing everywhere is better practice.
 *
 * HOW "by lazy" WORKS:
 * The instance is NOT created when the app starts.
 * It is created the FIRST TIME ApiServiceInstance.instance is accessed.
 * After that, the same instance is returned every time.
 * This is called "lazy initialization".
 *
 * USAGE ANYWHERE IN THE APP:
 * val api = ApiServiceInstance.instance
 * val room = api.createRoom(CreateRoomRequest("Ibrahim"))
 */

/**
 * A Singleton object that provides a single, reusable instance of the ApiService.
 */
object ApiServiceInstance {

    // Uses 'by lazy' to initialize the service only when it is accessed for the first time.
    val instance: ApiService by lazy {

        // Uses the Retrofit engine to generate the implementation of the ApiService interface.
        RetrofitClient.retrofit.create(ApiService::class.java)
        //create(...): This is a Proxy factory. Retrofit looks at your interface ApiService,
        // sees the methods like createRoom, and dynamically creates a "hidden" Java class that knows how to execute those specific HTTP requests
        //ApiService::class.java: You are passing the "Blueprint" of your interface so Retrofit knows exactly what methods it needs to implement for you.
    }
}