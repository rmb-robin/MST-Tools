package com.mst.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mst.model.SentenceQuery.MatchInfo;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.metadataTypes.EdgeResultTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommandedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;

public class SentenceQueryResultFactory {

	//private boolean foundEdgeName = false;

	private RecommandedTokenRelationship verbMinusOneTokenToken = null;
	private RecommandedTokenRelationship verbPlusOneTokenToken = null;
	private RecommandedTokenRelationship verbMinusOne = null;
	private RecommandedTokenRelationship verbPlusOne = null;
	
	private int verbMinusOneIndex, verbPlusOneIndex = 0;
	private SentenceQueryEdgeResult edgeResult; 
	private List<String> tokensHash;
	private boolean edgeFound; 
	
	
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
	

	
	private void init(){
		edgeFound = false; 
	    verbMinusOneTokenToken = null;
		verbPlusOneTokenToken = null;
		verbMinusOneIndex =0; verbPlusOneIndex = 0;
		verbMinusOne = null;
		verbPlusOne = null;
		verbMinusOneTokenToken = null;
		edgeResult = new SentenceQueryEdgeResult();
		edgeResult.setEdgeResultType(EdgeResultTypes.primaryEdge);
	}
	
	


    private void processVerbRelationship(RecommandedTokenRelationship recommandedTokenRelationship,int i,String edgeName){
    	String token = null;
    	
    	if(edgeName.equals(WordEmbeddingTypes.secondVerb)){
    		verbMinusOneIndex = i;
    		verbMinusOne = recommandedTokenRelationship;
    		 token = recommandedTokenRelationship.getTokenRelationship().getToToken().getToken();
    	}
    	
		if(!edgeFound){
			if(tokensHash.contains(token)) tokensHash.remove(token);
			edgeResult.setEdgeName(token);
			edgeResult.setTokenType("from");
			edgeFound = true;
		}
		
		if(edgeName.equals(WordEmbeddingTypes.firstVerb)){
    		verbPlusOneIndex = i;
    		verbPlusOne = recommandedTokenRelationship;
    		token = recommandedTokenRelationship.getTokenRelationship().getFromToken().getToken();
    	}
		
		if(!edgeFound){
			token = recommandedTokenRelationship.getTokenRelationship().getToToken().getToken();
			if(tokensHash.contains(token)) tokensHash.remove(token);
			edgeResult.setEdgeName(token);	
			edgeResult.setTokenType("to");
			edgeFound = true;
		}		
    }
    
    private boolean processDefaultRelationship(RecommandedTokenRelationship recommandedTokenRelationship,boolean isLeft){	
    	String edgeName = recommandedTokenRelationship.getTokenRelationship().getEdgeName();
    	if(!edgeName.equals(WordEmbeddingTypes.defaultEdge)) return false; 

		if(isLeft)
			verbMinusOneTokenToken = recommandedTokenRelationship;
		else 
			verbPlusOneTokenToken = recommandedTokenRelationship;
		return true;
    }
		
    
    private void findDefaultEdge(List<RecommandedTokenRelationship> wordEmbeddings){
    	
    		if(verbPlusOneIndex+1 < wordEmbeddings.size()){
    			for(int i = verbPlusOneIndex+1;i<wordEmbeddings.size();i++){
        			RecommandedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
        			if(processDefaultRelationship(recommandedTokenRelationship, false))
        				break;
        		}
    		}

    		if(verbMinusOneIndex-1 < 0) return; 
    		for(int i =verbMinusOneIndex-1;i>=0; i--){
    			RecommandedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
    			if(processDefaultRelationship(recommandedTokenRelationship, true))
    				break;
    	}
    }
    
	public SentenceQueryEdgeResult createSentenceQueryResultForDiscovery(String text, SentenceDiscovery sentenceDiscovery){
		init();
		String[] tokens = text.split(" ");
		tokensHash = new ArrayList<>(Arrays.asList(tokens));

		for(int i =0;i<sentenceDiscovery.getWordEmbeddings().size();i++) {
			RecommandedTokenRelationship recommandedTokenRelationship = sentenceDiscovery.getWordEmbeddings().get(i);
			String edgeName = recommandedTokenRelationship.getTokenRelationship().getEdgeName();
			if(edgeName.equals(WordEmbeddingTypes.firstVerb) || edgeName.equals(WordEmbeddingTypes.secondVerb))
				processVerbRelationship(recommandedTokenRelationship,i,edgeName);
			
			if(verbMinusOne!=null && verbPlusOne!=null) break;
		}
		

		findDefaultEdge(sentenceDiscovery.getWordEmbeddings());
		buildSentenceQueryEdgeResult();
		return edgeResult;
	}
	
	private void buildSentenceQueryEdgeResult(){
		String from = "";
		if(verbMinusOneTokenToken!=null)
		from = verbMinusOneTokenToken.getTokenRelationship().getFromTokenToTokenStringWithSpace();
		
		String to = "";
		if(verbPlusOneTokenToken!=null)
			to = verbPlusOneTokenToken.getTokenRelationship().getFromTokenToTokenStringWithSpace();
			
		edgeResult.setToToken(to);
		edgeResult.setFromToken(from);
	
		String matched = "";
		for(String token: tokensHash){
			String append = token + " ";
			matched += append;
		}
		edgeResult.setMatchedValue(matched);
	}
}
