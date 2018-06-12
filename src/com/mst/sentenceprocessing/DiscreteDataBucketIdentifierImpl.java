package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mst.interfaces.sentenceprocessing.DiscreteDataBucketIdentifier;
import com.mst.metadataProviders.DiscreteDataCustomFieldNames;
import com.mst.model.discrete.ComplianceDisplayFieldsBucketItem;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.discrete.DiscreteDataBucketGroup;
import com.mst.model.discrete.DiscreteDataBucketIdentifierResult;
import com.mst.model.discrete.DiscreteDataCustomField;
import com.mst.model.discrete.Followup;
import com.mst.model.discrete.FollowupProcedure;
import com.mst.model.metadataTypes.DiscreteDataBucketIdenticationType;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.TokenRelationshipUtil;

public class DiscreteDataBucketIdentifierImpl implements DiscreteDataBucketIdentifier {

	private class CompliantOnFollowupProcedure{
		public boolean isCompliant;
		public String matchedvalue; 
	}
	
	
	public DiscreteDataBucketIdentifierResult getBucket(DiscreteData discreteData,String resultType, List<Sentence> sentences,  DisceteDataComplianceDisplayFields fields){
		for (Map.Entry<String, DiscreteDataBucketGroup> entry : fields.getBucketGroups().entrySet()) {
			List<ComplianceDisplayFieldsBucketItem> filteredBuckets = findBucketsOnDiscrete(entry.getValue().getBucketItems(), discreteData);
			if(filteredBuckets.isEmpty()) continue;
			for(Sentence sentence: sentences){
				if(isSentenceMatchOnEdges(sentence, entry.getValue().getMatchedEdges())){
				ComplianceDisplayFieldsBucketItem bucket = findBucketForSentence(sentence, filteredBuckets);
				 if(bucket!=null) {
					 DiscreteDataBucketIdentifierResult result = new DiscreteDataBucketIdentifierResult();
					 result.setBucketName(bucket.getBucketName());
					 if(resultType.equals(DiscreteDataBucketIdenticationType.compliance))
							 result.setIsCompliant(areAllSentenceCompliant(sentences,bucket));
					 else 
						 result.setExpectedFollowup(bucket.getFollowUp());
					 return result;
				 }
			   }
			}
		}
		return null;
	}
	
	private boolean isSentenceMatchOnEdges(Sentence sentence, Map<String,HashSet<String>> mandatoryEdges){
		Map<String,List<TokenRelationship>> tokensByEdgename = sentence.getTokenRelationsByNameMap();
		for(Map.Entry<String, HashSet<String>> entry : mandatoryEdges.entrySet()){
			if(!tokensByEdgename.containsKey(entry.getKey())) return false;
			List<TokenRelationship> tokenRelationships = tokensByEdgename.get(entry.getKey());
			if(!isSentenceMatchOnEdge(tokenRelationships,entry.getValue()))return false;
		}
		return true;
	}
	
	private boolean isSentenceMatchOnEdge(List<TokenRelationship> sentenceTokenRelationships, HashSet<String> matchedTokFrom){
		for(TokenRelationship tokenRelationship: sentenceTokenRelationships){
			if(tokenRelationship.isToFromTokenSetMatch(matchedTokFrom)) return true;
		}
		return false;
	}
	
	private boolean areAllSentenceCompliant(List<Sentence> sentences, ComplianceDisplayFieldsBucketItem bucket){
		for(Sentence sentence: sentences){
			if(issentenceCompliant(sentence, bucket)) return true;
		}
		return false;
	}
	
	public boolean issentenceCompliant(Sentence sentence, ComplianceDisplayFieldsBucketItem bucket){
		Followup followup = bucket.getFollowUp();
		if(sentence.getTokenRelationships()==null || sentence.getTokenRelationships()==null) return false;
		
		if(!followup.getIsNumeric())
			return isCompliantOnFollowupProcedure(followup,sentence).isCompliant;
		
		return isCompliantOnTime(bucket,sentence);
	}
	
	private boolean isCompliantOnTime(ComplianceDisplayFieldsBucketItem bucket, Sentence sentence){
		List<TokenRelationship> suppCareEdges = TokenRelationshipUtil.getTokenRelationshipsByEdgeName(EdgeNames.suppcare, sentence.getTokenRelationships());
		if(suppCareEdges.isEmpty()) return false;
		List<TokenRelationship> matched = TokenRelationshipUtil.getTokenRelationshipsByEdgeName(EdgeNames.time, sentence.getTokenRelationships());
		if(matched.size()==0) return false;
		
		for(TokenRelationship relationship : matched){
			if(isTokenCardinal(relationship.getFromToken())){
				if(isCompliantOnNumeric(relationship.getFromToken(), relationship.getToToken(), relationship,bucket)) return true;
			}
			
			if(isTokenCardinal(relationship.getToToken())){
				if(isCompliantOnNumeric(relationship.getToToken(), relationship.getFromToken(), relationship,bucket)) return true;
			}
		}
		return false;
	}
	
