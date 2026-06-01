package com.fgm.gestion.positionnettebatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PositionnetteBatchConfig {

    @Bean
    public Step positionnetteStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  PositionnetteTasklet tasklet) {

        return new StepBuilder("positionnetteStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Job positionnetteJob(JobRepository jobRepository, Step positionnetteStep) {

        return new JobBuilder("positionnetteJob", jobRepository)
                .start(positionnetteStep)
                .build();
    }
}