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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import org.apache.commons.fileupload.ParameterParser;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;
import edu.pitt.sis.paws.core.Item2Vector;
import edu.pitt.sis.paws.core.utils.SQLManager;

/**
 * ResourceMap class is the central storage of the structural data of the
 * client application (node tree, concepts, etc). ResourceMap is responsible
 * for managing these structures and communicating any changes to the portal.
 * ResourceMap should be stored as a session object and exist as a single
 * instance per user/portal connection.
 */

public class ResourceMap
{
	protected int user_id;
	protected int group_id;
	
	protected User user;
	protected User group;
	
	protected Item2Vector<iNode> nodes;
	protected Item2Vector<User> users;
	protected Vector<Right> rights;

	/** All ratings in the current view of the portal
	 * @since 1.5
	 */
	protected Vector<Rating> ratings;

	/** Rights that apply to all nodes */
	protected Vector<Right> globally_defined_rights;
	/** Rights that apply to all users */
	protected Vector<Right> globally_accessible_rights;
	/** This is a count for virtual nodes that do not exist in the database
	 * but represent some database records, the id's of such nodes are 
	 * negative. */
	protected int top_virtual_node;
	/** collction of the nodes that aren't stored in the DB, but wrap some
	 * objects from DB */
//	private Item2Vector<iNode> virtual_nodes;

	/** List of registered types of items on the portal */
	protected Item2Vector<ItemType> itemtype_list;

	/** the id of the root node */
	protected final int root_node_id = 1;
	/** root node of the portal */
	protected iNode root_node;

	/** the id of the node that serves as a trash bin */
	protected int bin_node_id;
	/** the the node that serves as a trash bin */
	protected iNode bin_node;
	
	/** Node that is temporatily added in the "add new" mode before being sumbitted to the DB*/
	protected iNode pending_node;

	public Item2Vector<ItemType> getItemtypeList() { return itemtype_list; }
	public int getUserId() { return user_id; }
	public int getGroupId() { return group_id; }
	
	public User getUser() { return user; }
	public User getGroup() { return group; }
	
//	private void resetRootNode() { root_node = nodes.findById(root_node_id); }

