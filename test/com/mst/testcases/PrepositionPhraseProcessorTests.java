package com.mst.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.mst.interfaces.sentenceprocessing.RelationshipProcessor;
import com.mst.metadataProviders.NGramsHardCodedProvider;
import com.mst.metadataProviders.PartOfSpeechHardcodedAnnotatorEntityProvider;
import com.mst.metadataProviders.RelationshipInputProviderFileImpl;
import com.mst.metadataProviders.SemanticTypeHardCodedProvider;
import com.mst.metadataProviders.TestDataProvider;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.VerbTense;
import com.mst.model.metadataTypes.VerbType;
import com.mst.model.sentenceProcessing.PartOfSpeechAnnotatorEntity;
import com.mst.model.sentenceProcessing.PrepositionPhraseProcessingInput;
import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.Verb;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.NounRelationshipProcessor;
import com.mst.sentenceprocessing.PartOfSpeechAnnotatorImpl;
import com.mst.sentenceprocessing.PrepositionPhraseProcessingInputFactory;
import com.mst.sentenceprocessing.PrepositionPhraseProcessorImpl;
import com.mst.sentenceprocessing.SemanticTypeSentenceAnnotatorImpl;
import com.mst.sentenceprocessing.VerbProcessorImpl;

public class PrepositionPhraseProcessorTests {

	NGramsSentenceProcessorImpl ngramProcessor = new NGramsSentenceProcessorImpl();
	PrepositionPhraseProcessorImpl prepPhraseProcessor = new PrepositionPhraseProcessorImpl();
	NGramsHardCodedProvider ngramsProvider = new NGramsHardCodedProvider();
	
	PartOfSpeechAnnotatorEntity entity = new PartOfSpeechHardcodedAnnotatorEntityProvider().getPartOfSpeechAnnotatorEntity();
	PartOfSpeechAnnotatorImpl partOfSpeechAnnotator = new PartOfSpeechAnnotatorImpl();		
	
	SemanticTypeSentenceAnnotatorImpl stAnnotator = new SemanticTypeSentenceAnnotatorImpl();
	SemanticTypeHardCodedProvider stprovider = new SemanticTypeHardCodedProvider();
	RelationshipInput relationshipInput = new RelationshipInputProviderFileImpl().getRelationships("");
	RelationshipProcessor nounrelationshipProcessor = new NounRelationshipProcessor();

	@Test
	public void process()throws Exception{
		HashSet<String> expected = new HashSet<>();
		
		expected.clear();
		expected.add("disease");
		runAssert("She has a history of Chrons disease.", expected);
		
		expected.clear();
		expected.add("spine");
		runAssert("She has a cyst in the lumbar spine.", expected);
		
		expected.clear();
		expected.add("spine");
		runAssert("She has a cyst in the spine.", expected);
		
		expected.clear();
		expected.add("kidney");
		expected.add("spine");
		runAssert("She has a cyst in the spine and kidney.", expected);
		
		expected.clear();
		expected.add("patient");
		runAssert("Follow up ultrasound recommended in this postmenopausal patient.", expected);
		
		expected.clear();
		expected.add("spine");
		expected.add("lobe");
		expected.add("hepatic");
		//runAssert("She has a cyst in the lumbar spine and upper left hepatic lobe.", expected);
		
		expected.clear();
		expected.add("liver");
		expected.add("kidney");
		expected.add("stomach");
		runAssert("She has a cyst in the liver, kidney, and stomach.", expected);
		
		expected.clear();
		expected.add("lupron");
		expected.add("xtandi");
		expected.add("zytiga");
		runAssert("He is on Lupron, Xtandi, and Zytiga and is also taking a zinc supplement", expected);
		
		expected.clear();
		expected.add("patient");
		expected.add("cancer");
		runAssert("Follow up ultrasound recommended in this postmenopausal patient with ovarian cancer.", expected);
		

		expected.clear();
		expected.add("negative");
		expected.add("cancer");
		expected.add("ca125");
		//runAssert("No follow up ultrasound recommended in this postmenopausal post ovarian cancer treated patient as new labs negative for ca125.", expected);
	}
	
	
	@Test 
	public void processFailing() throws Exception{
		HashSet<String> expected = new HashSet<>();
			
		expected.clear();
		expected.add("cyst");
		//runAssert("She was diagnosed with a simple cyst and the cyst is stable.", expected);
	
	}
	
	private void runAssert(String sentenceText, HashSet<String> expected) throws Exception{
		Sentence sentence = TestDataProvider.getSentences(sentenceText).get(0);
		
		sentence = ngramProcessor.process(sentence, new NGramsHardCodedProvider().getNGrams());
		List<WordToken> tokens = stAnnotator.annotate(sentence.getModifiedWordList(), stprovider.getSemanticTypes());
		tokens = partOfSpeechAnnotator.annotate(tokens, entity);
		nounrelationshipProcessor.process(tokens, relationshipInput);
		tokens = prepPhraseProcessor.process(tokens, new PrepositionPhraseProcessingInputFactory().create());
	    
		List<WordToken> annotatedTokens = tokens.stream().filter(a -> a.getPropertyValueType()== PropertyValueTypes.PrepPhraseEnd).collect(Collectors.toList());
		
		assertEquals(expected.size(), annotatedTokens.size());
		for(WordToken t: annotatedTokens){
		   assertTrue(expected.contains(t.getToken()));
		}
	}
	
}
