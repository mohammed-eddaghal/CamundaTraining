package com.medd.camundatraining;

import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This component automatically starts the "Simple Process" when the Spring Boot application starts.
 */
@Component
public class ProcessStartup {

    private static final Logger logger = LoggerFactory.getLogger(ProcessStartup.class);

    @Autowired
    private RuntimeService runtimeService;

    /**
     * Triggers the Simple Process automatically when the application is ready.
     * This method is called after the Spring application context is fully initialized.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startProcessAtStartup() {
        logger.info("=== Starting Simple Process automatically ===");

        try {
            // Start a process instance of "Process_Simple"
            var processInstance = runtimeService.startProcessInstanceByKey("Process_Simple");
            logger.info("Process started successfully!");
            logger.info("Process Instance ID: {}", processInstance.getId());
            logger.info("Process Definition ID: {}", processInstance.getProcessDefinitionId());
            logger.info("=== Process is now waiting for user task approval ===");
        } catch (Exception e) {
            logger.error("Error starting process: {}", e.getMessage(), e);
        }
    }
}

