package com.mst.testHelpers;

import java.util.List;

import com.mst.interfaces.NounRelationshipInputProvider;
import com.mst.model.gentwo.RelationshipMapping;
import com.mst.model.gentwo.RelationshipInput;

public class NounRelationshipInputProviderFileImpl implements NounRelationshipInputProvider {

	private String getFullFilePath(){
		return System.getProperty("user.dir") + "\\testData\\nounrelationships.txt";
	}
	
	private int maxDistance;
	public RelationshipInput get(String frameName, int maxDistance) {
		this.maxDistance = maxDistance;
		RelationshipInput nounRelationshipInput = new RelationshipInput();
		nounRelationshipInput.setFrameName(frameName);
		List<String> lines = TestDataProvider.readLines(getFullFilePath());
		for(String line: lines){
			nounRelationshipInput.getRelationshipMappings().add(getNounRelationship(line));
		}
		return nounRelationshipInput;
	}
	
	private RelationshipMapping getNounRelationship(String line)
	{
		String[] values = line.split(",");
		RelationshipMapping nounRelationship = new RelationshipMapping();
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
