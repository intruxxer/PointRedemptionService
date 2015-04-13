package id.bri.switching.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;

import id.bri.switching.helper.PropertiesLoader;

public class Logging {
	
	private static Connection con;
	
	public Logging(){
		con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find the driver in the classpath!", e);
        }
        
        try {
            con = DriverManager.getConnection("jdbc:mysql://"+ PropertiesLoader.getProperty("DB_URL") 
            		+ ":"+ PropertiesLoader.getProperty("DB_PORT") +"/" + PropertiesLoader.getProperty("DB_NAME") + "?" 
            		+ "user=" + PropertiesLoader.getProperty("DB_USERNAME") +"&password=" + PropertiesLoader.getProperty("DB_PASSWORD")
            		);
            if(con != null){
            	System.out.println("Database connected via channel:"+String.valueOf(con));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect the database!", e);
        }
	}
	
	public void saveIsoMessage(String isomsg){
		try{
			int rows = 0;
	        String logQuery = "INSERT INTO logrequestpsw (isomessage) VALUES ('"+ isomsg +"')";
	        try {
	        	PreparedStatement prepStmt = con.prepareStatement(logQuery);
	            rows = prepStmt.executeUpdate();
	            if(rows > 0){
		        	System.out.println("ISO Message is saved with " + rows + " rows.");
		            if (prepStmt != null) { prepStmt.close(); }
		        }
	        } catch (SQLException e ) {
	        	e.printStackTrace();
	    	}    
		} catch(Exception e ) {
			e.printStackTrace();
		}
	}
	
	public void saveRedeemHistory(String[] history){
		try {	    	
			int rows = 0;
	        String logQuery = "INSERT INTO lbhst_redeem (" +
  				  "CPR_MTI, CPR_PROC_CODE, CPR_TERM_ID, CPR_BATCH_NBR, " +
  				  "CPR_TRX_DATE, CPR_TRX_TIME, CPR_REC_STATUS, CPR_MERCH_NBR, " +
  				  "CPR_CARDHOLDER_NBR, CPR_EXP_DATE, CPR_DESC_1, " +
  				  "CPR_CLCB_PROG, CPR_B063_SALES_AMT, CPR_B063_REDEEM_AMT, " +
  				  "CPR_B063_NET_SALES_AMT, CPR_B063_REDEEM_PTS, CPR_B063_BAL_PTS, " +
  				  "CPR_TRX_SOURCE" + ") VALUES (" + 
  				  history[0] + ", " + history[1] + ", " + history[2] + ", " + history[3] + ", " + history[4] + ", " +
  				  history[5] + ", " + history[6] + ", " + history[7] + ", " + history[8] + ", " + history[9] + ", " +
  				  history[10] + ", " + history[11] + ", " + history[12] + ", " + history[13] + ", " + history[14] + ", " +
  				  history[15] + ", " + history[16] +  ", " +history[17] + 
  				  ")";
	        try {
	        	PreparedStatement prepStmt = con.prepareStatement(logQuery);
	        	System.out.println(String.valueOf(prepStmt));
	            rows = prepStmt.executeUpdate();
	            if (prepStmt != null) { prepStmt.close(); }
	        } catch (SQLException e ) {
	        	e.printStackTrace();
	    	}
	        
	        if(rows > 0){
	        	String msgResponse = "Logging Trx is successful with " + rows +" rows.";
	        	System.out.println("Logging TRX? " + msgResponse);
	        	System.out.println("Redeem History is saved with " + rows + " rows.");
	        }else{
	        	String msgResponse = "Logging Trx is FAILED.";
	        	System.out.println("Logging TRX? " + msgResponse);
	        	System.out.println(logQuery);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void closeConnection(){
		try{
			if (this.con != null) { this.con.close(); }
		} catch (SQLException e ) {
			throw new RuntimeException("Cannot close the connection to the database!", e);
	    }

	}
	
	public void openConnection(){
		try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find the driver in the classpath!", e);
        }
        try {
            con = DriverManager.getConnection(
            		"jdbc:mysql://"+ PropertiesLoader.getProperty("DB_URL") 
            		+ ":"+ PropertiesLoader.getProperty("DB_PORT") +"/" + PropertiesLoader.getProperty("DB_NAME") + "?" 
            		+ "user=" + PropertiesLoader.getProperty("DB_USERNAME") +"&password=" + PropertiesLoader.getProperty("DB_PASSWORD")
            		);
            if(con != null){
            	System.out.println("Database connected via channel:"+String.valueOf(con));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect the database!", e);
        }
	}
	
}
