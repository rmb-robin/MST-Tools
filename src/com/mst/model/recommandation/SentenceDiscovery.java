package com.mst.model.recommandation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.utils.IndexType;

import com.mst.model.discrete.DiscreteData;
import com.mst.model.sentenceProcessing.BaseSentence;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.Verb;
import com.mst.model.sentenceProcessing.WordToken;

@Entity("sentenceDiscoveriesTest")
@Indexes({
    @Index(fields = {@Field(value = "origSentence", type = IndexType.TEXT)})
})
public class SentenceDiscovery extends BaseSentence {

	@Id
	private ObjectId id;
	
	private String origSentence;
	private String normalizedSentence;
	private LocalDate processingDate;
	private String organizationId; 
	private boolean didFail;
	
	
	private List<String> originalWords;
	private Map<Integer, Integer> nounPhraseIndexes; 
	
	private List<RecommendedTokenRelationship> wordEmbeddings = new ArrayList<>();
	private String source, practice, study;
	
	@Reference
	private DiscreteData discreteData;
	
	public SentenceDiscovery(){
		nounPhraseIndexes = new HashMap<>();
		wordEmbeddings = new ArrayList<>();
	}
	
	public boolean doesSentenceContainVerb(){
		for(WordToken token: modifiedWordList){
			if(token.isVerb()) return true;
		}
		return false;
	}
	
	public void addHasVerb(){
		WordToken hasToken = new WordToken();
		hasToken.setToken("has");
		hasToken.setVerb(new Verb());
		modifiedWordList.add(0, hasToken);
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

	public List<RecommendedTokenRelationship> getWordEmbeddings() {
		return wordEmbeddings;
	}
	public void setWordEmbeddings(List<RecommendedTokenRelationship> wordEmbeddings) {
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

	public String getPractice() {
		return practice;
	}

	public void setPractice(String practice) {
		this.practice = practice;
	}

	public String getStudy() {
		return study;
	}

	public void setStudy(String study) {
		this.study = study;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public boolean isDidFail() {
		return didFail;
	}

	public void setDidFail(boolean didFail) {
		this.didFail = didFail;
	}

	public DiscreteData getDiscreteData() {
		return discreteData;
	}

	public void setDiscreteData(DiscreteData discreteData) {
		this.discreteData = discreteData;
	}
}
