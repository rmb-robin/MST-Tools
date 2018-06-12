package com.mst.model.sentenceProcessing;

import com.mst.model.metadataTypes.PropertyValueTypes;

public class IterationDataRule {

	
	private String startRelationship; 
	private int pointValue;
	private String edgeNameTolookfor; 
	private String edgeNameToStopfor; 
	private PropertyValueTypes propertyValueType; 
	private boolean useSameEdgeName; 
	
	
	public String getStartRelationship() {
		return startRelationship;
	}
	public void setStartRelationship(String startRelationship) {
		this.startRelationship = startRelationship;
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

	public PropertyValueTypes getPropertyValueType() {
		return propertyValueType;
	}
	public void setPropertyValueType(PropertyValueTypes propertyValueType) {
		this.propertyValueType = propertyValueType;
	}
	public boolean getUseSameEdgeName() {
		return useSameEdgeName;
	}
	public void setUseSameEdgeName(boolean useSameEdgeName) {
		this.useSameEdgeName = useSameEdgeName;
	} 
	
	public boolean shouldUseSameEdge(){
		return this.getUseSameEdgeName() && this.getEdgeNameTolookfor().equals(this.getStartRelationship());
	}
}