	public ResourceMap(int _user_id, int _group_id, SQLManager sqlm)
	{
		// clocking
		Calendar start = null;
		Calendar finish = null;
		long diff_mills;
		start = new GregorianCalendar();
		
		user_id = _user_id;
		group_id = _group_id;
		
		//set collections
		nodes = new Item2Vector<iNode>();
		users = new Item2Vector<User>();
		rights = new Vector<Right>();
		globally_accessible_rights = new Vector<Right>();
//		globally_defined_rights = new Vector<Right>();
//		virtual_nodes = new Item2Vector<iNode>();
		ratings = new Vector<Rating>();
		itemtype_list = new Item2Vector<ItemType>();
		pending_node = null;
		bin_node = null;
		root_node = null;
		// end of -- set collections
		
		ResultSet rs = null;
		java.sql.Statement stmt = null;
		Connection conn = null;
		String qry = "";

		ArrayList<Connection> al_conn = new ArrayList<Connection>();
		ArrayList<java.sql.Statement> al_stmt = new ArrayList<java.sql.Statement>();
		ArrayList<ResultSet> al_rs = new ArrayList<ResultSet>();
		
		// WORK WITH DATABASE
		try
		{
			conn = sqlm.getConnection();
			al_conn.add(conn);
			
			// load user and group
			qry = "SELECT * FROM ent_user "; //WHERE UserID IN(" + _user_id + "," + _group_id + ");";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			
			while(rs.next())
			{
				int user_id = rs.getInt("UserID");
				String user_name = rs.getString("Name");
				String user_login = rs.getString("Login");
				String user_uri = rs.getString("URI");
				boolean is_group = (rs.getInt("isGroup")!=0)?true:false;
				
				User new_user = new User(user_id,user_name,user_uri,user_login, is_group);

				if(!new_user.getIsGroup() || (new_user.getIsGroup() && new_user.getId() == _group_id))
					this.users.add( new_user );

				if(new_user.getIsGroup() && new_user.getId() == _group_id)
					this.group = new_user;
				if(new_user.getId() == _user_id)
					this.user = new_user;
			}
			rs.close();
			stmt.close();
			rs = null;
			stmt = null;
			al_rs.add(rs);
			al_stmt.add(stmt);
			
			System.out.println("... [KTree2] Starting. Users added (inc one group): " + this.users.size());
			
			// load user rights
			qry = "SELECT * FROM ent_right WHERE UserID IN(" + Right.USER_ALL + "," + _user_id + "," + _group_id + ");";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			
			ArrayList<Integer> node_id_list = new ArrayList<Integer>();
			boolean see_from_root = false;
			
			while(rs.next())
			{
				// read data
				int a_user_id = rs.getInt("UserID");
				int right_type = rs.getInt("RightTypeID");
				int parent_type = rs.getInt("ParentTypeID");
				int child_type = rs.getInt("ChildTypeID");
				int a_node_id = rs.getInt("NodeID");
				int quantity = rs.getInt("Quantity");
				String desc = rs.getString("Description");
				int ownerFlag = rs.getInt("OwnerFlag");
				// process fata
				int user_macro = ( (a_user_id != Right.USER_ALL) &&
					(a_user_id != Right.USER_AUTHOR) ) ?
					((ownerFlag==1)? Right.USER_AUTHOR : 0) : a_user_id;

				User a_user = (user_macro == Right.USER_ALL ) ? null : users.findById(a_user_id);
				
				node_id_list.add(a_node_id);
				see_from_root = (!see_from_root && a_node_id==1)?true:see_from_root;

				if(a_user==null && user_macro != Right.USER_ALL)
					System.out.println("!!! [KTree2] SEVERE ResourceMap:: right's user cannot be found (id=)" + a_user_id);;
				
				Right right = new Right(a_node_id, desc, a_user, user_macro, right_type, null /*node*/,
					parent_type, child_type, quantity, (ownerFlag==1));
				right.setITag(a_node_id); // save node id in the integer tag
				
				rights.add(right);

				if(user_macro == Right.USER_ALL)
					globally_accessible_rights.add(right);

				// Add rights to target elements - user and node
				if(a_user != null) user.getRights().add(right);
				
			}// end of -- right's while
			
			rs.close();
			stmt.close();
			rs = null;
			stmt = null;
			al_rs.add(rs);
			al_stmt.add(stmt);
			System.out.println("... [KTree2] Starting. Rights added: " + this.rights.size());
			
			// get rooted tree
			String cumulative_list = "";
			for(int i=0; i<node_id_list.size(); i++)
			{
				qry = "SELECT node_root_path(" + node_id_list.get(i).intValue() + 
						", '" + node_id_list.get(i).intValue() + "') AS List;";
				stmt = conn.createStatement();
				rs = stmt.executeQuery(qry);
				
				if(rs.next())
				{
					String list = rs.getString("List");
					if(list==null || list.equalsIgnoreCase("null"))
						;
					else
						cumulative_list += ( (cumulative_list.length() == 0)?"":"," ) + list;
				}
				rs.close();
				stmt.close();
				rs = null;
				stmt = null;
				al_rs.add(rs);
				al_stmt.add(stmt);
			}
			
			// load item type map
			qry = "SELECT * FROM voc_itemtype WHERE type >0 ORDER BY ItemTypeID;";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			
			while(rs.next())
			{// cycle item types
				int itemtype_id = rs.getInt("ItemTypeID");
				String itemtype_title = rs.getString("Title");
				String itemtype_desc = rs.getString("Description");
				int itemtype_type = rs.getInt("Type");
				String itemtype_url = rs.getString("URL");
				String itemtype_icon = rs.getString("Icon");
				String class_name = rs.getString("ClassName");
				String resource_url_suffix = rs.getString("ResourceURLSuffix");
								
				itemtype_title = ((itemtype_title == null)?"":itemtype_title);
				itemtype_desc = ((itemtype_desc == null)?"":itemtype_desc);
				itemtype_url = ((itemtype_url == null)?"":itemtype_url);
				itemtype_icon = ((itemtype_icon == null)?"":itemtype_icon);
				
				ItemType a_itype = new ItemType(itemtype_id, itemtype_title, itemtype_url,
						itemtype_desc, itemtype_type, itemtype_icon, class_name, resource_url_suffix);
				
				itemtype_list.add(a_itype);
			}// end of - cycle item types
			rs.close();
			stmt.close();
			rs = null;
			stmt = null;
			al_rs.add(rs);
			al_stmt.add(stmt);
			System.out.println("... [KTree2] Starting. ItemTypes added: " + this.itemtype_list.size());
			
			
			// now select nodes from rooted tree
			qry = "SELECT * FROM ent_node WHERE NodeID IN(" + cumulative_list + ")" +
				((see_from_root)
					?" OR NodeID IN(SELECT NodeID FROM rel_node_node nn JOIN ent_node n "+
							"ON(nn.ChildNodeID=n.NodeID) WHERE nn.ParentNodeID=1)"
					:"") + ";";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			
			while(rs.next())
			{// cycle nodes
				int node_id = rs.getInt("NodeID");
				String node_title = rs.getString("Title");
				String node_descr = rs.getString("Description");
				int a_user_id = rs.getInt("UserID");
				int node_type = rs.getInt("ItemTypeID");
				String node_url = rs.getString("URL");
				String node_uri = rs.getString("URI");
				int folder_flag_i = rs.getInt("FolderFlag");
				String node_icon = rs.getString("Icon");
				int hidden_i = rs.getInt("Hidden");
				boolean hidden = hidden_i==1;
				int multiurl_i = rs.getInt("MultiURL");
				boolean multiurl = multiurl_i==1;
				String cond_hidden = rs.getString("ConditionallyHidden");

				User user = users.findById(a_user_id);
				node_descr = (node_descr==null) ? "" : node_descr ;
				node_url = (node_url==null) ? "" : node_url ;
				node_uri = (node_uri==null) ? "" : node_uri ;
				node_icon = (node_icon==null) ? "" : node_icon ;
				boolean folder_flag = (folder_flag_i == 1);

				// decode the type of the node
				ItemType itype = this.itemtype_list.findById(node_type);
				
				// work with conditional hiding
				if(!hidden && cond_hidden != null && cond_hidden.length()>0 && 
						cond_hidden.indexOf("{" + this.group.getLogin() + "}")>=0)
				{
					hidden = true;
				}
				
				// Deal with MultiURL flag and the actual URL
				if(multiurl && node_type == iNode.NODE_TYPE_I_PSERVICED_FOLDER)
				{
					ArrayList checkMulti = null;
					checkMulti = checkMultiURLNode(node_url, itype, node_type);
					node_url = (String)checkMulti.get(0);
					itype = (ItemType)checkMulti.get(1);
					node_type = ((Integer)checkMulti.get(2)).intValue();
						
					/*
					// get the URL
					ParameterParser pp = new ParameterParser();
					Map mp = pp.parse(node_url, ';');
					String all_url = (String)mp.get("all");
					String group_url = (String)mp.get(this.group.getTitle());
					String the_url = null; 
					// group url
					if(group_url != null && group_url.length()>0 && !group_url.equalsIgnoreCase("none"))
					{
						the_url = group_url;
					}
					// if not group ckech all
					if((group_url == null || group_url.length()==0) && all_url != null && all_url.length()>0)
					{
						the_url = all_url;
					}
					if(the_url != null)
					{// we found the right URL
						node_url = the_url;
					}
					else
					{// no url at all switch to regular node
						itype = this.itemtype_list.findById(iNode.NODE_TYPE_I_FOLDER);
						node_url = "";
					}
					*/
				}
				
				
//if(itype == null) System.out.println("!! nodeID=" + node_id + " type=" + node_type);
				String node_class_name = (itype != null)? itype.getClassName() : Node.class.getName();
				iNode node_class = null;
				iNode a_node = null;
				// load class
				try
				{
					node_class = (iNode)Class.forName(node_class_name).newInstance();
					a_node = node_class.NodeFactory(node_id, node_title, node_uri,
							node_type, node_descr, node_url, folder_flag, node_icon, hidden);
				}
				catch(ClassNotFoundException cnfe)
				{
					cnfe.printStackTrace(System.out);
					System.out.println("!!! [KTree2] SEVERE:: ResourceMap Loading not via NodeFactory");					
					a_node = new Node(node_id, node_title, node_uri,
							node_type, node_descr, node_url, folder_flag, node_icon, hidden);
				}
				
				a_node.setCreator(user);
				
				this.getNodes().add(a_node);

				switch (node_type)
				{// node_type
					case iNode.NODE_TYPE_I_ALL: // Root Node
					{
						this.root_node = a_node;
					}
					break;
					case iNode.NODE_TYPE_I_BIN: // Recycled bin
					{
						this.bin_node_id = a_node.getId();
						this.bin_node = a_node;
					}
					break;
				}// end of -- node_type
				
			}// end of - cycle nodes
			rs.close();
			stmt.close();
			rs = null;
			stmt = null;
			al_rs.add(rs);
			al_stmt.add(stmt);
			
			// set globally_defined_rights
			globally_defined_rights = root_node.getRights();
			
			// select connections
			qry = "SELECT * FROM rel_node_node WHERE ChildNodeID IN(" + cumulative_list + ")" +
			((see_from_root)
				?" OR ChildNodeID IN(SELECT NodeID FROM rel_node_node nn JOIN ent_node n "+
						"ON(nn.ChildNodeID=n.NodeID) WHERE nn.ParentNodeID=1)"
				:"") + " ORDER BY ParentNodeID, OrderRank;";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(qry);
			while(rs.next())
			{
				int parent_node_id = rs.getInt("ParentNodeID");
				int child_node_id = rs.getInt("ChildNodeID");
				double weight = rs.getInt("Weight");
				iNode parent_node = nodes.findById(parent_node_id);
				iNode child_node = nodes.findById(child_node_id);
				if(parent_node==null || child_node == null)
				{
					System.out.println("!!! [KTree2] SEVERE ResourceMap:: (parent_node==null):" + (parent_node==null) + "[" + parent_node_id + "]" +
							"(child_node==null):" + (child_node==null) + "[" + child_node_id + "]");
				}
				else
				{
					parent_node.getChildren().add(child_node, weight);
					child_node.setParent(parent_node);
				}
			}
			rs.close();
			stmt.close();
			rs = null;
			stmt = null;
			al_rs.add(rs);
			al_stmt.add(stmt);
			
			// update fully loaded status
			// leave false for "terminal" nodes only, that have no children so far
			for(int i=0; i<this.getNodes().size(); i++)
			{
				if(this.getNodes().get(i).getChildren().size() > 0)
					this.getNodes().get(i).setFullyLoaded(true);
			}
			
			// connect rights back
			Vector<Right> rights_to_remove = new Vector<Right>();
			for(int i=0; i<this.getRights().size(); i++)
			{
				Right a_right = this.getRights().get(i);
				iNode a_node = this.getNodes().findById(a_right.getITag());
				if(a_node == null)
				{
					System.out.println("--- [KTree2]  ResourceMap:: right for node_id=" + a_right.getITag() + " will be removed");
					// delete right then
					rights_to_remove.add(a_right);
				}
				else
				{
					a_node.getRights().add(a_right);
					a_right.setNode(a_node);
				}
			}
			// remove rights to remove
			for(int i=0; i<rights_to_remove.size(); i++)
				this.getRights().remove(rights_to_remove.get(i));
			
		}// end of -- WORK WITH DATABASE
		catch(Exception e) { e.printStackTrace(System.out);}
		finally
		{ 
//			sqlm.freeConnection(conn);
			SQLManager.recycleObjects(al_conn, al_stmt, al_rs);
		}
		
		// clocking
		finish = new GregorianCalendar();
		diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
		System.out.println("... [KTree2] ResourceMap created in " + diff_mills + "ms ---- ");
		
	}
	
//	private void expandForeignNodes(User user)
//	{
//		for(int i=0; i<nodes.size(); i++)
//		{
//			if(nodes.get(i).getNodeType() == iNode.NODE_TYPE_I_COURSE)
//			{
//				((NodeCourse)nodes.get(i)).expandForeignChildren(this, user);
//			}
//		}
//	}
	
	
	/** Getter for <i>nodes</i> collection.
	 */
	public Item2Vector<iNode> getNodes() { return nodes; }

