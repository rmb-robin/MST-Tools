package com.mst.model.discrete;

import java.util.ArrayList;
import java.util.List;

public class ComplianceDisplayFieldsBucketItem {

	private int AgeBegin, AgeEnd;
	private String MenopausalStatus;
	private double sizeMin, sizeMax;
	private String unitOfMeasure;
	private Followup followUp;
	private List<String> followUpProcedures;
	private String bucketName; 
	
	public ComplianceDisplayFieldsBucketItem(){
		followUpProcedures = new ArrayList<>();
	}
	
	public int getAgeBegin() {
		return AgeBegin;
	}
	public void setAgeBegin(int ageBegin) {
		AgeBegin = ageBegin;
	}
	public int getAgeEnd() {
		return AgeEnd;
	}
	public void setAgeEnd(int ageEnd) {
		AgeEnd = ageEnd;
	}
	public String getMenopausalStatus() {
		return MenopausalStatus;
	}
	public void setMenopausalStatus(String menopausalStatus) { 
		MenopausalStatus = menopausalStatus;
	}
	public double getSizeMin() {
		return sizeMin;
	}
	public void setSizeMin(double sizeMin) {
		this.sizeMin = sizeMin;
	}
	public double getSizeMax() {
		return sizeMax;
	}
	public void setSizeMax(double sizemMax) {
		this.sizeMax = sizemMax;
	}
	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}
	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}

	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public Followup getFollowUp() {
		return followUp;
	}
	public void setFollowUp(Followup followUp) {
		this.followUp = followUp;
	}

	public List<String> getFollowUpProcedures() {
		return followUpProcedures;
	}

	public void setFollowUpProcedures(List<String> followUpProcedures) {
		this.followUpProcedures = followUpProcedures;
	}
}
