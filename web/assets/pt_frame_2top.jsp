<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="stylesheet" href="KnowledgeTree.css" type="text/css" />
<title>Untitled Document</title>

<script language='javascript'>
<!--
	function leftFrameChange(param)
	{
	if(param==-1)
	{
		document.getElementById('leftFrameControl').innerHTML = 
			"<a href='#' onClick='leftFrameChange(1);'><img src='right.gif' width='16' height='16' border='0' alt='[>]' title='Show left frame'  /></a>";
		top.document.getElementById('topFrameSet').cols = '0,*';
		top.document.getElementById('leftFrame').noResize = 'true';
	}
	else if(param==1)
	{
		document.getElementById('leftFrameControl').innerHTML = 
			"<a href='#' onClick='leftFrameChange(-1);'><img src='left.gif' width='16' height='16' border='0' alt='[<]' title='Hide left frame'  /></a>";
		top.document.getElementById('topFrameSet').cols = '250,*';
		top.document.getElementById('leftFrame').noResize = null;
	}
}
-->


</script>
</head>

<body bgcolor="#CCFF66">
<table width="100%" border="0" cellpadding="0" cellspacing="0" style="padding-left:2px;">
  <tr>
    <td id="leftFrameControl"><a href='#' onClick='leftFrameChange(-1);'><img src='left.gif' width='16' height='16' border='0' alt='[<]' title='Hide left frame' /></a></td>
    <td><img src="spacer16x8.gif" width="8" height="16" border="0" /></td>
    <td><img src='reload.gif' width='16' height='16' border='0' alt='R' title='Reload' /></td>
    <td width="100%">&nbsp;</td>
  </tr>
</table>
</body>
</html>
