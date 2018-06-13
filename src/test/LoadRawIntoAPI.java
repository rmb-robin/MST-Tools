package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;

//import org.junit.Test;

import com.google.gson.Gson;
import com.mst.metadataProviders.TestHl7Provider;
import com.mst.model.raw.RawReportFile;

public class LoadRawIntoAPI {

	@Test
	public void loadRawHl7IntoAPI(){
		String endPoint = "http://localhost:8080/mst-sentence-service/webapi/rawreport/save"; 
				
				//"localhost:8080/mst-sentence-service/webapi/rawreport/save";
		String body = new TestHl7Provider().getInput();
		callPOSTService(endPoint, body);
	}

	//
	 private String callPOSTService(String endpoint, String body) {
	    	String ret = null;
	    	
	    	HttpURLConnection conn = null;
	    	
	    	try {
	    		URL url = new URL(endpoint);
	    		conn = (HttpURLConnection) url.openConnection();
	    		conn.setRequestMethod("POST");
	    		conn.setDoOutput(true);
	    		conn.setRequestProperty("Accept", "application/json");
	    		conn.setRequestProperty("Content-Type", "application/json");
	    		
	    		OutputStreamWriter streamWriter = new OutputStreamWriter(conn.getOutputStream());
	            
	    		RawReportFile file = new RawReportFile();
	    		file.setContent(body);
	    		file.setOrgId("58c6f3ceaf3c420b90160803");
	    		file.setOrgName("rad");
	    		Gson gson = new Gson();
	    		String input = gson.toJson(file);
	    		streamWriter.write(input);
	            streamWriter.flush();

	            BufferedReader br = null;
	            try {
	            	br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            } catch(IOException ioe) {;
	            System.out.println(ioe.getMessage());
	            }
	            
	    		String response = null;
	    		StringBuffer buffer = new StringBuffer();
	    		
	    		while ((response = br.readLine()) != null) {
	    			buffer.append(response);
	    		}
	    		
	    		ret = conn.getResponseCode() + "~" + buffer.toString();
	    		
	    	} catch(Exception e) {
	    		Exception t = e;
	    		System.out.println(e.getMessage());
	    	} finally {
	    		if(conn != null)
	    			conn.disconnect();
	    	}
	    	
	    	return ret;
	    }
}


