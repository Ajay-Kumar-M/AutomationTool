package org.automation.records;

public record ExpectedResultRecord(
        String testcaseId,
        String actionType,
        String locator,
        String status,
        String resultMessage,
        String[] additionalData
){}