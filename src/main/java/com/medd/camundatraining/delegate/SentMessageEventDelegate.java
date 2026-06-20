package com.medd.camundatraining.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Delegate executed when the MessageThrowEvent_OrderSent is triggered.
 * Performs a REST GET request to the notification endpoint.
 */
@Component("sentMessageEventDelegate")
public class SentMessageEventDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(SentMessageEventDelegate.class);

    @Autowired
    private Environment environment;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("✉️ Throwing Message Event: Sending coffee order notification...");

        // Retrieve port, defaulting to 8080
        String port = environment.getProperty("local.server.port");
        if (port == null) {
            port = environment.getProperty("server.port", "8080");
        }

        String url = "http://localhost:" + port + "/api/notify-order-sent";
        log.info("🌐 Sending GET request to endpoint: {}", url);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Response Status Code: {}", response.statusCode());
            log.info("Response Body: {}", response.body());
        } catch (Exception e) {
            log.error("Failed to execute REST call to {}: {}", url, e.getMessage());
            // Gracefully handle connectivity errors during unit tests or mock runs
        }
    }
}