	private CompliantOnFollowupProcedure isCompliantOnFollowupProcedure(Followup followup, Sentence sentence){
		
		CompliantOnFollowupProcedure result = new CompliantOnFollowupProcedure();
		Map<String,List<TokenRelationship>> relationshipMap = sentence.getTokenRelationsByNameMap();
		for(FollowupProcedure procedure: followup.getProcedures()){
			if(!relationshipMap.containsKey(procedure.getEdgeName())) continue;
			
			List<TokenRelationship> matchedRelationships = relationshipMap.get(procedure.getEdgeName());
			for(TokenRelationship tokenRelationship: matchedRelationships){
				if(tokenRelationship.isToFromTokenMatch(procedure.getValue())) {
					result.matchedvalue = procedure.getValue();
					result.isCompliant = true;
				}
			}
		}
		return result;
	}
	
	private boolean isCompliantOnNumeric(WordToken cardinal, WordToken durationMeasure, TokenRelationship relationship,ComplianceDisplayFieldsBucketItem bucket){
		Double measure = WordTokenTypeConverter.tryConvertToDouble(cardinal);
		if(measure==null)return false; 
		if(measure!= bucket.getFollowUp().getDuration()) return false; 
		if(!durationMeasure.getToken().equals(bucket.getFollowUp().getDurationMeasure())) return false;
		return true;
	}
	
	private boolean isTokenCardinal(WordToken workToken){
		if(workToken.getSemanticType()==null) return false;
		return workToken.getSemanticType().equals(SemanticTypes.CARDINAL_NUMBER);
	}
	
	private ComplianceDisplayFieldsBucketItem findBucketForSentence(Sentence sentence, List<ComplianceDisplayFieldsBucketItem> bucketItems){
		if(sentence.getTokenRelationships()==null)return null; 
		if(sentence.getTokenRelationships().isEmpty())return null; 
			
		List<TokenRelationship> unitOfMeasureEdges = TokenRelationshipUtil.getTokenRelationshipsByEdgeName(EdgeNames.unitOfMeasure,sentence.getTokenRelationships());
		if(unitOfMeasureEdges==null) return null;
		
		for(TokenRelationship tokenRelationship: unitOfMeasureEdges){
			ComplianceDisplayFieldsBucketItem bucket = findBucket(tokenRelationship, bucketItems);
			if(bucket!=null) return bucket;
		}
		return null;
	}
	
	private ComplianceDisplayFieldsBucketItem findBucket(TokenRelationship tokenRelationship, List<ComplianceDisplayFieldsBucketItem> bucketItems){
		double measurement = 0;
		String unitOfMeasure  = null;
		Double value = WordTokenTypeConverter.tryConvertToDouble(tokenRelationship.getFromToken());
		if(value != null){
			measurement = value; 
			unitOfMeasure = tokenRelationship.getToToken().getToken();
		}
		else 
		{ 
			value = WordTokenTypeConverter.tryConvertToDouble(tokenRelationship.getToToken());
			if(value!=null){
				measurement = value;
				unitOfMeasure = tokenRelationship.getFromToken().getToken();
			}
		}
		
		for(ComplianceDisplayFieldsBucketItem bucketItem : bucketItems){
			if(measurement>=bucketItem.getSizeMin() && measurement <= bucketItem.getSizeMax() 
					&& bucketItem.getUnitOfMeasure().toLowerCase().equals(unitOfMeasure))
				return bucketItem;
		}
		return null;
	}
	
	private List<ComplianceDisplayFieldsBucketItem> findBucketsOnDiscrete( List<ComplianceDisplayFieldsBucketItem> input, DiscreteData discreteData){
		List<ComplianceDisplayFieldsBucketItem>  result = new ArrayList<ComplianceDisplayFieldsBucketItem> ();
		for(ComplianceDisplayFieldsBucketItem bucketItem : input){
			if(!filterByAge(discreteData,bucketItem)) continue;
			if(!filterByMenopausalStatus(discreteData,bucketItem))continue;
			result.add(bucketItem);
		}
		return result;
	}
	
	private boolean filterByAge(DiscreteData discreteData, ComplianceDisplayFieldsBucketItem bucketItem){
		if(bucketItem.getAgeEnd()==0)return true;
		return (discreteData.getPatientAge() >= bucketItem.getAgeBegin() && discreteData.getPatientAge() <= bucketItem.getAgeEnd());
	}
	
	private boolean filterByMenopausalStatus(DiscreteData discreteData, ComplianceDisplayFieldsBucketItem bucketItem){
		if(bucketItem.getMenopausalStatus()==null) return true; 
		if(bucketItem.getMenopausalStatus().equals(""))return true;
		 
		 Map<String, DiscreteDataCustomField> customFieldsByName = discreteData.getCustomFields().stream().collect(
	                Collectors.toMap(x -> x.getFieldName(), x -> x));
		
		 if(!customFieldsByName.containsKey(DiscreteDataCustomFieldNames.menopausalStatus))return false;
		 String value = customFieldsByName.get(DiscreteDataCustomFieldNames.menopausalStatus).getValue();
		 return(value.equals(bucketItem.getMenopausalStatus()));
	}
}
