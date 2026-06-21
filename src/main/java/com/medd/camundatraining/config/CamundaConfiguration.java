package com.medd.camundatraining.config;

import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Spring configuration class registering the custom ID generator with the Camunda Process Engine.
 */
@Configuration
public class CamundaConfiguration {

    @Bean
    public IdGenerator idGenerator() {
        return new CustomIdGenerator();
    }

    @Bean
    @Order(Ordering.DEFAULT_ORDER + 1)
    public AbstractCamundaConfiguration customIdGeneratorConfiguration(IdGenerator idGenerator) {
        return new AbstractCamundaConfiguration() {
            @Override
            public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
                processEngineConfiguration.setIdGenerator(idGenerator);
            }
        };
    }
}
