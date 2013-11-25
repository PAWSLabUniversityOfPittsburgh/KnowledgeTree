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

import edu.pitt.sis.paws.core.*;

public class Right extends Item
{
	final static long serialVersionUID = 2L;
	
	// Right Type Macros & Constants
	public static final int RIGHT_TYPE_NONE = 1;  // MACRO
	public static final int RIGHT_TYPE_ALL = 2;  // MACRO

	public static final int RIGHT_TYPE_VIEW = 3;
	public static final int RIGHT_TYPE_EDIT = 4;
	public static final int RIGHT_TYPE_ADD = 5;
	public static final int RIGHT_TYPE_VIEW_AUTHOR = 6;
	public static final int RIGHT_TYPE_COPY = 7;
	public static final int RIGHT_TYPE_DELETE = 8;
	public static final int RIGHT_TYPE_VIEW_RATING = 9;
	public static final int RIGHT_TYPE_RATE = 10;
	
	// User ID Macros & Constants
	public static final int USER_ALL = 1;  // MACRO
	public static final int USER_AUTHOR = 2;  // MACRO
	// Quantity Macros & Constants
	public static final int QUANTITY_UNLIM = -1;

	// id field - Node ID
	// title field - Desciption field
	
	protected User user; // if null - macro (check user_macro)
	protected int user_macro; // user macro code
	protected int right_type;
	protected iNode node; // if null - macro (check node_macro)
	protected int parent_type;
	protected int child_type;
	protected int quantity;
	protected String description;

	protected boolean ownerFlag;
	
	// new constructor
	public Right(int _node_id, String _desc, User _user, int _user_macro, int _right_type, iNode _node, 
			int _parent_type, int _child_type, int _quantity, boolean _ownerFlag)
	{
		super(_node_id, _desc);
		user = _user;
		user_macro = _user_macro;
		right_type = _right_type;
		node = _node;
		parent_type = _parent_type;
		child_type = _child_type;
		quantity = _quantity;
		ownerFlag = _ownerFlag;
	}
	
	
	// constructor for cloning
	public Right(int _user_macro, int _right,/*	int _node_macro, */int _parent,
		int _child, int _quant, String _desc, boolean _ownrFlag)
	{
		user = null;
		user_macro = _user_macro;
		right_type = _right;
		node = null;
//		node_macro = _node_macro;
		parent_type = _parent;
		child_type = _child;
		quantity = _quant;
		description = _desc;
		ownerFlag = _ownrFlag;
	}

	public Right(User _user, int _user_macro, int _right, iNode _node, 
		/*int _node_macro, */int _parent, int _child, int _quant, String _desc,
		boolean _ownrFlag)
	{
		user = _user;
		user_macro = _user_macro;
		right_type = _right;
		node = _node;
//		node_macro = _node_macro;
		parent_type = _parent;
		child_type = _child;
		quantity = _quant;
		description = _desc;
		ownerFlag = _ownrFlag;
	}

	
	
	public Right clone() 
	{
		Right copy = null;
		try
		{
			copy = new Right(this.user_macro, this.right_type, 
				/*this.node_macro,*/ this.parent_type, this.child_type,
				this.quantity, new String(this.description),
				this.ownerFlag);
		}
		catch (Exception e) { System.err.println(e.toString()); }
		return copy;
	}

	public void setUser(User _user) { user = _user; }
	public User getUser() { return user; }

	public void setNode(iNode _node) { node = _node; }
	public iNode getNode() { return node; }
	
	public int getUserMacro() { return user_macro; }

	public int getRightType() { return right_type; }

	public int getParentType() { return parent_type; }

	public int getChildType() { return child_type; }
	
	public int getQuantity() { return quantity; }
	
	public boolean getOwnerFlag() { return ownerFlag; }

	/** This function checks whether operation of a certain type is allowed
	 * by this right.
	 */
	public boolean isAllowedWhat(int _right_type)
	{
		if( (this.right_type == _right_type) || 
			(this.right_type == Right.RIGHT_TYPE_ALL) )
			return true;
		else
			return false;
	}
	
	/** This function checks whether access of a certain user/group is
	 * regulated by this right.
	 */
	public boolean isAllowedWho(int _user_id, boolean ini_authorship)
	{
		boolean result = false;
//System.out.println("Right.isAllowedWho user_macro = " + user_macro);
//System.out.println("Right.isAllowedWho check _user_id = " + _user_id);
//if(user != null)System.out.println("Right.isAllowedWho this.user_id " + (this.user.getId()));
//System.out.println("Right.isAllowedWho ini_authorship " + ini_authorship);

//System.out.println("Right.isAllowedWho (user == null) " + (user == null));
//if(user != null)System.out.println("Right.isAllowedWho user_id " + user.getId());
		if( (this.user_macro == Right.USER_ALL) || 
			( (this.user_macro != Right.USER_AUTHOR) && (this.user.getId() == _user_id) ) ||
			( (this.user_macro == Right.USER_AUTHOR) /*&& (this.user.getId() == _user_id) */&& ini_authorship) )
			result =  true;
//System.out.println("Right.isAllowedWho result " + result);
		return result;
	}

