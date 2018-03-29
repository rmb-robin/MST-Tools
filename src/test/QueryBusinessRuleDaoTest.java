package test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mst.dao.QueryBusinessRuleDaoImpl;
import com.mst.model.businessRule.QueryBusinessRule;
import com.mst.model.metadataTypes.QueryBusinessRuleTypes;
import com.mst.util.MongoDatastoreProviderDefault;

public class QueryBusinessRuleDaoTest {

	@Test
	public void insert(){	
		
		MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault();
		QueryBusinessRuleDaoImpl dao = new QueryBusinessRuleDaoImpl();
		dao.setMongoDatastoreProvider(provider);

		QueryBusinessRule rule = new QueryBusinessRule();
		List<String> tokensToExlude = new ArrayList<>();
		tokensToExlude.add("she has a big cyst");
		
		rule.setTokenSequenceToExlude(tokensToExlude);
		rule.setRuleType(QueryBusinessRuleTypes.tokensequenceexlcude);
		rule.setOrganizationId("123");
		dao.save(rule);	
	}

	@Test
	public void get(){
		MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault();
		QueryBusinessRuleDaoImpl dao = new QueryBusinessRuleDaoImpl();
		dao.setMongoDatastoreProvider(provider);

		QueryBusinessRule rule = dao.get("123", QueryBusinessRuleTypes.tokensequenceexlcude);
		QueryBusinessRule newrule = rule;
	}
	
	
	
	
	
	
}
