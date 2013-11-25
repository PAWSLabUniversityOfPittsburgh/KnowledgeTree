<%@ page
	contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java"
	import="java.io.*, java.util.*,edu.pitt.sis.paws.kt2.*,edu.pitt.sis.paws.core.*"
	errorPage=""
%>
<!-- uri="http://java.sun.com/jsp/jstl/core" tagdir="/WEB-INF/tags/c.tld" -->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Untitled Document</title>
<link rel='stylesheet' href="<%=request.getContextPath()%>/assets/KnowledgeTree.css" type="text/css"/>

<script src="<%=request.getContextPath()%>/assets/prototype.js"></script>
<script language='javascript' type="text/javascript">
<!--
	var xmlhttp = false;
	var g_objWarningTimer;
	var g_dttmSessionExpires;

	function leftFrameHideUnhide(flag)
	{
		bgmd = Math.round(Math.random()*1000);
		url = new String("<%=request.getContextPath()%>/ajax_robot");
		url = url.concat("?BGMD=", bgmd, "&<%=AjaxRobot.REQUEST_HIDE_LEFT_FRAME%>=", flag);
		try
		{
			xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
			xmlhttp.open("GET", url, true);
			xmlhttp.send();
		}
		catch (e)
		{
			try
			{
				xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
				xmlhttp.open("GET", url, true);
				xmlhttp.send();
			}
			catch (E) { xmlhttp = false; }
		}
		
		if (!xmlhttp && typeof XMLHttpRequest!='undefined')
		{
			try
			{
				xmlhttp = new XMLHttpRequest();
				xmlhttp.open("GET", url, true);
				xmlhttp.send(null);
			}
			catch (e) { xmlhttp=false; }
		}
		
		if (!xmlhttp && window.createRequest)
		{
			try
			{
				xmlhttp = window.createRequest();
				xmlhttp.open("GET", url, true);
				xmlhttp.send(null);
			}
			catch (e) { xmlhttp=false; }
		}
	}
	
	function leftFrameChange(param)
	{
		if(param==-1)
		{
			document.getElementById('leftFrameControl').innerHTML =
				"\n\t<a href='#' onClick='leftFrameChange(1);'><img src='<%=request.getContextPath()%>/assets/folders.gif' width='16' height='16' border='0' alt='[&gt;]' title='Show folder hierarchy' /></a>\n";
			//top.document.getElementById('topFrameSet').cols = '0,*';
			top.document.getElementById('innerFrameSet').cols = '0,*';
			top.document.getElementById('leftFrame').noResize = true;
			// send AJAX message to hide left frame
			leftFrameHideUnhide('1');
		}
		else if(param==1)
		{
			document.getElementById('leftFrameControl').innerHTML = 
				"\n\t<a href='#' onClick='leftFrameChange(-1);'><img src='<%=request.getContextPath()%>/assets/folders.gif' width='16' height='16' border='0' alt='[&lt;]' title='Hide folder hierarchy' /></a>\n";
			//top.document.getElementById('topFrameSet').cols = '250,*';
			top.document.getElementById('innerFrameSet').cols = '250,*';
			top.document.getElementById('leftFrame').	noResize = false;

			leftFrameHideUnhide('-1');
		}
	}
	

-->
</script>
<%!
	public ClientDaemon cd;
	public ResourceMap res_map;
	public iNode current_node;
	public int user_id;
	public String user_name;
	public int group_id;
	public String group_name;
	public ItemVector<Item> groups;
	public String up_dir_icon = "";
	public String up_dir_href = "";
	public String edit_icon = "";
	public String edit_href = "";
	public String add_icon = "";
	public String add_href = "";
	public String copy_icon = "";
	public String copy_href = "";
	public String del_icon = "";
	public String del_href = "";
	public String move_icon = "";
	public String move_href = "";
	public boolean hide_left_frame;
%>

