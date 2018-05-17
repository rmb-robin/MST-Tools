package com.mst.interfaces.filter;

import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.businessRule.BusinessRule;

import java.util.List;

public interface BusinessRuleFilter {
    void modifySentenceQueryInput(SentenceQueryInput input, List<BusinessRule> rules);
    void modifySentenceQueryResult(List<SentenceQueryResult> sentenceQueryResults, List<BusinessRule> rule);
}
