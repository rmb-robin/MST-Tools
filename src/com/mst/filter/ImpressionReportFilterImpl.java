package com.mst.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.WordToken;

public class ImpressionReportFilterImpl extends ReportFilterByQueryImpl {
	private static List<String> impressionList = Arrays.asList("impression", "conclusion");

	public ImpressionReportFilterImpl(SentenceQueryInput query, Map<String, SentenceDb> sentenceCache) {
		super(query, sentenceCache);
	}

	private boolean containsWords(SentenceDb sentence, List<String> tokens) {

		if (sentence != null) {
			for (String token : tokens) {
					if(sentence.getOrigSentence().toLowerCase().contains(token.toLowerCase()))
						return true;
					}
		}
		return false;
	}
	
	@Override
	protected SentenceQueryInput getResultsFilter(SentenceQueryInput original) {
		SentenceQueryInput filter = super.getResultsFilter(original);
		return filter;
	}
	
	@Override
	public boolean qualifingFilter() {
		if ( ! sentencesContainsImpression() ) {
			return false; 
		}
		return super.qualifingFilter();
	}
	
	private boolean sentencesContainsImpression() {
		for ( SentenceDb sentence : getSentenceCache().values() ) {
			if ( this.containsWords(sentence, this.impressionList)) {
				return true;
			}
		}
		return false;
	}

	List<SentenceQueryResult> sortBestMatches(SentenceQueryInput filter, List<SentenceQueryResult> results,
			Map<String, SentenceDb> sentences) {

		List<SentenceQueryResult> bestResult = results.stream().sorted((e1, e2) -> {

			try {
				boolean o1contains = containsWords(sentences.get(e1.getSentenceId()), impressionList);
				boolean o2contains = containsWords(sentences.get(e2.getSentenceId()), impressionList);
				int compare = Boolean.compare(o2contains, o1contains);
				if (compare != 0)
					return compare;
			} catch (NullPointerException npe) {
				return 0;
			}
			try {
				int compare = Double.compare(getHighestEdgeValue(e2, sentences.get(e2.getSentenceId())),
						getHighestEdgeValue(e1, sentences.get(e1.getSentenceId())));
				if (compare != 0)
					return compare;
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			}
			return Long.compare(sentences.get(e2.getSentenceId()).getLineId(),
					sentences.get(e1.getSentenceId()).getLineId());
		}).collect(Collectors.toList());
		
		if (this.isDebug() ) {
			System.out.println("");
			for (SentenceQueryResult result : bestResult) {
				System.out.println("Impression ordered results : " + containsWords(sentences.get(result.getSentenceId()), impressionList)
						+ " : " 
						+ getHighestEdgeValue(result, sentences.get(result.getSentenceId())) + " "
						+ sentences.get(result.getSentenceId()).getLineId() + " " + result.getSentence());
			}
		}
		return bestResult;
	}
}