<%
	cd = ClientDaemon.getInstance();
	res_map = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);
//System.out.println("jspTop.doGet (res_map==null)=" + (res_map==null));	
	current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
	user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
	user_name = (String)session.getAttribute(ClientDaemon.SESSION_USER_NAME);
	group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
	groups = (ItemVector<Item>)session.getAttribute(ClientDaemon.SESSION_GROUPS);
	group_name = groups.findById(group_id).getTitle();
	
	// get a copy of current node from a map
	iNode c_node = null;
	if(current_node != null)
		c_node = res_map.getNodes().findById(current_node.getId());
	// deal with 'up-dir' icon
	boolean no_way_up = (c_node == null || c_node.getId()==1)?true:false;
	
	int parent_node_id = 0;
	if( (c_node != null) && (c_node.getParent() != null) ) 
		parent_node_id = c_node.getParent().getId();
	
	// DECIDE MODES
	boolean can_edit = false;
	boolean is_in_edit_mode = false;

	boolean can_add = false;
	boolean is_in_add_mode = false;

	boolean can_del = false;
	boolean is_in_del_mode = false;

	boolean can_copy = false;
	boolean is_in_copy_mode = false;

	boolean can_move = false;
	boolean is_in_move_mode = false;
	
	boolean can_rate = false;

	Integer show_mode_i = (Integer)session.getAttribute(ClientDaemon.SESSION_SHOW_MODE);
	int show_mode = (show_mode_i == null) ? Show.SHOW_MODE_VIEW : 
		show_mode_i.intValue();
	is_in_edit_mode = (show_mode == Show.SHOW_MODE_EDIT);
	is_in_del_mode = (show_mode == Show.SHOW_MODE_DELETE);
	is_in_add_mode = (show_mode == Show.SHOW_MODE_ADD);
	is_in_copy_mode = (show_mode == Show.SHOW_MODE_COPY);
	is_in_move_mode = (show_mode == Show.SHOW_MODE_MOVE);
	
	// decide whether to show up dir by type, so far - default
	if( !no_way_up && (!is_in_edit_mode) && (!is_in_add_mode) && 
		(!is_in_copy_mode) && (!is_in_del_mode) && (!is_in_move_mode))
	{
		up_dir_icon = "up_dir2_enable.gif";
		up_dir_href = " href='" + request.getContextPath() + "/content" + 
	"/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + parent_node_id + "' " +
	"target='_top'";
	}
	else
	{
		up_dir_icon = "up_dir2_disable.gif";
		up_dir_href = "";
	}

	// Deciding CAN_EDIT
	if( (c_node != null) && /*(c_node.getRights().size() > 0) && */
		(!is_in_edit_mode) && (!is_in_add_mode) && (!is_in_copy_mode)
		&& (!is_in_del_mode) && (!is_in_move_mode))
	{// inspect all the rights
		int parent_n_type = (c_node.getParent() == null) ? 
	iNode.NODE_TYPE_I_ALL : c_node.getParent().getNodeType();
		
		if( (c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
	(c_node.getNodeType() != iNode.NODE_TYPE_I_BIN)	)
	can_edit = (
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_EDIT, user_id, c_node, 
			parent_n_type, c_node.getNodeType())
		||
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_EDIT, group_id, c_node, 
			parent_n_type, c_node.getNodeType())
		);
		else
	can_edit = true; // my profile can always be edited
