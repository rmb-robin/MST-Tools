package com.mst.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mst.model.SentenceQuery.MatchInfo;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.metadataTypes.EdgeResultTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.util.RecommandedTokenRelationshipUtil;

public class SentenceQueryResultFactory {

	//private boolean foundEdgeName = false;

	private int verbMinusOneIndex, verbPlusOneIndex = 0;
	private SentenceQueryEdgeResult edgeResult; 
	private List<String> tokensHash;
	private boolean edgeFound; 
	private boolean verbverbCheck; 
	
	public static SentenceQueryResult createSentenceQueryResult(SentenceDb sentenceDb){
		SentenceQueryResult result = new SentenceQueryResult();
		result.setSentence(sentenceDb.getNormalizedSentence());
		result.setSentenceId(sentenceDb.getId().toString());
		result.setDiscreteData(sentenceDb.getDiscreteData());
		return result;
	}
	
	public static SentenceQueryEdgeResult createSentenceQueryEdgeResult(TokenRelationship relationship, String edgeType,Map<String, MatchInfo> matches){
		SentenceQueryEdgeResult queryEdgeResult = new SentenceQueryEdgeResult();
		queryEdgeResult.setEdgeName(relationship.getEdgeName());
		queryEdgeResult.setFromToken(relationship.getFromToken().getToken());
		queryEdgeResult.setToToken(relationship.getToToken().getToken());

		if(matches.containsKey(relationship.getEdgeName())){
			MatchInfo info = matches.get(relationship.getEdgeName());
			queryEdgeResult.setTokenType(info.getTokenType());
			queryEdgeResult.setMatchedValue(info.getValue());
		}
		
		queryEdgeResult.setEdgeResultType(edgeType);
		return queryEdgeResult;
	}

}
