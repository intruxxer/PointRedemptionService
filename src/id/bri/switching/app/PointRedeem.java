package id.bri.switching.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;

import id.bri.switching.helper.LogLoader;
import id.bri.switching.helper.MysqlConnect;
import id.bri.switching.helper.PropertiesLoader;
import id.bri.switching.helper.TextUtil;

public class PointRedeem {
	
	private static Map<String, String> cardInfo;
	private static Connection con;
	private static String errorMsg;
	
	public PointRedeem(){
		cardInfo = new HashMap<String, String>();
		con = null; errorMsg = "";
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
	
	//Table: lbcrdext
    public Map<String, String> debetPoint(String cardNum, String pointAmt) throws SQLException {
		
		Inquiry inq = new Inquiry();
    	int currPointBal = inq.inquiryPointCard(cardNum);

    	if ( (currPointBal > 0) && (currPointBal >= Integer.parseInt(pointAmt)) ){
    		currPointBal = currPointBal - Integer.parseInt(pointAmt);
    	}
    	
        String query = "UPDATE lbcrdext SET LB_CP_PAS_CURR_BAL = '" + String.valueOf(currPointBal) + "' WHERE LB_CARD_NMBR = '" + cardNum + "'";
        int rows = 0;
        try {
        	PreparedStatement prepStmt = con.prepareStatement(query);
            rows = prepStmt.executeUpdate();
        } catch (SQLException e ) {
        	errorMsg = "Error SQL exception Update Debet Point : " + e.toString();
        	e.printStackTrace();
    	}
        
        if(rows > 0){
        	cardInfo.put("cardNum", cardNum);
        	cardInfo.put("cardPoint", String.valueOf(currPointBal));
        	String msgResponse = "UPDATE debet lbcrdext SUCCESS in" + rows +" rows.";
        	System.out.println("Point Redeem updates? " + msgResponse);
        	return cardInfo;
        }else{
        	String msgResponse = "UPDATE debet lbcrdext FAILED.";
        	System.out.println("Point Redeem updates? " + msgResponse);
        	return cardInfo;
        }
        
	}
    
  //Table: lbcrdext
    public Map<String, String> kreditPoint(String cardNum, String pointAmt) throws SQLException {
		
		Inquiry inq = new Inquiry();
    	int currPointBal = inq.inquiryPointCard(cardNum);
    	currPointBal = currPointBal + Integer.parseInt(pointAmt);
    	
        String query = "UPDATE lbcrdext SET LB_CP_PAS_CURR_BAL = '" + String.valueOf(currPointBal) + "' WHERE LB_CARD_NMBR = '" + cardNum + "'";
        int rows = 0;
        try {
        	PreparedStatement prepStmt = con.prepareStatement(query);
            rows = prepStmt.executeUpdate();
        } catch (SQLException e ) {
        	errorMsg = "Error SQL exception Update Kredit Point : " + e.toString();
        	e.printStackTrace();
    	}
        
        if(rows > 0){
        	cardInfo.put("cardNum", cardNum);
        	cardInfo.put("cardPoint", String.valueOf(currPointBal));
        	String msgResponse = "UPDATE kredit lbcrdext SUCCESS in" + rows +" rows.";
        	System.out.println("Point Redeem updates? " + msgResponse);
        	return cardInfo;
        }else{
        	String msgResponse = "UPDATE kredit lbcrdext FAILED.";
        	System.out.println("Point Redeem updates? " + msgResponse);
        	return cardInfo;
        }
        
	}
    
    public void closeConnection(){
		try{
			if (con != null) { con.close(); }
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
