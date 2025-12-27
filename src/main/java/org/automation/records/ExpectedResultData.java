package org.automation.records;

import java.util.Map;

public record ExpectedResultData(
        String testcaseId,
        String actionType,
        String locator,
        String status,
        String resultMessage,
        String[] additionalData
){}