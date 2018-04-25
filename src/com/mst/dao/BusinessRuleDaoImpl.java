package com.mst.dao;

import com.mst.interfaces.dao.BusinessRuleDao;
import com.mst.model.businessRule.BusinessRule;
import org.mongodb.morphia.query.Query;

public class BusinessRuleDaoImpl extends BaseDocumentDaoImpl<BusinessRule> implements BusinessRuleDao {

    public BusinessRuleDaoImpl(Class<BusinessRule> entityClass) {
        super(entityClass);
    }

    @Override
    public BusinessRule get(String orgId, String ruleType) {
        Query<BusinessRule> query = this.getDatastore().createQuery(BusinessRule.class);
        query.field("organizationId").equal(orgId);
        query.field("ruleType").equal(ruleType);
        return query.get();
    }

    public void delete(String orgId, String ruleType) {
        Query<BusinessRule> query = this.getDatastore().createQuery(BusinessRule.class);
        query.field("organizationId").equal(orgId);
        query.field("ruleType").equal(ruleType);
        BusinessRule rule = query.get();
        if (rule != null) {
            String id = rule.getId().toString();
            delete(id);
        }
    }
}
