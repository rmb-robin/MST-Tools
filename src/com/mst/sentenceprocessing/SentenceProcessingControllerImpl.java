package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mst.interfaces.sentenceprocessing.AdditionalExistenceEdgeProcesser;
import com.mst.interfaces.sentenceprocessing.DistinctTokenRelationshipDeterminer;
import com.mst.interfaces.sentenceprocessing.DynamicEdgeCreationProcesser;
import com.mst.interfaces.sentenceprocessing.ExistenceToExistenceNoConverter;
import com.mst.interfaces.sentenceprocessing.NegationTokenRelationshipProcessor;
import com.mst.interfaces.sentenceprocessing.NgramsSentenceProcessor;
import com.mst.interfaces.sentenceprocessing.PartOfSpeechAnnotator;
import com.mst.interfaces.sentenceprocessing.PrepPhraseRelationshipProcessor;
import com.mst.interfaces.sentenceprocessing.PrepositionPhraseProcessor;
import com.mst.interfaces.sentenceprocessing.RelationshipProcessor;
import com.mst.interfaces.sentenceprocessing.SemanticTypeSentenceAnnotator;
import com.mst.interfaces.sentenceprocessing.SentenceMeasureNormalizer;
import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.interfaces.sentenceprocessing.VerbExistanceProcessor;
import com.mst.interfaces.sentenceprocessing.VerbPhraseProcessor;
import com.mst.interfaces.sentenceprocessing.VerbProcessor;
import com.mst.model.SentenceToken;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceRequestBase;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.AdditionalExistenceEdgeProcesserImpl;
import com.mst.model.sentenceProcessing.FailedSentence;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingFailures;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
 
public class SentenceProcessingControllerImpl implements  SentenceProcessingController{

	private NgramsSentenceProcessor ngramProcessor;  
	private PrepositionPhraseProcessor prepPhraseProcessor;
	private PartOfSpeechAnnotator partOfSpeechAnnotator;
	private SemanticTypeSentenceAnnotator stAnnotator;
	private RelationshipProcessor nounrelationshipProcessor; 
	private PrepPhraseRelationshipProcessor prepRelationshipProcessor;
	private VerbPhraseProcessor verbPhraseProcessor;
	private VerbProcessor verbProcessor;
    private SentenceFactory sentenceFactory;
	private SentenceMeasureNormalizer sentenceMeasureNormalizer;
	private NegationTokenRelationshipProcessor negationTokenRelationshipProcessor;
	private VerbExistanceProcessor verbExistanceProcessor;
	private AdditionalExistenceEdgeProcesser additionalExistenceEdgeProcesser;
	private SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput;
	private ExistenceToExistenceNoConverter existenceToExistenceNoConverter;
	private DistinctTokenRelationshipDeterminer distinctTokenRelationshipDeterminer;
	private DynamicEdgeCreationProcesser dynamicEdgeCreationProcesser;
	
