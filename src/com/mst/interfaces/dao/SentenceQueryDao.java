package com.mst.interfaces.dao;

import java.util.List;

import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.sentenceProcessing.SentenceDb;

public interface SentenceQueryDao extends IDao {

	List<SentenceDb> getSentences(SentenceQueryInput input);
}
