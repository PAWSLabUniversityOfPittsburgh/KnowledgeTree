package edu.pitt.sis.paws.kt2.note;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.pitt.sis.paws.core.Item2Vector;
import edu.pitt.sis.paws.core.iItem2;
import edu.pitt.sis.paws.core.utils.SQLManager;
import edu.pitt.sis.paws.kt2.rest.RestDataRobot;

/**
 * Servlet implementation class for Servlet: NotesAJAXRobot
 * 
 */
public class NoteBatch extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet
{
	static final long serialVersionUID = 1L;

	public static final String REQ_USER = "u";

	public static final String REQ_GROUP = "g";
	
	public static final String REQ_FORMAT = "f";
	
	public static final String REQ_FORMAT_JAVA_OBJ = "j";
	
	public static final String REQ_FORMAT_RDF = "r";
	
	public static final String REQ_URI = "uri";

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
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Long start_ns = System.nanoTime();

		//
		String s_user = request.getParameter(REQ_USER);
		String s_group = request.getParameter(REQ_GROUP);
		String s_uri = request.getParameter(REQ_URI);
		String s_format = request.getParameter(REQ_FORMAT);
		
		int uris_supplied = 0;
		int note_batches_compiled = 0;
		String annotation_state = "";

		int i_user = Integer.parseInt(s_user);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String uri_list = "";
		
