package edu.pitt.sis.paws.kt2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

public class PServicedNode extends Node
{
	final static long serialVersionUID = 2L;
	
	public PServicedNode() { ; }

	protected PServicedNode(int _id, String _title, String _uri, int _node_type,
			String _descr, String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		super(_id, _title, _uri, _node_type, _descr, _url, _folder_flag, _icon, _hidden);
	}

	public iNode NodeFactory(int _id, String _title, String _uri, int _node_type, String _descr,
			String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		return new PServicedNode(_id, _title, _uri, _node_type, _descr, _url, _folder_flag, _icon, _hidden);
	}
	
	public void showView(JspWriter out, HttpServletRequest request/*, boolean show_ratings*/) throws IOException
	{
	//System.out.println("Node.showView before check to external... node_type=" + this.node_type + " xtrnl_object="+this.xtrnl_object);	
//		String cancel_to = request.getContextPath() + "/content" + 
//			"/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + this.getId();
		
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		ResourceMap resmap = (ResourceMap) session.getAttribute(
				ClientDaemon.SESSION_RES_MAP);
	//System.out.println("ResourceMap.displayFolderView activities " + res_map.getActivities().size());/// DEBUG
		User user = resmap.getUsers().findById(user_id);
		User group = resmap.getUsers().findById(group_id);
		
		boolean can_see_author = canSeeAuthor(user, group, resmap);
		String display_author = ((can_see_author)? "&nbsp<sup>[" + 
				this.getCreatorAdderNames() + "]</sup>": "");
	//System.out.println("Node.showView can_see_author = " + can_see_author);	
		// up-dir icon
	//	boolean no_way_up = false;
		
		if(this.getFolderFlag())
		{// if it's a folder - show sub-nodes (not for root)
			if(this.getNodeType() != iNode.NODE_TYPE_I_ALL)
			{
				// Show 'Description' caption
				out.println("<div class='pt_main_subheader'>" +
					 this.getTitle() + display_author + "</div>");
					 
				out.println("<p>" + this.getDescription() + "</p>");
			}
			
			// DISPLAYING FOLDER CONTENT
//			ResourceMap.displayPServicedFolderView(this, out, request/*, show_ratings*/);
			resmap.displayFolderView(this, out, request/*, show_ratings*/);
			// end of -- DISPLAYING FOLDER CONTENT
			
		}// -- end if it's a folder - show sub-nodes
		else
		{// otherwise show default document
			if( (this.getDescription() != null) && 
				(!this.getDescription().equals("")) )
				out.println("<p>" + this.getDescription() + "</p>");	
			else
			// otherwise - default text
				out.println("<p>This document is empty..</p>");
			
		}// -- end otherwise show default document
		
		//if(show_ratings) showRatings(out, request, cancel_to);
	}
	
