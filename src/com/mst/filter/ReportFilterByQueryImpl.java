package com.mst.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.sentenceProcessing.SentenceDb;

public class ReportFilterByQueryImpl implements ReportQueryFilter {

	private List<SentenceQueryResult> processedMatches = new ArrayList<SentenceQueryResult>();
	private Map<String, SentenceDb> sentenceCache = null;
	private SentenceQueryInput originalQuery = null;
	private boolean debug = false;
	private SentenceQueryInput filter;
	private Boolean hasNumericEdges = false;
	
	
	public ReportFilterByQueryImpl(SentenceQueryInput query, Map<String, SentenceDb> sentenceCache) {
		this.originalQuery = query;
		this.sentenceCache = sentenceCache;
	}

	
	protected List<SentenceDb> processingSentencs; 
	
	@Override
	public List<SentenceQueryResult> build(SentenceQueryDaoImpl daoImpl, List<SentenceQueryResult> results,
			List<SentenceDb> sentences, SentenceQueryInput filter) {
		this.filter = filter;
		this.processingSentencs = sentences;
		SentenceQueryInput myFilter = getResultsFilter(filter);
		
		List<SentenceQueryResult> myResults = daoImpl.getSentences(myFilter, sentences);
			
		if(myResults.isEmpty()) return myResults;
		
				//controller.getSentenceQueryResults(sentences, "cyst", edgeQuery, "cyst")
				//controller.getSentenceQueryResults(myFilter, sentences);

		processedMatches = sortBestMatches(filter, myResults, sentenceCache);
		return processedMatches; 
	}

	private double doubleValue(String value) {
		try {
			return Double.parseDouble(value);
		} catch (Exception e) {
			return 0;
		}
	}

	protected double getHighestEdgeValue(SentenceQueryResult queryResult, SentenceDb sentenceDb) {

		if (queryResult.getSentenceQueryEdgeResults().size() == 0)
			return 0;

		if (queryResult.getSentenceQueryEdgeResults().size() == 1)
			try {
				return Double.parseDouble(queryResult.getSentenceQueryEdgeResults().get(0).getMatchedValue());
			} catch (Exception e) {
				return 0;
			}

		List<SentenceQueryEdgeResult> sortedEdges = queryResult.getSentenceQueryEdgeResults().stream()
				.sorted((e1, e2) -> {
					return Double.compare(doubleValue(e2.getMatchedValue()), doubleValue(e1.getMatchedValue()));
				}).collect(Collectors.toList());

		return doubleValue(sortedEdges.get(0).getMatchedValue());
	}

	public SentenceQueryInput getOriginalQuery() {
		return originalQuery;
	}

	public List<SentenceQueryResult> getProcessedMatches() {
		return processedMatches;
	}

	protected SentenceQueryInput getResultsFilter(SentenceQueryInput original) {
		SentenceQueryInput filter = new SentenceQueryInput();
		filter.setOrganizationId(original.getOrganizationId());
		
		SentenceQueryInstance instance = new SentenceQueryInstance();
		instance.setTokens(original.getSentenceQueryInstances().get(0).getTokens());
		
		EdgeQuery query = new EdgeQuery();
		query.setName("measurement");
		query.setValues(new HashSet<>(Arrays.asList("0", "1000000")));
		
		instance.getEdges().add(query);
		filter.getSentenceQueryInstances().add(instance);
		return filter;
	}

	public Map<String, SentenceDb> getSentenceCache() {
		return sentenceCache;
	}

	private void hasNumericEdges(Boolean isNumeric) {
		if (!hasNumericEdges && isNumeric != null) {
			hasNumericEdges = isNumeric;
		}
	}

	public boolean isDebug() {
		return debug;
	}

	private boolean isNumericEdge(EdgeQuery edge) {
		boolean isNumeric = false;
		for (String value : edge.getValues()) {
			if (value == null)
				return false;
			isNumeric = isNumeric || value.matches("[-+]?\\d*\\.?\\d+");
		}
		return isNumeric;
	}

	@Override
	public int process(List<SentenceQueryResult> results) throws ReportFilterException {

		// if there are no results found then just leave alone
		if (processedMatches.size() == 0) {
			return 0;
		}
		
		SentenceQueryResult bestMatch = processedMatches.get(0);
		int resultsTrimmed = 0;
		if (debug)
			System.out.println("    filter will save: " + processedMatches.get(0).getSentence());
		for (SentenceQueryResult result : processedMatches) {
			if (!bestMatch.equals(result)) {
				if (debug) {
					System.out.println("      Removing sentenceId: " + result.getSentenceId() + " : " + result.getSentence()
							+ " due to report filter on discreteData : " + result.getDiscreteData().getId().toString());
				}
				results.remove(result);
				resultsTrimmed++;
			}
		}
		return resultsTrimmed;
	}

	@Override
	public boolean qualifingFilter() {
		if(this.getProcessedMatches().isEmpty()) return false; 
		if(this.getProcessedMatches().size()==1) return true;
		
		hasNumericEdges = false;
		if (filter != null)
			filter.getSentenceQueryInstances().stream()
					.forEach(i -> i.getEdges().stream().forEach(e -> hasNumericEdges(isNumericEdge(e))));

		if (hasNumericEdges && processedMatches.size() > 0) {
			return true;
		}

		return false;
	}

	@Override
	public void setDebug(boolean debug) {
		this.debug = debug;

	}

	List<SentenceQueryResult> sortBestMatches(SentenceQueryInput filter, List<SentenceQueryResult> results,
			Map<String, SentenceDb> sentences) {

		List<SentenceQueryResult> bestResult = results.stream().sorted((e1, e2) -> {
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
				System.out.println("NumericEdge ordered results : " 
						+ getHighestEdgeValue(result, sentences.get(result.getSentenceId())) + " "
						+ sentences.get(result.getSentenceId()).getLineId() + " " + result.getSentence());
			}
		}
		return bestResult;
	}

}
