/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */
 
/**
 * @author Michael V. Yudelson
 */

package edu.pitt.sis.paws.kt2;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.pitt.sis.paws.core.Item;
import edu.pitt.sis.paws.core.ItemVector;

public class doAuthenticate extends HttpServlet 
{
	
	static final long serialVersionUID = -2L;

	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		// Check  for logoff option
		String logoff = req.getParameter("logoff");
		if(logoff != null)
		{
			// keep ensemble
			String is_ensemble = (String)req.getSession().getAttribute("ensemble");
			String ensemble = "";
			req.getSession().invalidate();
			if(is_ensemble!=null)
				req.getSession().setAttribute("ensemble", "1");
			ClientDaemon.forwardToURL(req, res, "/index.jsp");
			return;
		}
		// Get the authentication parameters
		String user_login = req.getRemoteUser();
		// Check the user login
		String qry = null;
		Connection conn = null;
		ResultSet rs1 = null;
		Statement stmt1 = null;
		ResultSet rs2 = null;
		Statement stmt2 = null;
		ClientDaemon cd = ClientDaemon.getInstance();
		
		// you don't need to re-search for user_id if you switch the group
		try
		{
			conn = cd.getSQLManager().getConnection();
			// Initialize session and resource map
			// Find and put user id \
			qry = "SELECT UserID, Name FROM ent_user WHERE Login='" +
				user_login + "'";
			stmt1 = conn.createStatement();
			rs1 = stmt1.executeQuery(qry);
			
			int user_id = 0;
			String user_name = null;
			if(rs1.next())
			{
				user_id = rs1.getInt("UserID");
				user_name = rs1.getString("Name");
			}
			rs1.close();
			rs1 = null;
			stmt1 .close();
			stmt1 = null;

			// Set user id session parameter
			HttpSession session = req.getSession();
			session.setAttribute(ClientDaemon.SESSION_USER_ID,
					new Integer(user_id));
			session.setAttribute(ClientDaemon.SESSION_USER_NAME,
					user_name);

			// Select the groups
			ItemVector<Item> groups = new ItemVector<Item>();
			Vector<Integer> group_ids = new Vector<Integer>();
			Vector<String> group_names = new Vector<String>();
			qry = "SELECT uu.ParentUserID, u.Name FROM rel_user_user uu" +
				" LEFT JOIN ent_user u ON(u.UserID=uu.ParentUserID) " +
				"WHERE uu.ChildUserID = " + user_id;
			stmt2 = conn.createStatement();
			rs2 = stmt2.executeQuery(qry);
			while(rs2.next())
			{
				int _id = rs2.getInt("ParentUserID");
				String _name = rs2.getString("Name");
				group_ids.add(_id);
				group_names.add(_name);
				groups.add(new Item(_id, _name));
			}
			rs2.close();
			rs2 = null;
			stmt2 .close();
			stmt2 = null;
			
			session.setAttribute(ClientDaemon.SESSION_GROUPS, groups);
			
			conn.close();
			conn = null;

			String forward = "";
			switch (group_ids.size())
			{
				case 0:
				{// No group associated
					forward = "/relogin.jsp?msg=You are not a member of any user" +
						" group! Please contact administrator.";
					ClientDaemon.forwardToURL(req, res, forward);
				}
				break;
				case 1:
				{// One group associated
					session.setAttribute(ClientDaemon.SESSION_GROUP_ID, group_ids.get(0));
					PrintWriter out = res.getWriter();
					req.setCharacterEncoding("utf-8");
					
					out.println("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN' 'http://www.w3.org/TR/html4/strict.dtd'>");
					out.println("<html><head>");
					out.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
					out.println("<link rel='icon' href='" + req.getContextPath() + "/assets/favicon.ico' type='image/x-icon'>");
					out.println("<link rel='shortcut icon' href='" + req.getContextPath() + "/assets/favicon.ico' type='image/x-icon'>"); 
					out.println("<title>Knowledge Tree. Select User Group</title>");

					out.println("</head><body>");
					out.println("<CENTER><img src='" + req.getContextPath()+ "/assets/KnowledgeTreeLogo2.gif' alt='Knowledge Tree Logo' align='middle'></CENTER>");
					out.println("<form name='group' action='" + req.getContextPath() + "/content/doGroup' method='post'>");
//					out.println("	<table border='0' cellspacing='2' cellpadding='2' align='center'>");
//					out.println("		<tr> ");
//					out.println("			<td width='80'>User group</td>");
//					out.println("			<td width='190'>");
					out.println("				<select name='" +  ClientDaemon.REQUEST_USER_GROUP + "'>");
					out.println("					<option value='"+group_ids.get(0)+"' selected>"+group_names.get(0)+"</option>");
					out.println("				</select>");
//					out.println("			</td> ");
//					out.println("		</tr>");
//					out.println("		<tr> ");
//					out.println("		  <td colspan='2'>");
					out.println("				<input type='Submit' value='Submit' id='Submit' />");
//					out.println("		  </td>");
//					out.println("		</tr>");
//					out.println("	</table>");
					out.println("</form>");

					out.println("<script type='text/javascript'>");
					out.println("	document.group.submit();");
					out.println("	document.getElementById('Submit').focus();");
					out.println("</script>");
					out.println("</body></html>");
					out.close();
					
				}
				break;
				default:
				{// Multiple groups associated
					PrintWriter out = res.getWriter();
					out.println("<html>");
					out.println("<head><title>Knowledge Tree. Select user group</title></head>");
					out.println("<body>");
					out.println("<CENTER>");
					out.println("<img src='" + req.getContextPath()+ "/assets/KnowledgeTreeLogo2.gif' alt='Knowledge Tree Logo' align='middle'>");
//					out.println("<H2 align='center'>User group</H2>");
//					out.println("<H4 align='center'>Please choose the user group you want to login into</H4>");
					out.println("<FORM ACTION='" + req.getContextPath() + "/content/doGroup' METHOD='POST' align='center'>");
//					out.println("	<table border='0' cellspacing='2' cellpadding='2' align='center'>");
//					out.println("		<tr> ");
//					out.println("			<td width='80'>User group</td>");
//					out.println("			<td width='190'>");
					out.println("				<div>User group / Class: <select name='" +  ClientDaemon.REQUEST_USER_GROUP + "'>");
					for(int i=0;i<group_ids.size();i++)
					{
						out.println("					<option value='"+group_ids.get(i)+"'>"+group_names.get(i)+"</option>");
					}
					out.println("				</select></div>");
//					out.println("			</td> ");
//					out.println("		</tr>");
//					out.println("		<tr> ");
//					out.println("		  <td colspan='2'>");
					out.println("				<input type='Submit' value='Submit' id='Submit' />");
//					out.println("		  </td>");
//					out.println("		</tr>");
//					out.println("	</table>");
					out.println("</FORM>");
					out.println("</CENTER>");
					out.println("<script language='javascript'>");
					out.println("	document.getElementById('Submit').focus();");
					out.println("</script>");

					out.println("</body></html>");
					out.close();
				}
				break;
			}
		}//end -- try
		catch (Exception e) { e.printStackTrace(System.err); }
		finally
		{
//			cd.getSQLManager().freeConnection(conn);
			if (rs1 != null) 
			{
				try { rs1.close(); } catch (SQLException e) { ; }
				rs1 = null;
			}
			if (rs2 != null) 
			{
				try { rs2.close(); } catch (SQLException e) { ; }
				rs2 = null;
			}
			if (stmt1 != null) 
			{
				try { stmt1.close(); } catch (SQLException e) { ; }
				stmt1 = null;
			}
			if (stmt2 != null) 
			{
				try { stmt2.close(); } catch (SQLException e) { ; }
				stmt2 = null;
			}
			if (conn != null)
			{
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}/**/
	}// -- end doGet
}
