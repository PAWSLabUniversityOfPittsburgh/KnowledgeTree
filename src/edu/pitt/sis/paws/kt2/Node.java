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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;
import edu.pitt.sis.adapt2.pservice.SimADAPT2SocialNavigationPService;
import edu.pitt.sis.paws.cbum.report.ReportAPI;
import edu.pitt.sis.paws.core.Item2;
import edu.pitt.sis.paws.core.Item2Vector;
import edu.pitt.sis.paws.core.ItemVector;
import edu.pitt.sis.paws.core.OrderedWeightedItem2Vector;
import edu.pitt.sis.paws.core.iItem2;
import edu.pitt.sis.paws.core.utils.SQLManager;
import edu.pitt.sis.paws.kt2.note.NoteBatch;


/**
 * @author Michael V. Yudelson
 */
public class Node extends Item2 implements iNode
{
	final static long serialVersionUID = 2L;
	
	// CONSTANTS
	public static final String DEFAULT_URI_PREFIX = "http://adapt2.sis.pitt.edu/kt/rest/ktree";
	// Form fields
	public static final String NODE_FRMFIELD_ID = "fld_id";
	public static final String NODE_FRMFIELD_TITLE = "fld_title";
	public static final String NODE_FRMFIELD_DESCRIPTION = "fld_description";
	public static final String NODE_FRMFIELD_NODETYPE = "fld_node_type";
	public static final String NODE_FRMFIELD_URI = "fld_uri";
	public static final String NODE_FRMFIELD_URL = "fld_url";
	public static final String NODE_FRMFIELD_ICON = "fld_icon";
	public static final String NODE_FRMFIELD_FOLDERFLAG = "fld_folderflag";
	public static final String NODE_FRMFIELD_HIDDEN = "fld_hidden";
	public static final String NODE_FRMFIELD_HIDDEN_YES = "1";
	public static final String NODE_FRMFIELD_HIDDEN_NO = "0";
	
	public static final String NODE_FRMFIELD_WEIGHTORDCHANGED = "fld_weight_order_changed";
//	public static final String NODE_FRMFIELD_RATING_VALUE = "your_rating_js_Value";
//	public static final String NODE_FRMFIELD_RATING_TEXT_VALUE = "your_rating_js_Text_Value";
//	public static final String NODE_FRMFIELD_RATING_EDIT_FLAG = "your_rating_edit";
//	public static final String NODE_FRMFIELD_RATING_ANONYMITY = "anonymity";
//	public static final String NODE_FRMFIELD_RATING_COMMENT = "your_comment_js_text";
	
	public static final String NODE_FRMFIELD_NODETYPE_RECENT = "fld_node_type_recent";
	
	public static final boolean IS_STORED_IN_DB = true; 
	public static final boolean IS_NOT_STORED_IN_DB = false; 
	
	private User creator;
	private Vector<Right> rights;

	private String description;

	/** URL of the external object that the node is associated with
	 */
	private String url;

	/** The type of the node
	 */
	private int node_type;
	private boolean folder_flag;
//	private String icon;

	private ItemVector<AnnotationItem> prefixes;
	private ItemVector<AnnotationItem> suffixes;
	
	private iNode parent;
	private OrderedWeightedItem2Vector<iNode> children;

	private boolean hidden = false;
	
	private boolean expanded = false; // property for tree visualization

	/** Rating of a group of users
	 * @since 1.5
	 */
	private Rating group_rating;

	/** personal rating of a user
	 * @since 1.5
	 */
	private Rating personal_rating;

	/**
	 * Flag defines whether node is attached to an onject stored outside KT's database
	 */
	private boolean stored_in_db;
	
	/**
	 * Flag determines whether the node and possibly its children have been fuly loaded from db
	 */
	private boolean is_fully_loaded;
	
	/**
	 * Saved personalized RDF Jena model
	 */
//	private Model personalized_nodel;

	
	/**
	 * HTML snippet with annotstion cue/icom if such exists
	 */
	private String annotation_cue;
	/**
	 * HTML snippet with annotstion style if such exists
	 */
	private String annotation_style;
	/**
	 * link that has enriched with personalized information
	 */
	private String personalized_link;

	public Node()
	{
		;
	}
	
	public Node(int _id, String _title, String _uri, int _node_type, String _descr,
		String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		super(_id, _title, _uri);
		expanded = false;
		parent = null;
		children = new OrderedWeightedItem2Vector<iNode>();
		node_type = _node_type;
		description = (_descr==null)?"":_descr;
		url = (_url==null)?"":_url;
		folder_flag = _folder_flag;
//		icon = _icon;
		creator = null;
		rights = new Vector<Right>();
		stored_in_db = IS_STORED_IN_DB;
		hidden = _hidden;

		group_rating = null;
		personal_rating = null;
		is_fully_loaded = false;
		
//		personalized_nodel = null;
		
		annotation_cue = "";
		annotation_style = "";
		personalized_link = "";
		
		prefixes = new ItemVector<AnnotationItem>();
		if(_icon!=null && _icon.length()>0)
			prefixes.add(new AnnotationItem(ANNOT_KT_ICON, "<img border='0' src='" + _icon + "'/>" ));
		
		suffixes = new ItemVector<AnnotationItem>();
	}
	
	public iNode NodeFactory(int _id, String _title, String _uri, int _node_type, String _descr,
			String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		return new Node(_id, _title, _uri, _node_type, _descr, _url, _folder_flag, _icon, _hidden);
	}

//	public Model getPersonalizedModel() { return personalized_nodel; }
//	public void setPersonalizedModel(Model _model) { personalized_nodel = _model; }
	
	public boolean isHidden() { return hidden; }
	public void setHidden(boolean _hidden) { hidden=_hidden; }
	public boolean isFullyLoaded() { return is_fully_loaded; }
	public void setFullyLoaded(boolean _is_fully_loaded) { is_fully_loaded = _is_fully_loaded; }
	
//	public String getURI()
//	{
//		return ( super.getURI()== null || super.getURI().length()==0 )?"http://adapt2.sis.pitt.edu/kt/rest/ktree"+getId():super.getURI();
//	}
	
	public String toString() { return "[ Node title:'" + this.getTitle() + "' id:" + this.getId() + " uri:" + this.getURI() + "]"; }
	
	public int compareTo(Object e) { return this.getTitle().compareTo(((Node)e).getTitle()); }

	public Vector<Right> getRights() { return rights; }

	public String getIcon()
	{
		AnnotationItem icon_annot = prefixes.findByTitle(ANNOT_KT_ICON);
		return (icon_annot==null)?null:icon_annot.getAnnotation();
	}

	public String getURL() { return url; }
	public void setURL(String _url) { url = _url; }

	public String getDescription() { return description; }
	public void setDescription(String _descr) { description = _descr; }

	public User getUser() { return creator; }
	public void setUser(User _user) { creator = _user; }

	public boolean getFolderFlag() { return folder_flag; }
	public void setFolderFlag(boolean _folder_flag) { folder_flag = _folder_flag; }

	/** Method collects all ontologies from this node up into a vector
	 * @param _ontologies - current vector of ontologies
	 */
	public int getNodeType() { return node_type; }
	public void setNodeType(int _node_type) { node_type = _node_type; }

	/** This function checks whether operation of a certain type is allowed
	 * for a certain user on this node.
	 */
	public boolean isAllowedWhatWho(int _right_type, int _user_id,
		boolean ini_authorship)
	{
//System.out.println("Node.isAllowedWhatWho this.id=" + this.getId() +
//	" right_type=" + _right_type + " _user_id=" + _user_id);
		boolean result = false;
		for(int i=0; i<this.rights.size(); i++ )
		{
//System.out.println("\tNode.isAllowedWhatWho r.right_type=" + this.rights.get(i).getRightType() +
//	" r.user=" + this.rights.get(i).getUser() + "");
			if( this.rights.get(i).isAllowedWhatWhoFor(_right_type,
				_user_id, this.getId(), ini_authorship) )
			{
				result = true;
				break;
			}
		}
		return result;
	}

/*	public boolean isAllowedWhatWhoFrom(int _right_type, int _user_id,
		int _parent_node_type, boolean ini_authorship)
	{
//System.out.println("Node.isAllowedWhatWhoTo this.id=" + this.getId() +
//	" right_type=" + _right_type + " _user_id=" + _user_id + " _child_node_type=" + _child_node_type);
		boolean result = false;
		for(int i=0; i<this.rights.size(); i++ )
		{
//System.out.println("\tNode.isAllowedWhatWhoTo r.right_type=" + this.rights.get(i).getRightType() +
//	" r.child_type=" + this.rights.get(i).getChildType() + " r.user=" + this.rights.get(i).getUser() + "");
			if( this.rights.get(i).isAllowedWhatWhoForFrom(_right_type,
				_user_id, this.getId(), _parent_node_type,
				ini_authorship) )
			{
				result = true;
				break;
			}
		}
		return result;
	}/**/

	public boolean isAllowedWhatWhoFromTo(int _right_type, int _user_id,
		int _parent_node_type, int _child_node_type, boolean ini_authorship)
	{
//System.out.println("Node _user_id=" + _user_id + " _right_type=" + _right_type + " _parent_node_type="+_parent_node_type+" _child_node_type=" + _child_node_type);
//System.out.println("Node.isAllowedWhatWhoTo this.id=" + this.getId() +
//	" right_type=" + _right_type + " _user_id=" + _user_id + " _child_node_type=" + _child_node_type);
		boolean result = false;
		
		for(int i=0; i<this.rights.size(); i++ )
		{
//System.out.println("\tNode.isAllowedWhatWhoTo r.right_type=" + this.rights.get(i).getRightType() +
//	" r.child_type=" + this.rights.get(i).getChildType() + " r.user=" + this.rights.get(i).getUser() + "");
			if( this.rights.get(i).isAllowedWhatWhoForFromTo(_right_type,
				_user_id, this.getId(), _parent_node_type,
				_child_node_type, ini_authorship) )
			{
				result = true;
				break;
			}
		}
//System.out.println("Node " + result);
		return result;
	}

	public boolean isAllowedWhatWhoFromTo_DownInhibitory(int _right_type, int _user_id,
		int _parent_node_type, int _child_node_type, boolean ini_authorship, 
		Vector<Right> _globally_def_rights, Vector<Right> _globally_acc_rights)
	{
//System.out.println("Node _user_id=" + _user_id + " _right_type=" + _right_type + " _parent_node_type="+_parent_node_type+" _child_node_type=" + _child_node_type);
//System.out.println("Node.isAllowedWhatWhoTo this.id=" + this.getId() +
//	" right_type=" + _right_type + " _user_id=" + _user_id + " _child_node_type=" + _child_node_type);
		boolean result = true;
		
		// check self - NO NEED
//		for(int i=0; i<this.rights.size(); i++ )
//		{
////System.out.println("\tNode.isAllowedWhatWhoTo r.right_type=" + this.rights.get(i).getRightType() +
////	" r.child_type=" + this.rights.get(i).getChildType() + " r.user=" + this.rights.get(i).getUser() + "");
//			if( ! (this.rights.get(i).isAllowedWhatWhoForFromTo(_right_type,
//				_user_id, this.getId(), _parent_node_type,
//				_child_node_type, ini_authorship)) )
//			{
//				return false;
//			}
//		}
		
		// now propagate to children
		for(int i=0; i<this.getChildren().size(); i++)
		{
			iNode child = this.getChildren().get(i);
			boolean child_result = child.isAllowedWhatWhoFromTo_DownInhibitory( 
				_right_type, _user_id, this.getNodeType(), 
				child.getNodeType(), ini_authorship, 
				_globally_def_rights, _globally_acc_rights);
				
			boolean global_def_result = true;
			boolean global_acc_result = true;

			// Second check globally defined rights
			for(int j=0; j<_globally_def_rights.size(); j++)
			{
				if( !(_globally_def_rights.get(j).
					isAllowedWhatWhoForFromTo(_right_type, _user_id,
					this.getId(), this.getNodeType(), child.getNodeType(),
					ini_authorship)) )
				{
					global_def_result = false;
					break;
				}
			}
	
			// Third check globally accessible rights
			for(int j=0; j<_globally_acc_rights.size(); j++)
			{
				if( _globally_acc_rights.get(j).
					isAllowedWhatWhoForFromTo(_right_type, _user_id,
					this.getId(), this.getNodeType(), child.getNodeType(),
					ini_authorship) )
				{
					global_acc_result = false;
					break;
				}
			}
			
			if(! (child_result || global_def_result || global_acc_result) )
				return false;
		}

//System.out.println("Node " + result);
		return result;
	}


