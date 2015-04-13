/**
 * MQServer
 *
 * Class yang berfungsi seolah seperti server yg mendengarkan request dari prosw melalui MQ,
 * kemudian memproses request tsb dan mengembalikan hasil proses ke prosw melalui MQ
 *
 * @package		id.bri.switching.mq
 * @author		PSD Team
 * @copyright           Copyright (c) 2013, PT. Bank Rakyat Indonesia (Persero) Tbk,
 */

// ---------------------------------------------------------------------------------

/*
 * ------------------------------------------------------
 *  Memuat package dan library
 * ------------------------------------------------------
 */

package id.bri.switching.mq;

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import id.bri.switching.app.Router;
import id.bri.switching.helper.LogLoader;
//import id.bri.switching.helper.MQHelper;
import id.bri.switching.helper.PropertiesLoader;

public class MQServer implements MessageListener {

	Connection connection;
	Session session;
	MessageProducer replyProducer;
	String messageQueueProducer;
	String messageQueueConsumer;
	
	public Connection getConnection() {
		return connection;
	}
    
    public void setConnection(Connection conn) {
    	connection = conn;
    }
    
    public synchronized void openConnection(String mqUrl) {
	    try {        	
	    	//Connection to activeMQ
	    	ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(mqUrl);
	    	this.connection = connectionFactory.createConnection();
	        this.connection.start();
	        LogLoader.setInfo(MQServer.class.getSimpleName(), "Connection to activeMQ is established...");
	        
	    } catch (JMSException e) {
	    	if (e.getLinkedException() instanceof IOException) {
	            // ActiveMQ is not running. Do some logic here.
	            // use the TransportListener to restart the activeMQ connection
	            // when activeMQ comes back up.
	    		
	    	} else if (e.getMessage().contains("Connection refused")) {
	    		LogLoader.setError(MQServer.class.getSimpleName(), "Cannot connect to MQ, connection refused");
	    	} else {
	    		LogLoader.setError(MQServer.class.getSimpleName(), "Cannot connect to MQ, error unknown");
	    	}
	    }
	}

	public void setupMessageConsumer(String messageQueueRequest, String messageQueueResponse) {
		try {
			if (this.connection == null) {	
				// check connection
	    		openConnection("tcp://10.107.11.206:61616");
	    		LogLoader.setInfo(MQServer.class.getSimpleName(), "Connection to activeMQ re-established...");
	    	}
			System.out.println("ActiveMQ Server is setup to listen to "+messageQueueRequest+" & to respond to "+messageQueueResponse);
			this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			this.messageQueueConsumer = messageQueueRequest;
			Destination requestQueue = this.session.createQueue(messageQueueRequest);
			MessageConsumer consumer = this.session.createConsumer(requestQueue);
			consumer.setMessageListener(this); // Finally, Trigger this.onMessage(Message message)
			
			//This messageQueueProducer is the name of queue in activeMQ to which producer refers when responding 
			//or acting as MQClient. MQServer also acts as MQClient as It needs to give response for each request it receives
			//from PSW.
			
			//[ when void onMessage() triggered ],
			//because void onMessage() is triggered automatically due to its nature as a must-be-override interface;
			//This approach is carried out as a way of global variable for void onMessage() to determine
			
			this.messageQueueProducer = messageQueueResponse;
			
	        LogLoader.setInfo(MQServer.class.getSimpleName(), "Listener is starting...");
	        
		} catch (JMSException e) {
			LogLoader.setError(MQServer.class.getSimpleName(), e);
		} catch (Exception e) {
			LogLoader.setError(MQServer.class.getSimpleName(), e);
		}	
	}

	public void onMessage(Message message) {
        try {
        	LogLoader.setInfo(MQServer.class.getSimpleName(), "Listener: ON and Processing...");
            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                String messageText = txtMsg.getText();
                //System.out.println(messageText);
                
                // Calling Router() object to process the request from PWS.
                // Here, ISOMessage is unpacked, extracted, and processed according to our business logic.
                // Upon unpacking from ISO to data, it is then we can proceed for executing our business logics,
                // Hence, dig out & play around with business logic in your DB apps;
                String result = Router.processISOMessage(messageText);
                
                // CHECK HERE; DO WE NEED TO SEND BACK?
                // If result !=  empty, there is message to send back as response upon receiving message
                // String result is the ISOMessage that needs to be sent as The Body of message
                // The ISOMessage produced from Router() will be enveloped by within TextMessage response.
                if (!result.equals("")) {
	                //1. Ensuring Connection's session is still Established
	                if (this.session == null) {
	                	this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);   
	                }
	                /*	PROBLEMATIC PART	*/    
	                //2. Setup The Producer/Publisher
	                //Setup a message producer to respond to messages from clients, we will get the destination
	                //to send to from the JMSReplyTo header field from a Message
	                Destination requestQueue = this.session.createQueue(this.messageQueueConsumer);
	    			this.replyProducer = this.session.createProducer(requestQueue);
	    			//There are two modes; NON_PERSISTENT vs PERSISTENT
	                this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	                
	    			//3. Create Message, Assign Correlation ID, and then finally SEND response to the Destination in ActiveMQ
	                //Set the correlation ID from the received message to be the correlation id of the response message, s.t.
	                //this lets the client identify which message is a corresponding response to which message esp. when there
	                //are more than one outstanding messages to the server.
	                TextMessage response = this.session.createTextMessage();
	                response.setText(result);
	                
	                //Enveloping process by JMS Correlation ID, etc
	                //Destination requestQueue = this.session.createQueue(this.messageQueueConsumer);
	                response.setJMSReplyTo(requestQueue);
	                response.setJMSCorrelationID(message.getJMSCorrelationID());
	    	        this.replyProducer.send(response);
                	
                	/*
                	try { 
    					MQClient mqclient = new MQClient();
    					mqclient.openConnection("tcp://10.107.11.206:61616");
    				    mqclient.setupMessageProducer("PSWLinux0Rdm.Response", result);
    				} catch (JMSException e) {
    			        	if (e.getLinkedException() instanceof IOException) {
    			                // ActiveMQ is not running. Do some logic here.
    			                // use the TransportListener to restart the activeMQ connection
    			                // when activeMQ comes back up.
    			        		
    			        	} else if (e.getMessage().contains("Connection refused")) {
    			        		LogLoader.setError(MQClient.class.getSimpleName(), "Cannot connect to MQ, connection refused");
    			        	} else {
    			        		LogLoader.setError(MQClient.class.getSimpleName(), "Cannot connect to MQ, error unknown");
    			        	}
    			    }
                	*/
	    	        LogLoader.setInfo(MQServer.class.getSimpleName(), "Sending verification message: success. ");
	    	        /*	PROBLEMATIC PART	*/ 
                } else {
                	LogLoader.setInfo(MQServer.class.getSimpleName(), "There is incoming message, but no response needed.");
                }
            }            
        } catch (JMSException e) {
            //Handle the exception appropriately
        	LogLoader.setError(MQServer.class.getSimpleName(), e);
        } 
    }
	
}
