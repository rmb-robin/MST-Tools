package com.mst.model.discrete;

public class ComplianceDisplayFieldsBucketItem {

	private int AgeBegin, AgeEnd;
	private String MenopausalStatus;
	private double sizeMin, sizeMax;
	private String unitOfMeasure;
	private String followUpTime;
	private String followUpProcedure;
	private String bucketName; 
	
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
	public String getFollowUpTime() {
		return followUpTime;
	}
	public void setFollowUpTime(String followUpTime) {
		this.followUpTime = followUpTime;
	}
	public String getFollowUpProcedure() {
		return followUpProcedure;
	}
	public void setFollowUpProcedure(String followUpProcedure) {
		this.followUpProcedure = followUpProcedure;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
}
