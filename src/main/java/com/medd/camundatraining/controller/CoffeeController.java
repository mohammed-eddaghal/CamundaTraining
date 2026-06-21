package com.medd.camundatraining.controller;

import com.medd.camundatraining.service.CoffeeInteropService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller providing endpoints for coffee preparation integration and notifications.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CoffeeController {

    private final CoffeeInteropService coffeeInteropService;

    /**
     * GET endpoint to correlate the 'Message_Interop' message event for the active process instance.
     *
     * @return Confirmation message of the correlation status.
     */
    @GetMapping("/api/interop")
    public String triggerInterop() {
        log.info("📞 Received REST API request to correlate 'Message_Interop' message event.");
        return coffeeInteropService.triggerInteropForActiveInstance();
    }

    /**
     * GET endpoint called by the message throw event in the process definition.
     *
     * @return Confirmation message.
     */
    @GetMapping("/api/notify-order-sent")
    public String notifyOrderSent() {
        log.info("📢 GET endpoint called: Coffee order message event has been sent!");
        return "Notification logged successfully!";
    }

    /**
     * GET endpoint to start a new instance of the "Simple Process" (Process_Simple).
     *
     * @return Confirmation message with the started instance ID.
     */
    @GetMapping("/api/start-simple-process")
    public String startSimpleProcess() {
        log.info("📞 Received REST API request to start 'Process_Simple'.");
        return coffeeInteropService.startSimpleProcess();
    }
}
