package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.sentenceProcessing.DynamicEdgeCreationRule;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;

public interface DynamicEdgeCreationProcesser {

	List<TokenRelationship> process(List<DynamicEdgeCreationRule> rules, Sentence sentence);
	
}
