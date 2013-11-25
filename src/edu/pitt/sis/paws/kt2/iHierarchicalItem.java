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

import edu.pitt.sis.paws.core.*; 

public interface iHierarchicalItem<iHIType extends iItem2> extends iItem2
{
	
	public void setParent(iHIType _parent);
	public iHIType getParent();
	public OrderedWeightedItem2Vector<iHIType> getChildren();
	public int getChildCountOfTypeByUser(int _node_type, int _user_id);
	
	public boolean getExpanded();
	public void setExpanded(boolean _expanded);
	public boolean expandParents(boolean _res);
}