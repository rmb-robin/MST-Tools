package com.mst.metadataProviders;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.discrete.ComplianceDisplayFieldsBucketItem;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.model.discrete.Followup;
import com.mst.model.discrete.FollowupProcedure;

public class DiscreteDataComplianceFieldProvider {

	private DisceteDataComplianceDisplayFields result;
	private String getFullFilePath(String filePath){
		return System.getProperty("user.dir") + "\\testData\\" + filePath;
	}
	
	public DisceteDataComplianceDisplayFields get(String orgName, String orgId){
		result = new DisceteDataComplianceDisplayFields();
		List<String> lines = TestDataProvider.readLines(getFullFilePath("disceteDataComplianceDisplayFields.txt"));	
		String dieseName = null;
		for(String line : lines){
			String[] values = line.split(",");
			if(values[0].equals("t")){
				dieseName = values[1];
				result.getBuckets().put(dieseName,new ArrayList<>());
				continue;
			}
			processLine(values, dieseName);
		}
		result.setOrgId(orgId);
		result.setOrgName(orgName);
		return result;
	}
	
	private void processLine(String[] values, String dieseName){

		ComplianceDisplayFieldsBucketItem bucketItem = new ComplianceDisplayFieldsBucketItem();
		bucketItem.setBucketName(values[1]);
		if(tryParseInt(values[2]))
			bucketItem.setAgeBegin(Integer.parseInt(values[2]));
		
		if(tryParseInt(values[3]))
			bucketItem.setAgeBegin(Integer.parseInt(values[3]));
		
		bucketItem.setMenopausalStatus(values[4]);
		
		
		if(tryParseDouble(values[5]))
			bucketItem.setSizeMin(Double.parseDouble(values[5]));
		
		if(tryParseDouble(values[6]))
			bucketItem.setSizeMax(Double.parseDouble(values[6]));
		
		bucketItem.setUnitOfMeasure(values[7]);
		
		bucketItem.setFollowUp(createFollowup(values));
		result.getBuckets().get(dieseName).add(bucketItem);
	}
	
	private List<FollowupProcedure> getFollowupProcedures(String[] values){
		if(values.length<12) return null;
		List<FollowupProcedure> result = new ArrayList<>();
		
		if(values[11]==null) return null;
		if(values[11].equals("")) return null;
		
		String[] edges = values[11].split("\\|");
		for(String edge:edges){
			String [] splits = edge.split(";");
			FollowupProcedure followupProcedure = new FollowupProcedure();
			followupProcedure.setEdgeName(splits[0]);
			followupProcedure.setValue(splits[1]);
			result.add(followupProcedure);
		}
		return result;
	}
	
	private Followup createFollowup(String[] values){
		Followup followup = new Followup();
		if(values[8].equals("t")){
			followup.setIsNumeric(true);
			if(tryParseDouble(values[9]))
				followup.setDuration(Integer.parseInt(values[9]));
		
			followup.setDurationMeasure(values[10]);
			return followup;
		}
		followup.setProcedures(getFollowupProcedures(values));
		return followup;
	}
	
	boolean tryParseInt(String value) {  
	     try {  
	         Integer.parseInt(value);  
	         return true;  
	      } catch (NumberFormatException e) {  
	         return false;  
	      }  
	}
	
	boolean tryParseDouble(String value){	
	     try {  
	         Double.parseDouble(value);  
	         return true;  
	      } catch (NumberFormatException e) {  
	         return false;  
	      }  
	}

	
}
