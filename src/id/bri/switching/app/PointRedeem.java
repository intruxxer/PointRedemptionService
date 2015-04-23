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
	private static Map<String, String> redeemptionInfo;
	private static Connection con;
	private static String errorMsg;
	
	public PointRedeem(){
		cardInfo = new HashMap<String, String>();
		redeemptionInfo = new HashMap<String, String>();
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
    	inq.closeConnection();

    	if ( (currPointBal > 0) && (currPointBal >= Integer.parseInt(pointAmt)) ){
    		currPointBal = currPointBal - Integer.parseInt(pointAmt);
    	}else{
        	String msgResponse = "UPDATE debet lbcrdext FAILED. Insufficient point.";
        	System.out.println("Point Redeem updates? " + msgResponse);
        	return cardInfo;
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
    	inq.closeConnection();
    	
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
    
public int updatePoint(String cardNum, String pointAmt) throws SQLException {
    	
        String query = "UPDATE lbcrdext SET LB_CP_PAS_CURR_BAL = '" + String.valueOf(pointAmt) + "' WHERE LB_CARD_NMBR = '" + cardNum + "'";
        int rows = 0;
        try {
        	PreparedStatement prepStmt = con.prepareStatement(query);
            rows = prepStmt.executeUpdate();
        } catch (SQLException e ) {
        	errorMsg = "Error SQL exception Update Kredit Point : " + e.toString();
        	e.printStackTrace();
    	}
        
        if(rows > 0){
        	String msgResponse = "UPDATE SET point lbcrdext SUCCESS in" + rows +" rows.";
        	System.out.println("Point Redeem updates? " + msgResponse);
        }else{
        	String msgResponse = "UPDATE SET point lbcrdext FAILED.";
        	System.out.println("Point Redeem updates? " + msgResponse);
        }
        
        return rows;
        
	}
    
    public Map<String, String> redeemPointTrx(String cardNum, String rule, String paramsVal, String paramsPoint, String trxTotal, String pointAmt) throws SQLException {
    	//CH-TRXTOTAL-{CRATE-TRXNET-PUSED-PBALANCE}
    	redeemptionInfo.put("cardNum", cardNum);
    	redeemptionInfo.put("trxTotal", trxTotal);
    	Inquiry inq = new Inquiry();
    	int currPointBalance = inq.inquiryPointCard(cardNum);
    	inq.closeConnection();
    	int pointBalance = 0; int updateStatusPointBalance = 0;
    	float conversionRate = (float) 0.00; float netSales = (float) 0.00;
    	//calculationRule: {P is Percentage, M is Multiply of, F is Flat
    	if(rule.equals("P")){
    		//Here, make sure we call it by paramsPoint == pointAmt
    		//e.g. redeemPointTrx(CH, P, (int) %TRXTOTAL, 10, TRXTOTAL, 10)
    		if( currPointBalance < Integer.valueOf(pointAmt) || 
    			Integer.valueOf(pointAmt) < Integer.valueOf(paramsPoint) )
    		{
    			System.out.println("Point is insufficient to redeem. ");
    			redeemptionInfo.put("cardTrxApproval", "DENIED");
    			redeemptionInfo.put("cardTrxApprovalInfo", "INSUFFICIENT POINT");
    			return redeemptionInfo;
    		}
    		
    		if( Integer.valueOf(paramsVal) > 100 )
        	{
        		System.out.println("Point claim is invalid to redeem. ");
        		redeemptionInfo.put("cardTrxApproval", "DENIED");
        		redeemptionInfo.put("cardTrxApprovalInfo", " OVER 100 PERCENT");
        		return redeemptionInfo;
        	}
    		
    		if( !pointAmt.equals(paramsPoint) )
        	{
        		System.out.println("Invalid point to redeem. ");
        		redeemptionInfo.put("cardTrxApproval", "DENIED");
        		redeemptionInfo.put("cardTrxApprovalInfo", "INVALID AMOUNT POINT TO REDEEM");
        		return redeemptionInfo;
        	}
    		
    		conversionRate = (float) (( Float.valueOf(paramsVal) * Float.valueOf(trxTotal) )/100.00);
    		netSales = Float.valueOf(trxTotal) - conversionRate;
    		//PBALANCE = CURRBALANCE - PUSED
        	pointBalance = currPointBalance - Integer.valueOf(pointAmt);
        	updateStatusPointBalance = updatePoint(cardNum, String.valueOf(pointBalance));
    		if(updateStatusPointBalance > 0){
    			System.out.println("Point before redeem: "+ currPointBalance);
            	System.out.println("Point after redeem: "+ pointBalance);
    			redeemptionInfo.put("trxDiscount", String.valueOf(conversionRate) );
        		redeemptionInfo.put("trxNetto", String.valueOf(netSales) );
        		redeemptionInfo.put("pointRedeemAmt", pointAmt );
        		redeemptionInfo.put("pointBalanceAmt", String.valueOf(pointBalance) );
        		redeemptionInfo.put("cardTrxApproval", "APPROVED");
    			redeemptionInfo.put("cardTrxApprovalInfo", "POINT REDEEMED");
    		}
    		else{
    			System.out.println("DB Update Point Balance FAILED. TRX DENIED. ");
    			redeemptionInfo.put("trxDiscount", "0" );
        		redeemptionInfo.put("trxNetto", trxTotal );
        		redeemptionInfo.put("pointRedeemAmt", "0" );
        		redeemptionInfo.put("pointBalanceAmt", String.valueOf(currPointBalance) );
        		redeemptionInfo.put("cardTrxApproval", "DENIED");
    			redeemptionInfo.put("cardTrxApprovalInfo", "POINT REDEEMPTION DB UPDATE FAILED");
    		}
    		
    	}
    	else if(rule.equals("M")){
    		//e.g. redeemPointTrx(CH, M, (int) n*paramsValue, 10, TRXTOTAL, n*10)
    		if( currPointBalance < Integer.valueOf(pointAmt) ||  
    			Integer.valueOf(pointAmt) < Integer.valueOf(paramsPoint) )
    		{
    			System.out.println("Point is insufficient to redeem. ");
    			redeemptionInfo.put("cardTrxApproval", "DENIED");
    			redeemptionInfo.put("cardTrxApprovalInfo", "INSUFFICIENT POINT");
    			return redeemptionInfo;
    		}
    		//Anticipating that for each, e.g. 10 points == 20,000 IDR, submitted point !== (n*10), then
    		//We take whole part (n) * paramsPoint (Here, params = 10 points) as point to be redeemed
    		//Remainder will be (1) Discarded if no debetPoint() occurs beforehand, or (2) Returned using kreditPoint()
    		int pointUsed = Integer.valueOf(pointAmt);
    		int remainder = Integer.valueOf(pointAmt) % Integer.valueOf(paramsPoint);
    		if(remainder != 0){ 
    			//numPoint is NOT n*paramsPoint
    			pointUsed = Integer.valueOf(pointAmt) - remainder;
    		}else if(remainder > 0){
    			//numPoint is n*paramsPoint
    			//Thus, pointUsed is still == pointAmt
    		}
    		int coefficient = pointUsed/Integer.valueOf(paramsPoint);
    		conversionRate = (float) ( Float.valueOf(paramsVal) * Float.valueOf(coefficient) );
    		//If conversion rate (discount) is larger than Total Trx, it is not allowed
    		if( conversionRate > Float.valueOf(trxTotal) ){
    			//Set discount == Total trx, hence Trx is free.
    			conversionRate = Float.valueOf(trxTotal);
    			netSales = Float.valueOf(trxTotal) - conversionRate;;
    			//Set the point to be redeemed to be reduced to the max eligible point
    			//PBALANCE = CBALANCE - MAXELLIGIBLEUSED
    			int maxPoint = Integer.valueOf(paramsPoint) * ( Integer.valueOf(trxTotal)/Integer.valueOf(paramsVal) );
    			pointBalance = currPointBalance - Integer.valueOf(maxPoint);
        		updateStatusPointBalance = updatePoint(cardNum, String.valueOf(pointBalance));
    		}else{
    			netSales = Float.valueOf(trxTotal) - conversionRate;
    			//PBALANCE = CBALANCE - PUSED
        		pointBalance = currPointBalance - Integer.valueOf(pointAmt);
        		updateStatusPointBalance = updatePoint(cardNum, String.valueOf(pointBalance));
    		}
        	if(updateStatusPointBalance > 0){
        		System.out.println("Point before redeem: "+ currPointBalance);
            	System.out.println("Point after redeem: "+ pointBalance);
    			redeemptionInfo.put("trxDiscount", String.valueOf(conversionRate) );
        		redeemptionInfo.put("trxNetto", String.valueOf(netSales) );
        		redeemptionInfo.put("pointRedeemAmt", String.valueOf(pointUsed) );
        		redeemptionInfo.put("pointBalanceAmt", String.valueOf(pointBalance) );
        		redeemptionInfo.put("cardTrxApproval", "APPROVED");
    			redeemptionInfo.put("cardTrxApprovalInfo", "POINT REDEEMED");
    		}
    		else{
    			System.out.println("DB Update Point Balance FAILED. TRX DENIED. ");
    			redeemptionInfo.put("trxDiscount", "0" );
        		redeemptionInfo.put("trxNetto", trxTotal );
        		redeemptionInfo.put("pointRedeemAmt", "0" );
        		redeemptionInfo.put("pointBalanceAmt", String.valueOf(currPointBalance) );
        		redeemptionInfo.put("cardTrxApproval", "DENIED");
    			redeemptionInfo.put("cardTrxApprovalInfo", "POINT REDEEMPTION DB UPDATE FAILED");
    		}
    	}else if(rule.equals("F")){
    		//e.g. redeemPointTrx(CH, F, (int)Flat, 10, TRXTOTAL, 10)
    		if( currPointBalance < Integer.valueOf(pointAmt) || 
        			Integer.valueOf(pointAmt) < Integer.valueOf(paramsPoint) )
        	{
        		System.out.println("Point is insufficient to redeem. ");
        		redeemptionInfo.put("cardTrxApproval", "DENIED");
        		redeemptionInfo.put("cardTrxApprovalInfo", "INSUFFICIENT POINT");
        		return redeemptionInfo;
        	}
    		if( !pointAmt.equals(paramsPoint) )
        	{
        		System.out.println("Invalid point to redeem. ");
        		redeemptionInfo.put("cardTrxApproval", "DENIED");
        		redeemptionInfo.put("cardTrxApprovalInfo", "INVALID AMOUNT POINT TO REDEEM");
        		return redeemptionInfo;
        	}
        		conversionRate = Float.valueOf(paramsVal);
        		netSales = Float.valueOf(trxTotal) - conversionRate;
        		//PBALANCE = CBALANCE - PUSED
        		pointBalance = currPointBalance - Integer.valueOf(paramsPoint);
        		updateStatusPointBalance = updatePoint(cardNum, String.valueOf(pointBalance));
        		if(updateStatusPointBalance > 0){
        			System.out.println("Point before redeem: "+ currPointBalance);
                	System.out.println("Point after redeem: "+ pointBalance);
        			redeemptionInfo.put("trxDiscount", String.valueOf(conversionRate) );
            		redeemptionInfo.put("trxNetto", String.valueOf(netSales) );
            		redeemptionInfo.put("pointRedeemAmt", paramsVal );
            		redeemptionInfo.put("pointBalanceAmt", String.valueOf(pointBalance) );
            		redeemptionInfo.put("cardTrxApproval", "APPROVED");
        			redeemptionInfo.put("cardTrxApprovalInfo", "POINT REDEEMED");
        		}
        		else{
        			System.out.println("DB Update Point Balance FAILED. TRX DENIED. ");
        			redeemptionInfo.put("trxDiscount", "0" );
            		redeemptionInfo.put("trxNetto", trxTotal );
            		redeemptionInfo.put("pointRedeemAmt", "0" );
            		redeemptionInfo.put("pointBalanceAmt", String.valueOf(currPointBalance) );
            		redeemptionInfo.put("cardTrxApproval", "DENIED");
        			redeemptionInfo.put("cardTrxApprovalInfo", "POINT REDEEMPTION DB UPDATE FAILED");
        		}
    		
    	}
    	
    	return redeemptionInfo;
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
