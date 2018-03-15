package com.mst.jsonSerializers;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.HL7Details;
import com.mst.model.HL7FreeText;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.raw.AllHl7Elements;
import com.mst.model.raw.HL7Element;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

public class HL7Processor {

	private String org = null;
	private HL7Details details = null;
	private Message msg = null;
	private Terser terser = null;
	private List<String> missingFields = new ArrayList<>();
	private Logger LOG;
	
	public HL7Processor(String org, HL7Details details, Message msg, Logger log) {
		setOrg(org);
		setDetails(details);
		setMessage(msg);
		this.LOG = log;
		
		terser = new Terser(msg);
	}

	public SentenceTextRequest processMessage() {
		SentenceTextRequest request = new SentenceTextRequest(); 
		
		if(details != null && msg != null) {
			request.setPractice(getOrg());
			request.setStudy(null);
			request.setSource("HL7");
	    	// id
	    	//sentence.setId(getHL7Value(details.getId()));
	    	// date (typically the visit/exam/publish date)
	    	//sentence.setProcedureDate(getHL7Date(details.getDate()));
	    	// free text (entire paragraph)
			request.setText(getHL7Text(details.getText()));
	    	// the bulk of where the HL7 segment values end up
			request.setDiscreteData(getHL7Discrete(details.getDiscrete()));
	    	
			request.setConvertLargest(details.isConvertLargest());
			request.setConvertMeasurements(details.isConvertMeasurements());
			
			// per Lev, required
			request.getDiscreteData().setOrganizationId(getOrg());
			
    		//SentenceMetadata metadata = new SentenceMetadata();
    		//metadata.addSimpleMetadataValue("structuredVersion", details.structuredVersion);
    		//metadata.addSimpleMetadataValue("semanticTypeFilter", details.semanticTypeFilter);
    		//sentence.setMetadata(metadata);
		}
		
		return request;
	}
	
	
	public Map<String, String> getAllAvailableHl7Fields(AllHl7Elements allelements){
		Map<String,String> result = new HashMap<String,String>();
		
		for(String key: allelements.getElements()){
			try{
				String val = terser.get(key);
				if(val!=null){
					result.put(key.replace("/.",""), val);
				}
			}
			catch(Exception ex){
				
			}
		}
		return result;
	}
	
