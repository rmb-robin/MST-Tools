package com.mst.cache.interfaces;

import java.util.List;

import com.mst.model.recommandation.RecommendedTokenRelationship;

public interface RecommendedTokenRelationshipCacheManager {

	void reload(String key, List<RecommendedTokenRelationship> relations);
	List<RecommendedTokenRelationship> getListByKey(String key);
	void addItem(String key, RecommendedTokenRelationship relation);
	RecommendedTokenRelationship getItem(String key);
}
