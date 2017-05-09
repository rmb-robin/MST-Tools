package com.mst.model.discrete;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


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
	private String accessionNumber; 
	private String examDescription; 
	private String modality; 
	private String resultStatus; 
	private String reportFinalizedBy; 
	private DateTime reportFinalizedDate; 
	private int patientAge;
	
	private LocalTime processTime; 
	private LocalDate processDate; 
	
	private String organizationName; 
	
	private List<DiscreteDataCustomField> customFields; 
	
	public DiscreteData(){
		customFields = new ArrayList<>();
	}

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
		return accessionNumber;
	}
	public void setAccessionNumber(String accessionNumber) {
		accessionNumber = accessionNumber;
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


	public List<DiscreteDataCustomField> getCustomFields() {
		return customFields;
	}
	public void setCustomFields(List<DiscreteDataCustomField> customFields) {
		this.customFields = customFields;
	}

	public LocalTime getProcessTime() {
		return processTime;
	}

	public void setTimeStamps() {
		this.processTime  = LocalTime.now();
		this.processDate = LocalDate.now();
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public void setProcessTime(LocalTime processTime) {
		this.processTime = processTime;
	}

	public LocalDate getProcessDate() {
		return processDate;
	} 
	
}
