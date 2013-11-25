package edu.pitt.sis.paws.kt2.oai_pmh;



import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.pitt.sis.paws.core.utils.SQLManager;
import edu.pitt.sis.paws.core.Item2;
import edu.pitt.sis.paws.core.Item2Vector;


/**
 * Servlet implementation class for Servlet: EnsembleOAIPMHServlet
 * 
 */
public class EnsembleOAIPMHServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet
{
	static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		SQLManager kt_sqlm = new SQLManager("java:comp/env/jdbc/portal");
		SQLManager um_sqlm = new SQLManager("java:comp/env/jdbc/um");
		
		Connection kt_conn = null;
		Connection um_conn = null;

		Item2Vector<OAI_PMH_Item> sql_list = new Item2Vector<OAI_PMH_Item>();
		Item2Vector<OAI_PMH_Item> java_list = new Item2Vector<OAI_PMH_Item>();
		Item2Vector<OAI_PMH_Item> c_list = new Item2Vector<OAI_PMH_Item>();
		
		try
		{
			kt_conn = kt_sqlm.getConnection();
			um_conn = um_sqlm.getConnection();
			
			// ------------------------
			// <!-- SQL -->
			Statement kt_stmt = kt_conn.createStatement();
			ResultSet kt_rs = kt_stmt.executeQuery("SELECT n1.Title AS Topic, n2.ItemTypeID, n2.Title, n2.URI, n2.URL, n2.DateModified, u.Name " +
					"FROM rel_node_node nn1 JOIN rel_node_node nn2 ON(nn1.ChildNodeID=nn2.PArentNodeID AND nn1.ParentNodeID=3509)" +
					" JOIN ent_node n2 ON(nn2.ChildNodeID=n2.NodeID AND n2.ItemTypeID IN(25,28)) JOIN ent_node n1 " +
					"ON(nn1.ChildNodeID=n1.NodeID) JOIN ent_user u ON(u.UserID=n2.UserID) ORDER BY nn1.OrderRank, nn2.OrderRank;");
			
			while(kt_rs.next())
			{
				int itemtype = kt_rs.getInt("ItemTypeID");
				String title = kt_rs.getString("Title");
				String uri = kt_rs.getString("URI");
				String url = kt_rs.getString("URL");
				String name = kt_rs.getString("Name");
				String date = kt_rs.getString("DateModified");
				String topic = kt_rs.getString("Topic");
				
				sql_list.add( new OAI_PMH_Item(itemtype, title, uri, url, name, date, topic) );
				
			}
			kt_rs.close(); kt_rs = null;
			kt_stmt.close(); kt_stmt = null;
			
			// look for metadata
			StringBuffer uri_list = new StringBuffer();
			for(int i=0; i<sql_list.size(); i++)
				uri_list.append( ((uri_list.length()>0)?",":"") + "'" + sql_list.get(i).getURI() + "'");

			Statement um_stmt = um_conn.createStatement();
			ResultSet um_rs = um_stmt.executeQuery(
					"SELECT DISTINCT c.Title, a.URI FROM ent_concept c JOIN rel_concept_activity ca ON (ca.ConceptID=c.ConceptID) " +
					"JOIN ent_activity a ON(a.ActivityID=ca.ActivityID AND a.URI IN(" + uri_list.toString() + ")) "+ 
					"JOIN rel_domain_concept dc ON(dc.ConceptID=ca.ConceptID AND dc.DomainID=12);"			
			);

			while(um_rs.next())
			{
				String title = um_rs.getString("Title");
				String uri = um_rs.getString("URI");
				OAI_PMH_Item res_item = sql_list.findByURI(uri);
				if(res_item!=null)
					res_item.metadata += ((res_item.metadata.length()>0)?", ":"") + title;
			}
			um_rs.close(); um_rs = null;
			um_stmt.close(); um_stmt = null;

			// ------------------------
			// <!-- Java -->
			kt_stmt = kt_conn.createStatement();
			kt_rs = kt_stmt.executeQuery("SELECT n1.Title AS Topic, n2.ItemTypeID, n2.Title, n2.URI, n2.URL, n2.DateModified, u.Name " +
					"FROM rel_node_node nn1 JOIN rel_node_node nn2 ON(nn1.ChildNodeID=nn2.PArentNodeID AND nn1.ParentNodeID=3011)" +
					" JOIN ent_node n2 ON(nn2.ChildNodeID=n2.NodeID AND n2.ItemTypeID IN(30,32)) JOIN ent_node n1 " +
					"ON(nn1.ChildNodeID=n1.NodeID) JOIN ent_user u ON(u.UserID=n2.UserID) ORDER BY nn1.OrderRank, nn2.OrderRank;");
			
			while(kt_rs.next())
			{
				int itemtype = kt_rs.getInt("ItemTypeID");
				String title = kt_rs.getString("Title");
				String uri = kt_rs.getString("URI");
				String url = kt_rs.getString("URL");
				String name = kt_rs.getString("Name");
				String date = kt_rs.getString("DateModified");
				String topic = kt_rs.getString("Topic");
				
				java_list.add( new OAI_PMH_Item(itemtype, title, uri, url, name, date, topic) );
				
			}
			kt_rs.close(); kt_rs = null;
			kt_stmt.close(); kt_stmt = null;
			
			// look for metadata
			uri_list = new StringBuffer();
			for(int i=0; i<java_list.size(); i++)
				uri_list.append( ((uri_list.length()>0)?", ":"") + "'" + java_list.get(i).getURI() + "'");

			um_stmt = um_conn.createStatement();
			um_rs = um_stmt.executeQuery(
					"SELECT DISTINCT c.Title, a.URI FROM ent_concept c JOIN rel_concept_activity ca ON (ca.ConceptID=c.ConceptID) " +
					"JOIN ent_activity a ON(a.ActivityID=ca.ActivityID AND a.URI IN(" + uri_list.toString() + ")) "+ 
					"JOIN rel_domain_concept dc ON(dc.ConceptID=ca.ConceptID AND dc.DomainID=11);"			
			);

			while(um_rs.next())
			{
				String title = um_rs.getString("Title");
				String uri = um_rs.getString("URI");
				OAI_PMH_Item res_item = java_list.findByURI(uri);
				if(res_item!=null)
					res_item.metadata += ((res_item.metadata.length()>0)?",":"") + title;
			}
			um_rs.close(); um_rs = null;
			um_stmt.close(); um_stmt = null;

			// ------------------------
			// <!-- C -->
			kt_stmt = kt_conn.createStatement();
			kt_rs = kt_stmt.executeQuery("SELECT n1.Title AS Topic, n2.ItemTypeID, n2.Title, n2.URI, n2.URL, n2.DateModified, u.Name " +
					"FROM rel_node_node nn1 JOIN rel_node_node nn2 ON(nn1.ChildNodeID=nn2.PArentNodeID AND nn1.ParentNodeID=887)" +
					" JOIN ent_node n2 ON(nn2.ChildNodeID=n2.NodeID AND n2.ItemTypeID IN(14,11)) JOIN ent_node n1 " + //
					"ON(nn1.ChildNodeID=n1.NodeID) JOIN ent_user u ON(u.UserID=n2.UserID) ORDER BY nn1.OrderRank, nn2.OrderRank;");
			
			while(kt_rs.next())
			{
				int itemtype = kt_rs.getInt("ItemTypeID");
				String title = kt_rs.getString("Title");
				String uri = kt_rs.getString("URI");
				String url = kt_rs.getString("URL");
				String name = kt_rs.getString("Name");
				String date = kt_rs.getString("DateModified");
				String topic = kt_rs.getString("Topic");
				
				topic = topic.substring(topic.indexOf(':')+2);
				
				c_list.add( new OAI_PMH_Item(itemtype, title, uri, url, name, date, topic) );
				
			}
			kt_rs.close(); kt_rs = null;
			kt_stmt.close(); kt_stmt = null;
			
			// look for metadata
			uri_list = new StringBuffer();
			for(int i=0; i<c_list.size(); i++)
				uri_list.append( ((uri_list.length()>0)?", ":"") + "'" + c_list.get(i).getURI() + "'");

			um_stmt = um_conn.createStatement();
			um_rs = um_stmt.executeQuery(
					"( SELECT DISTINCT c.Title, a1.URI  " +
					"FROM um2.ent_activity a1  " +
					"JOIN um2.rel_concept_activity ca ON (ca.ActivityID=a1.ActivityID AND a1.URI " + 
					"IN(" + uri_list.toString() + ") ) " +
					"JOIN um2.ent_concept c ON(c.ConceptID=ca.ConceptID)  " +
					"JOIN um2.rel_domain_concept dc ON(dc.ConceptID=ca.ConceptID AND dc.DomainID=1) ) " +
					"UNION ALL " +
					"( SELECT DISTINCT c.Title, a1.URI " +
					"FROM um2.ent_activity a1 JOIN um2.rel_activity_activity aa ON(a1.ActivityID=aa.ParentActivityID AND a1.URI " +
					"IN(" + uri_list.toString() + ") ) JOIN um2.ent_activity a2 ON(aa.ChildActivityID=a2.ActivityID) " +
					"JOIN um2.rel_concept_activity ca ON (ca.ActivityID=a2.ActivityID) " +
					"JOIN um2.rel_domain_concept dc ON(dc.ConceptID=ca.ConceptID AND dc.DomainID=1) " +
					"JOIN um2.ent_concept c ON(c.ConceptID=ca.ConceptID) ); "
			);

			while(um_rs.next())
			{
				String title = um_rs.getString("Title");
				String uri = um_rs.getString("URI");
				OAI_PMH_Item res_item = c_list.findByURI(uri);
				if(res_item!=null)
					res_item.metadata += ((res_item.metadata.length()>0)?",":"") + title;
			}
			um_rs.close(); um_rs = null;
			um_stmt.close(); um_stmt = null;
			
			
			kt_conn.close();	kt_conn = null;
			um_conn.close();	um_conn = null;
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace(System.out);
		}
		
