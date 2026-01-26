package org.automation.records;

public record LocatorData(
        String tagName,
        String text,
        String css,
        String xpath
) {}
