package com.mst.testcases;

import org.junit.Test;
import org.mongodb.morphia.Datastore;

import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.util.MongoDatastoreProviderDefault;

public class LoadSentenceMetadata {
	
	@Test
	public void loadMetaData(){
    	SentenceProcessingMetaDataInput input =new SentenceProcessingHardcodedMetaDataInputFactory().create();
    	Datastore ds = new MongoDatastoreProviderDefault().getDefaultDb();
    	ds.delete(ds.createQuery(SentenceProcessingMetaDataInput.class));
    	ds.save(input); 
	}
}