	public boolean isAllowedWhatWhoFromToQuant(int _right_type, int _user_id,
		int _parent_node_type, int _child_node_type, boolean ini_authorship,
		int _quant)
	{
//System.out.println("Node _user_id=" + _user_id + " _right_type=" + _right_type + " _parent_node_type="+_parent_node_type+" _child_node_type=" + _child_node_type);
//System.out.println("Node.isAllowedWhatWhoTo this.id=" + this.getId() +
//	" right_type=" + _right_type + " _user_id=" + _user_id + " _child_node_type=" + _child_node_type);
		boolean result = false;
		for(int i=0; i<this.rights.size(); i++ )
		{
//System.out.println("\tNode.isAllowedWhatWhoTo r.right_type=" + this.rights.get(i).getRightType() +
//	" r.child_type=" + this.rights.get(i).getChildType() + " r.user=" + this.rights.get(i).getUser() + "");
			if( this.rights.get(i).isAllowedWhatWhoForFromToQuant(
				_right_type, _user_id, this.getId(), _parent_node_type,
				_child_node_type, ini_authorship, _quant) )
			{
				result = true;
				break;
			}
		}
//System.out.println("Node " + result);
		return result;
	}

	/**Method checks whether the user specified by a user_is is the author
	 * of the node, true is returned if yes, false - if not
	 * @param _user_id - user id to be checked
	 * @return - true if the object is created by a User denoted by its id,
	 * 	otherwise returns false */
	public boolean isCreatedBy(int _user_id)
	{
//System.out.println("Node.isCreatedBy creator is null?=" + this.getCreator());
//if(this.xtrnl_object != null) System.out.println("	Node.isCreatedBy external creator is null="+xtrnl_object.getCreator());
		if( (this.creator != null) && (this.creator.getId() == _user_id)  )
			return true;
		else
			return false;
	}

	/**Method checks whether the specified user is the author
	 * of this Node, true is returned if yes, false - if not
	 * @param _user - user to be checked
	 * @return - true if the object is created by a User otherwise
	 * 	returns false */
	public boolean isCreatedBy(User _user)
	{
//System.out.println("Node.isCreatedBy creator is null?=" + this.getCreator());
//if(this.xtrnl_object != null) System.out.println("	Node.isCreatedBy external creator is null="+xtrnl_object.getCreator());
		if( (this.creator != null) && (this.creator.getId() == _user.getId()) )
			return true;
		else
			return false;
	}

	/** Returns the creator of the Node, if it has an external object then
	 * return the external object's creator
	 * @return the creator of the Node / ot its External Object */
	public User getCreator() { return creator; }
	
	public String getCreatorAdderNames()
	{
		String result =  "";
		
//		if(getExternalObject() == null)
//		{// creator is the adder
			result = (creator!=null)?creator.getTitle():"--";
//		}
//		else
//		{// there might be othe or the other
//			String creator_t = (getExternalObject().getCreator()!=null)?getExternalObject().getCreator().getTitle():"--";
//			String adder_t = (creator!=null)?creator.getTitle():"--";
//			if( (getExternalObject().getCreator()!=null) && (creator != null) &&
//					getExternalObject().getCreator().getId() == creator.getId())
//				result = creator_t;
//			else
//				result = creator_t + " (" + adder_t + ")";
//		}
	
		return result;
	}

	/** Sets the creator of the Node
	* @param _user - the creator of the external object */
	public void setCreator(User _user) {creator = _user; }

	public void setParent(iNode _parent) { parent = _parent; }
	public iNode getParent() { return parent; }

	// Children property
	public OrderedWeightedItem2Vector<iNode> getChildren() { return children; }

	public int getChildCountOfTypeByUser(int _node_type, int _user_id)
	{
		int result = 0;
		for(int i=0; i<children.size() ;i++)
		{
			if( (children.get(i).getNodeType()==_node_type) &&
				(children.get(i).getUser() != null) &&
				(children.get(i).getUser().getId() == _user_id) )
				result++;
		}
		return result;
	}


	// Expanded property
	public boolean getExpanded() { return expanded; }
	
	public void setExpanded(boolean _expanded) {  expanded = _expanded; }

	/** Method recursively checks whether all the Nodes the parent-child path
	 * to the Node that initialized the check are expanded. If not the
	 * expanded parameter is set ot true.
	 * @param   _res   this parameter has to be set to false in an initial
	 *	call to the function
	 * @return  boolean value of whether any expanded parameters have been
	 * altered
	 */
	public boolean expandParents(boolean _res)
	{
		boolean res = _res;

		if(parent != null)
		{
			if(!parent.getExpanded())
			{
				parent.setExpanded(true);
				res = true;
			}
			parent.expandParents(res);
		}
		/* Stub for multiparent version
		for(int i=0;i<sup_modules.Count();i++)
		{
			if(!sup_modules.At(i).expanded)
			{
				sup_modules.At(i).expanded = true;
				res = true;
			}
			sup_modules.At(i).expandParents(res);
		}/**/
		return res;
	}



//	public void outputSelf(JspWriter out, HttpServletRequest request,
//		int level, int display_mode, User user) throws IOException
//	{
//	}

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

//if((AppDaemon.CONTEXT_ASSETS_PATH_VAL==null))System.out.println("(AppDaemon.CONTEXT_ASSETS_PATH_VAL==null):  " + (AppDaemon.CONTEXT_ASSETS_PATH_VAL==null));
		String div_style = "pt_tree_left_opener_div";
//		float match = getProfileMatch(user);
//		if((double)match >= 0.5)
//			div_style = "pt_tree_left_opener_div3";
//		else
//			if(match >= 0.1)
//				div_style = "pt_tree_left_opener_div2";
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
//System.out.println("(content_greedy_n_trace && !show_nolink) = " + (content_greedy_n_trace && !show_nolink) + " show_nolink="+show_nolink);
				String opener_href = (content_greedy_n_trace) ?
					" href='" + request.getContextPath() + "/content/jspLeft?" + ((expanded)? "collapse" : "expand") + "=" + getId() + "#n" + getId() + "a'" : "";
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

		// print a checkbox or radio
		if(show_checkbox || show_checkbox_locked)
			out.print("<input type='checkbox' name='chk_node" + this.getId() +
				"' value='node" + this.getId() + "'" +
				((show_checkbox_locked)?" checked":"") + ">");
		if(show_radio)
		{
			out.print("<input type='radio' name='rad_node'" +
				" value='node" + this.getId() + "'>");
		}

		// pick an icon
		String icon_file = "";
//		icon_file = iNode.NODE_TYPE_ICONS_SMALL[this.getNodeType()];

		resmap.resolveIcon(this);
		icon_file = resmap.resolveIcon(this);
//		icon_file = ((this.getIcon()==null || this.getIcon().length() == 0)
//				?resmap.getItemtypeList().findById(this.getNodeType()).getIcon():this.getIcon()); 

		// print text of the node
		String style = "pt_tree_left_link";
		style = (current_item != null && getId() == current_item.getId()) ?
			"pt_tree_left_link_current" : style;
		style = (level >=0)?style:"pt_tree_left_item_no_link";

		boolean can_see_author = canSeeAuthor(user, group, resmap);
//		int parent_n_type = (this.getParent() == null) ?
//			iNode.NODE_TYPE_I_ALL : this.getParent().getNodeType();
//
//		if((this.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
//			(resmap != null))
//		{
//			can_see_author = (
//				resmap.isAllowedWhatWhoForFromTo(
//					Right.RIGHT_TYPE_VIEW_AUTHOR, user.getId(), this,
//					parent_n_type, this.getNodeType())
//				||
//				resmap.isAllowedWhatWhoForFromTo(
//					Right.RIGHT_TYPE_VIEW_AUTHOR, group.getId(), this,
//					parent_n_type, this.getNodeType())
//				);
//		}
//System.out.println("Node.outputTree Node.id=" + this.getId() + "can_see_author=" + can_see_author);

		String display_title = getTitle() +
			((can_see_author)? "&nbsp;<sup>[" + this.getCreatorAdderNames() + "]</sup>": "");

