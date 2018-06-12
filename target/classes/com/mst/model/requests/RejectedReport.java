package com.mst.model.requests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.converters.DateConverter;

import com.mst.util.LocalDateConverter;
import com.mst.util.LocalDateTimeConverter;

 
@Entity("rejectedReport")
@Converters(LocalDateConverter.class)
//@Indexes({
//  @Index(fields = @Field("id"))
//}) 
public class RejectedReport {

	@Id
	private ObjectId id;
	private String organizationId;
	private String organizationName;
	private String accessionNumber;
	private List<String> missingFields; 
	private LocalDate processingDate; 
	private String readingLocation;
	private String rawFileId; 
	
	
	//private LocalDateTime processingTime; 
	
	public RejectedReport(){
		missingFields = new ArrayList<>();
	}

 

	public String getAccessionNumber() {
		return accessionNumber;
	}

	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public List<String> getMissingFields() {
		return missingFields;
	}

	public void setMissingFields(List<String> missingFields) {
		this.missingFields = missingFields;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}



	public String getOrganizationId() {
		return organizationId;
	}



	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}


	public LocalDate getProcessingDate() {
		return processingDate;
	}

//	public LocalDateTime getProcessingTime() {
//		return processingTime;
//	}
	
	public void setTimeStamps(){
		this.processingDate = LocalDate.now();
	//	this.processingTime = LocalDateTime.now();
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getReadingLocation() {
		return readingLocation;
	}

	public void setReadingLocation(String readingLocation) {
		this.readingLocation = readingLocation;
	}



	public String getRawFileId() {
		return rawFileId;
	}



	public void setRawFileId(String rawFileId) {
		this.rawFileId = rawFileId;
	}
}
