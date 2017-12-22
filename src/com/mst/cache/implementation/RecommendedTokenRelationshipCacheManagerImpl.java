package com.mst.cache.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mst.cache.interfaces.RecommendedTokenRelationshipCacheManager;
import com.mst.model.metadataTypes.CacheKeyTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;

public class RecommendedTokenRelationshipCacheManagerImpl extends CacheManagerBase<RecommendedTokenRelationship> implements RecommendedTokenRelationshipCacheManager {

	
	public RecommendedTokenRelationshipCacheManagerImpl(){
		super(CacheKeyTypes.recommendedEdges,RecommendedTokenRelationship.class);
	}

	public void reload(String key, List<RecommendedTokenRelationship> relationships) {
		key = createkey(key);
		for(RecommendedTokenRelationship relationship: relationships){
			String json = objectToString(relationship);
			redisManager.addToSet(key, json);
			redisManager.addItem(relationship.getTokenRelationship().getUniqueIdentifier(), json);
		}
	}

	@Override
	public List<RecommendedTokenRelationship> getListByKey(String key) {
		key = createkey(key);
		Set<String> cacheValues = redisManager.getSet(key);
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		
		for(String json: cacheValues){
			result.add(stringToObject(json));
		}
		return result;
	}
	
	

	@Override
	public void addItem(String key, RecommendedTokenRelationship relationship) {
		key = createkey(key);
		String json = objectToString(relationship);
		redisManager.addToSet(key, json);
		redisManager.addItem(relationship.getTokenRelationship().getUniqueIdentifier(), json);
	}

	@Override
	public RecommendedTokenRelationship getItem(String key) {
		return stringToObject(redisManager.getItem(key));
	}
}
 