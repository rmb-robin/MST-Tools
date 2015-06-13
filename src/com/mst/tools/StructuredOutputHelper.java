package com.mst.tools;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.util.JSON;
import com.mst.model.GenericToken;
import com.mst.model.MapValue;
import com.mst.model.PrepPhraseToken;
import com.mst.model.Sentence;
import com.mst.model.SentenceMetadata;
import com.mst.model.StructuredData;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.VerbPhraseToken;
import com.mst.model.WordToken;
import com.mst.model.discreet.Meds;
import com.mst.model.discreet.Patient;
import com.mst.util.Constants;
import com.mst.util.GsonFactory;
import com.mst.util.Props;
import com.opencsv.CSVWriter;

public class StructuredOutputHelper {

	//private final Logger logger = LoggerFactory.getLogger(getClass());
	private Map<String, String[]> constructors = new HashMap<String, String[]>();
	
	private CSVWriter report;

	private Map<String, Integer> stCounts = new HashMap<String, Integer>(); // missing semantic type counts report
	private Map<String, Integer> relCounts = new HashMap<String, Integer>(); // missing relationship counts report
	private List<String[]> relByToken = new ArrayList<String[]>(); // missing relationships by token
	private List<String[]> stByToken = new ArrayList<String[]>(); // missing ST by token
	private List<String[]> foundSTByToken = new ArrayList<String[]>();
	
	private Pattern plusRegex = Pattern.compile("\\d+\\s*\\+\\s*\\d+");
	
	private Gson gson = GsonFactory.build();
	private DateFormat sdf = new SimpleDateFormat("M/d/yyyy");
	
	private boolean writeLogs = false; 
	
	private enum Headers {
		PATIENT_ID,
		VISIT_DATE,
		AGE,
		SEX,
		RACE,
		SUBJECT,
		ADMIN_OF_DRUG,
		DIAGNOSTIC_PROCEDURE,
		KNOWN_EVENT_DATE,
		OTHER,
		PROCEDURE_BY_METHOD,
		FINDING_SITE,
		ABSOLUTE_VALUE,
		GENERAL_VALUE,
		CLINICAL_FINDING,
		THERAPY,
		COMPLICATIONS,
		TREATMENT_PLAN,
		ABSENCE,
		RELATED_TO_VERB,
		SENTENCE;
	};
	
