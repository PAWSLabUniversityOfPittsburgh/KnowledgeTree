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
<title>Knowledge Tree: Portal for Adaptive Teaching and Learning</title>
<script type="text/javascript">
<!--
	if (top != self) 
		top.location.href = self.location.href;
-->
</script>
<style>
*{
	font-family:"Times New Roman", Times, serif;
	font-size:1.0em;
}
</style>
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
<table border="0" cellspacing="0" cellpadding="0" align="center" width="544">
	<tr>
		<td style="padding: 0px;" ><img src="<%=request.getContextPath()%>/assets/KnowledgeTreeLogo2.gif" alt="Knowledge Tree Logo" width="301" height="217" align="middle" /></td>
		<td style="padding:20px;" width="100%px" align="justify">Knowledge Tree portal  is a result of nearly a decade of reseach in the areas of adaptive web systems and user modeling. It provides access to a constellation of e-learning tools created at the <a href="http://www.sis.pitt.edu/~taler/" target="_blank">TALER Lab</a> and <a href="http://adapt2.sis.pitt.edu/wiki" target="_blank">PAWS Lab</a> of the <a href="http://www.ischool.pitt.edu/" target="_blank">School of Information Sciences</a>,  <a href="http://www.pitt.edu/" target="_blank">University of Pittsburgh</a>. These tools are supporting learning in a several domains, including C, Java, SQL, etc.</td>
	</tr>
</table>

<% if(is_ensemble==null)  { %>

<table border="0" cellspacing="0" cellpadding="10" align="center">
	<tr>
		<!-- Users -->
		<td valign="top">
			<form action="j_security_check" method="post">
			<table height="140px" border="0" cellspacing="0" cellpadding="4" style="background-color: #D7E8FE; margins:125px; border:1px solid #C3D9FF;">
				<tr>
				  <td  width="240px" colspan="2" align="center" style="font-weight:bold;">Current users login here</td>
				</tr>
				<tr> 
					<td width="50" style="color:red;">Login</td>
					<td width="150"><input id="j_username_main" name="j_username" type="text" value="" size="20" maxlength="20" /></td>
				</tr>
				<tr> 
					<td style="color:red;">Password</td>
					<td><input id="j_password_main" name="j_password" type="password" value="" size="20" maxlength="20" /></td>
				</tr>
				
				<tr> 
					<td align="left"><a target="_blank" href="<%=request.getContextPath()%>/forgotPwdPage.jsp">Forget?</a></td><td align="right"><input type="submit" value="Login" /></td>
				</tr>
			</table>
			</form>
		</td>
		<!-- Register -->
		<td valign="top">
			<table height="140px" border="0" cellspacing="0" cellpadding="5" style="background-color: #D7E8FE; margins:15px; border:1px solid #C3D9FF;">
				<tr><td width="240px" align="center" style="font-weight:bold;">Create trial account</td></tr>
				<tr>
						<td width="240px" align="justify" >To access our suite of showcase courses with personalized navigation support, create a trial account.</td>
				</tr>
				<tr><td align="center"><a href="<%=request.getContextPath()%>/register.html"><input type="button" value="Create trial account" onclick="document.location='<%=request.getContextPath()%>/register.html'" /></a></td></tr>
			</table>
		</td>
	</tr>
	<tr>
			<td colspan="2">If you are a researcher and would like to  quickly try our adaptation tools,<br />
					create a trial account (above) and proceed <a href="http://adapt2.sis.pitt.edu/cbum/" target="_blank">here</a>.</td>
	</tr>
	<tr>
		<!-- Try  -->
		<td valign="top" colspan="2">
			<table height="140px" border="0" cellspacing="0" cellpadding="5" style="background-color: #D7E8FE; margins:15px; border:1px solid #C3D9FF;">
				<tr><td width="512px" align="center" colspan="2" style="font-weight:bold;">Try without logging in</td></tr>
				<tr>
						<td width="512px" align="justify" colspan="2">Try our tools in-the-raw ... </td>
				</tr>
				<tr><td align="center">
					<a href="<%=request.getContextPath()%>/ensemble/c.html"><input type="button" value="C course" onclick="document.location='<%=request.getContextPath()%>/ensemble/c.html'" /></a>&nbsp;&nbsp;
					<a href="<%=request.getContextPath()%>/ensemble/java.html"><input type="button" value="Java course" onclick="document.location='<%=request.getContextPath()%>/ensemble/java.html'" /></a>&nbsp;&nbsp;
					<a href="<%=request.getContextPath()%>/ensemble/sql.html"><input type="button" value="Database course" onclick="document.location='<%=request.getContextPath()%>/ensemble/sql.html'" /></a>
				</td></tr>
				<tr>
						<td width="512px" align="justify" colspan="2">... or with a group-based adaptive navigation support</td>
				</tr>
				<tr><td align="center">
					<a href="<%=request.getContextPath()%>/ensemble/c_adapt.html"><input disabled="disabled" type="button" value="C course" onclick="document.location='<%=request.getContextPath()%>/ensemble/c_adapt.html'" /></a>&nbsp;&nbsp;
					<a href="<%=request.getContextPath()%>/ensemble/java_adapt.html"><input type="button" value="Java course" onclick="document.location='<%=request.getContextPath()%>/ensemble/java_adapt.html'" /></a>&nbsp;&nbsp;
					<a href="<%=request.getContextPath()%>/ensemble/sql_adapt.html"><input disabled="disabled" type="button" value="Database course" onclick="document.location='<%=request.getContextPath()%>/ensemble/sql_adapt.html'" /></a>
				</td></tr>
			</table>
		</td>
	</tr>
</table>
<table width="544px" border="0" cellspacing="0" cellpadding="2" align="center" style="margin-top:20px">
	<tr>
		<!-- Users -->
		<td valign="top" align="center">For more information go to <a href="http://adapt2.sis.pitt.edu/wiki" target="_blank">PAWS Lab page</a> or email us at paws at pitt dot edu</td>
	</tr>
</table>

<% } else if(is_ensemble!=null && !is_ensemble.equals("not")){ %>
<form action="j_security_check" method="post">
	<table border="0" cellspacing="0" cellpadding="4" align="center">
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
	</table>
</form>
<% }%>




<script type="text/javascript">
	document.getElementById("j_username_main").focus();
	document.getElementById("j_username_main").select();
</script>
</body>
</html>


