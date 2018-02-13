package com.mst.dao;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
	
	public List<DiscreteData> getDiscreteDatas(DiscreteDataFilter dataFilter, String orgId, boolean allValues){
		Query<DiscreteData> query = datastoreProvider.getDefaultDb().createQuery(DiscreteData.class);
		query.disableValidation();
		
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
		
		if(dataFilter.getReportFinalizedDate().size()==2){
			addDateQuery(dataFilter.getReportFinalizedDate().get(0),query,true);
			addDateQuery(dataFilter.getReportFinalizedDate().get(1),query,false);
		}
		
		
		if(!dataFilter.getProcessingDate().isEmpty()){
			addDateQuery(dataFilter.getProcessingDate().get(0),query,true);
			addDateQuery(dataFilter.getProcessingDate().get(0),query,false);
		}
		
		if(!dataFilter.getPatientDob().isEmpty()){
			addDateQuery(dataFilter.getPatientDob().get(0),query,true);
			addDateQuery(dataFilter.getPatientDob().get(0),query,false);
		}
		
		if(!dataFilter.getPatientMRN().isEmpty())
			query.field("patientMRN").hasAnyOf(dataFilter.getPatientMRN());
		
		if(!dataFilter.getPatientAccount().isEmpty())
			query.field("patientAccount").hasAnyOf(dataFilter.getPatientAccount());
		
		if(!dataFilter.getPatientEncounter().isEmpty())
			query.field("patientEncounter").hasAnyOf(dataFilter.getPatientEncounter());
		
		if(!dataFilter.getVrReportId().isEmpty())
			query.field("vrReportId").hasAnyOf(dataFilter.getVrReportId());
		
		if(!dataFilter.getAccessionNumber().isEmpty())
			query.field("accessionNumber").hasAnyOf(dataFilter.getAccessionNumber());
		
		if(!dataFilter.getBucketName().isEmpty())
			query.field("bucketName").hasAnyOf(dataFilter.getBucketName());
		
		if(dataFilter.getIsComplaint()!=null)
			query.field("isComplaint").equal(dataFilter.getIsComplaint());
	
		if(!dataFilter.getReportFinalizedBy().isEmpty())
			query.field("reportFinalizedBy").hasAnyOf(dataFilter.getReportFinalizedBy());
	
		//redo..
    //	if(!dataFilter.getMenopausalStatus().isEmpty())
	//		query.field("customFields.fieldName").hasAnyOf(dataFilter.getMenopausalStatus());
	
		if(!allValues)
			query.retrievedFields(true, "id","reportFinalizedDate");
		return query.asList();
	}
	
	private void addDateQuery(LocalDate localdate, Query<DiscreteData> query, boolean isFirst){
		Date date = Date.from(localdate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		if(isFirst)
			query.filter("reportFinalizedDate >= ", date);
		else 
		{
			LocalDate nextLocaldate = localdate.plusDays(1);
			Date nexDate = Date.from(nextLocaldate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
			
			query.filter("reportFinalizedDate < ", nexDate);	
		}
		
	}
	
	private Query<DiscreteData> getQueryByOrgNameAndDate(String orgId, LocalDate localDate){
		Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Query<DiscreteData> query = datastoreProvider.getDefaultDb().createQuery(DiscreteData.class);
		 query
		 .field("organizationId").equal(orgId)
		 .filter("processingDate =", date);
		 return query;
	}

	public List<DiscreteData> getByIds(Set<String> ids) {
		List<ObjectId> objectids = new ArrayList<>();
		ids.forEach(a-> objectids.add(new ObjectId(a)));
		
		Query<DiscreteData> query = datastoreProvider.getDefaultDb().createQuery(DiscreteData.class);
		 query
		 .field("id").hasAnyOf(objectids);
		 return query.asList();
	}
	
	public String save(DiscreteData discreteData, boolean isReprocess){
		if(!isReprocess) 
			discreteData.setId(new ObjectId());
		discreteData.setTimeStamps();
		return super.save(discreteData);
	}

	@Override
	public void saveCollection(List<DiscreteData> discreteDatas) {
		datastoreProvider.getDefaultDb().save(discreteDatas);
	}

	@Override
	public DiscreteData getbyid(String id) {
		Query<DiscreteData> query = datastoreProvider.getDefaultDb().createQuery(DiscreteData.class);
		 query
		 .field("id").equal(id);
		 return query.get();
	}
}