		// PRINTOUT
		PrintWriter out = response.getWriter();
		response.setContentType("text/xml; charset=utf-8");
		
//		out.println(
//			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" +
//			"<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/static-repository\"" + "\n" +
//			"		xmlns:oai=\"http://www.openarchives.org/OAI/2.0/\"" + "\n" +
//			"		xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + "\n" +
//			"		xmlns:dc =\"http://purl.org/dc/elements/1.1/\"" + "\n" +
//			"		xmlns:dcterms =\"http://purl.org/dc/terms/\"" + "\n" +
//			"		xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/static-repository" + "\n" +
//			"				http://www.openarchives.org/OAI/2.0/static-repository.xsd\">" + "\n" +
//			"	<Identify>" + "\n" +
//			"		<oai:repositoryName>University of Pittsburgh, Schoolf of Information Sciences, PAWS/TALER Repository</oai:repositoryName>" + "\n" +
//			"		<oai:baseURL>http://adapt2.sis.pitt.edu/kt/oai-pmh-ensemble.xml</oai:baseURL>" + "\n" +
//			"		<oai:protocolVersion>2.0</oai:protocolVersion>" + "\n" +
////			"		<oai:adminEmail>paws@pitt.edu</oai:adminEmail>" + "\n" +
//			"		<oai:adminEmail>myudelson@gmail.com</oai:adminEmail>" + "\n" +
//			"		<oai:earliestDatestamp>2008-02-29</oai:earliestDatestamp>" + "\n" +
//			"		<oai:deletedRecord>no</oai:deletedRecord>" + "\n" +
//			"		<oai:granularity>YYYY-MM-DD</oai:granularity>" + "\n" +
//			"	</Identify>" + "\n" +
//			"	<ListMetadataFormats>" + "\n" +
//			"		<oai:metadataFormat>" + "\n" +
//			"			<oai:metadataPrefix>oai_dc</oai:metadataPrefix>" + "\n" +
//			"			<oai:schema>http://www.openarchives.org/OAI/2.0/oai_dc.xsd</oai:schema>" + "\n" +
//			"			<oai:metadataNamespace>http://www.openarchives.org/OAI/2.0/oai_dc/</oai:metadataNamespace>" + "\n" +
//			"		</oai:metadataFormat>" + "\n" +
//			"	</ListMetadataFormats>" + "\n");
//

