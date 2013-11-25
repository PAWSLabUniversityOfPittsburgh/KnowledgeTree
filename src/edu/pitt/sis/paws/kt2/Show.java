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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Show extends HttpServlet
{
	static final long serialVersionUID = -2L;
	
	// Constants
	public static final int SHOW_MODE_VIEW = 1;  // also when parameter is null
	public static final int SHOW_MODE_EDIT = 2;
	public static final int SHOW_MODE_ADD = 3;
	public static final int SHOW_MODE_COPY = 4;
	public static final int SHOW_MODE_DELETE = 5;
	public static final int SHOW_MODE_MOVE = 6;
	public static final int SHOW_MODE_ADD_RATING = 7;
	public static final String SHOW_FLAG_RELOAD = "reload";

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		doGet(req, res);
	}
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		// check authentication
		if(!ClientDaemon.isSessionInited(req.getSession(false)))
		{
			ClientDaemon.forwardToURL(req,res, "/content/doAuthenticate");
			return;
		}

		HttpSession session = req.getSession();
		
		// deal with Node current id
		
		iNode current_node = (iNode)session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
		String node_id_s = req.getParameter(ClientDaemon.REQUEST_NODE_ID);
		String mode_s = req.getParameter(ClientDaemon.REQUEST_SHOW_MODE);
		
		String svc = req.getParameter("svc");
		if(svc != null && svc.length() > 0)
			session.setAttribute("svc", svc);
