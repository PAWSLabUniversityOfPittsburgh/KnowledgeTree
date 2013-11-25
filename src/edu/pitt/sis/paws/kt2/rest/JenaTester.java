package edu.pitt.sis.paws.kt2.rest;

import java.io.*;
import java.util.*;
//import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.db.*;

//import edu.pitt.sis.paws.kt.*;
//import edu.pitt.sis.paws.core.utils.*;

/**
 * Servlet implementation class for Servlet: JenaTester
 *
 */
 public class JenaTester extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet
 {
	 static final long serialVersionUID = 2L;
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public JenaTester()
	{
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Calendar start = null;
		Calendar finish = null;
		long diff_mills;
		PrintWriter out = response.getWriter();
		
		String inputFileName = "http://localhost:8080/webex/webex.rdf";
//		String inputFileName = "http://www.sis.pitt.edu/~paws/ont/quizpack.rdf";
		
		start = new GregorianCalendar();		
		Model model1 = ModelFactory.createDefaultModel();
		InputStream in = FileManager.get().open( inputFileName );
		if (in == null)
		{
			throw new IllegalArgumentException("File: " + inputFileName + " not found");
		}
		model1. read(in, "");
		in.close();
		finish = new GregorianCalendar();
		diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
		
//		model.write(System.out);
		out.println("URL opened in " + diff_mills + "ms size=" + model1.size());
		
		String className =	"com.mysql.jdbc.Driver";         // path of driver class
		String DB_URL =		"jdbc:mysql://localhost/jenatest?useUnicode=yes&characterEncoding=utf8";  // URL of database 
		String DB_USER =	"student";                          // database user id
		String DB_PASSWD =	"student";                         // database password
		String DB =			"MySQL"; 
		
		IDBConnection jena_conn = null;
		boolean empty = true;
		long size = 0;
		
//		start = new GregorianCalendar();
//		try
//		{
//			Class.forName (className);                          // Load the Driver
//			jena_conn = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
//			Model model_db = ModelRDB.createModel(jena_conn, "jt");
//			model_db.add(model1);
////			model_db.write(System.out);
//			size = model_db.size();
//			empty = !model_db.isEmpty();
//			jena_conn.close();
//		}
//		catch(Exception e) { e.printStackTrace(out);}
//		finish = new GregorianCalendar();
//		diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
//		out.println("Model written to DB in " + diff_mills + "ms (" + empty + ") size=" + size);
		
		start = new GregorianCalendar();
		try
		{
			Class.forName (className);                          // Load the Driver
			jena_conn = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
			Model model_db = ModelRDB.open(jena_conn,"jt");
			empty = !model_db.isEmpty();
			size = model_db.size();
//			model_db.write(System.out);
			jena_conn.close();
		}
		catch(Exception e) { e.printStackTrace(out);}
		finish = new GregorianCalendar();
		diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
		out.println("Model read from DB in " + diff_mills + "ms (" + empty + ") size=" + size);
		
		
		
		out.close();
	}  	  	  	    
}
 
 
 
 
 
 
