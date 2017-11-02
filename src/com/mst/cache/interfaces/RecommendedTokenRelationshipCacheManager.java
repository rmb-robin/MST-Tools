package com.mst.cache.interfaces;

import java.util.List;

import com.mst.model.recommandation.RecommandedTokenRelationship;

public interface RecommendedTokenRelationshipCacheManager {

	void reload(String key, List<RecommandedTokenRelationship> relations);
	List<RecommandedTokenRelationship> getListByKey(String key);
	void addItem(String key, RecommandedTokenRelationship relation);
	RecommandedTokenRelationship getItem(String key);
}
