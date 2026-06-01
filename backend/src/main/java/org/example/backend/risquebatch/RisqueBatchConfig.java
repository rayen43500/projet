package com.fgm.gestion.risquebatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class RisqueBatchConfig {

    @Bean
    public Step risqueStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           RisqueTasklet tasklet) {

        return new StepBuilder("risqueStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Job risqueJob(JobRepository jobRepository, Step risqueStep) {

        return new JobBuilder("risqueJob", jobRepository)
                .start(risqueStep)
                .build();
    }
}