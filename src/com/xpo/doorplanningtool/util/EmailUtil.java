package com.xpo.doorplanningtool.util;

import com.xpo.doorplanningtool.cnst.EmailConstants;
import com.xpo.doorplanningtool.vo.Email;
import org.apache.log4j.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailUtil {

	private static Logger logger = Logger.getRootLogger();
	
	private static Session session;
	
	private class SMTPAuthenticator extends Authenticator {
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(EmailConstants.SYSTEM_EMAIL.getValue(),EmailConstants.SYSTEM_EMAIL_PASSWORD.getValue());
		}
	}

	public EmailUtil() {
		Properties props = System.getProperties();
		props = new Properties();
		props.put("mail.smtp.user", EmailConstants.SYSTEM_EMAIL.getValue());
		props.put("mail.smtp.host", EmailConstants.SYSTEM_EMAIL_HOST.getValue());
		props.put("mail.smtp.port", EmailConstants.SYSTEM_EMAIL_PORT.getValue());
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.debug", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.socketFactory.port", EmailConstants.SYSTEM_EMAIL_PORT.getValue());
		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");

		SMTPAuthenticator auth = new SMTPAuthenticator();
		session = Session.getInstance(props, auth);
		session.setDebug(false);
	}

	public void sendJavaMail(String sic, String filename, String to_address, String shift_abbreviation)
	{
		String from = EmailConstants.SYSTEM_EMAIL.getValue();

		// Assuming you are sending email from localhost
		//String host = "CGOPRCL001.conway.prod.con-way.com";
		String host = EmailConstants.SYSTEM_EMAIL_HOST.getValue();
		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		try{
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO,
					new InternetAddress(to_address));

			// Set Subject: header field
			message.setSubject("Door Planning Tool " + shift_abbreviation);

			// Create the message part
			BodyPart messageBodyPart = new MimeBodyPart();

			// Fill the message
			messageBodyPart.setText(EmailConstants.SYSTEM_EMAIL_MESSAGE_BODY.getValue());

			// Create a multipar message
			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Part two is attachment
			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(filename);
			messageBodyPart.setDataHandler(new DataHandler(source));

			String new_file_name = "Door Planning Tool " + shift_abbreviation + ".xls";
			messageBodyPart.setFileName(new_file_name);
			multipart.addBodyPart(messageBodyPart);

			// Send the complete message parts
			message.setContent(multipart );

			// Send message
			Transport.send(message);
			System.out.println("Sent message successfully....");

		}catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}
	
	public static boolean sendEmail(Email email){
		
		MimeMessage msg;
		try {
			msg = prepareMsg(email);
			
			Transport transport = session.getTransport("smtps");
			transport.connect(EmailConstants.SYSTEM_EMAIL_HOST.getValue(), 465, "username", "password");
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();
			
		} catch (MessagingException e) {
			logger.error("Encountered MessagingException: ", e);
			return false;
		}
		
		return true;
	}
	
	private static MimeMessage prepareMsg(Email email) throws MessagingException {
		
		MimeMessage msg = new MimeMessage(session);
		msg.setText(email.getMessage());
		msg.setSubject(email.getSubject());
		msg.setFrom(new InternetAddress(email.getSender()));
		
		List<InternetAddress> toRecipients = new ArrayList<InternetAddress>();
		List<InternetAddress> ccRecipients = new ArrayList<InternetAddress>();
		List<InternetAddress> bccRecipients = new ArrayList<InternetAddress>();
		
		for (String toRecipient : email.getToRecipients()){
			try {
				InternetAddress address = new InternetAddress(toRecipient);
				toRecipients.add(address);
			} catch (AddressException e) {
				logger.error("Encountered AddressException on" + " " + toRecipient + " - ", e);
			}
		}
		
		msg.addRecipients(Message.RecipientType.TO, (InternetAddress[]) toRecipients.toArray());
		
		for (String ccRecipient : email.getCcRecipients()){
			try {
				InternetAddress address = new InternetAddress(ccRecipient);
				ccRecipients.add(address);
			} catch (AddressException e) {
				logger.error("Encountered AddressException on" + " " + ccRecipient + " - ", e);
			}
		}
		
		msg.addRecipients(Message.RecipientType.CC, (InternetAddress[]) ccRecipients.toArray());
		
		for (String bccRecipient : email.getCcRecipients()){
			try {
				InternetAddress address = new InternetAddress(bccRecipient);
				bccRecipients.add(address);
			} catch (AddressException e) {
				logger.error("Encountered AddressException on" + " " + bccRecipient + " - ", e);
			}
		}
		
		msg.addRecipients(Message.RecipientType.BCC, (InternetAddress[]) bccRecipients.toArray());
		
		return msg;
	}

}
