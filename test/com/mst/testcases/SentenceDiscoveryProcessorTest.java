package com.mst.testcases;

import org.junit.Test;

import com.mst.model.requests.RecommandationRequest;
import com.mst.sentenceprocessing.SentenceDiscoveryProcessorImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;

public class SentenceDiscoveryProcessorTest {

	private SentenceDiscoveryProcessorImpl discoveryProcessorImpl = new SentenceDiscoveryProcessorImpl();
	
	@Test
	public void process(){
		RecommandationRequest request = new RecommandationRequest();
		request.setText("mri ultrasound shows a simple ovarian cyst.");
		discoveryProcessorImpl.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
		try {
		//	discoveryProcessorImpl.process(request);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
