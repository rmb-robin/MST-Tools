package com.mst.model.sentenceProcessing;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

@Entity("discreteData")
//@Indexes({
//    @Index(fields = @Field("id"))
//})
public class DiscreteData {

	@Id
	private ObjectId id;
	private String readingLocation;
	private String patientMRN;
	private DateTime patientDob;
	private String sex; 
	private String patientAccount; 
	private int patientEncounter; 
	private String vrReportId;
	private String AccessionNumber; 
	private String examDescription; 
	private String modality; 
	private String resultStatus; 
	private String reportFinalizedBy; 
	private DateTime reportFinalizedDate; 
	private int patientAge;
	
	public String getReadingLocation() {
		return readingLocation;
	}
	public void setReadingLocation(String readingLocation) {
		this.readingLocation = readingLocation;
	}
	public String getPatientMRN() {
		return patientMRN;
	}
	public void setPatientMRN(String patientMRN) {
		this.patientMRN = patientMRN;
	}
	public DateTime getPatientDob() {
		return patientDob;
	}
	public void setPatientDob(DateTime patientDob) {
		this.patientDob = patientDob;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getPatientAccount() {
		return patientAccount;
	}
	public void setPatientAccount(String patientAccount) {
		this.patientAccount = patientAccount;
	}
	public int getPatientEncounter() {
		return patientEncounter;
	}
	public void setPatientEncounter(int patientEncounter) {
		this.patientEncounter = patientEncounter;
	}
	public String getVrReportId() {
		return vrReportId;
	}
	public void setVrReportId(String vrReportId) {
		this.vrReportId = vrReportId;
	}
	public String getAccessionNumber() {
		return AccessionNumber;
	}
	public void setAccessionNumber(String accessionNumber) {
		AccessionNumber = accessionNumber;
	}
	public String getExamDescription() {
		return examDescription;
	}
	public void setExamDescription(String examDescription) {
		this.examDescription = examDescription;
	}
	public String getModality() {
		return modality;
	}
	public void setModality(String modality) {
		this.modality = modality;
	}
	public String getResultStatus() {
		return resultStatus;
	}
	public void setResultStatus(String resultStatus) {
		this.resultStatus = resultStatus;
	}
	public String getReportFinalizedBy() {
		return reportFinalizedBy;
	}
	public void setReportFinalizedBy(String reportFinalizedBy) {
		this.reportFinalizedBy = reportFinalizedBy;
	}
	public DateTime getReportFinalizedDate() {
		return reportFinalizedDate;
	}
	public void setReportFinalizedDate(DateTime reportFinalizedDate) {
		this.reportFinalizedDate = reportFinalizedDate;
	}
	public int getPatientAge() {
		return patientAge;
	}
	public void setPatientAge(int patientAge) {
		this.patientAge = patientAge;
	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	} 
	
}
