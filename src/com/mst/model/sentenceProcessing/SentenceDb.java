package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.time.*;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.utils.IndexType;

import com.mst.model.discrete.DiscreteData;
import com.mst.util.LocalDateConverter;

@Entity("sentences")

@Indexes({
    @Index(fields = {@Field(value = "origSentence", type = IndexType.TEXT)})
})
@Converters(LocalDateConverter.class)
public class SentenceDb {
	
	@Id
	private ObjectId id;
	
	
	private int position;
	private long lineId;
	
	private String origSentence;
	private String normalizedSentence;
	private LocalDate processingDate;
	
	private List<String> originalWords;
	private List<WordToken> modifiedWordList = new ArrayList<>();
	private List<TokenRelationship> tokenRelationships = new ArrayList<>();
	private String source, practice, study;

	@Reference
	private DiscreteData discreteData;

	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public long getLineId() {
		return lineId;
	}
	public void setLineId(long lineId) {
		this.lineId = lineId;
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
	public List<TokenRelationship> getTokenRelationships() {
		return tokenRelationships;
	}
	public void setTokenRelationships(List<TokenRelationship> tokenRelationships) {
		this.tokenRelationships = tokenRelationships;
	}

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
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
	public void setId(ObjectId id) {
		this.id = id;
	}
	public ObjectId getId() {
		return id;
	}
	public DiscreteData getDiscreteData() {
		return discreteData;
	}
	public void setDiscreteData(DiscreteData discreteData) {
		this.discreteData = discreteData;
	}
	public LocalDate getProcessDate() {
		return processingDate;
	}
	public void setProcessDate(LocalDate processDate) {
		this.processingDate = processDate;
	}
	
	
	
	
}
