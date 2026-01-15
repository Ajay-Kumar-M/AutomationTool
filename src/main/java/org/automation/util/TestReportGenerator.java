package org.automation.util;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRCsvDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Complete example of generating JasperReports from CSV data
 * Framework independent - works with any custom test runner
 */
public class TestReportGenerator {

    // Configuration
    private static final String REPORT_TEMPLATE = "TestResult.jrxml";
    private static final String CSV_DATA_FILE = "result/automationResult.csv";
    private static final String OUTPUT_DIR = "result";
    private static final String OUTPUT_PDF = "result/test_report.pdf";
    private static final String OUTPUT_HTML = "result/test_report.html";
    private static final String OUTPUT_EXCEL = "result/test_report.xlsx";

    /**
     * Generate PDF report from CSV data
     */
    public void generatePdfReport() throws JRException {
        System.out.println("Generating PDF Report...");
        JasperPrint jasperPrint = fillReportFromCsv();
        JasperExportManager.exportReportToPdfFile(jasperPrint, OUTPUT_PDF);
        System.out.println("✓ PDF Report saved to: " + OUTPUT_PDF);
        terminateJasperThreads();
    }

    /**
     * Generate HTML report from CSV data
     */
    public void generateHtmlReport() throws JRException {
        System.out.println("Generating HTML Report...");
        JasperPrint jasperPrint = fillReportFromCsv();
        JasperExportManager.exportReportToHtmlFile(jasperPrint, OUTPUT_HTML);
        System.out.println("✓ HTML Report saved to: " + OUTPUT_HTML);
    }

    /**
     * Generate Excel report from CSV data
     */
    public void generateExcelReport() throws JRException {
        System.out.println("Generating Excel Report...");
        JasperPrint jasperPrint = fillReportFromCsv();
        // Note: XLS export requires additional library
        // Use JasperExportManager for basic Excel support
        // For advanced Excel features, use JRXlsxExporter
        System.out.println("✓ Excel Report generation requires JRXlsxExporter (optional dependency)");
    }

