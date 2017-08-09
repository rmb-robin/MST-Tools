package com.mst.testcases;

import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.converters.DateConverter;

import com.mongodb.MongoClient;
import com.mst.dao.DisceteDataComplianceDisplayFieldsDaoImpl;
import com.mst.dao.RejectedReportDaoImpl;
import com.mst.dao.SentenceDaoImpl;
import com.mst.interfaces.dao.DisceteDataComplianceDisplayFieldsDao;
import com.mst.interfaces.dao.RejectedReportDao;
import com.mst.metadataProviders.DiscreteDataComplianceFieldProvider;
import com.mst.metadataProviders.DynamicRuleProvider;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.RejectedReport;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.sentenceProcessing.DynamicEdgeCreationRule;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.util.MongoDatastoreProviderDefault;

import org.apache.log4j.Logger;

public class LoadDataToMongo {

	
	//@Test
	public void testLog(){
		 Logger logger = Logger.getLogger(LoadDataToMongo.class);
	        logger.error("Don't panic");
	}
	
	
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
		rr.setOrganizationId("orgName-Test");
		dao.save(rr);
	}
	
	//@Test
	public void getSentenceByOrgId(){
		
		SentenceDaoImpl dao =  new SentenceDaoImpl();
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		List<SentenceDb> results = dao.getSentenceByDate("58c6f3ceaf3c420b90160803");
		int t = results.size();
	}
	
	//@Test 
	public void writeOneSentence() throws Exception{
		processSentence("she was denied a ct scan.");
	}
	
	private void processSentence(String text) throws Exception{
		Sentence sentence = getSentence(text);
		sentence.setOrganizationId("58c6f3ceaf3c420b90160803");
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
		dd.setOrganizationId("orgName-Test");
		dd.setTimeStamps();
		SentenceDaoImpl dao =  new SentenceDaoImpl();
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		dao.saveSentences(sentences, null,null,false);
	}
	
	
	//@Test
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
	
	@Test
	public void run_Test(){
		List<DynamicEdgeCreationRule> input =new DynamicRuleProvider().getRules();
		Datastore ds = new MongoDatastoreProviderDefault().getDataStore();
		ds.delete(ds.createQuery(DynamicEdgeCreationRule.class));
		ds.save(input);
	}
}