		String href = (!show_nolink)?" href='" + request.getContextPath() + "/content/Show?id=" + getId() + "' target='_top'":"";
		out.print("&nbsp;<a" + ((user!=null && isCreatedBy(user.getId())) ? " style='text-decoration:underline;color:#006600;'" : "") + " class='" + style + "' title='" + display_title + "'" + href + ">" ); 
		// print an icon
		out.print("<img src='" + 
//				request.getContextPath() + "/assets/" + 
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

//	/**
//	 * @param out
//	 * @param req
//	 * @param current_node
//	 * @param level - level of the tree, -1 - do not show subnodes
//	 * @param user
//	 * @throws IOException
//	 */
//	public void outputTreeNode(JspWriter out, HttpServletRequest req,
//		iNode current_node, int level, int show_mode, User user,
//		boolean track_opens) throws IOException
//	{
//
//
//
//
///*		String div_style = "pt_tree_left_opener_div";
//		float match = this.getProfileMatch(user);
//		if(match >= .5)
//			div_style = "pt_tree_left_opener_div3";
//		else if(match >= .1)
//			div_style = "pt_tree_left_opener_div2";
//
//		out.print("<div class='" + div_style + "'>");
//		out.println("");
//		//display empty spaces before
////		for(int i=0;i<level;i++)
////			out.print("<IMG SRC='" + req.getContextPath() +
////				AppDaemon.CONTEXT_ASSETS_PATH_VAL +
////				"/dir_empty.gif' alt='.    ' border=0 " +
////				"style='vertical-align:baseline;display:inline'/>");
//		if(level>0)
//			out.print("<IMG SRC='" + req.getContextPath() +
//				AppDaemon.CONTEXT_ASSETS_PATH_VAL +
//				"/dir_empty.gif' height='16' width='" + (level * 18) +
//				"'alt='.    ' border=0 " +
//				"style='vertical-align:baseline;display:inline'/>");
//		// Checkbox?
//		 if(show_mode == HITEM_SHOW_CHECKBOX ||
//			show_mode == HITEM_SHOW_CHECKBOX_LOCKED)
//		 {
//		       // checl whether the concept is in the chosen or not
//		       out.print("<input type='checkbox' name='node" + this.getId() +
//				"' value='node" + this.getId() + "'" +
//				((show_mode == HITEM_SHOW_CHECKBOX_LOCKED)?" disabled checked":"") + ">");
//		 }
//
//		//display own opener or empty if no subs
//		if(level >=0)
//		{
//			if(this.getChildren().size()>0)
//			{
//				String href  ="";
//				String onClick = "";
//				href = (track_opens)?
//					(" href='" + req.getContextPath() +
//					"/content/jspLeft?" +
//					((this.expanded)?(AppDaemon.REQUEST_COLLAPSE):(AppDaemon.REQUEST_EXPAND)) +
//					"=" + this.getId() + "#node" + this.getId() + "'"):" href='#node" + this.getId() +"'";
//				onClick = (track_opens)?"":" onClick='flip_dir_icon(this);'";
//				out.print("<A name='node" + this.getId() +"'" + href + "' class='pt_tree_left_opener_bullet'" + onClick + ">");
////					onClick='flip_dir_icon(this);'
//				//onClick='flip_dir_icon(this);'
//				if(this.getExpanded())
//				{
//					out.print("<IMG SRC='" + req.getContextPath() +
//						AppDaemon.CONTEXT_ASSETS_PATH_VAL +
//						"/dir_minus.gif' alt='[-]' border=0 " +
//						"style='vertical-align:baseline;display:inline'>");
//				}
//				else
//					out.print("<IMG SRC='" + req.getContextPath() +
//						AppDaemon.CONTEXT_ASSETS_PATH_VAL +
//						"/dir_plus.gif' alt='[+]' border=0 " +
//						"style='vertical-align:baseline;display:inline'>");
//				out.print("</A>");
//			}
//			else
//			{// Document (default text)
//				out.print("<A name='node" + this.getId() + "' border=0 style='display:inline'><IMG SRC='" +
//					req.getContextPath() +
//					AppDaemon.CONTEXT_ASSETS_PATH_VAL + "/dir_empbull.gif'" +
//					" alt='&curren;' border=0 style='vertical-align:baseline;display:inline'/></A>");
//			}
//		}
//		//display an icon
//		String icon_file = "";
//		switch (this.getNodeType())
//		{
//			case NODE_TYPE_I_FOLDER: icon_file = "folder_small.gif";
//			break;
//			case NODE_TYPE_I_TOPIC_FOLDER: icon_file = "folder_topic_small.gif";
//			break;
//			case NODE_TYPE_I_UNTYPDOC: icon_file = "doc_small.gif";
//		      break;
//			case NODE_TYPE_I_QUIZ: icon_file = "quiz_small.gif";
//			break;
//			case NODE_TYPE_I_PAPER: icon_file = "cope_paper_small.gif";
//			break;
//			case NODE_TYPE_I_SUMMARY: icon_file = "cope_paper_summary.gif";
//			break;
//			case NODE_TYPE_I_CONCEPTS: icon_file = "concepts_index.gif";
//			break;
//			case NODE_TYPE_I_MYPROFILE: icon_file = "myprofile.gif";
//			break;
//		}
//		out.print("&nbsp;<IMG SRC='" + req.getContextPath() +
//			AppDaemon.CONTEXT_ASSETS_PATH_VAL +
//			"/" + icon_file + "' alt='' border=0 " +
//			"style='vertical-align:baseline;display:inline'>");
//
//
//		//Show title
//		String style = "pt_tree_left_link";
//	      if(show_mode == HITEM_SHOW_CHECKBOX ||
//	           show_mode == HITEM_SHOW_CHECKBOX_LOCKED)
//		     style = "pt_tree_left_item_no_link";
//
////		if(node.user_id==user_id) style = "pt_menu_left_link_owned";
//		if((current_node!=null) && (this.getId() == current_node.getId()))
//			style = "pt_tree_left_link_current";
//		// If authored change color and boldness
//		out.print("&nbsp;<a" +
//			((user != null && this.isCreatedBy(user.getId()))?" style='text-decoration:underline;color:#006600;'":"") + //font-weight:bolder;
//			" class='" + style +
//			"' title='" + this.getTitle() + "'" +
//			((!(show_mode == HITEM_SHOW_CHECKBOX || show_mode == HITEM_SHOW_CHECKBOX_LOCKED))?
//				(" href='" +	req.getContextPath() + "/content/Show?" +
//				AppDaemon.REQUEST_NODE_ID + "=" + this.getId() + "'"):"") +
//
//			" target='_top'>" + this.getTitle() + "</a>");
//		//end of self
//		out.print("</div>");
//		//beginning of children
//		if(this.getChildren().size()>0)
//			out.print("<div style='display:" +
//				((this.getExpanded())?"block":"none")+ "'>");
//		//SHOW
//		for(int i=0; (i<this.getChildren().size() && level>=0);i++)
//			this.getChildren().get(i).outputTreeNode(out, req,
//				current_node, level+1, show_mode, user, false);
//		//end of children
//		if(this.getChildren().size()>0) out.print("</div>");/**/
//	}
//
	public void showAdd(JspWriter out, HttpServletRequest request,
		String cancel_to_url, ResourceMap res_map) throws IOException
	{
//System.out.println("Node.showAdd starting...");
//		out.println("<body>");
		out.println("<p />");
		out.println("<form style='padding:5px 5px 5px 5px;' id='add' name='add' method='post' action='"
			+ request.getContextPath() + "/content/doAdd' target='_top'>");
		out.println("<!-- ID field -->");
		out.println("<input name='" + NODE_FRMFIELD_ID + "' type='hidden' value='" + this.getId() + "'>");
		out.println("<!-- Node Title -->");
		String node_type_name = "";
		switch (this.node_type)
		{
			case iNode.NODE_TYPE_I_FOLDER:
				node_type_name = "Folder";
			break;
			case iNode.NODE_TYPE_I_NONE:
				node_type_name = "Document";
			break;
		}

		out.println("<div class='pt_main_subheader_editing_name'>" + node_type_name + "</div>");
//		out.println("<p>" + this.getTitle() + "</p>");
		out.println("<div class='pt_main_subheader_editing_value'>" + this.getTitle() + "</div>");

		out.println("<!-- Node type field (to be added) -->");
		out.println("<div class='pt_main_subheader_editing_name'>Add document/folder of the following type</div>");
		out.println("<div class='pt_main_subheader_editing_value' title='The type of the document/folder'>");
		out.println("	<select name='fld_node_type' size='1'>");

		// Define what docs/folders are allowed to add
//		boolean all_types = false;
//		long node_types = 0;
//System.out.println("Node.showAdd rights # " + getRights().size());

// DELETE THIS AFTERWARDS
//		for(int i=0; i<this.getRights().size() ;i++)
//		{
//			Right right = this.getRights().get(i);
//			if( (right.getRightType() == Right.RIGHT_TYPE_ADD) ||
//				(right.getRightType() == Right.RIGHT_TYPE_ALL) )
//			{
//				if(right.getChildType() == Node.NODE_TYPE_I_ALL)
//				{
//					all_types = true;
//					break;
//				}
//				else
//				{
//					node_types |= (long)Math.pow(2,right.getChildType());
//				}
//			}
//		}
//System.out.println("Node.showAdd all_types = " + all_types);

		// get user_id and group_id
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();

		// Display all definitive doc/folder styles mark the one specified
//		System.out.println("Node.");
		Integer recent_node_type = (Integer)session.getAttribute(NODE_FRMFIELD_NODETYPE_RECENT);
		
		for(int i=0; i<res_map.getItemtypeList().size(); i++)
		{
			ItemType itype = res_map.getItemtypeList().get(i);
			if(itype.getId() == 1 ) continue;
//			if(
//			res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_ADD, user_id, this, this.getNodeType(), NODE_TYPES_I_DEFINITIVE[i])
//			||
//			res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_ADD, group_id, this, this.getNodeType(), NODE_TYPES_I_DEFINITIVE[i]))

			if(res_map.isAllowedWhatWho2ForFromToQuant(Right.RIGHT_TYPE_ADD,
				user_id, group_id, this, this.getNodeType(), itype.getId()/*NODE_TYPES_I_DEFINITIVE[i]*/))
			{
				out.println("		<option value='" +
					itype.getId()/*NODE_TYPES_I_DEFINITIVE[i]*/ + "'" + ((recent_node_type!=null) && (recent_node_type.intValue()==itype.getId())?" selected":"") + ">" +
					itype/*.getTitle()/*NODE_TYPES_S_DEFINITIVE[i]*/ + "</option>");
			}
		}
		out.println("	</select>");
		out.println("</div>");
		out.println("<p />");
		out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		out.println("<script type='text/javascript'> function mySubmit() { document.add.submit(); }; </script>");
		String cancel_to_url2 = " href='" + cancel_to_url + "' " + "target='_top'";
		out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
		out.println("</p>");
		out.println("</form>");
//		out.println("</body>");
	}// end of -- showAdd

	 public void showCopy(JspWriter out, HttpServletRequest request,
	       String cancel_to_url, ResourceMap res_map) throws IOException
	 {
	 //System.out.println("Node.showCopy starting...");
		out.println("<script type='text/javascript' >");
		out.println("<!--");
		out.println("function tree_opener(node)");
		out.println("{");
		out.println("	nodeDiv = node.parentNode;");
		out.println("	nodeDivPlus = document.getElementById(nodeDiv.id + 'plus');");
		out.println("	nodeDivMinus = document.getElementById(nodeDiv.id + 'minus');");
		out.println("	nodeDivChildren = document.getElementById(nodeDiv.id + 'children');");
		out.println("");
		out.println("	nodeDivPlus.style.display = (nodeDivPlus.style.display == 'none') ? 'inline' : 'none';");
		out.println("	nodeDivMinus.style.display = (nodeDivMinus.style.display == 'none') ? 'inline' : 'none';");
		out.println("	nodeDivChildren.style.display = (nodeDivMinus.style.display == 'inline') ? 'block' : 'none';");
		out.println("}");
		out.println("	function flip_dir_icon(node)");
		out.println("	{");
		out.println("		node.childNodes[0].style.display = (node.childNodes[0].style.display == 'none') ? 'inline' : 'none';");
		out.println("		node.childNodes[1].style.display = (node.childNodes[1].style.display == 'none') ? 'inline' : 'none';");
		out.println("		node.parentNode.nextSibling.style.display = (node.parentNode.nextSibling.style.display == 'none') ? 'block' : 'none';");
		out.println("	}");
		out.println("-->");
		out.println("</script>");
//		out.println("</head>");
//
//		out.println("<body>");
	      out.println("<p />");
	      out.println("<form style='padding:5px 5px 5px 5px;' id='copy' name='copy' method='post' action='"
	             + request.getContextPath() + "/content/doCopy'>");
		out.println("<!-- Parent of the nodes being copied -->");
		out.println("<input name='" + NODE_FRMFIELD_ID + "' type='hidden' value='" + this.getId() + "'>");
	      out.println("<!-- Copy what field -->");
	      out.println("<div class='pt_main_subheader_editing_name'>Copy (select one or more)</div>");
	      out.println("<div style='padding-left:15px;'>");


//	public void outputTree(JspWriter out, HttpServletRequest request,
//		iHTMLHierarchicalItem<iNode> current_item,
//		int level, int display_mode, User user, String jsAction)
//		throws IOException

		int checkbox_mode = iHTMLHierarchicalItem.HHTMLITEM_SHOW_NOLINK;
		if(this.getChildren().size()==0 || this.getChildren().size()==1)
				checkbox_mode |= iHTMLHierarchicalItem.HHTMLITEM_SHOW_CHECKBOX_LOCKED;
		else
				checkbox_mode |= HHTMLITEM_SHOW_CHECKBOX;

		if(this.getChildren().size()>0)
			for(int i=0; i<getChildren().size(); i++)
			      getChildren().get(i).outputTree(out, request, null,
					-1, checkbox_mode, null, null, null, "");
		else
		      this.outputTree(out, request, null, -1, checkbox_mode, null, null, null, "");

	      out.println("</div>");

	      out.println("<!-- To... -->");
	      out.println("<div class='pt_main_subheader_editing_name'>To...</div>");
	      out.println("<div style='padding-left:15px;'>");

		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();

		res_map.outputNodeTree(out, request, null, user_id, group_id,
			(iHTMLHierarchicalItem.HHTMLITEM_CONTENT_FULL_NOTRACE | iHTMLHierarchicalItem.HHTMLITEM_SHOW_RADIO
				| iHTMLHierarchicalItem.HHTMLITEM_SHOW_NOLINK),
			false);

		out.println("</div>");
	      out.println("<p />");

	      out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
	      out.println("<script type='text/javascript'> function mySubmit() { document.copy.submit(); }; </script>");
	      String cancel_to_url2 = " href='" + cancel_to_url + "' " + "target='_top'";
	      out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
	      out.println("</p>");
		out.println("</form>");
//	      out.println("</body>");
	 }// end of -- showCopy

	 public void showDelete(JspWriter out, HttpServletRequest request,
	       String cancel_to_url) throws IOException
	 {
	 //System.out.println("Node.showDelete starting...");
//		out.println("</head>");
//
//		out.println("<body>");
	      out.println("<p />");
	      out.println("<form style='padding:5px 5px 5px 5px;' id='del' name='del' method='post' action='"
	             + request.getContextPath() + "/content/doDelete'>");

//		HttpSession session = request.getSession();
//		int user_id = ((Integer)session.getAttribute(AppDaemon.SESSION_USER_ID)).intValue();
//		int group_id = ((Integer)session.getAttribute(AppDaemon.SESSION_GROUP_ID)).intValue();

		out.println("<input name='" + NODE_FRMFIELD_ID + "' type='hidden' value='" + this.getId() + "'>");
	      out.println("<!-- Are you sure you wanh to helete -->");
	      out.println("<div class='pt_main_subheader_editing_name'>Are you sure?</div>");
	      out.println("<div style='padding-left:15px;color:red;'>\"" + this.getTitle() + "\" and possibly all of its subnodes will be deleted!</div>");
	      out.println("<p />");

	      out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Delete</a>&nbsp;&nbsp;&nbsp;&nbsp;");
	      out.println("<script type='text/javascript'> function mySubmit() { document.del.submit(); }; </script>");

	      String cancel_to_url2 = " href='" + cancel_to_url + "' " + "target='_top'";
	      out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
	      out.println("</p>");
		out.println("</form>");
//	      out.println("</body>");
	 }// end of -- showDelete

	public iNode findChildOfTypeByUser(int _node_type, int _user_id)
	{
		iNode result = null;
//System.out.println("Node.findChildOfTypeByUser _node_type = " + _node_type);
		for(int i=0; i<children.size(); i++)
		{
			if((children.get(i).getNodeType() == _node_type) &&
				children.get(i).isCreatedBy(_user_id))
				return children.get(i);
		}
		return result;
	}

	public boolean canSeeAuthor(User user, User group, ResourceMap resmap)
	{
		// Deal with viewability of author
		boolean can_see_author = false;
		int parent_n_type = (this.getParent() == null) ?
			iNode.NODE_TYPE_I_ALL : this.getParent().getNodeType();

		if((this.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
			(resmap != null))
		{
			can_see_author = (
				resmap.isAllowedWhatWhoForFromTo(
					Right.RIGHT_TYPE_VIEW_AUTHOR, user.getId(), this,
					parent_n_type, this.getNodeType())
				||
				resmap.isAllowedWhatWhoForFromTo(
					Right.RIGHT_TYPE_VIEW_AUTHOR, group.getId(), this,
					parent_n_type, this.getNodeType())
				);
		}

//		String display_title = getTitle() +
//			((can_see_author)? "&nbsp;[" +
//				((this.getCreator()!=null)?this.getCreator().getTitle():"") + "]": "");
		return can_see_author;

	}
	
//	/** Method recursively traverses the node tree and adds all possible 
//	 * parent-child node type pairs
//	 * @param map - map containing all the found node type pairs
//	 * @since 1.5
//	 */
//	public void reportParentChildNodeTypePairs(HashMap<Integer, ItemVector<Item>> map)
//	{
//		for(int i=0; i<children.size(); i++)
//		{
//			Integer parent_type = new Integer(this.getNodeType());
//			int child_type = children.get(i).getNodeType();
//			
//			ItemVector<Item> value = null;
//			
//			if(!map.containsKey(parent_type))
//				value = new ItemVector<Item>();
//			else
//				value = map.get(parent_type);
//				
//			if((value.size()==0) || (value.findById(child_type)==null))
//			{
//				value.add(new Item(child_type,
//					new String(Integer.toString(child_type))));
//				map.put(parent_type, value);
////System.out.println("Node.reportParentChildNodeTypePairs found pair " + parent_type.intValue() + "-" + child_type);				
//			}
//			// recursion on children
//			children.get(i).reportParentChildNodeTypePairs(map);
//		}
//	}
//	
	/** Method creates a separate copy of the node as a parent of the other. External objects are not duplicated but referenced
	 * @param parent - parent of the new node
	 * @param resmap - resource map
	 * @param conn - connection to the database
	 * @since 1.5
	 */
	public void createACopyRecursive(iNode parent, ResourceMap resmap, 
		int user_id, User user, Connection conn)
	{
//		int user_id = this.getCreator().getId();
//		User user = this.getCreator();
		int new_node_id = 0;
		iNode new_node = null;
//		String ext_s = (this.getExternalObject()!=null)?this.getExternalObject().getId()+"":"NULL";
		// connect physically
		
		String description = (this.getDescription()!=null) ? 
			"'" + this.getDescription() + "'" : "NULL";
		String url = (this.getURL()!=null) ? 
			"'" + this.getURL() + "'" : "NULL";
		String uri = (((Node)this).getURI()!=null) ? 
			"'" + ((Node)this).getURI() + "'" : "NULL";
		String qry = "INSERT INTO ent_node (Title, UserID, DateCreated, " +
			"DateModified, DateAltered, ItemTypeID, FolderFlag, ExtID, " +
			"Description, URI, URL) VALUES ('" + this.getTitle() + 
			"', " + user_id + ", NOW(), NOW(), NOW(), " + 
			this.getNodeType() + ", " + this.getFolderFlag() + ", " + 
			"" + ", " + description + ", " + uri + "," + url + ");";
		
		ResultSet rs2 = null;
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
//		PreparedStatement stmt4 = null;
//		PreparedStatement stmt5 = null;
		try
		{
			// insert stub node
			stmt1 = conn.prepareStatement(qry);
			stmt1.executeUpdate();
			stmt1.close();
			stmt1 = null;
//			SQLManager.executeUpdate(conn, qry);

			// get last inserted id
			qry = "SELECT MAX(LAST_INSERT_ID(NodeID)) AS LastID FROM ent_node WHERE UserID=" + user_id + ";";
			stmt2 = conn.prepareStatement(qry);
			rs2 = stmt2.executeQuery(qry);
			while(rs2.next())
			{
				new_node_id = rs2.getInt("LastID");
			}
			rs2.close();
			rs2 = null;
			stmt2.close();
			stmt2 = null;

			// connect new node to the parent
			qry = "INSERT INTO rel_node_node (ParentNodeID, ChildNodeID, Weight, OrderRank)" +
				" VALUES (" + parent.getId() + ", " + new_node_id + ", 1, " + (parent.getChildren().size()+1) + ");";
			
			
			stmt3 = conn.prepareStatement(qry);
			stmt3.executeUpdate();
			stmt3.close();
			stmt3 = null;
//			SQLManager.executeUpdate(conn, qry);
			
			// create actual node object and external object if necessary
			new_node = new Node(new_node_id, this.getTitle(), this.getURI(),
				this.getNodeType(), this.getDescription(), this.getURL(), 
				this.getFolderFlag(), this.getIcon(), this.isHidden() );
			new_node.setCreator(user);

			// add new node to the resource map and rewrite resource
			// map into session
			parent.getChildren().add(new_node);
			new_node.setParent(parent);
			resmap.getNodes().add(new_node);
//System.out.println(" nodeeeee " + edited_node.getTitle());
		}//end -- try
		catch (SQLException e) { e.printStackTrace(System.err); }
		finally
		{
			if (rs2 != null)
			{
				try { rs2.close(); } catch (SQLException e) { ; }
				rs2= null;
			}
			if (stmt1 != null)
			{
				try { stmt1.close(); } catch (SQLException e) { ; }
				stmt1 = null;
			}
			if (stmt2 != null)
			{
				try { stmt2.close(); } catch (SQLException e) { ; }
				stmt2 = null;
			}
			if (stmt3 != null)
			{
				try { stmt3.close(); } catch (SQLException e) { ; }
				stmt3 = null;
			}
		}
		// continue on with recursion to children
		for(int i=0; i<this.getChildren().size(); i++)
		{
			this.getChildren().get(i).createACopyRecursive(new_node, resmap, user_id, user, conn);
		}
	}

	/** Method returns the rating represented as a group of values - the members of the group
	 * @return - the rating represented as a group of values - the members of the group
	 * @since 1.5
	 */
	public Rating getGroupRating() { return group_rating; }

	/** Method returns user's personal rating
	 * @return user's personal rating
	 * @since 1.5
	 */
	public Rating getPersonalRating() { return personal_rating; }
		
	/** Method sets the rating represented as a group of values - the members of the group
	 * @since 1.5
	 */
	public void setGroupRating(Rating _rating) { group_rating = _rating; }

	/** Method sets user's personal rating
	 * @since 1.5
	 */
	public void setPersonalRating(Rating _rating) { personal_rating = _rating; }

//	public String getRatingLabels() { return NODE_RATING_LABELS; }
//
//	public String[] getRatingLabelsArray() { return NODE_RATING_LABELS_ARRAY; }

	public boolean isStoredInDB() { return stored_in_db; }

	public void setStoredInDB(boolean _is_stored) { stored_in_db = _is_stored; }

	public void showView(JspWriter out, HttpServletRequest request/*, boolean show_ratings*/) throws IOException
	{
//		String cancel_to = request.getContextPath() + "/content" + 
//			"/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + this.getId();
		
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		ResourceMap resmap = (ResourceMap) session.getAttribute(
				ClientDaemon.SESSION_RES_MAP);
		User user = resmap.getUsers().findById(user_id);
		User group = resmap.getUsers().findById(group_id);
		
		boolean can_see_author = canSeeAuthor(user, group, resmap);
		String display_author = ((can_see_author)? "&nbsp<sup>[" + 
				this.getCreatorAdderNames() + "]</sup>": "");
	//System.out.println("Node.showView can_see_author = " + can_see_author);	
		// up-dir icon
	//	boolean no_way_up = false;
		
		if(this.folder_flag)
		{// if it's a folder - show sub-nodes (not for root)
			if(this.getNodeType() != iNode.NODE_TYPE_I_ALL)
			{
				// Show 'Description' caption
				out.println("<div class='pt_main_subheader'>" +
					 this.getTitle() + display_author + "</div>");
					 
				out.println("<p>" + this.getDescription() + "</p>");
			}
//System.out.println("~~~ [KTree2] Node.showView showing " + this.getChildren().size() + " items in folder");	
			
			resmap.displayFolderView(this, out, request/*, show_ratings*/);
		}// -- end if it's a folder - show sub-nodes
		else
		{// otherwise show default document
			if( (this.getDescription() != null) && 
				(!this.getDescription().equals("")) )
			{// show a description if it exists
				// Show 'Description' caption
	/*				out.println("<div style='font-size:0.9em; " + 
					"font-weight:bold; color:#000099; " +
					"margin:15px 0px 5px 0px; border:0px; " +
					"border-bottom:1px solid #999999; " +
					"font-family:Times, serif;'>" +
					"Current folder description</div>");*/
				out.println("<p>" + this.getDescription() + "</p>");	
			}
//			else if( (this.getURL() != null) && 
//				(!this.getURL().equals("")) )
//			{
//				boolean has_quest = this.getURL().indexOf("?") != -1;
//				String sess_ = session.getId().substring(session.getId().length()-5);
//				String url = this.getURL() + ((has_quest)?"&":"?") +
//					"usr=" + user.getLogin() + "&grp=" + 
//					group.getLogin() + "&sid=" + sess_;
//				//Show the url and append user,group & session id
//				out.println("<script>");
//				out.println("	document.location = '" + url + "';");
//				out.println("</script>");
//				out.println("<body>");
//				out.println("</body>");
//				;
//			}
			else
			// otherwise - default text
				out.println("<p>This document is empty.</p>");
			
		}// -- end otherwise show default document
		//if(show_ratings) showRatings(out, request, cancel_to);
	}
	
	/*
	public void showRatings(JspWriter out, HttpServletRequest request,
		String cancel_to) throws IOException
	{
		float value = 0;
		String value_s = "0";
		String rating_icon;
		String rating_html = "";
		out.println("<p/>");
		
		// PERSONAL RATING
		// grab value
		if(this.getPersonalRating() != null)
		{
			value = this.getPersonalRating().getRatingValue();
			value_s = Integer.toString((int)value);
		}
		
		rating_icon = (value!=0)?"stars_own" + (int)value + ".gif":"stars_0.gif";
		
		// personal rating html
		rating_html += "<div id='your_rating' style='display:block;'>" +
			"<img src='" + request.getContextPath() + 
			"/assets/dir_empbull.gif'>&nbsp;" +
			"<span style='font-size:0.9em; font-weight:bold; color:#000099; font-family:Times, serif;'>My rating:" +
			"</span>&nbsp;<img src='" + request.getContextPath() +
			"/assets/" + rating_icon + "' " + 
			"width='53' height='16'>&nbsp;" + 
			((value!=0)?value_s+"/5&nbsp;&nbsp;<img src='" + request.getContextPath() +
					"/assets/" +
				(this.getPersonalRating().getAnonymous()?
					"anonymous.gif'>": "signed.gif'>")
			:"N/A" ) + "&nbsp;&nbsp;<a href='#'><img border='0' src='" +
				request.getContextPath() + "/assets/" +
			((value!=0)?"edit":"add") + "_enable.gif' title='" +
			((value!=0)?"Edit":"Add") + " your rating' alt='[" +
			((value!=0)?"Edit":"Add") + "]' width='16' height='16' " + 
			"onClick='document.getElementById(\"add_edit_rating\")." +
			"style.display=\"block\";document.getElementById" +
			"(\"your_rating\").style.display=\"none\";document." + 
			"getElementById(\"group_rating\").style.display=\"none\";'>" +
			"</a>&nbsp;&nbsp;" + ((value!=0)?"" + this.getPersonalRating().getComment():"") + "</div>";
		
		// personal comment add/edit form
		rating_html += "\n\n <form id='add_edit_rating' name='add_edit_rating' style='display:none;' method='post' action='" +
			request.getContextPath() + "/content/doEdit' target='_top'>";
	//	rating_html += "\n<table width='100%' id='add_edit_rating_table' name='add_edit_rating_table'  border='0' cellspacing='0' cellpadding='0' style='display:block;'>";
		rating_html += "\n\t<script language='javascript' src='" +
			request.getContextPath() + "/assets/CastleRater.js'> var Likert = new Array(); </script>";
		rating_html += "\n\t<script language='javascript'>";
		
		// grabbing the scale of ratings
		
	//	rating_html += "\n\t\tLikert=" + ((getExternalObject() !=null )?getExternalObject().getRatingLabels():this.getRatingLabels()) + ";";
		rating_html += "\n\t\tLikert=" + Rating.RATING_LABELS + ";";
	
	//	String[] rating_labels_array = ((getExternalObject() !=null )?getExternalObject().getRatingLabelsArray():this.getRatingLabelsArray());
		String[] rating_labels_array = Rating.RATING_LABELS_ARRAY;
		
		rating_html += "\n\t\tfunction tree_opener(node)";
		rating_html += "\n\t\t{";
		rating_html += "\n\t\t\tnodeDiv = node.parentNode;";
		rating_html += "\n\t\t\tnodeDivPlus = document.getElementById(nodeDiv.id + 'plus');";
		rating_html += "\n\t\t\tnodeDivMinus = document.getElementById(nodeDiv.id + 'minus');";
		rating_html += "\n\t\t\tnodeDivChildren = document.getElementById(nodeDiv.id + 'children');";
		rating_html += "\n\t\t";
		rating_html += "\n\t\t\tnodeDivPlus.style.display = (nodeDivPlus.style.display == 'none') ? 'inline' : 'none';";
		rating_html += "\n\t\t\tnodeDivMinus.style.display = (nodeDivMinus.style.display == 'none') ? 'inline' : 'none';";
		rating_html += "\n\t\t\tnodeDivChildren.style.display = (nodeDivMinus.style.display == 'inline') ? 'block' : 'none';";
		rating_html += "\n\t\t}";
		
		rating_html += "\n\t</script>";
		rating_html += "\n\t<input type='hidden' id='" + NODE_FRMFIELD_RATING_VALUE + "' name='" + NODE_FRMFIELD_RATING_VALUE + "' value='" + value_s + "' />";
		rating_html += "\n\t<input type='hidden' id='" + NODE_FRMFIELD_RATING_TEXT_VALUE + "' name='" + NODE_FRMFIELD_RATING_TEXT_VALUE + "' value='" + ((value!=0)?rating_labels_array[(int)value-1]:"") + "' />";
		rating_html += "\n\t<input type='hidden' id='" + NODE_FRMFIELD_RATING_EDIT_FLAG + "' name='" + NODE_FRMFIELD_RATING_EDIT_FLAG + "' value='" + ((value!=0)?"1":"0") + "' />";
	
		rating_html += "\n\t<input name='" + NODE_FRMFIELD_ID + "' type='hidden' value='" + this.getId() + "'>";
	
	
		rating_html += "\n\t\t<span style='font-size:0.9em; font-weight:bold; color:#000099; font-family:Times, serif;'>" + 
			"My rating:</span>&nbsp;<a id='your_rating_js' ";
		rating_html += "\n\t\t\tonMouseOut=\"document.getElementById('latestHover').innerHTML=document.getElementById('" + NODE_FRMFIELD_RATING_TEXT_VALUE + "').value;\"";
		rating_html += "\n\t\t\tonMouseDown=\"document.getElementById('" + NODE_FRMFIELD_RATING_TEXT_VALUE + "').value=(Likert[your_rating_js.HoverValue-1]);\"";
		rating_html += "\n\t\t\tonMouseOver=\"document.getElementById('latestHover').innerHTML=(Likert[your_rating_js.HoverValue-1]);\"><script language='javascript'>var your_rating_js = CastleRater.CreateJSControl('your_rating_js', '" + 
			request.getContextPath() + "/assets/star_on.gif', '" + 
			request.getContextPath() + "/assets/star_off.gif', '" +
			request.getContextPath() + "/assets/star_over.gif', 5, " +
			value_s + ");</script></a>";
		
		rating_html += "\n\t\t\t<a id='latestHover' >" + ((value!=0)?rating_labels_array[(int)value-1]:"") + "</a><br />"; // onClick='alert("Rating value = " + your_rating_js.Value);'
		rating_html += "\n\t\t\t<label><input type='radio' id='" + NODE_FRMFIELD_RATING_ANONYMITY + "' name='" + NODE_FRMFIELD_RATING_ANONYMITY + "' value='signed'" + ((value!=0 && !this.getPersonalRating().getAnonymous())?" checked":"") + "><img src='" + 
			request.getContextPath() + "/assets/signed.gif'></label>&nbsp;&nbsp;" ;
		rating_html +=  "<label><input type='radio' id='" + NODE_FRMFIELD_RATING_ANONYMITY + "' name='" + NODE_FRMFIELD_RATING_ANONYMITY + "' value='anonymous'" + ((value!=0 && this.getPersonalRating().getAnonymous())?" checked":"") + "><img src='" + 
			request.getContextPath() + "/assets/anonymous.gif'></label>";
		rating_html += "<br/><textarea name='" + NODE_FRMFIELD_RATING_COMMENT + "' cols='40' rows='5' id='" + NODE_FRMFIELD_RATING_COMMENT + "'>" +
			((value != 0)?this.getPersonalRating().getComment():"")  + "</textarea><br/><br/>";
			rating_html += "\n<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;";
		rating_html += "\n<script type='text/javascript'>";
		rating_html += "\nfunction mySubmit()";
		rating_html += "\n{";
		rating_html += "\n\tvar error_msg = '';";
		rating_html += "\n\tvar error = false";
		rating_html += "\n\tif( document.getElementById('" + NODE_FRMFIELD_RATING_VALUE + "').value == '0' )";
		rating_html += "\n\t{";
		rating_html += "\n\t\terror_msg += 'Please choose your rating value\\n';";
		rating_html += "\n\t\terror = true;";
		rating_html += "\n\t}";
		rating_html += "\n\tif( !((document.add_edit_rating." + NODE_FRMFIELD_RATING_ANONYMITY + "[0].checked) || (document.add_edit_rating." + NODE_FRMFIELD_RATING_ANONYMITY + "[1].checked)))";
		rating_html += "\n\t{";
		rating_html += "\n\t\terror_msg += 'Please choose your rating to be either Signed or Anonymous\\n';";
		rating_html += "\n\t\terror = true;";
		rating_html += "\n\t}";
	
		rating_html += "\n\tif(error) alert(error_msg );";
		rating_html += "\n\telse";
		rating_html += "\n\t\tdocument.add_edit_rating.submit();";
		rating_html += "\n}";
		rating_html += "\n</script>";
		rating_html += "\n<a class='pt_main_edit_button_cancel' href='" + 
			cancel_to + "' target='_top'>Cancel</a>";
		rating_html += "\n</p>";
		rating_html += "\n</form>";
	
		// print personal rating html				
		out.println(rating_html);
	
		// GROUP RATING
		// grab value
		value = 0;
		value_s = "0";
		
		if(this.getGroupRating() != null) 
			value = this.getGroupRating().getRatingValue();
	
		if(((float)Math.round((float)value*2))/2 == Math.ceil(((float)Math.round((float)value*2))/2))
		{
			rating_icon = (value!=0)?"stars_other" + (int)value + ".gif":"stars_0.gif";
			value_s = Integer.toString((int)value);
		}
		else
		{
			rating_icon = (value!=0)?"stars_other" + ((float)Math.round((float)value*2))/2 + ".gif":"stars_0.gif";
			value_s = Float.toString(((float)Math.round((float)value*2))/2);
		}
		
		// group rating html
		rating_html = "<div id='group_rating' style='display:block;'>" +
			((value!=0)?
			"<a onClick='tree_opener(this)'><img id='group_ratingminus' src='" +
			request.getContextPath() + "/assets/dir_minus.gif' style='display:none;'><img id='group_ratingplus' src='" + 
			request.getContextPath() + "/assets/dir_plus.gif'  style='display:inline;'></a>"  
			:"<img src='" + request.getContextPath() + "/assets/dir_empbull.gif'>"
			) +
			"&nbsp;<span style='font-size:0.9em; font-weight:bold; color:#000099; font-family:Times, serif;'>Group rating:</span>&nbsp;<img src='" +
			request.getContextPath() + "/assets/" + rating_icon + "' width='53' height='16'>&nbsp;" + ((value!=0)?value_s + "/5":"N/A");
		if(value != 0) // if there are group ratings
		{
			// deal with ability to see atuhors of ratings
			HttpSession session = request.getSession();
			ResourceMap res_map = (ResourceMap) session.
				getAttribute(ClientDaemon.SESSION_RES_MAP);
			int user_id = ((Integer)session.getAttribute(ClientDaemon.
				SESSION_USER_ID)).intValue();
			int group_id = ((Integer)session.getAttribute(ClientDaemon.
				SESSION_GROUP_ID)).intValue();
			User user = res_map.getUsers().findById(user_id);
			User group = res_map.getUsers().findById(group_id);
			
			boolean can_see_author = canSeeAuthor(user, group, res_map);
	 
				rating_html += "\n\t<div id='group_ratingchildren' style='display:none;'>";
	
			for(int i=0; i<this.getGroupRating().getRatings().size(); i++)
			{
				value = 0;
				value_s = "0";
				value = this.getGroupRating().getRatings().get(i).getRatingValue();
				
				if(((float)Math.round((float)value*2))/2 == Math.ceil(((float)Math.round((float)value*2))/2))
				{
					rating_icon = (value!=0)?"stars_other" + (int)value + ".gif":"stars_0.gif";
					value_s = Integer.toString((int)value);
				}
				else
				{
					rating_icon = (value!=0)?"stars_other" + ((float)Math.round((float)value*2))/2 + ".gif":"stars_0.gif";
					value_s = Float.toString(((float)Math.round((float)value*2))/2);
				}
	
				rating_html += "\n\t\t<div style='display:block;'><img src='" +
					request.getContextPath() + "/assets/dir_empty.gif' width='18' height='16'>" 
					+ ((can_see_author && !this.getGroupRating().getRatings().get(i).getAnonymous())?"[" + this.getGroupRating().getRatings().get(i).getUser().getTitle() + "]&nbsp;&nbsp;":"") + "<img src='" + 
					request.getContextPath() + "/assets/" + 
					rating_icon + "'>&nbsp;&nbsp;" + this.getGroupRating().getRatings().get(i).getComment() + "</div>";
			}
	
			rating_html += "\n\t</div>";
		}
		rating_html += "\n</div>";
	
	
		// print group rating html	
		out.println(rating_html + " </div>");
	
	}
	*/
	public void showViewHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
//		if( (((this.node_type == iNode.NODE_TYPE_I_FOLDER)||(this.node_type == iNode.NODE_TYPE_I_NONE))&&(this.xtrnl_object != null)) &&
//			(!this.getFolderFlag()) && (this.getURL() != null) && 
//			(!this.getURL().equals("")) )
//		{
//			out.println("<meta http-equiv='refresh' content='0;URL=" +
//				this.getURL() + "'>");
//		}
		
		// REPORT VISIT
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		if(this.getId() > 0 )
		{
	//System.out.println("[KTree] Node.showView reporting to UMS=" + reportApi.getUMS());			
			ResourceMap res_map = (ResourceMap) session.
					getAttribute(ClientDaemon.SESSION_RES_MAP);
			user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
			group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
			User user_ = res_map.getUsers().findById(user_id);
			User group_ = res_map.getUsers().findById(group_id);
			String sess_ = session.getId().substring(session.getId().length()-5);
			
			String svc = (String)session.getAttribute("svc");
					
			
			ClientDaemon cd = ClientDaemon.getInstance();
			cd.getReportAPI().report(ReportAPI.APPLICATION_KNOWLEDGETREE, "ktree" + this.getId(), sess_, -1, 
					group_.getLogin(), user_.getLogin(), "Portal" + ((svc!=null && svc.length()>0)?";"+svc:""));
		}
		
//		out.println("</head>");
//		out.println("<body>");
	}
	
	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		StringBuffer browserDetector = HTMLUtilities.javaScriptBrowserDetector();
		
		out.println(browserDetector);
		
		out.println("<script type='text/javascript'>");
		out.println("   _editor_url = '" + request.getContextPath() + "/assets/htmlarea/';");
		out.println("   _editor_lang = 'en';");
		out.println("</script>");
		out.println("<script type='text/javascript' src='" + request.getContextPath() + "/assets/htmlarea/htmlarea.js'></script>");
		out.println("<script type='text/javascript' >");
		out.println("<!--");
		out.println("var editor = null;");
		out.println("function initEditor()");
		out.println("{");
		out.println("	editor = new HTMLArea('fld_description');");
		out.println("	editor.generate();");
		out.println("	return false;");
		out.println("}");
		out.println("function insertHTML()");
		out.println("{");
		out.println("	var html = prompt('Enter some HTML code here');");
		out.println("	if (html) { editor.insertHTML(html); }");
		out.println("}");
		out.println("function highlight()");
		out.println("{");
		out.println("	editor.surroundHTML('<span style=\"background-color: yellow\">', '</span>');");
		out.println("}");
		out.println("function flip(node, dir)");
		out.println("{");
		out.println("	actor = node.parentNode.parentNode;");
	//	out.println("	alert('actor.id='+actor.id);");
		out.println("	swap = null;");
		out.println("	if(dir==-1) // swap up");
		out.println("	{");
		out.println("		swap = actor.previousSibling;");
		out.println("	}");
		out.println("	else if(dir==1) // swap down");
		out.println("	{");
		out.println("		swap = actor.nextSibling;");
		out.println("	}");
	//	out.println("	alert('swap.id='+swap.id);");
		out.println("	swap_id = swap.id;");
		out.println("	if(swap_id!=null) // swap if somewhere to swap");
		out.println("	{");
		out.println("		i_actor_id = parseInt(actor.id.substring(3));");
		out.println("		i_swap_id = parseInt(swap.id.substring(3));");
		out.println("		actor_in = actor.childNodes[0];");
		out.println("		swap_in = swap.childNodes[0];");
	//	out.println("		actor_in = actor.childNodes[0];");
	//	out.println("		swap_in = swap.childNodes[0];");
		out.println("		");
		out.println("		actor_in_html = actor_in.innerHTML;");
		out.println("		actor_in.innerHTML = swap_in.innerHTML;");
		out.println("		swap_in.innerHTML = actor_in_html;");
		out.println("		");
		out.println("		actor_in.childNodes[0].value = i_actor_id;");
		out.println("		swap_in.childNodes[0].value = i_swap_id;");
		out.println("		document.getElementById('" + NODE_FRMFIELD_WEIGHTORDCHANGED + "').value = 1;");
		out.println("	}");
		out.println("}");
		out.println("-->");
		out.println("</script>");
		out.println("</head>");
		out.println("");
		out.println("<body onload='HTMLArea.init(); HTMLArea.onload = initEditor;'>");
	}
	
	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException
	{
		out.println("<form style='padding:5px 5px 5px 5px;' id='edit' name='edit' method='post' action='"
			+ request.getContextPath() + "/content/doEdit' target='_top'>");
		out.println("<!-- ID field -->");
		out.println("<input name='" + NODE_FRMFIELD_ID + "' type='hidden' value='" + this.getId() + "' />");
		out.println("<!-- Title field -->");
		out.println("<div class='pt_main_subheader_editing_name'>Title</div>");
		out.println("<div class='pt_main_subheader_editingue' title='The title of the document/folder'><input name='" + NODE_FRMFIELD_TITLE + "' type='text' value=\"" + this.getTitle() + "\" size='70' maxlength='255' /></div><p/>");
		out.println("<!-- URL field -->");
		out.println("<div class='pt_main_subheader_editing_name'>URL</div>");
		out.println("<div class='pt_main_subheader_editingue' title='The URL the document/folder is associated with'><input name='" + NODE_FRMFIELD_URL + "' type='text' value=\"" + this.getURL() + "\" size='70' maxlength='255' /></div>");
		out.println("<!-- URI field -->");
//		boolean is_internal_type = ((this.node_type == iNode.NODE_TYPE_I_FOLDER)||(this.node_type == iNode.NODE_TYPE_I_NONE));
		out.println("<div class='pt_main_subheader_editing_name'>URI</div>");
		out.println("<div class='pt_main_subheader_editingue' title='The URI the document/folder is associated with'><input name='" + NODE_FRMFIELD_URI + "' type='text' value='" + this.getURI() + "' size='70' maxlength='255' readonly /></div><p/>");
		if(request.isUserInRole("admin"))
		{
			out.println("<!-- Hidden flag field -->");
			out.println("<div class='pt_main_subheader_editing_name'>Hidden</div>");
			out.println("<div class='pt_main_subheader_editingue' title='Hidden Flag'><input name='" + NODE_FRMFIELD_HIDDEN + "' type='checkbox' value='1' " + (this.isHidden()?"checked":"") + "/></div><p/>");
		}
	/*		out.println("<!-- Node type field -->");
		out.println("<div style='font-family::\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Node type</div>");
		out.println("<div style='padding:0px 0px 10px 15px;' title='The type of the document/folder'>");
		out.println("	<select name='fld_node_type' size='1'>");
		// Display all definitive doc/folder styles mark the one specified
		for(int i=0; i<NODE_TYPES_S_DEFINITIVE.length; i++)
		{
			boolean selected = this.node_type == NODE_TYPES_I_DEFINITIVE[i];
			out.println("		<option value='" + 
				NODE_TYPES_I_DEFINITIVE[i] + "'" + 
				((selected)?" selected":"") + ">" + 
				NODE_TYPES_S_DEFINITIVE[i] + "</option>");
		}
		out.println("	</select>");
		out.println("</div>");*/
		out.println("<!-- Description field -->");
		out.println("<div class='pt_main_subheader_editing_name'>Description</div>");
		out.println("<textarea name='fld_description' id='" + NODE_FRMFIELD_DESCRIPTION + "' style='width:100%;padding:0px' rows='20' cols='80' title='The description of the document/folder'>");
		out.println(this.description);
	/*		out.println("&lt;p&gt;Here is some sample text: &lt;b&gt;bold&lt;/b&gt;, &lt;i&gt;italic&lt;/i&gt;, &lt;u&gt;underline&lt;/u&gt;. &lt;/p&gt;");
		out.println("&lt;p align=center&gt;Different fonts, sizes and colors (all in bold):&lt;/p&gt;");
		out.println("&lt;p&gt;&lt;b&gt;");
		out.println("&lt;font face='arial'           size='7' color='#000066'&gt;arial&lt;/font&gt;,");
		out.println("&lt;font face='courier new'     size='6' color='#006600'&gt;courier new&lt;/font&gt;,");
		out.println("&lt;font face='georgia'         size='5' color='#006666'&gt;georgia&lt;/font&gt;,");
		out.println("&lt;font face='tahoma'          size='4' color='#660000'&gt;tahoma&lt;/font&gt;,");
		out.println("&lt;font face='times new roman' size='3' color='#660066'&gt;times new roman&lt;/font&gt;,");
		out.println("&lt;font face='verdana'         size='2' color='#666600'&gt;verdana&lt;/font&gt;,");
		out.println("&lt;font face='tahoma'          size='1' color='#666666'&gt;tahoma&lt;/font&gt;");
		out.println("&lt;/b&gt;&lt;/p&gt;");
		out.println("&lt;p&gt;Click on &lt;a href='http://www.interactivetools.com/'&gt;this link&lt;/a&gt; and then on the link button to the details ... OR ... select some text and click link to create a &lt;b&gt;new&lt;/b&gt; link.&lt;/p&gt;");
	*/
		out.println("</textarea>");
		out.println("</div>");
		
		
		// Pring children names, weights and ordering
		boolean is_weighted = true;
		boolean is_ordered = true;
		
		if( (this.getChildren().size()>0) && (is_weighted || is_ordered) )
		{
			out.println("<!-- Folder content -->");
			out.println("<div class='pt_main_subheader_editing_name'>Content</div>");// 
			out.println("<input name='" + NODE_FRMFIELD_WEIGHTORDCHANGED + "' type='hidden' id='" + NODE_FRMFIELD_WEIGHTORDCHANGED + "' value='0'>");
			out.print("<div>");
			for( int i=0; i<this.getChildren().size(); i++)
			{
	//System.out.println("Node.showEdit child:" + this.getChildren().get(i).getTitle() + " weight:" + this.getChildren().getWeight(i));			
				out.println("<div id='pos" + (i+1) + "'><div style='display:inline;'><input name='doc" + this.getChildren().get(i).getId() + "' value='" + (i+1) + "' type='hidden'>");
				out.println("	<a name='anch" + i + "'></a><a href='#anch" + i + "' title='Move Up' onClick='flip(this, -1);'>&nbsp;&uarr;&nbsp;Up</a>&nbsp;<a href='#anch" + i + "' title='Move Down' onClick='flip(this, 1);'>&nbsp;&darr;&nbsp;Down</a>&nbsp;Weight:&nbsp;");
				out.println("	<select name='weight" + (this.getChildren().get(i).getId()) + "' style='width:50px;' onChange='document.getElementById(\"" + NODE_FRMFIELD_WEIGHTORDCHANGED + "\").value = 1;'>");
				out.println("		<option value='0.1'" + ((this.getChildren().getWeight(i)==0.1)?" selected":"") + ">0.1</option>");
				out.println("		<option value='0.2'" + ((this.getChildren().getWeight(i)==0.2)?" selected":"") + ">0.2</option>");
				out.println("		<option value='0.3'" + ((this.getChildren().getWeight(i)==0.3)?" selected":"") + ">0.3</option>");
				out.println("		<option value='0.4'" + ((this.getChildren().getWeight(i)==0.4)?" selected":"") + ">0.4</option>");
				out.println("		<option value='0.5'" + ((this.getChildren().getWeight(i)==0.5)?" selected":"") + ">0.5</option>");
				out.println("		<option value='0.6'" + ((this.getChildren().getWeight(i)==0.6)?" selected":"") + ">0.6</option>");
				out.println("		<option value='0.7'" + ((this.getChildren().getWeight(i)==0.7)?" selected":"") + ">0.7</option>");
				out.println("		<option value='0.8'" + ((this.getChildren().getWeight(i)==0.8)?" selected":"") + ">0.8</option>");
				out.println("		<option value='0.9'" + ((this.getChildren().getWeight(i)==0.9)?" selected":"") + ">0.9</option>");
				out.println("		<option value='1.0'" + ((this.getChildren().getWeight(i)==1.0)?" selected":"") + ">1.0</option>");
				out.println("		<option value='2.0'" + ((this.getChildren().getWeight(i)==2.0)?" selected":"") + ">2.0</option>");
				out.println("		<option value='3.0'" + ((this.getChildren().getWeight(i)==3.0)?" selected":"") + ">3.0</option>");
				out.println("		<option value='4.0'" + ((this.getChildren().getWeight(i)==4.0)?" selected":"") + ">4.0</option>");
				out.println("		<option value='5.0'" + ((this.getChildren().getWeight(i)==5.0)?" selected":"") + ">5.0</option>");
				out.println("		<option value='6.0'" + ((this.getChildren().getWeight(i)==6.0)?" selected":"") + ">6.0</option>");
				out.println("		<option value='7.0'" + ((this.getChildren().getWeight(i)==7.0)?" selected":"") + ">7.0</option>");
				out.println("		<option value='8.0'" + ((this.getChildren().getWeight(i)==8.0)?" selected":"") + ">8.0</option>");
				out.println("		<option value='9.0'" + ((this.getChildren().getWeight(i)==9.0)?" selected":"") + ">9.0</option>");
				out.println("		<option value='10.0'" + ((this.getChildren().getWeight(i)==10.0)?" selected":"") + ">10.0</option>");
				out.println("	</select>");
				out.println("	&nbsp;&nbsp;" + this.getChildren().get(i).getTitle() + "</div>");
				out.print("</div>");
			}
			out.print("</div>");
		}
		
		out.print("<div>&nbsp;</div>");
		out.println("<div>");
		out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		
		out.println("<script type='text/javascript'>");
		out.println("function mySubmit()");
		out.println("{");
		out.println("	var error_msg = 'Fields:\\n';");
		out.println("	var error = false");
		out.println("	");
		out.println("	if( document.edit.fld_title.value.length == 0 )");
		out.println("	{");
		out.println("		errror_msg += '  * Title';");
		out.println("		error = true;");
		out.println("	}");
		out.println("	error_msg = '\\n should not contail an empty string';");
		out.println("	");
		out.println("	if(error) alert(error_msg );");
		out.println("	else");
		out.println("	{");
	//	out.println("		alert(navigator.appName);");
		out.println("		if(BrowserDetect.browser!='Safari') document.edit.onsubmit();");
		out.println("		document.edit.submit();");
		out.println("	}");	
		out.println("}");
		out.println("</script>");
		
	//**//**		
	//	String cancel_to_url = " href='" + request.getContextPath() + "/content" + 
	//		"/Show" +
	//		((cancel_to != null)?
	//			("?" + ClientDaemon.REQUEST_NODE_ID + "=" + cancel_to.getId()):""
	//		) + "' " + "target='_top'";
	
		String cancel_to_url2 = " href='" + cancel_to_url + "' " + "target='_top'";
	//System.out.println("NodeUn : cancel_to_url = " + cancel_to_url);
	//System.out.println("NodeUn : cancel_to_url2 = " + cancel_to_url2);
	
		out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
		out.println("</div>");
		out.println("</form>");
	}
	
