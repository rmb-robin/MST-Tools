package com.mst.util;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveMQ {

	private Connection connection = null;
	private Session session = null;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public ActiveMQ() {
		try {
			String url = Props.getProperty("activemq_host");
            String user = Props.getProperty("activemq_user");
            String pw = Props.getProperty("activemq_pw");
            
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
			connection = connectionFactory.createConnection();
			connection.start();
			
			// JMS messages are sent and received using a Session. We will create here a non-transactional session object. If you want
	        // to use transactions you should set the first parameter to 'true'
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
		} catch(Exception e) {
			logger.error("Error establishing a connection to ActiveMQ. {}", e);
			//e.printStackTrace();
		}
	}

	public boolean publishMessage(String qName, String payload) {
		boolean ret = true;

		try {
	        Destination destination = session.createQueue(qName);
	        MessageProducer producer = session.createProducer(destination);
	
	        TextMessage message = session.createTextMessage(payload);
			producer.send(message);

			//logger.info("Published message to AcitveMQ queue: {}", payload);
			
		} catch(Exception e) {
			ret = false;
			logger.error("Error publishing message to AcitveMQ queue. {}", e);
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public void closeConnection() {
        try {
			connection.close();
		} catch(JMSException e) {
			logger.error("Error closing ActiveMQ connection. {}", e);
			//e.printStackTrace();
		}
	}
}
