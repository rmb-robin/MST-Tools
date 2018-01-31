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
}
