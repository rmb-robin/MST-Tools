package com.mst.interfaces;

import java.util.List;

import com.mst.model.WordToken;
import com.mst.model.gentwo.NounRelationshipInput;
import com.mst.model.gentwo.TokenRelationship;

public interface NounRelationshipProcessor {

	List<TokenRelationship> process(List<WordToken> tokens,List<NounRelationshipInput> inputs);
	
}