		Item2Vector<iItem2> req_note_details = new Item2Vector<iItem2>();
		Item2Vector<iItem2> res_note_details = new Item2Vector<iItem2>();
		try
		{
			conn = sqlManager.getConnection();
			if(REQ_FORMAT_JAVA_OBJ.equalsIgnoreCase(s_format))
			{
				ObjectInputStream in = new ObjectInputStream(
				request.getInputStream());
				req_note_details = (Item2Vector<iItem2>)in.readObject();
				
//System.out.println("////////");		
//for(int i=0; req_note_details!=null && i<req_note_details.size(); i++)
//{
//	System.out.println(i + " this.getId() = " + req_note_details.get(i).getId() + " this.getURI() = " + req_note_details.get(i).getURI());
//}
				
				uris_supplied = (req_note_details !=null)?req_note_details.size():uris_supplied;
				
//System.out.println(" reqArray.size() = " + reqArray.size());		
				for(int i = 0; req_note_details !=null && i<req_note_details.size(); i++)
				{
					iItem2 req = req_note_details.get(i);
//System.out.println("-- this.getId() = " + req.getId() + " this.getURI() = " + req.getURI());
					
//					uri_list += ((uri_list.length()>0)?",":"") + "'" + req.getId() + "'";
					uri_list += ((uri_list.length()>0)?",":"") + "'" + req.getURI() + "'";
					
					// get depth info
					int internal_notes = 0;
					if(req.getURI().indexOf("http://adapt2.sis.pitt.edu/kt/rest/ktree")>-1)
					{// of it's KT
						// request KT NodeIDs
						URL url = new URL("http://" + request.getServerName() + 
								((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath() +
								"/rest/forerun?" + RestDataRobot.REST_NODE_ID + "=" + req.getId());
						URLConnection u_conn = url.openConnection();
						BufferedReader u_in = new BufferedReader(new InputStreamReader(u_conn.getInputStream()));
						String node_ids = u_in.readLine();
						u_in.close();

						if(node_ids != null && !node_ids.equals("0"))
						{
							// query for number
							Statement u_stmt = conn.createStatement();
							ResultSet u_rs = u_stmt.executeQuery(
									"SELECT COUNT(*) AS NoOther FROM ent_note nt JOIN ent_node n ON(nt.URI=n.URI) WHERE IsPreviousVersion=0 " + 
									"AND nt.GroupID=" + s_group + " AND (IsShared=1 OR nt.UserID=" + s_user + ") AND NodeID  IN(" + node_ids + ");");
							if(u_rs.next())
							{
								internal_notes = u_rs.getInt("NoOther");
							}
							u_rs.close();
							u_rs = null;
							u_stmt.close();
							u_stmt = null;
						}
						
					}// of it's KT
					// end of -- get depth info
//if(internal_notes>0) System.out.println("- " + req.getURI() + " : " + internal_notes);					
					req.setId(0);
					req.setITag(0);
					req.setSTag("");
					if(internal_notes>0)
					{
						res_note_details.add(req);
						req.setSTag("" + internal_notes);
//System.out.println("o Id = " + req.getId() + " URI = " + req.getURI() + " notes inside " + internal_notes);
					}
				}

//System.out.println(" uri_list = " + uri_list);				
				
				in.close();
				in = null;
			}
			
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery("SELECT nt.* FROM ent_note nt WHERE IsPreviousVersion=0 AND nt.GroupID=" + s_group + " AND " + 
					"(IsShared=1 OR nt.UserID=" + s_user + ") " + ((uri_list.length()>0)?"AND nt.URI IN (" + uri_list + ")":"AND 1=2") + ";");
			
//			rs = stmt.executeQuery("SELECT nt.*, n.Title FROM ent_note nt JOIN ent_node n ON (nt.URI=n.URI) WHERE IsPreviousVersion=0 AND nt.GroupID=" + s_group + " AND " + 
//					"(IsShared=1 OR nt.UserID=" + s_user + ") " + ((uri_list.length()>0)?"AND n.NodeID IN (" + uri_list + ")":"AND 1=2") + ";");
			
//			note_details.removeAllElements();
			while (rs.next())
			{
				String n_uri = rs.getString("URI");
				int n_user_id = rs.getInt("UserID");
				
				// get the item
				iItem2 note_item = req_note_details.findByURI(n_uri);
				iItem2 added_item = res_note_details.findByURI(n_uri);
				if(added_item==null)
				{
					res_note_details.add(note_item);
//System.out.println("i Id = " + note_item.getId() + " URI = " + note_item.getURI());
				}
				
				if(n_user_id == i_user) note_item.setId(1);
				else note_item.setITag(note_item.getITag() + 1);
				
			}
			rs.close();
			rs = null;

			stmt.close();
			stmt = null;

			conn.close();
			conn = null;
			
			for(int i=0; i< res_note_details.size(); i++)
			{
				iItem2 note_item = res_note_details.get(i);
				int saved_s_tag = 0;
				if(note_item.getSTag() != null && note_item.getSTag().length()>0)
				{
					saved_s_tag = Integer.parseInt(note_item.getSTag());
//System.out.println(note_item.getURI() + " saved_s_tag " + saved_s_tag);				
				}
				String self = (note_item.getId()>0)?"_self":"";
				String others = "";
				if(note_item.getITag()>10)
					others = "_3";
				else if(note_item.getITag()>5)
					others = "_2";
				else if(note_item.getITag()>0)
					others = "_1";
				else 
					others = "_0";
				
				annotation_state += note_item.getURI() + "\t" + ((note_item.getId()>0)?"self":"----") + others +  "(" + note_item.getITag() + ")" + ((saved_s_tag > 0)?"+":"") + "\n";
				
				note_item.setSTag("&nbsp;" + 
						((note_item.getId()>0 || note_item.getITag()>0)?
						"<a id='note-btn' onclick='window.parent.openNote(" + s_user + "," + s_group + ",\"" + note_item.getURI() + "\",\"" + note_item.getTitle() + "\",\"main" + start_ns + "\");' target='_top' href='javascript:;'>" +
						"<img border='0' title='View/create/edit notes' alt='' src='" + request.getContextPath() + "/assets/icons/notes_annot" + self + others + ".gif' style='vertical-align: text-bottom;'/>" +
						"</a><span style='color:grey;'>" + ((note_item.getITag()>0)?"(" + note_item.getITag() + ")":"") + "</span>"
						:"") +
						((saved_s_tag > 0)?"<img title='Some item(s) in this folder have additional notes' src='" + request.getContextPath() + "/assets/icons/notes_more.gif' />":"")
				);
			}
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace(System.out);
		}
		catch (ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace(System.out);
		}
		
		note_batches_compiled = res_note_details.size();

//System.out.println("=======");		
//for(int i=0; res_note_details!=null && i<res_note_details.size(); i++)
//{
//	System.out.println(i + " Id = " + res_note_details.get(i).getId() + " URI = " + res_note_details.get(i).getURI());
//}
		
		
		if(REQ_FORMAT_JAVA_OBJ.equalsIgnoreCase(s_format))
		{
			ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
//			ArrayList a = new ArrayList();
//			a.addAll(note_details);
			
			out.writeObject(res_note_details);
			out.flush();
			out.close();
			out = null;
		}
			
		res_note_details.clear();
		res_note_details = null;
		req_note_details.clear();
		req_note_details = null;
		
		Long finis_ns = System.nanoTime();
		
		try
		{
			conn = sqlManager.getConnection();
			stmt = conn.createStatement();
			
			stmt.executeUpdate("INSERT INTO log_note_view (UserID, GroupID, URI, ViewMode, Feature, StartNS, FinishNS, CostMS, URIsSupplied, NoteBatchesCompiled, NavigationState) VALUES" + 
					"(" + s_user + "," + s_group + ",'" + s_uri + "','batch','--'," + start_ns + "," + finis_ns + "," + (double)((double)finis_ns-start_ns)/1000000 + "," + uris_supplied + "," + note_batches_compiled + ",'" + annotation_state + "');");
			
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