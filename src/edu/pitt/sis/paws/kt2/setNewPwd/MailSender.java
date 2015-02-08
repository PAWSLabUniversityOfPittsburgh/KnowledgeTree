package edu.pitt.sis.paws.kt2.setNewPwd;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Instruction is here:
 * http://www.mkyong.com/java/javamail-api-sending-email-via-gmail-smtp-example/
 *
 */
public class MailSender {
	
	final static String USERNAME = "paws.lab.donotreply@gmail.com";
	final static String PASSWORD = "pawslab2015";
	
	public MailSender(String urlToSend, String sendTo) {
		// TODO Auto-generated constructor stub

		Properties props = new Properties();
		//Open Debug model
		//props.setProperty("mail.debug", "true");
		props.setProperty("mail.smtp.auth", "true");
		props.setProperty("mail.smtp.starttls.enable", "true");
		props.setProperty("mail.smtp.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.port", "587");
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USERNAME, PASSWORD);
			}
		  });
 
		try {
 
			Message message = new MimeMessage(session);
			String contentString="<h3>Dear Knowledge-Tree user:<h3>"
					+ "<p>Please click this link to reset your password:<p>"
					+ "<a href=http://"+urlToSend+">"+urlToSend+"</a>"
							+ "<br/><br/><br/>"
							+ "----------------------------------------------------------------<br/>"
							+ "<h4>From <a href="+"\"http://adapt2.sis.pitt.edu/kt/content/Show\">Knowledge-Tree</a><br>"
							+ "Developed by<a href=\"http://adapt2.sis.pitt.edu/wiki/Main_Page\">PAWS Lab</a></h4>"
							+ "<img src=http://adapt2.sis.pitt.edu/kt/assets/KnowledgeTreeLogo2.gif>";
			
			message.setFrom(new InternetAddress("paws.lab.donotreply@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(sendTo));
			message.setSubject("Reset your Knowledge-Tree account password");
			message.setContent(contentString,"text/html;charset=utf-8");
 
			Transport.send(message);
 
			System.out.println("Successfully send email to "+sendTo);
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static void main(String[] args) {
		MailSender ms=new MailSender("aa.com", "haz51@pitt.edu");
	}
}