	private DiscreteData getHL7Discrete(List<HL7Element> discrete) {
		DiscreteData data = new DiscreteData();
		
		for(HL7Element element : discrete) {
			try {
				String value = getHL7Value(element);
				
				if(element.getName().equalsIgnoreCase("AccessionNumber")) {
					data.setAccessionNumber(value);
				} else if(element.getName().equalsIgnoreCase("ExamDescription")) {
					data.setExamDescription(value);
				} else if(element.getName().equalsIgnoreCase("Modality")) {
					data.setModality(value);
				} else if(element.getName().equalsIgnoreCase("PatientAccount")) {
					data.setPatientAccount(value);
				} else if(element.getName().equalsIgnoreCase("PatientAge")) {
					data.setPatientAge(Integer.parseInt(value));
				} else if(element.getName().equalsIgnoreCase("PatientDOB")) {
					DateTimeFormatter format = DateTimeFormatter.ofPattern(element.getFormat());
					try {
						data.setPatientDob(LocalDate.parse(value, format));
					} catch(DateTimeParseException e) {
						LOG.error(e.toString());
						missingFields.add(element.getName());
						//data.setPatientDob(LocalDate.parse(element.getDefaultValue(), format));
					}
				} else if(element.getName().equalsIgnoreCase("PatientEncounter")) {
					data.setPatientEncounter(value);
				} else if(element.getName().equalsIgnoreCase("PatientMRN")) {
					data.setPatientMRN(value);
				} else if(element.getName().equalsIgnoreCase("ReadingLocation")) {
					data.setReadingLocation(value);
				} else if(element.getName().equalsIgnoreCase("ReportFinalizedBy")) {
					data.setReportFinalizedBy(value);
				} else if(element.getName().equalsIgnoreCase("ReportFinalizedDate")) {
					DateTimeFormatter format = DateTimeFormatter.ofPattern(element.getFormat());
					try {
						// override for date values that come in as yyyyMMddHHmm (format expects 'ss')
						if(value.length() == 12) {
							value += "00";
						}
						data.setReportFinalizedDate(LocalDate.parse(value, format));
					} catch(Exception e) {
						LOG.error(e.toString());
						missingFields.add(element.getName());
						//data.setReportFinalizedDate(LocalDate.parse(element.getDefaultValue(), format));
					}
				} else if(element.getName().equalsIgnoreCase("ResultStatus")) {
					data.setResultStatus(value);
				} else if(element.getName().equalsIgnoreCase("PatientSex")) {
					data.setSex(value);
				} else if(element.getName().equalsIgnoreCase("VRReportID")) {
					data.setVrReportId(value);
				} else if(element.getName().equalsIgnoreCase("ReportFinalizedByID")) {
					data.setReportFinalizedById(value);
				} else if(element.getName().equalsIgnoreCase("OrderControl")) {
					data.setOrderControl(value);
				}
			   
				else if(element.getName().equalsIgnoreCase("PatientName")) {
				data.setPatientName(value);
			}
			
				else if(element.getName().equalsIgnoreCase("PatientClass")) {
					data.setPatientClass(value);
				}
				
					
				   else if(element.getName().equalsIgnoreCase("PrincipalResultInterpreterID")) {
						data.setPrincipalResultInterpreterID(value);
					}
					
						
				  else if(element.getName().equalsIgnoreCase("PrincipalResultInterpreterName")) {
							data.setPrincipalResultInterpreterName(value);
				  }
				
				  else if(element.getName().equalsIgnoreCase("AssignedPatientLocation")) {
						data.setAssignedPatientLocation(value);
				  }
			
				  else if(element.getName().equalsIgnoreCase("OrderingProviderId")) {
					data.setOrderingProviderId(value);
				  }
				
				  else if(element.getName().equalsIgnoreCase("OrderingProviderName")) {
						data.setOrderingProviderName(value);
					  }
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return data;
	}
	
	private String getHL7Text(HL7FreeText text) {
		StringBuilder sb = new StringBuilder();
		
		for(HL7Element element : text.getHl7Elements()) {
			String value = getHL7Value(element);
			if(value != null) {
				sb.append(value);
				sb.append(text.getDelimiter());
			}
		}
		
		return sb.toString();
	}
	
	private Date getHL7Date(HL7Element element) {
		Date date = null;
		String value = getHL7Value(element);
		
		try {
			date = new SimpleDateFormat(element.getFormat()).parse(value);
			
		} catch(Exception e) {
			e.printStackTrace();
			
			try {
				date = new SimpleDateFormat(element.getFormat()).parse(element.getDefaultValue());
			} catch(Exception e2) {
				e2.printStackTrace();
			}
		}
		
		return date;
	}
	
	private String getHL7Value(HL7Element element) {
		String value = null;
		
		try {
			if(element.getLocation().indexOf("OBX-5") > -1) {
				StringBuilder text = new StringBuilder();
				String val = "";
				
				// OBX-5 contains ~ (tilde) sub-delimiters. The normal terser.get() way of getting this field's
				// data returns only position 1. This workaround loops until a null is found, concatenating everything
				// with a user-defined delimiter. There must be a way to access the full field's value.

				// check for null to end the loop because OBX-5(n) has a variable upper boundary.
				for(int i=0; val != null; i++) {
					try {
						text.append(val);
						text.append(element.getDelimiter());
						String location = element.getLocation().replaceAll("\\(n\\)", "(" + String.valueOf(i) + ")");
						//System.out.println("location: " + location);
						val = terser.get(location);
						
					} catch(Exception e) {
						System.out.println("Exception in for loop: "+ e.toString());
					}
				}
				
				value = text.toString();
				
			} else if(element.getLocation().equalsIgnoreCase("/.OBR-33")) {
				// report finalized by first and last names
				// OBR-33-1 is handled elsewhere and written to ReportFinalizedById
				String lName = terser.get("/.OBR-33-2");
				String fName = terser.get("/.OBR-33-3");
				value = processName(lName) + ", " + processName(fName);
			} 
			
			
		 else if(element.getLocation().equalsIgnoreCase("/.OBR-32")) {
			// report finalized by first and last names
			// OBR-33-1 is handled elsewhere and written to ReportFinalizedById
			String lName = terser.get("/.OBR-32-2");
			String fName = terser.get("/.OBR-32-3");
			value = processName(lName) + ", " + processName(fName);
		}
		
		 else if(element.getLocation().equalsIgnoreCase("/.PID-5")) {
				// report finalized by first and last names
				// OBR-33-1 is handled elsewhere and written to ReportFinalizedById
				String lName = terser.get("/.PID-5-1");
				String fName = terser.get("/.PID-5-2");
				value = processName(lName) + ", " + processName(fName);
			}
			
		 else if(element.getLocation().equalsIgnoreCase("/.OBR-16")) {
				// report finalized by first and last names
				// OBR-33-1 is handled elsewhere and written to ReportFinalizedById
				String lName = terser.get("/.OBR-16-2");
				String fName = terser.get("/.OBR-16-3");
				value = processName(lName) + ", " + processName(fName);
			}	
		else {
				value = terser.get(element.getLocation());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(valueIsNull(value)) {
			if(element.isRequired()) {
				missingFields.add(element.getName());
			}
			if(element.getDefaultValue() != null) {
				value = element.getDefaultValue();
			}
		}
		
		return value;
	}
	
	private String processName(String name) {
		// uppercase the first letter of the incoming value; lowercase the remaining letters 
		// handles spaces and hyphens within names 
		if(name != null && name.trim().length() > 1) {
			StringBuilder sb = new StringBuilder();
			if(name.indexOf(' ') > 0) {
				for(String s : name.split(" ")) {
					sb.append(normalizeName(s));
					sb.append(' ');
				}
			} else if(name.indexOf('-') > 0) {
				for(String s : name.split("-")) {
					sb.append(normalizeName(s));
					sb.append('-');
				}
			} else {
				sb.append(normalizeName(name));
				sb.append(' ');
			}
			
			return sb.deleteCharAt(sb.length()-1).toString();
		} else {
			return name;
		}
	}
	
	private String normalizeName(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
	}
	
	private boolean valueIsNull(String value) {
		if(value == null || value.trim().length() == 0 || value.toUpperCase().startsWith("UNK")) {
			return true;
		} else {
			return false;
		}
	}
	
	public List<String> getMissingFields() {
		return missingFields;
	}
	
	public Terser getTerser() {
		return terser;
	}

	public void setTerser(Terser terser) {
		this.terser = terser;
	}

	public String getOrg() {
		return org;
	}
	
	public void setOrg(String org) {
		this.org = org;
	}
	
	public HL7Details getDetails() {
		return details;
	}

	public void setDetails(HL7Details details) {
		this.details = details;
	}

	public Message getMessage() {
		return msg;
	}

	public void setMessage(Message msg) {
		this.msg = msg;
	}
}


