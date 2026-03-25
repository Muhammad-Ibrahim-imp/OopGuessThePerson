package OOP.Project.Frontend.data.network

// The engine that executes HTTP requests and handles connections, timeouts, and sockets.
import okhttp3.OkHttpClient

// A tool that "intercepts" network traffic to print request and response details in Logcat.
import okhttp3.logging.HttpLoggingInterceptor

// The main library that turns your Java/Kotlin interfaces into actual REST API calls.
import retrofit2.Retrofit

// A translator that automatically converts JSON data into Kotlin data classes (and vice versa).
import retrofit2.converter.gson.GsonConverterFactory

/*
 * RetrofitClient is a SINGLETON that configures and provides
 * the Retrofit HTTP client used for all REST API calls.
 *
 * WHAT IS RETROFIT?
 * Retrofit is a library that turns your interface (ApiService)
 * into actual HTTP calls. You define WHAT you want to call,
 * Retrofit figures out HOW to call it.
 *
 * WHAT IS A SINGLETON?
 * A singleton means only ONE instance of this object ever exists
 * in your entire app. In Kotlin, "object" keyword creates a singleton.
 *
 * Why singleton for Retrofit?
 * Creating a Retrofit instance is expensive (slow). Creating it once
 * and reusing it everywhere is much more efficient.
 *
 * HOW RETROFIT WORKS:
 * 1. You define an interface with @GET, @POST annotations (ApiService)
 * 2. Retrofit reads those annotations and generates real HTTP calls
 * 3. When you call api.createRoom(...), Retrofit:
 *    - Builds the URL: BASE_URL + "api/rooms/create"
 *    - Converts your data class to JSON (via Gson)
 *    - Sends the HTTP POST request
 *    - Receives the JSON response
 *    - Converts JSON back to your data class (via Gson)
 *    - Returns the result
 */
object RetrofitClient {
    /*
     * BASE_URL is the root URL of your Spring Boot backend.
     *
     * 10.0.2.2 is a special IP address that the Android emulator
     * uses to reach your development PC's localhost.
     *
     * Why not 127.0.0.1 or localhost?
     * Inside the Android emulator, 127.0.0.1 refers to the EMULATOR
     * itself, not your PC. 10.0.2.2 is the emulator's way of saying
     * "the PC that is running me".
     *
     * When you deploy to Railway, change this to:
     * private const val BASE_URL = "https://your-app.up.railway.app/"
     *
     * IMPORTANT: BASE_URL must always end with a forward slash "/"
     */
    private const val BASE_URL ="http://10.0.2.2:8080/"
    /*
     * HttpLoggingInterceptor sits between your app and the network.
     * Every time a request goes out or a response comes in,
     * it prints the full details to Android Studio's Logcat.
     *
     * Level.BODY means log:
     *   - The URL and HTTP method (GET, POST etc.)
     *   - All request headers
     *   - The full request body (JSON you sent)
     *   - The HTTP status code (200, 404 etc.)
     *   - The full response body (JSON you received)
     *
     * This is EXTREMELY useful when debugging — you can see exactly
     * what was sent and received without guessing.
     *
     * In production, change this to Level.NONE to stop logging.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply{ // apply {} is used for initializing objects in a clean, readable way.
        /*
         * .apply {} is a Kotlin scope function.
         * It runs the block of code on the object and returns the object.
         * This is equivalent to:
         *   val interceptor = HttpLoggingInterceptor()
         *   interceptor.level = HttpLoggingInterceptor.Level.BODY
         */
        level = HttpLoggingInterceptor.Level.BODY // Sets the logging detail level
    }

    /*
    * OkHttpClient is the underlying HTTP engine that Retrofit uses.
    * Think of OkHttp as the car engine — Retrofit is the steering wheel.
    *
    * We configure OkHttp with our logging interceptor so every
    * request/response is automatically logged.
    *
    * OkHttpClient.Builder() uses the Builder pattern:
    * - You chain configuration methods
    * - .build() creates the final configured object
    *
    * You can add more interceptors here later, for example:
    * - Auth interceptor to add Authorization headers
    * - Timeout interceptor to limit request time
    */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // attach our logger to every request
        .build()                            // create the final OkHttpClient

    /*
    * Retrofit is the main HTTP client we use throughout the app.
    *
    * Retrofit.Builder() also uses the Builder pattern.
    *
    * .baseUrl(BASE_URL)
    *   All relative URLs in ApiService are appended to this.
    *   "api/rooms/create" becomes "http://10.0.2.2:8080/api/rooms/create"
    *
    * .client(okHttpClient)
    *   Use our configured OkHttpClient (with logging) instead of default.
    *
    * .addConverterFactory(GsonConverterFactory.create())
    *   This tells Retrofit to use Gson for JSON conversion.
    *   Gson automatically:
    *   - Converts your Kotlin data class TO JSON when sending a request
    *   - Converts received JSON BACK TO your Kotlin data class
    *   You never have to manually parse JSON.
    *
    * val (not private val) because ApiService.kt needs to access this
    * to create the API service instance.
    */
    val retrofit : Retrofit = Retrofit.Builder()// Uses the Builder Pattern to separate the construction of the object from its representation.
        .baseUrl(BASE_URL) // Sets the root URL for the API (e.g., http://10.0.2.2:8080/ for your local Spring Boot).
        .client(okHttpClient) // Attaches the OkHttpClient that handles timeouts, logging, and interceptors
        .addConverterFactory(GsonConverterFactory.create()) // Tells Retrofit to use Gson to automatically convert JSON strings into Kotlin Data Objects.
        .build() // Finalizes the configuration and creates the immutable Retrofit object.
}