package com.mst.dao;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.query.Query;

import com.mst.model.test.SentenceCompareSummary;
import com.mst.model.test.SentenceEdgeCompare;
import com.mst.model.test.SingleSentenceCompare;

public class SentenceCompareSummaryDao extends BaseDocumentDaoImpl<SentenceCompareSummary> {

	public SentenceCompareSummaryDao() {
		super(SentenceCompareSummary.class);
	}

	public List<String> query(){
		
		Query<SentenceCompareSummary> q = this.datastoreProvider.getDefaultDb().createQuery(SentenceCompareSummary.class);
		SentenceCompareSummary summary = q.get();
		
		List<String> result = new ArrayList<>();
		List<SingleSentenceCompare> mismatched =  summary.getMismatchedSentences();
		
		for(SingleSentenceCompare compare: mismatched){
			String sentenceA = compare.getSentenceA();
			for(SentenceEdgeCompare edgeCompare:compare.getMisMatchedEdges()) {
				String edgeName = edgeCompare.getEdgeName();
				
				for(String toFrom: edgeCompare.getSentenceAToFrom()){
					String val = sentenceA + "," + edgeName + "," + toFrom;
					result.add(val);
				}
			} 
		}
		return result;
	}
	
	
	
}
