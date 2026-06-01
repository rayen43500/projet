package com.fgm.gestion.mouvementbancairebatch;

import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MouvementBancaireBatchConfig {

    private final MouvementBancaireTasklet mouvementBancaireTasklet;

    public MouvementBancaireBatchConfig(MouvementBancaireTasklet mouvementBancaireTasklet) {
        this.mouvementBancaireTasklet = mouvementBancaireTasklet;
    }

    @Bean
    public Step mouvementBancaireStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("mouvementBancaireStep", jobRepository)
                .tasklet(mouvementBancaireTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job mouvementBancaireJob(JobRepository jobRepository,
                                    Step mouvementBancaireStep) {
        return new JobBuilder("mouvementBancaireJob", jobRepository)
                .start(mouvementBancaireStep)
                .build();
    }
}