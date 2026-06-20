package com.medd.camundatraining.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Helper bean called from execution/task listeners in the coffee preparation subprocess.
 * Marks tasks as completed and propagates the completion flags to the parent process.
 */
@Component("coffeePrepHelper")
public class CoffeePrepHelper {

    private static final Logger log = LoggerFactory.getLogger(CoffeePrepHelper.class);

    /**
     * Marks a coffee preparation task as completed and propagates the flag to the parent process scope.
     *
     * @param execution    The execution context of the subprocess task.
     * @param variableName The completion variable name (e.g. 'pureCoffeeDone', 'milkDone', 'sugarDone').
     */
    public void markTaskDone(DelegateExecution execution, String variableName) {
        log.info("🎯 Marking coffee task '{}' as done.", variableName);
        execution.setVariable(variableName, true);
        
        // Retrieve the parent process instance ID via the historic process instance
        String processInstanceId = execution.getProcessInstanceId();
        HistoricProcessInstance hpi = execution.getProcessEngineServices().getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (hpi != null && hpi.getSuperProcessInstanceId() != null) {
            String superProcessInstanceId = hpi.getSuperProcessInstanceId();
            log.info("↗️ Propagating '{}' to parent process instance: {}", variableName, superProcessInstanceId);
            execution.getProcessEngineServices().getRuntimeService().setVariable(superProcessInstanceId, variableName, true);
        }
    }
}
