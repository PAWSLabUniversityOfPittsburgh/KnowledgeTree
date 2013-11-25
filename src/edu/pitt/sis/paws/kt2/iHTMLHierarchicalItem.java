/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */

/** Interface in intended to wrap all of the entities that can be used as a node
 * in a portal tree: folders, untyped nodes, and nodes with special types.
 * @author Michael V. Yudelson
 */

package edu.pitt.sis.paws.kt2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import edu.pitt.sis.paws.core.*;

/** Interface in intended to wrap all of the entities that can be used as a node
 * in a portal tree: folders, untyped nodes, and nodes with special types.
 * @author Michael V. Yudelson
 */
public interface iHTMLHierarchicalItem<iHIType extends iItem2> extends iHierarchicalItem<iHIType>
{
	// CONSTANTS
	//    Display mode
	public static final int HHTMLITEM_CONTENT_GREEDY_TRACE = 1;
	public static final int HHTMLITEM_CONTENT_FULL_NOTRACE = 2;
	public static final int HHTMLITEM_TRACE_NO = 4;
	public static final int HHTMLITEM_TRACE_YES = 8;
	public static final int HHTMLITEM_SHOW_REGULAR = 16;
	public static final int HHTMLITEM_SHOW_CHECKBOX = 32;
	public static final int HHTMLITEM_SHOW_CHECKBOX_LOCKED = 64;
	public static final int HHTMLITEM_SHOW_RADIO = 128;
	public static final int HHTMLITEM_SHOW_NOLINK = 256;

	//	public void outputSelf(JspWriter out, HttpServletRequest request, 
	//		int level, int display_mode, User user) throws IOException;

	public void outputTree(JspWriter out, HttpServletRequest request, 
				iHTMLHierarchicalItem<iHIType> current_item, int level, int display_mode, 
				User user, User group, ResourceMap resmap, 
				String jsAction) throws IOException;
}
