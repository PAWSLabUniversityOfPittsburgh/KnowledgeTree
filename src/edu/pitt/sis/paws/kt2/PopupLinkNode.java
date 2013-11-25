package edu.pitt.sis.paws.kt2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

public class PopupLinkNode extends LinkNode
{
	final static long serialVersionUID = 2L;
	
	public PopupLinkNode()
	{
		;
	}

	private PopupLinkNode(int _id, String _title, String _uri, int _node_type,
			String _descr, String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		super(_id, _title, _uri, _node_type, _descr, _url, _folder_flag, _icon, _hidden);
	}

	public iNode NodeFactory(int _id, String _title, String _uri, int _node_type, String _descr,
			String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		return new PopupLinkNode(_id, _title, _uri, _node_type, _descr, _url, _folder_flag, _icon, _hidden);
	}
	
	public void showView(JspWriter out, HttpServletRequest request/*, boolean show_ratings*/) throws IOException
	{
		String activity_url = ClientDaemon.addIdentityToURL(this.getURL(), request);

		HttpSession session = request.getSession();
		String svc = (String)session.getAttribute("svc");
		session.removeAttribute("svc");
		
		activity_url += ((svc!=null && svc.length()>0)?"&svc="+svc:"");
		
//		System.out.println("ExternalResourcePopup.showView starting...");	
//		System.out.println("ExternalResourcePopup.showView forwarding to URL='" + this.url + "'");	

		out.println("<!-- Open -->");
		out.println("<div class='pt_main_subheader' title='Link to open'>Open&nbsp;\"" + this.getTitle() + "\"</div>");
		out.println("<div style='padding:3px; padding-left:15px;'/>");
		out.println("A new window will open automatically. Click <a href='" + activity_url + "'target='_blank'>here</a> to open it manually.");
		out.println("</div>");
		out.println("<script language='javascript'>window.open('" + activity_url + "');</script>");
				
	}
	
}
