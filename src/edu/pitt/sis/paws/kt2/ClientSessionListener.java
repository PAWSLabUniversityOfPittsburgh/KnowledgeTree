/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */
 
/**
 * @author Michael V. Yudelson
 */

package edu.pitt.sis.paws.kt2;

import javax.servlet.http.*;

public class ClientSessionListener 
	implements javax.servlet.http.HttpSessionListener//,
//	javax.servlet.http.HttpSessionAttributeListener 
{
	private String name = null;
	private HttpSession session = null;

	public void sessionCreated(HttpSessionEvent event)
	{
		session = event.getSession();
System.out.println("~~~ [KTree2] ClientSessionListener.sessionCreated: " + session.getId());		
//System.out.println("Active time left: " + session.getLastAccessedTime() + "/" + session.getMaxInactiveInterval());		
	}

	public void sessionDestroyed(HttpSessionEvent event)
	{
		session = event.getSession();
System.out.println("~~~ [KTree2] ClientSessionListener.sessionDestroyed: " + session.getId());		
	}

	public void attributeAdded(HttpSessionBindingEvent event)
	{
		name = event.getName();
		session = event.getSession();
System.out.println("~~~ [KTree2] ClientSessionListener.attributeAdded: " + name);		
	}

	public void attributeRemoved(HttpSessionBindingEvent event)
	{
		name = event.getName();
		session = event.getSession();
System.out.println("~~~ [KTree2] ClientSessionListener.attributeRemoved: " + name);		
	}

//	public void attributeReplaced(HttpSessionBindingEvent event)
//	{
//		name = event.getName();
//		session = event.getSession();
//System.out.println("[CoPE] ClientSessionListener.attributeReplaced: " + name);		
//	}
}
