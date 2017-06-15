package com.mst.model.discrete;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.mst.util.LocalDateConverter;


@Entity("discreteData")
//@Indexes({
//    @Index(fields = @Field("id"))
//})
@Converters(LocalDateConverter.class)
public class DiscreteData {

	@Id
	private ObjectId id;
	private String readingLocation;
	private String patientMRN;
	private DateTime patientDob;
	private String sex; 
	private String patientAccount; 
	private String patientEncounter; 
	private String vrReportId;
	private String accessionNumber; 
	private String examDescription; 
	private String modality; 
	private String resultStatus; 
	private String reportFinalizedBy; 
	private DateTime reportFinalizedDate; 
	private int patientAge;

	private LocalDate processingDate; 
	
	private String organizationName; 
	private String bucketName; 
	
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
	public String getPatientEncounter() {
		return patientEncounter;
	}
	public void setPatientEncounter(String patientEncounter) {
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
		this.accessionNumber = accessionNumber;
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

	public void setTimeStamps() {
		this.processingDate = LocalDate.now();
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
	
	public void setProcessingDate(LocalDate processingDate){
		this.processingDate = processingDate;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	} 
	
}
