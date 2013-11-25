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

public interface iNodePropertyValue extends iHTMLRepresentable
{
	public int getId();

	public String getStringValue();
	public void setStringValue(String _str_value);

	public int getIntValue();
	public void setIntValue(int _int_value);
	
	public User getUser();
	public void setUser(User _user);

	public iNodePropertyValue clone(User _user);
}