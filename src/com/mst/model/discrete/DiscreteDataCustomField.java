package com.mst.model.discrete;

import com.mst.model.metadataTypes.CustomFieldDataType;

public class DiscreteDataCustomField {

	private String fieldName; 
	private String value; 
	private CustomFieldDataType fieldType;
	
	public DiscreteDataCustomField(){}
	
	public DiscreteDataCustomField(String fieldName, String value, CustomFieldDataType fieldType) {
		this.fieldName = fieldName;
		this.value = value;
		this.fieldType = fieldType;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public CustomFieldDataType getFieldType() {
		return fieldType;
	}
	public void setFieldType(CustomFieldDataType fieldType) {
		this.fieldType = fieldType;
	} 
	
	
}
