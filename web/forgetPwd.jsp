<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="icon" href="assets/favicon.ico" type="image/x-icon" />
<link rel="shortcut icon" href="assets/favicon.ico" type="image/x-icon" /> 
<script src="assets/md5.js" type="text/javascript"></script>
<script src="assets/jquery-2.1.3.min.js"></script>
<title>Reset Password</title>
</head>
<body>
	<center><img src="assets/KnowledgeTreeLogo2.gif" alt="Knowledge Tree Logo" align="middle" /></center>
<br/>
<form id="resetform" method="post">
	<table border="0" cellspacing="0" cellpadding="4" align="center" width="450px" style=" padding:5px; background-color:#D7E8FE; border:1px dotted #000099">
    	<tr><td colspan="2" style="font-weight:bold; text-align:center;">Reset Password</td></tr>
    	
        <tr>
    	  <td colspan="2" style="color:red; text-align:justify; font-size:14px">Please input your e-mail address. The there will be a link sent to your e-mail for you to reset your password.</td>
    	</tr>

		<tr>
			<td align="right">Email Address</td>
			<td><input id="emailid" name='email' type='email' maxlength='50' size='45' value='' required/><span style="color:red;">&nbsp;*</span></td>
		</tr>

		<tr> 
			<td align="left"><input type="reset" value="Reset" /></td>
			<td align="right"><input id="resetbutton" type="submit" value="Send e-mail"/></td>
		</tr>
	</table>
</form>
<script type='text/javascript'>
 $(document).ready(function(){
	$('form').submit(function(event){
	    var email=$("#emailid").val();
	    $.ajax({
	      type:"post",//请求方式
	      url:"setNewPwd",//发送请求地址
	      timeout:30000,//超时时间：30秒
	      dataType:"json",//设置返回数据的格式
	      //请求成功后的回调函数 data为json格式
	      data:{email: email},
	      success:function(data){
	         alert(data.result);
	     },
	     //请求出错的处理
	     error:function(){
	         alert("request error");
	     }
	  });
		event.preventDefault();//IMPORTANT!!
	});
});


</script>
</body>
</body>
</html>