	public void outputTree(JspWriter out, HttpServletRequest request,
			iHTMLHierarchicalItem<iNode> current_item,
			int level, int display_mode, User user, User group,
			ResourceMap resmap, String jsAction) throws IOException
		{//outputTree(out, request, null, -1, checkbox_mode, null, null, null, "");
			//modes
			boolean content_greedy_n_trace = (iHTMLHierarchicalItem.
				HHTMLITEM_CONTENT_GREEDY_TRACE & display_mode) > 0;
			boolean show_checkbox = (iHTMLHierarchicalItem.
				HHTMLITEM_SHOW_CHECKBOX & display_mode) > 0;
			boolean show_checkbox_locked = (iHTMLHierarchicalItem.
				HHTMLITEM_SHOW_CHECKBOX_LOCKED & display_mode) > 0;
			boolean show_radio = (iHTMLHierarchicalItem.
				HHTMLITEM_SHOW_RADIO & display_mode) > 0;
			boolean show_nolink = (iHTMLHierarchicalItem.
				HHTMLITEM_SHOW_NOLINK & display_mode) > 0;

			String div_style = "pt_tree_left_opener_div";

			// self div open
			out.print("<div id='n" + this.getId() + "' name='n" + this.getId() + "' class='" + div_style + "'>");
			// folder ident image
			if(level > 0)
				out.print("<img src='" + request.getContextPath() + "/assets/dir_empty.gif' height='16' width='"+ (level * 18) + "'alt='.    ' border=0 " + "style='vertical-align:baseline;display:inline'/>");

			// anchor with folder/doc bullet
			if(level >= 0) // if we need the anchor
			{
				if(getChildren().size() > 0) // has children
				{
					String opener_href = (content_greedy_n_trace) ?
						" href='" + request.getContextPath() + "/content/jspLeft?" + ((this.getExpanded())? "collapse" : "expand") + "=" + getId() + "#n" + getId() + "a'" : "";
					String opener_onclick = (content_greedy_n_trace) ? "" :
						" onClick='tree_opener(this)'";

					out.print("<a name='n" + getId() + "a'" + opener_href + opener_onclick + " class='pt_tree_left_opener_bullet'>");

					if(content_greedy_n_trace) // do not pack both icons then
					{
						out.print("<img src='" + request.getContextPath() + "/assets/" + ((getExpanded())?"dir_minus.gif":"dir_plus.gif") + "' alt='[-]' border=0 " + "style='vertical-align:baseline;display:inline'>");
					}
					else // pack both icons
					{
						out.print("<img id='n" + getId() + "minus' src='" + "/assets/dir_minus.gif' alt='[-]' border=0 " + "style='vertical-align:baseline;display:" + ((getExpanded())?"inline":"none") +"'>");
						out.print("<img id='n" + getId() + "plus' src='" + "/assets/dir_plus.gif'  alt='[+]' border=0 " + "style='vertical-align:baseline;display:" + ((getExpanded())?"none":"inline") +"'>");
					}
					out.print("</a>");
				}
				else // no children
				{
					out.print("<a name='n" + getId() + "a' border=0 style='display:inline'><img src='" + request.getContextPath() + "/assets/dir_empbull.gif'" + " alt='&curren;' border=0 style='vertical-align:baseline;display:inline'/></a>");
				}
			}

//			// print a checkbox or radio
//			if(show_checkbox || show_checkbox_locked)
//				out.print("<input type='checkbox' name='chk_node" + this.getId() +
//					"' value='node" + this.getId() + "'" +
//					((show_checkbox_locked)?" checked":"") + ">");
//			if(show_radio)
//			{
//				out.print("<input type='radio' name='rad_node'" +
//					" value='node" + this.getId() + "'>");
//			}

			// pick an icon
			String icon_file = resmap.resolveIcon(this); 
//			String icon_file = ((this.getIcon()==null || this.getIcon().length() == 0)
//					?resmap.getItemtypeList().findById(this.getNodeType()).getIcon():this.getIcon()); 

			// print text of the node
			String style = "pt_tree_left_link";
			style = (current_item != null && getId() == current_item.getId()) ?
				"pt_tree_left_link_current" : style;
			style = (level >=0)?style:"pt_tree_left_item_no_link";

			boolean can_see_author = canSeeAuthor(user, group, resmap);

			String display_title = getTitle() +
				((can_see_author)? "&nbsp;<sup>[" + this.getCreatorAdderNames() + "]</sup>": "");

			String href = (!show_nolink)?" href='" + request.getContextPath() + "/content/Show?id=" + getId() + "' target='_top'":"";
			out.print("&nbsp;<a" + ((user!=null && isCreatedBy(user.getId())) ? " style='text-decoration:underline;color:#006600;'" : "") + " class='" + style + "' title='" + display_title + "'" + href + ">" ); 
			// print an icon
			out.print("<img src='" + 
//					request.getContextPath() + "/assets/" + 
					icon_file + "' alt='' border=0 " + "style='vertical-align:baseline;display:inline'>");
			out.print(display_title + "</a>");

			if(show_radio || show_checkbox || show_checkbox_locked)
				out.print("</input>");

			// self div close
			out.println("</div>");

			// deal with children
			if(level < 0) return; // here no recursion

			if(getChildren().size() > 0 && (!content_greedy_n_trace || getExpanded()) )
			{
				out.println("<div id='n" + this.getId() + "children' style='display:" + ((getExpanded()) ? "block" : "none") + "'>");
				for(int i = 0; i < getChildren().size(); i++)
					getChildren().get(i).outputTree(out, request, current_item, level + 1, display_mode, user, group, resmap, jsAction);
				out.println("</div>");
			}

		}

}
