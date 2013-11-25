package edu.pitt.sis.paws.kt2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;

public class RSSLinkNode extends LinkNode
{
	final static long serialVersionUID = 2L;
	
	public RSSLinkNode() { ; }

	protected RSSLinkNode(int _id, String _title, String _uri, int _node_type,
			String _descr, String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		super(_id, _title, _uri, _node_type, _descr, _url, _folder_flag, _icon, _hidden);
	}

	public iNode NodeFactory(int _id, String _title, String _uri, int _node_type, String _descr,
			String _url, boolean _folder_flag, String _icon, boolean _hidden)
	{
		return new RSSLinkNode(_id, _title, _uri, _node_type, _descr, _url, _folder_flag, _icon, _hidden);
	}
	
//	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings) throws IOException
//	{
//		if( (this.getURL() != null) && (!this.getURL().equals("")) )
//		{
//			String url = ClientDaemon.addIdentityToURL(this.getURL(), request);
//			out.println("<script>");
//			out.println("	document.location = '" + url + "';");
//			out.println("</script>");
//			out.println("<body>");
//			out.println("</body>");
//		}
//		else
//			out.println("<p>This document is empty.</p>");
//	}

	public void showEditHeader(JspWriter out, HttpServletRequest request) throws IOException
	{
		out.println("<script type='text/javascript'>");
		out.println("function dropdown(select)");
		out.println("{");
//		out.println("	alert('name of select: ' + select.name + '\n'");
//		out.println("		+ 'selection.text: ' + select.options[select.selectedIndex].text + '\n'");
//		out.println("		+ 'selection.value: ' + select.options[select.selectedIndex].value + '\n'");
//		out.println("		+ 'selection.id: ' + select.options[select.selectedIndex].id );");
//		out.println("	if(select.options[select.selectedIndex].id.length>0)");
		out.println("	{");
		out.println("		document.edit." + NODE_FRMFIELD_TITLE + ".value =  select.options[select.selectedIndex].title;");
		out.println("		document.edit." + NODE_FRMFIELD_URL + ".value = select.options[select.selectedIndex].value;");
		out.println("		document.edit." + NODE_FRMFIELD_URI + ".value = select.options[select.selectedIndex].id;");
		out.println("	}");
		out.println("}");
		out.println("</script>");
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
		out.println("<div class='pt_main_subheader_editingue' title='The title of the resource'><input readonly name='" + NODE_FRMFIELD_TITLE + "' type='text' value=\"" + this.getTitle() + "\" size='70' maxlength='200'></div><p/>");
		out.println("<!-- URL field -->");
		out.println("<div class='pt_main_subheader_editing_name'>URL</div>");
		out.println("<div class='pt_main_subheader_editingue' title='The URL of the resource'><input readonly name='" + NODE_FRMFIELD_URL + "' type='text' value=\"" + this.getURL() + "\" size='70' maxlength='255'></div><p/>");
		out.println("<!-- URI field -->");
		out.println("<div class='pt_main_subheader_editing_name'>URI</div>");
		out.println("<div class='pt_main_subheader_editingue' title='The URI of the resource'><input readonly name='" + NODE_FRMFIELD_URI + "' type='text' value=\"" + this.getURI() + "\" size='70' maxlength='255'></div><p/>");
		
		out.println("<!-- List field -->");
		out.println("<div class='pt_main_subheader_editing_name'>Resource List</div>");
		out.println("<select name='fld_list' id='fld_list' onChange='dropdown(this);'>");
		out.println("	<option id='' value='' title=''>--select an item from list--</option>");
		
		// compile a list of links,titles, and uri's
		HttpSession session = request.getSession();
		ResourceMap res_map = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);

