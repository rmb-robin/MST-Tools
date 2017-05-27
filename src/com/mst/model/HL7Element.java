package com.mst.model;

public class HL7Element {
		private String name;
		private String location;
		private boolean required;
		private String format = null;
		private String defaultValue = null;
		// the delimiter to OUTPUT when concatenating fields within a single HL7 element
		// not to be confused with the HL7 delimiters that come in with the HL7 message (^~\&)
		private String delimiter = " ";
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getLocation() {
			return location;
		}
		
		public void setLocation(String location) {
			this.location = location;
		}
		
		public boolean isRequired() {
			return required;
		}
		
		public void setRequired(boolean required) {
			this.required = required;
		}
		
		public String getFormat() {
			return format;
		}
		
		public void setFormat(String format) {
			this.format = format;
		}
		
		public String getDelimiter() {
			return delimiter;
		}

		public void setDelimiter(String delimiter) {
			this.delimiter = delimiter;
		}

		public String getDefaultValue() {
			if(defaultValue == null || defaultValue.length() == 0) {
				return defaultValue;
			} else {
				return null;
			}
		}
		
		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}
	}

