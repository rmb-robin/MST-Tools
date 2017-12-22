package com.mst.testcases;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.mst.cache.implementation.RecommendedTokenRelationshipCacheManagerImpl;
import com.mst.dao.RedisManagerImpl;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;

import static  org.junit.Assert.*;

public class RecommendedTokenRelationshipCacheManagerTest {

	@Test 
	public void reload(){
		RecommendedTokenRelationship relationship = new RecommendedTokenRelationship();
		relationship.setIsVerified(true);
		TokenRelationship r = new TokenRelationship();
		relationship.setTokenRelationship(r);
		
		RecommendedTokenRelationshipCacheManagerImpl manager = new RecommendedTokenRelationshipCacheManagerImpl();
		manager.addItem("cystTest", relationship);
	}
	
	@Test
	public void get(){	
		RecommendedTokenRelationshipCacheManagerImpl manager = new RecommendedTokenRelationshipCacheManagerImpl();
		List<RecommendedTokenRelationship> result = manager.getListByKey("cyst");
		assertEquals(1, result.size());
	}
	
}
