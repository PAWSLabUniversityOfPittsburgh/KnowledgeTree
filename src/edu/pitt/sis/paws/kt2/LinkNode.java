package edu.pitt.sis.paws.kt2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import edu.pitt.sis.paws.cbum.report.ReportAPI;

public class LinkNode extends Node
{
	final static long serialVersionUID = 2L;
	
	public LinkNode()
	{
		;
	}

	protected LinkNode(int _id, String _title, String _uri, int _node_type,
			String _descr, String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		super(_id, _title, _uri, _node_type, _descr, _url, _folder_flag, _icon, _hidden);
	}

	public iNode NodeFactory(int _id, String _title, String _uri, int _node_type, String _descr,
			String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		return new LinkNode(_id, _title, _uri, _node_type, _descr, _url, _folder_flag, _icon, _hidden);
	}
	
	public void showViewHeader(JspWriter out, HttpServletRequest request) throws IOException
	{
		// REPORT VISIT
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		if(this.getId() > 0 )
		{
			ResourceMap res_map = (ResourceMap) session.
					getAttribute(ClientDaemon.SESSION_RES_MAP);
			user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
			group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
			User user_ = res_map.getUsers().findById(user_id);
			User group_ = res_map.getUsers().findById(group_id);
			String sess_ = session.getId().substring(session.getId().length()-5);
			
			ClientDaemon cd = ClientDaemon.getInstance();
			cd.getReportAPI().report(ReportAPI.APPLICATION_KNOWLEDGETREE, "ktree" + this.getId(), sess_, -1, 
					group_.getLogin(), user_.getLogin(), "Portal");
		}
	}

	public void showView(JspWriter out, HttpServletRequest request/*, boolean show_ratings*/) throws IOException
	{
		if( (this.getURL() != null) && 
			(!this.getURL().equals("")) )
		{
			String url = ClientDaemon.addIdentityToURL(this.getURL(), request);
			
			HttpSession session = request.getSession();
			String svc = (String)session.getAttribute("svc");
			session.removeAttribute("svc");
//System.out.println("SVC="+svc);			

			//Show the url and append user,group & session id
			out.println("<script>");
			out.println("	document.location = '" + url + ((svc!=null && svc.length()>0)?"&svc="+svc:"") + "';");
			out.println("</script>");
//			out.println("<body>");
//			out.println("</body>");
		}
		else
			out.println("<p>This document is empty.</p>");
	}

	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
//		out.println("</head>");
//		out.println("<body>");
	}

	public void showEdit(JspWriter out, HttpServletRequest request, 
			String cancel_to_url) throws IOException
	{
		out.println("<form style='padding:5px 5px 5px 5px;' id='edit' name='edit' method='post' action='"
			+ request.getContextPath() + "/content/doEdit' target='_top'>");
		out.println("<!-- ID field -->");
		out.println("<input name='" + NODE_FRMFIELD_ID + "' type='hidden' value='" + this.getId() + "'>");
		out.println("<!-- Title field -->");
		out.println("<div class='pt_main_subheader_editing_name'>Title</div>");
		out.println("<div class='pt_main_subheader_editingue' title='The title of the link'><input name='" + NODE_FRMFIELD_TITLE + "' type='text' value=\"" + this.getTitle() + "\" size='70' maxlength='200'></div><p/>");
		out.println("<!-- URL field -->");
		out.println("<div class='pt_main_subheader_editing_name'>URL</div>");
		out.println("<div class='pt_main_subheader_editingue' title='The URL of hte link'><input name='" + NODE_FRMFIELD_URL + "' type='text' value=\"" + this.getURL() + "\" size='70' maxlength='255'></div><p/>");
		out.println("<!-- URI field -->");
		out.println("<div class='pt_main_subheader_editing_name'>URI</div>");
		out.println("<div class='pt_main_subheader_editingue' title='The URI of the resource'><input readonly name='" + NODE_FRMFIELD_URI + "' type='text' value=\"" + this.getURI() + "\" size='70' maxlength='255'></div><p/>");
		if(request.isUserInRole("admin"))
		{
			out.println("<!-- Hidden flag field -->");
			out.println("<div class='pt_main_subheader_editing_name'>Hidden</div>");
			out.println("<div class='pt_main_subheader_editingue' title='Hidden Flag'><input name='" + NODE_FRMFIELD_HIDDEN + "' type='checkbox' value='1' " + (this.isHidden()?"checked":"") + "/></div><p/>");
		}
		
		out.print("<div>&nbsp;</div>");
		out.println("<div>");
		out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		
		out.println("<script type='text/javascript'>");
		out.println("function mySubmit()");
		out.println("{");
		out.println("	var error_msg = 'Fields:\\n';");
		out.println("	var error = false");
		out.println("	");
		out.println("	if( document.edit.fld_title.value.length == 0 )");
		out.println("	{");
		out.println("		errror_msg += '  * Title';");
		out.println("		error = true;");
		out.println("	}");
		out.println("	error_msg = '\\n should not contail an empty string';");
		out.println("	");
		out.println("	if(error) alert(error_msg );");
		out.println("	else");
		out.println("	{");
	//	out.println("		alert(navigator.appName);");
//		out.println("		if(BrowserDetect.browser!='Safari') document.edit.onsubmit();");
		out.println("		document.edit.submit();");
		out.println("	}");	
		out.println("}");
		out.println("</script>");
		
		String cancel_to_url2 = " href='" + cancel_to_url + "' " + "target='_top'";
		out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
		out.println("</div>");
		out.println("</form>");
	}
	

	
}
