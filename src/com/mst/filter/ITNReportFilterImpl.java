package com.mst.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class ITNReportFilterImpl extends ReportFilterByQueryImpl {
	private static List<String> enlargedFindingSiteList = Arrays.asList("enlarged", "enlarge", "enlargement");
	private static List<String> heterogeneousFindingSiteList = Arrays.asList("heterogeneous", "prominent",
			"multinodular", "multi-nodular");
	private static List<String> instance2TokenList = Arrays.asList("isthmus", "thyroid");
	private static List<String> instance1TokenList = Arrays.asList("cyst", "cysts", "mass", "masses", "lesion",
			"lesions", "nodule", "nodules", "hypodensity", "attenuation");
	private static List<String> diseaseLocationList = Arrays.asList("isthmus", "thyroid");
	private static List<String> impressionList = Arrays.asList("impression", "conclusion");

	public ITNReportFilterImpl(SentenceQueryInput query, Map<String, SentenceDb> sentenceCache) {
		super(query, sentenceCache);
	}

	private boolean containsWords(SentenceDb sentence, List<String> tokens) {

		if (sentence != null) {
			for (String token : tokens) {
				for (WordToken word : sentence.getModifiedWordList()) {
					if (word.getToken().equalsIgnoreCase(token)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	private boolean containsEnlarged(){
		
		for(SentenceDb sentence :this.processingSentencs){
			for(TokenRelationship relationship: sentence.getTokenRelationships()){
				if(relationship.getEdgeName().equals(EdgeNames.enlarged_finding_sites)) return true;
			}
		}
		return false;
	}

	@Override
	protected SentenceQueryInput getResultsFilter(SentenceQueryInput original) {
		SentenceQueryInput filter = new SentenceQueryInput();
		filter.setOrganizationId(original.getOrganizationId());

		SentenceQueryInstance instance = new SentenceQueryInstance();

		
		boolean isEnlarged = containsEnlarged();
	
		if(!isEnlarged){
			instance.setTokens(original.getSentenceQueryInstances().get(0).getTokens());
			EdgeQuery query;
			

			query = new EdgeQuery();
			query.setName(EdgeNames.measurement);
			query.setValues(new HashSet<>(Arrays.asList("0", "1000000")));
			instance.getEdges().add(query);
		
		
			query = new EdgeQuery();
			query.setName(EdgeNames.diseaseLocation);
			query.setValues(new HashSet<>(ITNReportFilterImpl.diseaseLocationList));
			instance.getEdges().add(query);
			filter.getSentenceQueryInstances().add(instance);
		}
		
		
		//this second instance.
		instance = new SentenceQueryInstance();
		instance.setTokens(ITNReportFilterImpl.instance2TokenList);
		
		if(!isEnlarged)
			instance.setAppender("or");
		

		EdgeQuery query = new EdgeQuery();
		query.setName(EdgeNames.hetrogeneous_finding_sites);
		query.setValues(new HashSet<>(ITNReportFilterImpl.heterogeneousFindingSiteList));
		instance.getEdges().add(query);

		query = new EdgeQuery();
		query.setName(EdgeNames.enlarged_finding_sites);
		query.setValues(new HashSet<>(ITNReportFilterImpl.enlargedFindingSiteList));
		instance.getEdges().add(query);

		if(!isEnlarged){
			query = new EdgeQuery();
			query.setName(EdgeNames.existence);
			query.setValues(new HashSet<>(Arrays.asList()));
			instance.getEdges().add(query);
		}
		
		filter.getSentenceQueryInstances().add(instance);
		return filter;
	}

	private boolean isEnlarged(List<SentenceQueryEdgeResult> sentenceQueryEdgeResults) {
		boolean enlargedFound = false;
		boolean hetrogeneousFound = false;
		boolean existenceFound = false;
		for (SentenceQueryEdgeResult result : sentenceQueryEdgeResults) {
			// TODO when using confidence logic the isMatched is a requirement in this condition
			if (result.getEdgeName().equals(EdgeNames.enlarged_finding_sites))
				enlargedFound = true;
			if (result.getEdgeName().equals(EdgeNames.hetrogeneous_finding_sites))
				hetrogeneousFound = true;
			if (result.getEdgeName().equals(EdgeNames.existence))
				existenceFound = true;
		}
		if (enlargedFound && hetrogeneousFound && existenceFound) {
			return true;
		}

		return false;
	}

	@Override
	public boolean qualifingFilter() {
		if(this.getProcessedMatches().isEmpty()) return false; 
		if(this.getProcessedMatches().size()==1) return true;
		
		
		if (getOriginalQuery() == null || getOriginalQuery().getSentenceQueryInstances().size() != 2) {
			return false;
		}

		SentenceQueryInstance instance1 = getOriginalQuery().getSentenceQueryInstances().get(0);

		if (Collections.disjoint(instance1.getTokens(), instance1TokenList)) {
			return false;
		}
		boolean locationFound = false;
		boolean measurementFound = false;
		boolean existenceFound = false;
		for (EdgeQuery query : instance1.getEdges()) {
			if (query.getName().equals(EdgeNames.diseaseLocation)) {
				if (Collections.disjoint(query.getValuesLower(), diseaseLocationList)) {
					return false;
				}
				locationFound = true;
			}
			if (query.getName().equals(EdgeNames.measurement)) {
				measurementFound = true;
			}
			if (query.getName().equals(EdgeNames.existence)) {
				existenceFound = true;
			}
		}
		if (!(locationFound && existenceFound && measurementFound) && !containsEnlarged()) {
			return false;
		}

		SentenceQueryInstance instance2 = getOriginalQuery().getSentenceQueryInstances().get(1);
		if (Collections.disjoint(instance2.getTokens(), instance2TokenList)) {
			return false;
		}
		if (instance2.getAppender() == null || !instance2.getAppender().equalsIgnoreCase("or")) {
			return false;
		}
		boolean heterogeneousFound = false;
		boolean enlargedFound = false;
		existenceFound = false;
		for (EdgeQuery query : instance2.getEdges()) {
			if (query.getName().equals(EdgeNames.hetrogeneous_finding_sites)) {
				if (Collections.disjoint(query.getValuesLower(), heterogeneousFindingSiteList)) {
					return false;
				}
				heterogeneousFound = true;
			}
			if (query.getName().equals(EdgeNames.enlarged_finding_sites)) {
				if (Collections.disjoint(query.getValuesLower(), enlargedFindingSiteList)) {
					return false;
				}
				enlargedFound = true;
			}
			if (query.getName().equals(EdgeNames.existence)) {
				existenceFound = true;
			}
		}

		if (!(enlargedFound && existenceFound && heterogeneousFound)) {
			return false;
		}

		return super.qualifingFilter();
	}

	List<SentenceQueryResult> sortBestMatches(SentenceQueryInput filter, List<SentenceQueryResult> results,
			Map<String, SentenceDb> sentences) {

		List<SentenceQueryResult> bestResult = results.stream().sorted((e1, e2) -> {

			try {
				boolean o1contains = isEnlarged(e1.getSentenceQueryEdgeResults());
				boolean o2contains = isEnlarged(e2.getSentenceQueryEdgeResults());
				int compare = Boolean.compare(o2contains, o1contains);
				if (compare != 0) {
					return compare;
				}
			} catch (NullPointerException npe) {
				return 0;
			}
			
			try {
				boolean o1contains = containsWords(sentences.get(e1.getSentenceId()), impressionList);
				boolean o2contains = containsWords(sentences.get(e2.getSentenceId()), impressionList);
				int compare = Boolean.compare(o2contains, o1contains);
				if (compare != 0) {
					return compare;
				}
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
				System.out.println("ITN ordered results : " + containsWords(sentences.get(result.getSentenceId()), impressionList)
						+ " : " + isEnlarged(result.getSentenceQueryEdgeResults()) + " : "
						+ getHighestEdgeValue(result, sentences.get(result.getSentenceId())) + " "
						+ sentences.get(result.getSentenceId()).getLineId() + " " + result.getSentence());
			}
		}
		return bestResult;
	}
}
