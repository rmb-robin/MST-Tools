package com.mst.interfaces.dao;

import java.util.HashSet;
import java.util.List;

import com.mst.model.sentenceProcessing.RecommandedTokenRelationship;

public interface RecommendedTokenRelationshipDao extends IDao {

	List<RecommandedTokenRelationship> queryByKey(HashSet<String> keys);
	void saveCollection(List<RecommandedTokenRelationship> recommandedTokenRelationships);
	
}
