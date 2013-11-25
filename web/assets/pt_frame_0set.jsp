<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="stylesheet" href="KnowledgeTree.css" type="text/css" />
<title>Untitled Document</title>
<%!
	final String REQUEST_FRAMES = "frames";
	final String REQUEST_FRAMES_LEFT_ON = "L";
	final String REQUEST_FRAMES_LEFT_OFF = "l";
	final String REQUEST_FRAMES_TOP_ON = "T";
	final String REQUEST_FRAMES_TOP_OFF = "t";
	final String REQUEST_FRAMES_BOTTOM_ON = "B";
	final String REQUEST_FRAMES_BOTTOM_OFF = "b";
	final String REQUEST_FRAMES_BOTTOM_FULL = "F";
	
	String frames;
	int left;
	int top;
	int bottom;
%>
<%
	frames = request.getParameter(REQUEST_FRAMES);
	if(frames == null) frames = "LTb";
	
	left = (frames.contains(REQUEST_FRAMES_LEFT_ON))? 1 :
		(frames.contains(REQUEST_FRAMES_LEFT_OFF)) ? 0 : -1 ;
	top = (frames.contains(REQUEST_FRAMES_TOP_ON))? 1 :
		(frames.contains(REQUEST_FRAMES_TOP_OFF)) ? 0 : -1 ;
	bottom = (frames.contains(REQUEST_FRAMES_BOTTOM_ON))? 1 :
		(frames.contains(REQUEST_FRAMES_BOTTOM_OFF)) ? 0 : 
		(frames.contains(REQUEST_FRAMES_BOTTOM_FULL)) ? 2 : -1 ;
%>
</head>


<frameset rows="*" cols="250,*" id="topFrameSet" framespacing="0" frameborder="YES" border="4">
  <frame src="pt_frame_1left.jsp" id="leftFrame" name="leftFrame" scrolling="YES"  marginheight="0" marginwidth="0" />

  <frameset rows="20,*" framespacing="0" frameborder="NO" border="0">
    <frame src="pt_frame_2top.jsp" name="topFrame" scrolling="NO" marginheight="0" marginwidth="0" />
    <frameset rows="*,22" id="bottomFrameSet" framespacing="0" frameborder="YES" border="4">
	  <frame src="pt_frame_3main.jsp" name="mainFrame" marginheight="0" marginwidth="0" />
	  <frame src="pt_frame_4bottom.jsp" id="bottomFrame" name="bottomFrame" scrolling="NO" marginheight="0" marginwidth="0" noresize />
    </frameset>
  </frameset>
</frameset>
<noframes>
<body>
</body>
</noframes>
</html>
