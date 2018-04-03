package com.mst.filter;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.filter.QueryBusinessRuleFilter;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.businessRule.QueryBusinessRule;

public class TokenSequenceQueryBusinessRuleFilterImpl implements QueryBusinessRuleFilter {

	@Override
	public List<SentenceQueryResult> filterByBusinessRule(List<SentenceQueryResult> nonFilteredResults,
			QueryBusinessRule rule) {
		
		if(nonFilteredResults==null)return nonFilteredResults;
		if(rule==null)return nonFilteredResults;
		if(rule.getTokenSequenceToExlude()==null)return nonFilteredResults;
		return filter(nonFilteredResults,rule.getTokenSequenceToExlude());
	}
	
	private List<SentenceQueryResult> filter(List<SentenceQueryResult> resultsToFilter, List<String> tokenSequence){
		List<SentenceQueryResult> results = new ArrayList<>();
		
		for(SentenceQueryResult  result: resultsToFilter){
			boolean shouldExclude = false;
			for(String sequenceTokExclude: tokenSequence){
				if(result.getSentence().contains(sequenceTokExclude)){
					shouldExclude = true; 
					break;
				}
			}
			
			if(shouldExclude) continue;
			results.add(result);	
		}
		return results;
	}

	
	
}
