package com.mst.util.test;

import java.util.List;

import com.mst.interfaces.NounRelationshipInputProvider;
import com.mst.model.gentwo.NounRelationship;
import com.mst.model.gentwo.NounRelationshipInput;

public class NounRelationshipInputProviderFileImpl implements NounRelationshipInputProvider {

	private String getFullFilePath(){
		return System.getProperty("user.dir") + "\\testData\\nounrelationships.txt";
	}
	
	private int maxDistance;
	public NounRelationshipInput get(String frameName, int maxDistance) {
		this.maxDistance = maxDistance;
		NounRelationshipInput nounRelationshipInput = new NounRelationshipInput();
		nounRelationshipInput.setFrameName(frameName);
		List<String> lines = TestDataProvider.readLines(getFullFilePath());
		for(String line: lines){
			nounRelationshipInput.getNounRelationships().add(getNounRelationship(line));
		}
		return nounRelationshipInput;
	}
	
	private NounRelationship getNounRelationship(String line)
	{
		String[] values = line.split(",");
		NounRelationship nounRelationship = new NounRelationship();
		nounRelationship.setFromToken(values[0]);
		nounRelationship.setFromSemanticType(isSemantic(values[1]));
		
		nounRelationship.setToToken(values[2]);
		nounRelationship.setToSemanticType(isSemantic(values[3]));
		
		nounRelationship.setMaxDistance(getDistance(values[4]));
		nounRelationship.setEdgeName(values[5]);
		return nounRelationship;
		
	}
	
	private int getDistance(String val){
		if(val.equals("null")) return this.maxDistance;
		return Integer.parseInt(val);
	}
	
	private boolean isSemantic(String val){
		if(val.toLowerCase().equals("f")) return false;
		return true;
	}
}
