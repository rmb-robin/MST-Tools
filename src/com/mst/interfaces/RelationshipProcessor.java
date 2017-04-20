package com.mst.interfaces;

import java.util.List;

import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public interface RelationshipProcessor {

	List<TokenRelationship> process(List<WordToken> tokens,RelationshipInput input);
	
}
