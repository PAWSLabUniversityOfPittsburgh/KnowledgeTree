<%@ page 
	contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java"
	import="edu.pitt.sis.paws.kt2.*,edu.pitt.sis.paws.core.*"
	errorPage=""
%>

<%!
	public static final int SHOW_MODE_VIEW = 1;  // also when parameter is null
	public static final int SHOW_MODE_EDIT = 2;
	public static final int SHOW_MODE_ADD = 3;
	public static final int SHOW_MODE_COPY = 4;
	public static final int SHOW_MODE_DELETE = 5;
	public static final int SHOW_MODE_MOVE = 6;
	public static final int SHOW_MODE_ADD_RATING = 7;
	public static final String SHOW_FLAG_RELOAD = "reload";

	iNode current_node = null;
	String node_id_s = null;
	String mode_s = null;
	String svc = null;
	ClientDaemon cd = null;
	ResourceMap res_map = null;
	String reload = null;
	int user_id = 0;
	int group_id = 0;
	int node_id = 0;
	String title = null;
	public String user_name;
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
	Integer current_mode = null;
%>

<%

if(!ClientDaemon.isSessionInited(request.getSession(false)))
{
	ClientDaemon.forwardToURL(request,response, "/content/doAuthenticate");
	return;
}

		cd = ClientDaemon.getInstance();
		current_mode = (Integer)session.getAttribute(ClientDaemon.SESSION_SHOW_MODE);
		current_node = (iNode)session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
String	expand = request.getParameter(ClientDaemon.REQUEST_EXPANDPATH);
		group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		
		groups = (ItemVector<Item>)session.getAttribute(ClientDaemon.SESSION_GROUPS);

		group_name = groups.findById(group_id).getTitle();
		mode_s = request.getParameter(ClientDaemon.REQUEST_SHOW_MODE);
		node_id_s = request.getParameter(ClientDaemon.REQUEST_NODE_ID);
		reload = request.getParameter(SHOW_FLAG_RELOAD);
		res_map = (ResourceMap)session.getAttribute(ClientDaemon.SESSION_RES_MAP);
		svc = request.getParameter("svc");
		user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		user_name = (String)session.getAttribute(ClientDaemon.SESSION_USER_NAME);

if(svc != null && svc.length() > 0)
	session.setAttribute("svc", svc);


if(reload != null)
{
	// get user id and group id
	res_map = new ResourceMap(user_id, group_id, cd.getSQLManager());
	session.setAttribute(ClientDaemon.SESSION_RES_MAP, res_map);

	// AND SET NODE TO ROOT or only child of root
	node_id_s = null;
	current_node = null;
}

if(node_id_s != null) // new node is specified
{
	node_id = Integer.parseInt(node_id_s);
	
	if((current_node == null) || (node_id != current_node.getId()) )
	{
		current_node = res_map.getNodes().findById(node_id);
		session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
	}
}
else if(current_node != null) // old node is specified
	node_id = current_node.getId();
else // root node is meant
{
	// if root has one sub-node, choose sub
	if(res_map.getRootNode().getChildren().size() == 1)
	{
		node_id = res_map.getRootNode().getChildren().get(0).getId();
		current_node = res_map.getRootNode().getChildren().get(0);
	}
	else
	{
		node_id = res_map.getRootNode().getId();
		current_node = res_map.getRootNode();
	}
	session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
}

if(current_node == null) // node was not found -- set root
{
	node_id = res_map.getRootNode().getId();
	current_node = res_map.getRootNode();
	session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
}

title = current_node.getTitle();

// Load current node if necessary
if(!current_node.isFullyLoaded())
{
//	// clocking
//	Calendar start = null;
//	Calendar finish = null;
//	long diff_mills;
//	start = new GregorianCalendar();
//	
//	System.out.print("+++ [KTree2] Nodes uploaded in " );
	res_map.FullyLoad(current_node, cd.getSQLManager());
	session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
//
//	finish = new GregorianCalendar();
//	diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
//	System.out.println(diff_mills + "ms, heap size " + Runtime.getRuntime().freeMemory());
}

