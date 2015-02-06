package edu.pitt.sis.paws.kt2.addition;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender {
	public static void main(String[] args) throws MessagingException {
		String inpuUrlString="aa.com";
		Properties props = new Properties();
		// Open Debug model
		//props.setProperty("mail.debug", "true");
		// 发送服务器需要身份验证
		props.setProperty("mail.smtp.auth", "true");
		// 设置邮件服务器主机名
		props.setProperty("mail.host", "smtp.163.com");
		// 发送邮件协议名称
		props.setProperty("mail.transport.protocol", "smtp");
		
		// 设置环境信息
		Session session = Session.getInstance(props);
		
		// 创建邮件对象
		Message msg = new MimeMessage(session);
		//set Subject&Content
		String contentString="<h3>Dear Knowledge-Tree user:<h3>"
				+ "<p>Please click this link to reset your password:<p>"
				+ "<a href=http://"+inpuUrlString+">"+inpuUrlString+"</a>"
						+ "<br/><br/><br/>"
						+ "----------------------------------------------------------------<br/>"
						+ "<h4>From <a href="+"\"http://adapt2.sis.pitt.edu/kt/content/Show\">Knowledge-Tree</a><br>"
						+ "Developed by<a href=\"http://adapt2.sis.pitt.edu/wiki/Main_Page\">PAWS Lab</a></h4>"
						+ "<img src=http://adapt2.sis.pitt.edu/kt/assets/KnowledgeTreeLogo2.gif>";
		msg.setContent(contentString,"text/html;charset=utf-8");
		msg.setSubject("JavaMail测试");
		
		//set sender
		msg.setFrom(new InternetAddress("paws_donotreply@163.com"));
		//msg.setFrom(new InternetAddress("zenithda@163.com"));
		Transport transport = session.getTransport();
		// set mail user&password, user can both include "@163.com" and not
		//transport.connect("zenithda@163.com", "zouhaoda1313");
		transport.connect("paws_donotreply@163.com", "pawslab2015");
		// 发送邮件
		transport.sendMessage(msg, new Address[] {new InternetAddress("haz51@pitt.edu")});
		// 关闭连接
		transport.close();
	}

}