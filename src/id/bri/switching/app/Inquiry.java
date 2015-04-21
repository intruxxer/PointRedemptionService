package id.bri.switching.app;

import id.bri.switching.helper.PropertiesLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Inquiry {

	private static Map<String, String> cardInfo;
	private static Connection con;
	
	public Inquiry(){
		cardInfo = new HashMap<String, String>();
		con = null;
        
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
	public int inquiryPointCard(String cardNum) throws SQLException {
		int currPointBal = 0;
    	try {
    		String pointQuery = "SELECT LB_CP_PAS_CURR_BAL FROM lbcrdext WHERE LB_CARD_NMBR = '"+ cardNum +"'";
        	PreparedStatement prepStmt = con.prepareStatement(pointQuery);
            ResultSet resSet = prepStmt.executeQuery(pointQuery);
            while(resSet.next()){
            	currPointBal = resSet.getInt("LB_CP_PAS_CURR_BAL");
            }
            
            if (prepStmt != null) { prepStmt.close(); }
        } catch (SQLException e ) {
        	e.printStackTrace();
    	} finally {
    		
    	}
    	
    	return currPointBal;
	}
	
	//Table: lbcpcrd
    public Map<String, String> inquiryStatusCard(String cardNum) throws SQLException {
    	try {
	       
	        String statusQuery = "SELECT CP_POSTING_FLAG, CP_BLOCK_CODE, CM_STATUS FROM lbcpcrd WHERE CP_CARDNMBR = '"+ cardNum +"'";
	        PreparedStatement prepStmt = con.prepareStatement(statusQuery);
	        ResultSet resSet = prepStmt.executeQuery(statusQuery);

            if(resSet.next()) {
            	cardInfo.put("cardNumber", cardNum);
            	if( !resSet.getString("CP_POSTING_FLAG").equals("PP") ) {
	            	cardInfo.put("cardStatus", "-PP");
            	}
            	else if( !( resSet.getString("CP_BLOCK_CODE").equals(" ") || resSet.getString("CP_BLOCK_CODE").equals("V") || resSet.getString("CP_BLOCK_CODE").equals("P") || resSet.getString("CP_BLOCK_CODE").equals("Q") ) ) {
	            	cardInfo.put("cardStatus", "-BC");
            	}
            	else if( !( resSet.getString("CM_STATUS").equals("1") || resSet.getString("CM_STATUS").equals("2") ) ) {
            		cardInfo.put("cardStatus", "-ST");
            	}else {
            		cardInfo.put("cardStatus", "OK");
            	}
             }else {
            	cardInfo.put("cardNumber", cardNum);
             	cardInfo.put("cardStatus", "N/A"); 
             }
            
            if (prepStmt != null) { prepStmt.close(); }
            
    	} catch (SQLException e ) {
    		e.printStackTrace();
	    }
    	
		return cardInfo;    	
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
