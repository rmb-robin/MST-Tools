package test;


import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.mst.model.sentenceProcessing.IterationDataRule;
import com.mst.model.sentenceProcessing.RelationshipMapping;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.util.MongoConnectionEntity;
import com.mst.sentenceprocessing.SentenceDiscoveryProcessingHardcodedMetaDataInputFactory;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.util.MongoDatastoreProviderDefault;

public class LoadSentenceMetadata {
	
	@Test
	public void loadSentenceMetaData(){
    	SentenceProcessingMetaDataInput input =new SentenceProcessingHardcodedMetaDataInputFactory().create();
    	Datastore ds = new MongoDatastoreProviderDefault().getDefaultDb();
    	ds.delete(ds.createQuery(SentenceProcessingMetaDataInput.class));
    	ds.save(input); 
	}
	
	//@Test 
	public void loadDiscoveryMetaData(){
		SentenceProcessingMetaDataInput input =new SentenceDiscoveryProcessingHardcodedMetaDataInputFactory().create();
    	Datastore ds = new MongoDatastoreProviderDefault().getDefaultDb();
    	ds.delete(ds.createQuery(IterationDataRule.class));
    	ds.save(input.getIterationRuleProcesserInput());
    	ds.delete(ds.createQuery(SentenceProcessingMetaDataInput.class));
    	input.setIterationRuleProcesserInput(null);
    	ds.save(input); 
	}

	@Test
	public void difference(){
		Datastore ds = new MongoDatastoreProviderDefault().getDefaultDb();
		Query<SentenceProcessingMetaDataInput> q = ds.createQuery(SentenceProcessingMetaDataInput.class);
		SentenceProcessingMetaDataInput qaData = q.get();
		
		
		List<RelationshipMapping> s = qaData.getNounRelationshipsInput().getRelationshipMappings();

		for(RelationshipMapping m: s){
			System.out.println(m.getFromToken() + "," + m.getIsFromSemanticType() + "," + m.getToToken() +  "," + 
					m.getIsToSemanticType() + "," + m.getMaxDistance() + "," + m.getEdgeName());
		}
		
		
		
		
//		MongoDatastoreProviderDefault datastoreProviderDefault = new MongoDatastoreProviderDefault();
//		MongoConnectionEntity entity = new MongoConnectionEntity();
//		entity.setIpAddress("10.0.129.219");
//		entity.setDatabaseName("test");
//		datastoreProviderDefault.set(entity);
//		ds = datastoreProviderDefault.getDefaultDb();
//		
//		
		
//		Query<SentenceProcessingMetaDataInput> qProd = ds.createQuery(SentenceProcessingMetaDataInput.class);
//		SentenceProcessingMetaDataInput paData = qProd.get();
//		
//		List<RelationshipMapping> prodMappings = paData.getNounRelationshipsInput().getRelationshipMappings();
//		
//		System.out.println("QA" + s.size());
//		System.out.println("Prod" + prodMappings.size());
//		
	
		
		
	}
	
	
	
	
	
	
	
	
}
