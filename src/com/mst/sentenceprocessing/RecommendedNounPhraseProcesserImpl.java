package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mst.interfaces.sentenceprocessing.RecommendedNounPhraseProcesser;
import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.RecommandedNounPhraseResult;
import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.RelationshipMapping;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.RecommandedTokenRelationshipUtil;

public class RecommendedNounPhraseProcesserImpl  extends RelationshipProcessorBase implements RecommendedNounPhraseProcesser {

	private RelationshipInput input;
	private TokenRelationshipFactory factory; 
	
	public RecommendedNounPhraseProcesserImpl(){
		factory = new TokenRelationshipFactoryImpl();
	}

	public void setNamedEdges(List<RecommendedTokenRelationship> edges, RelationshipInput input) {
		setrelationshipMaps(input.getRelationshipMappings());
		this.input = input;
		for(RecommendedTokenRelationship recommandedTokenRelationship: edges){
			processSingleEdge(recommandedTokenRelationship);
		}
	}

	public RecommandedNounPhraseResult process(List<RecommendedTokenRelationship> embeddedwords){
		
		Map<Integer, RecommendedTokenRelationship> wordEmbeddingsByIndex = getFilteredWordEmbeddings(embeddedwords);
		int iterator = 0;
		int prevIndex=0;
		int beginNounPhraseIndex=0; 
		int endNounPhraseIndex=0; 
		Map<Integer, Integer> nounPhraseIndexes = new HashMap<Integer,Integer>();
		for(Entry<Integer,RecommendedTokenRelationship> entry : wordEmbeddingsByIndex.entrySet()){
			TokenRelationship tokenRelationship = entry.getValue().getTokenRelationship();
			 
			if(iterator==0 || entry.getKey()-prevIndex>1){
				tokenRelationship.getFromToken().setPropertyValueType(PropertyValueTypes.NounPhraseBegin);
				beginNounPhraseIndex = entry.getKey();
			}
			if(!wordEmbeddingsByIndex.containsKey(entry.getKey()+1)) {
				tokenRelationship.getToToken().setPropertyValueType(PropertyValueTypes.NounPhraseEnd);
				endNounPhraseIndex = entry.getKey();
				if(!nounPhraseIndexes.containsKey(beginNounPhraseIndex))
					nounPhraseIndexes.put(beginNounPhraseIndex, endNounPhraseIndex);
				//setVerifiedStatus(beginNounPhraseIndex, endNounPhraseIndex, embeddedwords);
			}	
			if(iterator == wordEmbeddingsByIndex.size()-1) break;
			prevIndex = entry.getKey();
			iterator+=1;
		}	
		RecommandedNounPhraseResult nounPhraseResult = new RecommandedNounPhraseResult();
		nounPhraseResult.setRecommandedTokenRelationships(embeddedwords);
		nounPhraseResult.setNounPhraseIndexes(nounPhraseIndexes);
		return nounPhraseResult;
	}
	
	private RecommendedTokenRelationship create(RecommendedTokenRelationship from, RecommendedTokenRelationship to){
		return factory.createRecommendedRelationship(WordEmbeddingTypes.defaultEdge, EdgeTypes.related, from.getTokenRelationship().getFromToken(),to.getTokenRelationship().getToToken());
	}
	
	
	public List<RecommendedTokenRelationship> addEdges(List<RecommendedTokenRelationship> edges, RelationshipInput input){
		Map<String, RecommendedTokenRelationship> edgesByKey = RecommandedTokenRelationshipUtil.getByUniqueKey(edges);
		
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		this.input = input;
		for(int i =0; i<edges.size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship = edges.get(i);
			if(!recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge))continue; 
			RelationshipMapping mapping = findMapping(recommandedTokenRelationship);
			if(mapping!=null) continue; 
			result.addAll(findPermitation(i, recommandedTokenRelationship, edges,edgesByKey));
		}
		return result;
	}
	
	private List<RecommendedTokenRelationship> findPermitation(int fromIndex,RecommendedTokenRelationship recommandedTokenRelationship,List<RecommendedTokenRelationship> edges,Map<String, RecommendedTokenRelationship> edgesByKey ){
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		for(int i =fromIndex+1; i<edges.size();i++){
			RecommendedTokenRelationship iterating = edges.get(i);
			if(!iterating.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge))return  result;
			RecommendedTokenRelationship newRelationship = create(recommandedTokenRelationship,iterating);
			RelationshipMapping mapping = findMapping(newRelationship);
			if(mapping==null) continue; 
			if(edgesByKey.containsKey(newRelationship.getKey())) continue;
			newRelationship.getTokenRelationship().setNamedEdge(mapping.getNamedEdgeName());
			result.add(newRelationship);
		}
		return result;
	}
	
	private Map<Integer,RecommendedTokenRelationship> getFilteredWordEmbeddings(List<RecommendedTokenRelationship> wordEmbeddings){
		Map<Integer,RecommendedTokenRelationship> result = new HashMap<>();
		for(int i = 0;i<wordEmbeddings.size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship =  wordEmbeddings.get(i);
			TokenRelationship tokenRelationship = recommandedTokenRelationship.getTokenRelationship();
			String type = tokenRelationship.getEdgeName();
			if(type.equals(WordEmbeddingTypes.defaultEdge) || type.equals(WordEmbeddingTypes.secondPrep) ||
					type.equals(WordEmbeddingTypes.secondVerb))
					result.put(i, recommandedTokenRelationship);
		}
		return result;
	}
	
	private RelationshipMapping findMapping(RecommendedTokenRelationship recommandedTokenRelationship){
		for(RelationshipMapping mapping :input.getRelationshipMappings()){
			if(!isWordTokenMatchToRelationship(mapping.getIsFromSemanticType(),false,mapping.getFromToken(), recommandedTokenRelationship.getTokenRelationship().getFromToken())) continue; 
			if(isWordTokenMatchToRelationship(mapping.getIsToSemanticType(),false,mapping.getToToken(), recommandedTokenRelationship.getTokenRelationship().getToToken())) {
				return mapping;
			 
			}
		}
		return null;
	}
	
	
	private void processSingleEdge(RecommendedTokenRelationship recommandedTokenRelationship){
		RelationshipMapping mapping = findMapping(recommandedTokenRelationship);
		if(mapping==null) return;
		recommandedTokenRelationship.getTokenRelationship().setNamedEdge(mapping.getNamedEdgeName());
	}
}
