package com.mst.testcases;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.query.Query;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.model.requests.RejectedReport;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.MongoDatastoreProviderDefault;

public class QueryBuildHelperTest {

	private MongoDatastoreProvider datastoreProvider;
	
	@Test 
	public void buildQuery(){
		datastoreProvider = new MongoDatastoreProviderDefault();
		Query<SentenceDb> query = datastoreProvider.getDataStore().createQuery(SentenceDb.class); 
		
		query
		.field("origSentence").contains("cyst")
		.field("organizationId").equal("58c6f3ceaf3c420b90160803")
		.field("tokenRelationships.edgeName").hasThisElement("simple cyst modifiers")
		.field("tokenRelationships.edgeName").doesNotHaveThisElement("existence");
		
		
		List<SentenceDb>  r = query.asList();
	}
	
	
}
