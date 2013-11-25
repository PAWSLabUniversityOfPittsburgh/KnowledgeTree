package edu.pitt.sis.paws.kt2.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.pitt.sis.paws.kt2.ClientDaemon;

public class RestNodeView extends HttpServlet implements Servlet
{
	static final long serialVersionUID = -2L;
	
   /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public RestNodeView()
	{
		super();
	}   	

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
//			String req_user_id = request.getParameter("id");
//			String req_user_login = request.getParameter("login");
//			System.out.println("RestUserView.doGet:: req_user_id=" + req_user_id);		
//			System.out.println("RestUserView.doGet:: req_user_login=" + req_user_login);		
//			System.out.println("RestUserView.doGet:: query={" + request.getQueryString() + "}\n");
		
		String _node_id = request.getParameter(RestDataRobot.REST_NODE_ID);
		String _format = request.getParameter(RestDataRobot.REST_FORMAT);
		String _embed = request.getParameter(RestDataRobot.REST_EMBED);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(RestDataRobot.REST_NODE_ID, _node_id);
		params.put(RestDataRobot.REST_FORMAT, _format);
		params.put(RestDataRobot.REST_EMBED, _embed);
		params.put(RestDataRobot.REST_CONTEXT_PATH, 
				// CONTEXT PATH
				"http://" + request.getServerName() + 
				((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath()
//				"http://adapt2.sis.pitt.edu/kt"
				);
		
//			System.out.println("rURL " + request.getRequestURL());	
//			System.out.println("rURI " + request.getRequestURI());	

		ClientDaemon cd = ClientDaemon.getInstance();
		params = RestDataRobot.getNodeInfo(params, cd.getSQLManager(), false /*multiple nodes*/);
		String result = params.get(RestDataRobot.REST_RESULT);
		
		PrintWriter out = response.getWriter();
		out.println(result);
	}  	
}
