package com.mst.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.sentenceProcessing.SentenceDb;

public class CystAndAAAReportFilterImpl extends ReportFilterByQueryImpl {
	private static List<String> cystTokenList = Arrays.asList("cyst", "cysts", "lesion", "lesions", "structure", "structures");
	private static List<String> cystLocationList = Arrays.asList("adnexal", "adnexa", "adnexum", "ovarian", "ovaries", "ovary", "paraovarian");
	private static List<String> aaaTokenList = Arrays.asList("aneurysm","aneurysms","dilation","dilations","dilatation","dilatations","distention","distention","sacs","sac");
	private static List<String> aaaLocationList = Arrays.asList("abdominal","abdominals","diaphragm","infrarenal","celiac axis","ima","sma","suprarenal");

	public CystAndAAAReportFilterImpl(SentenceQueryInput query, Map<String, SentenceDb> sentenceCache) {
		super(query, sentenceCache);
	}

	@Override
	public boolean qualifingFilter() {
	    if ( isOvarianCystFilter() || isAAAFilter() ) 
	    	return true;
	    
	
		return super.qualifingFilter();
	}

	private boolean isOvarianCystFilter() {
		if (getOriginalQuery() == null || getOriginalQuery().getSentenceQueryInstances().size() > 0) {
			return false;
		}

		SentenceQueryInstance instance1 = getOriginalQuery().getSentenceQueryInstances().get(0);

		if (Collections.disjoint(instance1.getTokens(), cystTokenList)) {
			return false;
		}
		boolean locationFound = false;
		boolean existenceFound = false;
		for (EdgeQuery query : instance1.getEdges()) {
			if (query.getName().equals(EdgeNames.diseaseLocation)) {
				if (Collections.disjoint(query.getValuesLower(), cystLocationList)) {
					return false;
				}
				locationFound = true;
			}
		
			if (query.getName().equals(EdgeNames.existence)) {
				existenceFound = true;
			}
		}
		if (!(locationFound && existenceFound)) {
			return false;
		}
		return true;
	}

	private boolean isAAAFilter() {
		if (getOriginalQuery() == null || getOriginalQuery().getSentenceQueryInstances().size() > 0) {
			return false;
		}

		SentenceQueryInstance instance1 = getOriginalQuery().getSentenceQueryInstances().get(0);

		if (Collections.disjoint(instance1.getTokens(), aaaTokenList)) {
			return false;
		}
		boolean locationFound = false;
		boolean existenceFound = false;
		for (EdgeQuery query : instance1.getEdges()) {
			if (query.getName().equals(EdgeNames.diseaseLocation)) {
				if (Collections.disjoint(query.getValuesLower(), aaaLocationList)) {
					return false;
				}
				locationFound = true;
			}
		
			if (query.getName().equals(EdgeNames.existence)) {
				existenceFound = true;
			}
		}
		if (!(locationFound && existenceFound)) {
			return false;
		}
		return true;
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
				System.out.println("Numeric ordered results : " + 
						+ getHighestEdgeValue(result, sentences.get(result.getSentenceId())) + " "
						+ sentences.get(result.getSentenceId()).getLineId() + " " + result.getSentence());
			}
		}
		return bestResult;
	}
}
