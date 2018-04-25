package com.mst.interfaces.filter;

import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.businessRule.BusinessRule;

import java.util.List;

public interface BusinessRuleFilter {
    List<SentenceQueryResult> filterByBusinessRule(List<SentenceQueryResult> nonFilteredResults, BusinessRule rule);
    SentenceQueryInput modifySentenceQueryInput(SentenceQueryInput input, BusinessRule rule);
    List<SentenceQueryResult> modifySentenceQueryResults(List<SentenceQueryResult> sentenceQueryResults, BusinessRule rule);
}
