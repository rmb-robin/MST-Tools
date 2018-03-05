package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.NgramsSentenceProcessor;
import com.mst.interfaces.sentenceprocessing.PartOfSpeechAnnotator;
import com.mst.interfaces.sentenceprocessing.PrepositionPhraseProcessor;
import com.mst.interfaces.sentenceprocessing.RecommandedSubjectAnnotator;
import com.mst.interfaces.sentenceprocessing.RecommendedNegativeRelationshipFactoryImpl;
import com.mst.interfaces.sentenceprocessing.SemanticTypeSentenceAnnotator;
import com.mst.interfaces.sentenceprocessing.SentenceDiscoveryProcessor;
import com.mst.interfaces.sentenceprocessing.SentenceMeasureNormalizer;
import com.mst.interfaces.sentenceprocessing.VerbExistanceProcessor;
import com.mst.interfaces.sentenceprocessing.VerbProcessor;
import com.mst.interfaces.sentenceprocessing.WordEmbeddingProcessor;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.RecommandationRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.RecommandedNounPhraseResult;
import com.mst.model.sentenceProcessing.RecommandedSubjectAnnotatorImpl;
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
	private RecommandedSubjectAnnotator subjectAnnotator; 
	private RecommendedNegativeRelationshipFactoryImpl negativeRelationshipfactory;
	private VerbExistanceProcessor verbExistanceProcessor; 
	
	public SentenceDiscoveryProcessorImpl(){
		sentenceFactory = new SentenceFactory();
		ngramProcessor = new NGramsSentenceProcessorImpl();
		partOfSpeechAnnotator = new PartOfSpeechAnnotatorImpl();
		stAnnotator = new SemanticTypeSentenceAnnotatorImpl();
		sentenceMeasureNormalizer = new SentenceMeasureNormalizerImpl();
		wordEmbeddingProcessor = new WordEmbeddingProcesseorImpl();
		verbProcessor = new VerbProcessorImpl();
		subjectAnnotator = new RecommandedSubjectAnnotatorImpl();
		negativeRelationshipfactory = new RecommendedNegativeRelationshipFactoryImpl();
		verbExistanceProcessor = new VerbExistanceProcessorImpl();
	}

	public void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput){
		this.sentenceProcessingMetaDataInput = sentenceProcessingMetaDataInput;
	}
	
	
	public List<SentenceDiscovery> process(SentenceTextRequest request) throws Exception{
		
		List<Sentence> sentences = sentenceFactory.getSentences(request.getText(),"","",request.getSource());
		List<SentenceDiscovery> discoveries = new ArrayList<>();
		for(Sentence sentence: sentences){
			sentence = ngramProcessor.process(sentence,this.sentenceProcessingMetaDataInput.getNgramsInput());
			List<WordToken> tokens = stAnnotator.annotate(sentence.getModifiedWordList(),this.sentenceProcessingMetaDataInput.getSemanticTypes());
			
			sentence.setTokenRelationships(new ArrayList<TokenRelationship>());
			tokens = partOfSpeechAnnotator.annotate(tokens, this.sentenceProcessingMetaDataInput.getPartOfSpeechAnnotatorEntity());
			tokens = sentenceMeasureNormalizer.Normalize(tokens, true,true);
			tokens = verbProcessor.process(tokens, this.sentenceProcessingMetaDataInput.getVerbProcessingInput());
	
			tokens = filterTokens(tokens);
			List<RecommendedTokenRelationship> wordEmbeddings = wordEmbeddingProcessor.process(tokens); 			sentence.setModifiedWordList(tokens);
			//SentenceDiscovery discovery =  convert(sentence, nounPhraseResult);		
		//	subjectAnnotator.annotate(discovery);
		//	discovery.getWordEmbeddings().addAll(negativeRelationshipfactory.create(discovery.getModifiedWordList()));
			discovery.getWordEmbeddings().addAll(verbExistanceProcessor.processDiscovery(discovery));
			discoveries.add(discovery);
		}
		return discoveries;
	}
	
	private List<WordToken> filterTokens(List<WordToken> tokens){
		List<WordToken> result = new ArrayList<>();
		HashSet<String> byPassPOS = new HashSet<>();
		byPassPOS.add(PartOfSpeachTypes.DET);
		byPassPOS.add(PartOfSpeachTypes.PUNCTUATION);
	
		for(WordToken token: tokens){
			if(token.getPos()!=null && byPassPOS.contains(token.getPos())) continue;
			
			result.add(token);	
		}
		return result;
	}
	
	private SentenceDiscovery convert(Sentence sentence, RecommandedNounPhraseResult nounPhraseResult){
		SentenceDiscovery sentenceDiscovery = new SentenceDiscovery();
		sentenceDiscovery.setWordEmbeddings(nounPhraseResult.getRecommandedTokenRelationships());
		sentenceDiscovery.setNounPhraseIndexes(nounPhraseResult.getNounPhraseIndexes());
		sentenceDiscovery.setModifiedWordList(sentence.getModifiedWordList());
		sentenceDiscovery.setNormalizedSentence(getNormalizeSentenceFromTokens(sentence.getModifiedWordList()));
		sentenceDiscovery.setOriginalWords(sentence.getOriginalWords());
		sentenceDiscovery.setOrigSentence(sentence.getOrigSentence());
		sentenceDiscovery.setProcessingDate(sentence.getProcessDate());
		sentenceDiscovery.setSource(sentence.getSource());
		sentenceDiscovery.setStudy(sentence.getStudy());
		sentenceDiscovery.setPractice(sentence.getPractice());
		return sentenceDiscovery;
	}

	private String getNormalizeSentenceFromTokens(List<WordToken> tokens){
		String result = "";
		for(WordToken token: tokens){
			result += token.getToken() + " ";
		}
		return result;
	}
}
