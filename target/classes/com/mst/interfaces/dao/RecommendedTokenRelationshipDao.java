package com.mst.interfaces.dao;

import java.util.HashSet;
import java.util.List;

import com.mst.model.recommandation.RecommendedTokenRelationship;

public interface RecommendedTokenRelationshipDao extends IDao {

	List<RecommendedTokenRelationship> queryByKey(HashSet<String> keys);
	void saveCollection(List<RecommendedTokenRelationship> recommandedTokenRelationships);
	List<RecommendedTokenRelationship> getVerified();
	
}
