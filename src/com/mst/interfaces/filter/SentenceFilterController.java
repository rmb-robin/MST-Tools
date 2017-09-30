package com.mst.interfaces.filter;

import java.util.List;
import java.util.Map;

import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryInstanceResult;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.sentenceProcessing.SentenceDb;

public interface SentenceFilterController {
	List<SentenceQueryResult> getSentenceQueryResults(List<SentenceDb> sentences, String token, List<EdgeQuery> edgeQuery, String searchToken);
	void filterForAndNot(SentenceQueryInstance sentenceQueryInstance);
	void filterForAnd(SentenceQueryInstance sentenceQueryInstance);
	Map<String,SentenceQueryResult>  getQueryResults();
	void addSentencesToResult(SentenceQueryInstanceResult result);
	Map<String, EdgeQuery> convertEdgeQueryToDictionary(SentenceQueryInstance input);
}
