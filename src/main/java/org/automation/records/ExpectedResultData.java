package org.automation.records;

import java.util.Map;

public record ExpectedResultData(
        String testcaseId,
        String resultMessage, // e.g., "Welcome", "https://dashboard", "#logout"
        String[] additionalData
){}