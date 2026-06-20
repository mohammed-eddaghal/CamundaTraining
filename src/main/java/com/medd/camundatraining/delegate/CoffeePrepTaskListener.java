package com.medd.camundatraining.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Robust Task Listener called upon user task completion inside the coffee prep subprocess.
 * Marks tasks as completed and propagates completion flags to the parent process scope.
 */
@Component("coffeePrepTaskListener")
public class CoffeePrepTaskListener implements TaskListener {

    private static final Logger log = LoggerFactory.getLogger(CoffeePrepTaskListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        String taskKey = delegateTask.getTaskDefinitionKey();
        String variableName;

        switch (taskKey) {
            case "UserTask_PureCoffee":
                variableName = "pureCoffeeDone";
                break;
            case "UserTask_AddMilk":
                variableName = "milkDone";
                break;
            case "UserTask_AddSugar":
                variableName = "sugarDone";
                break;
            default:
                log.warn("⚠️ Unknown task definition key in task listener: {}", taskKey);
                return;
        }

        log.info("🎯 Task Listener: Marking task '{}' ({}) as done.", taskKey, variableName);

        DelegateExecution execution = delegateTask.getExecution();
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
