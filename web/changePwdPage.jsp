<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    language="java"
	import="edu.pitt.sis.paws.kt2.*,edu.pitt.sis.paws.core.*"
	errorPage=""%>
<%
	if(!ClientDaemon.isSessionInited(request.getSession(false)))
	{
		ClientDaemon.forwardToURL(request,response, "/content/doAuthenticate");
		return;
	}
	
	ClientDaemon cd = ClientDaemon.getInstance();
	String user_name = (String)session.getAttribute(ClientDaemon.SESSION_USER_NAME);
	int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();

%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Change Password</title>
<script src="assets/jquery-2.1.3.min.js"></script>
</head>
<body>
	<center><img src="assets/KnowledgeTreeLogo2.gif" alt="Knowledge Tree Logo" align="middle" /></center>
<br/>
	<form id="resetform" method="post">
	<table border="0" cellspacing="0" cellpadding="4" align="center" width="450px" style=" padding:5px; background-color:#D7E8FE; border:1px dotted #000099">
    	<tr><td colspan="2" style="font-weight:bold; text-align:center;">Reset Password</td></tr>
    	
        <tr>
    	  <td id="information" colspan="2" style="color:red; text-align:justify; font-size:14px">Please set up new password for your account.</td>
    	</tr>

		<tr>
			<td align="right">Name</td>
			<td><input id="email"type='text' maxlength='50' size='35' value='<%=user_name%>' readonly="readonly"/></td>
		</tr>
		
		<tr>
			<td align="right">New Password</td>
			<td><input id="newPassword1" type='password' maxlength='50' size='35' value='' required/></td>
		</tr>
		
		<tr>
			<td align="right">Confirm Password</td>
			<td><input id="newPassword2" type='password' maxlength='50' size='35' required/></td>
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
	//check password
	$('#newPassword1,#newPassword2').keyup(function(event) {
		/* Act on the event */
		var pwd1=$('#newPassword1').val();
		var pwd2=$('#newPassword2').val();
		//alert(pwd1+" "+pwd2);
		if (pwd1!=pwd2) {
			$('#resetbutton').attr('disabled', 'disabled');
			$('#information').html('*Two password must be same!');
		}
		if(pwd1==pwd2){
			$('#resetbutton').removeAttr('disabled');
			$('#information').html('Please set up new password for your account.');
		}
	});
	//ajax
	$('form').submit(function(event){
	    var password=$("#newPassword1").val();
	    var userid=<%=user_id%>
	    $.ajax({
	      type:"post",//请求方式
	      url:"SaveNewPwd",//发送请求地址
	      timeout:30000,//超时时间：30秒
	      dataType:"json",//设置返回数据的格式
	      //请求成功后的回调函数 data为json格式
	      data:{"password": password,"userid":userid,"isChangePwd":"yes"},
	      success:function(d){
	    	  //give back sent information
			 //alert(d.status);
	    	  alert("Successfully change password, please login");
	    	  window.location="index.jsp";
	     },
	     //请求出错的处理
	     error:function(d){
	         alert(d.status);
	     }
	  });
		event.preventDefault();//IMPORTANT!!
	});
	
});

</script>
</html>