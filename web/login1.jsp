<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java" 
	import="java.util.*, edu.pitt.sis.paws.kt2.*" 
	errorPage="" %>
	
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="icon" href="<%= request.getContextPath()%>/assets/favicon.ico" type="image/x-icon" />
<link rel="shortcut icon" href="<%= request.getContextPath()%>/assets/favicon.ico" type="image/x-icon" /> 
<title>Portal for Adaptive Teaching and Learning. Authentication</title>
<script type="text/javascript">
<!--
	if (top != self) 
		top.location.href = self.location.href;
-->
</script>
</head>

<body>
<%!
	String is_ensemble = "";
%>
<%
	// Ensemble uber Alles
	is_ensemble = request.getParameter("ensemble");
	if(is_ensemble==null)
		is_ensemble = (String)session.getAttribute("ensemble");

	//	ClientDaemon cd = (ClientDaemon)ClientDaemon.getInstance(application);
	Enumeration enu = session.getAttributeNames();
	for(;enu.hasMoreElements();)
		session.removeAttribute((String)enu.nextElement());
	session.setAttribute(ClientDaemon.SESSION_HIDE_LEFT_FRAME, "hide");
//System.out.println(">> is_ensemble="+is_ensemble);	
	if(is_ensemble!=null && !is_ensemble.equals("not"))
		session.setAttribute("ensemble", "1");
	else
	{
		session.removeAttribute("ensemble");
		is_ensemble = null;
	}
%>
<!--  -->
<center><img src="<%=request.getContextPath()%>/assets/KnowledgeTreeLogo2.gif" alt="Knowledge Tree Logo" align="middle" /></center>
<br/>
<form action="j_security_check" method="post">
	<table border="0" cellspacing="0" cellpadding="4" align="center">
    	<!-- Regular -->
    	<% if(is_ensemble==null)  { %>
    	<tr style="background-color:#D7E8FE;">
    	  <td colspan="2" align="center">Current users login here</td>
    	</tr>
		<tr style="background-color:#D7E8FE;"> 
			<td width="50">Login</td>
			<td width="150"><input id="j_username" name="j_username" type="text" value="" size="25" maxlength="15" /></td>
		</tr>
		<tr style="background-color:#D7E8FE;"> 
			<td>Password</td>
			<td><input id="j_password" name="j_password" type="password" value="" size="25" maxlength="15" /></td>
		</tr>
		<tr style="background-color:#D7E8FE;"> 
			<td><!--<input type="reset" value="Reset" />--></td>
			<td align="right"><input type="submit" value="Login" /></td>
		</tr>
		<tr><td colspan="2">&nbsp;</td></tr>
		<tr style="background-color:#D7E8FE;"><td colspan="2" align="center">Visitors create trial account</td></tr>
		<tr style="background-color:#D7E8FE;"><td colspan="2" align="center"><a href="<%=request.getContextPath()%>/register.html"><input type="button" value="Create trial account" /></a></td></tr>
		
		<% } else if(is_ensemble!=null && !is_ensemble.equals("not")){ %>
		<!-- Ensemble -->
    	<tr style="background-color:#D7E8FE;">
    	  <td colspan="2" align="center">Ensemble login</td>
    	</tr>
    	
		<tr style="background-color:#D7E8FE;display:none;"> 
			<td width="50">Login</td>
			<td width="150"><input id="j_username" name="j_username" type="text" value="meta_ensemble" size="25" maxlength="15" /></td>
		</tr>
		<tr style="background-color:#D7E8FE;display:none;"> 
			<td>Password</td>
			<td><input id="j_password" name="j_password" type="password" value="meta_ensemble" size="25" maxlength="15" /></td>
		</tr>
		
		<tr style="background-color:#D7E8FE;"> 
			<td colspan="2" align="center"><input type="submit" value="Enter" /></td>
		</tr>
		<tr><td colspan="2">&nbsp;</td></tr>
		<tr style="background-color:#D7E8FE;"><td colspan="2" align="center">Regular KnowledgeTree users login</td></tr>
		<tr style="background-color:#D7E8FE;"><td colspan="2" align="center"><a href="<%=request.getContextPath()%>/login.jsp?ensemble=not"><input type="button" value="Click here" /></a></td></tr>
		<% }%>

	</table>
</form>

<!--
<h4><center>Rooted In</center></h4>
<div><center>
	<a target="_blank" href="http://java.sun.com"><img src="assets/poweredby/java.gif" alt="Java Logo" width="44" height="82" border="0" /></a>&nbsp;&nbsp;
	<a target="_blank" href="http://tomcat.apache.org"><img src="assets/poweredby/tomcat.gif" alt="Apache Tomcat Logo" width="116" height="82" border="0" /></a>&nbsp;&nbsp;
	<a target="_blank" href="http://www.mysql.com/"><img src="assets/poweredby/mysql.gif" alt="MySQL Logo" width="155" height="82" border="0" /></a>
	<a target="_blank" href="http://jena.sourceforge.net/"><img src="assets/poweredby/jena.gif" alt="Jena API Logo" width="139" height="82" border="0" /></a>&nbsp;&nbsp;
	<a target="_blank" href="http://prototypejs.org/"><img src="assets/poweredby/prototype.gif" alt="Prototype Javascript Framework Logo" width="176" height="82" border="0" /></a>
</center></div>
-->
<script type="text/javascript">
	document.getElementById("j_username").focus();
	document.getElementById("j_username").select();
</script>
</body>
</html>


