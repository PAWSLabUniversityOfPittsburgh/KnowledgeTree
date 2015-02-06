package edu.pitt.sis.paws.kt2.dbpersistors;

import java.sql.ResultSet;
import java.util.HashMap;

public interface DbConnection {

	public abstract ResultSet executeStatement(String query,
			HashMap<Integer, String> param);

	public abstract boolean executeUpdate(String query,
			HashMap<Integer, String> param);

	public abstract Integer executeUpdateGetID(String query,
			HashMap<Integer, String> param);

	public abstract void closeResources();

}