<%@ page 
	contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java"
	import="edu.pitt.sis.paws.kt2.*, java.util.*"
	errorPage=""
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
	"http://www.w3.org/TR/html4/loose.dtd">
<%!public ClientDaemon cd;
	public ResourceMap res_map;
	public iNode current_node;
	int user_id;
	public String top = "";
	public String main = "";
Calendar start = null;
Calendar finish = null;
long diff_mills;%>
<%
start = new GregorianCalendar();
//	cd = ClientDaemon.getInstance();
	res_map = (ResourceMap) session.
	getAttribute(ClientDaemon.SESSION_RES_MAP);
	current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
	user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();

	main = request.getContextPath() + "/content/jspMain"; //(String)urlList.get(1);
	top = request.getContextPath() + "/content/jspTop";
%>
<html>
<head>
<link rel='stylesheet' href="<%=request.getContextPath()%>/assets/KnowledgeTree.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; no-cache; c
	harset=utf-8">
<title><%=(current_node!=null)?current_node.getTitle():"Welcome!"%></title>


</head>

<frameset rows="40,*" framespacing="0" frameborder="NO" border="0">
	<frame src="<%=top%>" name="topFrame" scrolling="NO" marginheight="0" marginwidth="0">
	<frame src="<%=main%>" name="mainFrame" marginheight="4" marginwidth="4">
</frameset>

<noframes>
	<body>
	</body>
</noframes>
</html>
