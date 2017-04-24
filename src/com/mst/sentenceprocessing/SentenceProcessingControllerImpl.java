package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.NgramsSentenceProcessor;
import com.mst.interfaces.PartOfSpeechAnnotator;
import com.mst.interfaces.PrepPhraseRelationshipProcessor;
import com.mst.interfaces.PrepositionPhraseProcessor;
import com.mst.interfaces.RelationshipProcessor;
import com.mst.interfaces.SemanticTypeSentenceAnnotator;
import com.mst.interfaces.SentenceProcessingController;
import com.mst.interfaces.VerbPhraseProcessor;
import com.mst.interfaces.VerbProcessor;
import com.mst.model.SentenceToken;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
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
	}

	public void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput){
		this.sentenceProcessingMetaDataInput = sentenceProcessingMetaDataInput;
	}
		
	public List<Sentence> processSentences(List<String> sentenceTexts) throws Exception{
		
		List<Sentence> sentences = new ArrayList<>();	
		for(String sentenceText: sentenceTexts){
			Sentence sentence = getSentence(sentenceText);
			sentence = processSentence(sentence);
			sentences.add(sentence);
		}
		return sentences;
	}
	
	private Sentence processSentence(Sentence sentence) throws Exception{
		sentence = ngramProcessor.process(sentence,this.sentenceProcessingMetaDataInput.getNgramsInput());
		List<WordToken> tokens = stAnnotator.annotate(sentence.getModifiedWordList(),this.sentenceProcessingMetaDataInput.getSemanticTypes());
		List<TokenRelationship> tokenRelationships = new ArrayList<>();
		
		tokens = partOfSpeechAnnotator.annotate(tokens, this.sentenceProcessingMetaDataInput.getPartOfSpeechAnnotatorEntity());
		tokens = verbProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getVerbProcessingInput());
		tokenRelationships.addAll(nounrelationshipProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getNounRelationshipsInput()));
		tokens = prepPhraseProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getPhraseProcessingInput());
		tokenRelationships.addAll(prepRelationshipProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getPhraseRelationshipMappings()));
		tokens = verbPhraseProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getVerbPhraseInput());
		sentence.setModifiedWordList(tokens);
		sentence.setTokenRelationships(tokenRelationships);
		return sentence;
	}
	
	
	private Sentence getSentence(String sentenceText){
		SentenceToken sentenceToken = tokenizer.splitSentencesNew(sentenceText).get(0);
		int position = 0;
		
		String cs = cleaner.cleanSentence(sentenceToken.getToken());
		Sentence sentence = new Sentence(null, position++);
		List<String> words =  tokenizer.splitWordsInStrings(cs);
		sentence.setOriginalWords(words);
		
		sentence.setPractice("pratice");
		sentence.setSource(null);
		sentence.setNormalizedSentence(cs);
		sentence.setOrigSentence(sentenceToken.getToken());
		return sentence;
	}
	
}