	/** Getter for <i>users</i> collection.
	 */
	public Item2Vector<User> getUsers() { return users; }
	/** Getter for <i>rights</i> collection.
	 */
	public Vector<Right> getRights() { return rights; }
//	/** Getter for <i>globally_defined_rights</i> collection.
//	 */
//	public Vector<Right> getGloballyDefinedRights()
//		{ return globally_defined_rights; }
	/** Getter for <i>globally_accessible_rights</i> collection.
	 */
	public Vector<Right> getGloballyAccessibleRights() { return globally_accessible_rights; }

	public void displayFolderView_old(Item2Vector<iNode> folder_list,
		JspWriter out, HttpServletRequest request/*, boolean show_ratings*/)
		throws IOException
	{
//System.out.println("ResourceMap.displayFolderView entering...");
		// get the user_id
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		ResourceMap res_map = (ResourceMap) session.getAttribute(
				ClientDaemon.SESSION_RES_MAP);
//System.out.println("ResourceMap.displayFolderView activities " + res_map.getActivities().size());/// DEBUG
		User user = res_map.getUsers().findById(user_id);
		User group = res_map.getUsers().findById(group_id);
		
		// Show 'Folders' caption
		out.println("<div class='pt_main_subheader'>Folders/Documents</div>");
		
		// Show folders
		String icon = "";
		String alt = "";
//System.out.println("~~~ [KTree2] ResourceMap.displayFolderView showing " + folder_list.size() + " items in folder");	
		for(int i=0; i<folder_list.size(); i++)
		{// for all items
			iNode child = folder_list.get(i);
//System.out.println("\titem " + (i+1) + " hidden=" + child.isHidden());	
			if (child.isHidden() && !request.isUserInRole("admin")) continue;
			icon = resolveIcon(child);
//			icon = ((child.getIcon()==null || child.getIcon().length() == 0)
//					?res_map.getItemtypeList().findById(child.getNodeType()).getIcon()
//					:child.getIcon()); 
				
//				iNode.NODE_TYPE_ICONS_LARGE[child.getNodeType()];
			alt = "&curren;";
			
			String div_style = (child.isHidden())?"pt_main_folder_document":"pt_main_folder_document_hidden";
//System.out.println("#ResourceMap.displayFolderView title=" + child.getTitle() + " match="  + match  + " div_style=" + div_style); /// DEBUG
			boolean can_see_author = child.canSeeAuthor(user, group, res_map);
			String display_title = child.getTitle() + 
				((can_see_author)? "&nbsp;<sup>[" + child.getCreatorAdderNames() + 
						"]</sup>": "");
			
			String link_color = child.isHidden()?"color:#999999;":(child.isCreatedBy(user_id)?"color:#006600;":"");
			
			out.println("<div class='" + div_style + "'>" + /*rating_html + "&nbsp;" + */
				/*"<img src='" +*/ icon + /*"' border='0' alt='" + alt + "' />" +*/
				"&nbsp;" + "<a style='" + ((child.isCreatedBy(user_id))?"text-decoration:underline;":"") + link_color + "'" +
				" href='" + request.getContextPath() +
				"/content/Show?" +  ClientDaemon.REQUEST_NODE_ID +
				"=" + child.getId() + "' target='_top'" + ((child.isHidden())?"title='(this item is hidden)'":"") + ">" +
				display_title + "</a></div>");
		}// end of -- for all items

	}// - end - displayFolderView

