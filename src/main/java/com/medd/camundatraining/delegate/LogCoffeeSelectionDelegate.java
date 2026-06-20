package com.medd.camundatraining.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * A Camunda Java Delegate responsible for logging the user's coffee preferences
 * to the Spring Boot application logs.
 * <p>
 * This delegate is registered as a Spring component with the name "logCoffeeSelectionDelegate".
 * </p>
 */
@Slf4j
@Component("logCoffeeSelectionDelegate")
public class LogCoffeeSelectionDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Retrieve coffee selection variables (with defaults if null)
        Boolean pureCoffee = (Boolean) execution.getVariable("pureCoffee");
        Boolean withMilk = (Boolean) execution.getVariable("withMilk");
        Boolean moreSugar = (Boolean) execution.getVariable("moreSugar");

        // Format and log the final result
        StringBuilder order = new StringBuilder("☕ [COFFEE ORDER] ");
        order.append("Pure Coffee: ").append(pureCoffee != null && pureCoffee ? "YES" : "NO");
        order.append(" | With Milk: ").append(withMilk != null && withMilk ? "YES" : "NO");
        order.append(" | More Sugar: ").append(moreSugar != null && moreSugar ? "YES" : "NO");

        log.info(order.toString());
    }
}
