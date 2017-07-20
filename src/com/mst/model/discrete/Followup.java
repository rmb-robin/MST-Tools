package com.mst.model.discrete;

import java.util.ArrayList;
import java.util.List;

public class Followup {

	private int duration;
	private String durationMeasure;
	private boolean isNumeric;
	private List<FollowupProcedure> procedures;
	
	public Followup(){
		procedures = new ArrayList<>();
	}
	
	
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


	public List<FollowupProcedure> getProcedures() {
		return procedures;
	}


	public void setProcedures(List<FollowupProcedure> procedures) {
		this.procedures = procedures;
	}


	public void setNumeric(boolean isNumeric) {
		this.isNumeric = isNumeric;
	}
}
