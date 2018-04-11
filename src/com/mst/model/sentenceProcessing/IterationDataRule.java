package com.mst.model.sentenceProcessing;

import com.mst.model.metadataTypes.PropertyValueTypes;

public class IterationDataRule {

	
	private String startRelationship; 
	private boolean isLeftDirection; 
	private String property; 
	private int pointValue;
	private String edgeNameTolookfor; 
	private String edgeNameToStopfor; 
	private boolean isEndOfSentenceRule; 
	private PropertyValueTypes propertyValueType; 
	
	
	
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
	
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}

	public int getPointValue() {
		return pointValue;
	}
	public void setPointValue(int pointValue) {
		this.pointValue = pointValue;
	}
	public String getEdgeNameTolookfor() {
		return edgeNameTolookfor;
	}
	public void setEdgeNameTolookfor(String stopEdgeName) {
		this.edgeNameTolookfor = stopEdgeName;
	}
	public String getEdgeNameToStopfor() {
		return edgeNameToStopfor;
	}
	public void setEdgeNameToStopfor(String edgeNameToStopfor) {
		this.edgeNameToStopfor = edgeNameToStopfor;
	}
	public boolean isEndOfSentenceRule() {
		return isEndOfSentenceRule;
	}
	public void setEndOfSentenceRule(boolean isEndOfSentenceRule) {
		this.isEndOfSentenceRule = isEndOfSentenceRule;
	}
	public PropertyValueTypes getPropertyValueType() {
		return propertyValueType;
	}
	public void setPropertyValueType(PropertyValueTypes propertyValueType) {
		this.propertyValueType = propertyValueType;
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
