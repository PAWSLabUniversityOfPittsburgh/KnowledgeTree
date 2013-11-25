package edu.pitt.sis.paws.kt2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

public class DownloadLinkNode extends LinkNode
{
	final static long serialVersionUID = 2L;
	
	public DownloadLinkNode()
	{
		;
	}

	protected DownloadLinkNode(int _id, String _title, String _uri, int _node_type,
			String _descr, String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		super(_id, _title, _uri, _node_type, _descr, _url, _folder_flag, _icon, _hidden);
	}

	public iNode NodeFactory(int _id, String _title, String _uri, int _node_type, String _descr,
			String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		return new DownloadLinkNode(_id, _title, _uri, _node_type, _descr, _url, _folder_flag, _icon, _hidden);
	}
	
	public void showView(JspWriter out, HttpServletRequest request/*, boolean show_ratings*/) throws IOException
	{
		String activity_url = ClientDaemon.addIdentityToURL(this.getURL(), request);
//		System.out.println("ExternalResourceDownloadable.showView starting...");	
//		System.out.println("ExternalResourceDownloadable.showView forwarding to URL='" + this.url + "'");	

		out.println("<!-- Download -->");
		out.println("<div class='pt_main_subheader' title='Downloadable link'>Download&nbsp;\"" + this.getTitle() + "\"</div>");
		out.println("<div style='padding:3px; padding-left:15px;'/>");
		out.println("<a href='" + activity_url + "'target='_blank'>Download link</a>&nbsp(Right-click and choose \"Save AS\")");
		out.println("</div>");
	}
	
}