//System.out.println("top.jsp Check for 'can edit' ---- over");
	}
	

	// Deciding CAN_DEL
	if( (c_node != null) && /*(c_node.getRights().size() > 0) && */
		(!is_in_edit_mode) && (!is_in_add_mode) && (!is_in_copy_mode)
		&& (!is_in_del_mode) && (!is_in_move_mode))
	{// inspect all the rights
		int parent_n_type = (c_node.getParent() == null) ? 
	iNode.NODE_TYPE_I_ALL : c_node.getParent().getNodeType();
		
		if( (c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
	(c_node.getNodeType() != iNode.NODE_TYPE_I_BIN) )
	can_del = 
		(		
			(
			res_map.isAllowedWhatWhoForFromTo(
		Right.RIGHT_TYPE_DELETE, user_id, c_node, 
		parent_n_type, c_node.getNodeType())
			||
			res_map.isAllowedWhatWhoForFromTo(
		Right.RIGHT_TYPE_DELETE, group_id, c_node, 
		parent_n_type, c_node.getNodeType())
			)
		) && //(((Node)c_node).getChildren().size()==0)
		// downwards
		(		
			(
			res_map.isAllowedWhatWhoForFromTo_DownInhibitory(
		Right.RIGHT_TYPE_DELETE, user_id, c_node, 
		parent_n_type, c_node.getNodeType())
			||
			res_map.isAllowedWhatWhoForFromTo_DownInhibitory(
		Right.RIGHT_TYPE_DELETE, group_id, c_node, 
		parent_n_type, c_node.getNodeType())
			)
		)
		;
//System.out.println("top.jsp Check for 'can edit' ---- over");
	}
	

	// Deciding CAN_ADD
	if( (c_node != null) && /*(c_node.getRights().size() > 0) && */
		/*(c_node.getFolderFlag()) && */
		(!is_in_add_mode) && (!is_in_edit_mode) && (!is_in_copy_mode)
		&& (!is_in_del_mode) && (!is_in_move_mode))
	{// inspect all the rights
		can_add = false;
//		System.out.println("top No of Type="+iNode.NODE_TYPES_I_DEFINITIVE.length);
		for(int i=0; i<iNode.NODE_TYPES_I_DEFINITIVE.length;i++)
		{
	if(
		(c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
		(c_node.getNodeType() != iNode.NODE_TYPE_I_BIN) &&
		res_map.isAllowedWhatWho2ForFromToQuant(Right.RIGHT_TYPE_ADD,
			user_id, group_id, c_node, c_node.getNodeType(), 
			iNode.NODE_TYPES_I_DEFINITIVE[i])
	)
		can_add = true;
		}/**/
	}
	
	// Deciding CAN_COPY
/*	if( (c_node != null) && 
		(!is_in_add_mode) && (!is_in_edit_mode) && (!is_in_copy_mode)
		&& (!is_in_del_mode) && (!is_in_move_mode))
	{// inspect all the rights
	
		for(int i=0; i<iNode.NODE_TYPES_I_DEFINITIVE.length;i++)
		{
	if(	
		(c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
		(c_node.getNodeType() != iNode.NODE_TYPE_I_BIN) &&
		res_map.isAllowedWhatWho2ForFromToQuant(Right.RIGHT_TYPE_COPY,
			user_id, group_id, c_node, c_node.getNodeType(), 
			iNode.NODE_TYPES_I_DEFINITIVE[i])
	)
		can_copy = true;
		}
	}/**/		
	
	// Deciding CAN_MOVE = CAN_DEL + CAN_COPY
	can_move = can_del && can_copy;

	// Deciding CAN_RATE
	if( (c_node != null) && /*(c_node.getRights().size() > 0) && */
		(!is_in_edit_mode) && (!is_in_add_mode) && (!is_in_copy_mode)
		&& (!is_in_del_mode) && (!is_in_move_mode))
	{// inspect all the rights
		int parent_n_type = (c_node.getParent() == null) ? 
	iNode.NODE_TYPE_I_ALL : c_node.getParent().getNodeType();
		
		if( (c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
	(c_node.getNodeType() != iNode.NODE_TYPE_I_BIN)	)
	can_rate = (
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_RATE, user_id, c_node, 
			parent_n_type, c_node.getNodeType())
		||
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_RATE, group_id, c_node, 
			parent_n_type, c_node.getNodeType())
		);
		else
	can_rate = true; // my profile & bin can always be edited
//System.out.println("top.jsp Check for 'can edit' ---- over");
	}

	
	String base_href = "";
	if(c_node != null)
		base_href = " href='" + request.getContextPath() + "/content" + 
	"/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + c_node.getId() + "&" + 
	ClientDaemon.REQUEST_SHOW_MODE + "=";
	
	if( can_edit)
	{
		edit_icon = "edit_enable.gif";
		edit_href = base_href + Show.SHOW_MODE_EDIT + "' target='_top'";
	}
	else
	{
		edit_icon = "edit_disable.gif";
		edit_href = "";
	}

	if( can_add )
	{
		add_icon = "add_enable.gif";
		add_href = base_href + Show.SHOW_MODE_ADD + "' target='_top'";
	}
	else
	{
		add_icon = "add_disable.gif";
		add_href = "";
	}

	if( can_copy )
	{
		copy_icon = "copy_enable.gif";
		copy_href = base_href + Show.SHOW_MODE_COPY + "' target='_top'";
	}
	else
	{
		copy_icon = "copy_disable.gif";
		copy_href = "";
	}

	if( can_del )
	{
		del_icon = "delete_enable.gif";
		del_href = base_href + Show.SHOW_MODE_DELETE + "' target='_top'";
	}
	else
	{
		del_icon = "delete_disable.gif";
		del_href = "";
	}
	
	if( can_move )
	{
		move_icon = "move_enable.gif";
		move_href = base_href + Show.SHOW_MODE_MOVE + "' target='_top'";
	}
	else
	{
		move_icon = "move_disable.gif";
		move_href = "";
	}
	
	Object o_lframe = session.getAttribute(ClientDaemon.SESSION_HIDE_LEFT_FRAME);
	hide_left_frame = (o_lframe != null);
%>

<script language='javascript' type="text/javascript">

<!-- 
function openNote(uid, gid, uri)
{
	var params = $H({u:uid, g:gid,uri:uri});

	var au = new Ajax.Updater(
		'kt-notes', 
		'<%=request.getContextPath()%>/notes', 
		{ 
			method: 'post',
			parameters: params,
			onSuccess:function(response){ 
				Tip(response.responseText, CLOSEBTN, true, TITLE, 'Notes', CLOSEBTNTEXT, 'close', CLOSEBTNCOLORS, ['#3366CC', '#E8EEF7', '#E8EEF7', '#3366CC'], STICKY, true, HEIGHT, 300, EXCLUSIVE, true, BGCOLOR,'#E8EEF7',BORDERCOLOR,'#3366CC', BORDERSTYLE,'dotted',WIDTH,400,TEXTALIGN,'justify');
			}.bind(this)
		} 
	);
}
 -->
</script>

<style>
table, td, tr
{
	padding:0px;
	margin:0px;
}
a, a:visited, a:link, a:active { text-decoration:none; }
a:hover { text-decoration:underline; }
/*User name, reload, logout, etc.*/ 
.session_control 
{
	font-size:14px;
	font-family:'Times New Roman', Times, serif;
	color:black;
	text-decoration:none;
	padding:0px;
	margin:0px;
}
a.session_control:link, a.session_control:active, a.session_control:visited
{
	color:#0000cc;
}

.menu
{
	font-size:14px;
	color:#3366cc;
	text-decoration:none;
	padding:0px;
	margin:0px;
}
.menu_img
{
	vertical-align:text-top;
}
a.menu:link, a.menu:active, a.menu:visited
{
	color:#3366cc;
}
.menu:hover
{
	color:#0066CC;
}
</style>
</head>

<body marginwidth="0" marginheight="0" style="font-family:Times, serif;">
<script src="<%=request.getContextPath()%>/assets/wz_tooltip.js"></script>

<div id="session_control" style="padding:1px; margin:1px;" align="right">
	<!-- User and group name -->
	<strong class="session_control"><%=user_name%></strong>&nbsp;|&nbsp;<span class="session_control"><%=group_name%></span>
	<!-- Switch group -->
	<!-- 
	<a class="session_control" href="#"><img title='Change group' style="vertical-align:text-bottom;" src="<%=request.getContextPath()%>/assets/drop_down.gif" width='16' height='16' border='0' onmouseover="src='<%=request.getContextPath()%>/assets/drop_down_hover.gif';" onmouseout="src='<%=request.getContextPath()%>/assets/drop_down.gif';"/></a>
 	-->
</div>
<table id="top_menu" width="100%" border="0" cellpadding="0" cellspacing="0" style="padding:0px;margin:0px;border-bottom:1px solid #3366CC;">
	<tr>
		<!-- Left Frame control -->
		<!-- 		
		<td id="leftFrameControl"><a href="#" onClick='leftFrameChange(<%=(hide_left_frame)?"1":"-1"%>);'><img title='<%=(hide_left_frame)?"Show":"Hide"%> folder hierarchy' style="vertical-align:text-bottom;" src="<%=request.getContextPath()%>/assets/folders.gif" width='16' height='16' border='0' onmouseover="src='<%=request.getContextPath()%>/assets/folders_hover.gif';" onmouseout="src='<%=request.getContextPath()%>/assets/folders.gif';"/></a></td>
		<td>&nbsp;&nbsp;</td>
		-->
		
		<!-- Up one folder -->
		<td><a <%=up_dir_href%>><img alt='[Up]' title='Up one directory' style="vertical-align:text-bottom;" src="<%=request.getContextPath()%>/assets/updir.gif" width='16' height='16' border='0' onmouseover="src='<%=request.getContextPath()%>/assets/updir_hover.gif';" onmouseout="src='<%=request.getContextPath()%>/assets/updir.gif';"/></a></td>
		
		<td>&nbsp;&nbsp;</td>
		<!-- Add -->
		<%
		if(can_add)
		{
			out.println("<td><a " + add_href + "><img style='vertical-align:text-bottom;' src=\"" + request.getContextPath() + "/assets/add.gif\" width='16' height='16' border='0' alt='[Add]' title='Add new document/folder' onmouseover=\"src='" + request.getContextPath() + "/assets/add_hover.gif';\" onmouseout=\"src='" + request.getContextPath() + "/assets/add.gif';\"/></a></td>");
		}
		%>
		<!-- Delete -->
		<%
		if(can_del)
		{
			out.println("<td><a" + del_href + "><img style='vertical-align:text-bottom;' src='" + request.getContextPath() + "/assets/delete.gif' width='16' height='16' border='0' alt='[Delete]' title='Delete document/folder' onmouseover=\"src='" + request.getContextPath() + "/assets/delete_hover.gif';\" onmouseout=\"src='" + request.getContextPath()+ "/assets/delete.gif';\"/></a></td>");
		}
		%>
		
 		<!-- Edit -->
		<%
		if(can_edit)
		{
			out.println("<td><a" + edit_href + "><img style='vertical-align:text-bottom;' src='" + request.getContextPath() + "/assets/edit.gif' width='16' height='16' border='0' alt='[Edit]' title='Edit document/folder' onmouseover=\"src='" + request.getContextPath() + "/assets/edit_hover.gif';\" onmouseout=\"src='" + request.getContextPath()+ "/assets/edit.gif';\"/></a></td>");
		}
		%>
		
		
		<td><a id='note-btn' href='javascript:;' target='_top' onClick='parent.openNote(<%=user_id%>,<%=group_id%>,"<%=c_node.getURI()%>");'><img style='vertical-align:text-bottom;' src='<%=request.getContextPath()%>/assets/icons/notes_menu.gif' width='16' height='16' border='0' alt='[Note]' title='Write Notes' onmouseover="src='<%=request.getContextPath()%>/assets/icons/notes_menu_hover.gif';" onmouseout="src='<%=request.getContextPath()%>/assets/icons/notes_menu.gif';"/></a></td>
		
		<!-- Bookmarks -->
		<!-- 
		<td><a href="#"><img title='Star' style="vertical-align:text-bottom;" src="<%=request.getContextPath()%>/assets/star_menu.gif" width='24' height='16' border='0' onmouseover="src='<%=request.getContextPath()%>/assets/star_menu_hover.gif';" onmouseout="src='<%=request.getContextPath()%>/assets/star_menu.gif';"/></a></td>
		-->
		
		<td width="100%" id='kt-fudge'>&nbsp;</td>
		
		<!-- Search field and button-->
		<!-- 
		<td><input type="text" size="20" maxlength="50" style="margin:0px; padding:0px;"/></td>
		<td><a href="#"><img title='Search' style="vertical-align:text-bottom;" src="<%=request.getContextPath()%>/assets/search.gif" width='16' height='16' border='0' onmouseover="src='<%=request.getContextPath()%>/assets/search_hover.gif';" onmouseout="src='<%=request.getContextPath()%>/assets/search.gif';"/></a></td>
		-->
		
		<td>&emsp;</td>
		
		<!-- Help link -->
		<!-- 
		<td><a href="#" class="session_control">Help</a></td>
		<td>|</td>
		 -->
		 
		 <!-- Reload -->
		<td><a class='session_control' href='<%=request.getContextPath() + "/content/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + res_map.getRootNode().getId() + "&" + Show.SHOW_FLAG_RELOAD %>=1' target = "_top">Reload</a></td>
		<td>&nbsp;|&nbsp;</td>
		
		<!-- Logout -->
		<td><a href='<%=request.getContextPath() + "/content/doAuthenticate?logoff=1"%>' target="_top" class="session_control">Logout</a>&nbsp;</td>
	</tr>
	<tr>
		<!-- Breadcrumbs -->
		<td colspan="18" class="menu"><%=res_map.getBreadCrumbs(c_node, request.getContextPath())%></td>
		<!--  
		<td colspan="18" class="menu"><a href="#" class="menu"><img class="menu_img" title="Home" src="<%=request.getContextPath()%>/assets/home.gif" width='16' height='16' border='0' onmouseover="src='<%=request.getContextPath()%>/assets/home_hover.gif';" onmouseout="src='<%=request.getContextPath()%>/assets/home.gif';"/></a>&zwj;<img class="menu_img" src="<%=request.getContextPath()%>/assets/breadcrumb_spacer.gif" width="12" height="16"/>&zwnj;<a href="#" class="menu">IS 2470 Interactive Systems Design</a>&zwj;<img class="menu_img" src="<%=request.getContextPath()%>/assets/breadcrumb_spacer.gif" width="12" height="16"/>&zwnj;<a href="#" class="menu">Lecture 2</a>&zwj;<img class="menu_img" src="<%=request.getContextPath()%>/assets/breadcrumb_spacer.gif" width="12" height="16"/>&zwnj;<a href="#" class="menu">Lecture 2 recording via virtPresenter</a></td>
		-->
	</tr>
	<tr>
		<td colspan="18" bgcolor="#E8EEF7" align="center" style="padding:0px;margin:0px;">
			<!-- Bookmark status -->
			<!-- 
			<img title='Star' style="vertical-align:text-bottom;" src="<%=request.getContextPath()%>/assets/star_off.gif" width='16' height='16' border='0'/>&nbsp;
			<img title='Star' style="vertical-align:text-bottom;" src="<%=request.getContextPath()%>/assets/star_on.gif" width='16' height='16' border='0'/>&nbsp;
			 -->
			<strong style="padding:1px;"><%= c_node.getTitle() %></strong>
		</td>
	</tr>
</table>

<div id="kt-notes" style="display:none;">
</div>

</body>
</html>
