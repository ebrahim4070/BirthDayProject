package com.birthday.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailUtil {

    public static void sendEmail(String toEmail, String subject, String body) {
        final String fromEmail = "ebrahimhossain4070@gmail.com"; // সঠিক ইমেইল
        final String password = "davhecxd dcrpmntk";             // App Password (স্পেস ছাড়া বসাও)
        // Gmail App password

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // Gmail SMTP server
        props.put("mail.smtp.port", "587");            // TLS port
        props.put("mail.smtp.auth", "true");           // Enable authentication
        props.put("mail.smtp.starttls.enable", "true");// Enable STARTTLS

        // Create session
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // Create email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "Birthday Manager"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);
            message.setText(body);

            // Send email
            Transport.send(message);
            System.out.println("✅ Email sent successfully to: " + toEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
