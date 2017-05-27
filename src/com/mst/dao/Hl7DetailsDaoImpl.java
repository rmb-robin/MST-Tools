package com.mst.dao;

import java.util.List;
import com.mst.interfaces.dao.Hl7DetailsDao; 
import com.mst.model.HL7Details;
 
public class Hl7DetailsDaoImpl  extends BaseDocumentDaoImpl<HL7Details> implements Hl7DetailsDao  {

	public Hl7DetailsDaoImpl() {
		super(HL7Details.class);
	}

	public List<HL7Details> getByOrgName(String orgName) {
		return datastoreProvider.getDataStore().createQuery(HL7Details.class)
		.field("org").equalIgnoreCase(orgName)
		.asList();
	}
}
