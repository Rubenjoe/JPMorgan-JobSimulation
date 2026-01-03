package com.jpmc.midascore;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserPopulator {
    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private DatabaseConduit databaseConduit;

    public void populate() {
        String[] userLines = fileLoader.loadStrings("/test_data/lkjhgfdsa.hjkl");
        if (userLines == null) {
            System.err.println("UserPopulator: fileLines is null");
            return;
        }
        System.out.println("UserPopulator: Found " + userLines.length + " user lines");
        for (int i = 0; i < userLines.length; i++) {
            String userLine = userLines[i];
            if (userLine == null || userLine.trim().isEmpty()) {
                continue;
            }
            try {
                String[] userData = userLine.split(", ");
                if (userData.length < 2) {
                    System.err.println("UserPopulator: Line " + i + " has insufficient data: " + userLine);
                    continue;
                }
                // Trim and extract numeric part (handle cases with newlines/extra text)
                String balanceStr = userData[1].trim().split("\\s+")[0];
                String userName = userData[0].trim();
                float balance = Float.parseFloat(balanceStr);
                UserRecord user = new UserRecord(userName, balance);
                databaseConduit.save(user);
                System.out.println("UserPopulator: Saved user " + userName + " with balance " + balance);
            } catch (Exception e) {
                // Log and continue with next user
                System.err.println("Error processing user line " + i + ": " + userLine + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
