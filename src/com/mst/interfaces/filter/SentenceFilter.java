package com.mst.interfaces.filter;

import java.util.List;

 
import com.mst.model.SentenceQuery.EdgeMatchOnQueryResult;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.ShouldMatchOnSentenceEdgesResult;
import com.mst.model.businessRule.BusinessRule;
import com.mst.model.sentenceProcessing.TokenRelationship;

public interface SentenceFilter {
	EdgeMatchOnQueryResult matchEdgesOnQuery(List<TokenRelationship> existingtokenRelationships, List<EdgeQuery> edgeQueries, String searchToken, String measurementClassification, List<BusinessRule> businessRules);
	ShouldMatchOnSentenceEdgesResult shouldAddTokenFromRelationship(TokenRelationship relation, String token);
}
