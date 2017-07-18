package com.mst.dao;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;

import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.model.SentenceQuery.DiscreteDataFilter;
import com.mst.model.discrete.DiscreteData;

public class DiscreteDataDaoImpl extends BaseDocumentDaoImpl<DiscreteData> implements DiscreteDataDao {

	public DiscreteDataDaoImpl(){
		super(DiscreteData.class);
	}
	
	@Override
	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		super.setMongoDatastoreProvider(provider);
	}

	@Override
	public List<DiscreteData> getByNameAndDate(String orgId, LocalDate localDate) {
		return getQueryByOrgNameAndDate(orgId,localDate).asList();
	}

	public long getCountByNameAndDate(String orgId, LocalDate localDate) {
		 return getQueryByOrgNameAndDate(orgId,localDate).countAll();
	}
	
	public List<DiscreteData> getDiscreteDataIds(DiscreteDataFilter dataFilter, String orgId){
		Query<DiscreteData> query = datastoreProvider.getDataStore().createQuery(DiscreteData.class);
		
		query.field("organizationId").equal(orgId);
		
		if(!dataFilter.getExamDescription().isEmpty())
			query.field("examDescription").hasAnyOf(dataFilter.getExamDescription());
		
		
		if(!dataFilter.getModality().isEmpty())
			query.field("modality").hasAnyOf(dataFilter.getModality());
		
		if(dataFilter.getPatientAge().size()==2){
			query.field("patientAge").greaterThanOrEq(dataFilter.getPatientAge().get(0));
			query.field("patientAge").lessThanOrEq(dataFilter.getPatientAge().get(1));
		}
		
		if(!dataFilter.getPatientSex().isEmpty())
			query.field("sex").hasAnyOf(dataFilter.getPatientSex());
		
		
		if(!dataFilter.getReadingLocation().isEmpty())
			query.field("readingLocation").hasAnyOf(dataFilter.getReadingLocation());
		
		if(!dataFilter.getResultStatus().isEmpty())
			query.field("resultStatus").hasAnyOf(dataFilter.getResultStatus());
		
//		if(dataFilter.getReportFinalizedDate()!=null){
//			Date date = Date.from(dataFilter.getReportFinalizedDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
//			.filter("processingDate =", date);
//		}
		
		query.retrievedFields(true, "id");
		return query.asList();
	//	List<ObjectId> result = new ArrayList<>();
		
		//discreteDatas.forEach(a-> result.add(a.getId()));
		//return result;
	}
	
	private Query<DiscreteData> getQueryByOrgNameAndDate(String orgId, LocalDate localDate){
		Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Query<DiscreteData> query = datastoreProvider.getDataStore().createQuery(DiscreteData.class);
		 query
		 .field("organizationId").equal(orgId)
		 .filter("processingDate =", date);
		 return query;
	}
}

