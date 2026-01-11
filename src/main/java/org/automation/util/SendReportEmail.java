package org.automation.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SendReportEmail {

    public static void sendMail(String attachmentPath) {
        // Sender's email credentials (use environment variables or secure storage in production!)
        final String username = "majayslm@gmail.com";  // Your full email address
        final String password = "hayc gngb yfzm lcsi";    // Gmail App Password (not regular password)

        // SMTP server properties (for Gmail; adjust for other providers)
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("ajay.kumar0495@gmail.com"));
            message.setSubject("Automation Testcase Executed!");
            // Create multipart for body + attachments
            MimeMultipart multipart = new MimeMultipart();
            // Message body part
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Hello!\n\nAutomation testcase has been executed.\nCheck the attached report for detailed break-down.\n\nBest regards.");
            multipart.addBodyPart(messageBodyPart);
            // Attachment part(s) - add one or more
            File file = new File("result/test_report.pdf");
            if (file.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                String filePath = "result/test_report.pdf";
                attachmentPart.attachFile(new File(filePath));
                multipart.addBodyPart(attachmentPart);
            }
            file = new File("result/allure-report-single/index.html");
            if (file.exists()) {
                MimeBodyPart attachmentPart2 = new MimeBodyPart();
                attachmentPart2.attachFile(new File("result/allure-report-single/index.html"));
                multipart.addBodyPart(attachmentPart2);
            }
            // Set multipart content
            message.setContent(multipart);
            // Send email
            Transport.send(message);
            System.out.println("Email with attachment sent successfully!");
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args){
        sendMail("");
    }
}