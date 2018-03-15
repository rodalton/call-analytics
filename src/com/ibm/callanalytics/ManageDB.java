package com.ibm.callanalytics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class ManageDB {

	Connection conn = null; 
	ResultSet rs = null; 
	String username; 
	String password; 
	String url; 
	
	public ManageDB(){
		//Get dashDB credentials & connection info 
		Map<String, String> credentials = VCAPHelper.getDbCreds();
		username = credentials.get("username").toString(); 
		password = credentials.get("password").toString(); 
		url = credentials.get("jdbcurl").toString();
	}

	public Connection connect() {
		try{
			String myDriver = "com.ibm.db2.jcc.DB2Driver";
				
			Class.forName(myDriver);
			conn = DriverManager.getConnection(url, username, password);			
		}
		catch (Exception e) {
			e.printStackTrace();
		}        
		return conn;
	}

	public int addCall(String time, String date, int length) {
		int call_id = 0; 

		try{ 
			String query = " INSERT INTO CALLS (CALL_TIME, CALL_DATE, CALL_LENGTH)"
					+ " VALUES ('" + time + "', '" + date + "', " + length + ")";

			// Use executeUpdate to return the generated key 
			Statement statement = conn.createStatement();
			statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

			rs = statement.getGeneratedKeys();

			while (rs.next()) {
				call_id = rs.getInt(1);     
			}

			conn.close();
		}
		catch(Exception e){
			System.out.println(e.getMessage());		
		}
		//Return the generated primary key for a new call
		return call_id; 
	}

	public void insertUtterance(String speaker, String utterance, String tone, int call_id) {
		try {
			String insert = "INSERT INTO UTTERANCES (SPEAKER, UTTERANCE, TONE, CALL_ID)" 
					+ " VALUES(?,?,?,?)"; 

			PreparedStatement preparedStatement = conn.prepareStatement(insert);
			preparedStatement.setString(1, speaker);
			preparedStatement.setString(2, utterance);
			preparedStatement.setString(3, tone);
			preparedStatement.setInt(4, call_id);

			preparedStatement.executeUpdate();

			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertKeywords(String keyword, int call_id) {
		try {
			String insert = "INSERT INTO KEYWORDS (KEYWORD, CALL_ID)" 
					+ " VALUES(?,?)"; 

			PreparedStatement preparedStatement = conn.prepareStatement(insert);
			preparedStatement.setString(1, keyword);
			preparedStatement.setInt(2, call_id);

			preparedStatement.executeUpdate();

			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertEntities(String keyword, int call_id) {
		try {
			String insert = "INSERT INTO ENTITIES (ENTITY, CALL_ID)" 
					+ " VALUES(?,?)"; 

			PreparedStatement preparedStatement = conn.prepareStatement(insert);
			preparedStatement.setString(1, keyword);
			preparedStatement.setInt(2, call_id);

			preparedStatement.executeUpdate();

			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
