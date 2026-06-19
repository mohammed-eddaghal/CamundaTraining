package com.medd.camundatraining.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A Camunda Java Delegate responsible for generating and sending today's date
 * to the process context.
 * <p>
 * This delegate is registered as a Spring component with the name "sendTodayDateDelegate".
 * It calculates the current local system date, formats it to "yyyy-MM-dd",
 * and stores it as a process variable named {@code todayDate}.
 * </p>
 */
@Slf4j
@Component("sendTodayDateDelegate")
public class SendTodayDateDelegate implements JavaDelegate {

    /**
     * Executes the delegate logic when the process instance reaches the Service Task.
     * Sets the {@code todayDate} process variable with the current system date.
     * 
     * @param execution The process instance context of the execution.
     * @throws Exception If any error occurs during Java Delegate execution.
     */
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Set a process variable containing today's date
        execution.setVariable("todayDate", today);
        
        log.info("📬 SendTodayDateDelegate: Set process variable 'todayDate' to: {}", today);
    }
}
