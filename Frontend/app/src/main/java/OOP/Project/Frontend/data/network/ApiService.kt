// Defines the package location for this file within the data/network layer.
package OOP.Project.Frontend.data.network

// Imports the Data Transfer Objects (DTOs) used for request and response mapping.
import OOP.Project.Frontend.data.dto.CreateRoomRequest
import OOP.Project.Frontend.data.dto.JoinRoomRequest
import OOP.Project.Frontend.data.dto.RoomDto
// Imports Retrofit annotations like @POST, @GET, @Body, and @Path.
import retrofit2.http.*

/**
 * Interface defining the HTTP endpoints for communicating with the Spring Boot backend.
 */
interface ApiService {

    // Sends a POST request to create a room; converts the Request object into a JSON body.
    @POST("api/room/create")
    suspend fun createRoom(@Body request: CreateRoomRequest): RoomDto

    // Sends a POST request to join a room; the 'suspend' keyword allows non-blocking network calls.
    @POST("api/room/join")
    suspend fun joinRoom(@Body request: JoinRoomRequest): RoomDto

    // Sends a GET request where '{code}' is a dynamic variable in the URL path.
    @GET("api/room/{code}")
    suspend fun getRoom(@Path("code") code: String): RoomDto
}

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