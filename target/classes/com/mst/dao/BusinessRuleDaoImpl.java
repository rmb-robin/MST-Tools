package com.mst.dao;

import com.mst.interfaces.dao.BusinessRuleDao;
import com.mst.model.businessRule.BusinessRule;
import com.mst.model.businessRule.BusinessRule.RuleType;
import org.mongodb.morphia.query.Query;

import java.util.List;

public class BusinessRuleDaoImpl extends BaseDocumentDaoImpl<BusinessRule> implements BusinessRuleDao {

    public BusinessRuleDaoImpl(Class<BusinessRule> entityClass) {
        super(entityClass);
    }

    @Override
    public List<BusinessRule> get(String orgId, RuleType ruleType) {
        Query<BusinessRule> query = this.getDatastore().createQuery(BusinessRule.class);
        query.field("organizationId").equal(orgId);
        query.field("ruleType").equal(ruleType);
        return query.asList();
    }

    public void delete(String orgId, String className) {
        Query<BusinessRule> query = this.getDatastore().createQuery(BusinessRule.class);
        query.field("organizationId").equal(orgId);
        query.field("className").equal(className);
        BusinessRule rule = query.get();
        if (rule != null) {
            String id = rule.getId().toString();
            delete(id);
        }
    }
}
