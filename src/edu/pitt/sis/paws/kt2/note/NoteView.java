package edu.pitt.sis.paws.kt2.note;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.pitt.sis.paws.core.utils.SQLManager;

/**
 * Servlet implementation class for Servlet: NotesAJAXRobot
 * 
 */
public class NoteView extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet
{
	static final long serialVersionUID = 1L;

	private static final String REQ_NOTE_ID = "id";

	private static final String REQ_NOTE_ID2 = "id2";

	private static final String REQ_USER = "u";

	private static final String REQ_GROUP = "g";

	private static final String REQ_EVENT = "e";

	private static final String REQ_TIME = "tm";

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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String s_user = request.getParameter(REQ_USER);
		String s_group = request.getParameter(REQ_GROUP);
		String s_note_id = request.getParameter(REQ_NOTE_ID);
		String s_note_id2 = request.getParameter(REQ_NOTE_ID2);
		String s_event = request.getParameter(REQ_EVENT);
		String s_time = request.getParameter(REQ_TIME);

		s_note_id2 = (s_note_id2 == null || s_note_id2.length() == 0) ? "1" : s_note_id2;

		// System.out.println("NV u: " + s_user + " g:" + s_group + " e:" +
		// s_event + " id:" + s_note_id + " id2:" + s_note_id2 );

		Connection conn = null;
		Statement stmt = null;

		try
		{
			conn = sqlManager.getConnection();
			stmt = conn.createStatement();

			stmt.executeUpdate("INSERT INTO ent_note_event (NoteID, NoteID2, Event, UserID, GroupID, DateNTime, DateNTimeNS, DateNTimeReported)" + " VALUES(" + s_note_id + "," + s_note_id2 + ",'"
					+ s_event + "'," + s_user + "," + s_group + ",NOW()," + System.nanoTime() + "," + s_time + ");");

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