/*this file enables your backend to send and receive real-time messages efficiently,
like a chat system or a multiplayer game notification system.*/
package OOP.Project.Backend.config;


import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
// Enables WebSocket support with the STOMP sub-protocol.
// STOMP works like a pub/sub (publish/subscribe) system:
//   - Server publishes to a topic
//   - All clients subscribed to that topic receive the message instantly


@Configuration //@Configuration Tells Spring:“This class contains setup/configuration code.”
@EnableWebSocketMessageBroker //It enables: WebSocket support, STOMP protocol, Message broker system (pub/sub)
//WebSocket → keeps connection open for real-time communication
//STOMP → structures messages and enables routing + subscriptions
//Message Broker → distributes messages efficiently using pub/sub
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer { //WebSocketMessageBrokerConfigurer is a Spring interface that lets you customize how WebSocket + STOMP messaging works in your application.
    //It lets you define how clients connect, send messages, and receive real-time updates in a Spring WebSocket system.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { //registerStompEndpoints() defines the URLs where clients connect to start a WebSocket session and optionally enables fallback and security rules.
        // Android connects to this URL to establish a WebSocket connection:
        // ws://yourserver.com/ws
        // withSockJS() adds a fallback for environments that block WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // allow all origins (fine for development)
                .withSockJS();
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        // /queue is for user-specific messages (invite notifications)
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}