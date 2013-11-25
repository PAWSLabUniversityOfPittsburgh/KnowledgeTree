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
import java.util.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.core.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael V. Yudelson
 */
public class doCopy extends HttpServlet
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
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/html;no-cache;charset=UTF-8");
		out.println("<html><head>");
		out.println("<link rel='stylesheet' href='" + request.getContextPath() +
			"/assets/KnowledgeTree.css' type='text/css'/>");
		out.println("</head>");

//		out.println("~~~ [CoPE] doCopy Parameters<br/>");

		HttpSession session = request.getSession();
		ResourceMap res_map = (ResourceMap)session.getAttribute(
			ClientDaemon.SESSION_RES_MAP);
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		User user = res_map.getUsers().findById(user_id);
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();

		String id_s = request.getParameter(Node.NODE_FRMFIELD_ID);
		int parent_id = Integer.parseInt(id_s);

		String url2 = request.getContextPath() + "/content/Show?" +  
			ClientDaemon.REQUEST_NODE_ID + "=" + parent_id;
		String url = " href='" + url2 + "' target='_top'";

		if(id_s == null)
		{
			System.out.println("!!! [KTree2] ERROR! doCopy.doPost: Parent node id not specified");
			out.println("<body>");
			out.println("<div style='color:red;'>ERROR! doCopy.doPost: Parent node id not specified!</div>");
		      out.println("<p/>");
		      out.println("<a class='pt_main_edit_button'" + url + ">&nbsp;&nbsp;OK&nbsp;&nbsp;</a>");
			out.println("</body></html>");
			return;
		}

		iNode target = null;
		ItemVector<iNode> sources = new ItemVector<iNode>();

		Enumeration enu = request.getParameterNames();
		Pattern p_radio = Pattern.compile("rad_node[0-9]*");
		Matcher m_radio = p_radio.matcher("");
		Pattern p_check = Pattern.compile("chk_node[0-9]*");
		Matcher m_check = p_check.matcher("");
		for(;enu.hasMoreElements();)
		{
			String key = (String)enu.nextElement();
			m_radio.reset(key);
			m_check.reset(key);
			if(m_radio.matches())
			{
				if(target != null)
					System.out.println("!!! [KTree2] ERROR! doCopy:: multiple targets specified");
				int node_id = Integer.parseInt(request.getParameter(key).substring(4));
				target = res_map.getNodes().findById(node_id);
				out.println("Param radio " + key + " = " + request.getParameter(key) + " " + target + "<br/>");
			}
			if(m_check.matches())
			{
				int node_id = Integer.parseInt(request.getParameter(key).substring(4));
				iNode source =  res_map.getNodes().findById(node_id);
				sources.add(source);
				out.println("Param check " + key + " = " + request.getParameter(key) + " " + source + "<br/>");
			}
		}
		
		// Analysis
		//	1. A. Copying to itself (sources parent == target)
		if( (target == null) || (sources.size()==0) )
		{
			out.println("<body>");
			out.println("<div style='color:red;'>Copy target and/or node to copy not chosen!</div>");
		      out.println("<p/>");
		      out.println("<a class='pt_main_edit_button'" + url + ">&nbsp;&nbsp;OK&nbsp;&nbsp;</a>");
			out.println("</body></html>");
			return;
		}
		
		//	1. B. Copying to itself (sources parent == target)
		if(parent_id == target.getId())
		{
			out.println("<body>");
			out.println("<div style='color:red;'>You cannot copy documents and/or folders to themselves!</div>");
		      out.println("<p/>");
		      out.println("<a class='pt_main_edit_button'" + url + ">&nbsp;&nbsp;OK&nbsp;&nbsp;</a>");
			out.println("</body></html>");
			return;
		}
		
		//	2. all the add rights are avaliable
		//		create a map one-to-many. one parent node type id
		//		many - child node type ids.
		//		check if for all those node type pairs user can create
		//		new nodes in the target folder Map Vector

//		Integer int1 = new Integer(10);
//		Integer int2 = new Integer(10);
//System.out.println("Compare int1==int2: " + int1.equals(int2));

		HashMap<Integer, ItemVector<Item>> map = new HashMap<Integer, ItemVector<Item>>();
		//	add target - source.children relations
		for(int i=0; i<sources.size(); i++)
		{
			Integer parent_type = new Integer(target.getNodeType());
			int child_type = sources.get(i).getNodeType();
			ItemVector<Item> value = null;
			
			if(!map.containsKey(parent_type))
				value = new ItemVector<Item>();
			else
				value = map.get(parent_type);
				
			if((value.size()==0) || (value.findById(child_type)==null))
			{
				value.add(new Item(child_type,
					new String(Integer.toString(child_type))));
				map.put(parent_type, value);
//System.out.println("CoPE.doCopy found pair " + parent_type.intValue() + "-" + child_type);				
			}
//			sources.get(i).reportParentChildNodeTypePairs(map);
		}

		// list the Map content
//		Iterator it = map.keySet().iterator();
//System.out.println("---- keys: " + map.size());				
//		while (it.hasNext())
//		{
//			Integer key = (Integer)it.next();
//			ItemVector<Item> value = map.get(key);
//			for(int i=0; i<value.size(); i++)
//				System.out.println("Node type pair " + key.intValue() + "-" + value.get(i).getId());
//		}

		//	3. parse the add rights for all node type pairs at the target node
		boolean go_ahead = true;
		
		Iterator<Integer> it = map.keySet().iterator();
		int parent_type = 0; // for detecting why cannot copy
		int child_type = 0; // for detecting why cannot copy
		while (it.hasNext())
		{
			Integer key = it.next();
			ItemVector<Item> value = map.get(key);
			parent_type = key.intValue();
			
			for(int i=0; i<value.size(); i++)
			{
				child_type = value.get(i).getId();
				go_ahead = 	res_map.isAllowedWhatWho2ForFromToQuant(
					Right.RIGHT_TYPE_ADD, user_id, group_id, 
					target, parent_type, child_type);
				
				if(!go_ahead) break;
			}
			if(!go_ahead) break;
		}
		
		if(!go_ahead) // if cannot copy show a message
		{
			out.println("<body>");
			out.println("<div style='color:red;'>You do not have sufficient rights to perform copying</div>");
			out.println("<div style='color:red;'>(This is because you cannot add " + 
				iNode.NODE_TYPES_S_ALL[parent_type-1] + " for/into " +
				iNode.NODE_TYPES_S_ALL[child_type-1] + ".)</div>");
		      out.println("<p/>");
		      out.println("<a class='pt_main_edit_button'" + url + ">&nbsp;&nbsp;OK&nbsp;&nbsp;</a>");
			out.println("</body></html>");
			return;
		}
		else // can copy
		{
			Connection conn = null;
			ClientDaemon cd = ClientDaemon.getInstance();
			try
			{
				conn = cd.getSQLManager().getConnection();
				for(int i=0; i<sources.size(); i++)
					sources.get(i).createACopyRecursive(target, res_map, user_id, user, conn);
				out.println("<script language='javascript'>");
				out.println("//	if(parent != null)");
				out.println("		top.location = '" + url2 + "';");
				out.println("</script>");
				out.println("<body>");
				out.println("</body>");
				out.println("</html>");
			}
			catch (Exception e) { e.printStackTrace(System.err); }
			finally
			{
//				cd.getSQLManager().freeConnection(conn);
				if (conn != null)
				{
					try { conn.close(); } catch (SQLException e) { ; }
					conn = null;
				}

			}/**/
		}
		return;
	}
}