		ItemType itype = res_map.getItemtypeList().findById(this.getNodeType());
		if(itype==null)
			System.out.println("!!! [KTree2] SEVERE! RSS Item Type without RSS URL");
		else
		{
			String url_suffix = itype.getResourceURLSuffix();
			url_suffix = (url_suffix == null || url_suffix.length() == 0)?"":url_suffix;
			
			String rss_url = itype.getURI();
			
			// JENA
			Model model = ModelFactory.createDefaultModel();
			model.read(rss_url);
			
			// clocking
			Calendar start = null;
			Calendar finish = null;
			long diff_mills;
			start = new GregorianCalendar();

			ResIterator channels = model.listSubjectsWithProperty(RDF.type, RSS.channel);
			Resource channel = null;
			if (channels.hasNext())
				channel = (Resource)channels.next();
			if (channel != null && channel.hasProperty(RSS.items))
			{
				Seq items = channel.getProperty(RSS.items).getSeq();
				for (int i=1; i<= items.size(); i++)
				{
					Resource res_item = items.getResource(i);
					String res_uri = res_item.toString();
					String res_title =  res_item.getProperty(RSS.title).getString();
					String res_url =  res_item.getProperty(RSS.link).getString() + url_suffix;
					String selected = (this.getURI().equalsIgnoreCase(res_uri))?" selected":"";
					String bullet = (this.getURI().equalsIgnoreCase(res_uri))?"&bull;&nbsp;":"";
					out.println("	<option id='" + res_uri + "' value='" + res_url + "'" + selected + " title='" + res_title + "'>" + bullet + res_title + "</option>");
				}
			}
			
			// clocking
			finish = new GregorianCalendar();
			diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
			System.out.println("~~~ [KTree2] RSS parsed in " + diff_mills + "ms ---- ");
				
			
//			// Informa
//			// clocking
//			Calendar start = null;
//			Calendar finish = null;
//			long diff_mills;
//			start = new GregorianCalendar();
//			try
//			{
//				ChannelIF channel = FeedParser.parse(new ChannelBuilder(), rss_url);
//				
//				Iterator<ItemIF> iter_items = channel.getItems().iterator();
//				while (iter_items.hasNext())
//				{
//					ItemIF item = iter_items.next();
//					String res_uri = item.toString();
//					String res_title =  item.getTitle();
//					String res_url =  item.getLink().toString();
//					String selected = (this.uri.equalsIgnoreCase(res_uri))?" selected":"";
//					String bullet = (this.uri.equalsIgnoreCase(res_uri))?"&bull;&nbsp;":"";
//					out.println("	<option id='" + res_uri + "' value='" + res_url + "'" + selected + " title='" + res_title + "'>" + bullet + res_title + "</option>");
//				}
//			}
//			catch(de.nava.informa.core.ParseException pe)
//			{
//				pe.printStackTrace(System.out);
//			}
//			// clocking
//			finish = new GregorianCalendar();
//			diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
//			System.out.println("... [XXX] RSS parsed by Informa in " + diff_mills + "ms ---- ");
		}
		
		out.println("</select>&nbsp;bullet (&bull;) marks currently saved resource");
		out.println("<div class='pt_main_subheader_editingue' title='The List of resource'>");
		out.println("</div>");

		out.print("<div>&nbsp;</div>");
		out.println("<div>");
		out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		
		out.println("<script type='text/javascript'>");
		out.println("function mySubmit()");
		out.println("{");
		out.println("	var error_msg = '';");//'Fields:\\n';");
		out.println("	var error = false");
		out.println("	");
//		out.println("	if( document.edit.fld_title.value.length == 0)");
//		out.println("	{");
//		out.println("		errror_msg += '  * Title';");
//		out.println("		error = true;");
//		out.println("	}");
		out.println("	if(document.edit.fld_list.options[document.edit.fld_list.selectedIndex].id=='')");
		out.println("	{");
		out.println("		error_msg += 'Resource should be selected';");
		out.println("		error = true;");
		out.println("	}");
//		out.println("	error_msg = '\\n should not be specified';");
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
	
	public void resetURI(Connection conn) throws SQLException
	{
		//DO NOTHING
		;
	}
	
}
