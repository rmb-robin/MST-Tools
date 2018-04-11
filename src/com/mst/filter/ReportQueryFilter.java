package com.mst.filter;

import java.util.List;

import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.sentenceProcessing.SentenceDb;

public interface ReportQueryFilter {

	List<SentenceQueryResult> build(SentenceQueryDaoImpl daoImp, List<SentenceQueryResult> results, List<SentenceDb> sentences, SentenceQueryInput filter);

	int process(List<SentenceQueryResult> results) throws ReportFilterException;

	boolean qualifingFilter();

	void setDebug(boolean debug);

}
