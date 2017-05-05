package com.mst.model.requests;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

 
@Entity("rejectedReport")
//@Indexes({
//  @Index(fields = @Field("id"))
//})
public class RejectedReport {

	@Id
	private ObjectId id;
	private String reportId;
	private String accessionNumber;
	private List<String> missingFields; 
	
	public RejectedReport(){
		missingFields = new ArrayList<>();
	}

	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		this.reportId = reportId;
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
	
}
