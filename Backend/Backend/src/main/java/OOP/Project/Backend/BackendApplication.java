package OOP.Project.Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication is the entry point of your entire backend.
// It does three things at once:
//   1. Marks this as a Spring configuration class
//   2. Enables autoconfiguration (Spring sets up Hibernate, Tomcat etc. automatically)
//   3. Tells Spring to scan OOP.Project.Backend and all sub-packages for
//      components like @Service, @Controller, @Repository
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// This one line starts the entire Spring Boot application:
		// boots Tomcat, connects to MySQL, sets up WebSocket — everything
		SpringApplication.run(BackendApplication.class, args);
	}
}