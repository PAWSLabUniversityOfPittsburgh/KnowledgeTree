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


public class doAdd extends HttpServlet
{
	static final long serialVersionUID = -2L;
	
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
		String node_type_s = (String)request.getParameter(Node.NODE_FRMFIELD_NODETYPE);
		
		
		
		String icon = (String)request.getParameter(Node.NODE_FRMFIELD_ICON);
		String hidden_s = (String)request.getParameter(Node.NODE_FRMFIELD_HIDDEN);
		boolean hidden = Node.NODE_FRMFIELD_HIDDEN_YES.equals(hidden_s);
//System.out.println("hidden_s=" + hidden_s + " hidden=" + hidden);
		
		if(id_s == null)
		{
			System.out.println("!!! [KTree2] doEdit.doPost: ERROR Id not specified!");
			return;
		}
		int id = Integer.parseInt(id_s);
		
		if(node_type_s == null)
		{
			System.out.println("!!! [KTree2] doEdit.doPost: ERROR Node Type not specified!");
			return;
		}
		
		int node_type = Integer.parseInt(node_type_s);
		
		HttpSession session = request.getSession();
		session.setAttribute(Node.NODE_FRMFIELD_NODETYPE_RECENT, node_type);
		
		ResourceMap res_map = (ResourceMap)session.getAttribute(
			ClientDaemon.SESSION_RES_MAP);
		iNode edited_node = res_map.getNodes().findById(id);
		if(edited_node == null)
		{
			System.out.println("!!! [KTree2] doEdit.doPost: ERROR Id not found!");
			return;
		}
		
		// Create stub node in database
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		User user = res_map.getUsers().findById(user_id);
		int folder_flag_i = ((node_type == iNode.NODE_TYPE_I_FOLDER) ||
			(node_type == iNode.NODE_TYPE_I_TOPIC_FOLDER) || 
			(node_type == iNode.NODE_TYPE_I_PSERVICED_FOLDER) )?1:0;
		boolean folder_flag = (folder_flag_i == 1)?true:false;//edited_node.getFolderFlag();
		int node_id = 0;

		ItemType item_type = res_map.getItemtypeList().findById(node_type);
		// Set default title
		String title = item_type.getTitle();
		// Load factory class
		String node_class_name = item_type.getClassName() ;
		iNode node_class = null;
		// load class
		try
		{
			node_class = (iNode)Class.forName(node_class_name).newInstance();
		}
		catch(ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace(System.out);
			System.out.println("!!! [KTree2] SEVERE! ResourceMap.doAdd:: ClassNotFoundException");
			return;
		}
		catch(InstantiationException ie)
		{
			ie.printStackTrace(System.out);
			System.out.println("!!! [KTree2] SEVERE! ResourceMap.doAdd:: InstantiationException");
			return;
		}
		catch(IllegalAccessException iae)
		{
			iae.printStackTrace(System.out);
			System.out.println("!!! [KTree2] SEVERE! ResourceMap.doAdd:: IllegalAccessException");
			return;
		}
		
		// Set default URI & URL
		String uri = node_class.getDefaultURI(node_id);
		String url = "";
		
//		ResultSet rs = null;
//		String qry = "INSERT INTO ent_node (Title, UserID, DateCreated, " +
//			"DateModified, DateAltered, ItemTypeID, FolderFlag) VALUES ('" + 
//			title + "', " + user_id + ", NOW(), NOW(), NOW(), " + 
//			node_type + ", " + folder_flag_i + ");";

//System.out.println("doAdd.doPost new stub node query \n\t " + qry);
		Connection conn = null;
		ClientDaemon cd = ClientDaemon.getInstance();
		
