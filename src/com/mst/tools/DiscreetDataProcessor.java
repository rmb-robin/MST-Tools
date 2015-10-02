package com.mst.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.util.JSON;
import com.mst.model.Sentence;
import com.mst.model.discreet.Discreet;
import com.mst.model.discreet.Meds;
import com.mst.model.discreet.Patient;
import com.mst.util.Constants;
import com.mst.util.GsonFactory;

/**
 * @author scottdaugherty
 *
 * The general idea here is to end up with multiple meds entries under one patientId per practice.
 * 
 * {
	   	"_id" : ObjectId("556f6245c2e6a3ef36007d4e"),
	    "patientId" : "1241447",
	    "practice" : "georgia",
	    "sourceFile" : "Bayer_GU_Meds.csv",
	    "meds" : [ 
	        {
	            "name" : "cipro",
	            "doseForm" : "tablet",
	            "doseStrength" : "500",
	            "doseUOM" : "mg",
	            "startDate" : ISODate("2012-04-15T20:00:00.000-04:00"),
	            "endDate" : ISODate("2012-05-16T20:00:00.000-04:00"),
	            "raw" : "cipro 500 mg tab"
	        }, 
	        {
	            "name" : "tamsulosin er",
	            "doseForm" : "capsule",
	            "doseStrength" : "0.4",
	            "doseUOM" : "mg",
	            "endDate" : ISODate("2015-01-07T20:00:00.000-04:00"),
	            "raw" : "tamsulosin er 0.4 mg 24 hr cap"
	        }
	    ],
	    "discreet" : []
	}
 */

public class DiscreetDataProcessor {

	public void processPractice(String practice, Sentence sentence) {
		
		if(practice.equalsIgnoreCase("NASHVILLE")) {
			if(sentence.getSource().equalsIgnoreCase("MEDS")) {
				processMeds(sentence);
			}
		} else if(practice.equalsIgnoreCase("WICHITA")) {
			
		} else {
			processMeds(sentence);
		}
	}
	
	private void processMeds(Sentence sentence) {
		
		try {
			String name = processTextField(sentence.getFullSentence());
			
			if(!sentence.getFullSentence().equalsIgnoreCase("no current medications")) {
				SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
				
				String patientId = processTextField(sentence.getId());
				Date startDate = processDateField((String) sentence.getMetadata().getSimpleMetadataValue("startDate"), sdf);
				Date endDate = processDateField((String) sentence.getMetadata().getSimpleMetadataValue("endDate"), sdf);
			
				Patient patient = new Patient(patientId, sentence.getPractice(), "");
				patient.meds.add(new Meds(name, startDate, endDate));
				
				writeToMongo(patient);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {

		}
	}
	
	private String processTextField(String text) {
		String ret = text.trim();
		return text.length() == 0 ? null : ret.toLowerCase();
	}
	
	private Date processDateField(String in, SimpleDateFormat sdf) {
		Calendar cal = Calendar.getInstance();
		Date date = null;
		try {
			if(in.trim().length() > 0) {
				cal.setTime(sdf.parse(in.trim()));
				
				if(cal.get(Calendar.YEAR) != 1900)
					date = cal.getTime();
			}
		} catch(Exception e) {
			System.out.println("DiscreetDataProcessor.processDateField:" + e.toString() + " - in: " + in);
		}
		return date;
	}

	private void writeToMongo(Patient patient) {
		DBCollection coll = Constants.MongoDB.INSTANCE.getDB().getCollection("discreet");
		Gson gson = GsonFactory.build();
		
		if(patient.patientId != null && patient.patientId.length() > 0) {
			StringBuilder oid = new StringBuilder();
			Patient existingPatient = getPatient(patient.patientId, patient.practice, oid);
			if(existingPatient == null) {
				BasicDBObject obj = (BasicDBObject) JSON.parse(gson.toJson(patient));
				coll.insert(obj);
			} else {
				boolean updated = mergePatientRecords(existingPatient, patient);
				if(updated) {
					DBObject query = QueryBuilder.start().put("_id").is(new ObjectId(oid.toString())).get();
					BasicDBObject obj = (BasicDBObject) JSON.parse(gson.toJson(existingPatient));
					//System.out.println(obj);
					coll.update(query, obj);
				}
			}
		}
	}
	
	private Patient getPatient(String patientId, String clientId, StringBuilder oid) {
		Patient patient = null;
		Gson gson = GsonFactory.build();
		
		DBCollection coll = Constants.MongoDB.INSTANCE.getDB().getCollection("discreet");
		
		DBObject query = QueryBuilder.start().put("patientId").is(patientId).put("practice").is(clientId).get();
		DBCursor cursor = coll.find(query);
		
		if(cursor.count() > 0) {
			BasicDBObject obj = (BasicDBObject) cursor.next();
			oid.append(obj.get("_id").toString());
			patient = gson.fromJson(obj.toString(), Patient.class);
		}
		
		return patient;
	}
	
	private boolean mergePatientRecords(Patient existingPatient, Patient newPatient) {
		//Patient merged = new Patient(existingPatient.patientId, existingPatient.clientId, newPatient.sourceFile);
		boolean updated = false;
		
		for(Meds meds : newPatient.meds) {
			if(!existingPatient.meds.contains(meds)) {
				existingPatient.meds.add(meds);
				updated = true;
			}
		}
		
		for(Discreet discreet : newPatient.discreet) {
			if(!existingPatient.discreet.contains(discreet)) {
				existingPatient.discreet.add(discreet);
				updated = true;
			}
		}
		
		return updated;
		
		//Collections.sort(existingPatient.meds);
		//Collections.sort(newPatient.meds);
		
		//Collections.sort(existingPatient.discreet);
		//Collections.sort(newPatient.discreet);
		
		// this could potentially leave out Meds with the same name but different dates, etc.
		//List<Meds> allMeds = Lists.newArrayList(Iterables.mergeSorted(ImmutableList.of(existingPatient.meds, newPatient.meds), Ordering.usingToString()));
		//List<Discreet> allDiscreet = Lists.newArrayList(Iterables.mergeSorted(ImmutableList.of(existingPatient.discreet, newPatient.discreet), Ordering.natural()));
		
		//merged.meds = allMeds;
		//merged.discreet = allDiscreet;
		
		//return existingPatient;
	}
}
