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

import java.io.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;


public class doEdit extends HttpServlet
{
	static final long serialVersionUID = -2L;

//	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
//	{
//		doPost(request, response);
//	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		request.setCharacterEncoding("utf-8");
		
		// first get the id of the node being edited
		String id_s = (String)request.getParameter(Node.NODE_FRMFIELD_ID);
		if(id_s == null)
		{
			System.out.println("!!! [KTree2] doEdit.doPost: ERROR Id not specified!");
			return;
		}
		
		int id = Integer.parseInt(id_s);
//System.out.println("doEdit.doPost node id = " + id);				
		HttpSession session = request.getSession();
		
		ResourceMap res_map = (ResourceMap)session.getAttribute(
			ClientDaemon.SESSION_RES_MAP);
		iNode edited_node = res_map.getNodes().findById(id);
		if(edited_node == null)
		{
			System.out.println("!!! [KTree2] doEdit.doPost: ERROR Id not found!");
			return;
		}
//System.out.println("doEdit.doPost NodeID="+id);

		String url = null;
		Connection conn = null;
		ClientDaemon cd = ClientDaemon.getInstance();
		
		try
		{
			conn = cd.getSQLManager().getConnection();
			
//String new_summary = request.getParameter(Summary.SUMMARY_FRMFIELD_SUMMARY);
//System.out.println("doEdit: new_summary='" + new_summary + "'");

			// 0. update own properties
			int changes = edited_node.updateObject(request);
			
			// 1. if the node is being edited
			if(id != 0)
				edited_node.saveToDB(conn, request, null, changes);
			
			// 2. if it is a new node being added
			else
			{
//System.out.println("doEdit.doPost saving blank node");
				id = edited_node.addToDB(conn, request, null);
			}
			
			url = request.getContextPath() + 
				"/content/Show?" +  ClientDaemon.REQUEST_NODE_ID +
				"=" + id;
			
			conn.close();
			conn = null;

		}//end -- try
		catch (Exception e) { e.printStackTrace(System.err); }
		finally
		{
//			cd.getSQLManager().freeConnection(conn);
			if (conn != null)
			{
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
			request.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();
			response.setContentType("text/html; charset=utf-8");
			out.println("<html><head>");
			out.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
			out.println("<title></title>");
			out.println("</head><body>");
			out.println("<script type='text/javascript'>");
			out.println("	document.location = '" + url + "';");
			out.println("</script>");
			out.println("</body>");
			out.println("</html>");
			out.close();
		}
	}
}