	public void saveToDB(Connection conn, HttpServletRequest request, iNode node,
			int changes) throws Exception
	{
		// if not stored in DB - exit
		if(!stored_in_db) return;
		
		String qry = "";
		String qry_children = "";
		
		// Process Title
		if((changes & iNode.NODE_CHANGE_TITLE) > 0)
		{
			qry += ((qry.length() > 0)?" ,":"") + "Title='" +
					SQLManager.stringUnquote(this.getTitle()) + "'";
		}
		
		// Process URL
		if((changes & iNode.NODE_CHANGE_URL) > 0)
		{
			qry += ((qry.length() > 0)?" ,":"") + "URL='" +
					SQLManager.stringUnquote(this.url) + "'";
		}
		
		// Process URI
		if((changes & iNode.NODE_CHANGE_URI) > 0)
		{
			qry += ((qry.length() > 0)?" ,":"") + "URI='" +
					SQLManager.stringUnquote(this.getURI() ) + "'";
		}
	
		// Process Description
		if((changes & iNode.NODE_CHANGE_DESCRIPTION) > 0)
		{
			qry += ((qry.length() > 0)?", ":"") + "Description='" +
					SQLManager.stringUnquote(this.description) + "'";
		}
		
		// Process Hidden
		if((changes & iNode.NODE_CHANGE_HIDDEN) > 0)
		{
			qry += ((qry.length() > 0)?", ":"") + "Hidden=" +
					((this.isHidden())?1:0);
		}
		
		PreparedStatement stmt1 = null;
//		PreparedStatement stmt2 = null;
//		PreparedStatement stmt3 = null;
		PreparedStatement stmt4 = null;

		// change db
		if(!qry.equals(""))
		{// Save changes into Node if any
			// Alter modification time/date
			qry += ((qry.length() > 0)?" ,":"") + "DateModified=NOW()";		
			String big_qry = "UPDATE ent_node SET " + qry + 
					" WHERE NodeID=" + this.getId() + ";";
			// Throw out line feeds
			stmt1 = conn.prepareStatement(SQLManager.stringUnbreak(big_qry));
			stmt1.executeUpdate();
			stmt1.close();
			stmt1 = null;
//			SQLManager.executeUpdate(conn, SQLManager.stringUnbreak(big_qry));
		}// -- end -- Save changes into Node if any
		
		// get user_id when rating is changed
//		int user_id = 0;
//		if((changes & (iNode.NODE_CHANGE_RATING_ADD | iNode.NODE_CHANGE_RATING_EDIT)) > 0)
//		{
//			HttpSession session = request.getSession();
//	//		ResourceMap res_map = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);
//			user_id = ((Integer) session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
//		}
		
		// new rating added
//		if((changes & iNode.NODE_CHANGE_RATING_ADD) > 0)
//		{
//			qry = "INSERT INTO ent_node_rating (NodeID, Rating, " + 
//					"Anonymous, Comment, UserID, DateCreated, " + 
//					"DateModified, DateAltered) VALUES (" + this.getId() + 
//					", " + this.getPersonalRating().getRatingValue() + ", " + 
//					this.getPersonalRating().getAnonymous() + 
//					", '" + SQLManager.stringUnquote(this.getPersonalRating().getComment()) + 
//					"', " + user_id + 
//					", NOW(), NOW(), NOW());";
//			stmt2 = conn.prepareStatement(qry);
//			stmt2.executeUpdate();
//			stmt2.close();
//			stmt2 = null;
////			SQLManager.executeUpdate(conn, qry);
//		}
		
		// rating changing
//		if((changes & iNode.NODE_CHANGE_RATING_EDIT) > 0)
//		{
//			qry = "UPDATE ent_node_rating SET Rating=" + this.getPersonalRating().getRatingValue() +
//			", Anonymous=" + this.getPersonalRating().getAnonymous() + ", Comment='" + 
//			SQLManager.stringUnquote(this.getPersonalRating().getComment())+ "', DateModified=NOW() WHERE " +
//			"NodeID=" + this.getId() + " AND UserID=" + user_id + ";";
//			
//			stmt3 = conn.prepareStatement(qry);
//			stmt3.executeUpdate();
//			stmt3.close();
//			stmt3 = null;
////			SQLManager.executeUpdate(conn, qry);
//		}
		
		// children order/weights changed
		if((changes & iNode.NODE_CHANGE_CHILDREN_WEIGHT_ORDR) > 0)
		{
			for(int i=0; i<this.getChildren().size(); i++)
			{
				qry_children = "UPDATE rel_node_node SET OrderRank = " + (i+1) + 
						", Weight="+ this.getChildren().getWeight(i) + " WHERE ParentNodeID = " + this.getId() + 
						" AND ChildNodeID = " + this.getChildren().get(i).getId() + ";";
				stmt4 = conn.prepareStatement(qry_children);
				stmt4.executeUpdate();
				stmt4.close();
				stmt4 = null;
//				SQLManager.executeUpdate(conn, qry_children);
			}
		}
		
	}// ed of -- saveToDB
	
