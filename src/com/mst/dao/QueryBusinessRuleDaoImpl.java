package com.mst.dao;

import org.mongodb.morphia.query.Query;

import com.mst.interfaces.dao.QueryBusinessRuleDao;
import com.mst.model.businessRule.QueryBusinessRule;

public class QueryBusinessRuleDaoImpl extends BaseDocumentDaoImpl<QueryBusinessRule> implements QueryBusinessRuleDao {
	
	public QueryBusinessRuleDaoImpl(){
		super(QueryBusinessRule.class);
	}

	@Override
	public QueryBusinessRule get(String orgId, String ruleType) {
		Query<QueryBusinessRule> query = 
				this.getDatastore().createQuery(QueryBusinessRule.class);
		query.field("organizationId").equal(orgId);
		query.field("ruleType").equal(ruleType);
		return query.get();
	}
}


