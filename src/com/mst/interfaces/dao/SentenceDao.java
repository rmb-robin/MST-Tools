package com.mst.interfaces.dao;

import java.util.List;

import com.mst.model.sentenceProcessing.SentenceDb;

public interface SentenceDao extends IDao {

	void saveSentences(List<SentenceDb> sentences);
	
}
