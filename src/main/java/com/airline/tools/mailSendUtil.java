package com.airline.tools;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class mailSendUtil {
    private MimeMessage mailmessage;
    private Session sendMailSession;
    private Transport transport;

    private String host;
    private int port;
    private String userName;
    private String password;
    private String auth;
    private Properties mailConfig = new Properties();

    public mailSendUtil() {
        try {
            InputStream in = mailSendUtil.class.getResourceAsStream("/mailConfig.properties");
            mailConfig.load(in);
            this.host = mailConfig.getProperty("mail.smtp.host");
            this.port = Integer.parseInt(mailConfig.getProperty("mail.smtp.port"));
            this.userName = mailConfig.getProperty("mail.smtp.username");
            this.password = mailConfig.getProperty("mail.smtp.password");
            this.auth = mailConfig.getProperty("mail.smtp.auth");
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMailSession = Session.getDefaultInstance(mailConfig, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(userName, password);
            }
        });
        mailmessage = new MimeMessage(sendMailSession);
    }

    public boolean sendHtmlMail (String receiver, String subject, String body) {
        try {
            mailmessage.setFrom(new InternetAddress(userName));
            mailmessage.setRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
            mailmessage.setSubject(subject);
            mailmessage.setSentDate(new java.util.Date());

            Multipart mainPart = new MimeMultipart();
            BodyPart html = new MimeBodyPart();
            html.setContent(body, "text/html; charset=utf-8");
            mainPart.addBodyPart(html);
            mailmessage.setContent(mainPart);
            mailmessage.saveChanges();
            transport = sendMailSession.getTransport("smtp");
            transport.connect(host, port, userName, password);
            transport.sendMessage(mailmessage, mailmessage.getAllRecipients());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
