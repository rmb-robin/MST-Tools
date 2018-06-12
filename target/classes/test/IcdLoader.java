package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mst.metadataProviders.TestDataProvider;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.IcdTenRequest;
import com.mst.model.requests.IcdTenSentenceInstance;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.sentenceprocessing.IDCTenProcesser;
import com.mst.sentenceprocessing.SentenceDiscoveryProcessingHardcodedMetaDataInputFactory;

public class IcdLoader {

	@Test
	public void load() throws Exception{
		
		List<String> allLines = TestDataProvider.readLines(createFullPath());
		IcdTenRequest request = createIcdTenRequest(allLines);
		SentenceProcessingMetaDataInput input =new SentenceDiscoveryProcessingHardcodedMetaDataInputFactory().create();	
		new IDCTenProcesser(input).processAndSave(request);
	}	
	
	
	private IcdTenRequest createIcdTenRequest(List<String> lines){
		
		IcdTenRequest request = new IcdTenRequest();
		request.setConvertLargest(true);
		request.setConvertMeasurements(true);
		request.setDiscreteData(new DiscreteData());
	
		List<IcdTenSentenceInstance> instances = new ArrayList<>();
		for(String line: lines){
			String code = line.substring(0,line.indexOf(","));
			String sentence = line.substring(line.indexOf(",")+1, line.length());
			IcdTenSentenceInstance instance = new IcdTenSentenceInstance();
			instance.setIcdCode(code);
			instance.setSentence(sentence);
			instances.add(instance);
		}
		request.setInstances(instances);
		return request;
	}
	
	private String createFullPath() {
		return System.getProperty("user.dir") + File.separator + "testData" + File.separator + "icd.txt";
		/*
		 * C:\Users\Bryan\RABHU\eclipse-workspace\MST-Tools\testData
		 */
	}

}