//System.out.println("Show:: SVC="+svc);

		ResourceMap res_map = (ResourceMap)session.getAttribute(
				ClientDaemon.SESSION_RES_MAP);
		
		ClientDaemon cd = ClientDaemon.getInstance();
		// check the reload statement
		String reload = req.getParameter(SHOW_FLAG_RELOAD);
		if(reload != null)
		{
			// get user id and group id
			int user_id = ((Integer)session.getAttribute(
					ClientDaemon.SESSION_USER_ID)).intValue();
			int group_id = ((Integer)session.getAttribute(
					ClientDaemon.SESSION_GROUP_ID)).intValue();
			res_map = new ResourceMap(user_id, group_id, cd.getSQLManager());

			session.setAttribute(ClientDaemon.SESSION_RES_MAP, res_map);
			
			// AND SET NODE TO ROOT or only child of root
			node_id_s = null;
			current_node = null;
		}

		int node_id;
		if(node_id_s != null) // new node is specified
		{
			node_id = Integer.parseInt(node_id_s);
			
			if((current_node == null) || (node_id != current_node.getId()) )
			{
				current_node = res_map.getNodes().findById(node_id);
				session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
			}
		}
		else if(current_node != null) // old node is specified
			node_id = current_node.getId();
		else // root node is meant
		{
			// if root has one sub-node, choose sub
			if(res_map.getRootNode().getChildren().size() == 1)
			{
				node_id = res_map.getRootNode().getChildren().get(0).getId();
				current_node = res_map.getRootNode().getChildren().get(0);
			}
			else
			{
				node_id = res_map.getRootNode().getId();
				current_node = res_map.getRootNode();
			}
			session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
		}
		
		if(current_node == null) // node was not found -- set root
		{
			node_id = res_map.getRootNode().getId();
			current_node = res_map.getRootNode();
			session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
		}
		
		String title = current_node.getTitle();
		
		// Load current node if necessary
		if(!current_node.isFullyLoaded())
		{
//			// clocking
//			Calendar start = null;
//			Calendar finish = null;
//			long diff_mills;
//			start = new GregorianCalendar();
//			
//			System.out.print("+++ [KTree2] Nodes uploaded in " );
			res_map.FullyLoad(current_node, cd.getSQLManager());
			session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
//
//			finish = new GregorianCalendar();
//			diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
//			System.out.println(diff_mills + "ms, heap size " + Runtime.getRuntime().freeMemory());
		}
		
		// Check for modes (view/edit/...)
		//	first delete old mode
		session.removeAttribute(ClientDaemon.SESSION_SHOW_MODE);
		int mode = 0;
		
		if(mode_s != null)
		{
			mode = Integer.parseInt(mode_s);
			// .. and load mode to the session
			session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,mode);
			switch(mode)
			{
				case Show.SHOW_MODE_EDIT:
					session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
						Show.SHOW_MODE_EDIT);
				break;
			      case Show.SHOW_MODE_ADD:
			            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
			                  Show.SHOW_MODE_ADD);
			      break;
			      case Show.SHOW_MODE_COPY:
			            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
			                  Show.SHOW_MODE_COPY);
			      break;
			      case Show.SHOW_MODE_DELETE:
			            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
			                  Show.SHOW_MODE_DELETE);
			      break;
			      case Show.SHOW_MODE_MOVE:
			            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
			                  Show.SHOW_MODE_MOVE);
			      break;
			      case Show.SHOW_MODE_ADD_RATING:
			            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
			                  Show.SHOW_MODE_ADD_RATING);
			      break;
			}
		}
		
		
		
		// Check for "hide left frame flag"
		Object o_lframe = session.getAttribute(ClientDaemon.SESSION_HIDE_LEFT_FRAME);
		boolean hide_left_frame = (o_lframe != null);
		
		// if there's a node pending and we're not editing it - recycle
		if( (res_map.getPendingNode()!= null) && (mode != Show.SHOW_MODE_EDIT) )
		{
			res_map.recycleNode(res_map.getPendingNode());
			session.setAttribute(ClientDaemon.SESSION_RES_MAP, res_map);
			
			if(current_node.getId() == 0)
			{
				current_node = current_node.getParent();
				session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
			}
		}

		// Make sure the path to the node is expanded if necessary
		String expand = req.getParameter(ClientDaemon.REQUEST_EXPANDPATH);
		if(expand != null)
		{
			if(res_map.getNodes().findById(current_node.getId()).
				expandParents(false))
				session.setAttribute(ClientDaemon.SESSION_RES_MAP,res_map);
		}

		// now let's call pservice if necessary
		Integer current_mode = (Integer)session.getAttribute(ClientDaemon.SESSION_SHOW_MODE);
		// if viewing a PServiced node - rerequest pservice
		if(current_mode.intValue() == Show.SHOW_MODE_VIEW &&
				current_node.getNodeType() == iNode.NODE_TYPE_I_PSERVICED_FOLDER)
		{
			current_node.performPersonalization(req);
		}
		
		// add note icons
		if(current_mode.intValue() == Show.SHOW_MODE_VIEW)
		{
			;
		}
		
		
		String left = req.getContextPath() + "/content/jspLeft" +
			((current_node!=null)?("#n" + current_node.getId()):"");
