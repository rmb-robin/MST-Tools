package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("dynamicedgecreationrules")
public class DynamicEdgeCreationRule {


	@Id
	private ObjectId id;
	private String name; 
	private List<DynamicEdgeCondition> conditions; 
	
	private String edgeName; 
	private List<String> toEdgeNames; 
	private String fromToken;
	private boolean isFromTokenSementicType; 
	private List<String> fromEdgeNames; 
	private String toToken;
	private boolean isToTokenSementicType; 
	
	
	public DynamicEdgeCreationRule(){
		conditions = new ArrayList<>();
		toEdgeNames = new ArrayList<>();
		fromEdgeNames = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DynamicEdgeCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<DynamicEdgeCondition> conditions) {
		this.conditions = conditions;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getEdgeName() {
		return edgeName;
	}

	public void setEdgeName(String edgeName) {
		this.edgeName = edgeName;
	}

	public List<String> getToEdgeNames() {
		return toEdgeNames;
	}

	public void setToEdgeNames(List<String> toEdgeNames) {
		this.toEdgeNames = toEdgeNames;
	}

	public String getFromToken() {
		return fromToken;
	}

	public void setFromToken(String fromToken) {
		this.fromToken = fromToken;
	}

	public boolean isFromTokenSementicType() {
		return isFromTokenSementicType;
	}

	public void setFromTokenSementicType(boolean isFromTokenSementicType) {
		this.isFromTokenSementicType = isFromTokenSementicType;
	}

	public List<String> getFromEdgeNames() {
		return fromEdgeNames;
	}

	public void setFromEdgeNames(List<String> fromEdgeNames) {
		this.fromEdgeNames = fromEdgeNames;
	}

	public String getToToken() {
		return toToken;
	}

	public void setToToken(String toToken) {
		this.toToken = toToken;
	}

	public boolean isToTokenSementicType() {
		return isToTokenSementicType;
	}

	public void setToTokenSementicType(boolean isToTokenSementicType) {
		this.isToTokenSementicType = isToTokenSementicType;
	}
	
	
}
