package com.mst.model.raw;

import java.time.LocalDate;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.mst.model.discrete.DiscreteData;
import com.mst.util.LocalDateConverter;


@Converters(LocalDateConverter.class)
@Entity("hl7parsedRequest")
public class HL7ParsedRequst {

	@Id
	private ObjectId id;
	
	private String rawFileId; 
	
	private String text;
	private DiscreteData discreteData; 
	private String source, practice, study;
	
	private LocalDate processedDate; 
	
	
	public DiscreteData getDiscreteData() {
		return discreteData;
	}
	public void setDiscreteData(DiscreteData discreteData) {
		this.discreteData = discreteData;
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
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getRawFileId() {
		return rawFileId;
	}
	public void setRawFileId(String rawFileId) {
		this.rawFileId = rawFileId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public LocalDate getProcessedDate() {
		return processedDate;
	}
	public void setProcessedDate(LocalDate processedDate) {
		this.processedDate = processedDate;
	}
	
}
