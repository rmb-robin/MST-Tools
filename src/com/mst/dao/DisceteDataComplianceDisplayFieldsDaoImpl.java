package com.mst.dao;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.DisceteDataComplianceDisplayFieldsDao;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;

public class DisceteDataComplianceDisplayFieldsDaoImpl extends BaseDocumentDaoImpl<DisceteDataComplianceDisplayFields> implements DisceteDataComplianceDisplayFieldsDao{

	public DisceteDataComplianceDisplayFieldsDaoImpl() {
		super(DisceteDataComplianceDisplayFields.class);
	}
	

	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		super.setMongoDatastoreProvider(provider);
	}

	public DisceteDataComplianceDisplayFields getbyOrgname(String orgName) {
		return super.getQueryByFieldName(orgName, "orgName").get();
	}

}
