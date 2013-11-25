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

import java.sql.*;
import javax.servlet.http.*;


public interface iDBStored 
{
	public void saveToDB(Connection conn, HttpServletRequest request, iNode node, int changes)
			throws Exception;
	public int addToDB(Connection conn, HttpServletRequest request, iNode node)
			throws Exception;
	public int updateObject(HttpServletRequest request) throws Exception;
	
	public boolean isStoredInDB();
	public void setStoredInDB(boolean _is_stored);
}