	public int updateObject(HttpServletRequest request) throws Exception
	{
	//String new_summary = request.getParameter(Summary.SUMMARY_FRMFIELD_SUMMARY);
	//System.out.println("Node: new_summary='" + new_summary + "'");
	
		int changes = iNode.NODE_CHANGE_NONE;
		
		String new_title = request.getParameter(Node.NODE_FRMFIELD_TITLE);
		String new_desc = SQLManager.stringUnnull(request.getParameter(Node.NODE_FRMFIELD_DESCRIPTION));
		String new_uri = SQLManager.stringUnnull(request.getParameter(Node.NODE_FRMFIELD_URI));
		String new_url = SQLManager.stringUnnull(request.getParameter(Node.NODE_FRMFIELD_URL));
		String new_hidden = SQLManager.stringUnnull(request.getParameter(Node.NODE_FRMFIELD_HIDDEN));
//System.out.println("new_uri=" + new_uri);	
//System.out.println("new_url=" + new_url);	
		String frm_weight_order_changed = request.getParameter(NODE_FRMFIELD_WEIGHTORDCHANGED);
		
//		String rate_textue = request.getParameter(NODE_FRMFIELD_RATING_TEXT_VALUE);

		// Title
		if( (new_title != null) && (!this.getTitle().equals(new_title)) )
		{
			this.setTitle(new_title);
			changes |= iNode.NODE_CHANGE_TITLE;
		}
		
		// Description
		if(!this.getDescription().equals(new_desc))
		{
			this.setDescription(new_desc);
			changes |= iNode.NODE_CHANGE_DESCRIPTION;
		}
		
		// URI
		if(!this.getURI().equals(new_uri))
		{
			this.setURI(new_uri);
			changes |= iNode.NODE_CHANGE_URI;
		}
		
		// URL
		if(!this.getURL().equals(new_url))
		{
			this.setURL(new_url);
			changes |= iNode.NODE_CHANGE_URL;
		}
		
		// Hidden
//System.out.println("Node.updateObject new_hidden=" + new_hidden);
		boolean new_hidden_value = NODE_FRMFIELD_HIDDEN_YES.equalsIgnoreCase(new_hidden);
		if(this.isHidden()!=new_hidden_value)
		{
			this.setHidden(new_hidden_value);
			changes |= iNode.NODE_CHANGE_HIDDEN;
		}
		
		if( (frm_weight_order_changed != null) && (frm_weight_order_changed.equals("1")) )
		{// reweight, reorder children
			changes |= iNode.NODE_CHANGE_CHILDREN_WEIGHT_ORDR;
			
			Vector<Integer> indices = new Vector<Integer>();
			Vector<Integer> ids = new Vector<Integer>();//
			Enumeration enu = request.getParameterNames();
			for(;enu.hasMoreElements();)
			{
				String key = (String)enu.nextElement();
				Pattern p = Pattern.compile("doc[0-9]+");
				Matcher m = p.matcher("");
				m.reset(key);
				if(m.matches())
				{
					int item_id = Integer.parseInt( key.substring(3) );
					int item_idx = Integer.parseInt(request.getParameter(key));
					double weight = Double.parseDouble(request.getParameter("weight"+item_id));
	//System.out.println("Reordering Id=" + item_id + " Idx=" + item_idx + " Weight=" + weight);
						indices.add(item_idx-1);
					ids.add(item_id);//
					this.getChildren().getWeights().set((item_idx-1), weight);
				}		
			}
			this.getChildren().reorder(indices, ids);
		}// end of -- reweight, reorder children
		
//		if(rate_textue != null)
//		{// save rating
//	//System.out.println("Node.saveToDB adding/editing rating");		
//			int rateue = Integer.parseInt(request.getParameter(NODE_FRMFIELD_RATING_VALUE));
//			int edit_flag = Integer.parseInt(request.getParameter(NODE_FRMFIELD_RATING_EDIT_FLAG));
//			String anonymous_s = request.getParameter(NODE_FRMFIELD_RATING_ANONYMITY);
//			String comment = request.getParameter(NODE_FRMFIELD_RATING_COMMENT);
//			int anonymous_i = (anonymous_s.equalsIgnoreCase("anonymous"))?1:0;
//			HttpSession session = request.getSession();
//			ResourceMap res_map = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);
//			int user_id = ((Integer) session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
//			User user = res_map.getUsers().findById(user_id);
//			
//			if(edit_flag == 0) // adding new rating
//			{
//				changes |= iNode.NODE_CHANGE_RATING_ADD;
//				
//				Rating new_rating = new Rating(rateue,
//					(anonymous_i==1), comment, user, this);
//				this.setPersonalRating(new_rating);
//				res_map.getRatings().add(new_rating);
//			}
//			else if (edit_flag == 1) // editing existing rating
//			{
//				changes |= iNode.NODE_CHANGE_RATING_EDIT;
//				
//				Rating edited_rating = this.getPersonalRating();
//				edited_rating.setRatingValue(rateue);
//				edited_rating.setAnonymous((anonymous_i==1));
//				edited_rating.setComment(comment);
//			}
//		}// end of -- save rating
	
		return changes;
	}
	
