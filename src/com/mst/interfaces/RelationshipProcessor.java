package com.mst.interfaces;

import java.util.List;

import com.mst.model.gentwo.RelationshipInput;
import com.mst.model.gentwo.TokenRelationship;
import com.mst.model.gentwo.WordToken;

public interface RelationshipProcessor {

	List<TokenRelationship> process(List<WordToken> tokens,RelationshipInput input);
	
}
