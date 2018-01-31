package com.mst.testcases;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mst.interfaces.sentenceprocessing.RelationshipProcessor;
import com.mst.interfaces.sentenceprocessing.VerbExistanceProcessor;
import com.mst.metadataProviders.NGramsHardCodedProvider;
import com.mst.metadataProviders.PartOfSpeechHardcodedAnnotatorEntityProvider;
import com.mst.metadataProviders.RelationshipInputProviderFileImpl;
import com.mst.metadataProviders.SemanticTypeHardCodedProvider;
import com.mst.metadataProviders.TestDataProvider;
import com.mst.metadataProviders.VerbProcessingInputProvider;
import com.mst.model.sentenceProcessing.PartOfSpeechAnnotatorEntity;
import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.VerbProcessingInput;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.NegationTokenRelationshipProcessorImpl;
import com.mst.sentenceprocessing.NounRelationshipProcessor;
import com.mst.sentenceprocessing.PartOfSpeechAnnotatorImpl;
import com.mst.sentenceprocessing.PrepPhraseRelationshipProcessorImpl;
import com.mst.sentenceprocessing.PrepositionPhraseProcessingInputFactory;
import com.mst.sentenceprocessing.PrepositionPhraseProcessorImpl;
import com.mst.sentenceprocessing.SemanticTypeSentenceAnnotatorImpl;
import com.mst.sentenceprocessing.VerbExistanceProcessorImpl;
import com.mst.sentenceprocessing.VerbPhraseInputFactoryImpl;
import com.mst.sentenceprocessing.VerbPhraseProcessorImpl;
import com.mst.sentenceprocessing.VerbProcessorImpl;
import static org.junit.Assert.*;

public class VerbExistanceProcessorTest {

	NGramsSentenceProcessorImpl ngramProcessor = new NGramsSentenceProcessorImpl();
	PrepositionPhraseProcessorImpl prepPhraseProcessor = new PrepositionPhraseProcessorImpl();
	NGramsHardCodedProvider ngramsProvider = new NGramsHardCodedProvider();
	
	PartOfSpeechAnnotatorEntity entity = new PartOfSpeechHardcodedAnnotatorEntityProvider().getPartOfSpeechAnnotatorEntity();
	PartOfSpeechAnnotatorImpl partOfSpeechAnnotator = new PartOfSpeechAnnotatorImpl();		
	
	SemanticTypeSentenceAnnotatorImpl stAnnotator = new SemanticTypeSentenceAnnotatorImpl();
	SemanticTypeHardCodedProvider stprovider = new SemanticTypeHardCodedProvider();
	RelationshipInput relationshipInput = new RelationshipInput();
	RelationshipProcessor nounrelationshipProcessor = new NounRelationshipProcessor();

	PrepPhraseRelationshipProcessorImpl prepRelationshipProcessor = new PrepPhraseRelationshipProcessorImpl();
	VerbPhraseProcessorImpl verbPhraseProcessor = new VerbPhraseProcessorImpl();
	VerbProcessorImpl verbProcessor = new VerbProcessorImpl();
	VerbProcessingInput verbProcessingInput = new VerbProcessingInputProvider().getInput();
	
	NegationTokenRelationshipProcessorImpl negationTokenProcessor = new NegationTokenRelationshipProcessorImpl();
	VerbExistanceProcessor verbExistanceProcessor = new VerbExistanceProcessorImpl();
	
	@Test
	public void passing() throws Exception {
		runAssert("She has a 3.5 mm simple cyst.","existence","she","cyst");
		runAssert("There is a simple 3.5 mm simple cyst.","existence","there","cyst");
		runAssert("CT shows a 3.5 mm simple cyst.","existence","ct","cyst");
		runAssert("CT shows no cyst","existence-no","ct","cyst");
		runAssert("CT reveals no cyst.","existence-no","ct","cyst");
		runAssert("She denies cyst.","existence-no","she","cyst");
		runAssert("She has no cyst.","existence-no","she","cyst");
		runAssert("CT may show a cyst","existence-maybe","ct","cyst"); 
	}
	
	@Test
	public void process() throws Exception{
		//runAssert("stable simple cyst.", "existence", "", "");
		
		
		//		runAssert("the simple cyst measures 3.5 mm","existence--maybe","ct","cyst"); 
	//	runAssert("She could have a cyst", "possibility", "she","cyst");
	}
	
	private void runAssert(String sentenceText, String edgeName, String from, String to) throws Exception{
		Sentence sentence = TestDataProvider.getSentences(sentenceText).get(0);
		
		sentence = ngramProcessor.process(sentence, new NGramsHardCodedProvider().getNGrams());
		List<WordToken> tokens = stAnnotator.annotate(sentence.getModifiedWordList(), stprovider.getSemanticTypes());
		tokens = partOfSpeechAnnotator.annotate(tokens, entity);
		tokens = verbProcessor.process(tokens, verbProcessingInput);
		nounrelationshipProcessor.process(tokens, relationshipInput);
		tokens = prepPhraseProcessor.process(tokens, new PrepositionPhraseProcessingInputFactory().create());
		tokens = verbPhraseProcessor.process(tokens, new VerbPhraseInputFactoryImpl().create());

		List<TokenRelationship> tokenRelationships = negationTokenProcessor.process(tokens);
		sentence.getTokenRelationships().addAll(tokenRelationships); 
		List<TokenRelationship> relationships = verbExistanceProcessor.process(sentence);

		TokenRelationship first = relationships.get(0);
		assertTrue(!relationships.isEmpty());
		
		assertEquals(edgeName, first.getEdgeName());
		assertEquals(from, first.getFromToken().getToken());
		assertEquals(to, first.getToToken().getToken());
		
		//assertEquals(relationship.getFromToken().getToken(), from);
		//	assertEquals(relationship.getToToken().getToken(), to);
	}
}