	public int addToDB(Connection conn, HttpServletRequest request, iNode node)
			throws Exception
	{
		int node_id = 0;
		HttpSession session = request.getSession();
		ResourceMap resmap = ((ResourceMap)session.getAttribute(ClientDaemon.SESSION_RES_MAP));
	
		if(this.isStoredInDB())
		{// if object is stored in db
			int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
			
			// save the node
			ResultSet rs = null;
			PreparedStatement stmt1 = null;
			PreparedStatement stmt2 = null;
			PreparedStatement stmt3 = null;
			
			String qry = "INSERT INTO ent_node (Title, Description, URI, URL, UserID, DateCreated, " +
				"DateModified, DateAltered, ItemTypeID, FolderFlag) VALUES ('" + 
				this.getTitle() + "', '" + 
				SQLManager.stringUnnull(this.getDescription()) + "', '" + 
				SQLManager.stringUnnull(this.getURI()) +"', '" + 
				SQLManager.stringUnnull(this.getURL()) + "', " + user_id + ", NOW(), NOW(), NOW(), " + 
				this.getNodeType() + ", " + this.getFolderFlag() + ");";
			// get the id
			try
			{
				// insert stub node
				stmt1 = conn.prepareStatement(qry);
				stmt1.executeUpdate();
				stmt1.close();
				stmt1 = null;
//				SQLManager.executeUpdate(conn, qry);
				
				// get last inserted id
				qry = "SELECT MAX(LAST_INSERT_ID(NodeID)) AS LastID FROM ent_node WHERE UserID=" + user_id + ";";
				stmt2 = conn.prepareStatement(qry);
				rs = stmt2.executeQuery();
///				PreparedStatement statement = conn.prepareStatement(qry);
//				rs = SQLManager.executeStatement(statement);
				while(rs.next())
				{
					node_id = rs.getInt("LastID");
				}
				rs.close();
				rs = null;
				stmt2.close();
				stmt2 = null;
				
				// connect new node to the parent
				qry = "INSERT INTO rel_node_node (ParentNodeID, ChildNodeID, Weight, OrderRank)" +
					" VALUES (" + this.getParent().getId() + ", " + node_id + ", 1, " + (this.getParent().getChildren().size()+1) + ");";
				stmt3 = conn.prepareStatement(qry);
				stmt3.executeUpdate();
				stmt3.close();
				stmt3 = null;
//				SQLManager.executeUpdate(conn, qry);
			}//end -- try
			catch (Exception e) { e.printStackTrace(System.err); }
		}// end of -- if object is stored in db
		else
		{
			node_id = -resmap.getNextVirtualNodeId();
			session.setAttribute(ClientDaemon.SESSION_RES_MAP, resmap);
		}
		
		// re-add the node with correct id
		this.setId(node_id);
		this.resetURI(conn);
		
		this.getParent().getChildren().remove(this);
		this.getParent().getChildren().add(this);
		
		resmap.getNodes().remove(this);
		resmap.getNodes().add(this);
		
		resmap.setPendingNode(null);
		
		session.setAttribute(ClientDaemon.SESSION_RES_MAP, resmap);
		
		return node_id;
	}
	
