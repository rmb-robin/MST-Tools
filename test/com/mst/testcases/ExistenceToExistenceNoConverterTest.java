package com.mst.testcases;

import org.junit.Test;

import com.mst.interfaces.sentenceprocessing.RelationshipProcessor;
import com.mst.metadataProviders.NGramsHardCodedProvider;
import com.mst.metadataProviders.PartOfSpeechHardcodedAnnotatorEntityProvider;
import com.mst.metadataProviders.RelationshipInputProviderFileImpl;
import com.mst.metadataProviders.SemanticTypeHardCodedProvider;
import com.mst.metadataProviders.TestDataProvider;
import com.mst.metadataProviders.VerbProcessingInputProvider;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.PartOfSpeechAnnotatorEntity;
import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.VerbProcessingInput;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.ExistenceToExistenceNoConverterImpl;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.NegationTokenRelationshipProcessorImpl;
import com.mst.sentenceprocessing.NounRelationshipProcessor;
import com.mst.sentenceprocessing.PartOfSpeechAnnotatorImpl;
import com.mst.sentenceprocessing.PrepPhraseRelationshipProcessorImpl;
import com.mst.sentenceprocessing.PrepositionPhraseProcessingInputFactory;
import com.mst.sentenceprocessing.PrepositionPhraseProcessorImpl;
import com.mst.sentenceprocessing.SemanticTypeSentenceAnnotatorImpl;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.sentenceprocessing.VerbPhraseInputFactoryImpl;
import com.mst.sentenceprocessing.VerbPhraseProcessorImpl;
import com.mst.sentenceprocessing.VerbProcessorImpl;

import static org.junit.Assert.*;

import java.util.List;
public class ExistenceToExistenceNoConverterTest {

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
	ExistenceToExistenceNoConverterImpl  existenceConverter = new ExistenceToExistenceNoConverterImpl(); 
	
	@Test
	public void process() throws Exception{
	
//		runAssert("No significant  enhancement is noted after contrast administration suggesting a "
//				+ " spectrum of simple cysts as well as proteinaceous/hemorrhagic cyst");
//		runAssert("Skin biopsies showed no lesions");
//		runAssert("no evidence of abdominal aortic aneurysm");
//		runAssert("no evidence of aortic aneurysm");
	//	runAssert("no abdominal aortic aneurysm is visualized");
//		runAssert("chest wall, lower neck, axillae: unremarkable    upper abdomen: unremarkable    bones: nonacute    impression:    no aortic aneurysm or dissection");
//		
		
	//	runAssert("No cyst");
	//	runAssert("no evidence of abdominal aortic aneurysm.");
	
		runAssert("no abdominal aortic aneurysm is visualized");
	}

	private void runAssert(String sentenceText) throws Exception{

		SentenceProcessingControllerImpl imp = new SentenceProcessingControllerImpl();
		imp.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
		SentenceTextRequest request = new SentenceTextRequest();
		request.setText(sentenceText);
		SentenceProcessingResult result = imp.processText(request);
		Sentence sentence = result.getSentences().get(0);
		
		List<TokenRelationship> relationships = sentence.getTokenRelationships();
		
}
}