// Check for modes (view/edit/...)
//	first delete old mode
session.removeAttribute(ClientDaemon.SESSION_SHOW_MODE);
int mode = 0;

if(mode_s != null)
{
	mode = Integer.parseInt(mode_s);
	// .. and load mode to the session
	session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,mode);
	switch(mode)
	{
		case Show.SHOW_MODE_EDIT:
			session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
				Show.SHOW_MODE_EDIT);
		break;
	      case Show.SHOW_MODE_ADD:
	            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
	                  Show.SHOW_MODE_ADD);
	      break;
	      case Show.SHOW_MODE_COPY:
	            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
	                  Show.SHOW_MODE_COPY);
	      break;
	      case Show.SHOW_MODE_DELETE:
	            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
	                  Show.SHOW_MODE_DELETE);
	      break;
	      case Show.SHOW_MODE_MOVE:
	            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
	                  Show.SHOW_MODE_MOVE);
	      break;
	      case Show.SHOW_MODE_ADD_RATING:
	            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
	                  Show.SHOW_MODE_ADD_RATING);
	      break;
	}
}



// Check for "hide left frame flag"

// if there's a node pending and we're not editing it - recycle
if( (res_map.getPendingNode()!= null) && (mode != Show.SHOW_MODE_EDIT) )
{
	res_map.recycleNode(res_map.getPendingNode());
	session.setAttribute(ClientDaemon.SESSION_RES_MAP, res_map);
	
	if(current_node.getId() == 0)
	{
		current_node = current_node.getParent();
		session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
	}
}

// Make sure the path to the node is expanded if necessary
if(expand != null)
{
	if(res_map.getNodes().findById(current_node.getId()).
		expandParents(false))
		session.setAttribute(ClientDaemon.SESSION_RES_MAP,res_map);
}

// now let's call pservice if necessary
// if viewing a PServiced node - rerequest pservice
if((current_mode == null || current_mode.intValue() == Show.SHOW_MODE_VIEW) &&
		current_node.getNodeType() == iNode.NODE_TYPE_I_PSERVICED_FOLDER)
{
	current_node.performPersonalization(request);
}

boolean no_way_up = (current_node == null || current_node.getId()==1)?true:false;

int parent_node_id = 0;
if( (current_node != null) && (current_node.getParent() != null) ) 
	parent_node_id = current_node.getParent().getId();

// DECIDE MODES
boolean can_edit = false;
boolean can_add = false;
boolean can_del = false;
boolean can_copy = false;
boolean can_move = false;
boolean can_rate = false;


