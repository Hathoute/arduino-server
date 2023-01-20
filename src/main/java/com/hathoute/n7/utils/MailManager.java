package com.hathoute.n7.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MailManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(MailManager.class);

  private static MailManager instance;

  private final boolean enabled;
  private final Properties smtpProperties;
  private final Session session;
  private final String sender;
  private final InternetAddress[] recipients;

  private MailManager() throws AddressException {
    enabled = ConfigManager.getInstance().getBoolean("smtp.enabled");
    if(!enabled) {
        LOGGER.info("SMTP is disabled");
        smtpProperties = null;
        session = null;
        sender = null;
        recipients = null;
        return;
    }

    LOGGER.info("Setting up SMTP");
    var shouldAuthenticate = ConfigManager.getInstance().getBoolean("smtp.auth");

    smtpProperties = new Properties();
    smtpProperties.put("mail.smtp.host", ConfigManager.getInstance().getString("smtp.host"));
    smtpProperties.put("mail.smtp.port", ConfigManager.getInstance().getString("smtp.port"));
    smtpProperties.put("mail.smtp.auth", shouldAuthenticate);
    smtpProperties.put("mail.smtp.starttls.enable", ConfigManager.getInstance().getString("smtp.starttls.enable"));
    smtpProperties.put("mail.smtp.ssl.trust", ConfigManager.getInstance().getString("smtp.ssl.trust"));

    var authenticator = shouldAuthenticate ? new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(
          ConfigManager.getInstance().getString("smtp.username"),
          ConfigManager.getInstance().getString("smtp.password")
        );
      }
    } : null;

    session = Session.getInstance(smtpProperties, authenticator);

    sender = ConfigManager.getInstance().getString("smtp.mail.sender");
    var recipientsCsv = ConfigManager.getInstance().getString("smtp.mail.recipients");
    recipients = InternetAddress.parse(recipientsCsv);
  }

  public static MailManager getInstance() {
    return instance;
  }

  public static void initialize() throws AddressException {
    LOGGER.debug("Initializing MailManager");
    instance = new MailManager();
    LOGGER.debug("Finished initializing MailManager");
  }

  public void sendMail(final String subject, final String body) {
    LOGGER.debug("Sending mail with subject: {} and body: {}", subject, body);
    if(!enabled) {
        LOGGER.debug("SMTP is disabled, not sending mail");
        return;
    }

    try {
      final var message = new MimeMessage(session);
      message.setFrom(sender);
      message.setRecipients(MimeMessage.RecipientType.TO, recipients);
      message.setSubject(subject);

      var mimeBodyPart = new MimeBodyPart();
      mimeBodyPart.setContent(body, "text/html; charset=utf-8");
      var multipart = new MimeMultipart();
      multipart.addBodyPart(mimeBodyPart);
      message.setContent(multipart);

      Transport.send(message);
    } catch (MessagingException e) {
      LOGGER.error("Could not send mail (subject: {}, body: {})", subject, body, e);
    }
  }
}
