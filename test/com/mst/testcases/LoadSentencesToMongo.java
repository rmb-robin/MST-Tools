package com.mst.testcases;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;

public class LoadSentencesToMongo {

	
	@Test
	public void process() throws Exception{
		processSentence("The simple cyst measures 3.5 mm");
		processSentence("She has a 3.5 mm simple cyst");
		processSentence("There is a simple 3.5 mm simple cyst");
		processSentence("The simple cyst measures 3.5 mm");
		processSentence("The simple cyst is 3.5 mm");
		processSentence("CT scan demonstrates a simple cyst in the right ovary.");
		processSentence("CT scan demonstrates a simple ovarian cyst.");  
		processSentence("CT scan reveals a simple cyst in the right ovary.");
		processSentence("CT scan shows a simple cyst in the right ovary.");
		processSentence("The cyst is simple.");
		processSentence("The cyst appears simple.");
		processSentence("There is no cyst.");
		processSentence("CT does not demonstrate a cyst."); 
	}

	
	private void processSentence(String text) throws Exception{
		Sentence sentence = getSentence(text);
		write(sentence);	
	}
	
	private void write(Sentence sentence){
		SentenceDb dbObj = SentenceConverter.convertToDocument(sentence);
		getDatastore().save(dbObj);
	}
	
	@Test
	public void loadMetaData(){
		SentenceProcessingMetaDataInput input =new SentenceProcessingHardcodedMetaDataInputFactory().create();
		Datastore ds = getDatastore();
		ds.delete(ds.createQuery(SentenceProcessingMetaDataInput.class));
		ds.save(input);
	}
	
	private Sentence getSentence(String text) throws Exception{
		SentenceProcessingControllerImpl controller = new  SentenceProcessingControllerImpl();
		controller.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
		List<String> input = new ArrayList<>();
		input.add(text);
		List<Sentence> sentences = controller.processSentences(input);
		Sentence result = sentences.get(0);
		return result;
	}
	
	
	protected Datastore getDatastore() {
		Morphia morphia = new Morphia();
    	
//    	morphia.mapPackage("com.mst.model.morphia");

    	Datastore datastore;
		try {
			datastore = morphia.createDatastore(new MongoClient("10.210.192.4"), "test");
	    	datastore.ensureIndexes();
	    	return datastore;
	    	
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

    }
}
