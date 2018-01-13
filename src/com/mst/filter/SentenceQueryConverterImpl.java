package com.mst.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.filter.SentenceQueryConverter;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;

import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;

public class SentenceQueryConverterImpl  implements SentenceQueryConverter {

	public SentenceQueryInstance getSTQueryInstance(SentenceQueryInput input) {
		
		for(SentenceQueryInstance instance :input.getSentenceQueryInstances()){
			if(instance.getIsSt())
				return instance; 
		}
		return null;
	}

	public SentenceQueryInput convertST(SentenceQueryInput sentenceQueryInput, SentenceQueryInstance stInstance, SentenceProcessingMetaDataInput metaData){
		Map<String,List<String>> stTypes = metaData.getSemanticTypesByTypeName();
		
		List<String> tokens = new ArrayList<>();
		for(String stToken: stInstance.getTokens()){
			if(!stTypes.containsKey(stToken)) continue; 
			tokens.addAll(stTypes.get(stToken));
		}
		stInstance.setTokens(tokens);
		return sentenceQueryInput;
	}	
}
