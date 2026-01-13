package org.automation.records;

public record DriverConfig(
        String browserType,
        String implicitWait,
        String driverType,
        Boolean isDocker,
        String dockerUrl,
        String dockerContainerName
){
    public String getBrowserType() {
        return browserType;
    }
}