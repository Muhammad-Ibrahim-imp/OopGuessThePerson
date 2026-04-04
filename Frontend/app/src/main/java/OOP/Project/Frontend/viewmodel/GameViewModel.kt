package OOP.Project.Frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import OOP.Project.Frontend.data.dto.*
import OOP.Project.Frontend.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel holds ALL game state and handles ALL communication with the backend.
// It survives screen rotations and configuration changes.
// All screens in the app share ONE instance of this ViewModel.
class GameViewModel : ViewModel() {

    // Repository is the ONLY layer that ViewModel talks to for backend data
    private val repository = GameRepository()
    private val gson = Gson() // Used to parse JSON strings into Kotlin objects

    // ── UI State ──────────────────────────────────────────────
    // StateFlows are observable state holders; Compose UI automatically
    // redraws when values change. Use private _xxx for mutable, public xxx for read-only.

    private val _room = MutableStateFlow<RoomDto?>(null)
    val room: StateFlow<RoomDto?> = _room

    private val _currentQuestion = MutableStateFlow<QuestionDto?>(null)
    val currentQuestion: StateFlow<QuestionDto?> = _currentQuestion

    private val _scores = MutableStateFlow<List<ScoreDto>>(emptyList())
    val scores: StateFlow<List<ScoreDto>> = _scores

    private val _gameFinished = MutableStateFlow(false)
    val gameFinished: StateFlow<Boolean> = _gameFinished

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Current player info — needed when submitting answers
    var myPlayerId: Long = -1
    var myName: String = ""

    // ── Actions ───────────────────────────────────────────────

    // Create a new game room as the host
    fun createRoom(hostName: String) {
        // viewModelScope.launch starts a coroutine bound to this ViewModel
        // Coroutine automatically cancels if ViewModel is destroyed (lifecycle-aware)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.createRoom(hostName) // Call repository to create room
                _room.value = response
                myName = hostName
                // Connect to WebSocket channels for real-time updates
                connectToRoom(response.roomCode)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create room: ${e.message}"
            } finally {
                _isLoading.value = false // Runs regardless of success/failure
            }
        }
    }

    // Join an existing game room as a player
    fun joinRoom(roomCode: String, playerName: String, funFact: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.joinRoom(roomCode, playerName, funFact)
                _room.value = response
                myName = playerName
                // Find our player ID safely:
                // 'find' returns the first player matching name (or null)
                // '?.id ?: -1' means if null, assign -1
                myPlayerId = response.players.find { it.name == playerName }?.id ?: -1
                // Connect to WebSocket channels for real-time updates
                connectToRoom(roomCode)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to join room: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Opens WebSocket and subscribes to all 3 game event channels
    private fun connectToRoom(roomCode: String) {
        // Calls Repository layer to establish the WebSocket connection
        repository.connectToRoom() // NOT recursion; different function in Repository

        // Channel 1: Receive new question from server
        repository.subscribe("/topic/game/$roomCode/question") { json ->
            // 'fromJson()' converts JSON string to Kotlin object
            // 'QuestionDto::class.java' provides Java class reference (required by Gson)
            _currentQuestion.value = gson.fromJson(json, QuestionDto::class.java)
        }

        // Channel 2: Receive score updates after each question
        repository.subscribe("/topic/game/$roomCode/scores") { json ->
            // TypeToken used because Gson cannot directly parse generic types like List<ScoreDto>
            // Preserves type info at runtime (avoids Java type erasure)
            val type = object : TypeToken<List<ScoreDto>>() {}.type
            _scores.value = gson.fromJson(json, type)
        }

        // Channel 3: Game finished — final leaderboard
        repository.subscribe("/topic/game/$roomCode/finished") { json ->
            val type = object : TypeToken<List<ScoreDto>>() {}.type
            _scores.value = gson.fromJson(json, type)
            _gameFinished.value = true // Update UI to show game is over
        }
    }

    // Host taps Start Game
    fun startGame() {
        // 'let' executes the block only if _room.value?.roomCode is NOT null
        // 'code' inside block is the safely unwrapped non-null roomCode
        _room.value?.roomCode?.let { code ->
            repository.send("/app/game/$code/start", "")
        }
    }

    // Player taps an answer button
    fun submitAnswer(answeredName: String) {
        _room.value?.roomCode?.let { code ->
            // Create AnswerRequest object and convert to JSON
            val body = gson.toJson(AnswerRequest(myPlayerId, answeredName))
            repository.send("/app/game/$code/answer", body)
        }
    }

    // Called by the UI after showing an error so it disappears
    fun clearError() {
        // Resets error state; UI observing _errorMessage will hide the error automatically
        _errorMessage.value = null
    }

    // Automatically called when ViewModel is destroyed (user leaves screen or app)
    override fun onCleared() {
        // Call parent cleanup
        super.onCleared()
        // Close WebSocket connection to avoid memory leaks and background work
        repository.disconnect()
    }
}