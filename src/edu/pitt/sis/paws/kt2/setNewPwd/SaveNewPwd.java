package edu.pitt.sis.paws.kt2.setNewPwd;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.pitt.sis.paws.core.utils.SQLManager;

public class SaveNewPwd extends HttpServlet{
	
	private SQLManager sqlManager=new SQLManager("java:comp/env/jdbc/portal");
	String outputJsonString = new String();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		//super.doGet(req, resp);
		doPost(req, resp);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		//super.doPost(req, resp);
		
		String newPassword=req.getParameter("password");
		String emailString=req.getParameter("email");
		String	md5Password=md5(newPassword);
		//System.out.println(md5Password);
		try {
			Connection connection=sqlManager.getConnection();
			Statement statement=connection.createStatement();
			String qryString=String.format("UPDATE ent_user SET Pass='%s' WHERE EMail='%s'", md5Password,emailString);
			statement.execute(qryString);
			System.out.println(qryString);
			outputJsonString=String.format("{\"status\":\"%s\"}", "true");
			qryString=String.format("DELETE FROM email_uuid WHERE email='%s'", emailString);
			statement.execute(qryString);
			System.out.println(qryString);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			outputJsonString=String.format("{\"status\":\"%s\"}", e);
		}
		PrintWriter out = resp.getWriter();
		
		System.out.println(outputJsonString);
		out.write(outputJsonString);
		out.flush();
		out.close();
		
	}
	
	private String md5( String source ) {
		try {
			MessageDigest md = MessageDigest.getInstance( "MD5" );
			byte[] bytes = md.digest( source.getBytes("UTF-8") );
			return getBase16String( bytes );
		} catch( Exception e )	{
			e.printStackTrace();
			return null;
		}
	}
	
	private String getBase16String( byte[] bytes ) {
	  StringBuffer sb = new StringBuffer();
	  for( int i=0; i<bytes.length; i++ ) {
	     byte b = bytes[ i ];
	     String hex = Integer.toHexString((int) 0x00FF & b);
	     if (hex.length() == 1) {
	        sb.append("0");
	     }
	     sb.append( hex );
	  }
	  return sb.toString();
	}

}
