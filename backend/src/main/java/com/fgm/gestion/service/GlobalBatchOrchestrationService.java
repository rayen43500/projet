package com.fgm.gestion.service;

import com.fgm.gestion.repository.TransactionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Chaîne complète de batchs (identique à l'ancien {@code POST /api/global/run-all}).
 */
@Service
public class GlobalBatchOrchestrationService {

    private final JobLauncher jobLauncher;
    private final Job transactionJob;
    private final Job intermediaireJob;
    private final Job valeurJob;
    private final Job positionnetteJob;
    private final Job risqueJob;
    private final Job mouvementBancaireJob;
    private final BanqueEtatService banqueEtatService;
    private final AppelRestitutionService appelRestitutionService;
    private final MvtBanqueInterService mvtBanqueInterService;
    private final TransactionRepository transactionRepository;
    private final ApportInitialEnsurerService apportInitialEnsurer;

    public GlobalBatchOrchestrationService(
            JobLauncher jobLauncher,
            Job transactionJob,
            Job intermediaireJob,
            Job valeurJob,
            Job positionnetteJob,
            Job risqueJob,
            Job mouvementBancaireJob,
            BanqueEtatService banqueEtatService,
            AppelRestitutionService appelRestitutionService,
            MvtBanqueInterService mvtBanqueInterService,
            TransactionRepository transactionRepository,
            ApportInitialEnsurerService apportInitialEnsurer) {
        this.jobLauncher = jobLauncher;
        this.transactionJob = transactionJob;
        this.intermediaireJob = intermediaireJob;
        this.valeurJob = valeurJob;
        this.positionnetteJob = positionnetteJob;
        this.risqueJob = risqueJob;
        this.mouvementBancaireJob = mouvementBancaireJob;
        this.banqueEtatService = banqueEtatService;
        this.appelRestitutionService = appelRestitutionService;
        this.mvtBanqueInterService = mvtBanqueInterService;
        this.transactionRepository = transactionRepository;
        this.apportInitialEnsurer = apportInitialEnsurer;
    }

    public void runAll(
            MultipartFile transactionsFile,
            MultipartFile intermediairesFile,
            MultipartFile valeursFile,
            String seance) throws Exception {

        String tmpDir = System.getProperty("java.io.tmpdir");
        String pathTx = tmpDir + "/" + transactionsFile.getOriginalFilename();
        String pathInter = tmpDir + "/" + intermediairesFile.getOriginalFilename();
        String pathVal = tmpDir + "/" + valeursFile.getOriginalFilename();

        transactionsFile.transferTo(new File(pathTx));
        intermediairesFile.transferTo(new File(pathInter));
        valeursFile.transferTo(new File(pathVal));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(seance, formatter);
        String dateSeanceIso = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Même date de séance que l’import UI (yyyyMMdd) : aligne Mongo avec findBySeance(...)
        jobLauncher.run(transactionJob, new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("filePath", pathTx)
                .addString("seanceCompact", seance)
                .toJobParameters());

        jobLauncher.run(intermediaireJob, new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("filePath", pathInter)
                .addString("dateSeance", dateSeanceIso)
                .toJobParameters());

        jobLauncher.run(valeurJob, new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("filePath", pathVal)
                .addString("seanceCompact", seance)
                .toJobParameters());

        JobParameters params = new JobParametersBuilder()
                .addString("seance", seance)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(positionnetteJob, params);
        jobLauncher.run(risqueJob, params);

        apportInitialEnsurer.ensureForSeance(date, transactionRepository.findBySeance(date));

        jobLauncher.run(mouvementBancaireJob, params);

        banqueEtatService.generateFromMouvementBancaire(seance);
        appelRestitutionService.genererAppelRestitution(date);
        mvtBanqueInterService.genererMvtBanque(date);
    }
}