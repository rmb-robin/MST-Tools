package com.mst.model.sentenceProcessing;

import java.util.List;

import com.mst.interfaces.sentenceprocessing.RecommandedSubjectAnnotator;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.util.RecommandedTokenRelationshipUtil;

public class RecommandedSubjectAnnotatorImpl implements RecommandedSubjectAnnotator {

	public void annotate(SentenceDiscovery discovery){
		boolean isSubjectComplimentSet = false;
		List<RecommendedTokenRelationship> relationships = discovery.getWordEmbeddings();
		for(int i =0;i<relationships.size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship = relationships.get(i);
			if(RecommandedTokenRelationshipUtil.isDefault(recommandedTokenRelationship)){
				boolean isSet = processSubjectCompliment(i, relationships);
				if(!isSubjectComplimentSet)
					isSubjectComplimentSet = isSet;
			}
		
			if(recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.secondVerb))
				processSubject(i, relationships);
		}
		if(!isSubjectComplimentSet)
			setSubjectComplimentOnVerb(relationships);
	}
	
	private void setSubjectComplimentOnVerb(List<RecommendedTokenRelationship> relationships){
		for(int i =0;i<relationships.size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship = relationships.get(i);
			 if(recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.firstVerb))
			{
				setSubjectComplement(recommandedTokenRelationship);
				return;
			}
			else if(recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.bothVerbs)){
				if(i+1>=relationships.size()){
					setSubjectComplement(recommandedTokenRelationship);
					return;
				}
				if(relationships.get(i+1).getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.bothVerbs)){ 
					setSubjectComplement(relationships.get(i+1));
					return;
				}
				else {
					setSubjectComplement(recommandedTokenRelationship);
					return;
				}
			}
		}
	}
	
	
	private void processSubject(int index, List<RecommendedTokenRelationship> relationships){
		for(int i = index; i>=0;i--){
			RecommendedTokenRelationship recommandedTokenRelationship = relationships.get(i);
			if(i==0){
				recommandedTokenRelationship.getTokenRelationship().getFromToken().setPropertyValueType(PropertyValueTypes.Subject);
				return;
			}
			
			RecommendedTokenRelationship prevRecommandedTokenRelationship = relationships.get(i-1);
			
			if(!recommandedTokenRelationship.getTokenRelationship().getToToken().getToken().equals(prevRecommandedTokenRelationship.getTokenRelationship().getFromToken().getToken())){
				recommandedTokenRelationship.getTokenRelationship().getFromToken().setPropertyValueType(PropertyValueTypes.Subject);
				return;
			}
		}
	}
	
	private boolean isPunc(RecommendedTokenRelationship relationship){
		return relationship.getTokenRelationship().getToToken().getPos()== PartOfSpeachTypes.PUNCTUATION;
	}
	
	private void setSubjectComplement(RecommendedTokenRelationship recommandedTokenRelationship){
		recommandedTokenRelationship.getTokenRelationship().getToToken().setPropertyValueType(PropertyValueTypes.SubjectComplement);
	}
	
	private boolean processSubjectCompliment(int index, List<RecommendedTokenRelationship> relationships){
	
		for(int i = index; i<relationships.size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship = relationships.get(i);
			if(!RecommandedTokenRelationshipUtil.isDefault(recommandedTokenRelationship))return false;
			

			if(i+1>=relationships.size()){
				if(isPunc(recommandedTokenRelationship)){
					setSubjectComplement(relationships.get(i-1));
				}
				if(i+1 == relationships.size()) {
					setSubjectComplement(recommandedTokenRelationship);
					return true;
				}
			}
			
			RecommendedTokenRelationship nextRecommandedTokenRelationship = relationships.get(i+1);
			
			if(nextRecommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.secondPrep)){
				setSubjectComplement(recommandedTokenRelationship);
				return true;
			}
		
			if(!RecommandedTokenRelationshipUtil.isDefault(nextRecommandedTokenRelationship))return false;
			if(i+1==relationships.size()-1){
				if(isPunc(nextRecommandedTokenRelationship)){
					setSubjectComplement(recommandedTokenRelationship);
					return true;
				}
			}

			if(!recommandedTokenRelationship.getTokenRelationship().getToToken().getToken().equals(nextRecommandedTokenRelationship.getTokenRelationship().getFromToken().getToken())){
				setSubjectComplement(recommandedTokenRelationship);
				return true;
			}
		
		}
		return false;
	}
}
