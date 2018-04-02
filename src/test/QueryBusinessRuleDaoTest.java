package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


//import org.junit.Test;

import com.mst.dao.QueryBusinessRuleDaoImpl;
import com.mst.metadataProviders.TestDataProvider;
import com.mst.model.businessRule.QueryBusinessRule;
import com.mst.model.metadataTypes.QueryBusinessRuleTypes;
import com.mst.util.MongoDatastoreProviderDefault;

public class QueryBusinessRuleDaoTest {

//	@Test
	public void insert(){	
		
		MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault();
		QueryBusinessRuleDaoImpl dao = new QueryBusinessRuleDaoImpl();
		dao.setMongoDatastoreProvider(provider);

		QueryBusinessRule rule = new QueryBusinessRule();

		List<String> tokensToExlcude =getTokenSequencesToExclude();
		rule.setTokenSequenceToExlude(tokensToExlcude);
		rule.setRuleType(QueryBusinessRuleTypes.tokensequenceexlcude);
		rule.setOrganizationId("5972aedebde4270bc53b23e3");
		dao.save(rule);	
	}

	//@Test
	public void get(){
		MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault();
		QueryBusinessRuleDaoImpl dao = new QueryBusinessRuleDaoImpl();
		dao.setMongoDatastoreProvider(provider);

		QueryBusinessRule rule = dao.get("123", QueryBusinessRuleTypes.tokensequenceexlcude);
		QueryBusinessRule newrule = rule;
	}
	
	private String createFullPath(){
		return System.getProperty("user.dir") + File.separator + "testData" +  
				File.separator + "tokensequencebusinessrule.txt" ;
	}
	
	private List<String> getTokenSequencesToExclude(){
		return TestDataProvider.readLines(createFullPath());
	}
	
	
	
	
	
	
}