		out.println(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" +
				"<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\"" + "\n" +
				"		xmlns:oai=\"http://www.openarchives.org/OAI/2.0/\"" + "\n" +
				"		xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + "\n" +
				"		xmlns:dc =\"http://purl.org/dc/elements/1.1/\"" + "\n" +
				"		xmlns:dcterms =\"http://purl.org/dc/terms/\"" + "\n" +
				"		xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/static-repository" + "\n" +
				"				http://www.openarchives.org/OAI/2.0/static-repository.xsd\">" + "\n" +
				"	<Identify>" + "\n" +
				"		<repositoryName>University of Pittsburgh, Schoolf of Information Sciences, PAWS/TALER Repository</repositoryName>" + "\n" +
				"		<baseURL>http://adapt2.sis.pitt.edu/kt/oai-pmh-ensemble.xml</baseURL>" + "\n" +
				"		<protocolVersion>2.0</protocolVersion>" + "\n" +
				"		<adminEmail>paws@pitt.edu</adminEmail>" + "\n" +
				"		<earliestDatestamp>2008-02-29</earliestDatestamp>" + "\n" +
				"		<deletedRecord>no</deletedRecord>" + "\n" +
				"		<granularity>YYYY-MM-DD</granularity>" + "\n" +
				"	</Identify>" + "\n" +
				"	<ListMetadataFormats>" + "\n" +
				"		<metadataFormat>" + "\n" +
				"			<metadataPrefix>oai_dc</metadataPrefix>" + "\n" +
				"			<schema>http://www.openarchives.org/OAI/2.0/oai_dc.xsd</schema>" + "\n" +
				"			<metadataNamespace>http://www.openarchives.org/OAI/2.0/oai_dc/</metadataNamespace>" + "\n" +
				"		</metadataFormat>" + "\n" +
				"	</ListMetadataFormats>" + "\n");

		
		// <!-- SQL -->
		StringBuffer xml_list = new StringBuffer();
		for(int i=0; i<sql_list.size(); i++)
			xml_list.append( sql_list.get(i).toString() );
		out.println( "<!-- SQL -->" );
		out.println( "	<ListRecords metadataPrefix=\"oai_dc\">" + "\n" );		
		out.println( xml_list.toString() );
		out.println( "	</ListRecords>" + "\n" );
	
