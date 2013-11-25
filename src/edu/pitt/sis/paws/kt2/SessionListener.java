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




public class SessionListener implements HttpSessionListener, HttpSessionAttributeListener 
{
	private String name = null;
	private HttpSession session = null;
//	private ClientDaemon portalDaemon = null;

	public void sessionCreated(HttpSessionEvent event)
	{
		session = event.getSession();
//		portalDaemon = (ClientDaemon)ClientDaemon.getInstance(event.getSession().getServletContext());
//		if(ClientDaemon.CONTEXT_DEBUG_VAL > 0)
//			System.out.println("*** PAWS-SL: Session pending   id=" + session.getId());
	}

	public void sessionDestroyed(HttpSessionEvent event)
	{
		session = event.getSession();

//		String session_inited = (String)session.getAttribute(ClientDaemon.SESSION_INITED);
//		portalDaemon = (ClientDaemon)ClientDaemon.getInstance(event.getSession().getServletContext());
		
//		if(portalDaemon.CONTEXT_DEBUG_VAL > 0)
//		{
//			if(session_inited!=null)
//				System.out.println("*** PAWS-SL: Session destroyed id=" + session.getId());
//			else
//				System.out.println("*** PAWS-SL: Session abandoned id=" + session.getId());
//		}
	}

	public void attributeAdded(HttpSessionBindingEvent event)
	{
		name = event.getName();
		session = event.getSession();
		
//		portalDaemon = (ClientDaemon)ClientDaemon.getInstance(event.getSession().getServletContext());
		if(name.equalsIgnoreCase(ClientDaemon.SESSION_INITED))
		{
			System.out.println("... [KTree2] PAWS-SL: Session started. ID = " + session.getId());
		}
	}

	public void attributeRemoved(HttpSessionBindingEvent event)
	{
		name = event.getName();
		session = event.getSession();
	}

	public void attributeReplaced(HttpSessionBindingEvent event)
	{
		name = event.getName();
		session = event.getSession();
	}
}
