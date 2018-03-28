package com.mst.interfaces.dao;

import java.util.List;

import com.mst.model.discrete.DiscreteData;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingFailures;

public interface SentenceDao extends IDao {

	void saveSentences(List<SentenceDb> sentences, DiscreteData discreteData, SentenceProcessingFailures failures);
	void saveReprocess(List<SentenceDb> sentences,SentenceProcessingFailures failures);
	List<SentenceDb> getByOrgId(String orgIg);
}
