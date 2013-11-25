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
import javax.servlet.*;
import javax.servlet.http.*;

//import edu.pitt.sis.paws.kt2.ResourceMap;

public class doGroup extends HttpServlet 
{
	static final long serialVersionUID = -2L;

	public void doPost(HttpServletRequest request, 
		HttpServletResponse response) throws ServletException, IOException
	{
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();

		String user_group_s = request.getParameter(ClientDaemon.REQUEST_USER_GROUP);
		int user_group = 0;
		if(user_group_s == null)
		{
			user_group = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		}
		else
			user_group = Integer.parseInt(user_group_s);
		
		ClientDaemon cd = ClientDaemon.getInstance();
		ResourceMap res_map = new ResourceMap(user_id, user_group, cd.getSQLManager());

		// record group and map into session and set session inited flag
		session.setAttribute(ClientDaemon.SESSION_GROUP_ID, new Integer(user_group));
		session.setAttribute(ClientDaemon.SESSION_RES_MAP , res_map);
		session.setAttribute(ClientDaemon.SESSION_INITED, "TRUE");
		
		System.out.println("... [KTree2] Logged in user: " + res_map.getUser().login + "  into group: " + res_map.getGroup().login);
		ClientDaemon.redirectToURL(request, response, request.getContextPath() + "/content/Show");
	}	
}