	public void displayFolderView(iNode _node,
			JspWriter out, HttpServletRequest request/*, boolean show_ratings*/)
			throws IOException
	{
		// tool tip script link
//			out.println("<script src=\"" + request.getContextPath() + "/assets/wz_tooltip.js\"></script>");

//System.out.println("ResourceMap.displayFolderView entering...");
		// get the user_id
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		ResourceMap res_map = (ResourceMap) session.getAttribute(
				ClientDaemon.SESSION_RES_MAP);
//System.out.println("ResourceMap.displayFolderView activities " + res_map.getActivities().size());/// DEBUG
		User user = res_map.getUsers().findById(user_id);
		User group = res_map.getUsers().findById(group_id);
		
		// get augmented view
		Item2Vector<iNode> folder_list = (Item2Vector<iNode>)session.getAttribute(ClientDaemon.SESSION_AUGMENTED_VIEW);
		folder_list = (folder_list==null)?_node.getChildren():folder_list;
		session.removeAttribute(ClientDaemon.SESSION_AUGMENTED_VIEW);
		
		// print channel's html first if exists
		AnnotationItem chanel_annot = _node.getPrefixes().findByTitle(iNode.ANNOT_ADAPT_PSERVICE);
		out.println(  (chanel_annot!=null)?chanel_annot.getAnnotation():"" );
		
		// Show 'Folders' caption
		out.println("<div class='pt_main_subheader'>Folders/Documents</div>");
		
		// Show folders
		String icon = "";
		AnnotationItem pservice = null;
		AnnotationItem note = null;
		String alt = "";
//System.out.println("~~~ [KTree2] ResourceMap.displayFolderView showing " + folder_list.size() + " items in folder");	
		for(int i=0; i<folder_list.size(); i++)
		{// for all items
			iNode child = folder_list.get(i);
//System.out.println("\titem " + (i+1) + " hidden=" + child.isHidden());	
			if (child.isHidden() && !request.isUserInRole("admin")) continue;
			icon = resolveIcon(child);
			pservice = child.getPrefixes().findByTitle(iNode.ANNOT_ADAPT_PSERVICE);
			note = child.getSuffixes().findByTitle(iNode.ANNOT_KT_NOTE);
			
//				icon = ((child.getIcon()==null || child.getIcon().length() == 0)
//						?res_map.getItemtypeList().findById(child.getNodeType()).getIcon()
//						:child.getIcon()); 
				
//					iNode.NODE_TYPE_ICONS_LARGE[child.getNodeType()];
			alt = "&curren;";
			
			String div_style = (child.isHidden())?"pt_main_folder_document":"pt_main_folder_document_hidden";
//System.out.println("#ResourceMap.displayFolderView title=" + child.getTitle() + " match="  + match  + " div_style=" + div_style); /// DEBUG
			boolean can_see_author = child.canSeeAuthor(user, group, res_map);
			String display_title = child.getTitle() + 
				((can_see_author)? "&nbsp;<sup>[" + child.getCreatorAdderNames() + 
						"]</sup>": "");
			
			String link_color = child.isHidden()?"color:#999999;":(child.isCreatedBy(user_id)?"color:#006600;":"");

//System.out.println("Node " + child.getTitle());
//System.out.println("annot " + ((pservice!=null)?pservice.getAnnotation():"~") + "\n");
			
			out.println("<div class='" + div_style + "' >" + /*rating_html + "&nbsp;" + */
				((pservice!=null)?pservice.getAnnotation():"") + 
				/*"<img src='" +*/ icon + /*"' border='0' alt='" + alt + "' />" +*/
				"&nbsp;" + "<a style='" + ((child.isCreatedBy(user_id))?"text-decoration:underline;":"") + link_color + "'" +
				" href='" + 
				// NEW LINK FROM PSERVICE??
				((child.getSTag()!=null && child.getSTag().length()>0)?child.getSTag():request.getContextPath() + "/content/Show?" +  ClientDaemon.REQUEST_NODE_ID + "=" + child.getId()) +
				"' target='" + (( (pservice!=null) && (pservice.getPopUp()) )?"_blank":"_top") + "'" + ((child.isHidden())?"title='(this item is hidden)'":"") + ">" +
				"<span style='" + ((pservice!=null)?pservice.getFormat():"") + "'>" + // Style personalization
				display_title + 
				"</span>" + // Style personalization
				"</a>" + ((note!=null)?note.getAnnotation():"") + "</div>");
				
				// DESTROY LINK FROM PSERVICE
				child.setSTag("");
		}// end of -- for all items

	}// - end - displayFolderView
	
	public static void displayPServicedFolderView_old(iNode node, JspWriter out, 
			HttpServletRequest request/*, boolean show_ratings*/) throws IOException
	{
		Model model = ModelFactory.createDefaultModel();// node.getPersonalizedModel(); 
			
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
//		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		ResourceMap res_map = (ResourceMap) session.getAttribute(
				ClientDaemon.SESSION_RES_MAP);
//		User user = res_map.getUsers().findById(user_id);
//		User group = res_map.getUsers().findById(group_id);

		// Show 'Folders' caption
		out.println("<div class='pt_main_subheader'>Folders/Documents</div>");
		// Show folders
		String icon = "";
		
		/*
		CREATE TABLE `portal_test2`.`ent_pservice_call_log` (
				  `PServiceCallID` INT NOT NULL AUTO_INCREMENT,
				  `Token` VARCHAR(50) NOT NULL,
				  `UserGroup` VARCHAR(50) NOT NULL,
				  `StartTS` BIGINT(20) NOT NULL,
				  `FinishTS` BIGINT(20) NOT NULL,
				  `Delay` INT NOT NULL,
				  `PortalURI` VARCHAR(255) NOT NULL,
				  PRIMARY KEY (`PServiceCallID`)
				)
				CHARACTER SET utf8;
		*/

		// VISUALIZE WHAT PSerice returned
		ResIterator channels = model.listSubjectsWithProperty(RDF.type, RSS.channel);
		Resource channel = null;
		if (channels.hasNext())
			channel = (Resource)channels.next();
		
