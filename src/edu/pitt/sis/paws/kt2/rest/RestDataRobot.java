package edu.pitt.sis.paws.kt2.rest;

import java.util.*;
import java.sql.*;

import com.hp.hpl.jena.vocabulary.DCTypes;
import edu.pitt.sis.paws.core.utils.*;

public class RestDataRobot
{
	// Constants
	//		Portal
	public static final String REST_NODE_ID = "node_id";
	
	//		Common constants
	public static final String REST_EMBED = "_embed"; // embeds use actual URLs not KTree URLs
	public static final String REST_FORMAT = "_format";
	public static final String REST_FORMAT_HTML = "html";
	public static final String REST_FORMAT_RDF = "rdf";
	public static final String REST_CONTEXT_PATH = "context_path";
	public static final String REST_STATUS = "Status";
	public static final String REST_STATUS_OK = "OK";
	public static final String REST_STATUS_ERROR = "Error";
	public static final String REST_METHOD = "_method";
	public static final String REST_METHOD_DELETE = "delete";
	
	public static final String REST_MORE = "more";
	public static final String REST_MORE_YES = "true";
	public static final String REST_MORE_NO = "false";
	
	public static final String REST_RESULT = "Result";

	private static Map<String, String> getNodeChildrenIDs(Map<String, String> _parameters, Connection _conn)
	{
		String _node_id = _parameters.get(REST_NODE_ID);
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		String result = "";
		boolean more = false;
		
		try
		{
			Statement stmt = _conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT n.NodeID, n.FolderFlag FROM ent_node n JOIN rel_node_node nn " +
					"ON(n.NodeID=nn.ChildNodeID) WHERE nn.ParentNodeID IN(" + _node_id + ");");
			
			while(rs.next())
			{
				result += ((result.length()>0)?",":"") + rs.getInt("NodeID") + "";
				more = more || (rs.getInt("FolderFlag") == 1);
			}
			
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
		}
		catch(SQLException sqle)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("SQL Exception while retrieving node  children info .", _context_path));
			sqle.printStackTrace(System.out);
		}
		
		_parameters.put(REST_STATUS, REST_STATUS_OK);
		_parameters.put(REST_RESULT, result);
		_parameters.put(REST_MORE, Boolean.toString(more));
		
		return _parameters;
	}
	
	public static Map<String, String> getNodeForerunnersIDs(Map<String, String> _parameters, SQLManager _sqlm)
	{
		String _node_id = _parameters.get(REST_NODE_ID);
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		
		Connection conn = null;
		String result = "";
		
		try
		{// retrieve user by login from db
			conn = _sqlm.getConnection();
			
			String current_node_id = _node_id;
			Map<String, String> _para = new HashMap<String, String>();
			_para.put(REST_CONTEXT_PATH, _context_path);
			boolean go_on = true;
			
			while(go_on)
			{
				_para.put(REST_NODE_ID, current_node_id);
				_para = getNodeChildrenIDs(_para, conn);
				
				if(_para.get(REST_STATUS).equals(REST_STATUS_ERROR)) return _para;
				
				current_node_id = _para.get(REST_RESULT);
				result += ((result.length()>0 && current_node_id!=null && current_node_id.length()>0)?",":"") + current_node_id;
				
				go_on = _para.get(REST_MORE).equalsIgnoreCase(Boolean.toString(true));
			}
			
			
			conn.close();
			conn = null;
		}
		catch(SQLException sqle)
		{
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("SQL Exception while retrieving node info.", _context_path));
			sqle.printStackTrace(System.out);
		}

		_parameters.put(REST_STATUS, REST_STATUS_OK);
		_parameters.put(REST_RESULT, ((result.length()>0)?result:"0"));
		return _parameters;
	}
	
	
	public static Map<String, String> getNodeInfo(Map<String, String> _parameters, SQLManager _sqlm, boolean multiple_nodes)
	{
		String node_id = _parameters.get(REST_NODE_ID);
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		if(multiple_nodes)
		{// get user info by ID
			;
		}// end of -- get user info by ID
		else if(node_id != null && node_id.length() != 0)
		{// get user info by Login or ID
			String qry = "SELECT * FROM ent_node WHERE NodeID=" + node_id + ";";
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			
			try
			{// retrieve user by login from db
				conn = _sqlm.getConnection();
				stmt = conn.prepareStatement(qry);
				rs = stmt.executeQuery();
				
				_parameters = formatNodeInfo(rs, _parameters, false /*multiple_nodes*/, conn);

				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
				conn.close();
				conn = null;
				
			}// end of -- retrieve user by login from db
			catch(SQLException sqle)
			{
				_parameters.put(REST_STATUS, REST_STATUS_ERROR);
				_parameters.put(REST_RESULT, getErrorMessageHTML("SQL Exception while retrieving node info.", _context_path));
				sqle.printStackTrace(System.out);
			}
			finally
			{
//				_sqlm.freeConnection(conn);
				if (rs != null) 
				{
					try { rs.close(); } catch (SQLException e) { ; }
					rs = null;
				}
				if (stmt != null) 
				{
					try { stmt.close(); } catch (SQLException e) { ; }
					stmt = null;
				}
				if (conn != null)
				{
					try { conn.close(); } catch (SQLException e) { ; }
					conn = null;
				}
			}
		}// end of -- get user info by Login or ID
		else
		{// no parameters
			_parameters.put(REST_STATUS, REST_STATUS_ERROR);
			_parameters.put(REST_RESULT, getErrorMessageHTML("Node ID specified incorrectly", _context_path));
		}// end of -- no parameters
		
		return _parameters;
	}
	
	
	private static Map<String, String> formatNodeInfo(ResultSet _rs, Map<String, String> _parameters, boolean multiple_nodes,
			Connection _conn) throws SQLException
	{
		String _node_id = _parameters.get(REST_NODE_ID);
		String _context_path = _parameters.get(REST_CONTEXT_PATH);
		String _format = _parameters.get(REST_FORMAT);
		String _embed = _parameters.get(REST_EMBED);
		
		
		if(!multiple_nodes)
		{// single node
			if(_rs.next())
			{// node exists
				String title = _rs.getString("Title");
//				String description = _rs.getString("Description");
				String node_uri = _rs.getString("URI");
				int user_id = _rs.getInt("UserID");
				String dt_created = _rs.getString("DateCreated");
				String dt_modified = _rs.getString("DateModified");
				int item_type_id = _rs.getInt("ItemTypeID");
				int is_folder = _rs.getInt("FolderFlag");
				
				String result = "";
				
				if(_format == null || _format.length() == 0 || _format.equals(REST_FORMAT_HTML))
				{// format is html
					// retrieve groups
				}// end of -- format is html
				else if(_format != null && _format.length() != 0 && _format.equals(REST_FORMAT_RDF))
				{//format is rdf
					if(is_folder==1)
					{// folder is a feed
						
						String qry = "SELECT n.*, nn.Weight FROM ent_node n JOIN rel_node_node nn ON(nn.ChildNodeID=n.NodeID) " +
								"WHERE nn.ParentNodeID = " + _node_id + " AND Hidden=0 ORDER BY nn.OrderRank;";
						String item_list_short = "";
						String item_list_full = "";
						
						PreparedStatement __stmt = _conn.prepareStatement(qry);
						ResultSet __rs = __stmt.executeQuery();
						while (__rs.next())
						{
							int __node_id = __rs.getInt("NodeID");
							String __title = __rs.getString("Title");
							String __description = __rs.getString("Description");
							String __uri = __rs.getString("URI");
							String __url = __rs.getString("URL"); // USE EXTERNAL URL
							int __folder_flag = __rs.getInt("FolderFlag");
							double __weight = __rs.getDouble("Weight");
							
//							System.out.println("_embed=" + _embed);
							// USE INTERNAL KTree URL if not embed
							if(_embed==null || _embed.length()==0)
								__url = _context_path + "/content/Show?id=" + __node_id;
							
							__url = __url.replace("&", "&amp;");
							
							int __user_id = __rs.getInt("UserID");
							String __dt_created = __rs.getString("DateCreated");
							String __dt_modified = __rs.getString("DateModified");
							int __item_type_id = __rs.getInt("ItemTypeID");
							int __is_folder = __rs.getInt("FolderFlag");
							String item_uri = __uri.replaceAll("&", "&amp;"); //_context_path + "/rest/ktree" + __node_id;
//System.out.println("__title " + __title + " --> " + __title.replaceAll("&", "&amp;"));							
							__title = __title.replaceAll("&", "&amp;");
							item_list_short += "				<rdf:li rdf:resource=\"" + item_uri + "\"/>\n"; 

							item_list_full += "	<item rdf:about=\"" + item_uri + "\">\n" +
									"		<rdf:value rdf:datatype='http://www.w3.org/2001/XMLSchema#double'>" + __weight + "</rdf:value>" +
									"		<title rdf:parseType='Literal'>" + __title + "</title>\n" +
									"		<link>" + ((__url == null || __url.length() == 0)?__uri:__url) + "</link>\n" +
//									(((__description != null) && (__description.length() > 0)) ? "		<description xml:lang='en' rdf:parseType='Literal'>" + __description + "</description>\n" : "") +
									((__folder_flag==1)?"<rdf:type rdf:resource='" + DCTypes.Collection.toString() + "'/>\n":"") +
									"	</item>\n";
						}
						__rs.close();
						__stmt.close();

						String rss_link = _context_path + "/rest/ktree" + _node_id;
						title = title.replaceAll("&", "&amp;");
						result += "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
							"<rdf:RDF\n" +
							"	xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
//							"	xmlns:dcterms=\"http://purl.org/dc/terms/\"\n" +
							"	xmlns=\"http://purl.org/rss/1.0/\"\n" +
//							"	xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
							">\n" + "	<channel rdf:about=\"" + rss_link + "\">\n" +
							"		<title rdf:parseType='Literal'>KnowledgeTree:: " + title + "</title>\n" +
							"		<link>" + _context_path + "/content/Show?id=" + _node_id + "</link>\n" +
//// 							"		<description>KnowledgeTree:: " + title + "(#" + _node_id + ")</description>\n" +
//							(((description != null) && (description.length() > 0)) ? "		<description xml:lang='en' rdf:parseType='Literal'>" + description + "</description>\n" : "") +
							"		<image rdf:resource=\""	+ _context_path + "/assets/knowledgetree_rss_image.gif\" />\n" +
							"		<items>\n" +
							"			<rdf:Seq>\n" + item_list_short + "			</rdf:Seq>\n" +
							"		</items>\n" +
							"	</channel>\n" + 
							"	<image rdf:about=\"" + _context_path + "/assets/knowledgetree_rss_image.gif\">\n" +
							"		<url>" + _context_path + "/assets/knowledgetree_rss_image.gif</url>\n" +
							"		<title rdf:parseType='Literal'>KnowledgeTree:: " + title + "</title>\n" +
							"		<link>" + rss_link + "</link>\n" +
							"	</image>\n" +
							item_list_full +
							"</rdf:RDF>";

					}// end of -- folder is a feed
					else
					{// single document is an item
						result = "";
//							getPageHeaderRDF() + "\n" + 
//							"	<foaf:Person rdf:about='" + _context_path + "/rdf/users#" + login + "'>\n" + 
//							"		<foaf:name>" + name + "</foaf:name>\n" + 
//							"		<foaf:holdsAccount>\n" + 
//							"			<foaf:OnlineAccount>\n" + 
//							"				<foaf:accountServiceHomepage rdf:resource='" + _context_path + "/login.jsp'/>\n" + 
//							"				<foaf:accountName>" + login + "</foaf:accountName>\n" + 
//							"			</foaf:OnlineAccount>\n" + 
//							"		</foaf:holdsAccount>\n" + 
//							"		<foaf:mbox_sha1sum>" + Digest.SHA1(email) + "</foaf:mbox_sha1sum>\n" + 
//							"		<vCard:note>" + how + "</vCard:note>\n" + 
//							"		<rdfs:isDefinedBy rdf:resource='" + _context_path + "/rdf/users'/>\n" + 
//							"	</foaf:Person>\n" + 
//							"</rdf:RDF>";
					}// end of -- single document is an item
					
					_parameters.put(REST_STATUS, REST_STATUS_OK);
					_parameters.put(REST_RESULT, result);
				}// end of -- format is rdf
				else
				{
					_parameters.put(REST_STATUS, REST_STATUS_ERROR);
					_parameters.put(REST_RESULT, getErrorMessageHTML("Specified format of data is not supported", _context_path));
				}
				
			}// end of -- node exists
			else
			{
				_parameters.put(REST_STATUS, REST_STATUS_ERROR);
				_parameters.put(REST_RESULT, getErrorMessageHTML("Specified user has not been found", _context_path));
			}
		}// end of -- single node
		else
		{// multiple nodes
//			if(_format != null && _format.length() == 0 && (!_format.equals(REST_FORMAT_HTML)) && (!_format.equals(REST_FORMAT_RDF)) )
//			{
//				_parameters.put(REST_STATUS, REST_STATUS_ERROR);
//				_parameters.put(REST_RESULT, getErrorMessageHTML("Specified format of data is not supported", _context_path));
//			}
//			else
//			{// format ok
//				String result = "";
//				// HEADER
//				if(_format == null || _format.length() == 0 || _format.equals(REST_FORMAT_HTML))
//				{
//					String home_html = "<a href='" + _context_path + "/rest'><img src='" + _context_path + "/assets/home.gif' title='Home' alt='Home' border='0'></a>";
//					String rdf_html = "<a href='" + _context_path + "/rdf/users'><img src='" + _context_path + "/assets/rdf.gif' title='RDF' alt='RDF' border='0'></a>";
//					String add_html = "<a href='" + _context_path + "/rest/group/world/users/new'><img src='" + _context_path + "/assets/add2_enable.gif' title='Add New User' alt='Add' border='0'></a>";
//					result = getPageHeaderHTML("Knowledge Tree - User Info", _context_path) +
//							"<table cellpadding='2px' cellspacing='0px' class='rest_user_table' width='500px'>\n"+
//							"	<caption class='rest_user_table_caption'>Users</caption>\n"+
//							"	<tr>\n"+
//							"	  <td class='rest_user_table_header'>" + home_html + "</td>\n"+
//							"	  <td class='rest_user_table_header' align='right'>" + add_html + "&nbsp;&nbsp;" + rdf_html + "</td>\n"+
//							"  	</tr>\n";
//				}
//				else if(_format != null && _format.length() != 0 && _format.equals(REST_FORMAT_RDF))
//					result = getPageHeaderRDF();
//				
//				int user_count = 0;
//				
//				while(_rs.next())
//				{// for all users
//					user_count ++;
//					
//					String login = _rs.getString("Login");
//					String name = _rs.getString("Name");
//					String email = _rs.getString("Email");
////					String org = _rs.getString("Organization");
////					String city = _rs.getString("City");
////					String country = _rs.getString("Country");
//					String how = _rs.getString("How");
//					
//					if(_format == null || _format.length() == 0 || _format.equals(REST_FORMAT_HTML))
//					{// format is html
////						// retrieve groups
////						String groups = "<div name='openerControl'>Groups</div>\n<div name='opener'>";
////						String gqry = 
////							"SELECT g.Login, g.Name "+
////							"FROM ent_user u RIGHT JOIN rel_user_user uu ON(uu.ChildUserID=u.UserID) "+
////							"RIGHT JOIN ent_user g ON(uu.ParentUserID=g.UserID) "+
////							"WHERE u.Login='" + login + "';";
////						PreparedStatement stmt = _conn.prepareStatement(gqry);
////						ResultSet grs = SQLManager.executeStatement(stmt);
////						while(grs.next())
////						{
////							String gname = grs.getString("Name");
////							String gmnemonic = grs.getString("Login");
////							groups += "<div>&nbsp;&nbsp;<a href='" + _context_path + "/rest/group/" + gmnemonic + "'>" + gname + "</a></div>";
////						}
////						groups += "</div>";
////
////						grs.close();
////						stmt.close();
////						// end of -- retrieve groups
////						String home_html = "<a href='" + _context_path + "/rest'><img src='" + _context_path + "/assets/home.gif' title='Home' alt='Home' border='0'></a>";
////						String view_html = "<a href='" + _context_path + "/rest/user/" + login + "'><img src='" + _context_path + "/assets/view_enabled.gif' title='View' alt='View' border='0'></a>";
////						String rdf_html = "<a href='" + _context_path + "/rdf/user/" + login + "'><img src='" + _context_path + "/assets/rdf.gif' title='RDF' alt='RDF' border='0'></a>";
////						String edit_html = "<a href='" + _context_path + "/rest/user/" + login + "/edit'><img src='" + _context_path + "/assets/edit2_enable.gif' title='Edit' alt='Edit' border='0'></a>";
//						
//						result += 
//							"<tr>\n" +
//							"	<td valign='top'>&nbsp;&nbsp;&bull;</td>\n" +
//							"	<td><a href='" + _context_path + "/rest/user/" + login + "'>" + name + "&nbsp;(" + login + ")</a></td>\n" +
//							"</tr>\n";
//							
////							"<table cellpadding='2px' cellspacing='0px' class='rest_user_table' width='500px'>\n"+
////							"	<caption class='rest_user_table_caption'>" + ((name!=null && name.length()>0)?name:"{unspecified}") + "</caption>\n"+
////							"	<tr>\n"+
////							"	  <td class='rest_user_table_header'>" + home_html + "</td>\n"+
////							"	  <td class='rest_user_table_header' align='right'>" + edit_html + "&nbsp;&nbsp;&nbsp;&nbsp;" + rdf_html + "&nbsp;&nbsp;" + view_html + "</td>\n"+
////							"  	</tr>\n"+
////							"	<tr>\n"+
////							"		<td>Login</td>\n"+
////							"		<td width='100%'>" + login + "</td>\n"+
////							"	</tr>\n"+
////							"	<tr>\n"+
////							"		<td>Email</td>\n"+
////							"		<td>" + email.replaceAll("@", "(at)").replaceAll("\\.", "(dot)") + "</td>\n"+
////							"	</tr>\n"+
////							"	<tr>\n"+
////							"		<td>Organization</td>\n"+
////							"		<td>" + org + "</td>\n"+
////							"	</tr>\n"+
////							"	<tr>\n"+
////							"		<td>City</td>\n"+
////							"		<td>" + city + "</td>\n"+
////							"	</tr>\n"+
////							"	<tr>\n"+
////							"		<td>Country</td>\n"+
////							"		<td>" + country + "</td>\n"+
////							"	</tr>\n"+
////							"	<tr valign='top'>\n"+
////							"		<td>Notes</td>\n"+
////							"		<td>" + how + "</td>\n"+
////							"	</tr>\n"+
////							"	<tr>\n"+
////							"		<td valign='top' class='rest_user_table_footer'>Member of</td>\n"+
////							"		<td class='rest_user_table_footer'>" + groups + "</td>\n"+
////							"	</tr>\n"+
////							"</table>\n<p/>\n";
//		
//					}// end of -- format is html
//					else if(_format != null && _format.length() != 0 && _format.equals(REST_FORMAT_RDF))
//					{//format is rdf
//						result += 
//							"	<foaf:Person rdf:about='" + _context_path + "/rdf/users#" + login + "'>\n" + 
//							"		<foaf:name>" + name + "</foaf:name>\n" + 
//							"		<foaf:holdsAccount>\n" + 
//							"			<foaf:OnlineAccount>\n" + 
//							"				<foaf:accountServiceHomepage rdf:resource='" + _context_path + "/login.jsp'/>\n" + 
//							"				<foaf:accountName>" + login + "</foaf:accountName>\n" + 
//							"			</foaf:OnlineAccount>\n" + 
//							"		</foaf:holdsAccount>\n" + 
//							((email != null && email.length() >0 )?"		<foaf:mbox_sha1sum>" + Digest.SHA1(email) + "</foaf:mbox_sha1sum>\n":"") + 
//							"		<vCard:note>" + how + "</vCard:note>\n" + 
//							"		<rdfs:isDefinedBy rdf:resource='" + _context_path + "/rdf/users'/>\n" + 
//							"	</foaf:Person>\n";
//					}// end of -- format is rdf
//				}// end of -- for all users
//				
//				
//				// FOOTER
//				if(_format == null || _format.length() == 0 || _format.equals(REST_FORMAT_HTML))
//					result +=
//						"	<tr>\n"+
//						"		<td class='rest_user_table_footer'>&nbsp;</td>\n"+
//						"		<td class='rest_user_table_footer'>" + user_count + " user(s)</td>\n"+
//						"	</tr>\n"+
//						"</table>\n" +
//						"</body></html>";
//				else if(_format != null && _format.length() != 0 && _format.equals(REST_FORMAT_RDF))
//					result += "</rdf:RDF>";
//					
//				_parameters.put(REST_STATUS, REST_STATUS_OK);
//				_parameters.put(REST_RESULT, result);
//			}// end of -- format ok
		}// end of -- multiple nodes
		return _parameters;
	}

	
	private static String getPageHeaderHTML(String _title, String _context_path)
	{
		String result =
			"<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN' 'http://www.w3.org/TR/html4/strict.dtd'>\n"+ 
			"<html><head>\n"+
			"<title>" + _title + "</title>\n"+
			"<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n"+
			"<link rel='StyleSheet' href='" + _context_path + "/assets/rest.css' type='text/css' />\n"+
			"<script type='text/javascript' src='" + _context_path + "/assets/rest.js'></script>\n"+
			"</head><body onload='opener.init(\"" + _context_path + "/assets/\");'>\n";
		
		return result;
	}
	
	
	public static String getPageHeaderRDF()
	{
		String result =
			"<?xml version='1.0' encoding='utf-8'?>\n" +
			"<rdf:RDF\n" +
			"		xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" +
			"		xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'\n" +
			"		xmlns:foaf='http://xmlns.com/foaf/0.1/'\n" +
			"		xmlns:vCard='http://www.w3.org/2006/vcard/ns#'\n" +
			"		xmlns:dc='http://purl.org/dc/elements/1.1/'\n" +
			"		xmlns:dcterms='http://purl.org/dc/terms/'\n" +
			"		xmlns:rss='http://purl.org/rss/1.0/'>";
		
		return result;
	}
	
	
	public static String getErrorMessageHTML(String _message, String _context_path)
	{
		String result = 
			getPageHeaderHTML("Knowledge Tree - Error", _context_path) +
			"<table cellpadding='0px' cellspacing='0px'>"+
			"<tr>"+
			"	<td class='error_table_caption'>Error</td>"+
			"</tr>"+
			"<tr>"+
			"	<td class='error_table_message'>" + _message + "</td>"+
			"</tr>"+
			"</table></body></html>";
		
		return result;
	}

	
	public static String getMessageHTML(String _title, String _message, String _context_path, String _style)
	{
		String result = 
			getPageHeaderHTML("Knowledge Tree - " + _title, _context_path) +
			"<table cellpadding='2px' cellspacing='0px' width='400' class='" + _style + "'>\n"+
			"<tr>\n"+
			"	<td class='" + _style + "_caption'>" + _title + "</td>\n"+
			"</tr>\n"+
			"<tr><td class='" + _style + "_message'>"+ _message + "</td></tr>\n"+
			"</table>";
		return result;
	}

	private static String clrStr(String _str)
	{
		return (_str == null || _str.length() == 0)?"":_str;
	}
}
