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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.pitt.sis.paws.cbum.report.ReportAPI;
import edu.pitt.sis.paws.core.utils.Digest;
import edu.pitt.sis.paws.core.utils.SQLManager;


/**
 * ClientDaemon is a singleton encapsulating constants variables and methods
 * central to the client application in general.
 */
public class ClientDaemon
{
	private static ClientDaemon instance = new ClientDaemon();

	// CONSTANTS
	//		CONTEXT
	public String context_ums_url;
	//		SESSION CONSTANTS
	public static final String SESSION_USER_ID = "user_id";
	public static final String SESSION_USER_NAME = "user_name";
	public static final String SESSION_RES_MAP = "res_map";
	public static final String SESSION_GROUP_ID = "group_id";
	public static final String SESSION_GROUP_NAME = "group_name";
	public static final String SESSION_GROUPS = "groups";
	public static final String SESSION_CURRENT_NODE = "current_node";
	public static final String SESSION_INITED = "SESSION_INITED";
	public static final String SESSION_AUGMENTED_VIEW = "augmented_view";
	// 		REQUEST PARAMETERS
	public static final String REQUEST_NODE_ID = "id";
	/** Flag requesting a check for the node to be visible (all its parents
	 * expanded). */
	public static final String REQUEST_EXPANDPATH = "exppath";
	public static final String REQUEST_FRAMES = "frames";
	public static final String REQUEST_USER_GROUP = "user_group";
	/** Parameter for the left "tree" frame to supply the id of the node being 
	 * recently expanded
	 */
	public static final String REQUEST_EXPAND = "expand";
	/** Parameter for the left "tree" frame to supply the id of the node being 
	 * recently collapsed
	 */
	public static final String REQUEST_COLLAPSE = "collapse";
	
	/** Token stored in session to specify the special modes of node
	 * handling e.g. editing mode. Used along with REQUEST_MODE.
	 * For mode specification refer to the Show class definition.
	 */
	public static final String SESSION_SHOW_MODE = "mode";
	/** Flag to determine whether left frame is shown or not. If value is not
	 * null then do not show.
	 */
	public static final String SESSION_HIDE_LEFT_FRAME = "lhide";

	// REQUEST PARAMETERS
	/** Parameter is used with Show servlet to specify the special conditions
	 * e.g. editing. For mode specification refer to the Show class definition
	 */
	public static final String REQUEST_SHOW_MODE = "mode";

	private SQLManager sqlManager;
	
	private ReportAPI reportApi;
	
	private ClientDaemon()
	{
		Calendar start = null;
		Calendar finish = null;
		long diff_mills;
		start = new GregorianCalendar();
		
		// Create SQL Manager
		sqlManager = new SQLManager("java:comp/env/jdbc/portal");
		
		// Read Context parameters
		try
		{
			Context initCtx = new InitialContext();
			context_ums_url = (String)initCtx.lookup("java:comp/env/ums");
			if (context_ums_url == null)
				throw new Exception("!!! [KTree2]: Failure read UMS URL");
		}
		catch(NamingException ex) { ex.printStackTrace(); }
		catch(Exception ex) { ex.printStackTrace(); }
		
		reportApi = new ReportAPI(context_ums_url);
		
		finish = new GregorianCalendar();
		diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
		System.out.println("... [KTree2] inited in " + diff_mills + "ms ---- ");
		
	}

	public static ClientDaemon getInstance() { return instance; }
	
	
	public SQLManager getSQLManager() { return sqlManager; }
	public ReportAPI getReportAPI () { return reportApi; }
	
	public static boolean isSessionInited(HttpSession session)
	{
		if((session == null) || (session.getAttribute(SESSION_INITED) == null))
			return false;
		else
			return true;
	}

	public static void setSessionInited(HttpSession session)
	{
		session.setAttribute(SESSION_INITED,"TRUE");
	}
	
	public static void redirectToURL(HttpServletRequest req, HttpServletResponse res,
			String URL) throws ServletException, IOException
	{
		res.sendRedirect(URL);
	}

	public static void forwardToURL(HttpServletRequest req, HttpServletResponse res, 
			String URL) throws ServletException, IOException
	{
		RequestDispatcher disp;
		disp = req.getRequestDispatcher(URL);
		disp.forward(req, res);
	}
	
	public static void includeURL(HttpServletRequest req, HttpServletResponse res,
			String URL) throws ServletException, IOException
	{
		RequestDispatcher disp;
		disp = req.getRequestDispatcher(URL);
		disp.include(req, res);
	}
		
	public ArrayList<Integer> stringToIntegerList(String s_integer_list, String delimeter)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		String [] tokens = s_integer_list.split(delimeter);
		for(int i=0; i<tokens.length; i++)
			result.add(Integer.parseInt(tokens[i]));

		return result;
	}

	public static String addIdentityToURL(String url, HttpServletRequest request)
	{
		int user_id;
		int group_id;
		
		String result = "";
		
		HttpSession session = request.getSession();
		ResourceMap res_map = (ResourceMap) session.
				getAttribute(ClientDaemon.SESSION_RES_MAP);
		user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		
		User user_ = res_map.getUsers().findById(user_id);
		User group_ = res_map.getUsers().findById(group_id);
		String sess_ = session.getId().substring(session.getId().length()-5);
		
		result =  url+((url.indexOf("?")!=-1)?"&":"?") + "usr=" + user_.getLogin() + "&grp=" +
			group_.getLogin() + "&sid=" + sess_;// + "&ums=" + ClientDaemon.CONTEXT_UMS;
		
		// replace macro-parameters
//System.out.println("Before replace url="+result);
//		String patternStr = "__usr__";
//		String replacementStr = user_.getLogin();
//		Pattern pattern = Pattern.compile(patternStr);
//		Matcher matcher = pattern.matcher(result);
//        String output = matcher.replaceAll(replacementStr);
//        
////		result.replaceAll("||usr||", user_.getLogin()); // user identity
//System.out.println("After replace url="+output);
//System.out.println("After replace url="+result.replaceAll("__usr__", user_.getLogin()));
		if(result.indexOf("__usr__")>-1)
			result = result.replaceAll("__usr__", user_.getLogin()); // user identity
		if(result.indexOf("__grp__")>-1)
			result = result.replaceAll("__grp__", group_.getLogin()); // group identity
		if(result.indexOf("__md5_usr__")>-1)
			result = result.replaceAll("__md5_usr__", Digest.MD5(user_.getLogin()) ); // MD5 of user identity
		
		
		return result;
	}
	
}

