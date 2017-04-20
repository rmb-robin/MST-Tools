package com.mst.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.mst.metadataProviders.NGramsHardCodedProvider;
import com.mst.metadataProviders.TestDataProvider;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.PrepositionPhraseProcessingInputFactory;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.sentenceprocessing.VerbPhraseInputFactoryImpl;

public class SentenceProcessingControllerTest {

	List<String> expectedSubjects = new ArrayList<>();
	List<String> expectedSubjectComplements = new ArrayList<>();
	
	@Test 
	public void processSentence() throws Exception{
		
		expectedSubjects.clear();
		expectedSubjects.add("ct-scan");
		expectedSubjectComplements.clear();
		expectedSubjectComplements.add("cyst");
		runAssert("CT scan demonstrates a benign cyst.",expectedSubjects,expectedSubjectComplements);
	}
	
	private void runAssert(String sentenceText, List<String> subjects, List<String> subjectComplements) throws Exception{
		
		SentenceProcessingControllerImpl controller = new  SentenceProcessingControllerImpl();
		controller.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
		Sentence result = controller.ProcessSentence(sentenceText);
		
		List<WordToken> tokens = result.getModifiedWordList();
		
		List<WordToken> subjectResult = tokens.stream().filter(a-> a.getPropertyValueType()==PropertyValueTypes.Subject).collect(Collectors.toList());	
		runAssertForSubjectOrComplement(subjects,subjectResult);

		List<WordToken> subjectComplementResult = tokens.stream().filter(a-> a.getPropertyValueType()==PropertyValueTypes.SubjectComplement).collect(Collectors.toList());	
		runAssertForSubjectOrComplement(subjectComplements,subjectComplementResult);
	
		assertTrue(result.getTokenRelationships().size()>0);
	}
	
	
	private void runAssertForSubjectOrComplement(List<String> expected, List<WordToken> actual){
		assertEquals(expected.size(),actual.size());
		
		for(WordToken subject: actual){
			assertTrue(expected.contains(subject.getToken()));
		}
	
	}
}
