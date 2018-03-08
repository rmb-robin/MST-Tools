package com.mst.model.SentenceQuery;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;

@Entity("sentencequeryresultdisplayfields")
public class SentenceQueryResultDisplayFields {

	@Id
	@JsonSerialize(using=ObjectIdJsonSerializer.class)
	private ObjectId id;
	
	private String organizationId; 
	private boolean showReadingLocation;
	private boolean showPatientMrn;
	private boolean patientDOB;
	private boolean showSex; 
	private boolean showPatientAccount; 
	private boolean showPatientEncoutner; 
	private boolean showVrReportId; 
	private boolean showExamDesc; 
	private boolean showModality; 
	private boolean showResultStatus; 
	private boolean showReportFinalizedBy; 
	private boolean showreportFinalizedById; 
	private boolean showReportFinalizedDate; 
	private boolean showPatientAge; 
	private boolean showProcessingAge; 
	private boolean showBucketName; 
	private boolean showCompliance; 
	private boolean showCustomFields; 
	private boolean showOrderControl; 
	private boolean showDuplicate;
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
	public boolean isShowReadingLocation() {
		return showReadingLocation;
	}
	public void setShowReadingLocation(boolean showReadingLocation) {
		this.showReadingLocation = showReadingLocation;
	}
	public boolean isShowPatientMrn() {
		return showPatientMrn;
	}
	public void setShowPatientMrn(boolean showPatientMrn) {
		this.showPatientMrn = showPatientMrn;
	}
	public boolean isPatientDOB() {
		return patientDOB;
	}
	public void setPatientDOB(boolean patientDOB) {
		this.patientDOB = patientDOB;
	}
	public boolean isShowSex() {
		return showSex;
	}
	public void setShowSex(boolean showSex) {
		this.showSex = showSex;
	}
	public boolean isShowPatientAccount() {
		return showPatientAccount;
	}
	public void setShowPatientAccount(boolean showPatientAccount) {
		this.showPatientAccount = showPatientAccount;
	}
	public boolean isShowPatientEncoutner() {
		return showPatientEncoutner;
	}
	public void setShowPatientEncoutner(boolean showPatientEncoutner) {
		this.showPatientEncoutner = showPatientEncoutner;
	}
	public boolean isShowVrReportId() {
		return showVrReportId;
	}
	public void setShowVrReportId(boolean showVrReportId) {
		this.showVrReportId = showVrReportId;
	}
	public boolean isShowExamDesc() {
		return showExamDesc;
	}
	public void setShowExamDesc(boolean showExamDesc) {
		this.showExamDesc = showExamDesc;
	}
	public boolean isShowModality() {
		return showModality;
	}
	public void setShowModality(boolean showModality) {
		this.showModality = showModality;
	}
	public boolean isShowResultStatus() {
		return showResultStatus;
	}
	public void setShowResultStatus(boolean showResultStatus) {
		this.showResultStatus = showResultStatus;
	}
	public boolean isShowReportFinalizedBy() {
		return showReportFinalizedBy;
	}
	public void setShowReportFinalizedBy(boolean showReportFinalizedBy) {
		this.showReportFinalizedBy = showReportFinalizedBy;
	}
	public boolean isShowreportFinalizedById() {
		return showreportFinalizedById;
	}
	public void setShowreportFinalizedById(boolean showreportFinalizedById) {
		this.showreportFinalizedById = showreportFinalizedById;
	}
	public boolean isShowReportFinalizedDate() {
		return showReportFinalizedDate;
	}
	public void setShowReportFinalizedDate(boolean showReportFinalizedDate) {
		this.showReportFinalizedDate = showReportFinalizedDate;
	}
	public boolean isShowPatientAge() {
		return showPatientAge;
	}
	public void setShowPatientAge(boolean showPatientAge) {
		this.showPatientAge = showPatientAge;
	}
	public boolean isShowProcessingAge() {
		return showProcessingAge;
	}
	public void setShowProcessingAge(boolean showProcessingAge) {
		this.showProcessingAge = showProcessingAge;
	}
	public boolean isShowBucketName() {
		return showBucketName;
	}
	public void setShowBucketName(boolean showBucketName) {
		this.showBucketName = showBucketName;
	}
	public boolean isShowCompliance() {
		return showCompliance;
	}
	public void setShowCompliance(boolean showCompliance) {
		this.showCompliance = showCompliance;
	}
	public boolean isShowCustomFields() {
		return showCustomFields;
	}
	public void setShowCustomFields(boolean showCustomFields) {
		this.showCustomFields = showCustomFields;
	}
	public boolean isShowOrderControl() {
		return showOrderControl;
	}
	public void setShowOrderControl(boolean showOrderControl) {
		this.showOrderControl = showOrderControl;
	}
	public boolean isShowDuplicate() {
		return showDuplicate;
	}
	public void setShowDuplicate(boolean showDuplicate) {
		this.showDuplicate = showDuplicate;
	} 
}
