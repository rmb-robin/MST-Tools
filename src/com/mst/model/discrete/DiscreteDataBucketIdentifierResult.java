package com.mst.model.discrete;

public class DiscreteDataBucketIdentifierResult {
	private boolean isCompliant;
	private String bucketName;
	private Followup expectedFollowup; 
	
	public boolean getIsCompliant() {
		return isCompliant;
	}
	public void setIsCompliant(boolean isCompliant) {
		this.isCompliant = isCompliant;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public Followup getExpectedFollowup() {
		return expectedFollowup;
	}
	public void setExpectedFollowup(Followup expectedFollowup) {
		this.expectedFollowup = expectedFollowup;
	}
}
