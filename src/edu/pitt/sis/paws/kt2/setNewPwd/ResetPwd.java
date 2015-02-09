package edu.pitt.sis.paws.kt2.setNewPwd;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.pitt.sis.paws.core.utils.SQLManager;

public class ResetPwd extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private SQLManager sqlManager=new SQLManager("java:comp/env/jdbc/portal");
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
	 	//super.doPost(req, resp);
	 	doGet(req, resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		//super.doGet(req, resp);
		
		String emailString=req.getParameter("email");
		String uuidString=req.getParameter("uuid");
		System.out.println("ResetPwd Servlet: "+emailString+" "+uuidString);
		String qryString="";
		Boolean isValid=false;
		
		try {
			Connection connection=sqlManager.getConnection();
			Statement statement=connection.createStatement();
			qryString="SELECT * FROM email_uuid WHERE EMail="+'"'+emailString+'"';
			ResultSet rSet=statement.executeQuery(qryString);
			if (rSet.next()) {
				String uuidInDB=rSet.getString("uuid");
				if (uuidInDB.equalsIgnoreCase(uuidString)) {
					isValid=true;
				}else {
					isValid=false;
				}
			}
			
			if (isValid==true) {
				req.setAttribute("email", emailString);
				RequestDispatcher requestDispatcher=req.getRequestDispatcher("/resetPwdPage.jsp");
				requestDispatcher.forward(req, resp);
			}else {
				RequestDispatcher requestDispatcher=req.getRequestDispatcher("/forgotPwdPage.jsp");
				requestDispatcher.forward(req, resp);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
