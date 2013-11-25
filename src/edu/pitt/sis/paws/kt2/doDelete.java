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
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class doDelete extends HttpServlet
{
	static final long serialVersionUID = -2L;
//	private Connection conn = null;


//	public void init(ServletConfig config) throws ServletException
//	{
//		super.init(config);
//		try{ conn = cd.getConnection(); }
//		catch (Exception e) {e.printStackTrace(System.out); }
//	}

	public void destroy()
	{
//		try{ if(conn != null ) conn.close(); }
//		catch (Exception e) {e.printStackTrace(System.out); }
	}

	public void doGet(HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException
	{
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException
	{
		// first get the id of the node being edited
		String id_s = (String)request.getParameter(Node.NODE_FRMFIELD_ID);
		if(id_s == null)
		{
			System.out.println("!!! [KTree2] doDelete.doPost: ERROR Id not specified!");
			return;
		}
		int id = Integer.parseInt(id_s);
		HttpSession session = request.getSession();
		ResourceMap res_map = (ResourceMap)session.getAttribute(
			ClientDaemon.SESSION_RES_MAP);
//System.out.println("@@@ bin_node_id " + res_map.getBinNodeId());
		iNode deleted_node = res_map.getNodes().findById(id);
		if(deleted_node == null)
		{
			System.out.println("!!! [KTree2] doDelete.doPost: ERROR Id not found!");
			return;
		}

		String url = ""; // return URL
		Connection conn = null;
		ClientDaemon cd = ClientDaemon.getInstance();
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
		try
		{
			conn = cd.getSQLManager().getConnection();
			
			// Remove connection to parent if exists
			String qry_rem_parent = "";
			iNode parent = deleted_node.getParent();
			if( parent != null)
			{
				qry_rem_parent = "DELETE FROM rel_node_node WHERE " +
					"ParentNodeID=" + deleted_node.getParent().getId() +
					" AND ChildNodeID=" + deleted_node.getId()  + ";";
				stmt1 = conn.prepareStatement(qry_rem_parent);
				stmt1.executeUpdate();
				stmt1.close();
				stmt1 = null;
//				SQLManager.executeUpdate(conn, qry_rem_parent);
			}

			// decide - delete node or connect to bin
			boolean disconnect = true;
//			if((deleted_node.getExternalObject() != null) &&
//				(deleted_node.getExternalObject().getOwners().size() > 1))
//			{
//				disconnect = false;
//System.out.println("~~~ Delete node of the external object ");
//			}

			if(disconnect) // connect node to bin
			{
				// Connect to the bin node
//System.out.println("bin_node_id: " + (res_map.getBinNodeId()));
				String qry = "INSERT INTO rel_node_node (ParentNodeID," +
					"ChildNodeID,Weight,OrderRank) VALUES(" +
					res_map.getBinNodeId() + "," +
					deleted_node.getId() + ",1,1);";
//				SQLManager.executeUpdate(conn, qry);
				stmt2 = conn.prepareStatement(qry);
				stmt2.executeUpdate();
				stmt2.close();
				stmt2 = null;
			}
			else // delete node
			{
				qry_rem_parent = "DELETE FROM rel_node_node WHERE " +
					" ChildNodeID=" + deleted_node.getId()  + ";";
//				SQLManager.executeUpdate(conn, qry_rem_parent);
				stmt3 = conn.prepareStatement(qry_rem_parent);
				stmt3.executeUpdate();
				stmt3.close();
				stmt3 = null;
			}

			conn.close();
			conn = null;
			
			url = request.getContextPath() +
				"/content/Show" +
				((parent != null)?
					"?" +  ClientDaemon.REQUEST_NODE_ID +
					"=" + parent.getId()
					:""
				);

			// delete node in the res_map
			res_map.sendNodeToBin(deleted_node, disconnect);

		}//end -- try
		catch (Exception e) { e.printStackTrace(System.err); }
		finally
		{
//			cd.getSQLManager().freeConnection(conn);
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
			if (stmt3 != null) 
			{
				try { stmt3.close(); } catch (SQLException e) { ; }
				stmt3 = null;
			}
			if (conn != null)
			{
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}

/*System.out.println("doAdd.doPost : url = " + url);
			this.redirectToURL(request, response, url);
System.out.println("After forward");/**/
			PrintWriter out = response.getWriter();
			out.println("<script language='javascript'>");
			out.println("//	if(parent != null)");
			out.println("		top.location = '" + url + "';");
			out.println("</script>");
			out.println("<body>");
			out.println("</body>");

//			edited_node.showView(out, request);/**/
		}
	}
}
