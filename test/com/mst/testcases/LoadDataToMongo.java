package com.mst.testcases;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;
import com.mst.dao.DisceteDataComplianceDisplayFieldsDaoImpl;
import com.mst.dao.RejectedReportDaoImpl;
import com.mst.dao.SentenceDaoImpl;
import com.mst.interfaces.dao.DisceteDataComplianceDisplayFieldsDao;
import com.mst.interfaces.dao.RejectedReportDao;
import com.mst.metadataProviders.DiscreteDataComplianceFieldProvider;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.RejectedReport;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.util.MongoDatastoreProviderDefault;
import com.sun.scenario.effect.impl.prism.PrImage;

public class LoadDataToMongo {

	
//	@Test
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

	//@Test
	public void loadRejectedReport(){
		RejectedReportDaoImpl dao = new RejectedReportDaoImpl();
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		RejectedReport rr = new RejectedReport();
		rr.setTimeStamps();
		rr.setOrganizationName("SomeOrg");
		dao.save(rr);
	}
	
	
//	@Test 
	public void writeOneSentence() throws Exception{
		processSentence("testing a  new sentence that has a timestamp.");
	}
	
	private void processSentence(String text) throws Exception{
		Sentence sentence = getSentence(text);
		write(sentence);	
	}
	
	private void write(Sentence sentence){
		SentenceDb dbObj = SentenceConverter.convertToDocument(sentence);
		List<SentenceDb> sentences = new ArrayList<>();
		sentences.add(dbObj);
		
		DiscreteData dd = new DiscreteData();
		dd.setAccessionNumber("111");
		dd.setExamDescription("desc");
		dd.setModality("m");	
		SentenceDaoImpl dao =  new SentenceDaoImpl();
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		dao.saveSentences(sentences, dd);
	}
	//@Test
	public void loadMetaData(){
		SentenceProcessingMetaDataInput input =new SentenceProcessingHardcodedMetaDataInputFactory().create();
		Datastore ds = new MongoDatastoreProviderDefault().getDataStore();
		ds.delete(ds.createQuery(SentenceProcessingMetaDataInput.class));
		ds.save(input);
	}
	
	@Test
	public void loadDiscreteDataComplianceFields(){
		DiscreteDataComplianceFieldProvider provider = new DiscreteDataComplianceFieldProvider();
		DisceteDataComplianceDisplayFields fields =  provider.get("rad","rad");
		DisceteDataComplianceDisplayFieldsDaoImpl dao = new DisceteDataComplianceDisplayFieldsDaoImpl();
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		dao.save(fields);
	}
	
	private Sentence getSentence(String text) throws Exception{
		SentenceProcessingControllerImpl controller = new  SentenceProcessingControllerImpl();
		controller.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
		List<String> input = new ArrayList<>();
		input.add(text);
		
		SentenceRequest request = new SentenceRequest();
		request.setSenteceTexts(input);
		
		List<Sentence> sentences = controller.processSentences(request);
		Sentence result = sentences.get(0);
		return result;
	}

}
