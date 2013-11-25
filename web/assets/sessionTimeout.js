var sessionTimerKT=
{
	contextRoot:'',
	sessionTimeout:2580000,/*45 min default*/
	restartSession:function()
	{
		var message = "Are you still there?\n\nYour current session is about to expire. "+
				"Your current session will expire in 2 minutes, at " + sessionTimerKT.getTimeSessionExpires() + 
				".\n\nIf you would like to continue your work session, please select the OK" + 
				" button to renew your session.\n\nIf you would like to log off now, please select the Cancel button.";
		if ( confirm(message) )
		{
			var now = new Date();
			var action = (g_dttmSessionExpires > now);
	
			if (action == true)
			{
				window.top.location.replace(window.top.location.href);
			}
			else
			{
				window.top.location.replace(sessionTimerKT.contextRoot + '/index.jsp');
			}
		}
		else
		{
			var now = new Date();
			var action = (g_dttmSessionExpires > now);
	
			if (action == true)
			{
				window.top.location.replace(sessionTimerKT.contextRoot + '/index.jsp');
			}
			else
			{
				window.top.location.replace(sessionTimerKT.contextRoot + '/index.jsp');
			}
		}
	},

	startSessionTimer:function()
	{
		g_objWarningTimer = setTimeout('sessionTimerKT.restartSession();', sessionTimerKT.sessionTimeout);/* 2580000-45 minutes, 10680000 - 3 hrs */
	},

	getTimeSessionExpires:function()
	{
		g_dttmSessionExpires = new Date(Date.parse(Date()) + 120000);
		var dttmSessionExpiresHour = g_dttmSessionExpires.getHours();
		var dttmSessionExpiresMinute = g_dttmSessionExpires.getMinutes();
		var strMeridianIndicator = 'AM';
		if (dttmSessionExpiresHour >= 12)
		{
			strMeridianIndicator = 'PM';
		}
		if (dttmSessionExpiresHour > 12)
		{
			dttmSessionExpiresHour = (dttmSessionExpiresHour - 12);
		}
		if (dttmSessionExpiresMinute < 10)
		{
			dttmSessionExpiresMinute = '0' + dttmSessionExpiresMinute;
		}
		var strDisplayTime = dttmSessionExpiresHour + ':' + dttmSessionExpiresMinute + ' ' + strMeridianIndicator;
		return strDisplayTime;
	},
	
	sessionTimerInit:function(context_root, session_timeout)
	{
		sessionTimerKT.contextRoot = context_root;
		sessionTimerKT.sessionTimeout = (session_timeout == 0)?sessionTimerKT.sessionTimeout:session_timeout;
		try
		{
			sessionTimerKT.startSessionTimer();
		}
		catch (exc){alert(exc);}
	}
}