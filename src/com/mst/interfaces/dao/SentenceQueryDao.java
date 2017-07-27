package com.mst.interfaces.dao;

import java.util.List;

import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.SentenceReprocessingInput;
import com.mst.model.sentenceProcessing.SentenceDb;

public interface SentenceQueryDao extends IDao {

	List<SentenceQueryResult> getSentences(SentenceQueryInput input);
	List<String> getEdgeNamesByTokens(List<String> tokens);
	List<SentenceDb> getSentencesByToken(SentenceReprocessingInput input);
}
