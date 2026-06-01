package com.fgm.gestion.transactionbatch;

import com.fgm.gestion.model.Transaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    public Step transactionStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                TransactionReader reader,
                                TransactionProcessor processor,
                                TransactionWriter writer) {

        return new StepBuilder("transactionStep", jobRepository)
                .<DataLine, Transaction>chunk(10000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }

    @Bean
    public Job transactionJob(JobRepository jobRepository, Step transactionStep) {
        return new JobBuilder("transactionJob", jobRepository)
                .start(transactionStep)
                .build();
    }
}