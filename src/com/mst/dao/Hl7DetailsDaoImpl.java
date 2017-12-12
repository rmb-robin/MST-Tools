package com.mst.dao;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.mongodb.morphia.query.Query;

import com.mst.interfaces.dao.Hl7DetailsDao; 
import com.mst.model.HL7Details;
import com.mst.model.discrete.DiscreteData;
 
public class Hl7DetailsDaoImpl  extends BaseDocumentDaoImpl<HL7Details> implements Hl7DetailsDao  {

	public Hl7DetailsDaoImpl() {
		super(HL7Details.class);
	}

	public List<HL7Details> getByOrgName(String orgName) {
		return datastoreProvider.getDefaultDb().createQuery(HL7Details.class)
		.field("org").equalIgnoreCase(orgName)
		.asList();
	}

}
