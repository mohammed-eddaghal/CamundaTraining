package com.medd.camundatraining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Camunda Training Spring Boot application.
 * This class bootstrapping the Spring application context and initializes the embedded Camunda Engine.
 */
@SpringBootApplication
public class CamundaTrainingApplication {

    /**
     * The main method that starts the Spring Boot application.
     * 
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(CamundaTrainingApplication.class, args);
    }

}
