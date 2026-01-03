package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionProcessor {

    private final DatabaseConduit databaseConduit;
    private final IncentiveService incentiveService;

    public TransactionProcessor(DatabaseConduit databaseConduit,
            IncentiveService incentiveService) {
        this.databaseConduit = databaseConduit;
        this.incentiveService = incentiveService;
    }

    public void process(Transaction transaction) {

        // 1. Fetch users
        UserRecord sender = databaseConduit.findById(transaction.getSenderId());
        UserRecord recipient = databaseConduit.findById(transaction.getRecipientId());

        if (sender == null || recipient == null) {
            return;
        }

        float amount = transaction.getAmount();

        // 2. Validate balance
        if (sender.getBalance() < amount) {
            return;
        }

        // 3. Apply transaction
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        // 4. Call Incentive API
        Incentive incentive = null;
        try {
            incentive = incentiveService.getIncentive(transaction);
        } catch (Exception e) {
            // If incentive API fails, continue without incentive
        }

        if (incentive != null && incentive.getAmount() > 0) {
            float incentiveAmount = (float) incentive.getAmount();
            recipient.setBalance(
                    recipient.getBalance() + incentiveAmount);
        }

        // 5. Save updated users
        databaseConduit.save(sender);
        databaseConduit.save(recipient);
    }
}