    /**
     * Core method: Load CSV, compile template, fill report
     */
    private JasperPrint fillReportFromCsv() throws JRException {
        System.out.println("Step 1: Loading JRXML template...");
        // Load the JRXML template from classpath
//        InputStream templateStream = getClass().getClassLoader().getResourceAsStream(REPORT_TEMPLATE);
//
//        if (templateStream == null) {
//            throw new JRException("Template not found: " + REPORT_TEMPLATE);
//        }
//
//        // Compile JRXML template
//        System.out.println("Step 2: Compiling JRXML template...");
//        JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
//        System.out.println("✓ Template compiled successfully");
//
        try {
            // Load CSV data
            System.out.println("Step 3: Loading CSV data source...");
            JRCsvDataSource csvDataSource = loadCsvDataSource();
            System.out.println("✓ CSV data loaded");

//            InputStream jasperStream = getClass().getResourceAsStream("TestResult.jasper");
            InputStream jasperStream = getClass().getClassLoader().getResourceAsStream("TestResult.jasper");

            if (jasperStream == null) {
                throw new RuntimeException("TestResult.jasper not found in classpath");
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);

            Map<String, Object> params = new HashMap<>();
            params.put("CSV_FILE_PATH", "result/automationResult.csv");

            // Fill report with data
            System.out.println("Step 4: Filling report with data...");
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    params,
                    csvDataSource
            );
            System.out.println("✓ Report filled successfully");

            return jasperPrint;
        } catch (Exception e) {
            System.out.println("Exception occurred - "+e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Load and configure CSV data source
     */
    private JRCsvDataSource loadCsvDataSource() throws JRException {
        JRCsvDataSource csvDataSource = null;
        String csvFilePath = "result/automationResult.csv";
        try {
            File csvFile = new File(csvFilePath);
            if (!csvFile.exists()) {
                throw new JRException("CSV file not found: " + csvFile.getAbsolutePath());
            }
            InputStream csvStream = new FileInputStream(csvFile);
            if (csvStream == null) {
                throw new JRException("CSV file not found: " + CSV_DATA_FILE);
            }
            // Create CSV data source
            csvDataSource = new JRCsvDataSource(csvStream, "UTF-8");
            // Configure CSV parsing
            csvDataSource.setUseFirstRowAsHeader(true);  // First row contains column names
            csvDataSource.setRecordDelimiter("\n");      // Line delimiter
            csvDataSource.setFieldDelimiter(',');     // Field delimiter
            return csvDataSource;
        } catch (Exception e) {
            System.out.println("Unable to load csv "+e.getMessage());
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return csvDataSource;
    }

    private void terminateJasperThreads() {
        Thread[] threads = new Thread[Thread.activeCount()];
        int count = Thread.enumerate(threads);

        for (int i = 0; i < count; i++) {
            Thread t = threads[i];
            if (t != null && t.isAlive() &&
                    (t.getName().contains("Jasper") ||
                            t.getName().contains("AWT") ||
                            t.getName().contains("Graphics"))) {
                if (!t.isDaemon()) {
                    t.setDaemon(true);  // Mark as daemon retroactively
                }
            }
        }
    }

    /**
     * Main entry point
     */
    public static void main(String[] args) {
        // Ensure output directory exists
        new File(OUTPUT_DIR).mkdirs();
        try {
            TestReportGenerator generator = new TestReportGenerator();
            // Generate all report formats
            generator.generatePdfReport();
//            generator.generateHtmlReport();
            System.out.println("\n✓ All reports generated successfully!");
            System.out.println("Check the 'output' directory for generated reports.");
        } catch (JRException e) {
            System.err.println("✗ Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/*
//            File input = new File("result/automationResult.csv");
//            File output = new File("result/automationResultUpdated.csv");
//            try (BufferedReader reader = new BufferedReader(new FileReader(input));
//                 BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    writer.write(line.replace("[", "(").replace("]",")"));
//                    writer.newLine();
//                }
//            }
 */
/*
//        Path path = Paths.get(REPORT_TEMPLATE);
//        if (Files.isReadable(path)) {
//            System.out.println("File exists and is readable");
//            System.out.println(path.toAbsolutePath());
//        } else {
//            System.out.println("File not found");
//        }
 */
/*
        // ✅ DEBUG: Print field names
        try {

//            System.out.println("✓ CSV Fields detected: " + java.util.Arrays.toString(fieldNames));

            // Try to read first record to verify data exists
//            if (csvDataSource.next()) {
//                Map<String, Integer> fieldNames = csvDataSource.getColumnNames();
//                System.out.println("printing csv headers - size:"+fieldNames.size());
//                for (Map.Entry<String, Integer> entry : fieldNames.entrySet()) {
//                    String key = entry.getKey();
//                    Integer value = entry.getValue();
//                    System.out.println(key + " = " + value);
//                }
//                System.out.println("✓ CSV has data - first record read successfully");
//                // Get value from first field
//                Object firstValue = csvDataSource.getFieldValue(new net.sf.jasperreports.engine.JRField() {
//                            @Override
//                            public Object clone() {
//                                return null;
//                            }
//                            @Override
//                            public boolean hasProperties() {
//                                return false;
//                            }
//                            @Override
//                            public JRPropertiesMap getPropertiesMap() {
//                                return null;
//                            }
//                            @Override
//                            public JRPropertiesHolder getParentProperties() {
//                                return null;
//                            }
//                            @Override
//                            public String getName() { return "TestCaseID"; }
//                            @Override
//                            public Class<?> getValueClass() { return String.class; }
//                            @Override
//                            public String getValueClassName() {
//                                return "";
//                            }
//                            @Override
//                            public JRPropertyExpression[] getPropertyExpressions() {
//                                return new JRPropertyExpression[0];
//                            }
//                            @Override
//                            public String getDescription() { return null; }
//                            @Override
//                            public void setDescription(String description) {}
//                        }
//                );
//                System.out.println("✓ First field value: " + firstValue);
//            } else {
//                System.out.println("✗ ERROR: CSV has no data or cannot read first record!");
//            }

            // IMPORTANT: Rewind the data source for actual report filling
//            csvDataSource.moveToFirstRecord();

        } catch (Exception e) {
            System.err.println("✗ Error reading CSV: " + e.getMessage());
            e.printStackTrace();
        }
 */