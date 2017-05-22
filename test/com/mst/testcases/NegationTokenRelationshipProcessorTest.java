package com.mst.testcases;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.mst.interfaces.sentenceprocessing.RelationshipProcessor;
import com.mst.metadataProviders.NGramsHardCodedProvider;
import com.mst.metadataProviders.PartOfSpeechHardcodedAnnotatorEntityProvider;
import com.mst.metadataProviders.RelationshipInputProviderFileImpl;
import com.mst.metadataProviders.SemanticTypeHardCodedProvider;
import com.mst.metadataProviders.TestDataProvider;
import com.mst.metadataProviders.VerbProcessingInputProvider;
import com.mst.model.metadataTypes.PropertyValueTypes;
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
import com.mst.sentenceprocessing.VerbPhraseInputFactoryImpl;
import com.mst.sentenceprocessing.VerbPhraseProcessorImpl;
import com.mst.sentenceprocessing.VerbProcessorImpl;
import static org.junit.Assert.*;

public class NegationTokenRelationshipProcessorTest {

	NGramsSentenceProcessorImpl ngramProcessor = new NGramsSentenceProcessorImpl();
	PrepositionPhraseProcessorImpl prepPhraseProcessor = new PrepositionPhraseProcessorImpl();
	NGramsHardCodedProvider ngramsProvider = new NGramsHardCodedProvider();
	
	PartOfSpeechAnnotatorEntity entity = new PartOfSpeechHardcodedAnnotatorEntityProvider().getPartOfSpeechAnnotatorEntity();
	PartOfSpeechAnnotatorImpl partOfSpeechAnnotator = new PartOfSpeechAnnotatorImpl();		
	
	SemanticTypeSentenceAnnotatorImpl stAnnotator = new SemanticTypeSentenceAnnotatorImpl();
	SemanticTypeHardCodedProvider stprovider = new SemanticTypeHardCodedProvider();
	RelationshipInput relationshipInput = new RelationshipInputProviderFileImpl().getNounRelationships(7);
	RelationshipProcessor nounrelationshipProcessor = new NounRelationshipProcessor();

	PrepPhraseRelationshipProcessorImpl prepRelationshipProcessor = new PrepPhraseRelationshipProcessorImpl();
	VerbPhraseProcessorImpl verbPhraseProcessor = new VerbPhraseProcessorImpl();
	VerbProcessorImpl verbProcessor = new VerbProcessorImpl();
	VerbProcessingInput verbProcessingInput = new VerbProcessingInputProvider().getInput();
	
	NegationTokenRelationshipProcessorImpl negationTokenProcessor = new NegationTokenRelationshipProcessorImpl();
	
	
	@Test
	public void process() throws Exception{
		runAssert("No ovarian mass.","no","mass");
		
		runAssert("She has no cyst.","has","cyst");
		runAssert("She has no ovarian mass.","has","mass");
		runAssert("CT does not show cyst.", "show", "cyst");		
		runAssert("She does not have a cyst.","have","cyst");
		runAssert("She may not have a cyst after all.","have","cyst");
		runAssert("CT may not show the cyst.","show","cyst");
		runAssert("She denies cyst.","deny","cyst");

	}
	
	private void runAssert(String sentenceText, String from, String to) throws Exception{
		Sentence sentence = TestDataProvider.getSentences(sentenceText).get(0);
		
		sentence = ngramProcessor.process(sentence, new NGramsHardCodedProvider().getNGrams());
		List<WordToken> tokens = stAnnotator.annotate(sentence.getModifiedWordList(), stprovider.getSemanticTypes());
		tokens = partOfSpeechAnnotator.annotate(tokens, entity);
		tokens = verbProcessor.process(tokens, verbProcessingInput);
		nounrelationshipProcessor.process(tokens, relationshipInput);
		tokens = prepPhraseProcessor.process(tokens, new PrepositionPhraseProcessingInputFactory().create());
		tokens = verbPhraseProcessor.process(tokens, new VerbPhraseInputFactoryImpl().create());

		List<TokenRelationship> tokenRelationships = negationTokenProcessor.process(tokens);
		TokenRelationship relationship = tokenRelationships.get(0);
		assertEquals(relationship.getFromToken().getToken(), from);
		assertEquals(relationship.getToToken().getToken(), to);
	}
}
