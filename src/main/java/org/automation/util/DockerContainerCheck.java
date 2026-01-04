package org.automation.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DockerContainerCheck {

    public static boolean isContainerRunning(String containerName) {
        try {
            Process process = new ProcessBuilder(
                    "docker", "inspect",
                    "-f", "{{.State.Running}}",
                    containerName
            ).start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String output = reader.readLine();
            return "true".equals(output);

        } catch (Exception e) {
            return false; // container not found or Docker not running
        }
    }

    public static void main(String[] args) {
        String container = "selenium-chrome1";

        if (isContainerRunning(container)) {
            System.out.println(container + " is running");
        } else {
            System.out.println(container + " is NOT running");
        }
    }
}
