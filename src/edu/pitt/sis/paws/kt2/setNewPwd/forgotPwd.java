package edu.pitt.sis.paws.kt2.setNewPwd;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.pitt.sis.paws.core.utils.SQLManager;


public class forgotPwd extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	private SQLManager sqlManager=new SQLManager("java:comp/env/jdbc/portal");
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		boolean emailIsInDB=false;
		boolean sentStatus=false;
		
		String EmailString=request.getParameter("email");
		String urlSentString=new String();
		System.out.println("Input email: "+EmailString);
		try {
			//Firstly, see if that email is in ent_user
			Connection connection=sqlManager.getConnection();
			String qryString="SELECT * FROM ent_user WHERE EMail="+'"'+EmailString+'"';
			//System.out.println(qryString);
			Statement stmtStatement=connection.createStatement();
			ResultSet rSet=stmtStatement.executeQuery(qryString);
			while (rSet.next()) {
				emailIsInDB=true;
			}


			if (emailIsInDB==true) {
				//Generate UUID
				UUIDGenerator uuidGenerator=new UUIDGenerator();
				String uuidString=uuidGenerator.getUUID();
				String handlePageString="/ResetPwd";//This is the page to handle url
				//Save Email, UUID to DB
				//test if that email has already been in mail_uuid table
				qryString="SELECT * FROM email_uuid WHERE email="+"'"+EmailString+"'";
				//System.out.println(qryString);
				rSet=stmtStatement.executeQuery(qryString);
				if (rSet.next()) {
					qryString="UPDATE email_uuid SET uuid="+"'"+uuidString+"'"+" WHERE email=\'"+EmailString+"'";
					//System.out.println(qryString);
				}else {
					qryString="INSERT INTO email_uuid VALUES("+'"'+EmailString+'"'+", "+"'"+uuidString+"')";
					//System.out.println(qryString);
				}
			
				//System.out.println(qryString);
				int influencedLine=stmtStatement.executeUpdate((qryString));
				if(influencedLine>0){
					//make Message sent to User Email
					urlSentString=request.getServerName()+":"+request.getServerPort()+ request.getContextPath()
							+handlePageString+"?email="+EmailString+"&uuid="+uuidString;
					//System.out.println(urlSentString);
					
					//SENT EMAIl
					MailSender mailSender=new MailSender(urlSentString, EmailString);
					sentStatus=true;
				} 
				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		PrintWriter out = response.getWriter(); 
		System.out.println(emailIsInDB+", "+sentStatus);
		out.print("{\"emailIsInDB\":\"" + emailIsInDB+"\",\"sentStatus\":"+"\""+sentStatus+"\"}");
		emailIsInDB=false;
		out.flush();
		out.close();
	}
}
