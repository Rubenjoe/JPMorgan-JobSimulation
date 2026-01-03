package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {

    private final TransactionProcessor transactionProcessor;

    public TransactionListener(TransactionProcessor transactionProcessor) {
        this.transactionProcessor = transactionProcessor;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "transaction-processor")
    public void consume(Transaction transaction) {
        transactionProcessor.process(transaction);
    }
}

