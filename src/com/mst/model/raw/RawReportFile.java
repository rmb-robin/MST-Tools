package com.mst.model.raw;

import java.time.LocalDate;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;

@Entity("rawreportfiles")
public class RawReportFile {

	@Id
	@JsonSerialize(using=ObjectIdJsonSerializer.class)
	private ObjectId id;
	private String content; 
	private LocalDate submittedDate; 
	private LocalDate processedDate;
	private boolean isProcessed;
	private String orgId; 

	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public LocalDate getSubmittedDate() {
		return submittedDate;
	}
	public void setSubmittedDate(LocalDate submittedDate) {
		this.submittedDate = submittedDate;
	}
	public LocalDate getProcessedDate() {
		return processedDate;
	}
	public void setProcessedDate(LocalDate processedDate) {
		this.processedDate = processedDate;
	}
	public boolean getIsProcessed() {
		return isProcessed;
	}
	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	} 
	

}
