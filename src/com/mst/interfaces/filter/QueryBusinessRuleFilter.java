package com.mst.interfaces.filter;

import java.util.List;

import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.businessRule.QueryBusinessRule;

public interface QueryBusinessRuleFilter {

	List<SentenceQueryResult> filterByBusinessRule(List<SentenceQueryResult> nonFilteredResults, QueryBusinessRule rule);

}
