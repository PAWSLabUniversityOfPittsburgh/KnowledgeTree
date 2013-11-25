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


public class AjaxRobot extends HttpServlet
{

	static final long serialVersionUID = -2L;
	
	// Constants
	/** Flag to determine whether left frame is shown or not. If value is not
	 * null then do not show.
	 */
	public static final String REQUEST_HIDE_LEFT_FRAME = "lhide";


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
		}
		
		HttpSession session = req.getSession();
		
		// Deal with "Hide Left Frame"
		String lhide = req.getParameter(REQUEST_HIDE_LEFT_FRAME);
		if(lhide.equalsIgnoreCase("1"))
			session.setAttribute(ClientDaemon.SESSION_HIDE_LEFT_FRAME, "hide");
		else
			session.removeAttribute(ClientDaemon.SESSION_HIDE_LEFT_FRAME);
//System.out.println("AjaxRobot.doGet lhide=" + lhide);
	}
}

