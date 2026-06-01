package com.fgm.gestion.intermediairebatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.fgm.gestion.model.Intermediaire;


@Configuration
public class IntermediaireBatchConfig {

    @Bean
    public Step intermediaireStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  IntermediaireReader reader,
                                  IntermediaireProcessor processor,
                                  IntermediaireWriter writer) {

        return new StepBuilder("intermediaireStep", jobRepository).<Intermediaire, Intermediaire>chunk(100, transactionManager).reader(reader).processor(processor).writer(writer).build();
    }

    @Bean
    public Job intermediaireJob(JobRepository jobRepository, Step intermediaireStep, IntermediaireJobListener listener) {

        return new JobBuilder("intermediaireJob", jobRepository).listener(listener).start(intermediaireStep).build();
    }
}