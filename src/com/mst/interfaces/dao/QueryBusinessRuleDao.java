package com.mst.interfaces.dao;

import com.mst.model.businessRule.QueryBusinessRule;

public interface QueryBusinessRuleDao extends IDao {
	String save(QueryBusinessRule rule);
    QueryBusinessRule get(String orgId, String ruleType);	
}
