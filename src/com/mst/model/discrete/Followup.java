package com.mst.model.discrete;

public class Followup {

	private int duration;
	private String durationMeasure;
	private boolean isNumeric;
	private String followupDescription; 
	
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getDurationMeasure() {
		return durationMeasure;
	}
	public void setDurationMeasure(String durationMeasure) {
		this.durationMeasure = durationMeasure;
	}
	public boolean getIsNumeric() {
		return isNumeric;
	}
	public void setIsNumeric(boolean isNumeric) {
		this.isNumeric = isNumeric;
	}
	public String getFollowupDescription() {
		return followupDescription;
	}
	public void setFollowupDescription(String followupDescription) {
		this.followupDescription = followupDescription;
	}
	
	
	
	
}
