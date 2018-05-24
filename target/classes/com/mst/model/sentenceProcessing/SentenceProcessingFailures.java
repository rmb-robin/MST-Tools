package com.mst.model.sentenceProcessing;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.mst.util.LocalDateConverter;


@Entity("sentenceProcessingFailures")
@Converters(LocalDateConverter.class)
public class SentenceProcessingFailures {
	
	@Id
	private ObjectId id;
	
	private LocalDate date; 
	private String orgId; 
	private String discreteDataId; 
	private List<FailedSentence> failedSentences; 
	
	public SentenceProcessingFailures(){
		failedSentences = new ArrayList<>();
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getDiscreteDataId() {
		return discreteDataId;
	}

	public void setDiscreteDataId(String discreteDataId) {
		this.discreteDataId = discreteDataId;
	}

	public List<FailedSentence> getFailedSentences() {
		return failedSentences;
	}

	public void setFailedSentences(List<FailedSentence> failedSentences) {
		this.failedSentences = failedSentences;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	
	
	
}
