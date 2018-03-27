package com.mst.interfaces.filter;

import java.util.List;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.sentenceProcessing.SentenceDb;

public interface ReportFilterController {
	List<SentenceQueryResult> getSentenceQueryResults(SentenceQueryInput input, MongoDatastoreProvider mongoProvider)
			throws Exception;

	List<SentenceQueryResult> getSentenceQueryResults(SentenceQueryInput input, List<SentenceDb> sentences);
}
