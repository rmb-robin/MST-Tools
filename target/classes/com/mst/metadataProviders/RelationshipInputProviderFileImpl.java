package com.mst.metadataProviders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.NounRelationshipInputProvider;
import com.mst.model.sentenceProcessing.PrepPhraseRelationshipMapping;
import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.RelationshipMapping;

public class RelationshipInputProviderFileImpl implements NounRelationshipInputProvider {

	private String getFullFilePath(String filePath){
		return System.getProperty("user.dir") + File.separator + "testData" +  
				File.separator + "sentenceDiscoverySpecificMetadata"  + File.separator  + filePath;
	}
	
	public RelationshipInput getRelationships(String fileName) {
	
		RelationshipInput nounRelationshipInput = new RelationshipInput();
		List<String> lines = TestDataProvider.readLines(getFullFilePath(fileName));
		for(String line: lines){
			nounRelationshipInput.getRelationshipMappings().add(createRelationshipMapping(line));
		}
		return nounRelationshipInput;
	}


	private RelationshipMapping createRelationshipMapping(String line)
	{
		//token-token,laterality,left,f,f,bpoc,t,f
		String[] values = line.split(",");
		RelationshipMapping relationshipMapping = new RelationshipMapping();
		
		relationshipMapping.setEdgeName(values[0]);
		relationshipMapping.setNamedEdgeName(values[1]);
		
		relationshipMapping.setFromToken(values[2]);
		relationshipMapping.setFromSemanticType(getBoolType(values[3]));
		relationshipMapping.setFromWildcard(getBoolType(values[4]));
		
		relationshipMapping.setToToken(values[5]);
		relationshipMapping.setToSemanticType(getBoolType(values[6]));
		relationshipMapping.setToWildcard(getBoolType(values[7]));
		return relationshipMapping;
	}

	
	private boolean getBoolType(String val){
		if(val.toLowerCase().equals("f")) return false;
		return true;
	}
}
