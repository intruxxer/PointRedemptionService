package id.bri.switching.app;

import id.bri.switching.helper.PropertiesLoader;
import id.bri.switching.mq.MQClient;
import id.bri.switching.mq.MQServer;

public class PointRedemptionService {
	
	public static MQServer mqserver;
	
	public static void main(String[] args) {
				
		mqserver = new MQServer();
		mqserver.openConnection(PropertiesLoader.getProperty("MQ_URL"));
		mqserver.setupMessageConsumer(
				PropertiesLoader.getProperty("POINTREQUESTQUEUE"), 
				PropertiesLoader.getProperty("POINTRESPONSEQUEUE")
				);
		
	}
  
}