	public StructuredOutputHelper(boolean writeLogs) {
		
		this.writeLogs = writeLogs;
		
		constructors.put("age|sociid", new String[]{"Age",""});
		constructors.put("bevpa|number", new String[]{"Absolute Value",""});
		constructors.put("bevbpr|number", new String[]{"Absolute Value",""});
		constructors.put("bevbpr|number", new String[]{"Absolute Value","Current"});
		constructors.put("for|number", new String[]{"Absolute Value",""});
		constructors.put("from|number", new String[]{"Absolute Value",""});
		constructors.put("in|number", new String[]{"Absolute Value",""});
		constructors.put("of|number", new String[]{"Absolute Value",""});
		constructors.put("on|number", new String[]{"Absolute Value",""});
		constructors.put("to|number", new String[]{"Absolute Value",""});
		constructors.put("with|number", new String[]{"Absolute Value",""});
		constructors.put("avb-added|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-administered|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-began|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-cancelled|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("avb-chose|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("avb-complains|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("avb-considered|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("avb-continued|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-continues|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-decrease|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-delay|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-discussed|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("avb-elected|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("avb-enrolled|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("avb-finish|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("avb-given|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-increase|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-obtained|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-presnt|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-refused|drugpr", new String[]{"Admin of Drug","Refused"});
		constructors.put("avb-remained|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-returns|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-scheduled|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-show|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("avb-signed|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("avb-start|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-stop|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("avb-stopped|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("avb-suggest|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("avb-takes|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-tolerated|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-treated|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("avb-undwen|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("bevbpa|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("bevbpa-failing|drugpr", new String[]{"Admin of Drug","Failing"});
		constructors.put("bevbpa-start|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("bevbpr|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("bevbpr-approved|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("bevbpr-chosen|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("bevbpr-consider|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("bevbpr-continue|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("bevbpr-discussed|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("bevbpr-do|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("bevbpr-failed|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("bevbpr-going|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("bevbpr-refused|drugpr", new String[]{"Admin of Drug","Refused"});
		constructors.put("bevbpr-seen|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("bevbpr-started|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("bevbpr-stopping|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("bevbpr-tolerate|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("bevbpr-worsened|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("bevpa-beenapproved|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("bevpa-beenfailing|drugpr", new String[]{"Admin of Drug","Failing"});
		constructors.put("bevpa-chosen|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("bevpa-considered|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("bevpa-continued|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("bevpa-decided|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("bevpa-discussed|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("bevpa-refused|drugpr", new String[]{"Admin of Drug","Refused"});
		constructors.put("bevpa-returned|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("bevpa-started|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("bevpa-stopped|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("bevpa-treated|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("bevpa-worked|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("bevpa-worsened|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|avb-added", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-administered", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-began", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-cancelled", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|avb-chose", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb-complains", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|avb-considered", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb-continued", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-continues", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-decrease", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-delay", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-discussed", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb-elected", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb-enrolled", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb-finish", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|avb-given", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-increase", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-obtained", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-presnt", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-refused", new String[]{"Admin of Drug","Refused"});
		constructors.put("drugpr|avb-remained", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-returns", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-scheduled", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-show", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|avb-signed", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|avb-start", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-stop", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|avb-stopped", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|avb-suggest", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb-takes", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-tolerated", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-treated", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb-undwen", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|bevbpa", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevbpa", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevbpa-failing", new String[]{"Admin of Drug","Failing"});
		constructors.put("drugpr|bevbpa-start", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevbpr", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|bevbpr-approved", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevbpr-chosen", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevbpr-consider", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevbpr-continue", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevbpr-discussed", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevbpr-do", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevbpr-failed", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevbpr-going", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevbpr-refused", new String[]{"Admin of Drug","Refused"});
		constructors.put("drugpr|bevbpr-seen", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevbpr-started", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevbpr-stopping", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|bevbpr-tolerate", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevbpr-worsened", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevpa-beenapproved", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevpa-beenfailing", new String[]{"Admin of Drug","Failing"});
		constructors.put("drugpr|bevpa-chosen", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevpa-considered", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevpa-continued", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevpa-decided", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevpa-discussed", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevpa-refused", new String[]{"Admin of Drug","Refused"});
		constructors.put("drugpr|bevpa-returned", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevpa-started", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevpa-stopped", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|bevpa-treated", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevpa-worked", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevpa-worsened", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|drugdrly", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|mvb", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|mvb-beevaluated", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|mvb-bestarted", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|mvb-betreated", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|mvb-continue", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|mvb-eval", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|mvb-recomm", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|mvb-start", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|mvb-undergo", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|ptconsul", new String[]{"Admin of Drug","Future"});
		constructors.put("for|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("from|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("had|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("in|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("mvb|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("mvb|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("mvb-beapproved|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("mvb-beevaluated|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("mvb-bestarted|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("mvb-betreated|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("mvb-continue|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("mvb-eval|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("mvb-recomm|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put("mvb-start|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("mvb-undergo|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("of|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("off|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("on|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("since|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("to|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("with|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("age|race", new String[]{"Age",""});
		constructors.put("avb-added|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-added|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-added|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-added|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-added|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-added|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-administered|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-administered|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-administered|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-administered|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-administered|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-administered|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-began|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-began|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-began|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-began|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-began|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-began|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-cancelled|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-cancelled|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-cancelled|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-cancelled|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-cancelled|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-cancelled|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-chose|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-chose|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-chose|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-chose|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-chose|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-chose|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-complains|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-complains|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-complains|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-complains|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-complains|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-complains|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-considered|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-considered|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-considered|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-considered|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-considered|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-considered|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-continued|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-continued|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-continued|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-continued|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-continued|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-continued|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-continues|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-continues|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-continues|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-continues|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-continues|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-continues|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-decrease|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-decrease|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-decrease|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-decrease|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-decrease|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-decrease|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-delay|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-delay|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-delay|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-delay|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-delay|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-delay|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-denies|dysn", new String[]{"Clinical Finding","Absence"});
		constructors.put("avb-denies|sympto", new String[]{"Clinical Finding","Absence"});
		constructors.put("avb-discussed|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-discussed|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-discussed|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-discussed|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-discussed|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-discussed|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-elected|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-elected|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-elected|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-elected|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-elected|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-elected|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-enrolled|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-enrolled|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-enrolled|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-enrolled|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-enrolled|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-enrolled|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-finish|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-finish|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-finish|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-finish|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-finish|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-finish|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-given|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-given|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-given|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-given|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-given|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-given|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-increase|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-increase|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-increase|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-increase|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-increase|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-increase|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-obtained|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-obtained|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-obtained|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-obtained|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-obtained|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-obtained|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-presnt|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-presnt|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-presnt|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-presnt|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-presnt|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-presnt|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-refused|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-refused|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-refused|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-refused|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-refused|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-refused|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-remained|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-remained|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-remained|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-remained|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-remained|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-remained|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-returns|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-returns|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-returns|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-returns|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-returns|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-returns|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-scheduled|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-scheduled|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-scheduled|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-scheduled|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-scheduled|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-scheduled|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-show|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-show|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-show|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-show|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-show|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-show|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-signed|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-signed|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-signed|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-signed|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-signed|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-signed|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-start|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-start|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-start|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-start|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-start|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-start|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-stop|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-stop|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-stop|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-stop|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-stop|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-stop|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-stopped|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-stopped|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-stopped|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-stopped|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-stopped|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-stopped|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-suggest|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-suggest|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-suggest|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-suggest|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-suggest|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-suggest|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-takes|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-takes|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-takes|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-takes|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-takes|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-takes|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-tolerated|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-tolerated|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-tolerated|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-tolerated|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-tolerated|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-tolerated|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-treated|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-treated|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-treated|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-treated|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-treated|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-treated|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("avb-undwen|neop", new String[]{"Clinical Finding",""});
		constructors.put("avb-undwen|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("avb-undwen|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("avb-undwen|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("avb-undwen|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("avb-undwen|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa|sympto", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-failing|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-failing|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-failing|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-failing|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-failing|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-failing|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-start|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-start|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-start|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-start|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-start|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpa-start|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr|sympto", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-approved|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-approved|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-approved|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-approved|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-approved|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-approved|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-chosen|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-chosen|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-chosen|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-chosen|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-chosen|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-chosen|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-consider|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-consider|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-consider|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-consider|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-consider|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-consider|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-continue|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-continue|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-continue|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-continue|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-continue|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-continue|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-discussed|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-discussed|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-discussed|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-discussed|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-discussed|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-discussed|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-do|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-do|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-do|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-do|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-do|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-do|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-failed|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-failed|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-failed|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-failed|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-failed|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-failed|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-going|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-going|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-going|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-going|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-going|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-going|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-refused|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-refused|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-refused|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-refused|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-refused|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-refused|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-seen|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-seen|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-seen|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-seen|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-seen|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-seen|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-started|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-started|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-started|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-started|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-started|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-started|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-stopping|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-stopping|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-stopping|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-stopping|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-stopping|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-stopping|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-tolerate|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-tolerate|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-tolerate|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-tolerate|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-tolerate|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-tolerate|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-worsened|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-worsened|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-worsened|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-worsened|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-worsened|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevbpr-worsened|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenapproved|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenapproved|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenapproved|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenapproved|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenapproved|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenapproved|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenfailing|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenfailing|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenfailing|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenfailing|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenfailing|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-beenfailing|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-chosen|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-chosen|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-chosen|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-chosen|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-chosen|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-chosen|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-considered|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-considered|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-considered|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-considered|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-considered|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-considered|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-continued|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-continued|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-continued|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-continued|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-continued|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-continued|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-decided|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-decided|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-decided|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-decided|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-decided|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-decided|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-discussed|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-discussed|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-discussed|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-discussed|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-discussed|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-discussed|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-refused|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-refused|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-refused|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-refused|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-refused|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-refused|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-returned|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-returned|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-returned|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-returned|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-returned|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-returned|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-started|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-started|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-started|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-started|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-started|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-started|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-stopped|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-stopped|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-stopped|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-stopped|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-stopped|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-stopped|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-treated|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-treated|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-treated|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-treated|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-treated|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-treated|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worked|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worked|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worked|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worked|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worked|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worked|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worsened|neop", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worsened|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worsened|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worsened|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worsened|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("bevpa-worsened|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("dysn|bevbpa", new String[]{"Clinical Finding",""});
		constructors.put("dysn|bevbpr", new String[]{"Clinical Finding",""});
		constructors.put("dysn|mvb", new String[]{"Clinical Finding",""});
		constructors.put("for|dysn", new String[]{"Clinical Finding",""});
		constructors.put("for|neop", new String[]{"Clinical Finding",""});
		constructors.put("for|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("for|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("for|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("for|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("for|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("for|sympto", new String[]{"Clinical Finding",""});
		constructors.put("had|dysn", new String[]{"Clinical Finding","Past"});
		constructors.put("mvb|dysn", new String[]{"Clinical Finding",""});
		constructors.put("mvb|neop", new String[]{"Clinical Finding",""});
		constructors.put("mvb|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("mvb|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("mvb|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("mvb|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("mvb|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("mvb|sympto", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beapproved|neop", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beapproved|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beapproved|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beapproved|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beapproved|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beapproved|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beevaluated|neop", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beevaluated|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beevaluated|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beevaluated|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beevaluated|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("mvb-beevaluated|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("mvb-bestarted|neop", new String[]{"Clinical Finding",""});
		constructors.put("mvb-bestarted|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("mvb-bestarted|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("mvb-bestarted|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("mvb-bestarted|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("mvb-bestarted|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("mvb-betreated|neop", new String[]{"Clinical Finding",""});
		constructors.put("mvb-betreated|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("mvb-betreated|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("mvb-betreated|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("mvb-betreated|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("mvb-betreated|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("mvb-continue|neop", new String[]{"Clinical Finding",""});
		constructors.put("mvb-continue|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("mvb-continue|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("mvb-continue|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("mvb-continue|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("mvb-continue|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("mvb-eval|neop", new String[]{"Clinical Finding",""});
		constructors.put("mvb-eval|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("mvb-eval|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("mvb-eval|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("mvb-eval|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("mvb-eval|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("mvb-recomm|neop", new String[]{"Clinical Finding",""});
		constructors.put("mvb-recomm|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("mvb-recomm|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("mvb-recomm|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("mvb-recomm|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("mvb-recomm|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("mvb-start|neop", new String[]{"Clinical Finding",""});
		constructors.put("mvb-start|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("mvb-start|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("mvb-start|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("mvb-start|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("mvb-start|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("mvb-undergo|neop", new String[]{"Clinical Finding",""});
		constructors.put("mvb-undergo|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("mvb-undergo|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("mvb-undergo|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("mvb-undergo|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("mvb-undergo|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("negata|bone pain", new String[]{"Clinical Finding","No bone pain"});
		constructors.put("negata|dysn", new String[]{"Clinical Finding","Absence"});
		constructors.put("negata|pain", new String[]{"Clinical Finding","No pain"});
		constructors.put("neop|avb-added", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-began", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-given", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-show", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpa", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpa", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevbpr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb-undergo", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-added", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-began", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-given", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-show", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpa", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevbpr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb-undergo", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-added", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-began", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-given", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-show", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpa", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevbpr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb-undergo", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-added", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-began", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-given", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-show", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpa", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevbpr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb-undergo", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-added", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-began", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-given", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-show", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpa", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevbpr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb-undergo", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-added", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-began", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-given", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-show", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpa", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevbpr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb-undergo", new String[]{"Clinical Finding",""});
		constructors.put("of|dysn", new String[]{"Clinical Finding",""});
		constructors.put("of|neop", new String[]{"Clinical Finding",""});
		constructors.put("of|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("of|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("of|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("of|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("of|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("of|sympto", new String[]{"Clinical Finding",""});
		constructors.put("pathology|tnmstage", new String[]{"Clinical Finding",""});
		constructors.put("sympto|bevbpa", new String[]{"Clinical Finding",""});
		constructors.put("sympto|bevbpr", new String[]{"Clinical Finding",""});
		constructors.put("sympto|mvb", new String[]{"Clinical Finding",""});
		constructors.put("with|neop", new String[]{"Clinical Finding",""});
		constructors.put("with|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("with|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("with|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("with|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("with|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("with|sympto", new String[]{"Clinical Finding",""});
		constructors.put("avb-undwen|diap", new String[]{"Diagnostic Procedure","Past"});
		constructors.put("diap|avb-decrease", new String[]{"Diagnostic Procedure","Decreased"});
		constructors.put("diap|avb-increase", new String[]{"Diagnostic Procedure","Increased"});
		constructors.put("diap|bevbpa", new String[]{"Diagnostic Procedure",""});
		constructors.put("diap|bevbpa", new String[]{"Diagnostic Procedure",""});
		constructors.put("diap|bevbpr", new String[]{"Diagnostic Procedure",""});
		constructors.put("diap|mvb", new String[]{"Diagnostic Procedure",""});
		constructors.put("for|diap", new String[]{"Diagnostic Procedure",""});
		constructors.put("had|diap", new String[]{"Diagnostic Procedure","Past"});
		constructors.put("in|diap", new String[]{"Diagnostic Procedure",""});
		constructors.put("mvb|diap", new String[]{"Diagnostic Procedure",""});
		constructors.put("of|diap", new String[]{"Diagnostic Procedure",""});
		constructors.put("with|diap", new String[]{"Diagnostic Procedure",""});
		constructors.put("in|bpoc", new String[]{"Finding Site",""});
		constructors.put("of|bpoc", new String[]{"Finding Site",""});
		constructors.put("bpoc|sympto", new String[]{"Finding Site",""});
		constructors.put("from|date", new String[]{"Known Event Date",""});
		constructors.put("in|date", new String[]{"Known Event Date",""});
		constructors.put("in|tempor", new String[]{"Known Event Date",""});
		constructors.put("mvb|tempor", new String[]{"Known Event Date",""});
		constructors.put("of|date", new String[]{"Known Event Date",""});
		constructors.put("on|date", new String[]{"Known Event Date",""});
		constructors.put("tempor|avb-presnt", new String[]{"Known Event Date",""});
		constructors.put("tempor|bevbpa", new String[]{"Known Event Date",""});
		constructors.put("tempor|bevbpa", new String[]{"Known Event Date",""});
		constructors.put("tempor|bevbpr", new String[]{"Known Event Date",""});
		constructors.put("tempor|mvb", new String[]{"Known Event Date",""});
		constructors.put("electronically|avb-signed", new String[]{"Procedural",""});
		constructors.put("avb-finish|prbymeth", new String[]{"Procedure by Method","Past"});
		constructors.put("avb-undwen|prbymeth", new String[]{"Procedure by Method","Past"});
		constructors.put("diap|avb-show", new String[]{"Procedure by Method",""});
		constructors.put("had|prbymeth", new String[]{"Procedure by Method","Past"});
		constructors.put("mvb|prbymeth", new String[]{"Procedure by Method",""});
		constructors.put("of|prbymeth", new String[]{"Procedure by Method",""});
		constructors.put("on|prbymeth", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|avb-chose", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|avb-show", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|bevbpa", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|bevbpa", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|bevbpr", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|bevpa-start", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|mvb", new String[]{"Procedure by Method",""});
		constructors.put("with|prbymeth", new String[]{"Procedure by Method",""});
		constructors.put("race|sociid", new String[]{"Race",""});
		constructors.put("bevbpr|sociid", new String[]{"Sex","Social History"});
		constructors.put("avb-presnt|", new String[]{"Subject",""});
		constructors.put("bevbpa-restart|", new String[]{"Subject",""});
		constructors.put("bevbpa-start|", new String[]{"Subject",""});
		constructors.put("bevbpr|patien", new String[]{"Subject",""});
		constructors.put("for|patien", new String[]{"Subject",""});
		constructors.put("mvb|patien", new String[]{"Subject",""});
		constructors.put("mvb|propn", new String[]{"Subject",""});
		constructors.put("mvb|sociid", new String[]{"Subject",""});
		constructors.put("patien|avb-chose", new String[]{"Subject",""});
		constructors.put("patien|avb-continues", new String[]{"Subject",""});
		constructors.put("patien|avb-denies", new String[]{"Subject",""});
		constructors.put("patien|avb-finish", new String[]{"Subject",""});
		constructors.put("patien|avb-presnt", new String[]{"Subject",""});
		constructors.put("patien|avb-start", new String[]{"Subject",""});
		constructors.put("patien|avb-takes", new String[]{"Subject",""});
		constructors.put("patien|avb-tolerated", new String[]{"Subject",""});
		constructors.put("patien|avb-undwen", new String[]{"Subject",""});
		constructors.put("patien|avd-denies", new String[]{"Subject",""});
		constructors.put("patien|bevbpa", new String[]{"Subject",""});
		constructors.put("patien|bevbpa", new String[]{"Subject",""});
		constructors.put("patien|bevbpa-restart", new String[]{"Subject","Current"});
		constructors.put("patien|bevbpa-start", new String[]{"Subject","Current"});
		constructors.put("patien|bevbpr", new String[]{"Subject",""});
		constructors.put("patien|bevpa-give", new String[]{"Subject",""});
		constructors.put("patien|bevpa-start", new String[]{"Subject",""});
		constructors.put("patien|bevpa-treat", new String[]{"Subject",""});
		constructors.put("patien|had", new String[]{"Subject",""});
		constructors.put("patien|mvb", new String[]{"Subject",""});
		constructors.put("patien|pvb-opted", new String[]{"Subject",""});
		constructors.put("propn|avb-finish", new String[]{"Subject",""});
		constructors.put("propn|avb-show", new String[]{"Subject",""});
		constructors.put("propn|avb-start", new String[]{"Subject",""});
		constructors.put("propn|avb-stop", new String[]{"Subject",""});
		constructors.put("propn|bevbpa", new String[]{"Subject",""});
		constructors.put("propn|bevbpa", new String[]{"Subject",""});
		constructors.put("propn|bevbpa-start", new String[]{"Subject",""});
		constructors.put("propn|bevbpr", new String[]{"Subject",""});
		constructors.put("propn|bevpa-give", new String[]{"Subject",""});
		constructors.put("propn|mvb", new String[]{"Subject",""});
		constructors.put("provid|avb-discussed", new String[]{"Subject",""});
		constructors.put("provid|avb-stop", new String[]{"Subject",""});
		constructors.put("provid|mvb-recomm", new String[]{"Subject",""});
		constructors.put("sociid|bevbpa", new String[]{"Subject",""});
		constructors.put("sociid|bevbpa", new String[]{"Subject",""});
		constructors.put("sociid|bevbpr", new String[]{"Subject",""});
		constructors.put("sociid|mvb", new String[]{"Subject",""});
		constructors.put("for|waiting", new String[]{"Treatment Plan",""});
		constructors.put("in|remiss", new String[]{"Treatment Plan",""});
		constructors.put("in|ptconsul", new String[]{"Treatment Plan",""});
		constructors.put("number|tempor", new String[]{"Treatment Plan",""});
		constructors.put("on|waiting", new String[]{"Treatment Plan",""});
		constructors.put("number|bevbpa", new String[]{"Value - Absolute",""});
		constructors.put("avb-decrease|qlco", new String[]{"Value - General",""});
		constructors.put("avb-increase|qlco", new String[]{"Value - General",""});
		constructors.put("bevbpa|qlco", new String[]{"Value - General",""});
		constructors.put("bevbpr|qlco", new String[]{"Value - General",""});
		constructors.put("neop|bevpa", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpa", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpa", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpa", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpa", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpa", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevpr", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevpr", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevpr", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevpr", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevpr", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevpr", new String[]{"Clinical Finding",""});

	}
	
	private String buildStructuredOutput(Sentence sentence, boolean writeToMongo) {
		
		StructuredData structured = new StructuredData();
		
		structured.patientID = sentence.getId();
		structured.practice = sentence.getPractice();
		structured.study = sentence.getStudy();
		structured.date = sentence.getProcedureDate();
		structured.sentence = sentence.getFullSentence();
		
		SentenceMetadata metadata = sentence.getMetadata();
		ArrayList<WordToken> words = sentence.getWordList();
		
		List<Integer> processedPrepPhrases = new ArrayList<Integer>();
		List<Integer> processedNounPhrases = new ArrayList<Integer>();
		
		// 1) loop through verb phrases
		for(VerbPhraseMetadata verbPhrase : metadata.getVerbMetadata()) {
			Multimap<String, MapValue> related = ArrayListMultimap.create();
			
			switch(verbPhrase.getVerbClass()) {
				case ACTION:
				case LINKING_VERB:
				case VERB_OF_BEING:
				case MODAL_AUX:
					
					// 1a) query constructors by subj/vb
					String subjST = (verbPhrase.getSubj() != null) ? words.get(verbPhrase.getSubj().getPosition()).getSemanticType() : null;
					String verbST = (verbPhrase.getVerbs().size() == 1) ? words.get(verbPhrase.getVerbs().get(0).getPosition()).getSemanticType() : verbPhrase.getSemanticType();
					
					if(verbST != null) {
						if(subjST != null) {
							String[] attribute = constructors.get(subjST + "|" + verbST);
							if(attribute != null) {
								related.put(attribute[0], new MapValue(verbPhrase.getSubj().getToken(), attribute[1], subjST + "|" + verbST, verbPhrase.getSubj().isNegated()));
								logFound(verbPhrase.getSubj().getToken(), subjST, subjST+"|"+verbST, verbPhrase.getSubj().getToken()+"|"+verbPhrase.getVerbString(), "VP", sentence.getFullSentence());
							} else {
								// write to audit report (i.e. no constructor)
								logMissing(relCounts, subjST + "|" + verbST);
								logMissing2(relByToken, verbPhrase.getSubj().getToken(), verbPhrase.getVerbString(), subjST, verbST, sentence.getFullSentence(), "VP");
							}
							
							// process prep phrases related to subj
							for(int ppIdx : verbPhrase.getSubj().getPrepPhrasesIdx()) {
								processPrepPhrase(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence());
							}
							
							// process noun phrases related to subj
							if(verbPhrase.getSubj().getNounPhraseIdx() > -1) {
								processNounPhrase(words, metadata, related, processedNounPhrases, verbPhrase.getSubj().getNounPhraseIdx(), sentence.getFullSentence(), null);
							}
							
						} else {
							// write to unknowns report (i.e. no ST)
							if(verbPhrase.getSubj() != null) {
								logMissing(stCounts, verbPhrase.getSubj().getToken());
								logMissingST(verbPhrase.getSubj().getToken(), verbPhrase.getSubj().getToken()+"|"+verbPhrase.getVerbString(), sentence.getFullSentence(), "VP");
							} else
								System.out.println("Verb is null? " + verbPhrase.getVerbString());
						}
						
						// 1b) query constructors by vb/subjc(s)
						for(VerbPhraseToken subjc : verbPhrase.getSubjC()) {
							String subjcST = words.get(subjc.getPosition()).getSemanticType();
						
							if(subjcST != null) {
								String[] attribute = constructors.get(verbST + "|" + subjcST);
								
								if(attribute != null) {
									related.put(attribute[0], new MapValue(subjc.getToken(), attribute[1], verbST + "|" + subjcST, subjc.isNegated()));
									logFound(subjc.getToken(), subjcST, verbST+"|"+subjcST, verbPhrase.getVerbString()+"|"+subjc.getToken(),"VP", sentence.getFullSentence());
								} else {
									logMissing(relCounts, subjcST + "|" + verbST);
									logMissing2(relByToken, verbPhrase.getVerbString(), subjc.getToken(), verbST, subjcST, sentence.getFullSentence(), "VP");
								}
								
								// process prep phrases related to subjc
								for(int ppIdx : subjc.getPrepPhrasesIdx()) {
									processPrepPhrase(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence());
								}
								
								// process noun phrases related to subjc
								if(subjc.getNounPhraseIdx() > -1) {
									processNounPhrase(words, metadata, related, processedNounPhrases, subjc.getNounPhraseIdx(), sentence.getFullSentence(), verbST);
								}
								
							} else {
								logMissing(stCounts, subjc.getToken());
								//logMissingST(missingSTByToken, verbPhrase.getVerbString(), subjc.getToken(), null, null, sentence.getFullSentence(), "VP");
								logMissingST(subjc.getToken(), verbPhrase.getVerbString()+"|"+subjc.getToken(), sentence.getFullSentence(), "VP");
							}
						}
						
						// 1c) process prep phrases related to verb
						for(int ppIdx : verbPhrase.getVerbs().get(verbPhrase.getVerbs().size()-1).getPrepPhrasesIdx()) {
							processPrepPhrase(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence());
						}
					} else {
						// TODO use verbPhrase.getVerbString() instead?
						String verb = (verbPhrase.getVerbs().size() == 1) ? words.get(verbPhrase.getVerbs().get(0).getPosition()).getToken() : verbPhrase.getVerbString();
						logMissing(stCounts, verb);
						//logMissingST(missingSTByToken, verbPhrase.getVerbString(), "", null, null, sentence.getFullSentence(), "VP");
						logMissingST(verbPhrase.getVerbString(), "", sentence.getFullSentence(), "VP");
					}
					
				case PREPOSITIONAL:
				case INFINITIVE:
					
				default:
					
					break;
			}
			
			if(!related.isEmpty()) {
				structured.related.add(related);
			}
		}
		
		// 2) loop through prep phrases to catch any that aren't grammatically-related to a verb phrase
		for(int i=0; i < metadata.getPrepMetadata().size(); i++) {
			Multimap<String, MapValue> temp = ArrayListMultimap.create();
			processPrepPhrase(words, metadata, temp, processedNounPhrases, processedPrepPhrases, i, sentence.getFullSentence());
			if(!temp.isEmpty())
				structured.unrelated.add(temp);
		}
		
		// 3) loop through noun phrases to catch any that aren't grammatically-related to a verb phrase
		for(int i=0; i < metadata.getNounMetadata().size(); i++) {
			Multimap<String, MapValue> temp = ArrayListMultimap.create();
			processNounPhrase(words, metadata, temp, processedNounPhrases, i, sentence.getFullSentence(), null);
			if(!temp.isEmpty())
				structured.unrelated.add(temp);
		}
		
		processRegex(structured.regex, sentence.getFullSentence());
		
		if(writeLogs)
			report(structured);
		
		try {
			if(writeToMongo) {
				if(!structured.related.isEmpty() || !structured.unrelated.isEmpty() || !structured.regex.isEmpty()) {
					DBCollection coll = Constants.MongoDB.INSTANCE.getDB().getCollection("structured");
					DBObject dbObject = (DBObject) JSON.parse(gson.toJson(structured));
					coll.insert(dbObject);
				}
			}
		} catch(Exception e) {
			System.out.println("Error writing structured data object to MongoDB. --- " + e.toString());
		}
		
		return gson.toJson(structured);
	}
	
	public String process(Sentence sentence, boolean writeToMongo) {
		return buildStructuredOutput(sentence, writeToMongo);
	}
	
	public void process(String practice, String study, int limit, boolean writeToMongo) {
		
		long startTime = Constants.getTime();
		
		try {
			if(writeLogs) {
				initReports(practice, study);
				
				String[] headers = new String[Headers.values().length];
				int j = 0;
				for(Headers header : Headers.values()) {
					headers[j] = header.name();
					j++;
				}
				report.writeNext(headers);
			}
			
			DBCollection coll = Constants.MongoDB.INSTANCE.getDB().getCollection("annotations");
			
			BasicDBList dbList = new BasicDBList();
//			dbList.add(new ObjectId("55573a99e4b0fd98a7a776c1"));
//			dbList.add(new ObjectId("55573a8ee4b0fd98a7a76b26"));
//			dbList.add(new ObjectId("55573a7fe4b0fd98a7a75b5a"));
//			dbList.add(new ObjectId("55577c23e4b01865f20d37f9"));
//			dbList.add(new ObjectId("55577bb5e4b01865f20ce6af"));
//			dbList.add(new ObjectId("55573a9ae4b0fd98a7a777a5"));
//			dbList.add(new ObjectId("55573a85e4b0fd98a7a76226"));
//			dbList.add(new ObjectId("55577bd3e4b01865f20cfc5c"));
//			dbList.add(new ObjectId("55577bfae4b01865f20d19af"));
//			dbList.add(new ObjectId("55577866e4b01865f20a9587"));
//			dbList.add(new ObjectId("55573a98e4b0fd98a7a7754d"));
//			dbList.add(new ObjectId("55573a93e4b0fd98a7a76fec"));
//			dbList.add(new ObjectId("55573a8ce4b0fd98a7a768ff"));
//			dbList.add(new ObjectId("55573f1be4b0fd98a7abe320"));
//			dbList.add(new ObjectId("555737c3e4b0fd98a7a4ab4b"));
//			dbList.add(new ObjectId("55573a91e4b0fd98a7a76e01"));
//			dbList.add(new ObjectId("55573a97e4b0fd98a7a774e1"));
//			dbList.add(new ObjectId("55573976e4b0fd98a7a64ada"));
//			dbList.add(new ObjectId("55573a86e4b0fd98a7a762cc"));
//			dbList.add(new ObjectId("55577b8ae4b01865f20cc918"));
		//	dbList.add(new ObjectId("55577961e4b01865f20b3ec8")); // He had biopsies in March 2007 which were positive for focal Gleason 3+3=6 adenocarcinoma.
			//dbList.add(new ObjectId("55573a71e4b0fd98a7a74d2a"));
			//dbList.add(new ObjectId("55573779e4b0fd98a7a463e8"));
//			dbList.add(new ObjectId("555742ece4b0fd98a7afc531"));
			dbList.add(new ObjectId("55577c18e4b01865f20d3042"));
			DBObject query = QueryBuilder.start()
					//.put("practice").is(practice)
					//.put("study").is(study)
					.put("_id").in(dbList)
					.get();
			
			DBCursor cursor = null;
			
			if(limit == -1)
				cursor = coll.find(query);
			else
				cursor = coll.find(query).limit(limit);
			
			while(cursor.hasNext()) {
				BasicDBObject obj = (BasicDBObject) cursor.next();
				Sentence sentence = gson.fromJson(obj.toString(), Sentence.class);
				
				String foo = buildStructuredOutput(sentence, writeToMongo);
				System.out.println(foo);
			}
			
			cursor.close();
			
			writeLogs(buildReportPath(practice, study));
			
			//getMeds(practice);
			
			System.out.println(Constants.formatTime((Constants.getTime() - startTime)/1000.0));
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			Constants.MongoDB.INSTANCE.close();
			try {
				report.close();
			} catch(Exception e) { e.printStackTrace(); }
		}
	}
	
	private void processPrepPhrase(ArrayList<WordToken> words, SentenceMetadata metadata, Multimap<String, MapValue> results, List<Integer> processedNounPhrases, List<Integer> processedPrepPhrases, int ppIdx, String fullSentence) {
		// Processed as pairs relating to the preposition (ST1|ST2)
		// The preposition is always ST1 and each successive token in the PP is ST2.
		// E.g. "June is my favorite month [of the year]."
		// Constructors will be queried with the corresponding STs for "of|year" ("of|the" will fail the test that requires ST2 to be an OBJ of the PP). 
		// unless... e.g. "[with [prostate cancer]]"
		// Constructors will be queried with the corresponding STs for "with|prostate cancer", i.e ST2 will represent the noun phrase rather than 
		// the individual tokens of which it is comprised.
		
		//Multimap<String, MapValue> results = ArrayListMultimap.create();
		
		if(!processedPrepPhrases.contains(ppIdx)) {
			List<PrepPhraseToken> prepPhrase = metadata.getPrepMetadata().get(ppIdx).getPhrase();
			
			String prepTokenST = words.get(prepPhrase.get(0).getPosition()).getSemanticType();
			
			if(prepTokenST != null) {
				// first, query the constructors by the semantic types of the token preceding the preposition and the preposition itself
				// this is the "cross-border" case
				try {
					String prevTokenST = words.get(prepPhrase.get(0).getPosition()-1).getSemanticType();
					
					if(prevTokenST != null) {
						String[] attribute = constructors.get(prevTokenST + "|" + prepTokenST);
						
						if(attribute != null) {
							String prevToken = words.get(prepPhrase.get(0).getPosition()-1).getToken();
							logFound(prevToken, prevTokenST, prevTokenST+"|"+prepTokenST, prevToken+"|"+prepPhrase.get(0).getToken(), "PPX", fullSentence);
							
							results.put(attribute[0], new MapValue(prevToken, attribute[1], prevTokenST + "|" + prepTokenST));
							
							// Avoid adding this PP to the processedPrepPhrases list. Assuming everything checks out, this will be taken care of below. 
						}
					}
				} catch(IndexOutOfBoundsException e) { }
				
				// loop through each member of the prep phrase after the initial preposition
				for(int i=1; i < prepPhrase.size(); i++) {
					PrepPhraseToken ppToken = prepPhrase.get(i);
					
					// is the token a prep phrase object?
					if(words.get(ppToken.getPosition()).isPrepPhraseObject()) {
						String objTokenST = "";
						String objToken = "";
						
						int npIdx = ppToken.getNounPhraseIdx();
						objTokenST = npIdx > -1 ? metadata.getNounMetadata().get(npIdx).getSemanticType() : null;
						
						// log that the prep|NP pair doesn't have a ST
						if(npIdx > -1 && objTokenST == null) {
							logMissingST(metadata.getNounMetadata().get(npIdx).getNounPhraseString(), prepPhrase.get(0).getToken()+"|"+metadata.getNounMetadata().get(npIdx).getNounPhraseString(), fullSentence, "PP");
						}
						
						// is the object part of a noun phrase and does that noun phrase have a ST?
						if(npIdx > -1 && objTokenST != null) {
							// TODO need additional logic to determine which tokens of the NP should be queried against constructors
							// currently sending up the ST for the entire phrase, which could lead to problems with something like "really enlarged heart"
							// yes, set ST2 to the ST of the noun phrase
							//objTokenST = metadata.getNounMetadata().get(npIdx).getSemanticType();
							objToken = metadata.getNounMetadata().get(npIdx).getNounPhraseString();
						} else {
							// no, set ST2 to the ST of the current token
							objTokenST = words.get(ppToken.getPosition()).getSemanticType();
							objToken = words.get(ppToken.getPosition()).getToken();
							// override npIdx since we want to process individual tokens against the preposition rather than the noun phrase as a whole (since the NP doesn't have a ST)
							npIdx = -1;
						}
						
						if(objTokenST != null) {
							String[] attribute = constructors.get(prepTokenST + "|" + objTokenST);
							if(attribute != null) {
								processedPrepPhrases.add(ppIdx);
								
								logFound(objToken, objTokenST, prepTokenST+"|"+objTokenST, prepPhrase.get(0).getToken()+"|"+objToken, "PP", fullSentence);
								
								if(npIdx > -1) {
									results.put(attribute[0], new MapValue(metadata.getNounMetadata().get(npIdx).getNounPhraseString(), attribute[1], prepTokenST + "|" + objTokenST, metadata.getNounMetadata().get(npIdx).isNegated()));
									processedNounPhrases.add(npIdx);
									// exit the for so as to avoid processing the individual NP tokens
									break;
								} else {
									results.put(attribute[0], new MapValue(ppToken.getToken(), attribute[1], prepTokenST + "|" + objTokenST, metadata.getPrepMetadata().get(ppIdx).isNegated()));
								}
								
							} else {
								// TODO if npIdx > 1 possibly query individual tokens against the preposition if no result from full phrase
								logMissing(relCounts, prepTokenST + "|" + objTokenST);
								logMissing2(relByToken, prepPhrase.get(0).getToken(), objToken, prepTokenST, objTokenST, fullSentence, "PP");
							}
						} else {
							logMissing(stCounts, ppToken.getToken());
							//logMissingST(missingSTByToken, prepPhrase.get(0).getToken(), objToken, null, null, fullSentence, "PP");
							logMissingST(objToken, prepPhrase.get(0).getToken()+"|"+objToken, fullSentence, "PP");
						}
					}
				}
				
				//if(!results.isEmpty())
				//	list.add(results);
				
			} else {
				logMissing(stCounts, prepPhrase.get(0).getToken());
				//logMissingST(missingSTByToken, prepPhrase.get(0).getToken(), "", null, null, fullSentence, "PP");
				logMissingST(prepPhrase.get(0).getToken(), "", fullSentence, "PP");
			}
		}
	}
	
	private void processNounPhrase(ArrayList<WordToken> words, SentenceMetadata metadata, Multimap<String, MapValue> list, List<Integer> processedNounPhrases, int npIdx, String fullSentence, String leftST) {

		// leftST represents the ST for token(s) that may occur before this noun phrase
		// the example used is "He is having abdominal pain."
		// in this case, leftST would be supplied as the ST for "is having"
		// for noun phrases floating off on their own, leftST will be null
		
		if(!processedNounPhrases.contains(npIdx)) {
			
			String npST = metadata.getNounMetadata().get(npIdx).getSemanticType();
			String npString = metadata.getNounMetadata().get(npIdx).getNounPhraseString();
			// first query constructors by ST of entire noun phrase, if present
			// this is sort of another cross-border situation
			if(leftST != null && npST != null) {
				String[] attribute = constructors.get(leftST + "|" + npST);
				
				if(attribute != null) {
					list.put(attribute[0], new MapValue(npString, attribute[1], leftST + "|" + npST, metadata.getNounMetadata().get(npIdx).isNegated()));
					processedNounPhrases.add(npIdx);
					logFound(npString, npST, leftST+"|"+npST, "", "NP", fullSentence);
				} else {
					logMissing(relCounts, leftST + "|" + npST);
					logMissing2(relByToken, "", npString, leftST, npST, fullSentence, "NP");
				}
				
			} else {
			
				// log the fact that the entire NP did not have a noun phrase
				if(npST == null) {
					logMissing(stCounts, npString);
					// commented out for now as it could be very chatty
					//logMissingST(npString, "", fullSentence, "NP");
				}
				
				List<GenericToken> nounPhrase = metadata.getNounMetadata().get(npIdx).getPhrase();
				// processed as consecutive pairs
				// ^^^ why does that comment say "consecutive pairs" when I'm querying every token of the NP against the final token?
				String finalTokenST = words.get(nounPhrase.get(nounPhrase.size()-1).getPosition()).getSemanticType();
				
				if(finalTokenST != null) {
					for(int i=0; i < nounPhrase.size()-1; i++) {
						String tokenST = words.get(nounPhrase.get(i).getPosition()).getSemanticType();
						
						if(tokenST != null) {
							String[] attribute = constructors.get(tokenST + "|" + finalTokenST);
							if(attribute != null) {
								list.put(attribute[0], new MapValue(nounPhrase.get(i).getToken(), attribute[1], tokenST + "|" + finalTokenST, metadata.getNounMetadata().get(npIdx).isNegated()));
								processedNounPhrases.add(npIdx);
								logFound(nounPhrase.get(i).getToken(), tokenST, tokenST+"|"+finalTokenST, nounPhrase.get(i).getToken()+"|"+nounPhrase.get(nounPhrase.size()-1).getToken(), "NP", fullSentence);
							} else {
								logMissing(relCounts, tokenST + "|" + finalTokenST);
								logMissing2(relByToken, nounPhrase.get(i).getToken(), nounPhrase.get(nounPhrase.size()-1).getToken(), tokenST, finalTokenST, fullSentence, "NP");
							}
						} else {
							logMissing(stCounts, nounPhrase.get(i).getToken());
							//logMissingST(missingSTByToken, nounPhrase.get(i).getToken(), nounPhrase.get(nounPhrase.size()-1).getToken(), null, null, fullSentence, "NP");
							logMissingST(nounPhrase.get(i).getToken(), nounPhrase.get(i).getToken()+"|"+nounPhrase.get(nounPhrase.size()-1).getToken(), fullSentence, "NP");
						}
					}
									
				} else {
					logMissing(stCounts, nounPhrase.get(nounPhrase.size()-1).getToken());
					//logMissingST(missingSTByToken, nounPhrase.get(nounPhrase.size()-1).getToken(), "", null, null, fullSentence, "NP");
					logMissingST(nounPhrase.get(nounPhrase.size()-1).getToken(), "", fullSentence, "NP");
				}
			}
		}
	}
	
	private void processRegex(List<Multimap<String, MapValue>> list, String sentence) {
		// PSA/Gleason regex processing. Find instances that could not be picked up by a constructor.
		
		Pattern ggRegex = Pattern.compile(Constants.GLEASON_REGEX);
		Pattern psaRegex1 = Pattern.compile("PSA\\s*\\d\\d\\/\\d\\d\\/\\d\\d:?\\s*<?\\d\\d?\\.?\\d{1,2}"); //PSA 09/26/13: 0.46PSA 01/29/14: 0.18PSA 06/19/14: 0.05.
		//Pattern psaRegex2 = Pattern.compile("PSA\\s*of\\s*\\d?\\d\\.\\d+\\s*((on|in)\\s*(\\d\\d\\/\\d\\d\\/\\d{2}|\\d{4}))?");
		Pattern chesapeakePSA1 = Pattern.compile("(?i)PSA( \\(Most Recent\\))? \\(\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*\\)"); // PSA (Most Recent) (5.4)   PSA (2.5)   PSA (Most Recent) (undetectable)   https://www.regex101.com/r/dV6zN8/1
		Pattern chesapeakePSA2 = Pattern.compile("PSA=\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*(ng\\/ml collected|from)?\\s*(\\d\\d?\\/\\d\\d?\\/\\d{2,4})"); // PSA=12.9 ng/ml collected 12/11/13   PSA=0.8 from 1/27/14     https://www.regex101.com/r/lO3fI5/1
		Pattern chesapeakePSA3 = Pattern.compile("(\\d\\d?\\/\\d\\d?\\/\\d{2,4}):\\s*(-->)?\\s*PSA=\\s*(\\d\\d?\\.?\\d\\d?)"); // 11/11/11: --> PSA= 2.5   12/04/08: PSA=2.2ng/ml   https://www.regex101.com/r/qF6xD5/1
		Pattern chesapeakePSA4 = Pattern.compile("PSA\\s*\\(?(\\d\\d?\\.?\\d\\d?)( ng\\/ml)?\\s*(on )?(\\d\\d?\\/\\d\\d?\\/\\d{2,4}|\\d\\d?\\/\\d\\d?)"); // https://www.regex101.com/r/bO8oC6/2
		Pattern chesapeakePSA5 = Pattern.compile("PSA\\s*(level of|of|down to|up to|is|was|stable at)\\s*(\\d\\d?\\.?\\d\\d?)(?!\\/)(\\s*(on|in|from))?(\\s*\\d\\d?\\/\\d\\d?\\/\\d{2,4}|\\s*\\d\\d?\\/\\d{2,4})?");
		
		final String GLEASON_LABEL = "Gleason";
		final String PSA_LABEL = "PSA";
		final String DIAP_LABEL = "Diagnostic Procedure";
		final String ABSV_LABEL = "Absolute Value";
		final String DATE_LABEL = "Known Event Date";
		
		Matcher matcher = ggRegex.matcher(sentence);

		while(matcher.find()) {
			String val = matcher.group(matcher.groupCount()); // get last group, which should be the Gleason value
			if(val != null) {
				val = val.trim();
				
				Multimap<String, MapValue> mm = ArrayListMultimap.create();
				
				mm.put(DIAP_LABEL, new MapValue(GLEASON_LABEL));
				mm.put(ABSV_LABEL, new MapValue(parseGleasonValue(val), null, ggRegex.toString()));
				
				list.add(mm);
			}
		}

		matcher = chesapeakePSA1.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(2), null, chesapeakePSA1.toString()));
			
			list.add(mm);
		}

		matcher = chesapeakePSA2.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(1), null, chesapeakePSA2.toString()));
			mm.put(DATE_LABEL, new MapValue(matcher.group(3)));
			
			list.add(mm);
		}

		matcher = chesapeakePSA3.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(3).trim(), null, chesapeakePSA3.toString()));
			mm.put(DATE_LABEL, new MapValue(matcher.group(1).trim()));
			
			list.add(mm);
		}

		matcher = chesapeakePSA4.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(1).trim(), null, chesapeakePSA4.toString()));
			if(matcher.groupCount() > 2)
				mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim()));
			//try {
			//	mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim()));
			//} catch(Exception e) { System.out.println(e.toString() + "\n\t" + sentence + "\n\t" + chesapeakePSA4.toString()); }
			
			list.add(mm);
		}

		matcher = chesapeakePSA5.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(2).trim(), null, chesapeakePSA5.toString()));
			//for(int i=0; i <= matcher.groupCount(); i++) {
			//	System.out.println(i + " - " + matcher.group(i));
			//}
			if(matcher.group(matcher.groupCount()) != null)
				mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim()));
			
			list.add(mm);
		}
		
		matcher = psaRegex1.matcher(sentence);

		while(matcher.find()) {
			String[] vals = matcher.group().split(" "); 
			if(vals.length > 0) {
				try {
					//unrelated.put("Diagnostic Procedure", "PSA|" + vals[2] + "|" + (vals[1].indexOf(':') > -1 ? vals[1].substring(0, vals[1].length()-1) : vals[1].trim()));
					//structured.unrelated.put("Diagnostic Procedure Value", vals[2]);
					//structured.unrelated.put("Diagnostic Procedure Date", vals[1].indexOf(':') > -1 ? vals[1].substring(0, vals[1].length()-1) : vals[1].trim());
					
				} catch(Exception e) {
					System.out.println(matcher.group());
				}
			}
		}
	}
	
	private void getMeds(String practice) {
		// ### add rows from discreet collection ###
		DBCollection coll = Constants.MongoDB.INSTANCE.getDB().getCollection("discreet");
		
		String[] data = new String[Headers.values().length];
		Arrays.fill(data, "");
		
		DBObject query = QueryBuilder.start()
				.put("practice").is(practice)
				.get();

		DBCursor cursor = coll.find(query);

		while(cursor.hasNext()) {					
			BasicDBObject obj = (BasicDBObject) cursor.next();
			//System.out.println(obj.toString());
			Patient patient = gson.fromJson(obj.toString(), Patient.class);
			
			for(Meds med : patient.meds) {
				Arrays.fill(data, "");
				data[Headers.PATIENT_ID.ordinal()] = patient.patientId;
				data[Headers.ADMIN_OF_DRUG.ordinal()] = med.name;
				String startDate = med.startDate != null ? sdf.format(med.startDate) : "";
				String endDate = med.endDate != null ? sdf.format(med.endDate) : "";
				data[Headers.KNOWN_EVENT_DATE.ordinal()] = startDate;
				data[Headers.OTHER.ordinal()] = endDate;
				
				report.writeNext(data);
			}
		}
		
		cursor.close();
	}
	
	private String buildReportPath(String practice, String study) {
		StringBuilder reportPath = new StringBuilder();
		reportPath.append(Props.getProperty("report_path"))
				  .append(practice)
				  .append("/")
				  .append(study)
				  .append("/")
				  .append(new SimpleDateFormat("M_d_yyyy").format(new Date()))
				  .append("/");
		
		File file = new File(reportPath + "foo.txt").getParentFile();
		if(file != null) {
			file.mkdirs();
		}
		
		return reportPath.toString();
	}
	
	private void writeLogs(String path) {
		if(writeLogs) {
			CSVWriter csvSTCounts = null;
			CSVWriter csvSTByToken = null;
			CSVWriter csvRelCounts = null;
			CSVWriter csvRelByToken = null;
			CSVWriter csvFoundST = null;
			
			try {
				csvSTCounts = new CSVWriter(new FileWriter(path + "missing_st_counts.csv"), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
				csvSTByToken = new CSVWriter(new FileWriter(path + "missing_st_by_token.csv"), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER);
				csvRelCounts = new CSVWriter(new FileWriter(path + "missing_relationship_counts.csv"), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
				csvRelByToken = new CSVWriter(new FileWriter(path + "missing_relationships_by_token.csv"), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER);
				csvFoundST = new CSVWriter(new FileWriter(path + "found_st_by_token.csv"), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER);
				String[] data = new String[2];
				//String[] data2 = new String[6];
				
				for(String key : stCounts.keySet()) {
					//System.out.println(key + "," + missingST.get(key));
					data[0] = key;
					data[1] = String.valueOf(stCounts.get(key));
					csvSTCounts.writeNext(data);
				}
				
				for(String key : relCounts.keySet()) {
					//System.out.println(key + "," + missingConstr.get(key));
					data[0] = key;
					data[1] = String.valueOf(relCounts.get(key));
					csvRelCounts.writeNext(data);
				}
				
				String[] constr2Headers = { "relationship","left","right","type","sentence" };
				csvRelByToken.writeNext(constr2Headers);
	//			for(String key : missingConstrByToken.keySet()) {
	//				String[] values = missingConstrByToken.get(key);
	//				String[] tokens = key.split("\\|"); 
	//				data2[0] = values[1] + "|" + values[2];
	//				data2[1] = tokens[0];
	//				data2[2] = tokens[1];
	//				data2[3] = values[0];
	//				data2[4] = values[3];
	//				data2[5] = values[4];
	//				constr2.writeNext(data2);
	//			}
				for(String[] array : relByToken) {
					csvRelByToken.writeNext(array);
				}
							
				String[] st2Headers = { "token","relationship","type","sentence" };
				csvSTByToken.writeNext(st2Headers);
				
				for(String[] array : stByToken) {
					csvSTByToken.writeNext(array);
				}
				
				String[] foundSTHeaders = { "token","token st","relationship-ST","relationship-token","type","sentence" };
				csvFoundST.writeNext(foundSTHeaders);
				
				for(String[] array : foundSTByToken) {
					csvFoundST.writeNext(array);
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				try { csvSTCounts.close(); } catch(Exception e) { }
				try { csvSTByToken.close(); } catch(Exception e) { }
				try { csvRelCounts.close(); } catch(Exception e) { }
				try { csvRelByToken.close(); } catch(Exception e) { }
				try { csvFoundST.close(); } catch(Exception e) { }
			}
		}
	}
	
	private void logMissing(Map<String, Integer> map, String key) {
		if(writeLogs) {
			Integer count = map.get(key);
			
			if(count == null) {
				map.put(key, 1);
			} else {
				count++;
				map.put(key, count);
			}
		}
	}
	
	private void logMissing2(List<String[]> list, String leftToken, String rightToken, String leftST, String rightST, String sentence, String type) {
		if(writeLogs) {
			if(leftST == null && rightST == null) {
				String[] row = { leftToken, leftToken + "|" + rightToken, type, sentence };
				list.add(row);
			} else {
				String[] row = { leftST + "|" + rightST, leftToken, rightToken, type, sentence };
				list.add(row);
			}
		}
	}
	
	private void logMissingST(String missing, String relationship, String sentence, String type) {
		if(writeLogs) {
			String[] row = { missing, relationship, type, sentence };
			stByToken.add(row);
		}
	}
	
	private void logFound(String token, String tokenST, String relationshipST, String relationshipToken, String type, String sentence) {	
		if(writeLogs) {
			String[] row = { token, tokenST, relationshipST, relationshipToken, type, sentence };
			foundSTByToken.add(row);
		}
	}
	
	private void report(StructuredData structured) {
		try {
			String[] data = new String[Headers.values().length];
			Arrays.fill(data, "");
			
			data[Headers.PATIENT_ID.ordinal()] = structured.patientID;
			data[Headers.VISIT_DATE.ordinal()] = structured.date != null ? sdf.format(structured.date) : "";
			data[Headers.SENTENCE.ordinal()] = structured.sentence;

			for(Multimap<String, MapValue> related : structured.related) {
				processMapEntries(related, "Y", data);
			}

			for(Multimap<String, MapValue> unrelated : structured.unrelated) {
				processMapEntries(unrelated, "N", data);
			}
			
			for(Multimap<String, MapValue> regex : structured.regex) {
				processMapEntries(regex, "N", data);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processMapEntries(Multimap<String, MapValue> map, String relatedToVerb, String[] data) {
		
		if(!map.isEmpty()) {
			data[Headers.RELATED_TO_VERB.ordinal()] = relatedToVerb;
			
			boolean write = true;
			
			for(Map.Entry<String, MapValue> entry : map.entries()) {
				MapValue vals = entry.getValue();
				
				data[Headers.OTHER.ordinal()] = vals.qualifier;
				data[Headers.ABSENCE.ordinal()] = vals.negated ? "Y" : "";
				
			    if(entry.getKey().equalsIgnoreCase("Age")) {
			    	String[] age = vals.value.split("-");
			    	data[Headers.AGE.ordinal()] = age[0];
			    }
			    else if(entry.getKey().equalsIgnoreCase("Sex"))
			    	data[Headers.SEX.ordinal()] = vals.value;
			    else if(entry.getKey().equalsIgnoreCase("Race"))
			    	data[Headers.RACE.ordinal()] = vals.value;
			    else if(entry.getKey().equalsIgnoreCase("Subject"))
			    	data[Headers.SUBJECT.ordinal()] = vals.value;
			    else if(entry.getKey().equalsIgnoreCase("Diagnostic Procedure"))
			    	data[Headers.DIAGNOSTIC_PROCEDURE.ordinal()] = vals.value;
			    else if(entry.getKey().equalsIgnoreCase("Absolute Value"))
					data[Headers.ABSOLUTE_VALUE.ordinal()] = vals.value.trim();
			    else if(entry.getKey().equalsIgnoreCase("General Value"))
			    	data[Headers.GENERAL_VALUE.ordinal()] = vals.value;
			    else if(entry.getKey().equalsIgnoreCase("Clinical Finding"))
			    	data[Headers.CLINICAL_FINDING.ordinal()] = vals.value;
			    else if(entry.getKey().equalsIgnoreCase("Known Event Date"))
			    	data[Headers.KNOWN_EVENT_DATE.ordinal()] = vals.value;
				else if(entry.getKey().equalsIgnoreCase("Therapy"))
			    	data[Headers.THERAPY.ordinal()] = vals.value;
			    else if(entry.getKey().equalsIgnoreCase("Treatment Plan"))
			    	data[Headers.TREATMENT_PLAN.ordinal()] = vals.value;
			    else if(entry.getKey().equalsIgnoreCase("Complications"))
			    	data[Headers.COMPLICATIONS.ordinal()] = vals.value;
			}
	
			Collection<MapValue> coll = map.get("Admin of Drug");
			for(MapValue val : coll) {
				data[Headers.OTHER.ordinal()] = val.qualifier;
				data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
				data[Headers.ADMIN_OF_DRUG.ordinal()] = val.value;
				
				report.writeNext(data);
				write = false;
			}
			
			coll = map.get("Procedure by Method");
			for(MapValue val : coll) {
				data[Headers.OTHER.ordinal()] = val.qualifier;
				data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
				data[Headers.PROCEDURE_BY_METHOD.ordinal()] = val.value;
				
				report.writeNext(data);
				write = false;
			}
			
			coll = map.get("Finding Site");
			for(MapValue val : coll) {
				data[Headers.OTHER.ordinal()] = val.qualifier;
				data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
				data[Headers.FINDING_SITE.ordinal()] = val.value;
				
				report.writeNext(data);
				write = false;
			}
			
			if(write)
		    	report.writeNext(data);
			
			clearHeaderValues(data);
		}
	}
	
	private String parseGleasonValue(String in) {
		String out = "";
		
//		if(in.matches("^\\d\\s*\\+\\s*\\d\\s*=\\s*\\d\\d?$")) { // 4+4=8
//			String[] arr = in.split("=");
//			out = arr[1].trim();
//		} else if(in.matches("^\\d\\s*\\+\\s*\\d\\s*\\d\\d?$")) { // 3+4 1.8%
//			String[] arr = in.split("=");
//			out = arr[1].trim();
//		} else if(in.matches("^\\d\\s*\\(\\s*\\d\\s*\\+\\s*\\d\\)?$")) { // 7(4+3) and 7 (3+4
//			String[] arr = in.split("\\(");
//			out = arr[0].trim();
//		} else if(in.matches("^\\d\\s*\\+\\s*\\d$")) { // 3+3
//			String[] arr = in.split("\\+");
//			int left = Integer.valueOf(arr[0].trim());
//			int right = Integer.valueOf(arr[1].trim());
//			out = String.valueOf(left+right);
//		} else if(in.matches("^\\d\\s*\\d\\s*\\+\\s*\\d")) { // 7 3+4
//			String[] arr = in.split(" ");
//			out = String.valueOf(arr[0].trim());
//		} else {
//			out = in;
//		}
		Matcher matcher = plusRegex.matcher(in);
		if(matcher.find()) {
			String[] arr = matcher.group().split("\\+");
			int left = Integer.valueOf(arr[0].trim());
			int right = Integer.valueOf(arr[1].trim());
			out = String.valueOf(left+right);
		} else {		
			out = in;
		}

		return out;
	}
	
	private void clearHeaderValues(String[] data) {
		for(Headers header : Headers.values()) {
			if(!(header == Headers.PATIENT_ID ||
			     header == Headers.VISIT_DATE || 
			     header == Headers.RELATED_TO_VERB ||
			     header == Headers.SENTENCE)) {
				
				data[header.ordinal()] = "";
			}
		}
	}
	
	private void initReports(String practice, String study) {
		
		String fileId = practice + "_" + study + "_" + new SimpleDateFormat("MM-dd-yyyy-HH:mm").format(new Date());
		
		try {
			//audit_report = new CSVWriter(new FileWriter("audit_report_" + fileId + ".csv"), ',');
			//unknowns_report = new CSVWriter(new FileWriter("unknowns_report_" + fileId + ".csv"), ',');
			report = new CSVWriter(new FileWriter(buildReportPath(practice, study) + "structured_" + fileId + ".csv"), ',');
		} catch(Exception e) {
			e.printStackTrace();
		}
	}	
}