int show_mode = (current_mode == null) ? Show.SHOW_MODE_VIEW :  current_mode.intValue();
boolean is_in_edit_mode = (show_mode == Show.SHOW_MODE_EDIT);
boolean is_in_del_mode = (show_mode == Show.SHOW_MODE_DELETE);
boolean is_in_add_mode = (show_mode == Show.SHOW_MODE_ADD);
boolean is_in_copy_mode = (show_mode == Show.SHOW_MODE_COPY);
boolean is_in_move_mode = (show_mode == Show.SHOW_MODE_MOVE);

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
if( (current_node != null) && /*(current_node.getRights().size() > 0) && */
	(!is_in_edit_mode) && (!is_in_add_mode) && (!is_in_copy_mode)
	&& (!is_in_del_mode) && (!is_in_move_mode))
{// inspect all the rights
	int parent_n_type = (current_node.getParent() == null) ? 
	iNode.NODE_TYPE_I_ALL : current_node.getParent().getNodeType();
	
	if( (current_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
	(current_node.getNodeType() != iNode.NODE_TYPE_I_BIN)	)
	can_edit = (
	res_map.isAllowedWhatWhoForFromTo(
		Right.RIGHT_TYPE_EDIT, user_id, current_node, 
		parent_n_type, current_node.getNodeType())
	||
	res_map.isAllowedWhatWhoForFromTo(
		Right.RIGHT_TYPE_EDIT, group_id, current_node, 
		parent_n_type, current_node.getNodeType())
	);
	else
	can_edit = true; // my profile can always be edited
//System.out.println("top.jsp Check for 'can edit' ---- over");
}


// Deciding CAN_DEL
if( (current_node != null) && /*(current_node.getRights().size() > 0) && */
	(!is_in_edit_mode) && (!is_in_add_mode) && (!is_in_copy_mode)
	&& (!is_in_del_mode) && (!is_in_move_mode))
{// inspect all the rights
	int parent_n_type = (current_node.getParent() == null) ? 
	iNode.NODE_TYPE_I_ALL : current_node.getParent().getNodeType();
	
	if( (current_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
	(current_node.getNodeType() != iNode.NODE_TYPE_I_BIN) )
	can_del = 
	(		
		(
		res_map.isAllowedWhatWhoForFromTo(
	Right.RIGHT_TYPE_DELETE, user_id, current_node, 
	parent_n_type, current_node.getNodeType())
		||
		res_map.isAllowedWhatWhoForFromTo(
	Right.RIGHT_TYPE_DELETE, group_id, current_node, 
	parent_n_type, current_node.getNodeType())
		)
	) && //(((Node)current_node).getChildren().size()==0)
	// downwards
	(		
		(
		res_map.isAllowedWhatWhoForFromTo_DownInhibitory(
	Right.RIGHT_TYPE_DELETE, user_id, current_node, 
	parent_n_type, current_node.getNodeType())
		||
		res_map.isAllowedWhatWhoForFromTo_DownInhibitory(
	Right.RIGHT_TYPE_DELETE, group_id, current_node, 
	parent_n_type, current_node.getNodeType())
		)
	)
	;
//System.out.println("top.jsp Check for 'can edit' ---- over");
}


// Deciding CAN_ADD
if( (current_node != null) && /*(current_node.getRights().size() > 0) && */
	/*(current_node.getFolderFlag()) && */
	(!is_in_add_mode) && (!is_in_edit_mode) && (!is_in_copy_mode)
	&& (!is_in_del_mode) && (!is_in_move_mode))
{// inspect all the rights
	can_add = false;
//	System.out.println("top No of Type="+iNode.NODE_TYPES_I_DEFINITIVE.length);
	for(int i=0; i<iNode.NODE_TYPES_I_DEFINITIVE.length;i++)
	{
	if(
		(current_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
		(current_node.getNodeType() != iNode.NODE_TYPE_I_BIN) &&
		res_map.isAllowedWhatWho2ForFromToQuant(Right.RIGHT_TYPE_ADD,
			user_id, group_id, current_node, current_node.getNodeType(), 
			iNode.NODE_TYPES_I_DEFINITIVE[i])
	)
	can_add = true;
	}/**/
}

// Deciding CAN_COPY
/*	if( (current_node != null) && 
	(!is_in_add_mode) && (!is_in_edit_mode) && (!is_in_copy_mode)
	&& (!is_in_del_mode) && (!is_in_move_mode))
{// inspect all the rights

	for(int i=0; i<iNode.NODE_TYPES_I_DEFINITIVE.length;i++)
	{
		if(	
			(current_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
			(current_node.getNodeType() != iNode.NODE_TYPE_I_BIN) &&
			res_map.isAllowedWhatWho2ForFromToQuant(Right.RIGHT_TYPE_COPY,
				user_id, group_id, current_node, current_node.getNodeType(), 
				iNode.NODE_TYPES_I_DEFINITIVE[i])
		)
		can_copy = true;
	}
}/**/		

// Deciding CAN_MOVE = CAN_DEL + CAN_COPY
can_move = can_del && can_copy;

// Deciding CAN_RATE
if( (current_node != null) && /*(current_node.getRights().size() > 0) && */
	(!is_in_edit_mode) && (!is_in_add_mode) && (!is_in_copy_mode)
	&& (!is_in_del_mode) && (!is_in_move_mode))
{// inspect all the rights
	int parent_n_type = (current_node.getParent() == null) ? 
iNode.NODE_TYPE_I_ALL : current_node.getParent().getNodeType();
	
if( (current_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
(current_node.getNodeType() != iNode.NODE_TYPE_I_BIN)	)
can_rate = (
	res_map.isAllowedWhatWhoForFromTo(
		Right.RIGHT_TYPE_RATE, user_id, current_node, 
		parent_n_type, current_node.getNodeType())
	||
	res_map.isAllowedWhatWhoForFromTo(
		Right.RIGHT_TYPE_RATE, group_id, current_node, 
		parent_n_type, current_node.getNodeType())
	);
	else
		can_rate = true; // my profile & bin can always be rated
//System.out.println("top.jsp Check for 'can edit' ---- over");
}

if((current_mode == null || current_mode.intValue() == Show.SHOW_MODE_VIEW) && current_node.getFolderFlag() && can_rate)
{
	current_node.addNoteIconsToChildren(request, user_id, group_id);
}

String base_href = "";
if(current_node != null)
	base_href = " href='" + request.getContextPath() + "/content" + 
		"/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + current_node.getId() + "&" + 
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

<html>
<head>
<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>
<link rel='icon' href='<%=request.getContextPath()%>/assets/favicon.ico' type='image/x-icon'>
<link rel='shortcut icon' href='<%=request.getContextPath()%>/assets/favicon.ico' type='image/x-icon'> 
<link rel='stylesheet' href='<%=request.getContextPath()%>/assets/KnowledgeTree.css' type='text/css'/>
<title> Knowledge Tree - <%=title%></title>
<script src='<%=request.getContextPath()%>/assets/Tokenizer.js'></script>
<script src='<%=request.getContextPath()%>/assets/prototype.js'></script>
<script src='<%=request.getContextPath()%>/assets/sessionTimeout.js'>
<script language='javascript' type='text/javascript'>
	window.onload = sessionTimerKT.sessionTimerInit('<%=request.getContextPath()%>',<%=(session.getMaxInactiveInterval() - 120)*1000%>);
</script>
<script language='javascript' type='text/javascript'> 
function openNote(uid, gid, uri, title, feature) 
{ 
	//alert('--start');
	var params = $H({u:uid, g:gid,uri:uri,f:feature});  
	var au = new Ajax.Request( 
		'<%=request.getContextPath()%>/notes',  
		{  
			method: 'get', 
			parameters: params,  
			onSuccess: function(response)
			{ 
				if(feature=='top')
					Tip(response.responseText, FIX, ['note-btn',18,2], CLOSEBTN, true, TITLE, 'Notes on: ' + title, CLOSEBTNTEXT, 'close', CLOSEBTNCOLORS, ['#3366CC', '#E8EEF7', '#E8EEF7', '#3366CC'], STICKY, true, HEIGHT, 300, EXCLUSIVE, true, BGCOLOR,'#E8EEF7',BORDERCOLOR,'#3366CC', BORDERSTYLE,'dotted',WIDTH,400,TEXTALIGN,'justify'); 
				else
				{
					//alert('w/h ' + document.body.clientWidth + '/' + document.body.clientHeight + '   '  +  ((document.body.clientWidth-400)/2) + '/' + ((document.body.clientHeight-300)/2) );
				 	Tip(response.responseText, FIX,[(document.body.clientWidth-400)/2,(document.body.clientHeight-300)/2], CLOSEBTN, true, TITLE, 'Notes on: ' + title, CLOSEBTNTEXT, 'close', CLOSEBTNCOLORS, ['#3366CC', '#E8EEF7', '#E8EEF7', '#3366CC'], STICKY, true, HEIGHT, 300, EXCLUSIVE, true, BGCOLOR,'#E8EEF7',BORDERCOLOR,'#3366CC', BORDERSTYLE,'dotted',WIDTH,400,TEXTALIGN,'justify'); 
				}
			}.bind(this) 
 
		}  
	); 
}

function flipComment(el)
{
	var nm = el.id;
	var params = '';
	var current_time = null; 
	if($(nm+'txt').style.display=='none')
	{//open
		$(nm+'txt').style.display='block';
		$(nm+'icn').src='<%=request.getContextPath()%>/assets/minus.gif';
		//alert('open ' + nm.substr(1, nm.length-1));
		current_time = new Date();
		params = $H({u:$('meedit-user').value, g:$('meedit-group').value, e:'open', id:nm.substr(1, nm.length-1), tm:current_time.getTime()});
	}
	else
	{//close
		$(nm+'txt').style.display='none';
		$(nm+'icn').src='<%=request.getContextPath()%>/assets/plus.gif';
		//alert('close ' + nm.substr(1, nm.length-1));
		current_time = new Date();
		params = $H({u:$('meedit-user').value, g:$('meedit-group').value, e:'close', id:nm.substr(1, nm.length-1), tm:current_time.getTime()});
	}

	// report open/close
	var au = new Ajax.Request( 
		'<%=request.getContextPath()%>/noteview',  
		{  
			method: 'get', 
			parameters: params
		}  
	); 

	
}

function showEditComment()
{
	$('me-text').style.display = 'none';
	$('meedit').style.display = 'block';
	
	$('me-editbtn').style.display = 'none';
}

function showCreateComment()
{
	$('me-text').style.display = 'none';
	$('meedit').style.display = 'block';
	
	$('me-createbtn').style.display = 'none';
}

function cancelEditComment()
{
	$('me-text').style.display = 'block';
	$('meedit').style.display = 'none';
	
	if($('me-editbtn')!=null)
		$('me-editbtn').style.display = 'inline';
	else
		$('me-createbtn').style.display = 'inline';
}

function saveComment()
{
	// were there any changes?
	
	// report
	var current_time = new Date();
	var params = $H({u:$('meedit-user').value, g:$('meedit-group').value, uri:$('meedit-uri').value, id:$('meedit-noteid').value, t:$('meedit-text').value, s:$('meedit-shared').checked, a:$('meedit-signed').checked, tm:current_time.getTime()});
	var au = new Ajax.Request( 
		'<%=request.getContextPath()%>/notes',  
		{  
			method: 'post', 
			parameters: params,  
			onSuccess: function(response)
			{  
				var trimmed_id = response.responseText.replace(/^\s+|\s+$/g, '');
				var tokens = trimmed_id.tokenize('|','',true);
				//alert('trimed |' + trimmed_id + '|');
				//alert('old |' + $('meedit-noteid').value + '|');
				var action = ($('meedit-noteid').value=='1')?'create':'edit';
				var params2 = $H({u:$('meedit-user').value, g:$('meedit-group').value, e:action, id:$('meedit-noteid').value, id2:tokens[0], tm:current_time.getTime()});
				new Ajax.Request( 
					'<%=request.getContextPath()%>/noteview',  
					{  
						method: 'get', 
						parameters: params2
					}
				);
				$('meedit-noteid').value = tokens[0];
				$('me-date').innerHTML = tokens[1];
			}.bind(this) 
			
		}  
	); 
	
	// move data
	$('me-text').innerHTML = $('meedit-text').value;
	$('me-shared').innerHTML = ($('meedit-shared').checked)?"shared":"private";
	$('me-signed').innerHTML = ($('meedit-signed').checked)?"signed":"anonymous";
	
	// redraw
	$('me-text').style.display = 'block';
	$('meedit').style.display = 'none';
	
	if($('me-editbtn')!=null)
		$('me-editbtn').style.display = 'inline';
	else
	{
		$('me-createbtn').id = 'me-editbtn';
		$('me-editbtn').innerHTML = 'Edit';
		$('me-editbtn').onClick = 'showEditComment();';
		$('me-editbtn').style.display = 'inline';
	}
}

function changePwd(){
	var newpwd=prompt("input your new pwd");
}

</script>
<!-- 
		//'kt-notes',  
			function(response){  
				Tip(response.responseText, CLOSEBTN, true, TITLE, 'Notes', CLOSEBTNTEXT, 'close', CLOSEBTNCOLORS, ['#3366CC', '#E8EEF7', '#E8EEF7', '#3366CC'], STICKY, true, HEIGHT, 300, EXCLUSIVE, true, BGCOLOR,'#E8EEF7',BORDERCOLOR,'#3366CC', BORDERSTYLE,'dotted',WIDTH,400,TEXTALIGN,'justify'); 
			}.bind(this) 
 -->

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

<table cellpadding="0" cellspacing="0" border="0" width="100%" height="100%">
	<tr>
		<!-- Session control/awareness -->
		<td colspan="18" id="session_control" style="padding:1px; margin:1px;" align="right">
			<strong class="session_control"><%=user_name%></strong>&nbsp;|&nbsp;<span class="session_control"><%=group_name%></span>
		</td>
	</tr>
	
	<tr>
		<!-- Menu -->
		<!-- Up one folder -->
		<td><a <%=up_dir_href%>><img alt='[Up]' title='Up one directory' style="vertical-align:text-bottom;" src="<%=request.getContextPath()%>/assets/updir.gif" width='16' height='16' border='0' onmouseover="src='<%=request.getContextPath()%>/assets/updir_hover.gif';" onmouseout="src='<%=request.getContextPath()%>/assets/updir.gif';"/></a></td>
		
		<td>&nbsp;&nbsp;</td>
		<!-- Add -->
		<%
		if(can_add)
		{
			out.println("<td><a " + add_href + "><img style='vertical-align:text-bottom;' src='" + request.getContextPath() + "/assets/add.gif' width='16' height='16' border='0' alt='[Add]' title='Add new document/folder' onmouseover=\"src='" + request.getContextPath() + "/assets/add_hover.gif';\" onmouseout=\"src='" + request.getContextPath() + "/assets/add.gif';\"/></a></td>");
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
		
		<%
		if(can_rate)
		{
			out.println("<td><a id='note-btn' href='javascript:;' target='_top' onClick='openNote(" + user_id + "," + group_id + ",\"" + current_node.getURI()+ "\",\"" + current_node.getTitle() + "\",\"top\");'><img style='vertical-align:text-bottom;' src='" + request.getContextPath() + "/assets/icons/notes_menu.gif' width='16' height='16' border='0' alt='[Note]' title='Write Notes' onmouseover=\"src='" + request.getContextPath() + "/assets/icons/notes_menu_hover.gif';\" onmouseout=\"src='" + request.getContextPath() + "/assets/icons/notes_menu.gif';\"/></a></td>");
		}
		%>
		
		
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
		
		<!-- Reset Password -->
		 <td><a href="<%=request.getContextPath()%>/changePwdPage.jsp" class="session_control">Change Password</a></td>
		 <td>&nbsp;|&nbsp;</td>
		 
		 <!-- Reload -->
		<td><a class='session_control' href='<%=request.getContextPath() + "/content/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + res_map.getRootNode().getId() + "&" + Show.SHOW_FLAG_RELOAD %>=1' target = "_top">Reload</a></td>
		<td>&nbsp;|&nbsp;</td>
		
		<!-- Logout -->
		<td><a href='<%=request.getContextPath() + "/content/doAuthenticate?logoff=1"%>' target="_top" class="session_control">Logout</a>&nbsp;</td>
	</tr>

	<tr>
		<!-- Breadcrumbs -->
		<td colspan="18" class="menu"><%=res_map.getBreadCrumbs(current_node, request.getContextPath())%></td>
	</tr>

	<tr>
		<!-- Title -->
		<td colspan="18" bgcolor="#E8EEF7" align="center" height="23px">
			<!-- Bookmark status -->
			<!-- 
			<img title='Star' style="vertical-align:text-bottom;" src="<%=request.getContextPath()%>/assets/star_off.gif" width='16' height='16' border='0'/>&nbsp;
			<img title='Star' style="vertical-align:text-bottom;" src="<%=request.getContextPath()%>/assets/star_on.gif" width='16' height='16' border='0'/>&nbsp;
			 -->
			<strong style="padding:1px;"><%= title %></strong>
		</td>
	</tr>

	<!-- Content -->
	<tr height="100%">
		<td colspan="18">
			<iframe height="100%" width="100%" frameborder="0" marginheight="5" marginwidth="5" src="<%=request.getContextPath()%>/content/jspMain" scrolling="auto"></iframe>
		</td>
	</tr>

</body>
</html>