<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java"
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="icon" href="assets/favicon.ico" type="image/x-icon" />
<link rel="shortcut icon" href="assets/favicon.ico" type="image/x-icon" /> 
<script src="assets/jquery-2.1.3.min.js"></script>
<title>Reset Password</title>

</head>
<%
	String emailString=(String)request.getAttribute("email");
%>
<body>
	<center><img src="assets/KnowledgeTreeLogo2.gif" alt="Knowledge Tree Logo" align="middle" /></center>
<br/>
	<form id="resetform" method="post">
	<table border="0" cellspacing="0" cellpadding="4" align="center" width="450px" style=" padding:5px; background-color:#D7E8FE; border:1px dotted #000099">
    	<tr><td colspan="2" style="font-weight:bold; text-align:center;">Reset Password</td></tr>
    	
        <tr>
    	  <td colspan="2" style="color:red; text-align:justify; font-size:14px">Please set up new password for your account.</td>
    	</tr>

		<tr>
			<td align="right">Email Address</td>
			<td><input type='text' maxlength='50' size='35' value='<%=emailString%>' readonly="readonly"/></td>
		</tr>
		
		<tr>
			<td align="right">New Password</td>
			<td><input id="newPassword1" type='text' maxlength='50' size='35' value='' required/></td>
		</tr>
		
		<tr>
			<td align="right">Confirm Password</td>
			<td><input id="newPassword2" type='text' maxlength='50' size='35' required/></td>
		</tr>
		
		<tr> 
			<td align="left"><input type="reset" value="Reset" /></td>
			<td align="right"><input id="resetbutton" type="submit" value="Save new password"/></td>
		</tr>
	</table>
	</form>
</body>
<script>
	$(document).ready(function() {
		
	});

</script>
</html>