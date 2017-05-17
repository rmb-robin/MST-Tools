package com.mst.model.requests;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.converters.DateConverter;

import com.mst.util.LocalDateTimeConverter;

 
@Entity("rejectedReport")
@Converters(DateConverter.class)
//@Indexes({
//  @Index(fields = @Field("id"))
//}) 
public class RejectedReport {

	@Id
	private ObjectId id;
	private String organizationName;
	private String accessionNumber;
	private List<String> missingFields; 
	private LocalDate processingDate; 
	private LocalTime processingTime; 
	
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



	public String getOrganizationName() {
		return organizationName;
	}



	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}



	public LocalDate getProcessingDate() {
		return processingDate;
	}

	public LocalTime getProcessingTime() {
		return processingTime;
	}
	
	public void setTimeStamps(){
		this.processingDate = LocalDate.now();
		this.processingTime = LocalTime.now();
	}
}
