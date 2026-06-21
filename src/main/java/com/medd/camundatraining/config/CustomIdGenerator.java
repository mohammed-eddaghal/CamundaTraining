package com.medd.camundatraining.config;

import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom ID Generator for Camunda with call-stack print debugging.
 */
public class CustomIdGenerator implements IdGenerator {

    private static final Logger log = LoggerFactory.getLogger(CustomIdGenerator.class);

    private final AtomicLong processSequence = new AtomicLong(1000);
    private final AtomicLong taskSequence = new AtomicLong(1);
    private final AtomicLong defaultSequence = new AtomicLong(10000);
    private final AtomicInteger debugCounter = new AtomicInteger(0);

    @Override
    public String getNextId() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        int count = debugCounter.getAndIncrement();
        if (count < 25) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== STACK TRACE FOR ID GENERATION CALL #").append(count).append(" ===\n");
            for (int i = 0; i < Math.min(stackTrace.length, 20); i++) {
                sb.append("  [").append(i).append("] ")
                  .append(stackTrace[i].getClassName()).append(".")
                  .append(stackTrace[i].getMethodName()).append("\n");
            }
            log.info(sb.toString());
        }

        boolean isExecution = false;
        boolean isTask = false;

        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains("TaskManager")) {
                isTask = true;
            } else if (className.contains("ExecutionManager")) {
                isExecution = true;
            }
        }

        if (isTask) {
            String taskId = null;
            try {
                org.camunda.bpm.engine.impl.context.BpmnExecutionContext bpmnContext = 
                    org.camunda.bpm.engine.impl.context.Context.getBpmnExecutionContext();
                if (bpmnContext != null) {
                    org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity execution = bpmnContext.getExecution();
                    if (execution != null) {
                        String processInstanceId = execution.getProcessInstanceId();
                        String activityId = execution.getCurrentActivityId();
                        if (processInstanceId != null && !processInstanceId.isEmpty() && activityId != null && !activityId.isEmpty()) {
                            taskId = processInstanceId + "_" + activityId + "_" + taskSequence.getAndIncrement();
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to generate contextual task ID, falling back", e);
            }
            if (taskId == null) {
                taskId = "TASK_" + taskSequence.getAndIncrement();
            }
            return taskId;
        }

        if (isExecution) {
            return "ORDER_" + processSequence.getAndIncrement();
        }

        return "ID_" + defaultSequence.getAndIncrement();
    }
}
