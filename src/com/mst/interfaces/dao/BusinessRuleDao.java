package com.mst.interfaces.dao;

import com.mst.model.businessRule.BusinessRule;

public interface BusinessRuleDao extends IDao {
    String save(BusinessRule rule);
    BusinessRule get(String orgId, String ruleType);
    void delete(String orgId, String ruleType);
}
