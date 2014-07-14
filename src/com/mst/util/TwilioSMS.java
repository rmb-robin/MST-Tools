package com.mst.util;

import java.util.*; 

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory; 
import com.twilio.sdk.resource.instance.Message; 

public class TwilioSMS { 
 
	public final String ACCOUNT_SID = "AC75a90e82c7f600132f6e50e7bc4a611f"; 
	public final String AUTH_TOKEN = "490c1cadaf9ab079bd3483549af686ea"; 
	public final String FROM_NUMBER = "+15029128794";

	public void sendSMS(String msg) { 
		TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN); 
	
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("To", "5024392730"));
		params.add(new BasicNameValuePair("From", FROM_NUMBER)); 
		params.add(new BasicNameValuePair("Body", msg));   
		
		MessageFactory messageFactory = client.getAccount().getMessageFactory(); 
		Message message = null;
		
		try {
			message = messageFactory.create(params);
		} catch (TwilioRestException e) {
			e.printStackTrace();
		} 
		
		message.getSid();
	} 
}