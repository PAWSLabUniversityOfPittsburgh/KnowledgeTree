<?xml version="1.0" encoding="UTF-8" ?>
<%@ page
	contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java"
	errorPage=""
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel='stylesheet' href='<%=request.getContextPath()%>/assets/rest.css' type='text/css' />

<%!
	public String message = "";
	public boolean success = false;
%>

<%
	message = request.getParameter("msg");
//System.out.println("msg=" + message);
	success = (message != null) && (message.length() >0) && !message.startsWith("0");
	if(!success)
	{
		int pos_1 = message.indexOf("|",0);
		int pos_2 = message.indexOf("|",pos_1+1);
		message = message.substring(pos_1+1, pos_2);
		message = "Error creating user accout: " + message;
	}
	else
	{
		message = "User account created succesfully";
	}
%>

<title>Registration result</title>
</head>
<body>

<table cellpadding='2px' cellspacing='0px' class='<%=((success)?"green":"burg")%>_table'>
	<tr>
		<td class='<%=((success)?"green":"burg")%>_table_caption'>New user account creation</td>
	</tr>
	<tr>
		<td class='<%=((success)?"green":"burg")%>_table_message'>
			<%=message%><br/>
			<%=((success)?"<a href='http://adapt2.sis.pitt.edu/kt'>Login page</a>":"<a href='http://adapt2.sis.pitt.edu/kt/register.html'>Registration</a>") %>
		</td>
	</tr>
</table>

</body>
</html>