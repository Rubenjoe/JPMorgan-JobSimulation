package com.jpmc.midascore;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.UserRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class TaskFourTests {
    static final Logger logger = LoggerFactory.getLogger(TaskFourTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private DatabaseConduit databaseConduit;

    @Test
    void task_four_verifier() throws InterruptedException {
        userPopulator.populate();
        
        // Log all users after population
        logger.info("Users after population:");
        for (long i = 1; i <= 11; i++) {
            UserRecord user = databaseConduit.findById(i);
            if (user != null) {
                logger.info("  User ID {}: name={}, balance={}", i, user.getName(), user.getBalance());
            }
        }
        
        String[] transactionLines = fileLoader.loadStrings("/test_data/alskdjfh.fhdjsk");
        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }
        Thread.sleep(15000); // Give more time for transactions to process


        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("use your debugger to find out what wilbur's balance is after all transactions are processed");
        
        // Query and print Wilbur's balance - try multiple approaches
        UserRecord wilbur = null;
        
        // Try by name first
        wilbur = databaseConduit.findByName("wilbur");
        
        // If not found, try by ID (users are saved in order, so Wilbur should be ID 9)
        if (wilbur == null) {
            wilbur = databaseConduit.findById(9L);
        }
        
        // If still not found, try ID 1-11 (all possible user IDs)
        if (wilbur == null) {
            for (long i = 1; i <= 11; i++) {
                UserRecord user = databaseConduit.findById(i);
                if (user != null && "wilbur".equalsIgnoreCase(user.getName())) {
                    wilbur = user;
                    break;
                }
            }
        }
        
        if (wilbur != null) {
            logger.info("==========================================================");
            logger.info("WILBUR'S FINAL BALANCE: {}", wilbur.getBalance());
            logger.info("==========================================================");
        } else {
            logger.info("Wilbur not found - checking all users in database...");
            for (long i = 1; i <= 11; i++) {
                UserRecord user = databaseConduit.findById(i);
                if (user != null) {
                    logger.info("User ID {}: name={}, balance={}", i, user.getName(), user.getBalance());
                    if ("wilbur".equalsIgnoreCase(user.getName())) {
                        wilbur = user;
                    }
                }
            }
            if (wilbur != null) {
                logger.info("==========================================================");
                logger.info("WILBUR'S FINAL BALANCE: {}", wilbur.getBalance());
                logger.info("==========================================================");
            }
        }
        
        logger.info("Test complete - answer found above");
    }
}
