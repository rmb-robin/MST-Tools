package com.mst.model.sentenceProcessing;

public class IterationDataRule {

	
	private String startRelationship; 
	private boolean isLeftDirection; 
	private String stopDirection; 
	private String property; 
	private int index;
	
	
	public String getStartRelationship() {
		return startRelationship;
	}
	public void setStartRelationship(String startRelationship) {
		this.startRelationship = startRelationship;
	}
	public boolean isLeftDirection() {
		return isLeftDirection;
	}
	public void setLeftDirection(boolean isLeftDirection) {
		this.isLeftDirection = isLeftDirection;
	}
	public String getStopDirection() {
		return stopDirection;
	}
	public void setStopDirection(String stopDirection) {
		this.stopDirection = stopDirection;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	} 
		
//	start relationship
//	 fromToken
//	  idx
//	  property - POS, ST, etc.
//	 toToken
//	  idx
//	  property - POS, ST, etc.
//	Direction 
//	stop relationship
//	
}
