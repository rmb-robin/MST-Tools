package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mst.interfaces.sentenceprocessing.DiscreteDataBucketIdentifier;
import com.mst.metadataProviders.DiscreteDataCustomFieldNames;
import com.mst.model.discrete.ComplianceDisplayFieldsBucketItem;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.discrete.DiscreteDataCustomField;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;

public class DiscreteDataBucketIdentifierImpl implements DiscreteDataBucketIdentifier {

	public String getBucket(DiscreteData discreteData, List<Sentence> sentences,  DisceteDataComplianceDisplayFields fields){
		for (Map.Entry<String, List<ComplianceDisplayFieldsBucketItem>> entry : fields.getBuckets().entrySet()) {
			List<ComplianceDisplayFieldsBucketItem> filteredBuckets = findBucketsOnDiscrete(entry.getValue(), discreteData);
			if(filteredBuckets.isEmpty()) continue;
			for(Sentence sentence: sentences){
				if(sentence.getOrigSentence().toLowerCase().contains(entry.getKey().toLowerCase())){
				 String bucketName = findBucketForSentence(sentence, filteredBuckets);
				 if(bucketName!=null) return bucketName;
				}
			}
		}
		return null;
	}
	
	private String findBucketForSentence(Sentence sentence, List<ComplianceDisplayFieldsBucketItem> bucketItems){
		if(sentence.getTokenRelationships()==null)return null; 
		if(sentence.getTokenRelationships().isEmpty())return null; 
			
		List<TokenRelationship> unitOfMeasureEdges = sentence.getTokenRelationshipsByEdgeName(EdgeNames.unitOfMeasure);
		if(unitOfMeasureEdges==null) return null;
		
		for(TokenRelationship tokenRelationship: unitOfMeasureEdges){
			String bucketName = findBucket(tokenRelationship, bucketItems);
			if(bucketName!=null) return bucketName;
		}
		return null;
	}
	
	private String findBucket(TokenRelationship tokenRelationship, List<ComplianceDisplayFieldsBucketItem> bucketItems){
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
				return bucketItem.getBucketName();
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
