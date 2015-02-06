package edu.pitt.sis.paws.kt2.dbpersistors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DbConnectionPortalTest2 implements DbConnection {
	
	private DataSource daraSource = null;
	private Connection connection = null;
	private PreparedStatement statement = null;
	private ResultSet rSet = null;

	public DbConnectionPortalTest2() {
		try {
			Context context = new InitialContext();
			daraSource = (DataSource) context.lookup("java:/comp/env/jdbc/portalTest2");
			context.close();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.sis.paws.gboard.dbpersistors.DbConnection#executeStatement(java.lang.String, java.util.HashMap)
	 */
	public ResultSet executeStatement(String query, HashMap<Integer, String> param) {
		closeResources();
		try {
			connection = daraSource.getConnection();
			statement = connection.prepareStatement(query);
			if (param != null) {
				for (Integer index: param.keySet()) {
					statement.setString(index, param.get(index));
				}
			}
			rSet = statement.executeQuery();
					
			return rSet;
		} catch (SQLException e) {
			System.out.println("\nPersistor Exception while executing: "+query+" (with parameters: "+((param == null) ? "" :param.toString())+")");
			e.printStackTrace();
			closeResources();
			return null;
		}
    }
		    
    /* (non-Javadoc)
	 * @see edu.pitt.sis.paws.gboard.dbpersistors.DbConnection#executeUpdate(java.lang.String, java.util.HashMap)
	 */
    public boolean executeUpdate(String query, HashMap<Integer, String> param) {
    	Connection updateConnection = null;
		PreparedStatement updateStatement = null;
    	try {
    		updateConnection = daraSource.getConnection();
    		updateStatement = updateConnection.prepareStatement(query);
			if (param != null) {
				for (Integer index: param.keySet()) {
					updateStatement.setString(index, param.get(index));
				}
			}
			updateStatement.executeUpdate();
			return true;
		} catch (SQLException e) {
			System.out.println("\nPersistor Exception while executing: "+query+" (with parameters: "+((param == null) ? "" :param.toString())+")");
			e.printStackTrace();
			return false;
		} finally{
			close(updateStatement);
			close(updateConnection);
        }
    }
    
    /* (non-Javadoc)
	 * @see edu.pitt.sis.paws.gboard.dbpersistors.DbConnection#executeUpdateGetID(java.lang.String, java.util.HashMap)
	 */
    public Integer executeUpdateGetID(String query, HashMap<Integer, String> param) {
    	Connection updateConnection = null;
		PreparedStatement updateStatement = null;
		ResultSet rs = null;
    	try {
    		updateConnection = daraSource.getConnection();
    		updateStatement = updateConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			if (param != null) {
				for (Integer index: param.keySet()) {
					updateStatement.setString(index, param.get(index));
				}
			}
			updateStatement.executeUpdate();
			
			rs = updateStatement.getGeneratedKeys();
			Integer insertID = null;
            if(rs.next())
            {
            	insertID = rs.getInt(1);
            }
            
			return insertID;
		} catch (SQLException e) {
			System.out.println("\nPersistor Exception while executing: "+query+" (with parameters: "+((param == null) ? "" :param.toString())+")");
			e.printStackTrace();
			return null;
		} finally{
			close(rs);
			close(updateStatement);
			close(updateConnection);
        }
    }
    
    /* (non-Javadoc)
	 * @see edu.pitt.sis.paws.gboard.dbpersistors.DbConnection#closeResources()
	 */
    public void closeResources() {
    	close(rSet);
		close(statement);
		close(connection);
    }
    
    private void close(ResultSet set) {
    	if (set != null) {
    		try {
    			set.close();
    		} catch (SQLException e) {
    			System.out.println("Exception in closing DB result set");
    		}
    	}
    }
    
    private void close(PreparedStatement statement) {
    	if (statement != null) {
    		try {
    			statement.close();
    		} catch (SQLException e) {
    			System.out.println("Exception in closing DB statement");
    		}
    	}
    }
    
    private void close(Connection connection) {
    	if (connection != null) {
    		try {
    			connection.close();
    		} catch (SQLException e) {
    			System.out.println("Exception in closing DB connection");
    		}
    	}
    }
}
