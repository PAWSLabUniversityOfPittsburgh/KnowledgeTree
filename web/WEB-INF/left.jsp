<%@ page 
	contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java"
	import="edu.pitt.sis.paws.kt2.*"
	errorPage=""
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
	"http://www.w3.org/TR/html4/loose.dtd">
<%!public ClientDaemon cd;
	public ResourceMap res_map;
	public iNode current_node;
	int user_id;
	int group_id;
//Calendar start = null;
//Calendar finish = null;
//long diff_mills;%>
<%
//start = new GregorianCalendar();
//	ClientDaemon cd = ClientDaemon.getInstance();
	res_map = (ResourceMap) session.
	getAttribute(ClientDaemon.SESSION_RES_MAP);
//System.out.println("jspLeft.doGet (res_map==null)=" + (res_map==null));	
	current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
	user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
	group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
%>
<html>
<head>
<link rel='stylesheet' href="<%=request.getContextPath()%>/assets/KnowledgeTree.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; no-cache; charset=utf-8">
<title>Left Frame</title>

</head>

<body> <!--onresize = 'alert("left frame name " + top.frames[0].name);
	/*alert("left frame width " + top.frames[0].width)*/;
	alert("left frame window width " + window.outerWidth + " "  + window.innerWidth);
	alert("left frame document width " + document.width);'-->
	<%
			// check the 'expanded' parameter
			String expand_s = request.getParameter(ClientDaemon.REQUEST_EXPAND);
			String collapse_s = request.getParameter(ClientDaemon.REQUEST_COLLAPSE);
			if(expand_s != null)
			{

		iNode expanded_node = res_map.getNodes().findById(
			Integer.parseInt(expand_s));
	//System.out.println("Module expanded_node==null " + (expanded_node==null));
	//System.out.println("Module search.id " + expand_s);
	//System.out.println("Module expanded_node.id " + expanded_node.getId());
	//System.out.println("Module expanded_node.expanded " + expanded_node.getExpanded() );
		expanded_node.setExpanded(true);
			//(expanded_node.getExpanded())?false:true);
		session.setAttribute(ClientDaemon.SESSION_RES_MAP,res_map);
		//------------------
	/*	ResMap rm = 
			(ResMap)session.getAttribute(sgtSessionManager.SS_RES_MAP);
		Module exp2 = rm.modules.findByID(Integer.parseInt(expand_s));
//	System.out.println("Module #" + exp2.id + " is now expanded? " + exp2.expanded);	/**/		
			}
			else if(collapse_s != null)
			{
		iNode expanded_node = res_map.getNodes().findById(
			Integer.parseInt(collapse_s));
	//System.out.println("Module expanded_module==null " + expanded_module==null);
	//System.out.println("Module expanded_module.id " + expanded_module.id);
	//System.out.println("Module expanded_module.expanded " + expanded_module.expanded);
	//System.out.println("Module #" + expanded.id + " was expanded? " + expanded.expanded);	
		expanded_node.setExpanded(false);
			//(expanded_node.getExpanded())?false:true);
		session.setAttribute(ClientDaemon.SESSION_RES_MAP,res_map);
		//------------------
	/*	ResMap rm = 
			(ResMap)session.getAttribute(sgtSessionManager.SS_RES_MAP);
		Module exp2 = rm.modules.findByID(Integer.parseInt(expand_s));
//	System.out.println("Module #" + exp2.id + " is now expanded? " + exp2.expanded);	/**/		
			}
			else if(current_node != null)
			{
		// the tree hasn't been active, so expand the way to the node
		iNode c_node = res_map.getNodes().findById(current_node.getId());
		boolean expanded_any = false;
		if(c_node != null)
		{
			expanded_any = c_node.expandParents(expanded_any);
			if(expanded_any)
				session.setAttribute(ClientDaemon.SESSION_RES_MAP,
			res_map);
		}
			}

	//System.out.println("left.jsp nodes # " + res_map.getNodes().size());
			res_map.outputNodeTree(out, request, current_node, user_id, group_id, 
		(iHTMLHierarchicalItem.HHTMLITEM_CONTENT_GREEDY_TRACE | iHTMLHierarchicalItem.HHTMLITEM_SHOW_REGULAR), 
		true);


	//finish = new GregorianCalendar();
	//diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
	//System.out.println("\t[CoPE] left.jsp millisec passed " + diff_mills);
	%>
<script language='javascript' type="text/javascript">
<!--
	//window.onresize = 'alert("left frame resized");';
//	alert('ch2 document.body.offsetWidth ' + document.body.offsetWidth);
//	alert('ch2 document.body.clientWidth ' + document.body.clientWidth);
-->
</script>
</body>

</html>
