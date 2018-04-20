package com.mst.interfaces.filter;

import java.util.List;

import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.businessRule.QueryBusinessRule;
import com.mst.model.metadataTypes.CreateSynonymQueryBusinessRuleType;

public interface QueryBusinessRuleFilter {

	List<SentenceQueryResult> filterByBusinessRule(List<SentenceQueryResult> nonFilteredResults, QueryBusinessRule rule);
    CreateSynonymQueryBusinessRuleType filterByBusinessRule(SentenceQueryInput input, QueryBusinessRule rule);
    List<SentenceQueryResult> modifyByBusinessRule(List<SentenceQueryResult> sentenceQueryResults, List<QueryBusinessRule.Rule> rulesApplied);
}
