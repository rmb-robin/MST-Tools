import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.mst.dao.HL7ParsedRequstDaoImpl;
import com.mst.interfaces.dao.HL7ParsedRequstDao;
import com.mst.metadataProviders.TestHl7Provider;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.raw.HL7ParsedRequst;
import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.SentenceTextRequest;

public class CallSentenceServiceIntegretationTest {


	//http://10.210.192.4
		@Test
		public void loadRawHl7IntoAPI(){
			String endPoint = "http://localhost:8080/mst-sentence-service/webapi/rawreport/save";
					
					//"http://localhost:8080/mst-sentence-service/webapi/sentence/savetext"; 
			//;
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
		    		//SentenceTextRequest request = new SentenceTextRequest();
		    		//request.setText(body);
		    		
		    		//DiscreteData d = new DiscreteData();
		    		//d.setOrganizationId("5a7a6a2a2cc55376ebdcd08e");
		    		//request.setDiscreteData(d);
		    		
		    		file.setContent(body);
		    		file.setOrgId("5982ab9381614df545da49a2");
		    		file.setOrgName("rad");
		    		Gson gson = new Gson();
		    	
		    		streamWriter.write(	gson.toJson(file));
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
