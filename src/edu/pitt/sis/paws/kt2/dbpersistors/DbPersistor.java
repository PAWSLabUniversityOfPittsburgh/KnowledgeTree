package edu.pitt.sis.paws.kt2.dbpersistors;

import java.sql.ResultSet;
import java.util.HashMap;

public class DbPersistor {
	
	public static final String GET_ALL_GROUPS = "SELECT u.UserID, u.Login, u.Name FROM ent_user u WHERE u.UserID>2 AND u.IsGroup=1;";
	public static final String GET_ALL_GROUPS_FOR_TEACHER = "SELECT u.UserID, u.Login, u.Name FROM ent_user u WHERE u.UserID>2 AND u.IsGroup=1 AND u.UserID IN (SELECT GroupID FROM rel_teacher_group WHERE TeacherID = ?);";
	public static final String GET_GROUP_BY_ID = "SELECT u.UserID, u.Login, u.Name FROM ent_user u WHERE u.UserID = ? AND IsGroup=1;";
	public static final String GET_GROUP_USERS = "SELECT u.UserID, u.Login, u.Name, u.EMail FROM rel_user_user uu, ent_user u WHERE uu.ParentUserID = ? AND uu.ChildUserID = u.UserID;";
	public static final String GET_GROUP_CONNECTED_APPS = "SELECT a.* FROM ent_user u JOIN rel_app_user au ON (u.UserID = au.UserID)"+
												" JOIN ent_app a ON(a.AppID = au.AppID) WHERE u.Login = ? AND a.Title NOT LIKE '%free spot%';";
	public static final String GET_ALL_APPS = "SELECT * FROM ent_app WHERE Title NOT LIKE '%free spot%';";
	public static final String ADD_APP_CONNECTION = "INSERT INTO rel_app_user (AppID, UserID) VALUES (?, ?);";
	public static final String CLEAN_UP_GROUP_APPS = "DELETE FROM rel_app_user WHERE UserID = ?;";
	public static final String GET_ALL_USERS = "SELECT u.UserID, u.Login, u.Name, u.EMail FROM ent_user u WHERE u.UserID>2 AND u.IsGroup <> 1;";

	public static final String CHECK_LOGIN = "SELECT * FROM ent_user WHERE Login = ?;";
	public static final String ADD_GROUP = "INSERT INTO ent_user (Login, Name, Pass, IsGroup, Sync, EMail, Organization, City, Country, How) VALUES (?, ?, '', 1, 1, '', '', '', '', '');";
	public static final String ASSIGN_GROUP_TO_TEACHER = "INSERT INTO rel_teacher_group (TeacherID, GroupID) VALUES (?, ?);";
	public static final String GET_GROUP = "SELECT * FROM ent_user WHERE IsGroup = 1 AND Login = ?;";
	public static final String GET_USER = "SELECT * FROM ent_user WHERE IsGroup = 0 AND Login = ?;";
	public static final String GET_USER_BY_LOGIN_OR_EMAIL = "SELECT * FROM ent_user WHERE (Login = ? OR EMail = ?) AND IsGroup = 0;";
	public static final String IS_USER_GROUP_MEMBER = "SELECT * FROM rel_user_user WHERE ParentUserID = ? AND ChildUserID = ?;";
	public static final String DELETE_USER_FROM_GROUP_PT2 = "DELETE FROM rel_user_user WHERE ParentUserID = ? AND ChildUserID = ?;";
	public static final String DELETE_USER_FROM_GROUP_UM2 = "DELETE FROM um2.rel_user_user WHERE GroupID = ? AND UserID = ?;";
	public static final String ADD_USER_TO_GROUP_PT2 = "INSERT INTO rel_user_user (ParentUserID, ChildUserID) VALUES (?, ?);";
	public static final String ADD_USER_TO_GROUP_UM2 = "INSERT INTO um2.rel_user_user (GroupID, UserID) VALUES (?, ?);";
	
	public static final String UPDATE_USER_PASSWORD = "UPDATE ent_user SET Pass = MD5( ? ) WHERE UserID = ?;";

	public static final String SEARCH_USER = "SELECT * FROM ent_user WHERE UserID>2 AND IsGroup <> 1 AND (Name LIKE ? OR Login LIKE ?) LIMIT 10;";
	
	public static final String GET_USER_FOR_SECURITY_CHECK = "SELECT * FROM ent_user WHERE Login = ? OR EMail = ?;";
	public static final String CHECK_USER_IS_ADMIN_BY_ID = "SELECT * FROM rel_user_user WHERE ChildUserID = ? AND "+
														"ChildUserID IN (SELECT ChildUserID FROM rel_user_user WHERE ParentUserID IN (SELECT UserID FROM ent_user WHERE Login = 'admins'));";
	
	public static final String ADD_NEW_USER = "INSERT INTO ent_user (Login, Name, Pass, IsGroup, Sync, EMail, Organization, City, Country, How, IsInstructor) VALUES (?, ?, MD5( ? ), 0, 1, ?, ?, ?, ?, '', ?);";
	public static final String ADD_NEW_USER_UM2 = "INSERT INTO ent_user (Login, Name, Pass, IsGroup, Sync, EMail, Organization, City, Country, How) VALUES (?, ?, MD5( ? ), 0, 1, ?, ?, ?, ?, '');";
	public static final String UPDATE_USER_INFO = "UPDATE ent_user SET Name = ?, EMail = ?, Organization = ?, City = ?, Country = ?, IsInstructor = ? WHERE Login = ?;";
	public static final String UPDATE_USER_INFO_UM2 = "UPDATE ent_user SET Name = ?, EMail = ?, Organization = ?, City = ?, Country = ? WHERE Login = ?;";
	
	public static final String GET_GROUP_COURSES = "SELECT c.course_id, c.course_name, g.term, g.year FROM ent_course c, ent_group g WHERE g.group_id = ? AND c.course_id = g.course_id;";
	public static final String GET_ALL_COURSES = "SELECT * FROM ent_course ORDER BY course_name ASC;";
	public static final String ADD_COURSE_TO_THE_GROUP = "INSERT INTO ent_group (group_id, group_name, course_id, creation_date, term, year) VALUES (?, ?, ?, NOW(), ?, ?);";
	
	
	private DbConnection connection = null;
	
	public DbPersistor(String databaseName) {
		if (databaseName.equalsIgnoreCase("portaltest2")) {
			connection = new DbConnectionPortalTest2();			
		} else if (databaseName.equalsIgnoreCase("um2")) {
			connection = new DbConnectionUm2();			
		} else if (databaseName.equalsIgnoreCase("aggregate")) {
			connection = new DbConnectionAggregate();			
		}
	}
	
	public void close() {
		if (connection != null) {
			connection.closeResources();
		}
	}
	
	public ResultSet persistData(String functionString, HashMap<Integer, String> param) {
		return connection.executeStatement(functionString, param);
	}
	
	public boolean persistUpdate(String functionString, HashMap<Integer, String> param) {
		return connection.executeUpdate(functionString, param);
	}
	
	public Integer persistUpdateGetID(String functionString, HashMap<Integer, String> param) {
		return connection.executeUpdateGetID(functionString, param);
	}

}
