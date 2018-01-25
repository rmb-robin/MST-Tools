package com.mst.dao;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.mongodb.morphia.query.Query;

import com.mst.interfaces.dao.Hl7DetailsDao; 
import com.mst.model.HL7Details;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.raw.AllHl7Elements;
 
public class Hl7DetailsDaoImpl  extends BaseDocumentDaoImpl<HL7Details> implements Hl7DetailsDao  {

	public Hl7DetailsDaoImpl() {
		super(HL7Details.class);
	}

	public List<HL7Details> getByOrgName(String orgName) {
		return datastoreProvider.getDefaultDb().createQuery(HL7Details.class)
		.field("org").equalIgnoreCase(orgName)
		.asList();
	}

	@Override
	public AllHl7Elements getAllElements() {
		return datastoreProvider.getDefaultDb().createQuery(AllHl7Elements.class).get();
	}

	@Override
	public void saveAllElements(AllHl7Elements allHl7Elements) {
	 datastoreProvider.getDefaultDb().save(allHl7Elements);

	}
}
