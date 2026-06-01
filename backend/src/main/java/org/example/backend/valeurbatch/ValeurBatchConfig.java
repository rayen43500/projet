package com.fgm.gestion.valeurbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.fgm.gestion.model.Valeur;



@Configuration
public class ValeurBatchConfig {

    @Bean
    public Step valeurStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           ValeurReader reader,
                           ValeurProcessor processor,
                           ValeurWriter writer) {

        return new StepBuilder("valeurStep", jobRepository)
                .<Valeur, Valeur>chunk(10000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    @Bean
    public Job valeurJob(JobRepository jobRepository, Step valeurStep) {

        return new JobBuilder("valeurJob", jobRepository)
                .start(valeurStep)
                .build();
    }
}