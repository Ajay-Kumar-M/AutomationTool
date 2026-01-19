package org.automation.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.automation.executor.PlaywrightDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import static org.automation.util.TestUtils.getResultDir;

public class SendReportEmail {

    private static final Properties config = new Properties();
    private static final Logger logger = LoggerFactory.getLogger(SendReportEmail.class);

    static {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("email.properties")) {
            if (is == null) {
                throw new RuntimeException("email.properties not found on classpath");
            }
            config.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load driver.properties", e);
        }
    }

    public static void sendMail() {
        // SMTP server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", config.getProperty("smtp.host", ""));
        props.put("mail.smtp.port", config.getProperty("smtp.port", ""));

        // Create session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        config.getProperty("smtp.username", ""),
                        config.getProperty("smtp.password", ""));
            }
        });

        try {
            // Create the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getProperty("smtp.username", "")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.getProperty("smtp.to", "")));
            message.setSubject("Automation Testcase Executed!");
            // Create multipart for body + attachments
            MimeMultipart multipart = new MimeMultipart();
            // Message body part
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Hello!\n\nAutomation testcase has been executed.\nCheck the attached report for detailed break-down.\n\nBest regards.");
            multipart.addBodyPart(messageBodyPart);
            // Attachment part(s) - add one or more
            File jasperReport = new File(getResultDir()+"/test_report.pdf");
            if (jasperReport.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(new File(jasperReport.getAbsolutePath()));
                multipart.addBodyPart(attachmentPart);
            }
            File allureReport = new File(getResultDir()+"/allure-report-single/index.html");
            if (allureReport.exists()) {
                MimeBodyPart attachmentPart2 = new MimeBodyPart();
                attachmentPart2.attachFile(new File(allureReport.getAbsolutePath()));
                multipart.addBodyPart(attachmentPart2);
            }
            // Set multipart content
            message.setContent(multipart);
            // Send email
            Transport.send(message);
            logger.info("Email with attachment sent successfully!");
        } catch (MessagingException e) {
            logger.error("Failed to send email: {}", e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args){
        sendMail();
    }
}