	public SentenceProcessingControllerImpl(){
		ngramProcessor = new NGramsSentenceProcessorImpl();
		prepPhraseProcessor = new PrepositionPhraseProcessorImpl();
		partOfSpeechAnnotator = new PartOfSpeechAnnotatorImpl();		
		stAnnotator  = new SemanticTypeSentenceAnnotatorImpl();
		nounrelationshipProcessor = new NounRelationshipProcessor();
		prepRelationshipProcessor = new PrepPhraseRelationshipProcessorImpl();
		verbPhraseProcessor = new VerbPhraseProcessorImpl();
		verbProcessor = new VerbProcessorImpl();
		sentenceFactory = new SentenceFactory();
		sentenceMeasureNormalizer = new SentenceMeasureNormalizerImpl();
		negationTokenRelationshipProcessor = new NegationTokenRelationshipProcessorImpl();
		verbExistanceProcessor = new VerbExistanceProcessorImpl();
		additionalExistenceEdgeProcesser = new AdditionalExistenceEdgeProcesserImpl();
		existenceToExistenceNoConverter = new ExistenceToExistenceNoConverterImpl();
		distinctTokenRelationshipDeterminer = new DistinctTokenRelationshipDeterminerImpl();
		dynamicEdgeCreationProcesser = new DynamicEdgeCreationProcesserImpl();
	}
	
	
	public void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput){
		this.sentenceProcessingMetaDataInput = sentenceProcessingMetaDataInput;
	}
		
	public List<Sentence> processSentences(SentenceRequest request) throws Exception{
		List<Sentence> sentences = new ArrayList<>();	
		for(String sentenceText: request.getSenteceTexts()){
			Sentence sentence = sentenceFactory.getSentence(sentenceText,request.getStudy(),request.getPractice(),request.getSource());
			sentence = processSentence(sentence,request.isConvertMeasurements(),request.isConvertLargest());
			sentences.add(sentence);
		}
		return sentences;
	}
			
	public SentenceProcessingResult reprocessSentences(List<Sentence> sentences){
		return processSentences(sentences, true,true);
	}

	private SentenceProcessingResult processSentences(List<Sentence> sentences,boolean isConvertMeasurements,boolean isConvertLargest){
		SentenceProcessingResult result = new SentenceProcessingResult();
		
		for(Sentence sentence: sentences){
			try{
				processSentence(sentence,isConvertMeasurements,isConvertLargest);
				sentence.setProcessDate();
			}
			catch(Exception ex){
				if(result.getFailures()==null)
					result.setFailures(new SentenceProcessingFailures());
				FailedSentence failedSentence = new FailedSentence();
				failedSentence.setError(ex.getLocalizedMessage());
				failedSentence.setSentence(sentence.getOrigSentence());
				result.getFailures().getFailedSentences().add(failedSentence);
				sentence.setDidFail(true);
			}
		}
		result.setSentences(sentences);
		return result;
	}
	
	public SentenceProcessingResult processText(SentenceTextRequest request) throws Exception {		
		
		List<Sentence> sentences = sentenceFactory.getSentences(request.getText(),request.getStudy(),request.getPractice(),request.getSource());
		return processSentences(sentences, request.isConvertMeasurements(),request.isConvertLargest());
	}
	
	
	private Sentence processSentence(Sentence sentence, boolean isConvertMeasurements,boolean isConvertLargest ) throws Exception{
		sentence = ngramProcessor.process(sentence,this.sentenceProcessingMetaDataInput.getNgramsInput());
		List<WordToken> tokens = stAnnotator.annotate(sentence.getModifiedWordList(),this.sentenceProcessingMetaDataInput.getSemanticTypes());
		
		sentence.setTokenRelationships(new ArrayList<TokenRelationship>());
		tokens = partOfSpeechAnnotator.annotate(tokens, this.sentenceProcessingMetaDataInput.getPartOfSpeechAnnotatorEntity());
		tokens = sentenceMeasureNormalizer.Normalize(tokens, isConvertMeasurements,isConvertLargest);
		
		tokens = verbProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getVerbProcessingInput());
		sentence.getTokenRelationships().addAll(nounrelationshipProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getNounRelationshipsInput()));
		tokens = prepPhraseProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getPhraseProcessingInput());
	//	sentence.getTokenRelationships().addAll(prepRelationshipProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getPhraseRelationshipMappings()));
		tokens = verbPhraseProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getVerbPhraseInput());
		
		
		List<TokenRelationship> negationRelationships =negationTokenRelationshipProcessor.process(tokens);
		sentence.getTokenRelationships().addAll(negationRelationships); 

		sentence.getTokenRelationships().addAll(verbExistanceProcessor.process(sentence));
		
		TokenRelationship additionalExistence = additionalExistenceEdgeProcesser.process(sentence);
		if(additionalExistence!=null)
			sentence.getTokenRelationships().add(additionalExistence);
	
		sentence.setTokenRelationships(existenceToExistenceNoConverter.convertExistenceNo(negationRelationships,sentence.getTokenRelationships()));
		
		sentence.getTokenRelationships().addAll(dynamicEdgeCreationProcesser.
					process(this.sentenceProcessingMetaDataInput.getDynamicEdgeCreationRules(), sentence));
		
		sentence.setModifiedWordList(tokens);
		List<TokenRelationship> distinctTokenRelations = distinctTokenRelationshipDeterminer.getDistinctTokenRelationships(sentence);
		sentence.setTokenRelationships(distinctTokenRelations);
		return sentence;
	}
	
	
}
