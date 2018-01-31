package com.mst.metadataProviders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.NounRelationshipInputProvider;
import com.mst.model.sentenceProcessing.PrepPhraseRelationshipMapping;
import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.RelationshipMapping;

public class RelationshipInputProviderFileImpl extends BaseProvider {

	private String getFullFilePath(String filePath){
		return System.getProperty("user.dir") + File.separator + "testData" + File.separator + filePath;
	}
	
	private int maxDistance;
	public RelationshipInput getNounRelationships(int maxDistance) {
		this.maxDistance = maxDistance;
		RelationshipInput nounRelationshipInput = new RelationshipInput();
		List<String> lines = TestDataProvider.readLines(getFullFilePath("nounrelationships.txt"));
		for(String line: lines){
			nounRelationshipInput.getRelationshipMappings().add(getNounRelationship(line));
		}
		return nounRelationshipInput;
	}
	
	public List<PrepPhraseRelationshipMapping> getPrepPhraseRelationshipMapping(){	
		List<String> lines = TestDataProvider.readLines(getFullFilePath("prepphraserelationships.txt"));
		List<PrepPhraseRelationshipMapping> mappings = new ArrayList<PrepPhraseRelationshipMapping>();
		
		for(String line: lines){
			mappings.add(getPrepPhraseRelationship(line));
		}
		return mappings;
	}
	
	private PrepPhraseRelationshipMapping getPrepPhraseRelationship(String line){
	//	on,f,f,LV,f,t,drugpr,t,f,take
		String[] values = line.split(",");
		PrepPhraseRelationshipMapping mapping = new PrepPhraseRelationshipMapping();
		
		mapping.setToken(values[0]);
		mapping.setTokenSemanticType(getBoolType(values[1]));
		mapping.setTokenPOSType(getBoolType(values[3]));
		
		mapping.setPreviousToken(values[3]);
		mapping.setPreviousTokenSemanticType(getBoolType(values[4]));
		mapping.setPreviousTokenPOSType(getBoolType(values[5]));
		
	
		mapping.setPrepObjectToken(values[6]);
		mapping.setPrepObjectTokenSemanticType(getBoolType(values[7]));
		mapping.setPrepObjectTokenPOSType(getBoolType(values[8]));
		mapping.setEdgeName(values[9]);
		return mapping;
		
	}

	private RelationshipMapping getNounRelationship(String line)
	{
		String[] values = line.split(",");
		RelationshipMapping nounRelationship = new RelationshipMapping();
		nounRelationship.setFromToken(values[0]);
		nounRelationship.setFromSemanticType(getBoolType(values[1]));
		
		nounRelationship.setToToken(values[2]);
		nounRelationship.setToSemanticType(getBoolType(values[3]));
		
		nounRelationship.setMaxDistance(getDistance(values[4]));
		nounRelationship.setEdgeName(values[5]);
		nounRelationship.setMaxDistance(this.maxDistance);
		return nounRelationship;
		
	}
	
	private int getDistance(String val){
		if(val.equals("null")) return this.maxDistance;
		return Integer.parseInt(val);
	}
	
	private boolean getBoolType(String val){
		if(val.toLowerCase().equals("f")) return false;
		return true;
	}
}
