package com.mst.dao;

import java.time.LocalDate;
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
	public List<DiscreteData> getByNameAndDate(String orgName, LocalDate date) {
		Query<DiscreteData> query = datastoreProvider.getDataStore().createQuery(DiscreteData.class);
		 query
		 .field("organizationName").equal(orgName);
		// .field("processingDate").equal(date);
		 return query.asList();
	}
	
	
	public long getCountByNameAndDate(String orgName, LocalDate date) {
		Query<DiscreteData> query = datastoreProvider.getDataStore().createQuery(DiscreteData.class);
		 query
		 .field("organizationName").equal(orgName);
		// .field("processingDate").equal(date);
		 return query.countAll();
	}
	
	
	
	
}
