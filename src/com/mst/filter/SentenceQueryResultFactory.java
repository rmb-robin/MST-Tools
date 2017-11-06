package com.mst.filter;

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

	private boolean foundEdgeName = false;
	private int verbIndex =0;
	private int defaultIndex = 0;
	private RecommandedTokenRelationship  verb = null;
	private RecommandedTokenRelationship defaultRelationship = null;
	private SentenceQueryEdgeResult edgeResult; 
	private HashSet<String> tokensHash;
	
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
		foundEdgeName = false; 
		verbIndex = 0;
		defaultIndex = 0; 
		verb = null;
		defaultRelationship = null;
		edgeResult = new SentenceQueryEdgeResult();
		edgeResult.setEdgeResultType(EdgeResultTypes.primaryEdge);
	}
	
	


    private void processVerbRelationship(RecommandedTokenRelationship recommandedTokenRelationship,int i,String edgeName){
    	if(verb == null){
			verbIndex = i;
			verb  = recommandedTokenRelationship;
		}
    	
    	String token = null;
    	if(edgeName.equals(WordEmbeddingTypes.secondVerb))
    		 token = recommandedTokenRelationship.getTokenRelationship().getToToken().getToken();
    	else 
    		token = recommandedTokenRelationship.getTokenRelationship().getFromToken().getToken();
    
		if(tokensHash.contains(token)&& !foundEdgeName){
			tokensHash.remove(token);
			edgeResult.setEdgeName(token);
			edgeResult.setTokenType("from");
			foundEdgeName = true; 
		}
			
		if(!foundEdgeName && tokensHash.contains(token)){
			token = recommandedTokenRelationship.getTokenRelationship().getToToken().getToken();
			tokensHash.remove(token);
			edgeResult.setEdgeName(token);	
			edgeResult.setTokenType("to");
			foundEdgeName = true;
		}		
    }
    
    private boolean processDefaultRelationship(RecommandedTokenRelationship recommandedTokenRelationship, int i,String edgeName){	
    	if(!edgeName.equals(WordEmbeddingTypes.defaultEdge)) return false; 

		defaultIndex = i;
		defaultRelationship = recommandedTokenRelationship;
		return true;
    }
		
    
    private void findDefaultEdge(int verbIndex, List<RecommandedTokenRelationship> wordEmbeddings, boolean goRight){
    	
    	if(goRight){
    		if(verbIndex+1 >= wordEmbeddings.size()) return; 
    		
    		for(int i = verbIndex+1;i<wordEmbeddings.size();i++){
    			RecommandedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
    			if(processDefaultRelationship(recommandedTokenRelationship, i, recommandedTokenRelationship.getTokenRelationship().getEdgeName()))
    				return;
    		}
    	}
    	else {
    		if(verbIndex-1 < 0) return; 
    		for(int i =verbIndex-1;i>=0; i--){
    			RecommandedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
    			if(processDefaultRelationship(recommandedTokenRelationship, i, recommandedTokenRelationship.getTokenRelationship().getEdgeName()))
    				return;
    		}
    	}
    }
    
	public SentenceQueryEdgeResult createSentenceQueryResultForDiscovery(String text, SentenceDiscovery sentenceDiscovery){
		init();
		String[] tokens = text.split(" ");
		tokensHash = new HashSet<>(Arrays.asList(tokens));

		for(int i =0;i<sentenceDiscovery.getWordEmbeddings().size();i++) {
			RecommandedTokenRelationship recommandedTokenRelationship = sentenceDiscovery.getWordEmbeddings().get(i);
			String edgeName = recommandedTokenRelationship.getTokenRelationship().getEdgeName();
			if(edgeName.equals(WordEmbeddingTypes.firstVerb) || edgeName.equals(WordEmbeddingTypes.secondVerb))
				processVerbRelationship(recommandedTokenRelationship,i,edgeName);
				if(verb!=null) break;
		}
		
		boolean goRight = false;
		if(verb.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.firstVerb))
			goRight = true;

		findDefaultEdge(verbIndex,sentenceDiscovery.getWordEmbeddings(),goRight);
		if(defaultRelationship!=null) 
			buildSentenceQueryEdgeResult();
		return edgeResult;
	}
	
	private void buildSentenceQueryEdgeResult(){
		String verbToken = verb.getTokenRelationship().getFromTokenToTokenStringWithSpace();
		String defaultToken = defaultRelationship.getTokenRelationship().getFromTokenToTokenStringWithSpace();
		
		
		if(verbIndex > defaultIndex){
			edgeResult.setToToken(verbToken);
			edgeResult.setFromToken(defaultToken);
		}
		else {
			edgeResult.setToToken(defaultToken);
			edgeResult.setFromToken(verbToken);
		}
		String matched = "";
		for(String token: tokensHash){
			String append = token + " ";
			matched += append;
		}
		edgeResult.setMatchedValue(matched);
	}
}