		// <!-- Java -->
		xml_list = new StringBuffer();
		for(int i=0; i<java_list.size(); i++)
			xml_list.append( java_list.get(i).toString() );
		out.println( "<!-- Java -->" );
		out.println( "	<ListRecords metadataPrefix=\"oai_dc\">" + "\n" );		
		out.println( java_list.toString() );
		out.println( "	</ListRecords>" + "\n" );
//System.out.println("js " + java_list.size());
		
		// <!-- C -->
		xml_list = new StringBuffer();
		for(int i=0; i<c_list.size(); i++)
			xml_list.append( c_list.get(i).toString() );
		out.println( "<!-- C -->" );
		out.println( "	<ListRecords metadataPrefix=\"oai_dc\">" + "\n" );		
		out.println( c_list.toString() );
		out.println( "	</ListRecords>" + "\n" );
		
		out.println( "</OAI-PMH>");
		out.close();
		
	}
}

class OAI_PMH_Item extends Item2
{ 
	static final String OAI_ID_PREFIX = "oai:pittsburgh:paws:";

	int itemtype;
	String url, user, date, metadata, oai_id, tabs, topic;
	
	public OAI_PMH_Item(int a_itemtype, String a_title, String a_uri, String a_url, String a_user, String a_date, String a_topic)
	{
		super(a_itemtype, a_title, a_uri);
		url = a_url;
		user = a_user;
		date = a_date;
		topic = a_topic;
		metadata = "";
		tabs = "\t\t";
		
		// produce oai_id suffix
		switch(a_itemtype)
		{
			case 11: // QuizPACK
				oai_id = "quizpack/" + getURI().substring(getURI().indexOf('#')+1);
			break;
			case 14: // C WebEx
				oai_id = "webex/" + getURI().substring(getURI().indexOf('#')+1);  //. replaceAll("http://adapt2.sis.pitt.edu/webex/webex.rdf#", "");
			break;
			case 25: // SQL WebEx
				oai_id = "webex/" + getURI().substring(getURI().indexOf('#')+1);  //. replaceAll("http://adapt2.sis.pitt.edu/webex/webex.rdf#", "");
			break;
			case 28: // SQL KnoT
				oai_id = "sqlknot/" + getURI().substring(getURI().indexOf('?')+1). replaceAll("=","_"). replaceAll("&","__");
			break;
			case 30: // Java WebEx
				oai_id = "webex/" + getURI().substring(getURI().indexOf('#')+1);  //. replaceAll("http://adapt2.sis.pitt.edu/webex/webex.rdf#", "");
			break;
			case 32: // QuizJET
				oai_id = "quizjet/" + getURI().substring(getURI().indexOf('=')+1);
			break;
			
		}
		
	}
	