	/** Returns a default URI for a node
	 * @param node_id current id of the node
	 * @return default URI for a node
	 */
	public String getDefaultURI(int node_id)
	{
		return DEFAULT_URI_PREFIX + node_id;
	}

	/** Resets URI to a predefined pattern, should it be necessary
	 * @param conn Connection
	 */
	public void resetURI(Connection conn) throws SQLException
	{
		this.setURI(DEFAULT_URI_PREFIX + this.getId());
		String qry = "UPDATE ent_node SET URI='" + this.getURI() + "' WHERE NodeID=" + this.getId() + ";";
		PreparedStatement stmt1 = conn.prepareStatement(qry);
		stmt1.executeUpdate();
		stmt1.close();
		stmt1 = null;
//		SQLManager.executeUpdate(conn, qry);
	}
	
	public static void main(String[] args)
	{
		String s = "Mike's lemonade & stuff";
		System.out.println(s.replaceAll("'", "\\\\'"));
		System.out.println(s.replaceAll("&", "&amp;"));
	}
	
	public void performPersonalization(HttpServletRequest request)
	{
		// If not pserviced - exit
		if(this.getNodeType() != iNode.NODE_TYPE_I_PSERVICED_FOLDER)
		{
			System.out.println("!!! [KTree2] SEVERE! Trying to call PService from " + this);
			return;
		}
		
		// get the user_id
		HttpSession session = request.getSession();
		String _session = session.getId().substring(session.getId().length()-5);
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		ResourceMap res_map = (ResourceMap) session.getAttribute(
				ClientDaemon.SESSION_RES_MAP);
		User user = res_map.getUsers().findById(user_id);
		User group = res_map.getUsers().findById(group_id);
		
		
		// REQUEST PService OUTPUT
		long start_ps = System.currentTimeMillis();
		String token = "ktree" + this.getId() + "|" + start_ps + "";
		Model model = ModelFactory.createDefaultModel();
		
		try
		{
	        // Construct data
	        String params = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user.getLogin(), "UTF-8") +
	    		"&" + URLEncoder.encode("group_id", "UTF-8") + "=" + URLEncoder.encode(group.getLogin(), "UTF-8") +
	    		"&" + URLEncoder.encode("session_id", "UTF-8") + "=" + URLEncoder.encode(_session, "UTF-8") +
	    		"&" + URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8") +
	    		"&" + URLEncoder.encode("invtoken", "UTF-8") + "=" + URLEncoder.encode(token, "UTF-8") +
	    		"&" + URLEncoder.encode(SimADAPT2SocialNavigationPService.PSERVICE_PARAM_URI, "UTF-8") + 
	    				"=" + URLEncoder.encode("http://" + request.getServerName() + ((request.getLocalPort() != 80)?":"+ 
	    						request.getLocalPort():"") + request.getContextPath() +
	    						"/rest/ktree" + this.getId(), "UTF-8");
	        // Send data
//	        URL url = new URL("http://adapt2.sis.pitt.edu/pservice/service/sim-adapt2-social/invoke/sim-adapt2-social/vis/sim-adapt-sn-portal-v2");
	        URL url = new URL(this.getURL());
//System.out.println("Pservice at " + this.getURL());	        
	        URLConnection conn = url.openConnection();
	        conn.setDoOutput(true);
	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	        wr.write(params);
	        wr.flush();
	    
	        // Get the response
			InputStream in  = conn.getInputStream();
			
//BufferedReader br = new BufferedReader(new InputStreamReader(in));
//StringBuffer buffer = new StringBuffer();
//String line;
//while ((line = br.readLine()) != null) {
//  buffer.append(line);
//}
			model.read(in, "");
			in.close();
	        wr.close();
		}
		catch(UnsupportedEncodingException uee) { uee.printStackTrace(System.out); }
		catch(IOException ioe) { ioe.printStackTrace(System.out); };
		// end of -- REQUEST PService OUTPUT
		long finish_ps = System.currentTimeMillis();
		
