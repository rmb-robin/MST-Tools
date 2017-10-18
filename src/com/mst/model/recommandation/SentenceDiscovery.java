package com.mst.model.recommandation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

@Entity("sentenceDiscoveries")
public class SentenceDiscovery {

	@Id
	private ObjectId id;
	
	private String origSentence;
	private String normalizedSentence;
	private LocalDate processingDate;
	
	private List<String> originalWords;
	private List<WordToken> modifiedWordList = new ArrayList<>();
	private Map<Integer, Integer> nounPhraseIndexes; 
	
	private List<RecommandedTokenRelationship> wordEmbeddings = new ArrayList<>();
	private String source;

	
	public SentenceDiscovery(){
		nounPhraseIndexes = new HashMap<>();
		wordEmbeddings = new ArrayList<>();
	}
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getOrigSentence() {
		return origSentence;
	}
	public void setOrigSentence(String origSentence) {
		this.origSentence = origSentence;
	}
	public String getNormalizedSentence() {
		return normalizedSentence;
	}
	public void setNormalizedSentence(String normalizedSentence) {
		this.normalizedSentence = normalizedSentence;
	}
	public LocalDate getProcessingDate() {
		return processingDate;
	}
	public void setProcessingDate(LocalDate processingDate) {
		this.processingDate = processingDate;
	}
	public List<String> getOriginalWords() {
		return originalWords;
	}
	public void setOriginalWords(List<String> originalWords) {
		this.originalWords = originalWords;
	}
	public List<WordToken> getModifiedWordList() {
		return modifiedWordList;
	}
	public void setModifiedWordList(List<WordToken> modifiedWordList) {
		this.modifiedWordList = modifiedWordList;
	}
	public List<RecommandedTokenRelationship> getWordEmbeddings() {
		return wordEmbeddings;
	}
	public void setWordEmbeddings(List<RecommandedTokenRelationship> wordEmbeddings) {
		this.wordEmbeddings = wordEmbeddings;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public Map<Integer, Integer> getNounPhraseIndexes() {
		return nounPhraseIndexes;
	}

	public void setNounPhraseIndexes(Map<Integer, Integer> nounPhraseIndexes) {
		this.nounPhraseIndexes = nounPhraseIndexes;
	}
}