	public String toString()
	{
//		OAI_ID_PREFIX
		return 
		tabs + "<oai:record>" + "\n" + 
		tabs + "	<oai:header>" + "\n" + 
		tabs + "		<oai:identifier>" + OAI_ID_PREFIX + oai_id + "</oai:identifier>"  + "\n" + 
		tabs + "		<oai:datestamp>" + date.substring(0, 10) + "</oai:datestamp>"  + "\n" +
		tabs + "	</oai:header>"  + "\n" +
		tabs + "	<oai:metadata>"  + "\n" +
		tabs + "		<oai_dc:dc"  + "\n" +
		tabs + "			xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\"" + "\n" + 
		tabs + "			xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""  + "\n" +
		tabs + "			xmlns:dc =\"http://purl.org/dc/elements/1.1/\"" + "\n" +
		tabs + "			xmlns:dcterms =\"http://purl.org/dc/terms/\"" + "\n" +
		tabs + "			xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/"  + "\n" +
		tabs + "				http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">"  + "\n" +
		tabs + "			<dc:title dc:language=\"eng\">" + getTitle().replaceAll("&", "&amp;") + "</dc:title>"  + "\n" +
		tabs + "			<dc:creator>" + user + "</dc:creator>"  + "\n" +
		tabs + "			<dc:date>" + date.substring(0, 10) + "</dc:date>"  + "\n" +
//		tabs + "			<dc:subject>Digital Libraries</dc:subject>"  + "\n" +
		tabs + "			<dc:description>" + metadata + "</dc:description>"  + "\n" + 
//		tabs + "			<dc:identifier>" + getURI() + "</dc:identifier>"  + "\n" + 
		tabs + "			<dc:format xsi:type=\"dcterms:IMT\">text/html</dc:format>"  + "\n" + 
		tabs + "			<dc:language>eng</dc:language>"  + "\n" + 
		tabs + "			<dc:subject>" + topic + "</dc:subject>"  + "\n" + 
		tabs + "			<dc:identifier xsi:type=\"dcterms:URI\">" + url.replaceAll("&", "&amp;") + "&amp;grp=meta_group&amp;usr=meta_ensemble" + "</dc:identifier>"  + "\n" +
//		tabs + "			<dc:identifier xsi:type=\"dcterms:URI\">" + url + "&grp=meta_group&usr=meta_ensemble" + "</dc:identifier>"  + "\n" +
//		tabs + "			<dc:type xsi:type=\"dcterms:DCMIType\">InteractiveResource</dc:type>"  + "\n" + 
//		tabs + "			<dc:type>http://purl.org/dc/dcmitype/InteractiveResource</dc:type>"  + "\n" + 
		tabs + "			<dc:type>InteractiveResource</dc:type>"  + "\n" + 
		
		tabs + "		</oai_dc:dc>"  + "\n" +
		tabs + "	</oai:metadata>"  + "\n" +
		tabs + "</oai:record>"  + "\n";
	}
} 



