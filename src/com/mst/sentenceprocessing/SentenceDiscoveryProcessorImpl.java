package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.NgramsSentenceProcessor;
import com.mst.interfaces.sentenceprocessing.PartOfSpeechAnnotator;
import com.mst.interfaces.sentenceprocessing.PrepositionPhraseProcessor;
import com.mst.interfaces.sentenceprocessing.SemanticTypeSentenceAnnotator;
import com.mst.interfaces.sentenceprocessing.SentenceDiscoveryProcessor;
import com.mst.interfaces.sentenceprocessing.SentenceMeasureNormalizer;
import com.mst.interfaces.sentenceprocessing.VerbProcessor;
import com.mst.interfaces.sentenceprocessing.WordEmbeddingProcessor;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.RecommandationRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class SentenceDiscoveryProcessorImpl implements SentenceDiscoveryProcessor{

	private SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput;
	private SentenceFactory sentenceFactory;
	
	private NgramsSentenceProcessor ngramProcessor;  
	private PartOfSpeechAnnotator partOfSpeechAnnotator;
	private SemanticTypeSentenceAnnotator stAnnotator;
	private SentenceMeasureNormalizer sentenceMeasureNormalizer;
	private WordEmbeddingProcessor wordEmbeddingProcessor; 
	private VerbProcessor verbProcessor; 
	
	public SentenceDiscoveryProcessorImpl(){
		sentenceFactory = new SentenceFactory();
		ngramProcessor = new NGramsSentenceProcessorImpl();
		partOfSpeechAnnotator = new PartOfSpeechAnnotatorImpl();
		stAnnotator = new SemanticTypeSentenceAnnotatorImpl();
		sentenceMeasureNormalizer = new SentenceMeasureNormalizerImpl();
		wordEmbeddingProcessor = new WordEmbeddingProcesseorImpl();
		verbProcessor = new VerbProcessorImpl();
	}
	
	public void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput){
		this.sentenceProcessingMetaDataInput = sentenceProcessingMetaDataInput;
	}
	
	
	public List<SentenceDiscovery> process(RecommandationRequest request) throws Exception{
		
		List<Sentence> sentences = sentenceFactory.getSentences(request.getText(),"","",request.getSource());
		List<SentenceDiscovery> discoveries = new ArrayList<>();
		for(Sentence sentence: sentences){
			sentence = ngramProcessor.process(sentence,this.sentenceProcessingMetaDataInput.getNgramsInput());
			List<WordToken> tokens = stAnnotator.annotate(sentence.getModifiedWordList(),this.sentenceProcessingMetaDataInput.getSemanticTypes());
			
			sentence.setTokenRelationships(new ArrayList<TokenRelationship>());
			tokens = partOfSpeechAnnotator.annotate(tokens, this.sentenceProcessingMetaDataInput.getPartOfSpeechAnnotatorEntity());
			tokens = sentenceMeasureNormalizer.Normalize(tokens, true,true);
			tokens = verbProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getVerbProcessingInput());
			sentence.setModifiedWordList(tokens);
			
			List<TokenRelationship> wordEmbeddings = wordEmbeddingProcessor.process(tokens);
			discoveries.add(convert(sentence, wordEmbeddings));
		}
		return discoveries;
	}
	
	private SentenceDiscovery convert(Sentence sentence, List<TokenRelationship> wordEmbeddings){
		SentenceDiscovery sentenceDiscovery = new SentenceDiscovery();
		sentenceDiscovery.setWordEmbeddings(wordEmbeddings);
		sentenceDiscovery.setModifiedWordList(sentence.getModifiedWordList());
		sentenceDiscovery.setNormalizedSentence(sentence.getNormalizedSentence());
		sentenceDiscovery.setOriginalWords(sentence.getOriginalWords());
		sentenceDiscovery.setOrigSentence(sentence.getOrigSentence());
		sentenceDiscovery.setProcessingDate(sentence.getProcessDate());
		sentenceDiscovery.setSource(sentence.getSource());
		return sentenceDiscovery;
	}
	
}
