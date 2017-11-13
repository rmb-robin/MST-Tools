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
import com.mst.model.recommandation.RecommandedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.util.RecommandedTokenRelationshipUtil;

public class SentenceQueryResultFactory {

	//private boolean foundEdgeName = false;

	private List<RecommandedTokenRelationship> verbMinusOneTokenTokens;
	private List<RecommandedTokenRelationship> verbPlusOneTokenTokens;
	private RecommandedTokenRelationship verbMinusOne = null;
	private RecommandedTokenRelationship verbPlusOne = null;
	private RecommandedTokenRelationship verbverb = null;
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
	

	
	private void init(){
		edgeFound = false; 
		verbverbCheck = false;
		verbMinusOneTokenTokens = new ArrayList<>();
		verbPlusOneTokenTokens = new ArrayList<>();
		verbMinusOneIndex =0; verbPlusOneIndex = 0;
		verbMinusOne = null;
		verbPlusOne = null;
		verbverb = null;
		edgeResult = new SentenceQueryEdgeResult();
		edgeResult.setEdgeResultType(EdgeResultTypes.primaryEdge);
	}
	
	
	private boolean verbverbCheck(RecommandedTokenRelationship recommandedTokenRelationship){
		if(verbverb==null) return false;
		if(verbverb.getTokenRelationship().getFromToken().getToken().equals(recommandedTokenRelationship.getTokenRelationship().getToToken().getToken()))return true;
		return false;	
	}
	
    private void processVerbRelationship(RecommandedTokenRelationship recommandedTokenRelationship,int i,String edgeName){
    	String token = null;
    	
    	if(edgeName.equals(WordEmbeddingTypes.secondVerb)) {
    		verbMinusOneIndex = i;
    		verbMinusOne = recommandedTokenRelationship;
    		 token = recommandedTokenRelationship.getTokenRelationship().getToToken().getToken();
    		 verbverbCheck = verbverbCheck(recommandedTokenRelationship);
    	
    	
			if(!edgeFound){
				if(tokensHash.contains(token)) tokensHash.remove(token); 
				if(!verbverbCheck){
					edgeResult.setEdgeName(token);
					edgeResult.setTokenType("from");
					edgeFound = true;
				}
			}
		
    	}
		if(edgeName.equals(WordEmbeddingTypes.firstVerb) || edgeName.equals(WordEmbeddingTypes.verbPrep)){
    		verbPlusOneIndex = i;
    		verbPlusOne = recommandedTokenRelationship;

		if(!edgeFound){
			
			if(verbverbCheck){
				token = recommandedTokenRelationship.getTokenRelationship().getFromToken().getToken();
				edgeResult.setTokenType("from");
			}
			else 
			{				
				token = recommandedTokenRelationship.getTokenRelationship().getToToken().getToken();
				edgeResult.setTokenType("to");
			}
			if(tokensHash.contains(token)) tokensHash.remove(token);
			edgeResult.setEdgeName(token);	
			edgeFound = true;
		}	
	  }
    }
    
    private boolean processDefaultRelationship(RecommandedTokenRelationship recommandedTokenRelationship,boolean isLeft){	
    	String edgeName = recommandedTokenRelationship.getTokenRelationship().getEdgeName();
    	if(!edgeName.equals(WordEmbeddingTypes.defaultEdge)) return false; 

		if(isLeft)
			verbMinusOneTokenTokens.add(0,recommandedTokenRelationship);
		else 
			verbPlusOneTokenTokens.add(recommandedTokenRelationship);
		return true;
    }
		
    
    private void findDefaultEdge(List<RecommandedTokenRelationship> wordEmbeddings){
    	
    		boolean found = false;
    		int count = 0;
			
    		if(verbPlusOneIndex+1 < wordEmbeddings.size()){
    			for(int i = verbPlusOneIndex+1;i<wordEmbeddings.size();i++){
        			RecommandedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
        			if(count>=2) break;
        			if(!processDefaultRelationship(recommandedTokenRelationship, false)&& found) break;
        			else 
        				{
        					found = true;
        					count+=1;
        				}
        			
        		}
    		}
    		found = false;
    		count = 0;
			
    		if(verbMinusOneIndex-1 < 0) return; 
    		for(int i =verbMinusOneIndex-1;i>=0; i--){
    			RecommandedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
    			if(count>=2) break;
    			if(!processDefaultRelationship(recommandedTokenRelationship, true) && found) break;
    			else {
    				found = true;
    				count +=1;
    			}
    	}
    }
    
    private List<RecommandedTokenRelationship> filteredWordEmbeddings(List<RecommandedTokenRelationship> input){
    	HashSet<String> map = new HashSet<>();
    	List<RecommandedTokenRelationship> result = new ArrayList<>();
    	for(RecommandedTokenRelationship recommandedTokenRelationship: input){
    		if(map.contains(recommandedTokenRelationship.getKey())) continue;
    		result.add(recommandedTokenRelationship);
    		map.add(recommandedTokenRelationship.getKey());
    	}
    	return result;
    }
    
  
     
	public SentenceQueryEdgeResult createSentenceQueryResultForDiscovery(String text, SentenceDiscovery sentenceDiscovery){
		init();
		String[] tokens = text.split(" ");
		tokensHash = new ArrayList<>(Arrays.asList(tokens));
		List<RecommandedTokenRelationship> edges = filteredWordEmbeddings(sentenceDiscovery.getWordEmbeddings());
		verbverb = RecommandedTokenRelationshipUtil.getByEdgeName(edges, WordEmbeddingTypes.bothVerbs); 
		
		for(int i =0;i<edges.size();i++) {
			RecommandedTokenRelationship recommandedTokenRelationship = edges.get(i);
			String edgeName = recommandedTokenRelationship.getTokenRelationship().getEdgeName();
			if(edgeName.equals(WordEmbeddingTypes.firstVerb) || edgeName.equals(WordEmbeddingTypes.secondVerb) || 
				edgeName.equals(WordEmbeddingTypes.verbPrep))
				processVerbRelationship(recommandedTokenRelationship,i,edgeName);
			
			if(verbMinusOne!=null && verbPlusOne!=null) break;
		}
		

		findDefaultEdge(edges);
		buildSentenceQueryEdgeResult();
		return edgeResult;
	}
	
	private String getTokenValue(List<RecommandedTokenRelationship> relationships){
		String result = "";
		
		for(RecommandedTokenRelationship recommandedTokenRelationship: relationships){
			TokenRelationship relation = recommandedTokenRelationship.getTokenRelationship();
			if(!result.contains(relation.getFromToken().getToken()))
				result +=  relation.getFromToken().getToken() + " ";
				
			if(!result.contains(relation.getToToken().getToken())) {
				String append = relation.getToToken().getToken() + " ";
				result +=  append;
			}
		}
		return result.trim();
	}
	
	private void buildSentenceQueryEdgeResult(){
		String from = getTokenValue(verbMinusOneTokenTokens);
		String to = getTokenValue(verbPlusOneTokenTokens);
			
		edgeResult.setToToken(to);
		edgeResult.setFromToken(from);
	
		String matched = "";
		for(String token: tokensHash){
			String append = token + " ";
			matched += append;
		}
		edgeResult.setMatchedValue(matched.trim());
	}
}
