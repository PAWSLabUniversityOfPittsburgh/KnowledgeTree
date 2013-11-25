/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */
 
/** This Interface declares a behavior of any strictly hierarchical object that 
 * has a single parent and multople children
 * @author Michael V. Yudelson
 */

package edu.pitt.sis.paws.kt2;

import java.io.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

public interface iHTMLRepresentable
{
	// Client extension 
	public void showViewHeader(JspWriter out, HttpServletRequest request)
		throws IOException;
	public void showView(JspWriter out, HttpServletRequest request/*, boolean show_ratings*/)
		throws IOException;
	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException;
	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException;
		
}