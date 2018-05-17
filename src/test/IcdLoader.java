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
			String[] vals = line.split(",");
			IcdTenSentenceInstance instance = new IcdTenSentenceInstance();
			instance.setIcdCode(vals[1]);
			instance.setSentence(vals[0]);
			instances.add(instance);
		}
		request.setSentenceInstances(instances);
		return request;
	}
	
	private String createFullPath() {
		return System.getProperty("user.dir") + File.separator + "testData" + File.separator + "icd.txt";
	}

}
