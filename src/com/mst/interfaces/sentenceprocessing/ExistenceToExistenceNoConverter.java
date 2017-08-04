package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.sentenceProcessing.TokenRelationship;

public interface ExistenceToExistenceNoConverter {

	List<TokenRelationship> convertExistenceNo(List<TokenRelationship> negationRelations, List<TokenRelationship> existing);
	
}
