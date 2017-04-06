package com.mst.interfaces;

import java.util.List;

import com.mst.model.WordToken;
import com.mst.model.gentwo.RelationshipInput;
import com.mst.model.gentwo.TokenRelationship;

public interface RelationshipProcessor {

	List<TokenRelationship> process(List<WordToken> tokens,RelationshipInput input);
	
}
