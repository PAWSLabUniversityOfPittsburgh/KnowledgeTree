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

import java.util.*;

import edu.pitt.sis.paws.core.*;

public class User extends Item2
{
	final static long serialVersionUID = 2L;
	
	protected String login;
	protected boolean isGroup;
	
	private Item2Vector<User> sub_ordinates; // here users of a group
	private Item2Vector<User> super_ordinates; // here groups of a user

//	protected ItemVector<iConcept> concepts; // descriptive concepts

	private Vector<Right> rights;
	
	/** Ratings of the nodes
	 * @since 1.5
	 */
	protected Vector<Rating> ratings;

	public User(int _id, String _title, String _uri, String _login, boolean is_group) // group creation - BOTH
	{
		super(_id, _title, _uri);
//		id = _id;
//		title = _title;
		login = _login;
		isGroup = is_group;
		sub_ordinates = new Item2Vector<User>();
		super_ordinates = new Item2Vector<User>();
		rights = new Vector<Right>();
//		concepts = new ItemVector<iConcept>();
		ratings = new Vector<Rating>();
	}
	
//	public User(int _id, String _title, String _login) // user creation
//	{
//		super(_id, _title);
//		title = _title;
//		login = _login;
//		isGroup = false;
//		sub_ordinates = new Item2Vector<User>();
//		super_ordinates = new Item2Vector<User>();
//		rights = new Vector<Right>();
//		concepts = new Item2Vector<iConcept>();
//		ratings = new Vector<Rating>();
//	}

//	public Object clone() 
//	{
//		User copy = null;
//		try
//		{
////			if(this.isGroup)
//				copy = new User(this.id,new String(this.title), new String(this.uri),
//					new String(this.login), this.isGroup);
////			else
////				copy = new User(this.id,new String(this.title),
////					new String(this.login));
//		}
//		catch (Exception e) { System.err.println(e.toString()); }
//		return copy;
//	}

	// Implementing the Item2 interface
//	public int getId() { return id; }
//	public String getTitle() { return title; }
//	public void setTitle(String _title) { title = _title; }
//	public int compareTo(Object e) { return title.compareTo(((Node)e).title); }
//	public ItemVector<iConcept> getConcepts() { return concepts; }
	
	public String getLogin() { return login; }
	public void setLogin(String _login)
	{
		if(!isGroup) login = _login;
		else System.out.println("!!! [KTree2] Error! User id " + this.getId() + " is a group. Cannot set login!");
	}

	public Item2Vector<User> getSuperordinates() { return super_ordinates; }
	public Item2Vector<User> getSubordinates() { return sub_ordinates; }

	public Vector<Right> getRights() { return rights; } 
	
	public boolean getIsGroup() { return isGroup; }

	public String toString()
	{
		return "[ User title:'" + this.getTitle() + "' id:" + this.getId() + 
			((isGroup)?(" group of " + sub_ordinates.size() + " members"):(" login: " + login)) + "]"; 
	}

	/** Method checks is a specified user (identifiend by a user id) is a member of this group provided this is a group, otherwise auto reply is set to false
	 * @param user_id id of the object to test the membership of
	 * @return true if specified user (identifiend by a user id) is a member of this group, false otherwise 
	 * @since 1.5
	 */
	public boolean isMemberOf(int user_id)
	{
		if(isGroup)
			return (this.getSubordinates().findById(user_id) != null);
		else
			return false;
	}

	/** Method checks is a specified user is a member of this group provided this is a group, otherwise auto reply is set to false
	 * @param user object to test the membership of
	 * @return true if specified user is a member of this group, false otherwise
	 * @since 1.5
	 */
	public boolean isMemberOf(User user)
	{
		return isMemberOf(user.getId());
	}
	
	/** Method returns group's set of ratings
	 * @return group's set of ratings
	 * @since 1.5
	 */
	public Vector<Rating> getRatings() { return ratings; }
		
	
}
