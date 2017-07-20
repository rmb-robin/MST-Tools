package com.mst.testcases;

import java.util.List;

import org.junit.Test;

import com.mst.metadataProviders.DiscreteDataComplianceFieldProvider;
import com.mst.model.discrete.ComplianceDisplayFieldsBucketItem;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.discrete.DiscreteDataBucketIdentifierResult;
import com.mst.model.discrete.DiscreteDataCustomField;
import com.mst.model.discrete.Followup;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.sentenceprocessing.DiscreteDataBucketIdentifierImpl;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import static org.junit.Assert.*;

public class DiscreteDataBucketIdentifierTest {

	private DisceteDataComplianceDisplayFields getFields(){
		DiscreteDataComplianceFieldProvider provider = new DiscreteDataComplianceFieldProvider();
		return  provider.get("rad","rad");
	}
	private DisceteDataComplianceDisplayFields fields = getFields();
	private SentenceProcessingControllerImpl controller;
	
	@Test
	public void getBucketTest() throws Exception{
		controller = new SentenceProcessingControllerImpl();
		controller.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
	
		String sentence = "Abdominal aortic aneurysm measuring 4 cm. Follow up yearly";
		DiscreteData discreteData = new DiscreteData();
		runTest(sentence,discreteData,"Bucket4");
		
		sentence = "The abdominal aortic aneurysm measures 5.1 cm distal to the renal arteries. Vascular consultation is recommended.";
	//	runTest(sentence, discreteData, "Bucket6"); //need change here to 7 per visit time. 
		
		discreteData.setPatientAge(33);
		sentence = "There is an enlarged thyroid nodule that appears to be heterogeneous and measures 3.1cm. Consider follow-up ultrasound.";
		//runTest(sentence, discreteData, "Bucket2");  
	}
	
	@Test
	public void getComplianceTest() throws Exception{
		
		ComplianceDisplayFieldsBucketItem bucketItem = new ComplianceDisplayFieldsBucketItem();
		DiscreteDataBucketIdentifierImpl bucketIdentifierImpl = new DiscreteDataBucketIdentifierImpl();
		
		
		Followup followup = new Followup();
		followup.setDuration(5);
		followup.setDurationMeasure("years");
		followup.setIsNumeric(true);
		bucketItem.setFollowUp(followup);
	
		controller = new SentenceProcessingControllerImpl();
		SentenceProcessingHardcodedMetaDataInputFactory meta = new SentenceProcessingHardcodedMetaDataInputFactory();
		controller.setMetadata(meta.create());
	
		SentenceTextRequest request = new SentenceTextRequest();
		request.setText("Recommend follow up in 5 years");
		request.setDiscreteData(new DiscreteData());
		SentenceProcessingResult result =  controller.processText(request);
		
		Sentence sentence = result.getSentences().get(0);
		boolean isCompliant = bucketIdentifierImpl.issentenceCompliant(sentence, bucketItem);
		assertTrue(isCompliant);
		
		request.setText("Recommend follow up in 3 years");
		result =  controller.processText(request);
		
		sentence = result.getSentences().get(0);
		isCompliant = bucketIdentifierImpl.issentenceCompliant(sentence, bucketItem);
		assertFalse(isCompliant);
		
		followup.setDuration(3);
		isCompliant = bucketIdentifierImpl.issentenceCompliant(sentence, bucketItem);
		assertTrue(isCompliant);	
	}
	
	private void runTest(String sentence, DiscreteData discreteData, String expectedBucketName) throws Exception{
		DiscreteDataBucketIdentifierImpl identifier = new DiscreteDataBucketIdentifierImpl();
		
		SentenceRequest request = new SentenceRequest();
		request.getSenteceTexts().add(sentence);
		List<Sentence> sentences = controller.processSentences(request);
		DiscreteDataBucketIdentifierResult result = identifier.getBucket(discreteData, sentences, fields);
		assertEquals(expectedBucketName,result.getBucketName());
	}
	
	
}
 