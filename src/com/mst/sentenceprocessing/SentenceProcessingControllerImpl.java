package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mst.interfaces.sentenceprocessing.AdditionalExistenceEdgeProcesser;
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
	private Tokenizer tokenizer;
	private SentenceCleaner cleaner;
	private SentenceMeasureNormalizer sentenceMeasureNormalizer;
	private NegationTokenRelationshipProcessor negationTokenRelationshipProcessor;
	private VerbExistanceProcessor verbExistanceProcessor;
	private AdditionalExistenceEdgeProcesser additionalExistenceEdgeProcesser;
	private SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput;

	public SentenceProcessingControllerImpl(){
		ngramProcessor = new NGramsSentenceProcessorImpl();
		prepPhraseProcessor = new PrepositionPhraseProcessorImpl();
		partOfSpeechAnnotator = new PartOfSpeechAnnotatorImpl();		
		stAnnotator  = new SemanticTypeSentenceAnnotatorImpl();
		nounrelationshipProcessor = new NounRelationshipProcessor();
		prepRelationshipProcessor = new PrepPhraseRelationshipProcessorImpl();
		verbPhraseProcessor = new VerbPhraseProcessorImpl();
		verbProcessor = new VerbProcessorImpl();
		tokenizer = new Tokenizer();
		cleaner  = new SentenceCleaner();
		sentenceMeasureNormalizer = new SentenceMeasureNormalizerImpl();
		negationTokenRelationshipProcessor = new NegationTokenRelationshipProcessorImpl();
		verbExistanceProcessor = new VerbExistanceProcessorImpl();
		additionalExistenceEdgeProcesser = new AdditionalExistenceEdgeProcesserImpl();
	}
	
	
	public void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput){
		this.sentenceProcessingMetaDataInput = sentenceProcessingMetaDataInput;
	}
		
	public List<Sentence> processSentences(SentenceRequest request) throws Exception{
		List<Sentence> sentences = new ArrayList<>();	
		for(String sentenceText: request.getSenteceTexts()){
			Sentence sentence = getSentence(sentenceText,request);
			sentence = processSentence(sentence,request);
			sentences.add(sentence);
		}
		return sentences;
	}
			
	public SentenceProcessingResult reprocessSentences(List<Sentence> sentences){
		return processSentences(sentences, null);
	}

	private SentenceProcessingResult processSentences(List<Sentence> sentences,SentenceRequestBase request){
		SentenceProcessingResult result = new SentenceProcessingResult();
		
		for(Sentence sentence: sentences){
			try{
				processSentence(sentence,request);
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
		
		List<Sentence> sentences = getSentences(request);
		return processSentences(sentences, request);
	
	}
	
	private Sentence processSentence(Sentence sentence, SentenceRequestBase request) throws Exception{
		sentence = ngramProcessor.process(sentence,this.sentenceProcessingMetaDataInput.getNgramsInput());
		List<WordToken> tokens = stAnnotator.annotate(sentence.getModifiedWordList(),this.sentenceProcessingMetaDataInput.getSemanticTypes());
		
		sentence.setTokenRelationships(new ArrayList<TokenRelationship>());
		tokens = partOfSpeechAnnotator.annotate(tokens, this.sentenceProcessingMetaDataInput.getPartOfSpeechAnnotatorEntity());
		tokens = verbProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getVerbProcessingInput());
		sentence.getTokenRelationships().addAll(nounrelationshipProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getNounRelationshipsInput()));
		tokens = prepPhraseProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getPhraseProcessingInput());
		sentence.getTokenRelationships().addAll(prepRelationshipProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getPhraseRelationshipMappings()));
		tokens = verbPhraseProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getVerbPhraseInput());
		if(request!=null)
			tokens = sentenceMeasureNormalizer.Normalize(tokens, request.isConvertMeasurements(), request.isConvertLargest());
		
		sentence.getTokenRelationships().addAll(negationTokenRelationshipProcessor.process(tokens));
		sentence.getTokenRelationships().addAll(verbExistanceProcessor.process(sentence));
		
		TokenRelationship additionalExistence = additionalExistenceEdgeProcesser.process(sentence);
		if(additionalExistence!=null)
			sentence.getTokenRelationships().add(additionalExistence);
	
		sentence.setModifiedWordList(tokens);
		return sentence;
	}
	
	private Sentence getSentence(String sentenceText,SentenceRequest request){
		SentenceToken sentenceToken = tokenizer.splitSentencesNew(sentenceText).get(0);
		return createSentence(sentenceToken,request.getStudy(),request.getPractice(),request.getSource(),0);
	}
	
	private List<Sentence> getSentences(SentenceTextRequest request){
		List<SentenceToken> sentenceTokens = tokenizer.splitSentencesNew(request.getText());
		List<Sentence> result = new ArrayList<Sentence>();
		 
		int position = 1;
		for(SentenceToken sentenceToken: sentenceTokens){
			Sentence sentence = createSentence(sentenceToken, request.getStudy(),request.getPractice(), request.getSource(), position);
			result.add(sentence);
			position +=1;
		}
		return result;
	}

	private Sentence createSentence(SentenceToken sentenceToken, String study, String practice, String source,int position){
		String cs = cleaner.cleanSentence(sentenceToken.getToken());
		Sentence sentence = new Sentence(null, position);
		List<String> words =  tokenizer.splitWordsInStrings(cs);
		sentence.setOriginalWords(words);
		
		sentence.setPractice(practice);
		sentence.setSource(source);
		sentence.setStudy(study);
		sentence.setNormalizedSentence(cs);
		sentence.setOrigSentence(sentenceToken.getToken());
		sentence.setProcessDate();
		return sentence;
		
	}
	
	
}