		try
		{
			conn = cd.getSQLManager().getConnection();
			// insert stub node
//			if(node_type != iNode.NODE_TYPE_I_CONCEPTS)
//			{
//				AppDaemon.executeUpdate(conn, qry);
//				
//				// get last inserted id
//				qry = "SELECT MAX(LAST_INSERT_ID(NodeID)) AS LastID FROM ent_node WHERE UserID=" + user_id + ";";
//				PreparedStatement statement = conn.prepareStatement(qry);
//				rs = AppDaemon.executeStatement(statement);
//				while(rs.next())
//				{
//					node_id = rs.getInt("LastID");
//				}
//				rs.close();
//				rs = null;
//				statement .close();
//				statement = null;
//	
//				// connect new node to the parent
//				qry = "INSERT INTO rel_node_node (ParentNodeID, ChildNodeID, Weight, OrderRank)" +
//					" VALUES (" + edited_node.getId() + ", " + node_id + ", 1, " + (edited_node.getChildren().size()+1) + ");";
//				AppDaemon.executeUpdate(conn, qry);
//			}
			
			iNode new_node = null;
			new_node = node_class.NodeFactory(node_id, title, uri,
					node_type, ""/*desc*/, url, folder_flag, icon, hidden);
			new_node.setCreator(user);
			
//			// create actual node object and external object if necessary
//			switch (node_type) 
//			{
//				case iNode.NODE_TYPE_I_FOLDER:
//				case iNode.NODE_TYPE_I_UNTYPDOC:
//				case iNode.NODE_TYPE_I_TOPIC_FOLDER:
//				{
//					new_node = new Node(node_id, title, uri,
//							node_type, "", "", folder_flag, icon);
//					new_node.setCreator(user);
//				}
//				break;
//				case iNode.NODE_TYPE_I_COURSE:
//				{
//					new_node = new NodeCourse(node_id, title, uri,
//							node_type, "", "", folder_flag, icon);
//					new_node.setCreator(user);
//				}
//				break;
//				case iNode.NODE_TYPE_I_QUIZ:
//				case iNode.NODE_TYPE_I_DISSECTION:
//				case iNode.NODE_TYPE_I_WADEIN:
//				case iNode.NODE_TYPE_I_CODEEXAMPLE:
//				case iNode.NODE_TYPE_I_LINK:
//				{
//					new_node = new Node(node_id, title, ""/*TO DO URI*/,
//							node_type, "", "", folder_flag, icon);
//					new_node.setCreator(user);
////					ExternalResourceVisual erv = new ExternalResourceVisual(0, title, "",
////							user, "", "");
////					new_node = new NodeUntyped(node_id, title, 
////							node_type, "", "", folder_flag, "", user, erv);
//				}
//				break;
//				case iNode.NODE_TYPE_I_KARELROBOT:
//				{
//					new_node = new Node(node_id, title, ""/*TO DO URI*/,
//							node_type, "", "", folder_flag, icon);
//					new_node.setCreator(user);
/////					ExternalResourceDownloadable erd = new ExternalResourceDownloadable(
////							0, title, "", user, "", "");
////					new_node = new NodeUntyped(node_id, title, 
////							node_type, "", "", folder_flag, "",	user, erd);
//				}
//				case iNode.NODE_TYPE_I_SYS_QUIZGUIDE: //System QuizGUIDE
//				case iNode.NODE_TYPE_I_SYS_NAVEX: // System NavEx
//				case iNode.NODE_TYPE_I_SYS_WADEIN: // System WADEIn II
//				case iNode.NODE_TYPE_I_LINK_POPUP:
//				{
//					new_node = new Node(node_id, title, ""/*TO DO URI*/,
//							node_type, "", "", folder_flag, icon);
//					new_node.setCreator(user);
//					// create CodeExample object
////					ExternalResourcePopup erp = new ExternalResourcePopup(0, title, "",
////							user, "", "");
////					new_node = new NodeUntyped(node_id, title, 
////							node_type, "", "", folder_flag, "", user, erp);
//				}
//				break;
//			}
			
			
			// add new node to the resource map and rewrite resource
			// map into session
			res_map.getNodes().add(new_node);
			// add node into "pending"
			res_map.setPendingNode(new_node);
			
//System.out.println(" nodeeeee " + edited_node.getTitle());
			edited_node.getChildren().add(new_node);
			new_node.setParent(edited_node);
			
			// apply rights to the node
			
			// url
			url = request.getContextPath() + 
				"/content/Show?" +  ClientDaemon.REQUEST_NODE_ID +
				"=" + new_node.getId() + "&" + 
				ClientDaemon.REQUEST_SHOW_MODE + "=" +
				Show.SHOW_MODE_EDIT;
			
			conn.close();
			conn = null;
		}//end -- try
		catch (Exception e) { e.printStackTrace(System.err); }
		finally
		{
			if (conn != null)
			{
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}


/*System.out.println("doAdd.doPost : url = " + url);
			this.redirectToURL(request, response, url);
System.out.println("After forward");/**/
			request.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();
			response.setContentType("text/html; charset=utf-8");
			out.println("<script>");
			out.println("	if(parent != null)");
			out.println("		parent.location = '" + url + "';");
			out.println("</script>");
			out.println("<body>");
			out.println("</body>");

//			edited_node.showView(out, request);/**/
		}
	}
	
}
