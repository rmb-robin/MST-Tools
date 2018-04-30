package test;


import org.junit.Test;
import org.mongodb.morphia.Datastore;

import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.sentenceprocessing.SentenceDiscoveryProcessingHardcodedMetaDataInputFactory;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.util.MongoDatastoreProviderDefault;

public class LoadSentenceMetadata {
	
	//@Test
	public void loadSentenceMetaData(){
    	SentenceProcessingMetaDataInput input =new SentenceProcessingHardcodedMetaDataInputFactory().create();
    	Datastore ds = new MongoDatastoreProviderDefault().getDefaultDb();
    	ds.delete(ds.createQuery(SentenceProcessingMetaDataInput.class));
    	ds.save(input); 
	}
	
	@Test 
	public void loadDiscoveryMetaData(){
		SentenceProcessingMetaDataInput input =new SentenceDiscoveryProcessingHardcodedMetaDataInputFactory().create();
    	Datastore ds = new MongoDatastoreProviderDefault().getDefaultDb();
    	ds.delete(ds.createQuery(SentenceProcessingMetaDataInput.class));
    	ds.save(input); 
	}
}
