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
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

@Entity("sentenceDiscoveries")
@Indexes({
    @Index(fields = {@Field(value = "origSentence", type = IndexType.TEXT)})
})
public class SentenceDiscovery {

	@Id
	private ObjectId id;
	
	private String origSentence;
	private String normalizedSentence;
	private LocalDate processingDate;
	private String organizationId; 
	private boolean didFail;
	
	
	private List<String> originalWords;
	private List<WordToken> modifiedWordList = new ArrayList<>();
	private Map<Integer, Integer> nounPhraseIndexes; 
	
	private List<RecommandedTokenRelationship> wordEmbeddings = new ArrayList<>();
	private String source, practice, study;
	
	@Reference
	private DiscreteData discreteData;
	
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
