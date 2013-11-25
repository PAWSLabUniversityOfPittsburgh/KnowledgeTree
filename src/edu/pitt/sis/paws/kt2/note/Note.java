package edu.pitt.sis.paws.kt2.note;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.pitt.sis.paws.core.utils.SQLManager;

/**
 * Servlet implementation class for Servlet: NotesAJAXRobot
 * 
 */
public class Note extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet
{
	static final long serialVersionUID = 1L;

	private static final String REQ_NOTE_ID = "id";

	private static final String REQ_USER = "u";

	private static final String REQ_GROUP = "g";

	private static final String REQ_URI = "uri";

	private static final String REQ_TEXT = "t";

	private static final String REQ_TIME = "tm";

	private static final String REQ_SHARED = "s";

	private static final String REQ_SIGNED = "a"; // anonymity

	private static final String REQ_FEATURE = "f"; // anonymity
	
	private SQLManager sqlManager;

	public static final String db_context = "java:comp/env/jdbc/portal";;

	public void init() throws ServletException
	{
		super.init();
		sqlManager = new SQLManager(db_context);
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String s_user = request.getParameter(REQ_USER);
		String s_group = request.getParameter(REQ_GROUP);
		String s_uri = request.getParameter(REQ_URI);
		String s_note_id = request.getParameter(REQ_NOTE_ID);
		String s_text = request.getParameter(REQ_TEXT);
		String s_time = request.getParameter(REQ_TIME);
		String s_privacy = request.getParameter(REQ_SHARED);
		String s_anonymity = request.getParameter(REQ_SIGNED);
		

		// System.out.println("u: " + s_user + " g:" + s_group + " uri:" + s_uri
		// + " id:" + s_note_id + " t:" + s_text
		// + " s:" + s_privacy + " tm:" + s_time);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		int new_node_it = 1;
		String new_note_date = "";

		try
		{
			conn = sqlManager.getConnection();
			boolean autocommit = conn.getAutoCommit();

			// 1. Mark previous version as old
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE ent_note SET IsPreviousVersion=1 WHERE NoteID=" + s_note_id + ";");
			stmt.close();
			stmt = null;

			// 2. Create new Version
			stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO ent_note (PreviousVersionNoteID, URI, UserID, GroupID, NoteText, " + "IsShared, IsSigned, DateCreated, DateNTimeReported, IsPreviousVersion) VALUES(" + s_note_id
					+ "," + "'" + s_uri + "'," + s_user + "," + s_group + ",'" + SQLManager.stringUnquote(s_text) + "'," + 
					(("true".equalsIgnoreCase(s_privacy)) ? 1 : 0) + "," + (("true".equalsIgnoreCase(s_anonymity)) ? 1 : 0) + ",NOW()," + s_time + ",0)");
			stmt.close();
			stmt = null;

			// 3. Get new NoteID
			stmt = conn.createStatement();
			String qry = "SELECT (LAST_INSERT_ID(NoteID)) AS LastID, DateCreated FROM ent_note WHERE UserID=" + s_user + " AND GroupID=" + s_group + " AND URI='" + s_uri
					+ "' AND DateNTimeReported=" + s_time + ";";

			rs = stmt.executeQuery(qry);
			if (rs.next())
			{
				new_node_it = rs.getInt("LastID");
				new_note_date = rs.getString("DateCreated");
				// System.out.println("new_note_date " + new_note_date);
			}

			rs.close();
			rs = null;
			stmt.close();
			stmt = null;

			conn.setAutoCommit(autocommit);
			conn.close();
			conn = null;
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace(System.out);
		}

		PrintWriter out = response.getWriter();
		response.setContentType("text/xml; charset=utf-8");
		out.print(new_node_it + "|" + new_note_date);
		out.close();
		out = null;
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Long start_ms = System.nanoTime();
		//
		String s_user = request.getParameter(REQ_USER);
		String s_group = request.getParameter(REQ_GROUP);
		String s_uri = request.getParameter(REQ_URI);
		String s_feature = request.getParameter(REQ_FEATURE);

		String result = "--oops--";

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Vector<Integer> note_ids = new Vector<Integer>();
		Vector<Integer> note_is_signed = new Vector<Integer>();
		Vector<String> note_users = new Vector<String>();
		Vector<String> note_texts = new Vector<String>();
		Vector<String> note_dates = new Vector<String>();
		String your_text = "";
		String your_date = "--";
		int your_is_shared = 1;
		int your_is_signed = 1;
		int your_note_id = 1;

		try
		{
			conn = sqlManager.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT u.UserID AS UID, u.Name, n.* FROM ent_note n JOIN ent_user u " + "ON(n.UserID=u.UserID) JOIN ent_user g ON(n.GroupID=g.UserID) "
					+ "WHERE IsPreviousVersion=0 AND n.GroupID=" + s_group + " AND n.URI='" + s_uri + "' AND (IsShared=1 OR n.UserID=" + s_user + ") ORDER BY DateCreated");
			while (rs.next())
			{
				String n_user = rs.getString("Name");
				String n_text = rs.getString("NoteText");
				String n_date = rs.getString("DateCreated");
				int is_shared = rs.getInt("IsShared");
				int is_signed = rs.getInt("IsSigned");
				int n_user_id = rs.getInt("UID");
				int n_note_id = rs.getInt("NoteID");

				if (n_user_id == Integer.parseInt(s_user))
				{
					your_text = n_text;
					your_date = n_date;
					your_is_shared = is_shared;
					your_is_signed = is_signed;
					your_note_id = n_note_id;
				}
				else
				{
					note_users.add(n_user);
					note_texts.add(n_text);
					note_dates.add(n_date);
					note_ids.add(n_note_id);
					note_is_signed.add(is_signed);
				}
			}
			rs.close();
			rs = null;

			stmt.close();
			stmt = null;

			conn.close();
			conn = null;

			String other_comments = (note_users.size() > 0) ? "" : "no other notes";

			for (int i = 0; i < note_users.size(); i++)
			{
				other_comments += "<div>\n" + "	<div><a id='c" + note_ids.get(i) + "' href='javascript:;' onClick='flipComment(this);'><img id='c" + note_ids.get(i) + "icn' src='"
						+ request.getContextPath() + "/assets/plus.gif' border='0' width='12' height='12' style='vertical-align:baseline;'/></a> <strong>" + ((note_is_signed.get(i).intValue()==1)?note_users.get(i):"anonymous") + "</strong> ("
						+ note_dates.get(i) + ")</div>\n" + "	<div id='c" + note_ids.get(i) + "txt' name='comment' style='display:none;'>" + note_texts.get(i).replaceAll("\\n", "<br />") + "</div>\n" + "</div>\n";
			}

			result = "<script language='javascript' type='text/javascript'>\n"
					+ "function flipComment(el)\n"
					+ "{\n"
					+ "	var nm = el.id;\n"
					+ "	if($(nm+'txt').style.display=='none')\n"
					+ "	{//open\n"
					+ "		$(nm+'txt').style.display='block';\n"
					+ "		$(nm+'icn').src='"
					+ request.getContextPath()
					+ "/assets/minus.gif';\n"
					+ "	}\n"
					+ "	else\n"
					+ "	{//close\n"
					+ "		$(nm+'txt').style.display='none';\n"
					+ "		$(nm+'icn').src='"
					+ request.getContextPath()
					+ "/assets/plus.gif';\n"
					+ "	}\n"
					+ "}\n"
					+ "</script>\n"
					+ "\n"
					+ "<div id='me'>\n"
					+ ((your_text != null && your_text.length() > 0) ? "	<div id='me-header'><strong>me</strong> (<span id='me-date'>" + your_date + "</span>)" +
							" <span id='me-shared' style='font-weight:bold;font-style:italic;'>" + ((your_is_shared == 1) ? "shared" : "private") + "</span>" + 
							" <span id='me-signed' style='font-weight:bold;'>" + ((your_is_signed == 1) ? "signed" : "anonymous") + "</span>" + 
							" <a id='me-editbtn' href='javascript:;' onClick='showEditComment();'>Edit</a></div>\n" + "	<div id='me-text' style='padding-bottom:10px;'>" + 
							your_text.replaceAll("\\n", "<br />") + "</div>\n"
							
							: "	<div id='me-header'><strong>me</strong> (<span id='me-date'>" + your_date + "</span>) " +
								"<span id='me-shared' style='font-weight:bold;font-style:italic;'></span> "+
								"<span id='me-signed' style='font-weight:bold;font-style:italic;'></span> "+
								"<a id='me-createbtn' href='javascript:;' onClick='showCreateComment();'>Create</a></div>\n" +
								"<div id='me-text' style='padding-bottom:10px;'></div>\n") + 
					"</div>\n" +
					"<div id='meedit' style='display:none; padding-bottom:10px; padding-left:2px;'>\n" + "<form>\n" + 
					
					"<div>" + 
					"<input type='radio' id='meedit-shared' name='" + REQ_SHARED + "' value='true' " + ((your_is_shared == 1) ? "checked" : "") + "/>shared" +
					"<input id='meedit-private' type='radio' name='" + REQ_SHARED + "' value='false' " + ((your_is_shared == 1) ? "" : "checked") + "/>private" +
					//"</div>\n" + 
					"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
					//"<div>" + 
					"<input type='radio' id='meedit-signed' name='" + REQ_SIGNED + "' value='true' " + ((your_is_signed == 1) ? "checked" : "") + "/>signed" +
					"<input id='meedit-anonymous' type='radio' name='" + REQ_SIGNED + "' value='false' " + ((your_is_signed == 1) ? "" : "checked") + "/>anonymous" +
					"</div>\n" + 
					
					"<textarea id='meedit-text' name='"
					+ REQ_TEXT + "' rows='10' cols='40'>" + your_text + "</textarea>\n"
					+ "<div><input type='button' value='Submit' onClick='saveComment();' />&nbsp;&nbsp;<input type='reset' value='Cancel' onClick='cancelEditComment();' /></div>\n"
					+ "<input type='hidden' id='meedit-group' name='" + REQ_GROUP + "' value='" + s_group + "' />\n" + "<input type='hidden' id='meedit-user' name='" + REQ_USER + "' value='"
					+ s_user + "' />\n" + "<input type='hidden' id='meedit-noteid' name='" + REQ_NOTE_ID + "' value='" + your_note_id + "' />\n"
					+ "<input type='hidden' id='meedit-uri' name='" + REQ_URI + "' value='" + s_uri + "' />\n" + "</form>\n" + "</div>\n" +

					
					
					other_comments;

		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace(System.out);
		}

		PrintWriter out = response.getWriter();
		response.setContentType("text/xml; charset=utf-8");
		out.println(result);
		out.close();
		out = null;
		
		Long finish_ms = System.nanoTime();
		
		try
		{
			conn = sqlManager.getConnection();
			stmt = conn.createStatement();
			
			stmt.executeUpdate("INSERT INTO log_note_view (UserID, GroupID, URI, ViewMode, Feature, StartNS, FinishNS, CostMS, URIsSupplied, NoteBatchesCompiled, NavigationState) VALUES" + 
					"(" + s_user + "," + s_group + ",'" + s_uri + "','single','" + s_feature + "'," + start_ms + "," + finish_ms + "," + (double)((double)finish_ms-start_ms)/1000000 + ",0,0,'');");
			
			stmt.close();
			stmt = null;
			conn.close();
			conn = null;
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace(System.out);
		}

	}
}