	/** This function checks whether access to a certain node is
	 * regulated by this right.
	 */
	public boolean isAllowedFor(int _node_id)
	{
		if(this.node.getId() == _node_id)
			return true;
		else
			return false;
	}
	/** This function checks whether access to a certain node is
	 * regulated a specified quantity.
	 */
	public boolean isAllowedHow(int _quantity)
	{
		if(this.quantity == _quantity)
			return true;
		else
			return false;
	}
	
	/** This function checks whether parent node type can be regulated by
	 * this right.
	 */
	public boolean isAllowedFrom(int _parent_node_type)
	{
//System.out.println("\t Right.isAllowedFrom checking this.parent_type=" + this.parent_type + " vs " + _parent_node_type);
		if( (this.parent_type == iNode.NODE_TYPE_I_ALL) || 
			(this.parent_type == _parent_node_type) )
			return true;
		else
			return false;
	}
	
	/** This function checks whether child node type can be regulated by
	 * this right.
	 */
	public boolean isAllowedTo(int _child_node_type)
	{
		if( (this.child_type == iNode.NODE_TYPE_I_ALL) || 
			(this.child_type == _child_node_type) )
			return true;
		else
			return false;
	}
	
	/** This function checks whether right allowes certain quantity of the 
	 * action.
	 */
	public boolean isAllowedQuant(int _quant)
	{
		if( (this.quantity > _quant) || 
			(this.quantity == -1) )
			return true;
		else
			return false;
	}
	
	// compositional methods
	
	/** This function checks whether operation of a certain type is allowed
	 * to a certain node for a certain user.
	 */
	public boolean isAllowedWhatWhoFor(int _right_type, int _user_id, 
		int _node_id, boolean ini_authorship)
	{
		// deleting, copying and rating of the root is not allowed
		if( isAllowedWhat(_right_type) && 
			isAllowedWho(_user_id, ini_authorship) &&
			isAllowedFor(_node_id) )
			return true;
		else
			return false;
	}

	/** This function checks whether operation of a certain type is allowed
	 * to a certain node for a certain user and certain parent node type.
	 */
	public boolean isAllowedWhatWhoForFrom(int _right_type, int _user_id, 
		int _node_id, int _parent_node_type, boolean ini_authorship)
	{
		if( isAllowedWhatWhoFor(_right_type, _user_id, _node_id, ini_authorship) 
				&& isAllowedFrom(_parent_node_type) )
			return true;
		else
			return false;
	}

	/** This function checks whether operation of a certain type is allowed
	 * to a certain node for a certain user and certain parent and child node
	 * types.
	 */
	public boolean isAllowedWhatWhoForFromTo(int _right_type, int _user_id, 
		int _node_id, int _parent_node_type, int _child_node_type,
		boolean ini_authorship)
	{
//System.out.println("\t Right.isAllowedWhatWhoForFromTo " +
//"this " + this.right_type + ", " + ((this.user != null)?this.user.getId():"userNULL") + ", " + 
//((this.node != null)?this.node.getId():"nodeNULL") + ", " + this.parent_type + ", " + this.child_type + 
//"  -VS-  " + _right_type + ", " +  _user_id + ", " + _node_id + ", " + _parent_node_type + ", " + _child_node_type);	
//
//System.out.println("\t\t isAllowedWhat(_right_type) " + isAllowedWhat(_right_type));
//System.out.println("\t\t isAllowedWho(_user_id, ini_authorship) " + isAllowedWho(_user_id, ini_authorship) );
//System.out.println("\t\t isAllowedFor(_node_id) " + isAllowedFor(_node_id));
//System.out.println("\t\t isAllowedFrom(_parent_node_type) " + isAllowedFrom(_parent_node_type));
//System.out.println("\t\t isAllowedTo(_child_node_type) " + isAllowedTo(_child_node_type));
		if( isAllowedWhatWhoForFrom(_right_type, _user_id, 
				_node_id, _parent_node_type, ini_authorship) && 
			isAllowedTo(_child_node_type) && 
			!((_right_type == Right.RIGHT_TYPE_DELETE || 
					_right_type == Right.RIGHT_TYPE_COPY /*||	_right_type == Right.RIGHT_TYPE_RATE*/ ) 
					&&(_node_id == 1) && _child_node_type == iNode.NODE_TYPE_I_ALL )
			)
		{
//System.out.println("\t\t = true");
			return true;
		}
		else
		{
//System.out.println(" = false");
			return false;
		}
	}

	/** This function checks whether operation of a certain type is allowed
	 * to a certain node for a certain user and certain parent and child node
	 * types and a certain quantity.
	 */
	public boolean isAllowedWhatWhoForFromToQuant(int _right_type, int _user_id, 
		int _node_id, int _parent_node_type, int _child_node_type,
		boolean ini_authorship, int _quant)
	{
//System.out.print("\t Right.isAllowedWhatWhoForFromTo " +
//"this " + this.right_type + ", " + this.user.getId() + ", " + 
//this.node.getId() + ", " + this.parent_type + ", " + this.child_type + 
//"  -VS-  " + _right_type + ", " +  _user_id + ", " + _node_id + ", " + _parent_node_type + ", " + _child_node_type);	
		if( isAllowedWhatWhoForFromTo(_right_type, _user_id, _node_id, 
				_parent_node_type, _child_node_type,
				ini_authorship) && isAllowedQuant(_quant))
		{
//System.out.println(" = true");
			return true;
		}
		else
		{
//System.out.println(" = false");
			return false;
		}
	}

}
