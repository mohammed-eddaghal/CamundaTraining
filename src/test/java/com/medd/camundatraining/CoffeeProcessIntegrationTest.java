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
    void testFullCoffeePrepWithTaskMemoryAndCancellation() {
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
                .taskDefinitionKeyIn("UserTask_PureCoffee", "UserTask_AddMilk", "UserTask_AddSugar")
                .list();

        assertEquals(3, subtasks.size());
        Task pureCoffeeTask = subtasks.stream()
                .filter(t -> "UserTask_PureCoffee".equals(t.getTaskDefinitionKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("UserTask_PureCoffee not found"));
        
        // 5. Complete ONE of the coffee prep tasks (Pour Pure Coffee) to test the memory feature
        taskService.complete(pureCoffeeTask.getId());

        // 6. Trigger the interop message via the REST GET controller without passing processInstanceId
        // The service will dynamically find the active subscription on the processInstanceId and correlate it
        String response = coffeeController.triggerInterop();
        assertTrue(response.contains("Message 'Message_Interop' correlated successfully for dedicated instance"));

        // 7. Verify that the remaining subprocess tasks (Milk and Sugar) are cancelled
        long remainingSubtasksCount = taskService.createTaskQuery()
                .taskDefinitionKeyIn("UserTask_PureCoffee", "UserTask_AddMilk", "UserTask_AddSugar")
                .count();
        assertEquals(0, remainingSubtasksCount);

        // 8. Verify that the process has moved to the "Coffee Preparation Interrupted" task
        Task interruptedTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey("UserTask_CoffeeCancelled")
                .singleResult();
        assertNotNull(interruptedTask);
        assertEquals("Coffee Preparation Interrupted", interruptedTask.getName());

        // 9. Complete the interrupted task to return to the subprocess
        taskService.complete(interruptedTask.getId(), Map.of("finishWork", true));

        // 10. Verify that the process has returned to the Call Activity
        // and ONLY recreated the incomplete tasks (withMilk and moreSugar)
        // while "Pour Pure Coffee" is skipped because of the memory (pureCoffeeDone == true)
        List<Task> recreatedTasks = taskService.createTaskQuery()
                .taskDefinitionKeyIn("UserTask_PureCoffee", "UserTask_AddMilk", "UserTask_AddSugar")
                .list();
        
        assertEquals(2, recreatedTasks.size());
        assertFalse(recreatedTasks.stream().anyMatch(t -> "UserTask_PureCoffee".equals(t.getTaskDefinitionKey())));
        assertTrue(recreatedTasks.stream().anyMatch(t -> "UserTask_AddMilk".equals(t.getTaskDefinitionKey())));
        assertTrue(recreatedTasks.stream().anyMatch(t -> "UserTask_AddSugar".equals(t.getTaskDefinitionKey())));

        // 11. Complete the remaining coffee prep tasks to proceed to completion
        for (Task t : recreatedTasks) {
            taskService.complete(t.getId());
        }

        // 12. Assert that the process instance is now finished
        long instanceCount = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .count();
        assertEquals(0, instanceCount);
    }
}
