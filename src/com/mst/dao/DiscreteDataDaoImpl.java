package com.mst.dao;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.mongodb.morphia.query.Query;

import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
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
	
	private Query<DiscreteData> getQueryByOrgNameAndDate(String orgId, LocalDate localDate){
		Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Query<DiscreteData> query = datastoreProvider.getDataStore().createQuery(DiscreteData.class);
		 query
		 .field("organizationId").equal(orgId)
		 .filter("processingDate =", date);
		 return query;
	}
}

