package com.mst.cache.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mst.cache.interfaces.RecommendedTokenRelationshipCacheManager;
import com.mst.model.metadataTypes.CacheKeyTypes;
import com.mst.model.recommandation.RecommandedTokenRelationship;

public class RecommendedTokenRelationshipCacheManagerImpl extends CacheManagerBase<RecommandedTokenRelationship> implements RecommendedTokenRelationshipCacheManager {

	
	public RecommendedTokenRelationshipCacheManagerImpl(){
		super(CacheKeyTypes.recommendedEdges,RecommandedTokenRelationship.class);
	}

	public void reload(String key, List<RecommandedTokenRelationship> relationships) {
		key = createkey(key);
		for(RecommandedTokenRelationship relationship: relationships){
			redisManager.addToSet(key, objectToString(relationship));
		}
	}

	@Override
	public List<RecommandedTokenRelationship> getListByKey(String key) {
		key = createkey(key);
		Set<String> cacheValues = redisManager.getSet(key);
		List<RecommandedTokenRelationship> result = new ArrayList<>();
		
		for(String json: cacheValues){
			result.add(stringToObject(json));
		}
		return result;
	}

	@Override
	public void addItem(String key, RecommandedTokenRelationship relationship) {
		key = createkey(key);
		redisManager.addToSet(key, objectToString(relationship));
	}

}
 