package com.mst.model.discrete;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Meds { //implements Comparable<Meds> {
	public String name;
	public String altName;
	public String status; // active, inactive, discontinued
	public String doseForm; // capsule, tablet
	public String doseStrength; // 100, 0.25
	public String doseUOM; // mg, unit, mcg
	public String doseRoute; // oral, transdermal
	public Date startDate;
	public Date endDate;
	public Date discontinuedDate;
	public String raw;
	public String altRaw;
	
	private static Pattern DOSAGE_REGEX = Pattern.compile("(?i)(\\d+\\.?,?\\d*\\s*)(mg(?:\\/\\w*)?|mcg(?:\\/\\w*)?|gram(?:\\/\\w*)?|g\\/\\w*|unit\\/ml|unit)");
	//private static Pattern DOSAGE_REGEX = Pattern.compile("(?i)(\\d*\\.?,?\\d*\\s*)(mg\\/g|mg|mcg|gram(?:\\/\\w*)?|g\\/\\w*|unit\\/ml|unit)");
	private static Pattern FORM_REGEX = Pattern.compile("(?i)(aerosol|cap(sule)?|tab(let)?|cream|gel|gum|lotion|ointment|powder|suppository|syrup|jelly|packet|spray|solution|patch|suspension|oil)");
	private static Pattern ROUTE_REGEX = Pattern.compile("(?i)(oral|transdermal|inject(able|ion)?|topical|sublingual|(intra)?nasal|rectal|vaginal|urethral|inhalation|inhaler|subcutaneous|ophthalmic|external|intramuscular|intravenous)");
	
	public Meds(String name) {
		this(name, null, null, null, null, null);
	}
	
	public Meds(String name, Date startDate, Date endDate) {
		this(name, startDate, endDate, null, null, null);
	}
	
	public Meds(String name, Date startDate, Date endDate, String altName, String status, Date discontinuedDate) {
		this.raw = name;
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.altName = altName;
		this.status = status;
		this.discontinuedDate = discontinuedDate;
		
		parseMedsData(this);
		
		if(this.altName != null) {
			Meds meds = new Meds(altName);
			parseMedsData(meds);
			this.altName = meds.name;
			this.altRaw = meds.raw;
		}
	}

	public Meds(String name, Date startDate, Date endDate, String altName, String status, Date discontinuedDate, String doseForm, String doseStrength, String doseUOM, String doseRoute, String raw) {
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.altName = altName;
		this.status = status;
		this.discontinuedDate = discontinuedDate;
		this.doseForm = doseForm;
		this.doseStrength = doseStrength;
		this.doseUOM = doseUOM;
		this.doseRoute = doseRoute;
		this.raw = raw;
		
//		parseMedsData(this);
//		
//		if(this.altName != null) {
//			Meds meds = new Meds(this.altName);
//			parseMedsData(meds);
//			this.altName = meds.name;
//			this.altRaw = meds.raw;
//		}
	}

	
	private static void parseMedsData(Meds meds) {

		try {
			Matcher matcher = DOSAGE_REGEX.matcher(meds.raw);
			
			if(matcher.find()) {
				meds.doseStrength = matcher.group(1).trim();
				meds.doseUOM = matcher.group(2).trim();
				meds.name = meds.raw.substring(0, matcher.start()-1).trim();
				if(meds.name.endsWith(","))
					meds.name = meds.name.substring(0, meds.name.length()-1).trim();
	//			for(int i=0; i<matcher.groupCount(); i++) {
	//				String foo = matcher.group(i);
	//				System.out.println(foo);
	//			}
			}
			
			matcher = FORM_REGEX.matcher(meds.raw);
			
			if(matcher.find()) {
				String form = matcher.group().trim();
				
				if(form.equalsIgnoreCase("tab"))
					meds.doseForm = "tablet";
				else if(form.equalsIgnoreCase("cap"))
					meds.doseForm = "capsule";
				else
					meds.doseForm = form;
			}
			
			matcher = ROUTE_REGEX.matcher(meds.raw);
			
			if(matcher.find()) {
				String route = matcher.group().trim();
				
				if(route.matches("inject(able|ion)?"))
					meds.doseRoute = "injection";
				else if(route.matches("inhaler"))
					meds.doseRoute = "inhalation";
				else
					meds.doseRoute = route;
			}
		} catch(Exception e) {
			System.out.println("Error in Meds.parseMedsData()");
			System.out.println(e.toString());
			System.out.println(meds.raw);
		}
	}
	
//	@Override
//	public int compareTo(Meds compare) {
//		int val = this.toString().compareTo(compare.toString());
//		return val;
//    }
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.name).append(" | ")
			.append(this.altName).append(" | ")
			.append(this.status).append(" | ")
			.append(this.startDate).append(" | ")
			.append(this.endDate).append(" | ")
			.append(this.discontinuedDate);
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o){
	    if(o instanceof Meds){
	        Meds toCompare = (Meds) o;
	        return this.toString().equals(toCompare.toString());
	    }
	    return false;
	}
}
