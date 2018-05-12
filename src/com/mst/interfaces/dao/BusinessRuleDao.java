package com.mst.interfaces.dao;

import com.mst.model.businessRule.BusinessRule;
import com.mst.model.businessRule.BusinessRule.RuleType;


import java.util.List;

public interface BusinessRuleDao extends IDao {
    String save(BusinessRule rule);
    List<BusinessRule> get(String orgId, RuleType ruleType);
    void delete(String orgId, String className);
}