//		String right = req.getContextPath() + "/content/jspRight" +
//			((current_node!=null)?("#node" + current_node.getId()):"");/**/
		String main = req.getContextPath() + "/content/jspMain"; //(String)urlList.get(1);
		String top = req.getContextPath() + "/content/jspTop";
		
		PrintWriter out = res.getWriter();
		req.setCharacterEncoding("utf-8");

		out.println("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN' 'http://www.w3.org/TR/html4/strict.dtd'>");
		out.println("<html><head>");
		out.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
		out.println("<link rel='icon' href='" + req.getContextPath() + "/assets/favicon.ico' type='image/x-icon'>");
		out.println("<link rel='shortcut icon' href='" + req.getContextPath() + "/assets/favicon.ico' type='image/x-icon'>"); 
		out.println("<link rel='stylesheet' href='" + req.getContextPath() +
			"/assets/KnowledgeTree.css' type='text/css'/>");
		out.println("<title> Knowledge Tree - " + title + "</title>");
		out.println("<script src='" + req.getContextPath() + "/assets/prototype.js'></script>");
		out.println("<script src='" + req.getContextPath() + "/assets/sessionTimeout.js' language='javascript' type='text/javascript'>");
		out.println("<!--		");
		out.println("window.onload = sessionTimerKT.sessionTimerInit('" + req.getContextPath() + "'," + (session.getMaxInactiveInterval() - 120)*1000 + ");");
		out.println("-->");
		out.println("</script>");	
		
		
		out.println("<script language='javascript' type='text/javascript'>\n" + 
			"function openNote(uid, gid, uri)\n" + 
			"{\n" + 
			"	var params = $H({u:uid, g:gid,uri:uri});\n" + 
			"\n" + 
			"	var au = new Ajax.Updater(\n" + 
			"		'kt-notes', \n" + 
			"		'" + req.getContextPath()+ "/notes',\n" +  
			"		{ \n" + 
			"			method: 'post',\n" + 
			"			parameters: params,\n" + 
			"			onSuccess:function(response){ \n" + 
			"				Tip(response.responseText, CLOSEBTN, true, TITLE, 'Notes', CLOSEBTNTEXT, 'close', CLOSEBTNCOLORS, ['#3366CC', '#E8EEF7', '#E8EEF7', '#3366CC'], STICKY, true, HEIGHT, 300, EXCLUSIVE, true, BGCOLOR,'#E8EEF7',BORDERCOLOR,'#3366CC', BORDERSTYLE,'dotted',WIDTH,400,TEXTALIGN,'justify');\n" + 
			"			}.bind(this)\n" + 
			"		} \n" + 
			"	);\n" + 
			"}\n" + 
			"</script>\n");
		
		out.println("</head>");
		
		// narrow top
//		out.println("<frameset rows='*' cols='" + ((hide_left_frame)?"0":"250") + ",*' id='topFrameSet' framespacing='0' frameborder='YES' border='4'>");
//		out.println("	<frame src='" + left + "' id='leftFrame' name='leftFrame' scrolling='YES' marginheight='2' marginwidth='2'>");
//		out.println("	<frame src='" + right + "' id='rightFrame' name='rightFrame' scrolling='YES' marginheight='0' marginwidth='0'>");
//		out.println("</frameset>");
		
		out.println("<body marginwidth='0' marginheight='0' >");
		out.println("<script src='" + req.getContextPath()+ "/assets/wz_tooltip.js'></script>");
		out.println("<iframe frameborder='0' width='100%' height='85' src='" + top + "' name='topFrame' scrolling='no' noresize='noresize' id='topFrame' title='topFrame' marginheight='0' marginwidth='5'></iframe>");
		out.println("<iframe frameborder='0' width='100%' style='height:auto' frameborder='2' src='" + main + "' id='mainFrame' name='mainFrame' scrolling='auto' id='mainFrame' title='mainFrame' marginheight='5' marginwidth='5' ></iframe>");
		out.println("</body>");
		out.println("</html>");
		
		/*
		// broad top
		out.println("<frameset rows='85,*' cols='*' framespacing='0' id='topFrameSet' border='0' >");
		out.println("	<frame src='" + top + "' name='topFrame' scrolling='no' noresize='noresize' id='topFrame' title='topFrame' marginheight='0' marginwidth='5' />");
//		out.println("	<frameset cols='" + ((hide_left_frame)?"0":"250") + ",*' id='innerFrameSet' border='4' >");
//		out.println("		<frame frameborder='2' src='" + left + "' id='leftFrame' name='leftFrame' " + ((hide_left_frame)?"noresize='noresize' ":"") + "scrolling='yes' id='leftFrame' title='leftFrame' marginheight='2' marginwidth='2' />");
		out.println("		<frame frameborder='2' src='" + main + "' id='mainFrame' name='mainFrame' scrolling='yes' id='mainFrame' title='mainFrame' marginheight='5' marginwidth='5' />");
//		out.println("	</frameset>");
		out.println("</frameset>");
		out.println("<script src='<%=request.getContextPath()%>/assets/wz_tooltip.js'></script>");
		out.println("<noframes>");
		out.println("</noframes>");
		out.println("</html>");
		/**/
	}
}

