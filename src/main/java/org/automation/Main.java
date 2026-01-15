package org.automation;

import net.sf.jasperreports.engine.*;
import org.automation.util.TestReportGenerator;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws JRException {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

//        String jrxmlPath = "src/main/resources/TestResult.jrxml";
//        String jasperPath = "src/main/resources/TestResult.jasper";
//
//        JasperCompileManager.compileReportToFile(jrxmlPath, jasperPath);
//        System.out.println("Report compiled successfully.");

        TestReportGenerator generator = new TestReportGenerator();
        generator.generatePdfReport();
    }
}