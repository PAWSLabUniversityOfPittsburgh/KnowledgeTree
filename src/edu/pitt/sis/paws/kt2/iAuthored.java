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

/** This interface wrapps the functionality of an class that can have a
 * property if bening authored
 */
public interface iAuthored
{
	public boolean isCreatedBy(int user_id);
	public boolean isCreatedBy(User _user);
	public User getCreator();
	public void setCreator(User _user);
	
	/**
	 * The name of a user who created the resourse and the one who placed the resource at a given place (if different from creator) 
	 * @return
	 */
	public String getCreatorAdderNames();
}