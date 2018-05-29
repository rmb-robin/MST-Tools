package com.mst.sentenceprocessing;

import java.util.List;

import com.mst.dao.DisceteDataComplianceDisplayFieldsDaoImpl;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.DisceteDataComplianceDisplayFieldsDao;
import com.mst.interfaces.sentenceprocessing.DiscreteDataBucketIdentifier;
import com.mst.interfaces.sentenceprocessing.DiscreteDataInputProcessor;
import com.mst.interfaces.sentenceprocessing.DiscreteDataNormalizer;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.discrete.DiscreteDataBucketIdentifierResult;
import com.mst.model.metadataTypes.DiscreteDataBucketIdenticationType;
import com.mst.model.sentenceProcessing.Sentence;

public class DiscreteDataInputProcessorImpl implements DiscreteDataInputProcessor {

	private DiscreteDataNormalizer discreteDataNormalizer; 
	private DisceteDataComplianceDisplayFieldsDao complianceDisplayFieldsDao;
	DiscreteDataBucketIdentifier bucketIdentifier;
	
	
	public DiscreteDataInputProcessorImpl(MongoDatastoreProvider provider){
		discreteDataNormalizer = new DiscreteDataNormalizerImpl();
		complianceDisplayFieldsDao = new DisceteDataComplianceDisplayFieldsDaoImpl();
		complianceDisplayFieldsDao.setMongoDatastoreProvider(provider);
		bucketIdentifier = new DiscreteDataBucketIdentifierImpl();
	}

	public DiscreteData processDiscreteData(DiscreteData discreteData, List<Sentence> sentences, String resultType){
		discreteData = discreteDataNormalizer.process(discreteData);
		
		if(discreteData.getOrganizationId()==null)return discreteData;
		DisceteDataComplianceDisplayFields fields = complianceDisplayFieldsDao.getbyOrgname(discreteData.getOrganizationId());
		if(fields==null) return discreteData; 
		DiscreteDataBucketIdentifierResult result =  bucketIdentifier.getBucket(discreteData,resultType, sentences, fields);
		if(result==null)return discreteData;
		
		discreteData.setBucketName(result.getBucketName());
		if(resultType.equals(DiscreteDataBucketIdenticationType.compliance))
			discreteData.setIsCompliant(result.getIsCompliant());
		else 
			discreteData.setExpectedFollowup(result.getExpectedFollowup());
		return discreteData;
	}

	public DiscreteData processDiscreteData(DiscreteData discreteData, String resultType){
		discreteData = discreteDataNormalizer.process(discreteData);
		return discreteData;
	}
	
	
}
