package id.bri.switching.app;

import id.bri.switching.mq.*;

public class PointRedemptionService {
	
	public static MQServer mqserver;
	
	public static void main(String[] args) {
				
		mqserver = new MQServer();
		mqserver.openConnection("tcp://10.107.11.206:61616");
		mqserver.setupMessageConsumer("PSWLinux0Rdm.HostRequest", "PSWLinux0Rdm.Response");
		
	}
  
}