		if (channel != null && channel.hasProperty(RSS.items))
		{
			
			// show channel header - summary annotation
			Statement channel_prop = channel.getProperty(DC.description);
//System.out.println("channel_educ " + channel.getProperty(DCTerms.educationLevel));			
//System.out.println("channel_prop " + channel_prop);
//StringWriter sw = new StringWriter();
//model.write(sw,"RDF/XML-ABBREV");
//System.out.println("model\n " + sw.toString() + "\n");			
			String text = (channel_prop!=null)?channel_prop.getString():"";
			out.println(text);
			
			// Construct table header
			out.println("<table border='0' cellspacing='1' cellpadding='3'>");

			Seq items = channel.getProperty(RSS.items).getSeq();
			
//			for (NodeIterator niter = items.iterator(); niter.hasNext();)
			for (int i=1; i<= items.size(); i++)
			{// for all items
				Resource res_item = items.getResource(i);
				if(res_item==null) continue;
//				Resource res_item = (Resource)niter.next();
				String res_title =  res_item.getProperty(RSS.title).getString();
				String res_url =  res_item.getProperty(RSS.link).getString();// + url_suffix;
				
				// load item properties
				Statement desc_prop = res_item.getProperty(DC.description);
				String icon2_html = (desc_prop!=null)?desc_prop.getString():"";
				
				Statement style_prop = res_item.getProperty(DC.format);
				String style_html = (style_prop!=null)?style_prop.getString():"";
				
//				String icon2_style = res_item.getProperty(DC.format).getString();
				
				//extract NodeID
				String node_id_s = res_url.substring(res_url.indexOf("?id=")+4, res_url.length());
				int any_other = node_id_s.indexOf("&");
				node_id_s = ((any_other>=0)?node_id_s.substring(0, any_other):node_id_s);
				int node_id = -1;
				try
				{
					if(node_id_s!=null && node_id_s.length()>0)
						node_id = Integer.parseInt(node_id_s);
				}
				catch(NumberFormatException nfe) {;}
				
//System.out.println("node_id=" + node_id);				
				iNode child = res_map.getNodes().findById(node_id);
				if(child==null)
				{
					System.out.println("!!! [KTree2] ResourceMap.displayPServicedFolderView SEVERE child of node#" +
							node.getId() + " cannot be found by node_id=" + node_id);
					continue;
				}
				if(child.isHidden()) continue;
//System.out.println("(child==null)" + (child==null));
				
				icon = res_map.resolveIcon(child);
				
//				icon = (child!=null)?
//						((child.getIcon()==null || child.getIcon().length() == 0)
//							?res_map.getItemtypeList().findById(child.getNodeType()).getIcon()
//							:child.getIcon())
//						:""; 
				
				// html for rating
//				String rating_html = "";
//				String rating_icon = "stars_0.gif";
				
//				if(show_ratings && child!=null)
//				{
//					float value = 0;
//					if(child.getPersonalRating() != null) // PERSONAL RATING
//					{
//						value = child.getPersonalRating().getRatingValue();
//						rating_icon = "stars_own" + (int)value + ".gif";
//					}
//					else if(child.getGroupRating() != null) // GROUP RATING
//					{
//						value = child.getGroupRating().getRatingValue();
//
//						if(((float)Math.round((float)value*2))/2 == Math.ceil(((float)Math.round((float)value*2))/2))
//							rating_icon = "stars_other" + (int)value + ".gif";
//						else
//							rating_icon = "stars_other" + ((float)Math.round((float)value*2))/2 + ".gif";
//					}
//					
//					rating_html = 
//						"<img src='" + request.getContextPath() + "/assets/" + rating_icon + "'/>";
//					
//				}
//				else
//					rating_html = "";

//				String div_style = "pt_main_folder_document";
//				boolean can_see_author = (child!=null)?child.canSeeAuthor(user, group, res_map):false;
//				String display_title = res_title + 
//					((can_see_author)? "&nbsp;<sup>[" + child.getCreatorAdderNames() + 
//							"]</sup>": "");
				
				String anchor = "<a" + ((child!=null && child.isCreatedBy(user_id))?" style='text-decoration:underline;color:#006600;'":"") +
						" href='" + 
						res_url +
						"' target='_top'>";
				
				// Print table row
				out.println("<tr>");
				// PService Icon
				out.println("	<td" + style_html + ">");
				out.println("		" + icon2_html);
				out.println("	</td>");
				// Item Icon
				out.println("	<td>");
				out.println("		" + /*"<img src='" +*/ icon /*+ "' border='0' />" /*+ anchor + "</a>"*/);
				out.println("	</td>");
				// Title Cell
				out.println("	<td>");
				out.println("		" + anchor +  res_title + "</a>");
				out.println("	</td>");
				out.println("</tr>");
				
			}// end of -- for all items /**/
			// Close Table
			out.println("</table>");

		}
		// end of -- VISUALIZE WHAT PSerice returned

		
	}// - end - displayFolderView

	
	public boolean isAllowedWhatWhoFor(int _right_type, int _user_id,
		iNode _node)
	{
		boolean result = false;

		// First check the trail to the root
		iNode current_node = _node;
		boolean ini_authorship = false;
		if(_node.getUser() != null)
			ini_authorship = (_node.getUser().getId() == _user_id);
//System.out.println("ResourceMap.isAllowedWhatWhoFor before trail, current_node " + current_node);
		while( (result == false) && (current_node != null) )
		{
//System.out.println("ResourceMap.isAllowedWhatWhoFor trail node #" + current_node.getId());
			result = current_node.isAllowedWhatWho(_right_type, _user_id,
				ini_authorship);
			current_node = current_node.getParent();
		}
//System.out.println("ResourceMap.isAllowedWhatWhoFor before globally defined");
		// Second check globally defined rights
//		for(int i=0; i<this.globally_defined_rights.size(); i++)
//		{
//			if( this.globally_defined_rights.get(i).
//				isAllowedWhatWhoFor(_right_type, _user_id,
//				_node.getId(), ini_authorship) )
//			{
//				result = true;
//				break;
//			}
//		}
//System.out.println("ResourceMap.isAllowedWhatWhoFor before globally accessible");
		// Third check globally accessible rights
		for(int i=0; i<this.globally_accessible_rights.size(); i++)
		{
			if( this.globally_accessible_rights.get(i).
				isAllowedWhatWhoFor(_right_type, _user_id,
				_node.getId(), ini_authorship) )
			{
				result = true;
				break;
			}
		}
		return result;
	}// end of - isAllowedWhatWhoFor


