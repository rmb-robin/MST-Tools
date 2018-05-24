package com.mst.model.raw;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;
import com.mst.util.LocalDateConverter;

@Converters(LocalDateConverter.class)
@Entity("rawreportfiles")
public class RawReportFile {

	
	public  RawReportFile() {
		submittedDates = new ArrayList<>();
	}
	
	@Id
	@JsonSerialize(using=ObjectIdJsonSerializer.class)
	private ObjectId id;
	private String content; 
	private List<LocalDate> submittedDates; 
	private boolean isProcessed;
	private String orgId; 
	private String orgName; 
	private boolean isProcessingtypeSentenceDiscovery;
	
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
	public List<LocalDate> getSubmittedDates() {
		return submittedDates;
	}
	public void setSubmittedDates(List<LocalDate> submittedDates) {
		this.submittedDates = submittedDates;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public boolean getIsProcessingtypeSentenceDiscovery() {
		return isProcessingtypeSentenceDiscovery;
	}
	public void setIsProcessingtypeSentenceDiscovery(boolean isProcessingtypeSentenceDiscovery) {
		this.isProcessingtypeSentenceDiscovery = isProcessingtypeSentenceDiscovery;
	} 
	

}