		// Save tracking to DB
		String qry = "INSERT INTO ent_pservice_call_log (Token,UserGroup,StartTS,FinishTS,Delay,PortalURI) VALUES(" +
				"'" + token + "', '" + user.getLogin() + ":" + group.getLogin() + "', " + start_ps + ", " + 
				finish_ps + ", " + (finish_ps - start_ps) + ", '" + this.getURI() + "');";
		
		SQLManager _sqlm = ClientDaemon.getInstance().getSQLManager();
		Connection conn = null;
		PreparedStatement stmt1 = null;

		try
		{// save to db
			conn = _sqlm.getConnection();
			stmt1 = conn.prepareStatement(qry);
			stmt1.executeUpdate();
			stmt1.close();
			stmt1 = null;
			conn.close();
			conn = null;
//			SQLManager.executeUpdate(conn, qry);
		}// end of -- save to db
		catch(SQLException sqle)
		{
			sqle.printStackTrace(System.out);
		}
		finally
		{
			if (stmt1 != null) 
			{
				try { stmt1.close(); } catch (SQLException e) { ; }
				stmt1 = null;
			}
			if (conn != null)
			{
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
			
		}
		
		// save pservice model in the node - old way
//		this.setPersonalizedModel(model);
		
		// save results of peersonalization in the nodes and create the augmented view if necessary
		ResIterator channels = model.listSubjectsWithProperty(RDF.type, RSS.channel);
		Resource channel = null;
		if (channels.hasNext())
			channel = (Resource)channels.next();
		
		if (channel != null && channel.hasProperty(RSS.items))
		{// for channels
			// save channel header
			Statement channel_prop = channel.getProperty(DC.description);
			String parent_html = (channel_prop!=null)?channel_prop.getString():"";
			
			// load new channel wide stuff
			AnnotationItem parent_anno_pservice = this.getPrefixes().findByTitle(ANNOT_ADAPT_PSERVICE);
			if(parent_anno_pservice==null)
			{
				parent_anno_pservice = new AnnotationItem(ANNOT_ADAPT_PSERVICE,parent_html);
				this.getPrefixes().add(parent_anno_pservice);
			}
			else
				parent_anno_pservice.setAnnotation(parent_html);
			
//System.out.println("---\n" + parent_html + "\n---\n");

			Item2Vector<iNode> augmented_children = new OrderedWeightedItem2Vector();
			boolean anything_different = true;
			// save annotations
			Seq items = channel.getProperty(RSS.items).getSeq();
			for (int i=1; i<= items.size(); i++)
			{// for all channel items
				Resource res_item = items.getResource(i);
				if(res_item==null) continue;
				String res_title =  res_item.getProperty(RSS.title).getString();
//System.out.println("-- title=" + res_title);				
				// read item properties
				Statement annot_prop = res_item.getProperty(DC.description);
				String annot_html = (annot_prop!=null)?annot_prop.getString():"";
//System.out.println("-- annot=" + annot_html);				
//System.out.println("-- annot=" + annot_prop.toString());				
				String res_url =  res_item.getProperty(RSS.link).getString();
				
				Statement style_prop = res_item.getProperty(DC.format);
				String style_html = (style_prop!=null)?style_prop.getString():"";
				
				Statement res_relation = res_item.getProperty(DC.relation);
				String res_s_relation = (res_relation!=null)?res_relation.getString():"";
//System.out.println("recommended=" + res_relation + "\n");						
				
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
				
				// load item properties or create a fresh augmented node
				iNode child = res_map.getNodes().findById(node_id);
				if(child==null)
				{
					anything_different = true;
					Random rand = new Random();
					int id_rand = rand.nextInt();

					iNode dummy_node = 	NodeFactory(id_rand, res_title, res_url, iNode.NODE_TYPE_I_UNTYPDOC, "" /*desc*/,
							res_url, false /*folder_flag*/, null /*icon*/, false /*hidden*/);
					
//					augmented_children.add(dummy_node);
					child = dummy_node;
				}
				
				{
					AnnotationItem anno_pservice = child.getPrefixes().findByTitle(ANNOT_ADAPT_PSERVICE);
					if(anno_pservice==null)
					{
						anno_pservice = new AnnotationItem(ANNOT_ADAPT_PSERVICE,annot_html,style_html);
						child.getPrefixes().add(anno_pservice);
					}
					else
					{
						anno_pservice.setAnnotation(annot_html);
						anno_pservice.setFormat(style_html);
					}
//System.out.println("recommended~" + res_s_relation + "\n");						
					anno_pservice.setgetPopUp((res_s_relation.equals("recommended")?true:false));
					
					child.setSTag(res_url);
					
					augmented_children.add(child);
				}

			}// end of -- for all channel items
			
			if(anything_different)
				session.setAttribute(ClientDaemon.SESSION_AUGMENTED_VIEW, augmented_children);
			
		}// end of -- for channels
		
	}
	
	public ItemVector<AnnotationItem> getPrefixes() { return prefixes; }
	public ItemVector<AnnotationItem> getSuffixes() { return suffixes; }
	
	public void addNoteIconsToChildren(HttpServletRequest request, int user_id, int group_id)
	{
		// query for knowledge
		Item2Vector<iItem2> note_details = new Item2Vector<iItem2>();
		note_details.addAll(this.getChildren());

		try
		{
			URL url = new URL("http://" + request.getServerName() + 
					((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath() +
					"/notebatch?" + NoteBatch.REQ_USER + "=" + user_id + "&" + NoteBatch.REQ_GROUP + "=" + group_id + "&" +
					NoteBatch.REQ_FORMAT + "=" + NoteBatch.REQ_FORMAT_JAVA_OBJ + "&" +
					NoteBatch.REQ_URI + "=" + URLEncoder.encode(this.getURI(), "UTF-8"));
//System.out.println("http://" + request.getServerName() + 
//					((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath() +
//					"/notebatch?" + NoteBatch.REQ_USER + "=" + user_id + "&" + NoteBatch.REQ_GROUP + "=" + group_id + "&" +
//					NoteBatch.REQ_FORMAT + "=" + NoteBatch.REQ_FORMAT_JAVA_OBJ + "&" +
//					NoteBatch.REQ_URI + "=" + URLEncoder.encode(this.getURI(), "UTF-8"));
			
			
			URLConnection conn = url.openConnection();
			conn.setUseCaches(false);
			conn.setDefaultUseCaches(false);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("Content-Type","java-internal/" + note_details.getClass().getName());
			ObjectOutputStream oo = new ObjectOutputStream(conn.getOutputStream());
			oo.writeObject(note_details);
			oo.flush();
			oo.close();

			ObjectInputStream ii = new ObjectInputStream(conn.getInputStream());
			note_details =  (Item2Vector<iItem2>)ii.readObject();
			ii.close();
			ii = null;
			
			/*
		URLConnection dbpc = (new URL(report_url)).openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
			dbpc.getInputStream()));
		in.close();
			 */
			
		}
		catch(IOException ioe) { ioe.printStackTrace(System.out); }
		catch(ClassNotFoundException cnfe) { cnfe.printStackTrace(System.out); }

//System.out.println("------");		
//for(int i=0; note_details!=null && i<note_details.size(); i++)
//{
//	System.out.println(i + " this.getId() = " + note_details.get(i).getId() + " this.getURI() = " + note_details.get(i).getURI());
//}
		
		// upload
		for(int i=0; note_details!=null && i<note_details.size(); i++)
		{
			iItem2 note = note_details.get(i);
			iNode child = this.getChildren().findByURI(note.getURI());
			if(child==null)
				System.out.println("!!! [KTree] SEVERE Received note for non-existing child with URI " + note.getURI());

			AnnotationItem annot = child.getSuffixes().findByTitle(Node.ANNOT_KT_NOTE);
			if(annot==null)
			{
				annot = new AnnotationItem(Node.ANNOT_KT_NOTE,"");
				child.getSuffixes().add(annot);
			}
			annot.setAnnotation(note.getSTag());
			
//System.out.println(child.getSuffixes().findByTitle(Node.ANNOT_KT_NOTE));			
		}
		
	}

}
