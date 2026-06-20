package com.medd.camundatraining.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * REST Controller providing an endpoint to trigger/correlate a BPMN Message Event.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CoffeeController {

    private final RuntimeService runtimeService;

    /**
     * GET endpoint to correlate the 'Message_Interop' message event.
     * Can correlate to a specific process instance or correlate globally to all active subscriptions.
     * <p>
     * Example URL: http://localhost:8080/api/interop?processInstanceId=xyz
     * Or global:  http://localhost:8080/api/interop
     * </p>
     * 
     * @param processInstanceId Optional process instance ID to target a specific workflow instance.
     * @return Confirmation message of the correlation status.
     */
    @GetMapping("/api/interop")
    public String triggerInterop(@RequestParam(value = "processInstanceId", required = false) String processInstanceId) {
        log.info("📞 Received API request to correlate 'Message_Interop' message event.");

        if (processInstanceId != null && !processInstanceId.trim().isEmpty()) {
            // Correlate to a specific process instance
            try {
                runtimeService.createMessageCorrelation("Message_Interop")
                        .processInstanceId(processInstanceId)
                        .correlate();
                log.info("✅ Message_Interop correlated successfully for processInstanceId: {}", processInstanceId);
                return "Message 'Message_Interop' correlated successfully for instance: " + processInstanceId;
            } catch (Exception e) {
                log.error("❌ Failed to correlate message for instance {}: {}", processInstanceId, e.getMessage());
                return "Failed to correlate message: " + e.getMessage();
            }
        } else {
            // Correlate globally to all active subscriptions
            try {
                List<MessageCorrelationResult> results = runtimeService.createMessageCorrelation("Message_Interop")
                        .correlateAllWithResult();
                log.info("✅ Message_Interop correlated globally. Total instances affected: {}", results.size());
                return "Message 'Message_Interop' correlated globally! Total instances triggered: " + results.size();
            } catch (Exception e) {
                log.error("❌ Failed to correlate message globally: {}", e.getMessage());
                return "Failed to correlate message globally: " + e.getMessage();
            }
        }
    }
}
