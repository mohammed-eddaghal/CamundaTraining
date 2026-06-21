package com.medd.camundatraining.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.springframework.stereotype.Service;

/**
 * Service class handling interop coffee preparation boundary message correlation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoffeeInteropService {

    private final RuntimeService runtimeService;

    /**
     * Correlates the 'Message_Interop' message event specifically to the dedicated active
     * process instance currently waiting for it.
     *
     * @return Confirmation message of the correlation status.
     */
    public String triggerInteropForActiveInstance() {
        log.info("📞 Service received request to trigger interop correlation.");

        // Find the first active event subscription for 'Message_Interop'
        EventSubscription subscription = runtimeService.createEventSubscriptionQuery()
                .eventName("Message_Interop")
                .eventType("message")
                .list()
                .stream()
                .findFirst()
                .orElse(null);

        if (subscription == null) {
            log.warn("⚠️ No active process instance found waiting for 'Message_Interop'");
            return "No active process instance is currently waiting for coffee cancellation (Message_Interop).";
        }

        String processInstanceId = subscription.getProcessInstanceId();
        log.info("🔍 Found waiting subscription on process instance: {}", processInstanceId);

        try {
            runtimeService.createMessageCorrelation("Message_Interop")
                    .processInstanceId(processInstanceId)
                    .correlate();
            log.info("✅ Message_Interop correlated successfully for processInstanceId: {}", processInstanceId);
            return "Message 'Message_Interop' correlated successfully for dedicated instance: " + processInstanceId;
        } catch (Exception e) {
            log.error("❌ Failed to correlate message for instance {}: {}", processInstanceId, e.getMessage());
            return "Failed to correlate message for instance " + processInstanceId + ": " + e.getMessage();
        }
    }

    /**
     * Starts a new process instance of the "Simple Process" (Process_Simple).
     *
     * @return Confirmation message with the started instance ID.
     */
    public String startSimpleProcess() {
        log.info("🚀 Starting new instance of 'Process_Simple' via interop service.");
        try {
            var processInstance = runtimeService.startProcessInstanceByKey("Process_Simple");
            log.info("✅ Started 'Process_Simple' instance: {}", processInstance.getId());
            return "Process 'Process_Simple' started successfully! Instance ID: " + processInstance.getId();
        } catch (Exception e) {
            log.error("❌ Failed to start 'Process_Simple': {}", e.getMessage());
            return "Failed to start process 'Process_Simple': " + e.getMessage();
        }
    }
}
