package com.medd.camundatraining;

import com.medd.camundatraining.controller.CoffeeController;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CoffeeProcessIntegrationTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CoffeeController coffeeController;

    @Test
    void testFullCoffeePrepWithCancellation() {
        // 1. Start the Presence Process
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process_Presence");
        assertNotNull(processInstance);
        String processInstanceId = processInstance.getId();

        // 2. Find and complete the "Verify Presence" task
        Task presenceTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey("UserTask_Presence")
                .singleResult();
        assertNotNull(presenceTask);
        taskService.complete(presenceTask.getId(), Map.of("isPresent", "yes"));

        // 3. Find and complete the "Choose Coffee Options" task
        Task coffeeSelectionTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey("UserTask_CoffeeSelection")
                .singleResult();
        assertNotNull(coffeeSelectionTask);

        // Select all three options so we activate all inclusive branches
        taskService.complete(coffeeSelectionTask.getId(), Map.of(
                "pureCoffee", true,
                "withMilk", true,
                "moreSugar", true
        ));

        // 4. Verify that the call activity is running and the subprocess tasks are active
        // Because of the inclusive split gateway, we should have 3 active user tasks in the subprocess:
        // "Pour Pure Coffee", "Add Milk to Coffee", "Add Extra Sugar"
        List<Task> subtasks = taskService.createTaskQuery()
                .taskAssignee("admin")
                .list();

        assertTrue(subtasks.stream().anyMatch(t -> "UserTask_PureCoffee".equals(t.getTaskDefinitionKey())));
        assertTrue(subtasks.stream().anyMatch(t -> "UserTask_AddMilk".equals(t.getTaskDefinitionKey())));
        assertTrue(subtasks.stream().anyMatch(t -> "UserTask_AddSugar".equals(t.getTaskDefinitionKey())));

        // 5. Trigger the interop message via the REST GET controller directly
        String response = coffeeController.triggerInterop(processInstanceId);
        assertEquals("Message 'Message_Interop' correlated successfully for instance: " + processInstanceId, response);

        // 6. Verify that the subprocess tasks are cancelled
        long remainingSubtasksCount = taskService.createTaskQuery()
                .taskDefinitionKeyIn("UserTask_PureCoffee", "UserTask_AddMilk", "UserTask_AddSugar")
                .count();
        assertEquals(0, remainingSubtasksCount);

        // 7. Verify that the process has moved to the "Coffee Preparation Interrupted" task
        Task interruptedTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey("UserTask_CoffeeCancelled")
                .singleResult();
        assertNotNull(interruptedTask);
        assertEquals("Coffee Preparation Interrupted", interruptedTask.getName());

        // 8. Complete the interrupted task to return to the subprocess
        taskService.complete(interruptedTask.getId(), Map.of("finishWork", true));

        // 9. Verify that the process has returned to the Call Activity and recreated the subprocess tasks
        List<Task> recreatedTasks = taskService.createTaskQuery()
                .taskAssignee("admin")
                .list();
        assertTrue(recreatedTasks.stream().anyMatch(t -> "UserTask_PureCoffee".equals(t.getTaskDefinitionKey())));
        assertTrue(recreatedTasks.stream().anyMatch(t -> "UserTask_AddMilk".equals(t.getTaskDefinitionKey())));
        assertTrue(recreatedTasks.stream().anyMatch(t -> "UserTask_AddSugar".equals(t.getTaskDefinitionKey())));

        // 10. Complete all coffee prep tasks to proceed to completion
        for (Task t : recreatedTasks) {
            taskService.complete(t.getId());
        }

        // 11. Assert that the process instance is now finished
        long instanceCount = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .count();
        assertEquals(0, instanceCount);
    }
}
