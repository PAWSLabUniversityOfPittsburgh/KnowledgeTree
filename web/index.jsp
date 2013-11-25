<!--
Disclaimer:
	Java code contained in this file is created as part of educational
	research and development. You assume full responsibility and risk of
	loss resulting from compiling and running this code.
Author: Michael V. Yudelson (C) 2005-2007
Affiliation: University of Pittsburgh, School of Information Sciences
	URL: http://www.yudelson.org
	Email: myudelson@gmail.com
-->
<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java" import="edu.pitt.sis.paws.kt2.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="icon" href="<%=request.getContextPath()%>/assets/favicon.ico" type="image/x-icon">
<link rel="shortcut icon" href=<%=request.getContextPath()%>/"assets/favicon.ico" type="image/x-icon"> 
<title>Portal for Adaptive Teaching and Learning. Authentication page</title>
<script type="text/javascript">
<!--
	if (top != self) 
		top.location.href = self.location.href;
-->
</script>
</head>
<%
	// invalidate session
	String is_ensemble = (String)session.getAttribute("ensemble");
	session.removeAttribute(ClientDaemon.SESSION_INITED);
	session.invalidate();
//	System.out.println("index is_ensemble="+is_ensemble);
	if(is_ensemble!=null)
	{
		request.getSession(true).setAttribute("ensemble","1");
	}
%>
<script type="text/javascript">
<!--
		document.location = '<%=request.getContextPath() + "/content/Show"%>';
-->
</script>
<body>
<!--
<CENTER><img src="<%=request.getContextPath()%>/assets/KnowledgeTreeLogo2.gif" alt="Knowledge Tree Logo" align="middle"></CENTER>
<H2 align="center"><a href="<%=request.getContextPath()%>/content/Show">Login</a></H2>
 -->
</body>
</html>


