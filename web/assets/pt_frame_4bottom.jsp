<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<link rel="stylesheet" href="KnowledgeTree.css" type="text/css" />
<title>Untitled Document</title>


<script language='javascript'>
<!--
	function bottomFrameChange(param)
	{
	if(param==-1)
	{
		document.getElementById('bottomFrameControl').innerHTML = 
			"<a href='#' onClick='bottomFrameChange(1);'><img src='up.gif' width='16' height='16' border='0' alt='[^]' title='Show bottom frame' /></a>";
		top.document.getElementById('bottomFrameSet').rows = '*,22';
		top.document.getElementById('bottomFrame').noResize = 'true';
	}
	else if(param==1)
	{
		document.getElementById('bottomFrameControl').innerHTML = 
			"<a href='#' onClick='bottomFrameChange(-1);'><img src='down.gif' width='16' height='16' border='0' alt='[v]' title='Hide bottom frame' /></a>";
		top.document.getElementById('bottomFrameSet').rows = '*,100';
		top.document.getElementById('bottomFrame').noResize = null;
	}
}
-->
</script>
</head>

<body bgcolor="#FFFF66">
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td class="pt_menu_main_bottom_start" id="bottomFrameControl"><a href='#' onClick='bottomFrameChange(1);'><img src='up.gif' width='16' height='16' border='0' alt='[^]' title='Show bottom frame' /></a></td>
    <td class="pt_menu_main_bottom_tab">Thing<img src="spacer16x8.gif" width="8" height="16" /><a href="#"><img src="maximize.gif" width="16" height="16" border="0" style="vertical-align:text-bottom;" /></td>
    <td class="pt_menu_main_bottom_tab_selected">Stuff<img src="spacer16x8.gif" width="8" height="16" /><a href="#"><img src="maximize.gif" width="16" height="16" border="0" style="vertical-align:text-bottom;" /></a></td>
    <td class="pt_menu_main_bottom_tab">Nostuff<img src="spacer16x8.gif" width="8" height="16" /><a href="#"><img src="maximize.gif" width="16" height="16" border="0" style="vertical-align:text-bottom;" /></td>
    <td class="pt_menu_main_bottom_end">&nbsp;</td>
  </tr>
</table>
</body>
</html>