/*	public boolean isAllowedWhatWhoForFrom(int _right_type, int _user_id,
		iNode _node, int _parent_node_type)
	{
		boolean result = false;
//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check trail to root");
		// First check the trail to the root
		iNode current_node = _node;
//System.out.println("ResourceMap.isAllowedWhatWhoForTo before trail, current_node " + current_node);
		while( (result == false) && (current_node != null) )
		{
//System.out.println("\tChecking node # " + current_node.getId());
//System.out.println("ResourceMap.isAllowedWhatWhoForTo trail node #" + current_node.getId());
			result = current_node.isAllowedWhatWhoFrom(_right_type,
				_user_id, _parent_node_type);
			current_node = current_node.getParent();
		}

//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check globally defined rights");
		// Second check globally defined rights
		for(int i=0; i<this.globally_defined_rights.size(); i++)
		{
//System.out.println("\tChecking globally defined right # " + i);
			if( this.globally_defined_rights.get(i).
				isAllowedWhatWhoForFrom(_right_type, _user_id,
				_node.getId(), _parent_node_type) )
			{
				result = true;
				break;
			}
		}

//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check globally accessible rights");
		// Third check globally accessible rights
		for(int i=0; i<this.globally_accessible_rights.size(); i++)
		{
//System.out.println("\tChecking globally accessible right # " + i);
			if( this.globally_accessible_rights.get(i).
				isAllowedWhatWhoForFrom(_right_type, _user_id,
				_node.getId(), _parent_node_type) )
			{
				result = true;
				break;
			}
		}
		return result;
	}// - end - isAllowedWhatWhoForFrom/**/

	public boolean isAllowedWhatWhoForFromTo(int _right_type, int _user_id,
		iNode _node, int _parent_node_type, int _child_node_type)
	{
		boolean result = false;
//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check trail to root");
		// First check the trail to the root
		iNode current_node = _node;
		boolean ini_authorship = _node.isCreatedBy(_user_id);

//System.out.println("ResourceMap.isAllowedWhatWhoForTo before trail, current_node " + current_node);
//System.out.println("ResourceMap.isAllowedWhatWhoForFromTo ini_authorship=" + ini_authorship);
		while( (result == false) && (current_node != null) )
		{
//System.out.println("\tChecking node # " + current_node.getId());
//System.out.println("ResourceMap.isAllowedWhatWhoForTo trail node #" + current_node.getId());
			result = current_node.isAllowedWhatWhoFromTo(_right_type,
				_user_id, _parent_node_type,_child_node_type,
				ini_authorship);
			current_node = current_node.getParent();
		}

//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check globally defined rights");
		// Second check globally defined rights
//		for(int i=0; i<this.globally_defined_rights.size(); i++)
//		{
////System.out.println("\tChecking globally defined right # " + i);
//			if( this.globally_defined_rights.get(i).
//				isAllowedWhatWhoForFromTo(_right_type, _user_id,
//				_node.getId(), _parent_node_type, _child_node_type,
//				ini_authorship) )
//			{
//				result = true;
//				break;
//			}
//		}
		return result;
	}// end of - isAllowedWhatWhoForFromTo

	public boolean isAllowedWhatWhoForFromTo_DownInhibitory(int _right_type, int _user_id,
		iNode _node, int _parent_node_type, int _child_node_type)
	{
		boolean result = false;
//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check trail to root");
		// First check the trail to the root
		iNode current_node = _node;
		boolean ini_authorship = _node.isCreatedBy(_user_id);

//System.out.println("ResourceMap.isAllowedWhatWhoForTo before trail, current_node " + current_node);
//System.out.println("ResourceMap.isAllowedWhatWhoForFromTo ini_authorship=" + ini_authorship);

		result = current_node.isAllowedWhatWhoFromTo_DownInhibitory(_right_type,
			_user_id, _parent_node_type,_child_node_type,
			ini_authorship, globally_defined_rights,
			globally_accessible_rights);


//System.out.println("ResourceMap.isAllowedWhatWhoForFromTo result = " + result);
		return result;
	}// end of - isAllowedWhatWhoForFromTo

	public boolean isAllowedWhatWho2ForFromToQuant(int _right_type, int _user_id, int _group_id,
		iNode _node, int _parent_node_type, int _child_node_type)
	{
		boolean result = false;
//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check trail to root");
		// First check the trail to the root
		iNode current_node = _node;
		int children_type_count = _node.getChildCountOfTypeByUser(_child_node_type, _user_id);
//System.out.print("ResourceMap.isAllowedWhatWhoForToQuant right=" + _right_type + " user=" + _user_id + " group=" + _group_id +
//" parentT=" + _parent_node_type + " childT=" + _child_node_type + " children#="+children_type_count);
		boolean ini_authorship = false;
		if(_node.getUser() != null)
			ini_authorship = (_node.getUser().getId() == _user_id);
//System.out.println("ResourceMap.isAllowedWhatWhoForTo before trail, current_node " + current_node);
//System.out.println("ResourceMap.isAllowedWhatWhoForFromTo ini_authorship=" + ini_authorship);
		while( (result == false) && (current_node != null) )
		{
//System.out.println("\tChecking node # " + current_node.getId());
//System.out.println("ResourceMap.isAllowedWhatWhoForTo trail node #" + current_node.getId());
			result =
				(current_node.isAllowedWhatWhoFromToQuant(_right_type,
				_user_id, _parent_node_type,_child_node_type,
				ini_authorship, children_type_count))
				||
				(current_node.isAllowedWhatWhoFromToQuant(_right_type,
				_group_id, _parent_node_type,_child_node_type,
				ini_authorship, children_type_count)) ;
			current_node = current_node.getParent();
		}

//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check globally defined rights");
		// Second check globally defined rights
//		for(int i=0; i<this.globally_defined_rights.size(); i++)
//		{
////System.out.println("\tChecking globally defined right # " + i);
//			if( this.globally_defined_rights.get(i).
//				isAllowedWhatWhoForFromToQuant(_right_type, _user_id,
//				_node.getId(), _parent_node_type, _child_node_type,
//				ini_authorship, children_type_count)
//				||
//				this.globally_defined_rights.get(i).
//				isAllowedWhatWhoForFromToQuant(_right_type, _group_id,
//				_node.getId(), _parent_node_type, _child_node_type,
//				ini_authorship, children_type_count)  )
//			{
//				result = true;
//				break;
//			}
//		}

//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check globally accessible rights");
		// Third check globally accessible rights
		for(int i=0; i<this.globally_accessible_rights.size(); i++)
		{
//System.out.println("\tChecking globally accessible right # " + i);
			if( this.globally_accessible_rights.get(i).
				isAllowedWhatWhoForFromToQuant(_right_type, _user_id,
				_node.getId(), _parent_node_type, _child_node_type,
				ini_authorship, children_type_count)
				||
				this.globally_accessible_rights.get(i).
				isAllowedWhatWhoForFromToQuant(_right_type, _group_id,
				_node.getId(), _parent_node_type, _child_node_type,
				ini_authorship, children_type_count)  )
			{
				result = true;
				break;
			}
		}

//System.out.println("ResourceMap.isAllowedWhatWhoForFromTo result = " + result);
//System.out.println(" " + result);
		return result;
	}// end of - isAllowedWhatWhoForFromToQuant

	public void outputNodeTree(JspWriter out, HttpServletRequest req,
		iNode current_node, int user_id, int group_id, int show_mode, boolean track_opens)
		throws IOException
	{
//Calendar start = null;
//Calendar finish = null;
//long diff_mills;
//start = new GregorianCalendar();
		User user = null;
		User group = null;
		if(user_id > 0) user = getUsers().findById(user_id);
		if(group_id > 0) group = getUsers().findById(group_id);
		for(int i=0;i<root_node.getChildren().size();i++)
		{
			root_node.getChildren().get(i).outputTree( out, req, current_node,
				0, show_mode, user, group, this, "");
		}
		
//			root_nodes.get(i).outputTreeNode(out, req, current_node, 0, 
//			show_mode, user, track_opens);
//finish = new GregorianCalendar();
//diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
//System.out.println("\t[CoPE] ResourceMap.outputNodeTree millisec passed " + diff_mills);
	}

	public int getNextVirtualNodeId() { return ++top_virtual_node; } 
	public int getTopVirtualNode() { return top_virtual_node; } 

	public boolean sendNodeToBin(iNode node_to_bin, boolean put_to_bin)
	{
		boolean result = false;
		if(node_to_bin == null)
			return result;
		iNode parent = node_to_bin.getParent();
		if(parent != null) // if has parent
		{
//System.out.println("~~ parent " + parent.getTitle());
			int index = parent.getChildren().findIndexById(node_to_bin.getId());
//System.out.println("~~ child children " + parent.getChildren().size());
//System.out.println("~~ no of index " + index);
			parent.getChildren().removeElementAt(index);
//			result = (deleted!=null)?true:false;
			result = true;
		}
//System.out.println("~~ bin_node==null " + (bin_node==null));
		if((bin_node != null)&&(put_to_bin))
		{
			node_to_bin.setParent(bin_node);
			bin_node.getChildren().add(node_to_bin);
		}
		return result;
	}
	
	public iNode getRootNode() {return root_node; }
	
	public int getBinNodeId() {return bin_node_id; }
	
	public void resetBinNode()
	{
		if(bin_node_id != 0)
			bin_node = nodes.findById(bin_node_id);
//System.out.println("!! bin_node==null " + (bin_node==null));
	}

	/** Method returns a vector of all ratings in the current view of the portal
	 * @return a vector of all ratings in the current view of the portal
	 * @since 1.5
	 */
	public Vector<Rating> getRatings() { return ratings; }
	
	public void setPendingNode(iNode _pending_node)
	{
		pending_node = _pending_node;
//System.out.println("ResourceMap.setPendingNode pending_node=" + pending_node);
	}

	public iNode getPendingNode() { return pending_node; }
	
	/** deleting a pending node */
	public void recycleNode(iNode _pending_node)
	{
//System.out.println("ResourceMap.recycleNode pending_node=" + pending_node);
		if(_pending_node==null && pending_node == null)
		{
			System.out.println("!!! [KTree2] ResourceMap.recycleNode trying to recycle a null node.");
			return;
		}
		
		if(pending_node != null)
			_pending_node = pending_node;
		
		if(_pending_node.getId() != 0)
		{
			System.out.println("!!! [KTree2] ResourceMap.recycleNode trying to recycle a non-pending node. Use deletion instead.");
			return;
		}
		
		if(_pending_node.getChildren().size() != 0)
		{
			System.out.println("!!! [KTree2] ResourceMap.recycleNode trying to recycle a node with children.");
			return;
		}
		
		// logically delete the node
		// 1. from nodes collection
		nodes.remove(_pending_node);
		// 2. from its parent
		if(_pending_node.getParent() != null)
			_pending_node.getParent().getChildren().remove(_pending_node);
		// 3. from pending node pointer
		pending_node = null;
		
		// deal with external objects
		switch(_pending_node.getNodeType())
		{
			case iNode.NODE_TYPE_I_FOLDER:
			case iNode.NODE_TYPE_I_UNTYPDOC:
			case iNode.NODE_TYPE_I_TOPIC_FOLDER:
			{// no special treatment is necessary
				;
			}
			break;
			
			case iNode.NODE_TYPE_I_BIN:
			case iNode.NODE_TYPE_I_MYPROFILE:
			{ // looks crazy
				System.out.println("!!! [KTree2] ResourceMap.recycleNode trying to recycle a node of this type is alerting (" + iNode.NODE_TYPES_S_ALL[_pending_node.getNodeType()-1] + ")");
			}
			break;
			
//			case iNode.NODE_TYPE_I_QUIZ:
//			case iNode.NODE_TYPE_I_DISSECTION:
//			case iNode.NODE_TYPE_I_WADEIN:
//			case iNode.NODE_TYPE_I_CODEEXAMPLE:
//			case iNode.NODE_TYPE_I_KARELROBOT:
//			case iNode.NODE_TYPE_I_SYS_QUIZGUIDE:
//			case iNode.NODE_TYPE_I_SYS_NAVEX:
//			case iNode.NODE_TYPE_I_SYS_WADEIN:
//			{// these are activities
//				if(_pending_node.getExternalObject() == null)
//				{
//					System.out.println("!!! ResourceMap.recycleNode trying to recycle a null external object if a node.");
//				}
//				else
//				{
//					activities.remove(_pending_node.getExternalObject());
//					_pending_node.getExternalObject().getOwners().removeAllElements();
//					_pending_node.setExternalObject(null);
//				}
//			}
//			break;
			
		}// end of -- switch
		_pending_node = null;
	}// end of -- recyclePendingNode
	
	/**
	 * Method loads missing children of the node
	 */
	public void FullyLoad(iNode _node, SQLManager sqlm)
	{
		if(_node.isFullyLoaded())
			System.out.println("!!! [KTree2] Node.FullyLoad:: attempting to load fully loaded node (id=" + _node.getId() + ")");
		else
		{
			// if it's terminal node, it's fully loaded
			if(_node.getFolderFlag() == false)
			{
				_node.setFullyLoaded(true);
				return;
			}	
			ResultSet rs = null;
			Connection conn = null;
			java.sql.Statement stmt = null;
			String qry = "";
			
			if(_node.getChildren().size() >0 )
				System.out.println("!!! [KTree2] SEVERE:: not fully loaded node has children!! (id=" + _node.getId() + ")");
			// reset children
			_node.getChildren().clear();
			
			// WORK WITH DATABASE
			try
			{
				conn = sqlm.getConnection();
				// now select nodes from rooted tree
				qry = "SELECT n.* FROM ent_node n JOIN rel_node_node nn ON(nn.ChildNodeID=n.NodeID) " +
						" JOIN voc_itemtype it ON(n.ItemTypeID=it.ItemtypeID)" +
						"WHERE nn.ParentNodeID=" + _node.getId() + " AND it.Type>0 ORDER BY nn.OrderRank;";
				stmt = conn.createStatement();
				rs = stmt.executeQuery(qry);
				
				while(rs.next())
				{// cycle nodes
					int node_id = rs.getInt("NodeID");
					String node_title = rs.getString("Title");
					String node_descr = rs.getString("Description");
					int a_user_id = rs.getInt("UserID");
					int node_type = rs.getInt("ItemTypeID");
					String node_url = rs.getString("URL");
					String node_uri = rs.getString("URI");
					int folder_flag_i = rs.getInt("FolderFlag");
					String node_icon = rs.getString("Icon");
					int hidden_i = rs.getInt("Hidden");
					boolean hidden = hidden_i==1;

					User user = users.findById(a_user_id);
					node_descr = (node_descr==null) ? "" : node_descr ;
					node_url = (node_url==null) ? "" : node_url ;
					node_uri = (node_uri==null) ? "" : node_uri ;
					boolean folder_flag = (folder_flag_i == 1);
					int multiurl_i = rs.getInt("MultiURL");
					boolean multiurl = multiurl_i==1;
					node_icon = (node_icon==null) ? "" : node_icon ;
					String cond_hidden = rs.getString("ConditionallyHidden");

					// work with conditional hiding
					if(!hidden && cond_hidden != null && cond_hidden.length()>0 && 
							cond_hidden.indexOf("{" + this.group.getLogin() + "}")>=0)
					{
						hidden = true;
					}
					
					// decode the type of the node
					ItemType itype = this.itemtype_list.findById(node_type);
					
					if(multiurl && node_type == iNode.NODE_TYPE_I_PSERVICED_FOLDER)
					{
						ArrayList checkMulti = null;
						checkMulti = checkMultiURLNode(node_url, itype, node_type);
						node_url = (String)checkMulti.get(0);
						itype = (ItemType)checkMulti.get(1);
						node_type = ((Integer)checkMulti.get(2)).intValue();
					}
					
					String node_class_name = (itype != null)? itype.getClassName() : Node.class.getName();

					//if(itype == null) System.out.println("!!~ nodeID=" + node_id);
					iNode node_class = null;
					iNode a_node = null;
					// load class
					try
					{
						node_class = (iNode)Class.forName(node_class_name).newInstance();
						a_node = node_class.NodeFactory(node_id, node_title, node_uri,
								node_type, node_descr, node_url, folder_flag, node_icon, hidden);
					}
					catch(ClassNotFoundException cnfe)
					{
						cnfe.printStackTrace(System.out);
						System.out.println("!!! [KTree2] SEVERE:: ResourceMap Loading not via NodeFactory");					
						a_node = new Node(node_id, node_title, node_uri,
								node_type, node_descr, node_url, folder_flag, node_icon, hidden);
					}					
					
					a_node.setCreator(user);
					
					this.getNodes().add(a_node);
					
					_node.getChildren().add(a_node);
					a_node.setParent(_node);

					switch (node_type)
					{// node_type
						case iNode.NODE_TYPE_I_ALL: // Root Node
						{
							this.root_node = a_node;
						}
						break;
						case iNode.NODE_TYPE_I_BIN: // Recycled bin
						{
							this.bin_node_id = a_node.getId();
							this.bin_node = a_node;
						}
						break;
					}// end of -- node_type
					
				}// end of - cycle nodes
				rs.close();
				stmt.close();
				conn.close();
				rs = null;
				stmt = null;
				conn = null;
			}// end of -- WORK WITH DATABASE
			catch(Exception e) { e.printStackTrace(System.out);}
			finally
			{
//				sqlm.freeConnection(conn);
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
			
			_node.setFullyLoaded(true);
		}
	}
	
	public String getBreadCrumbs_old(iNode _leaf_node, String _context_path)
	{
		String result = "";
		ArrayList<iNode> crumb_list = new ArrayList<iNode>();
		while(_leaf_node != null && _leaf_node.getParent() != null)
		{
			crumb_list.add(0, _leaf_node);
			_leaf_node = _leaf_node.getParent();
		}
		
		result += "<a href='" + _context_path + "/content/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + 
			this.root_node.getId() + /* "&" + Show.SHOW_FLAG_RELOAD + "=1" +*/ "' target = '_top'><img style='vertical-align:bottom' src='" + 
			_context_path + "/assets/home.gif' width='16' " + "height='16' border='0' alt='[Home]' title='Home' /></a>";
		
		for(Iterator<iNode> iter = crumb_list.iterator(); iter.hasNext();)
		{
			iNode node = iter.next();
			result += " &raquo; " +
					((iter.hasNext())?"<a title='" + node.getTitle() + "' href='" + _context_path + "/content/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + node.getId() + "' target = '_top'>" :"")+ 
					node.getTitle() + 
					((iter.hasNext())?"</a>":"");
		}
		return result;
	}

	public String getBreadCrumbs(iNode _leaf_node, String _context_path)
	{
		String result = "";
		ArrayList<iNode> crumb_list = new ArrayList<iNode>();
		while(_leaf_node != null && _leaf_node.getParent() != null)
		{
			crumb_list.add(0, _leaf_node);
			_leaf_node = _leaf_node.getParent();
		}
		/*
		<a href="#" class="menu"><img class="menu_img" title="Home" src="<%=request.getContextPath()%>/assets/home.gif" width='16' height='16' border='0' onmouseover="src='<%=request.getContextPath()%>/assets/home_hover.gif';" onmouseout="src='<%=request.getContextPath()%>/assets/home.gif';"/></a>
		&zwj;<img class="menu_img" src="<%=request.getContextPath()%>/assets/breadcrumb_spacer.gif" width="12" height="16"/>
		&zwnj;<a href="#" class="menu">IS 2470 Interactive Systems Design</a>
		&zwj;<img class="menu_img" src="<%=request.getContextPath()%>/assets/breadcrumb_spacer.gif" width="12" height="16"/>
		&zwnj;<a href="#" class="menu">Lecture 2</a>
		&zwj;<img class="menu_img" src="<%=request.getContextPath()%>/assets/breadcrumb_spacer.gif" width="12" height="16"/>
		&zwnj;<a href="#" class="menu">Lecture 2 recording via virtPresenter</a>
		*/
		
		result += "<a class='menu' href='" + _context_path + "/content/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + 
			this.root_node.getId() + "' target = '_top'><img class='menu_img' src='" + 
			_context_path + "/assets/home.gif' width='16' height='16' border='0' alt='[Home]' title='Home' "+
			" onmouseover=\"src='" + _context_path + "/assets/home_hover.gif';\" onmouseout=\"src='" + _context_path + "/assets/home.gif';\" /></a>";
		
		for(Iterator<iNode> iter = crumb_list.iterator(); iter.hasNext();)
		{
			iNode node = iter.next();
			
			// spacer
			result += "&nbsp;<img class='menu_img' src='" + _context_path + "/assets/breadcrumb_spacer.gif' width='12' height='16'/>";
			// node
			result += " " +
					((iter.hasNext())?"<a href='" + _context_path + "/content/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + node.getId() + "' class='menu' target = '_top'>":"") + 
					node.getTitle() + 
					((iter.hasNext())?"</a>":"");
//			result += " &raquo; " +
//					((iter.hasNext())?"<a title='" + node.getTitle() + "' href='" + _context_path + "/content/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + node.getId() + "' target = '_top'>" :"")+ 
//					node.getTitle() + 
//					((iter.hasNext())?"</a>":"");
		}
		return result;
	}
	
	public ArrayList checkMultiURLNode(String _node_url, ItemType _itemtype, int _node_type)
	{
		// get the URL
		ParameterParser pp = new ParameterParser();
		Map mp = pp.parse(_node_url, ';');
		String all_url = (String)mp.get("all");
		String group_url = (String)mp.get(this.group.getLogin());
		String the_url = null; 
		// group url
		if(group_url != null && group_url.length()>0 && !group_url.equalsIgnoreCase("none"))
		{
			the_url = group_url;
		}
		// if not group ckech all
		if((group_url == null || group_url.length()==0) && all_url != null && all_url.length()>0 && !all_url.equalsIgnoreCase("none"))
		{
			the_url = all_url;
		}
		if(the_url != null)
		{// we found the right URL
			_node_url = the_url;
		}
		else
		{// no url at all switch to regular node
			_itemtype = this.itemtype_list.findById(iNode.NODE_TYPE_I_FOLDER);
			_node_type = iNode.NODE_TYPE_I_FOLDER;
			_node_url = "";
		}
		ArrayList result = new ArrayList();
		result.add(_node_url);
		result.add(_itemtype);
		result.add(new Integer(_node_type));
		return result;
		// deal with the class
	}
	
	public String resolveIcon(iNode _node)
	{
		return (_node!=null)?
				((_node.getIcon()==null || _node.getIcon().length() == 0)
					?this.getItemtypeList().findById(_node.getNodeType()).getIcon()
					:_node.getIcon())
				:""; 
	}
	
}

