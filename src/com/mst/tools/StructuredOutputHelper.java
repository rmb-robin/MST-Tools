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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.util.JSON;
import com.mst.model.GenericToken;
import com.mst.model.MapValue;
import com.mst.model.MapValue2;
import com.mst.model.PrepPhraseToken;
import com.mst.model.Sentence;
import com.mst.model.SentenceMetadata;
import com.mst.model.StructuredData;
import com.mst.model.StructuredData2;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.VerbPhraseToken;
import com.mst.model.WordToken;
import com.mst.model.discreet.Meds;
import com.mst.model.discreet.Patient;
import com.mst.util.Constants;
import com.mst.util.GsonFactory;
import com.mst.util.Props;
import com.opencsv.CSVWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class StructuredOutputHelper {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	//private Map<String, String[]> constructors = new HashMap<String, String[]>();
	
	private CSVWriter report;

	private Map<String, Integer> stCounts = new HashMap<>(); // missing semantic type counts report
	private Map<String, Integer> relCounts = new HashMap<>(); // missing relationship counts report
	private Set<String> relByToken = new HashSet<>(); // missing relationships by token
	private Set<String> stByToken = new HashSet<>(); // missing ST by token
	private Set<String> unprocessedSentences = new HashSet<>(); // sentences for which we found no structured data
	private Set<String> foundSTByToken = new HashSet<>();
	
	private Pattern plusRegex = Pattern.compile("\\d+\\s*\\+\\s*\\d+");
	
	private Gson gson = GsonFactory.build();
	private DateFormat sdf = new SimpleDateFormat("M/d/yyyy");
	
	private boolean writeLogs = false;
	
	private final String ABSENCE_QUALIFIER = "Absence";
	
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
		DEBUG,
		VERB,
		PROCEDURE_BY_METHOD,
		FINDING_SITE,
		ABSOLUTE_VALUE,
		GENERAL_VALUE,
		CLINICAL_FINDING,
		THERAPY,
		COMPLICATIONS,
		TREATMENT_PLAN,
		ABSENCE,
		NEGATION_SRC,
		RELATED_TO_VERB,
		VERB_PHRASE_COUNT,
		SUBJ_SUBJC_EQUAL,
		SENTENCE;
	};
	
	public StructuredOutputHelper(boolean writeLogs) {
		
		System.out.println("StructuredOutputHelper constructor fired... writeLogs: " + writeLogs);
		
		//jedis = Constants.RedisDB.INSTANCE.getInstance();
		
		this.writeLogs = writeLogs;
/*			
//		constructors.put(""bevbpa|number", new String[]{"Absolute Value",""});
//		constructors.put(""bevbpr|number", new String[]{"Absolute Value","Current"});
		constructors.put("for|number", new String[]{"Absolute Value",""});
		constructors.put("from|number", new String[]{"Absolute Value",""});
		constructors.put("in|number", new String[]{"Absolute Value",""});
		constructors.put("of|number", new String[]{"Absolute Value",""});
		constructors.put("on|number", new String[]{"Absolute Value",""});
		constructors.put("to|number", new String[]{"Absolute Value",""});
		constructors.put("with|number", new String[]{"Absolute Value",""});
		constructors.put(""avb-added|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-administered|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-began|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-cancelled|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put(""avb-chose|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""avb-complains|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""avb-considered|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""avb-continued|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-continues|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-decrease|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-delay|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-discussed|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""avb-elected|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""avb-enrolled|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""avb-finish|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put(""avb-given|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-increase|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-obtained|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-presnt|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-refused|drugpr", new String[]{"Admin of Drug","Refused"});
		constructors.put(""avb-remained|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-returns|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-scheduled|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-show|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""avb-signed|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""avb-start|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-stop|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put(""avb-stopped|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put(""avb-suggest|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""avb-takes|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-tolerated|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-treated|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""avb-undwen|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put(""bevbpa|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""bevbpa-failing|drugpr", new String[]{"Admin of Drug","Failing"});
		constructors.put(""bevbpa-start|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""bevbpr|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put(""bevbpr-approved|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""bevbpr-chosen|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""bevbpr-consider|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""bevbpr-continue|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""bevbpr-discussed|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""bevbpr-do|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""bevbpr-failed|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""bevbpr-going|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""bevbpr-refused|drugpr", new String[]{"Admin of Drug","Refused"});
		constructors.put(""bevbpr-seen|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""bevbpr-started|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""bevbpr-stopping|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put(""bevbpr-tolerate|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""bevbpr-worsened|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""bevbpa-beenapproved|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""bevbpa-beenfailing|drugpr", new String[]{"Admin of Drug","Failing"});
		constructors.put(""bevbpa-chosen|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""bevbpa-considered|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""bevbpa-continued|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""bevbpa-decided|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""bevbpa-discussed|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""bevbpa-refused|drugpr", new String[]{"Admin of Drug","Refused"});
		constructors.put(""bevbpa-returned|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""bevbpa-started|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""bevbpa-stopped|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put(""bevbpa-treated|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""bevbpa-worked|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""bevbpa-worsened|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|avb"-added", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-administered", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-began", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-cancelled", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|avb"-chose", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb"-complains", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|avb"-considered", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb"-continued", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-continues", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-decrease", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-delay", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-discussed", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb"-elected", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb"-enrolled", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb"-finish", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|avb"-given", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-increase", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-obtained", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-presnt", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-refused", new String[]{"Admin of Drug","Refused"});
		constructors.put("drugpr|avb"-remained", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-returns", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-scheduled", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-show", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|avb"-signed", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|avb"-start", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-stop", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|avb"-stopped", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|avb"-suggest", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|avb"-takes", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-tolerated", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-treated", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|avb"-undwen", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|bevb"pa", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevb"pa", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevb"pa-failing", new String[]{"Admin of Drug","Failing"});
		constructors.put("drugpr|bevb"pa-start", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevb"pr", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|bevb"pr-approved", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevb"pr-chosen", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevb"pr-consider", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevb"pr-continue", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevb"pr-discussed", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevb"pr-do", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevb"pr-failed", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevb"pr-going", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevb"pr-refused", new String[]{"Admin of Drug","Refused"});
		constructors.put("drugpr|bevb"pr-seen", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevb"pr-started", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevb"pr-stopping", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|bevb"pr-tolerate", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevb"pr-worsened", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevb"pa-beenapproved", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevb"pa-beenfailing", new String[]{"Admin of Drug","Failing"});
		constructors.put("drugpr|bevb"pa-chosen", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevb"pa-considered", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevb"pa-continued", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevb"pa-decided", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevb"pa-discussed", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|bevb"pa-refused", new String[]{"Admin of Drug","Refused"});
		constructors.put("drugpr|bevb"pa-returned", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevb"pa-started", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|bevb"pa-stopped", new String[]{"Admin of Drug","Past"});
		constructors.put("drugpr|bevb"pa-treated", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevb"pa-worked", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|bevb"pa-worsened", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|drugdrly", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|mvb"", new String[]{"Admin of Drug",""});
		constructors.put("drugpr|mvb"-beevaluated", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|mvb"-bestarted", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|mvb"-betreated", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|mvb"-continue", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|mvb"-eval", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|mvb"-recomm", new String[]{"Admin of Drug","Future"});
		constructors.put("drugpr|mvb"-start", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|mvb"-undergo", new String[]{"Admin of Drug","Current"});
		constructors.put("drugpr|ptconsul", new String[]{"Admin of Drug","Future"});
		constructors.put("for|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("from|drugpr", new String[]{"Admin of Drug",""});
//		constructors.put(""bevbpr|drugpr", new String[]{"Admin of Drug","Past"});
		constructors.put("in|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""mvb|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""mvb|drugpr", new String[]{"Admin of Drug",""});
		constructors.put(""mvb-beapproved|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""mvb-beevaluated|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""mvb-bestarted|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""mvb-betreated|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""mvb-continue|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""mvb-eval|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""mvb-recomm|drugpr", new String[]{"Admin of Drug","Future"});
		constructors.put(""mvb-start|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put(""mvb-undergo|drugpr", new String[]{"Admin of Drug","Current"});
		constructors.put("of|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("off|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("on|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("since|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("to|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("with|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("after|drugpr", new String[]{"Admin of Drug",""});
		constructors.put("age|race", new String[]{"Age",""});
		constructors.put("age|sociid", new String[]{"Age",""});
		constructors.put(""avb-added|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-added|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-added|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-added|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-added|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-added|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-administered|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-administered|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-administered|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-administered|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-administered|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-administered|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-began|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-began|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-began|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-began|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-began|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-began|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-cancelled|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-cancelled|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-cancelled|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-cancelled|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-cancelled|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-cancelled|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-chose|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-chose|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-chose|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-chose|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-chose|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-chose|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-complains|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-complains|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-complains|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-complains|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-complains|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-complains|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-considered|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-considered|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-considered|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-considered|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-considered|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-considered|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continued|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continued|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continued|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continued|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continued|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continued|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continues|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continues|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continues|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continues|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continues|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continues|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-decrease|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-decrease|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-decrease|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-decrease|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-decrease|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-decrease|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-delay|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-delay|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-delay|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-delay|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-delay|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-delay|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-denies|dysn", new String[]{"Clinical Finding","Absence"});
		constructors.put(""avb-denies|sympto", new String[]{"Clinical Finding","Absence"});
		constructors.put(""avb-discussed|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-discussed|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-discussed|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-discussed|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-discussed|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-discussed|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-elected|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-elected|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-elected|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-elected|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-elected|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-elected|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-enrolled|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-enrolled|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-enrolled|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-enrolled|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-enrolled|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-enrolled|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-finish|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-finish|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-finish|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-finish|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-finish|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-finish|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-given|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-given|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-given|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-given|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-given|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-given|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-increase|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-increase|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-increase|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-increase|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-increase|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-increase|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-obtained|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-obtained|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-obtained|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-obtained|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-obtained|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-obtained|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-presnt|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-presnt|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-presnt|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-presnt|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-presnt|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-presnt|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-refused|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-refused|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-refused|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-refused|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-refused|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-refused|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-remained|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-remained|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-remained|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-remained|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-remained|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-remained|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-returns|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-returns|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-returns|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-returns|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-returns|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-returns|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-scheduled|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-scheduled|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-scheduled|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-scheduled|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-scheduled|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-scheduled|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-show|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-show|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-show|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-show|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-show|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-show|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-signed|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-signed|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-signed|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-signed|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-signed|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-signed|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-start|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-start|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-start|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-start|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-start|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-start|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stop|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stop|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stop|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stop|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stop|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stop|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stopped|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stopped|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stopped|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stopped|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stopped|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-stopped|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-suggest|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-suggest|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-suggest|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-suggest|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-suggest|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-suggest|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-takes|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-takes|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-takes|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-takes|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-takes|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-takes|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-tolerated|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-tolerated|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-tolerated|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-tolerated|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-tolerated|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-tolerated|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-treated|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-treated|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-treated|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-treated|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-treated|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-treated|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-undwen|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-undwen|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-undwen|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-undwen|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-undwen|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-undwen|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa|sympto", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-failing|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-failing|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-failing|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-failing|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-failing|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-failing|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-start|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-start|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-start|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-start|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-start|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-start|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr|sympto", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-approved|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-approved|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-approved|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-approved|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-approved|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-approved|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-chosen|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-chosen|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-chosen|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-chosen|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-chosen|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-chosen|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-consider|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-consider|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-consider|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-consider|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-consider|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-consider|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-continue|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-continue|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-continue|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-continue|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-continue|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-continue|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-discussed|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-discussed|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-discussed|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-discussed|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-discussed|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-discussed|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-do|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-do|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-do|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-do|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-do|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-do|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-failed|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-failed|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-failed|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-failed|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-failed|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-failed|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-going|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-going|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-going|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-going|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-going|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-going|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-refused|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-refused|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-refused|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-refused|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-refused|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-refused|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-seen|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-seen|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-seen|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-seen|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-seen|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-seen|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-started|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-started|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-started|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-started|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-started|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-started|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-stopping|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-stopping|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-stopping|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-stopping|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-stopping|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-stopping|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-tolerate|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-tolerate|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-tolerate|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-tolerate|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-tolerate|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-tolerate|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-worsened|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-worsened|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-worsened|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-worsened|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-worsened|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr-worsened|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenapproved|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenapproved|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenapproved|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenapproved|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenapproved|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenapproved|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenfailing|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenfailing|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenfailing|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenfailing|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenfailing|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-beenfailing|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-chosen|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-chosen|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-chosen|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-chosen|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-chosen|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-chosen|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-considered|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-considered|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-considered|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-considered|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-considered|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-considered|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-continued|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-continued|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-continued|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-continued|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-continued|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-continued|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-decided|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-decided|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-decided|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-decided|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-decided|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-decided|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-discussed|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-discussed|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-discussed|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-discussed|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-discussed|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-discussed|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-refused|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-refused|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-refused|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-refused|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-refused|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-refused|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-returned|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-returned|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-returned|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-returned|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-returned|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-returned|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-started|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-started|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-started|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-started|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-started|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-started|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-stopped|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-stopped|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-stopped|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-stopped|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-stopped|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-stopped|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-treated|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-treated|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-treated|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-treated|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-treated|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-treated|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worked|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worked|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worked|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worked|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worked|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worked|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worsened|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worsened|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worsened|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worsened|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worsened|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-worsened|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("dysn|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("dysn|bevb"pr", new String[]{"Clinical Finding",""});
		constructors.put("dysn|mvb"", new String[]{"Clinical Finding",""});
		constructors.put("for|dysn", new String[]{"Clinical Finding",""});
		constructors.put("for|neop", new String[]{"Clinical Finding",""});
		constructors.put("for|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("for|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("for|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("for|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("for|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("for|sympto", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr|dysn", new String[]{"Clinical Finding","Past"});
		constructors.put(""mvb|dysn", new String[]{"Clinical Finding",""});
		constructors.put(""mvb|neop", new String[]{"Clinical Finding",""});
		constructors.put(""mvb|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""mvb|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""mvb|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""mvb|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""mvb|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""mvb|sympto", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beapproved|neop", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beapproved|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beapproved|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beapproved|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beapproved|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beapproved|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beevaluated|neop", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beevaluated|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beevaluated|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beevaluated|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beevaluated|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-beevaluated|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-bestarted|neop", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-bestarted|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-bestarted|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-bestarted|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-bestarted|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-bestarted|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-betreated|neop", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-betreated|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-betreated|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-betreated|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-betreated|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-betreated|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-continue|neop", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-continue|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-continue|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-continue|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-continue|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-continue|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-eval|neop", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-eval|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-eval|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-eval|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-eval|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-eval|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-recomm|neop", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-recomm|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-recomm|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-recomm|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-recomm|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-recomm|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-start|neop", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-start|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-start|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-start|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-start|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-start|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-undergo|neop", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-undergo|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-undergo|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-undergo|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-undergo|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""mvb-undergo|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("negata|bone pain", new String[]{"Clinical Finding","No bone pain"});
		constructors.put("negata|dysn", new String[]{"Clinical Finding","Absence"});
		constructors.put("negata|pain", new String[]{"Clinical Finding","No pain"});
		constructors.put("neop|avb"-added", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-began", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-given", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-show", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb"", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb"-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb"-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb"-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb"-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb"-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb"-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb"-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop|mvb"-undergo", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-added", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-began", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-given", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-show", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb"", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb"-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb"-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb"-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb"-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb"-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb"-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb"-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|mvb"-undergo", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-added", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-began", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-given", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-show", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb"", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb"-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb"-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb"-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb"-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb"-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb"-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb"-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|mvb"-undergo", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-added", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-began", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-given", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-show", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb"", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb"-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb"-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb"-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb"-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb"-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb"-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb"-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|mvb"-undergo", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-added", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-began", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-given", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-show", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb"", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb"-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb"-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb"-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb"-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb"-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb"-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb"-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|mvb"-undergo", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-added", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-administered", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-began", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-cancelled", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-chose", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-complains", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-continues", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-decrease", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-delay", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-elected", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-enrolled", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-finish", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-given", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-increase", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-obtained", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-presnt", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-remained", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-returns", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-scheduled", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-show", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-signed", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-stop", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-suggest", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-takes", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-tolerated", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-undwen", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-failing", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-approved", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-consider", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-do", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-failed", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-going", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-seen", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-stopping", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-tolerate", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-beenapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-beenfailing", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-chosen", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-considered", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-continued", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-decided", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-discussed", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-refused", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-returned", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-started", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-stopped", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-treated", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-worked", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa-worsened", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb"", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb"-beapproved", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb"-beevaluated", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb"-bestarted", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb"-betreated", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb"-continue", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb"-eval", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb"-recomm", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb"-start", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|mvb"-undergo", new String[]{"Clinical Finding",""});
		constructors.put("of|dysn", new String[]{"Clinical Finding",""});
		constructors.put("of|neop", new String[]{"Clinical Finding",""});
		constructors.put("of|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("of|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("of|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("of|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("of|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("of|sympto", new String[]{"Clinical Finding",""});
		constructors.put("pathology|tnmstage", new String[]{"Clinical Finding",""});
		constructors.put("sympto|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("sympto|bevb"pr", new String[]{"Clinical Finding",""});
		constructors.put("sympto|mvb"", new String[]{"Clinical Finding",""});
		constructors.put("with|neop", new String[]{"Clinical Finding",""});
		constructors.put("with|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("with|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("with|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("with|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("with|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put("with|sympto", new String[]{"Clinical Finding",""});
		constructors.put(""avb-undwen|diap", new String[]{"Diagnostic Procedure","Past"});
		constructors.put("diap|avb"-decrease", new String[]{"Diagnostic Procedure","Decreased"});
		constructors.put("diap|avb"-increase", new String[]{"Diagnostic Procedure","Increased"});
		constructors.put("diap|bevb"pa", new String[]{"Diagnostic Procedure",""});
		constructors.put("diap|bevb"pa", new String[]{"Diagnostic Procedure",""});
		constructors.put("diap|bevb"pr", new String[]{"Diagnostic Procedure",""});
		constructors.put("diap|mvb"", new String[]{"Diagnostic Procedure",""});
		constructors.put("for|diap", new String[]{"Diagnostic Procedure",""});
//		constructors.put(""bevbpr|diap", new String[]{"Diagnostic Procedure","Past"});
		constructors.put("in|diap", new String[]{"Diagnostic Procedure",""});
//		constructors.put(""mvb|diap", new String[]{"Diagnostic Procedure",""});
		constructors.put("of|diap", new String[]{"Diagnostic Procedure",""});
		constructors.put("with|diap", new String[]{"Diagnostic Procedure",""});
		constructors.put("in|bpoc", new String[]{"Finding Site",""});
		constructors.put("of|bpoc", new String[]{"Finding Site",""});
		constructors.put("bpoc|sympto", new String[]{"Finding Site",""});
		constructors.put("from|date", new String[]{"Known Event Date",""});
		constructors.put("in|date", new String[]{"Known Event Date",""});
		constructors.put("in|tempor", new String[]{"Known Event Date",""});
//		constructors.put(""mvb|tempor", new String[]{"Known Event Date",""});
		constructors.put("of|date", new String[]{"Known Event Date",""});
		constructors.put("on|date", new String[]{"Known Event Date",""});
		constructors.put("tempor|avb"-presnt", new String[]{"Known Event Date",""});
		constructors.put("tempor|bevb"pa", new String[]{"Known Event Date",""});
		constructors.put("tempor|bevb"pa", new String[]{"Known Event Date",""});
		constructors.put("tempor|bevb"pr", new String[]{"Known Event Date",""});
		constructors.put("tempor|mvb"", new String[]{"Known Event Date",""});
		constructors.put("electronically|avb"-signed", new String[]{"Procedural",""});
		constructors.put(""avb-finish|prbymeth", new String[]{"Procedure by Method","Past"});
		constructors.put(""avb-undwen|prbymeth", new String[]{"Procedure by Method","Past"});
		constructors.put("diap|avb"-show", new String[]{"Procedure by Method",""});
		constructors.put(""bevbpr|prbymeth", new String[]{"Procedure by Method","Past"});
		constructors.put(""mvb|prbymeth", new String[]{"Procedure by Method",""});
		constructors.put("of|prbymeth", new String[]{"Procedure by Method",""});
		constructors.put("on|prbymeth", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|avb"-chose", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|avb"-show", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|bevb"pa", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|bevb"pa", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|bevb"pr", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|bevb"pa-start", new String[]{"Procedure by Method",""});
		constructors.put("prbymeth|mvb"", new String[]{"Procedure by Method",""});
		constructors.put("with|prbymeth", new String[]{"Procedure by Method",""});
		constructors.put("race|sociid", new String[]{"Race",""});
		constructors.put(""bevbpr|sociid", new String[]{"Sex","Social History"});
		constructors.put(""avb-presnt|", new String[]{"Subject",""});
		constructors.put(""bevbpa-restart|", new String[]{"Subject",""});
		constructors.put(""bevbpa-start|", new String[]{"Subject",""});
		constructors.put(""bevbpr|patien", new String[]{"Subject",""});
		constructors.put("for|patien", new String[]{"Subject",""});
		constructors.put(""mvb|patien", new String[]{"Subject",""});
		constructors.put(""mvb|propn", new String[]{"Subject",""});
		constructors.put(""mvb|sociid", new String[]{"Subject",""});
		constructors.put("patien|avb"-chose", new String[]{"Subject",""});
		constructors.put("patien|avb"-continues", new String[]{"Subject",""});
		constructors.put("patien|avb"-denies", new String[]{"Subject",""});
		constructors.put("patien|avb"-finish", new String[]{"Subject",""});
		constructors.put("patien|avb"-presnt", new String[]{"Subject",""});
		constructors.put("patien|avb"-start", new String[]{"Subject",""});
		constructors.put("patien|avb"-takes", new String[]{"Subject",""});
		constructors.put("patien|avb"-tolerated", new String[]{"Subject",""});
		constructors.put("patien|avb"-undwen", new String[]{"Subject",""});
		constructors.put("patien|avd-denies", new String[]{"Subject",""});
		constructors.put("patien|bevb"pa", new String[]{"Subject",""});
		constructors.put("patien|bevb"pa", new String[]{"Subject",""});
		constructors.put("patien|bevb"pa-restart", new String[]{"Subject","Current"});
		constructors.put("patien|bevb"pa-start", new String[]{"Subject","Current"});
		constructors.put("patien|bevb"pr", new String[]{"Subject",""});
		constructors.put("patien|bevb"pa-give", new String[]{"Subject",""});
		constructors.put("patien|bevb"pa-start", new String[]{"Subject",""});
		constructors.put("patien|bevb"pa-treat", new String[]{"Subject",""});
		constructors.put("patien|bevb"pr", new String[]{"Subject",""});
		constructors.put("patien|mvb"", new String[]{"Subject",""});
		constructors.put("patien|pvb-opted", new String[]{"Subject",""});
		constructors.put("propn|avb"-finish", new String[]{"Subject",""});
		constructors.put("propn|avb"-show", new String[]{"Subject",""});
		constructors.put("propn|avb"-start", new String[]{"Subject",""});
		constructors.put("propn|avb"-stop", new String[]{"Subject",""});
		constructors.put("propn|bevb"pa", new String[]{"Subject",""});
		constructors.put("propn|bevb"pa", new String[]{"Subject",""});
		constructors.put("propn|bevb"pa-start", new String[]{"Subject",""});
		constructors.put("propn|bevb"pr", new String[]{"Subject",""});
		constructors.put("propn|bevb"pa-give", new String[]{"Subject",""});
		constructors.put("propn|mvb"", new String[]{"Subject",""});
		constructors.put("provid|avb"-discussed", new String[]{"Subject",""});
		constructors.put("provid|avb"-stop", new String[]{"Subject",""});
		constructors.put("provid|mvb"-recomm", new String[]{"Subject",""});
		constructors.put("sociid|bevb"pa", new String[]{"Subject",""});
		constructors.put("sociid|bevb"pa", new String[]{"Subject",""});
		constructors.put("sociid|bevb"pr", new String[]{"Subject",""});
		constructors.put("sociid|mvb"", new String[]{"Subject",""});
		constructors.put("for|waiting", new String[]{"Treatment Plan",""});
		constructors.put("in|remiss", new String[]{"Treatment Plan",""});
		constructors.put("in|ptconsul", new String[]{"Treatment Plan",""});
		constructors.put("number|tempor", new String[]{"Treatment Plan",""});
		constructors.put("on|waiting", new String[]{"Treatment Plan",""});
		constructors.put("number|bevb"pa", new String[]{"Absolute Value",""});
		constructors.put(""avb-decrease|qlco", new String[]{"General Value",""});
		constructors.put(""avb-increase|qlco", new String[]{"General Value",""});
		constructors.put(""bevbpa|qlco", new String[]{"General Value",""});
		constructors.put(""bevbpr|qlco", new String[]{"General Value",""});
		constructors.put("neop|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pa", new String[]{"Clinical Finding",""});
		constructors.put("neop|bevb"pr", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|bevb"pr", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|bevb"pr", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|bevb"pr", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bevb"pr", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|bevb"pr", new String[]{"Clinical Finding",""});
		constructors.put("presence|with", new String[]{"","Presence"});
		constructors.put("suspicious|for", new String[]{"","Suspicious"});
		constructors.put("change-val|of", new String[]{"","Progression"});
		constructors.put("positive|for", new String[]{"","Presence"});
		constructors.put("discussion|of", new String[]{"","Future"});
		constructors.put("initiation|of", new String[]{"","Start"});
		constructors.put("history|of", new String[]{"Clinical Finding","History"});
		constructors.put("neop-mets|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|bpoc", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|dysn", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|fnd", new String[]{"Clinical Finding",""});
		constructors.put("neop|avb"-found", new String[]{"Clinical Finding",""});
		constructors.put("neop-abn|avb"-found", new String[]{"Clinical Finding",""});
		constructors.put("neop-can|avb"-found", new String[]{"Clinical Finding",""});
		constructors.put("neop-les|avb"-found", new String[]{"Clinical Finding",""});
		constructors.put("neop-mets|avb"-found", new String[]{"Clinical Finding",""});
		constructors.put("neop-tum|avb"-found", new String[]{"Clinical Finding",""});
		constructors.put(""avb-developed|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-developed|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-developed|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-developed|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-developed|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-developed|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-confirm|neop", new String[]{"Clinical Finding",""});
		constructors.put(""avb-confirm|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""avb-confirm|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""avb-confirm|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""avb-confirm|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""avb-confirm|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-known|neop", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-known|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-known|neop-can", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-known|neop-les", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-known|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa-known|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-developed|sympto", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr|symptomatic", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpr|asymptomatic", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa|symptomatic", new String[]{"Clinical Finding",""});
		constructors.put(""bevbpa|asymptomatic", new String[]{"Clinical Finding",""});
		constructors.put("did-show|neop", new String[]{"Clinical Finding",""});
		constructors.put("did-show|neop-abn", new String[]{"Clinical Finding",""});
		constructors.put("did-show|neop-can", new String[]{"Clinical Finding",""});
		constructors.put("did-show|neop-les", new String[]{"Clinical Finding",""});
		constructors.put("did-show|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("did-show|neop-tum", new String[]{"Clinical Finding",""});
		constructors.put(""avb-continues|on", new String[]{"","Currently On"});
		constructors.put(""avb-increase|to", new String[]{"","Change of therapy"});
		constructors.put(""avb-presnt|for", new String[]{"","Currently On"});
		constructors.put(""avb-start|on", new String[]{"","Currently On"});
		constructors.put(""avb-start|on", new String[]{"","Currently On"});
		constructors.put(""avb-treated|with", new String[]{"","Received therapy"});
		constructors.put(""bevbpr|off", new String[]{"","Currently Off"});
		constructors.put(""bevbpr|on", new String[]{"","Currently On"});
		constructors.put("diagre|after", new String[]{"","Improved"});
		constructors.put("diagre|on", new String[]{"","Improved"});
		constructors.put("diagre|since", new String[]{"","Improved"});
		constructors.put("diagre|with", new String[]{"","Improved"});
		constructors.put("diagre|with", new String[]{"","Not improved"});
		constructors.put("drugpr|to", new String[]{"","Change of therapy"});
		constructors.put("drugdrly|of", new String[]{"","Currently On"});
		constructors.put("drugdrly|with", new String[]{"","Received therapy"});
		constructors.put("number|of", new String[]{"","Dosage"});
		constructors.put("off|of", new String[]{"","Currently Off"});
		constructors.put("patien|on", new String[]{"","Currently On"});
		constructors.put("patien|on", new String[]{"","Currently On"});
		constructors.put("qlco|on", new String[]{"","Currently On"});
		constructors.put("qlco|with", new String[]{"","Currently On"});
		constructors.put("sympto|with", new String[]{"","Effects"});
		constructors.put("also|on", new String[]{"","Multiple Therapies"});
		constructors.put("alternatives|to", new String[]{"","Discussion"});
		constructors.put("approval|for", new String[]{"","Approval/Cost"});
		constructors.put("benefit|from", new String[]{"","Benefits"});
		constructors.put("benefits|of", new String[]{"","Benefits"});
		constructors.put("candidate|for", new String[]{"","Candidate for"});
		constructors.put("combination|of", new String[]{"","Multiple Therapies"});
		constructors.put("consideration|of", new String[]{"","Discussion"});
		constructors.put("continue|on", new String[]{"","Continue"});
		constructors.put("continue|with", new String[]{"","Continue"});
		constructors.put("cost|of", new String[]{"","Approval/Cost"});
		constructors.put("course|of", new String[]{"","Currently On"});
		constructors.put("currently|off", new String[]{"","Currently Off"});
		constructors.put("currently|on", new String[]{"","Currently On"});
		constructors.put("dose|of", new String[]{"","Currently On"});
		constructors.put("dose|to", new String[]{"","Currently On"});
		constructors.put("due|for", new String[]{"","Due For"});
		constructors.put("due|to", new String[]{"","Effects"});
		constructors.put("effects|of", new String[]{"","Effects"});
		constructors.put("good|with", new String[]{"","Improved"});
		constructors.put("happy|with", new String[]{"","Improved"});
		constructors.put("improved|after", new String[]{"","Improved"});
		constructors.put("improved|with", new String[]{"","Improved"});
		constructors.put("improvement|from", new String[]{"","Improved"});
		constructors.put("improvement|since", new String[]{"","Improved"});
		constructors.put("improvement|with", new String[]{"","Improved"});
		constructors.put("instead|of", new String[]{"","Change of therapy"});
		constructors.put("interested|in", new String[]{"","Discussion"});
		constructors.put("managed|with", new String[]{"","Currently On"});
		constructors.put("more|of", new String[]{"","Currently On"});
		constructors.put("not|on", new String[]{"","Currently Off"});
		constructors.put("now|off", new String[]{"","Currently Off"});
		constructors.put("now|on", new String[]{"","Currently On"});
		constructors.put("option|of", new String[]{"","Option"});
		constructors.put("literature|on", new String[]{"","Literature"});
		constructors.put("pamphlet|on", new String[]{"","Literature"});
		constructors.put("pay|for", new String[]{"","Approval/Cost"});
		constructors.put("placed|on", new String[]{"","Currently On"});
		constructors.put("pleased|with", new String[]{"","Improved"});
		constructors.put("prescription|for", new String[]{"","Currently On"});
		constructors.put("problems|with", new String[]{"","Effects"});
		constructors.put("proceed|with", new String[]{"","Intent"});
		constructors.put("progress|with", new String[]{"","Improved"});
		constructors.put("put|on", new String[]{"","Currently On"});
		constructors.put("reaction|to", new String[]{"","Effects"});
		constructors.put("ready|for", new String[]{"","Intent"});
		constructors.put("repeat|of", new String[]{"","Currently On"});
		constructors.put("resolved|with", new String[]{"","Improved"});
		constructors.put("result|from", new String[]{"","Result"});
		constructors.put("results|of", new String[]{"","Result"});
		constructors.put("samples|of", new String[]{"","Samples"});
		constructors.put("satisfied|with", new String[]{"","Improved"});
		constructors.put("stay|off", new String[]{"","Currently Off"});
		constructors.put("stay|on", new String[]{"","Currently On"});
		constructors.put("still|on", new String[]{"","Currently On"});
		constructors.put("success|with", new String[]{"","Improved"});
		constructors.put("supply|of", new String[]{"","Currently On"});
		constructors.put("switch|from", new String[]{"","Change of therapy"});
		constructors.put("switch|to", new String[]{"","Change of therapy"});
		constructors.put("switched|to", new String[]{"","Change of therapy"});
		constructors.put("trial|of", new String[]{"","Sample"});
		constructors.put("tried|on", new String[]{"","Received therapy"});
		constructors.put("was|on", new String[]{"","Past"});
		constructors.put("wean|off", new String[]{"","Stopping"});
		constructors.put("well|with", new String[]{"","Improved"});
		constructors.put("went|with", new String[]{"","Intention"});

		// *********
		
		constructors.put("|sympto", new String[]{"Clinical Finding",""});
		constructors.put("|neop-mets", new String[]{"Clinical Finding",""});
		constructors.put("|diap", new String[]{"Diagnostic Procedure",""});
		constructors.put("|qlco", new String[]{"General Value",""});
		constructors.put("|bpoc", new String[]{"Finding Site",""});
//		constructors.put(""mvb-start|on", new String[]{"","Future"});
		
*/
	}
	
	private StructuredData buildStructuredOutput(Sentence sentence, boolean writeToMongo) {
		
		StructuredData structured = new StructuredData();
		
		structured.patientId = sentence.getId();
		structured.practice = sentence.getPractice();
		structured.study = sentence.getStudy();
		structured.date = sentence.getProcedureDate();
		structured.sentence = sentence.getFullSentence();
		
		List<Integer> processedPrepPhrases = new ArrayList<Integer>();
		List<Integer> processedNounPhrases = new ArrayList<Integer>();
		
		try {
			SentenceMetadata metadata = sentence.getMetadata();
			ArrayList<WordToken> words = sentence.getWordList();
		
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
						String verbST = null;
						
						// verbST will be either null, that of the only token, that of the entire phrase (if present), or that of
						// the final token (if entire phrase has no ST)
						if(verbPhrase.getVerbs().size() == 1) {
							verbST = words.get(verbPhrase.getVerbs().get(0).getPosition()).getSemanticType();
						} else {
							verbST = verbPhrase.getSemanticType();
							
							if(verbST == null)
								verbST = words.get(verbPhrase.getVerbs().get(verbPhrase.getVerbs().size()-1).getPosition()).getSemanticType();
						}
						
						if(verbST != null) {
							if(subjST != null) {
								String[] attribute = getConstructor(subjST + "|" + verbST);
								if(attribute != null) {
									related.put(attribute[0], new MapValue(verbPhrase.getSubj().getToken(), verbPhrase.isPhraseNegated() ? ABSENCE_QUALIFIER : attribute[1], subjST + "|" + verbST, verbPhrase.isPhraseNegated()));
									logFound(verbPhrase.getSubj().getToken(), subjST, subjST+"|"+verbST, verbPhrase.getSubj().getToken()+"|"+verbPhrase.getVerbString(), "VP", sentence.getFullSentence());
								} else {
									// write to audit report (i.e. no constructor)
									logMissing(relCounts, subjST + "|" + verbST);
									logMissing2(relByToken, verbPhrase.getSubj().getToken(), verbPhrase.getVerbString(), subjST, verbST, sentence.getFullSentence(), "VP");
								}
							} else {
								if(verbPhrase.getSubj() != null) {
									// subj exists, but has no ST
									logMissing(stCounts, verbPhrase.getSubj().getToken());
									logMissingST(verbPhrase.getSubj().getToken(), verbPhrase.getSubj().getToken()+"|"+verbPhrase.getVerbString(), sentence.getFullSentence(), "VP");
								}
							}
							
							// 1b) query constructors by vb/subjc(s)
							for(VerbPhraseToken subjc : verbPhrase.getSubjC()) {
								String subjcST = words.get(subjc.getPosition()).getSemanticType();
							
								if(subjcST != null) {  // TODO possibly defer if SUBJC is within a noun phrase to avoid double-reporting
									String[] attribute = getConstructor(verbST + "|" + subjcST);
									
									if(attribute != null) {
										related.put(attribute[0], new MapValue(subjc.getToken(), verbPhrase.isPhraseNegated() ? ABSENCE_QUALIFIER : attribute[1], verbST + "|" + subjcST, verbPhrase.isPhraseNegated()));
										logFound(subjc.getToken(), subjcST, verbST+"|"+subjcST, verbPhrase.getVerbString()+"|"+subjc.getToken(),"VP", sentence.getFullSentence());
									} else {
										logMissing(relCounts, subjcST + "|" + verbST);
										logMissing2(relByToken, verbPhrase.getVerbString(), subjc.getToken(), verbST, subjcST, sentence.getFullSentence(), "VP");
									}
								} else {
									logMissing(stCounts, subjc.getToken());
									logMissingST(subjc.getToken(), verbPhrase.getVerbString()+"|"+subjc.getToken(), sentence.getFullSentence(), "VP");
								}
							}
							
						} else {
							String verb = (verbPhrase.getVerbs().size() == 1) ? words.get(verbPhrase.getVerbs().get(0).getPosition()).getToken() : verbPhrase.getVerbString();
							logMissing(stCounts, verb);
							logMissingST(verbPhrase.getVerbString(), "", sentence.getFullSentence(), "VP");
						}
						
						/* changed 6/17/15 - pulled this out of the above "if" that required the subj/vb/subjc to have an ST to process related PPs */
						// process prep phrases related to subj
						if(verbPhrase.getSubj() != null) {
							for(int ppIdx : verbPhrase.getSubj().getPrepPhrasesIdx()) {
								// TODO apply negation to verb phrase as a whole and pass to processPrepPhrase()
								processPrepPhrase(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), null);
							}
						}
				
						// process noun phrases related to subj
						if(verbPhrase.getSubj() != null && verbPhrase.getSubj().getNounPhraseIdx() > -1) {
							processNounPhrase(words, metadata, related, processedNounPhrases, verbPhrase.getSubj().getNounPhraseIdx(), sentence.getFullSentence(), null, false);
						}
						
						// process prep phrases related to (final) verb (of phrase)
						for(int ppIdx : verbPhrase.getVerbs().get(verbPhrase.getVerbs().size()-1).getPrepPhrasesIdx()) {
							processPrepPhrase(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), verbST);
						}
						
						for(VerbPhraseToken subjc : verbPhrase.getSubjC()) {
							// process prep phrases related to subjc
							for(int ppIdx : subjc.getPrepPhrasesIdx()) {
								processPrepPhrase(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), verbST);
							}
							
							// process noun phrases related to subjc
							if(subjc.getNounPhraseIdx() > -1) {
								processNounPhrase(words, metadata, related, processedNounPhrases, subjc.getNounPhraseIdx(), sentence.getFullSentence(), verbST, verbPhrase.isPhraseNegated());
							}
						}
						/* ---------------------- */
						
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
				processPrepPhrase(words, metadata, temp, processedNounPhrases, processedPrepPhrases, i, sentence.getFullSentence(), false, null);
				if(!temp.isEmpty())
					structured.unrelated.add(temp);
			}
			
			// 3) loop through noun phrases to catch any that aren't grammatically-related to a verb phrase
			for(int i=0; i < metadata.getNounMetadata().size(); i++) {
				Multimap<String, MapValue> temp = ArrayListMultimap.create();
				processNounPhrase(words, metadata, temp, processedNounPhrases, i, sentence.getFullSentence(), null, false);
				if(!temp.isEmpty())
					structured.unrelated.add(temp);
			}
			
			// 4) process fragments when no metadata is present
			if(metadata.getVerbMetadata().isEmpty() && metadata.getPrepMetadata().isEmpty() && metadata.getNounMetadata().isEmpty()) {
				int size = -1;
				// account for punctuation ending the sentence
				if(words.get(words.size()-1).isPunctuation())
					size = words.size() - 1;
				else
					size = words.size();
				
				// TODO possibly expand this to look for negation anywhere in the fragment and apply downstream.
				// Ex. PSA not stable.
				boolean negated = Constants.NEGATION.matcher(words.get(0).getToken()).matches();
				
				//for(int i=size-1; i >= 0; i--) {
				for(int i=0; i < size; i++) {
					String rightST = words.get(i).getSemanticType();
					
					if(rightST != null) {
						String[] attribute = getConstructor("|" + rightST);
						
						if(attribute != null) {
							Multimap<String, MapValue> temp = ArrayListMultimap.create();
							temp.put(attribute[0], new MapValue(words.get(i).getToken(), negated ? ABSENCE_QUALIFIER : attribute[1], "|" + rightST, negated));
							
							structured.unrelated.add(temp);
						}
					}
				}
				/*
				if(size == 2) {
					String leftST = words.get(0).getSemanticType() == null ? "" : words.get(0).getSemanticType();
					String rightST = words.get(1).getSemanticType() == null ? "" : words.get(1).getSemanticType();
					
					String[] attribute = getConstructor(leftST + "|" + rightST);
					
					if(attribute != null) {
						Multimap<String, MapValue> temp = ArrayListMultimap.create();
						temp.put(attribute[0], new MapValue(finalToken, attribute[1], "|" + finalTokenST, attribute[1].equalsIgnoreCase("Absence")));
						
						structured.unrelated.add(temp);
					}
				} else if(size == 1) {
					String rightST = words.get(0).getSemanticType();
					
					if(rightST != null) {
						String[] attribute = getConstructor("|" + rightST);
						
						if(attribute != null) {
							Multimap<String, MapValue> temp = ArrayListMultimap.create();
							temp.put(attribute[0], new MapValue(words.get(0).getToken(), attribute[1], "|" + rightST, attribute[1].equalsIgnoreCase("Absence")));
							
							structured.unrelated.add(temp);
						}
					}
				}
				*/
			}
			
			// 5) process various regex patterns
			processRegex(structured.regex, sentence.getFullSentence());
			
			if(writeLogs)
				report(structured);
			
			String json = gson.toJson(structured);
			
			try {
				if(writeToMongo) {
					if(!structured.related.isEmpty() || !structured.unrelated.isEmpty() || !structured.regex.isEmpty()) {
						DBCollection coll = Constants.MongoDB.INSTANCE.getCollection("structured");
						DBObject dbObject = (DBObject) JSON.parse(json);
						coll.insert(dbObject);
					}
				}
			} catch(Exception e) {
				logger.error("buildStructuredOutput(): writeToMongo {}", e);
			}
		} catch(Exception e) {
			logger.error("buildStructuredOutput(): {}", e);
		}
		
		return structured;
	}
	
	private String[] getConstructor(String key) {
		String[] array = new String[2];
		List<String> list;
		
		try(Jedis jedis = Constants.MyJedisPool.INSTANCE.getResource()) {
			list = jedis.hmget("ct:"+key, "attr", "qualifier");
		}
		
		if(list.get(0) == null && list.get(1) == null) 
			array = null;
		else {
			array[0] = list.get(0);
			array[1] = list.get(1).length() == 0 ? null : list.get(1);
		}
		return array;
	}
	
	private StructuredData2 buildStructuredOutput2(Sentence sentence, boolean writeToMongo) {
		
		StructuredData2 structured = new StructuredData2();
		
		structured.patientId = sentence.getId();
		structured.practice = sentence.getPractice();
		structured.study = sentence.getStudy();
		structured.date = sentence.getProcedureDate();
		structured.sentence = sentence.getFullSentence();
		
		// maintain lists of prep and noun phrases as they are processed (perhaps by virtue of being within a verb phrase).
		// these phrases will not be processed again during their respective loops.
		List<Integer> processedPrepPhrases = new ArrayList<Integer>(); 
		List<Integer> processedNounPhrases = new ArrayList<Integer>();
		
		Set<String> negSource = new HashSet<>();
		
		try {
			SentenceMetadata metadata = sentence.getMetadata();
			ArrayList<WordToken> words = sentence.getWordList();
		
			// 1) loop through verb phrases
			for(VerbPhraseMetadata verbPhrase : metadata.getVerbMetadata()) {
				Multimap<String, MapValue2> related = ArrayListMultimap.create();
				negSource.clear();
				
				// Jan wanted to know which component of the verb phrase was negated for research purposes when looking at the structured data
				if(verbPhrase.getSubj() != null && verbPhrase.getSubj().isNegated())
					negSource.add("SUBJ");
				for(VerbPhraseToken token : verbPhrase.getSubjC())
					if(token.isNegated())
						negSource.add("SUBJC");
				for(VerbPhraseToken token : verbPhrase.getVerbs())
					if(token.isNegated())
						negSource.add("VB");
				
				int verbCompletenessTally = 0;
				
				switch(verbPhrase.getVerbClass()) {
					case ACTION:
					case LINKING_VERB:
					case VERB_OF_BEING:
					case MODAL_AUX:
						
						String subjST = (verbPhrase.getSubj() != null) ? words.get(verbPhrase.getSubj().getPosition()).getSemanticType() : null;
						String verbST = null;
						
						// verbST will be either null, that of the only token, that of the entire phrase (if present), or that of
						// the final token (if entire phrase has no ST)
						if(verbPhrase.getVerbs().size() == 1) {
							verbST = words.get(verbPhrase.getVerbs().get(0).getPosition()).getSemanticType();
						} else {
							verbST = verbPhrase.getSemanticType();
							
							if(verbST == null)
								verbST = words.get(verbPhrase.getVerbs().get(verbPhrase.getVerbs().size()-1).getPosition()).getSemanticType();
						}
						
						if(verbST != null) {
							// 1a) query constructors by subj/vb
							if(subjST != null) {
								String[] attribute = getConstructor(subjST + "|" + verbST);
								if(attribute != null) {
									related.put(attribute[0], new MapValue2(verbPhrase.getSubj().getToken(), verbPhrase.isPhraseNegated() ? ABSENCE_QUALIFIER : attribute[1], subjST + "|" + verbST, verbPhrase.isPhraseNegated(), "related", Joiner.on(',').join(negSource)));
									logFound(verbPhrase.getSubj().getToken(), subjST, subjST+"|"+verbST, verbPhrase.getSubj().getToken()+"|"+verbPhrase.getVerbString(), "VP", sentence.getFullSentence());
								} else {
									// no constructor for this ST pair
									logMissing(relCounts, subjST + "|" + verbST);
									logMissing2(relByToken, verbPhrase.getSubj().getToken(), verbPhrase.getVerbString(), subjST, verbST, sentence.getFullSentence(), "VP");
								}
							} else {
								if(verbPhrase.getSubj() != null) {
									// subj exists, but has no ST
									logMissing(stCounts, verbPhrase.getSubj().getToken() + "|" + words.get(verbPhrase.getSubj().getPosition()).getPOS());
									logMissingST(verbPhrase.getSubj().getToken(), verbPhrase.getSubj().getToken()+"|"+verbPhrase.getVerbString(), sentence.getFullSentence(), "VP");
								}
							}
							
							// 1b) query constructors by vb/subjc(s)
							for(VerbPhraseToken subjc : verbPhrase.getSubjC()) {
								String subjcST = words.get(subjc.getPosition()).getSemanticType();
							
								if(subjcST != null) {  // TODO possibly defer if SUBJC is within a noun phrase to avoid double-reporting
									String[] attribute = getConstructor(verbST + "|" + subjcST);
									
									if(attribute != null) {
										related.put(attribute[0], new MapValue2(subjc.getToken(), verbPhrase.isPhraseNegated() ? ABSENCE_QUALIFIER : attribute[1], verbST + "|" + subjcST, verbPhrase.isPhraseNegated(), "related", Joiner.on(',').join(negSource)));
										logFound(subjc.getToken(), subjcST, verbST+"|"+subjcST, verbPhrase.getVerbString()+"|"+subjc.getToken(),"VP", sentence.getFullSentence());
									} else {
										logMissing(relCounts, subjcST + "|" + verbST);
										logMissing2(relByToken, verbPhrase.getVerbString(), subjc.getToken(), verbST, subjcST, sentence.getFullSentence(), "VP");
									}
								} else {
									logMissing(stCounts, subjc.getToken() + "|" + words.get(subjc.getPosition()).getPOS());
									logMissingST(subjc.getToken(), verbPhrase.getVerbString()+"|"+subjc.getToken(), sentence.getFullSentence(), "VP");
								}
							}
							
						} else {
							// log missing ST for verb
							//String verb = (verbPhrase.getVerbs().size() == 1) ? words.get(verbPhrase.getVerbs().get(0).getPosition()).getToken() : verbPhrase.getVerbString();
							logMissing(stCounts, verbPhrase.getVerbString()+"|VB");
							logMissingST(verbPhrase.getVerbString(), "", sentence.getFullSentence(), "VP");
						}
						
						/* changed 6/17/15 - pulled this out of the above "if" that required the subj/vb/subjc to have a ST to process related PPs */
						if(verbPhrase.getSubj() != null) {
							// 1c) process prep phrases related to subj
							for(int ppIdx : verbPhrase.getSubj().getPrepPhrasesIdx()) {
								// TODO apply negation to verb phrase as a whole and pass to processPrepPhrase()
								// 9/23/15 - changed to send verbST rather than null to support sentences where SUBJ and VB are one and the same. "Is on Lupron."
								processPrepPhrase2(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), verbST, "related", negSource);
							}
							// 1d) process noun phrases related to subj
							if(verbPhrase.getSubj().getNounPhraseIdx() > -1) {
								processNounPhrase2(words, metadata, related, processedNounPhrases, verbPhrase.getSubj().getNounPhraseIdx(), sentence.getFullSentence(), null, false, "related", negSource);
							}
						}
				
						// 1e) process prep phrases related to (final) verb (of phrase)
						for(int ppIdx : verbPhrase.getVerbs().get(verbPhrase.getVerbs().size()-1).getPrepPhrasesIdx()) {
							processPrepPhrase2(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), verbST, "related", negSource);
						}
						
						for(VerbPhraseToken subjc : verbPhrase.getSubjC()) {
							// 1f) process prep phrases related to subjc
							for(int ppIdx : subjc.getPrepPhrasesIdx()) {
								processPrepPhrase2(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), words.get(subjc.getPosition()).getSemanticType(), "related", negSource);
							}
							
							// 1g) process noun phrases related to subjc
							if(subjc.getNounPhraseIdx() > -1) {
								processNounPhrase2(words, metadata, related, processedNounPhrases, subjc.getNounPhraseIdx(), sentence.getFullSentence(), verbST, verbPhrase.isPhraseNegated(), "related", negSource);
							}
						}
						
					case PREPOSITIONAL:
					case INFINITIVE:
						
					default:
						
						break;
				}
				
				if(!related.isEmpty()) {
					structured.data.add(related);
				}
			}
			
			// 2) loop through prep phrases to catch any that aren't grammatically-related to a verb phrase
			negSource.clear();
			for(int i=0; i < metadata.getPrepMetadata().size(); i++) {
				Multimap<String, MapValue2> temp = ArrayListMultimap.create();
				processPrepPhrase2(words, metadata, temp, processedNounPhrases, processedPrepPhrases, i, sentence.getFullSentence(), false, null, "unrelated", negSource);
				if(!temp.isEmpty())
					structured.data.add(temp);
			}
			
			// 3) loop through noun phrases to catch any that aren't grammatically-related to a verb phrase
			negSource.clear();
			for(int i=0; i < metadata.getNounMetadata().size(); i++) {
				Multimap<String, MapValue2> temp = ArrayListMultimap.create();
				processNounPhrase2(words, metadata, temp, processedNounPhrases, i, sentence.getFullSentence(), null, false, "unrelated", negSource);
				if(!temp.isEmpty())
					structured.data.add(temp);
			}
			
			// 4) process fragments when no metadata is present
			if(metadata.getVerbMetadata().isEmpty() && metadata.getPrepMetadata().isEmpty() && metadata.getNounMetadata().isEmpty()) {
				int size = -1;
				// account for punctuation ending the sentence
				if(words.get(words.size()-1).isPunctuation())
					size = words.size() - 1;
				else
					size = words.size();
				
				// TODO possibly expand this to look for negation anywhere in the fragment and apply downstream.
				// Ex. "PSA not stable." <-- does not capture negation currently.
				boolean negated = Constants.NEGATION.matcher(words.get(0).getToken()).matches();
				
				for(int i=0; i < size; i++) {
					String rightST = words.get(i).getSemanticType();
					
					if(rightST != null) {
						String[] attribute = getConstructor("|" + rightST);
						
						if(attribute != null) {
							Multimap<String, MapValue2> temp = ArrayListMultimap.create();
							temp.put(attribute[0], new MapValue2(words.get(i).getToken(), negated ? ABSENCE_QUALIFIER : attribute[1], "|" + rightST, negated, "unrelated", null));
							
							structured.data.add(temp);
						}
					}
				}
			}
			
			// 5) process various regex patterns
			processRegex2(structured.data, sentence.getFullSentence());
			
			if(writeLogs) {
				report2(structured, metadata);
				if(structured.data.isEmpty()) {
					unprocessedSentences.add(sentence.getFullSentence());
				}
			}
			
			try {
				if(writeToMongo) {
					//if(!structured.related.isEmpty() || !structured.unrelated.isEmpty() || !structured.regex.isEmpty()) {
					if(!structured.data.isEmpty()) {
						String json = gson.toJson(structured);
						DBCollection coll = Constants.MongoDB.INSTANCE.getCollection("structured");
						DBObject dbObject = (DBObject) JSON.parse(json);
						coll.insert(dbObject);
					}
				}
			} catch(Exception e) {
				logger.error("buildStructuredOutput2(): writeToMongo {}", e);
			}
		} catch(Exception e) {
			logger.error("buildStructuredOutput2(): {}", e);
		}
		
		return structured;
	}
	
	public StructuredData process(Sentence sentence, boolean writeToMongo) {
		// this is used by the camel process
		System.out.println("Calling StructuredOutputHelper.process() for sentence: " + sentence.getFullSentence());
		return buildStructuredOutput(sentence, writeToMongo);
	}
	
	public StructuredData2 process2(Sentence sentence, boolean writeToMongo) {
		// this is used by the camel process
		//System.out.println("Calling StructuredOutputHelper.process2() for sentence: " + sentence.getFullSentence());
		
		if(writeLogs) {
			initReports("scott", "test");
			
			String[] headers = new String[Headers.values().length];
			int j = 0;
			for(Headers header : Headers.values()) {
				headers[j] = header.name();
				j++;
			}
			report.writeNext(headers);
		}
		
		StructuredData2 output = buildStructuredOutput2(sentence, writeToMongo);
		
		if(writeLogs) {
			writeLogs(buildReportPath("scott", "test"));
			try {
				report.close();
			} catch(Exception e) { e.printStackTrace(); }
		}
		return output;
	}
	
	// this only exists to support showing JSON on the annotation tool website because I don't have
	// time to figure out the Jersey built-in JSON unmarshalling in JAXB
	public String processReturnJSON(Sentence sentence, boolean writeToMongo) {
		return gson.toJson(buildStructuredOutput2(sentence, writeToMongo));
	}
	
	public void process2(String practice, String study, int limit, boolean writeToMongo, boolean medsOnly) {
		
		//long startTime = Constants.getTime();
		
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
			
			if(medsOnly) {
				getMeds(practice);
			} else {
				DBCollection coll = Constants.MongoDB.INSTANCE.getCollection("annotations");
				
				DBObject query = QueryBuilder.start()
						.put("practice").is(practice)
						.put("study").is(study)
						.get();
				
				DBCursor cursor = null;
				
				if(limit == -1)
					cursor = coll.find(query);
				else
					cursor = coll.find(query).limit(limit);
				
				int count = 0, cursorSize = cursor.size();
				
				while(cursor.hasNext()) {
					if(count++ % 100 == 0)
						System.out.println(count + " / " + cursorSize);
					
					BasicDBObject obj = (BasicDBObject) cursor.next();
					Sentence sentence = gson.fromJson(obj.toString(), Sentence.class);
					
					buildStructuredOutput2(sentence, writeToMongo);
					//System.out.println(foo);
				}
				
				cursor.close();
				
				writeLogs(buildReportPath(practice, study));
			}
			
			//System.out.println(Constants.formatTime((Constants.getTime() - startTime)/1000.0));
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			Constants.MongoDB.INSTANCE.close();
			try {
				report.close();
			} catch(Exception e) { e.printStackTrace(); }
		}
	}
	
	public void process(String practice, String study, int limit, boolean writeToMongo) {
		
		//long startTime = Constants.getTime();
		
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
			
			DBCollection coll = Constants.MongoDB.INSTANCE.getCollection("annotations");
			
			DBObject query = QueryBuilder.start()
					.put("practice").is(practice)
					.put("study").is(study)
					.get();
			
			DBCursor cursor = null;
			
			if(limit == -1)
				cursor = coll.find(query);
			else
				cursor = coll.find(query).limit(limit);
			
			int count = 0, cursorSize = cursor.size();
			
			while(cursor.hasNext()) {
				if(count++ % 100 == 0)
					System.out.println(count + " / " + cursorSize);
				
				BasicDBObject obj = (BasicDBObject) cursor.next();
				Sentence sentence = gson.fromJson(obj.toString(), Sentence.class);
				
				buildStructuredOutput(sentence, writeToMongo);
				//System.out.println(foo);
			}
			
			cursor.close();
			
			writeLogs(buildReportPath(practice, study));
			
			getMeds(practice);
			
			//System.out.println(Constants.formatTime((Constants.getTime() - startTime)/1000.0));
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			Constants.MongoDB.INSTANCE.close();
			try {
				report.close();
			} catch(Exception e) { e.printStackTrace(); }
		}
	}
	
	private void processPrepPhrase2(ArrayList<WordToken> words, SentenceMetadata metadata, Multimap<String, MapValue2> results, List<Integer> processedNounPhrases, List<Integer> processedPrepPhrases, int ppIdx, String fullSentence, boolean verbNegated, String verbST, String source, Set<String> negSource) {
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
			String qualifier = null;
			
			if(prepTokenST != null) {
				// first, query the constructors by the semantic types of the token preceding the preposition and the preposition itself
				// this is the "cross-border" case
				// TODO list example sentence for this case
				// TODO do I need to handle negation?
				
				String newPrepTokenST = prepTokenST;
				
				if(verbST != null) {
					//String[] attribute = getConstructor(verbST + "|" + prepTokenST);
					
					//if(attribute != null) {
						// 7/19/15 - altered the above logic to use the supplied verbST rather than simply looking backwards one token.
						// Changed on 6/24 - qualifier is picked up from token/preposition constructor lookup
						// Ex. "was on Lupron"; on Lupron has no qualifier but qualifier for was|on = "Past"
						// altered logic should work for sentences such as "I will start her on Vesicare 5 mg daily."
						// this will override the qualifier picked up later in this method when the intra-PP tokens are processed UNLESS that qualifier is Absence
						//qualifier = attribute[1];
						// 9/25/2015 - removed the above constructor check for verbST | prepTokenST because it was an early version of attempting to have the verb
						// influence the temporal of the finding. We now do that via qualifier on the verb phrase. Below, we create a "triple debug" to show that multiple
						// constructors are affecting the finding. 
					//}
					
					newPrepTokenST = verbST + "|" + prepTokenST; // ex. was_past|on|drugpr
					
					if(verbST.indexOf('_') > -1) {
						// handle verbs that may come in as the predecessor ST
						qualifier = verbST.split("_")[1];
					} else {
						// handle (so far) when SUBJC comes in as predecessor ST
						// Ex. He was given literature on Lupron.
						String[] attribute = getConstructor(verbST + "|" + prepTokenST);
						if(attribute != null)
							qualifier = attribute[1];
					}
					// Avoid adding this PP to the processedPrepPhrases list. Assuming everything checks out, this will be taken care of below.
				}
				
				// loop through each member of the prep phrase after the initial preposition
				for(int i=1; i < prepPhrase.size(); i++) {
					PrepPhraseToken ppToken = prepPhrase.get(i);
					
					// is the token a prep phrase object?
					if(words.get(ppToken.getPosition()).isPrepPhraseObject()) {
						String objTokenST = "";
						String objToken = "";
						
						// is the object within a noun phrase?
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
							objToken = metadata.getNounMetadata().get(npIdx).getNounPhraseString();
						} else {
							// no, set ST2 to the ST of the current token
							objTokenST = words.get(ppToken.getPosition()).getSemanticType();
							objToken = words.get(ppToken.getPosition()).getToken();
							// override npIdx since we want to process individual tokens against the preposition rather than the noun phrase as a whole (since the NP doesn't have a ST)
							npIdx = -1;
						}
						
						if(objTokenST != null) {
							String[] attribute = getConstructor(prepTokenST + "|" + objTokenST);
							if(attribute != null) {
								processedPrepPhrases.add(ppIdx);
								
								logFound(objToken, objTokenST, prepTokenST+"|"+objTokenST, prepPhrase.get(0).getToken()+"|"+objToken, "PP", fullSentence);
								
								if(metadata.getPrepMetadata().get(ppIdx).isNegated())
									negSource.add("PP");
								
								if(npIdx > -1) {
									if(metadata.getNounMetadata().get(npIdx).isNegated())
										negSource.add("NP");
									
									boolean negated = metadata.getPrepMetadata().get(ppIdx).isNegated() || metadata.getNounMetadata().get(npIdx).isNegated() || verbNegated; // TODO verbNegated causes unintended false positives
									results.put(attribute[0], new MapValue2(metadata.getNounMetadata().get(npIdx).getNounPhraseString(), negated ? ABSENCE_QUALIFIER : (qualifier != null ? qualifier : attribute[1]), newPrepTokenST + "|" + objTokenST, negated, source, Joiner.on(',').join(negSource)));
									processedNounPhrases.add(npIdx);
									// exit the for so as to avoid processing the individual NP tokens
									break;
								} else {
									boolean negated = metadata.getPrepMetadata().get(ppIdx).isNegated() || verbNegated;
									results.put(attribute[0], new MapValue2(ppToken.getToken(), negated ? ABSENCE_QUALIFIER : (qualifier != null ? qualifier : attribute[1]), newPrepTokenST + "|" + objTokenST, negated, source, Joiner.on(',').join(negSource)));
								}
								
							} else {
								// TODO if npIdx > 1 possibly query individual tokens against the preposition if no result from full phrase
								logMissing(relCounts, prepTokenST + "|" + objTokenST);
								logMissing2(relByToken, prepPhrase.get(0).getToken(), objToken, prepTokenST, objTokenST, fullSentence, "PP");
							}
						} else {
							logMissing(stCounts, ppToken.getToken()+"|"+words.get(ppToken.getPosition()).getPOS());
							//logMissingST(missingSTByToken, prepPhrase.get(0).getToken(), objToken, null, null, fullSentence, "PP");
							logMissingST(objToken, prepPhrase.get(0).getToken()+"|"+objToken, fullSentence, "PP");
						}
					}
				}
				
				//if(!results.isEmpty())
				//	list.add(results);
				
			} else {
				logMissing(stCounts, prepPhrase.get(0).getToken()+"|"+words.get(prepPhrase.get(0).getPosition()).getPOS());
				//logMissingST(missingSTByToken, prepPhrase.get(0).getToken(), "", null, null, fullSentence, "PP");
				logMissingST(prepPhrase.get(0).getToken(), "", fullSentence, "PP");
			}
		}
	}
	
	private void processPrepPhrase(ArrayList<WordToken> words, SentenceMetadata metadata, Multimap<String, MapValue> results, List<Integer> processedNounPhrases, List<Integer> processedPrepPhrases, int ppIdx, String fullSentence, boolean verbNegated, String verbST) {
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
			String qualifier = null;
			
			if(prepTokenST != null) {
				// first, query the constructors by the semantic types of the token preceding the preposition and the preposition itself
				// this is the "cross-border" case
				// TODO list example sentence for this case
				// TODO do I need to handle negation?
				
				try {
//					String prevTokenST = words.get(prepPhrase.get(0).getPosition()-1).getSemanticType();
//					
//					if(prevTokenST != null) {
//						String[] attribute = getConstructor(prevTokenST + "|" + prepTokenST);
//						
//						if(attribute != null) {
//							// Changed on 6/24 - qualifier is picked up from token/preposition constructor lookup
//							// Ex. "was on Lupron"; on Lupron has no qualifier but qualifier for was|on = "Past"
//							// this will override the qualifier picked up later in this method when the intra-PP tokens are processed UNLESS that qualifier is Absence
//							qualifier = attribute[1];
//							// Avoid adding this PP to the processedPrepPhrases list. Assuming everything checks out, this will be taken care of below. 
//						}
//					}
					
					if(verbST != null) {
						String[] attribute = getConstructor(verbST + "|" + prepTokenST);
						
						if(attribute != null) {
							// 7/19/15 - altered the above logic to use the supplied verbST rather than simply looking backwards one token.
							// Changed on 6/24 - qualifier is picked up from token/preposition constructor lookup
							// Ex. "was on Lupron"; on Lupron has no qualifier but qualifier for was|on = "Past"
							// altered logic should work for sentences such as "I will start her on Vesicare 5 mg daily."
							// this will override the qualifier picked up later in this method when the intra-PP tokens are processed UNLESS that qualifier is Absence
							qualifier = attribute[1];
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
						
						// is the object within a noun phrase?
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
							objToken = metadata.getNounMetadata().get(npIdx).getNounPhraseString();
						} else {
							// no, set ST2 to the ST of the current token
							objTokenST = words.get(ppToken.getPosition()).getSemanticType();
							objToken = words.get(ppToken.getPosition()).getToken();
							// override npIdx since we want to process individual tokens against the preposition rather than the noun phrase as a whole (since the NP doesn't have a ST)
							npIdx = -1;
						}
						
						if(objTokenST != null) {
							String[] attribute = getConstructor(prepTokenST + "|" + objTokenST);
							if(attribute != null) {
								processedPrepPhrases.add(ppIdx);
								
								logFound(objToken, objTokenST, prepTokenST+"|"+objTokenST, prepPhrase.get(0).getToken()+"|"+objToken, "PP", fullSentence);
								
								if(npIdx > -1) {
									boolean negated = metadata.getPrepMetadata().get(ppIdx).isNegated() || metadata.getNounMetadata().get(npIdx).isNegated() || verbNegated; // TODO verbNegated causes unintended false positives
									results.put(attribute[0], new MapValue(metadata.getNounMetadata().get(npIdx).getNounPhraseString(), negated ? ABSENCE_QUALIFIER : (qualifier != null ? qualifier : attribute[1]), prepTokenST + "|" + objTokenST, negated));
									processedNounPhrases.add(npIdx);
									// exit the for so as to avoid processing the individual NP tokens
									break;
								} else {
									boolean negated = metadata.getPrepMetadata().get(ppIdx).isNegated() || verbNegated;
									results.put(attribute[0], new MapValue(ppToken.getToken(), negated ? ABSENCE_QUALIFIER : (qualifier != null ? qualifier : attribute[1]), prepTokenST + "|" + objTokenST, negated));
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
	
	private void processNounPhrase2(ArrayList<WordToken> words, SentenceMetadata metadata, Multimap<String, MapValue2> list, List<Integer> processedNounPhrases, int npIdx, String fullSentence, String leftST, boolean leftNegated, String source, Set<String> negSource) {

		// leftST represents the ST for token(s) that may occur before this noun phrase
		// the example used is "He is having abdominal pain."
		// in this case, leftST would be supplied as the ST for "is having"
		// for noun phrases floating off on their own, leftST will be null
		
		if(!processedNounPhrases.contains(npIdx)) {
			
			String npST = metadata.getNounMetadata().get(npIdx).getSemanticType();
			String npString = metadata.getNounMetadata().get(npIdx).getNounPhraseString();
			boolean negated = metadata.getNounMetadata().get(npIdx).isNegated() || leftNegated;
			
			if(metadata.getNounMetadata().get(npIdx).isNegated())
				negSource.add("NP");
			
			// first query constructors by ST of entire noun phrase, if present
			// this is sort of another cross-border situation
			if(leftST != null && npST != null) {
				String[] attribute = getConstructor(leftST + "|" + npST);
				
				if(attribute != null) {
					list.put(attribute[0], new MapValue2(npString, negated ? ABSENCE_QUALIFIER : attribute[1], leftST + "|" + npST, negated, source, Joiner.on(',').join(negSource)));
					processedNounPhrases.add(npIdx);
					logFound(npString, npST, leftST+"|"+npST, "", "NP", fullSentence);
				} else {
					// catch situation such as "no bone pain" fragment. on hold because there is no way to know which structured attribute to use (e.g. Clinical Finding).
					// possibly the only way to handle this is with a constructor.
					//if(metadata.getNounMetadata().get(npIdx).isNegated()) {
					//	list.put(attribute[0], new MapValue(npString, attribute[1], leftST + "|" + npST, true));
					//}
					logMissing(relCounts, leftST + "|" + npST);
					logMissing2(relByToken, "", npString, leftST, npST, fullSentence, "NP");
				}
				
			// TODO should we first check leftST against phraseST and if no constructor hit then default to the below? 
			} else {
			
				// log the fact that the entire NP did not have a semantic type
				if(npST == null) {
					logMissing(stCounts, npString);
					//logMissingST(npString, "", fullSentence, "NP"); // commented out for now as it could be very chatty
				}
				
				List<GenericToken> nounPhrase = metadata.getNounMetadata().get(npIdx).getPhrase();
				// query every token of the NP against the final token
				String finalTokenST = words.get(nounPhrase.get(nounPhrase.size()-1).getPosition()).getSemanticType();
				String finalToken = nounPhrase.get(nounPhrase.size()-1).getToken();
				
				if(finalTokenST != null) {
					for(int i=0; i < nounPhrase.size()-1; i++) {
						String tokenST = words.get(nounPhrase.get(i).getPosition()).getSemanticType();
						
						if(tokenST != null) {
							String[] attribute = getConstructor(tokenST + "|" + finalTokenST);
							if(attribute != null) {
								
								list.put(attribute[0], new MapValue2(nounPhrase.get(i).getToken(), negated ? ABSENCE_QUALIFIER : attribute[1], tokenST + "|" + finalTokenST, negated, source, Joiner.on(',').join(negSource)));
								processedNounPhrases.add(npIdx);
								logFound(nounPhrase.get(i).getToken(), tokenST, tokenST+"|"+finalTokenST, nounPhrase.get(i).getToken()+"|"+finalToken, "NP", fullSentence);
							} else {
								logMissing(relCounts, tokenST + "|" + finalTokenST);
								logMissing2(relByToken, nounPhrase.get(i).getToken(), finalToken, tokenST, finalTokenST, fullSentence, "NP");
							}
						} else {
							logMissing(stCounts, nounPhrase.get(i).getToken()+"|"+words.get(nounPhrase.get(i).getPosition()).getPOS());
							logMissingST(nounPhrase.get(i).getToken(), nounPhrase.get(i).getToken()+"|"+finalToken, fullSentence, "NP");
						}
					}
				
				} else {
					logMissing(stCounts, finalToken+"|"+words.get(nounPhrase.get(nounPhrase.size()-1).getPosition()).getPOS());
					logMissingST(finalToken, "", fullSentence, "NP");
				}
				
				// special case for fragments such as "Bone pain."
				// query constructors for null|NP head ST when sentence does not contain a verb
				// TODO should this function more like fragments with no metadata?
				// ... "Bone pain." is handled correctly because bpoc|sympto exists. "Bone mets." leaves out Finding Site because no constructor exits.
				// ... would doing the looping approach for null|ST be a better solution?
				if(metadata.getVerbMetadata().size() == 0) {
//					String[] attribute = getConstructor("|" + finalTokenST);
//					if(attribute != null) {
//						
//						list.put(attribute[0], new MapValue(finalToken, negated ? ABSENCE_QUALIFIER : attribute[1], "|" + finalTokenST, negated));
//						processedNounPhrases.add(npIdx);
//						logFound(finalToken, "", "|"+finalTokenST, "|"+finalToken, "NP", fullSentence);
//					} else {
//						logMissing(relCounts, "|" + finalTokenST);
//						logMissing2(relByToken, "", finalToken, "", finalTokenST, fullSentence, "NP");
//					}
					
					for(GenericToken npToken : nounPhrase) {
						String rightST = words.get(npToken.getPosition()).getSemanticType();
						
						if(rightST != null) {
							String[] attribute = getConstructor("|" + rightST);
							
							if(attribute != null) {
								list.put(attribute[0], new MapValue2(npToken.getToken(), negated ? ABSENCE_QUALIFIER : attribute[1], "|" + rightST, negated, source, Joiner.on(',').join(negSource)));
								processedNounPhrases.add(npIdx);
							}
						}
					}
					
				}
			}
		}
	}
	
	private void processNounPhrase(ArrayList<WordToken> words, SentenceMetadata metadata, Multimap<String, MapValue> list, List<Integer> processedNounPhrases, int npIdx, String fullSentence, String leftST, boolean leftNegated) {

		// leftST represents the ST for token(s) that may occur before this noun phrase
		// the example used is "He is having abdominal pain."
		// in this case, leftST would be supplied as the ST for "is having"
		// for noun phrases floating off on their own, leftST will be null
		
		if(!processedNounPhrases.contains(npIdx)) {
			
			String npST = metadata.getNounMetadata().get(npIdx).getSemanticType();
			String npString = metadata.getNounMetadata().get(npIdx).getNounPhraseString();
			boolean negated = metadata.getNounMetadata().get(npIdx).isNegated() || leftNegated;
			
			// first query constructors by ST of entire noun phrase, if present
			// this is sort of another cross-border situation
			if(leftST != null && npST != null) {
				String[] attribute = getConstructor(leftST + "|" + npST);
				
				if(attribute != null) {
					list.put(attribute[0], new MapValue(npString, negated ? ABSENCE_QUALIFIER : attribute[1], leftST + "|" + npST, negated));
					processedNounPhrases.add(npIdx);
					logFound(npString, npST, leftST+"|"+npST, "", "NP", fullSentence);
				} else {
					// catch situation such as "no bone pain" fragment. on hold because there is no way to know which structured attribute to use (e.g. Clinical Finding).
					// possibly the only way to handle this is with a constructor.
					//if(metadata.getNounMetadata().get(npIdx).isNegated()) {
					//	list.put(attribute[0], new MapValue(npString, attribute[1], leftST + "|" + npST, true));
					//}
					logMissing(relCounts, leftST + "|" + npST);
					logMissing2(relByToken, "", npString, leftST, npST, fullSentence, "NP");
				}
				
			// TODO should we first check leftST against phraseST and if no constructor hit then default to the below? 
			} else {
			
				// log the fact that the entire NP did not have a noun phrase
				if(npST == null) {
					logMissing(stCounts, npString);
					//logMissingST(npString, "", fullSentence, "NP"); // commented out for now as it could be very chatty
				}
				
				List<GenericToken> nounPhrase = metadata.getNounMetadata().get(npIdx).getPhrase();
				// query every token of the NP against the final token
				String finalTokenST = words.get(nounPhrase.get(nounPhrase.size()-1).getPosition()).getSemanticType();
				String finalToken = nounPhrase.get(nounPhrase.size()-1).getToken();
				
				if(finalTokenST != null) {
					for(int i=0; i < nounPhrase.size()-1; i++) {
						String tokenST = words.get(nounPhrase.get(i).getPosition()).getSemanticType();
						
						if(tokenST != null) {
							String[] attribute = getConstructor(tokenST + "|" + finalTokenST);
							if(attribute != null) {
								
								list.put(attribute[0], new MapValue(nounPhrase.get(i).getToken(), negated ? ABSENCE_QUALIFIER : attribute[1], tokenST + "|" + finalTokenST, negated));
								processedNounPhrases.add(npIdx);
								logFound(nounPhrase.get(i).getToken(), tokenST, tokenST+"|"+finalTokenST, nounPhrase.get(i).getToken()+"|"+finalToken, "NP", fullSentence);
							} else {
								logMissing(relCounts, tokenST + "|" + finalTokenST);
								logMissing2(relByToken, nounPhrase.get(i).getToken(), finalToken, tokenST, finalTokenST, fullSentence, "NP");
							}
						} else {
							logMissing(stCounts, nounPhrase.get(i).getToken());
							logMissingST(nounPhrase.get(i).getToken(), nounPhrase.get(i).getToken()+"|"+finalToken, fullSentence, "NP");
						}
					}
									
				} else {
					logMissing(stCounts, finalToken);
					logMissingST(finalToken, "", fullSentence, "NP");
				}
				
				// special case for fragments such as "Bone pain."
				// query constructors for null|NP head ST when sentence does not contain a verb
				// TODO should this function more like fragments with no metadata?
				// ... "Bone pain." is handled correctly because bpoc|sympto exists. "Bone mets." leaves out Finding Site because no constructor exits.
				// ... would doing the looping approach for null|ST be a better solution?
				if(metadata.getVerbMetadata().size() == 0) {
//					String[] attribute = getConstructor("|" + finalTokenST);
//					if(attribute != null) {
//						
//						list.put(attribute[0], new MapValue(finalToken, negated ? ABSENCE_QUALIFIER : attribute[1], "|" + finalTokenST, negated));
//						processedNounPhrases.add(npIdx);
//						logFound(finalToken, "", "|"+finalTokenST, "|"+finalToken, "NP", fullSentence);
//					} else {
//						logMissing(relCounts, "|" + finalTokenST);
//						logMissing2(relByToken, "", finalToken, "", finalTokenST, fullSentence, "NP");
//					}
					
					for(GenericToken npToken : nounPhrase) {
						String rightST = words.get(npToken.getPosition()).getSemanticType();
						
						if(rightST != null) {
							String[] attribute = getConstructor("|" + rightST);
							
							if(attribute != null) {
								list.put(attribute[0], new MapValue(npToken.getToken(), negated ? ABSENCE_QUALIFIER : attribute[1], "|" + rightST, negated));
								processedNounPhrases.add(npIdx);
							}
						}
					}
					
				}
			}
		}
	}
	
	private void processRegex2(List<Multimap<String, MapValue2>> list, String sentence) {
		// PSA/Gleason regex processing. Find instances that could not be picked up by a constructor.
		
		//Pattern ggRegex = Pattern.compile(Constants.GLEASON_REGEX);
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
		
		final String SOURCE = "regex";
		
		Matcher matcher = Constants.GLEASON_REGEX.matcher(sentence);

		while(matcher.find()) {
			String val = matcher.group(matcher.groupCount()); // get last group, which should be the Gleason value
			if(val != null) {
				val = val.trim();
				
				Multimap<String, MapValue2> mm = ArrayListMultimap.create();
				
				mm.put(DIAP_LABEL, new MapValue2(GLEASON_LABEL, SOURCE));
				mm.put(ABSV_LABEL, new MapValue2(parseGleasonValue(val), null, Constants.GLEASON_REGEX.toString(), SOURCE));
				
				list.add(mm);
			}
		}

		matcher = chesapeakePSA1.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue2> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue2(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue2(matcher.group(2), null, chesapeakePSA1.toString(), SOURCE));
			
			list.add(mm);
		}

		matcher = chesapeakePSA2.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue2> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue2(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue2(matcher.group(1), null, chesapeakePSA2.toString(), SOURCE));
			mm.put(DATE_LABEL, new MapValue2(matcher.group(3), SOURCE));
			
			list.add(mm);
		}

		matcher = chesapeakePSA3.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue2> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue2(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue2(matcher.group(3).trim(), null, chesapeakePSA3.toString(), SOURCE));
			mm.put(DATE_LABEL, new MapValue2(matcher.group(1).trim(), SOURCE));
			
			list.add(mm);
		}

		matcher = chesapeakePSA4.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue2> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue2(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue2(matcher.group(1).trim(), null, chesapeakePSA4.toString(), SOURCE));
			if(matcher.groupCount() > 2)
				mm.put(DATE_LABEL, new MapValue2(matcher.group(matcher.groupCount()).trim(), SOURCE));
			//try {
			//	mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim()));
			//} catch(Exception e) { System.out.println(e.toString() + "\n\t" + sentence + "\n\t" + chesapeakePSA4.toString()); }
			
			list.add(mm);
		}

		matcher = chesapeakePSA5.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue2> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue2(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue2(matcher.group(2).trim(), null, chesapeakePSA5.toString(), SOURCE));
			//for(int i=0; i <= matcher.groupCount(); i++) {
			//	System.out.println(i + " - " + matcher.group(i));
			//}
			if(matcher.group(matcher.groupCount()) != null)
				mm.put(DATE_LABEL, new MapValue2(matcher.group(matcher.groupCount()).trim(), SOURCE));
			
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
	
	private void processRegex(List<Multimap<String, MapValue>> list, String sentence) {
		// PSA/Gleason regex processing. Find instances that could not be picked up by a constructor.
		
		//Pattern ggRegex = Pattern.compile(Constants.GLEASON_REGEX);
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
		
		Matcher matcher = Constants.GLEASON_REGEX.matcher(sentence);

		while(matcher.find()) {
			String val = matcher.group(matcher.groupCount()); // get last group, which should be the Gleason value
			if(val != null) {
				val = val.trim();
				
				Multimap<String, MapValue> mm = ArrayListMultimap.create();
				
				mm.put(DIAP_LABEL, new MapValue(GLEASON_LABEL));
				mm.put(ABSV_LABEL, new MapValue(parseGleasonValue(val), null, Constants.GLEASON_REGEX.toString()));
				
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
	
	public void getMeds(String practice) {
		// ### add rows from discreet collection ###
		DBCollection coll = Constants.MongoDB.INSTANCE.getCollection("discreet");
		
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
			CSVWriter csvUnprocessed = null;
			
			try {
				csvSTCounts = new CSVWriter(new FileWriter(path + "missing_st_counts.csv"), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
				csvSTByToken = new CSVWriter(new FileWriter(path + "missing_st_by_token.csv"), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER);
				csvRelCounts = new CSVWriter(new FileWriter(path + "missing_relationship_counts.csv"), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
				csvRelByToken = new CSVWriter(new FileWriter(path + "missing_relationships_by_token.csv"), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER);
				csvFoundST = new CSVWriter(new FileWriter(path + "found_st_by_token.csv"), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER);
				csvUnprocessed = new CSVWriter(new FileWriter(path + "unprocessed.csv"));
				
				String[] data = new String[4];
				
				String[] stCountsHeaders = { "token(s)","pos","count","token count" };
				csvSTCounts.writeNext(stCountsHeaders);
				for(String key : stCounts.keySet()) {
					try {
						//System.out.println(key + "," + missingST.get(key));
						if(key.indexOf('|') > -1) {
							String[] pipe = key.split("\\|");
							
							data[0] = pipe[0];
							data[1] = pipe[1];
						} else {
							data[0] = key;
							data[1] = "";
						}
						
						data[2] = String.valueOf(stCounts.get(key));
						String[] space = data[0].split(" ");
						data[3] = String.valueOf(space.length);
						csvSTCounts.writeNext(data);
					} catch(Exception e) {
						System.out.println(e.toString() + "\n" + key);
					}
				}
				
				data = new String[2];
				for(String key : relCounts.keySet()) {
					//System.out.println(key + "," + missingConstr.get(key));
					data[0] = key;
					data[1] = String.valueOf(relCounts.get(key));
					csvRelCounts.writeNext(data);
				}
				
				for(String item : unprocessedSentences) {
					csvUnprocessed.writeNext(new String[] { item });
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
				for(String item : relByToken) {
					csvRelByToken.writeNext(item.split("<>"));
				}
							
				String[] st2Headers = { "token","relationship","type","sentence" };
				csvSTByToken.writeNext(st2Headers);
				
				for(String item : stByToken) {
					csvSTByToken.writeNext(item.split("<>"));
				}
				
				String[] foundSTHeaders = { "token","token st","relationship-ST","relationship-token","type","sentence" };
				csvFoundST.writeNext(foundSTHeaders);
				
				for(String item : foundSTByToken) {
					csvFoundST.writeNext(item.split("<>"));
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
	
	private void logMissing2(Set<String> set, String leftToken, String rightToken, String leftST, String rightST, String sentence, String type) {
		if(writeLogs) {
			if(leftST == null && rightST == null) {
				String[] row = { leftToken, leftToken + "|" + rightToken, type, sentence };
				set.add(Joiner.on("<>").join(row));
			} else {
				String[] row = { leftST + "|" + rightST, leftToken, rightToken, type, sentence };
				set.add(Joiner.on("<>").join(row));
			}
		}
	}
	
	private void logMissingST(String missing, String relationship, String sentence, String type) {
		if(writeLogs) {
			String[] row = { missing, relationship, type, sentence };
			stByToken.add(Joiner.on("<>").join(row));
		}
	}
	
	private void logFound(String token, String tokenST, String relationshipST, String relationshipToken, String type, String sentence) {	
		if(writeLogs) {
			String[] row = { token, tokenST, relationshipST, relationshipToken, type, sentence };
			foundSTByToken.add(Joiner.on("<>").join(row));
		}
	}
	
	private void report(StructuredData structured) {
		try {
			String[] data = new String[Headers.values().length];
			Arrays.fill(data, "");
			
			data[Headers.PATIENT_ID.ordinal()] = structured.patientId;
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
	
	private void report2(StructuredData2 structured, SentenceMetadata metadata) {
		try {
			String[] data = new String[Headers.values().length];
			Arrays.fill(data, "");
			
			data[Headers.PATIENT_ID.ordinal()] = structured.patientId;
			data[Headers.VISIT_DATE.ordinal()] = structured.date != null ? sdf.format(structured.date) : "";
			data[Headers.SENTENCE.ordinal()] = structured.sentence;
			data[Headers.VERB_PHRASE_COUNT.ordinal()] = metadata.getVerbMetadata() == null ? "0" : String.valueOf(metadata.getVerbMetadata().size());

			for(VerbPhraseMetadata vpm1 : metadata.getVerbMetadata()) {
				if(vpm1.getSubj() != null) {
					int subjIdx = vpm1.getSubj().getPosition();
					for(VerbPhraseMetadata vpm2 : metadata.getVerbMetadata()) {
						for(VerbPhraseToken subjc : vpm2.getSubjC()) {
							if(subjIdx == subjc.getPosition()) {
								data[Headers.SUBJ_SUBJC_EQUAL.ordinal()] = "Y";
								break;
							}
						}
					}
				}
			}
			
			for(Multimap<String, MapValue2> related : structured.data) {
				processMapEntries2(related, data);
			}

			//for(Multimap<String, MapValue> unrelated : structured.unrelated) {
			//	processMapEntries(unrelated, "N", data);
			//}
			
			//for(Multimap<String, MapValue> regex : structured.regex) {
			//	processMapEntries(regex, "N", data);
			//}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processMapEntries2(Multimap<String, MapValue2> map, String[] data) {
		
		if(!map.isEmpty()) {
			boolean write = true;
			
			for(Map.Entry<String, MapValue2> entry : map.entries()) {
				MapValue2 vals = entry.getValue();
				
				data[Headers.RELATED_TO_VERB.ordinal()] = vals.source.equalsIgnoreCase("related") ? "Y" : "N";
				data[Headers.OTHER.ordinal()] = vals.qualifier;
				data[Headers.DEBUG.ordinal()] = vals.debug;
				data[Headers.VERB.ordinal()] = getVerbTemporal(vals.debug);
				data[Headers.ABSENCE.ordinal()] = vals.negated ? "Y" : "";
				data[Headers.NEGATION_SRC.ordinal()] = vals.negSource;
				
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
			    //else if(entry.getKey().equalsIgnoreCase("Diagnostic Procedure"))
			    //	data[Headers.DIAGNOSTIC_PROCEDURE.ordinal()] = vals.value;
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
	
			Collection<MapValue2> coll = map.get("Admin of Drug");
			for(MapValue2 val : coll) {
				data[Headers.OTHER.ordinal()] = val.qualifier;
				data[Headers.DEBUG.ordinal()] = val.debug;
				data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
				data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
				data[Headers.ADMIN_OF_DRUG.ordinal()] = val.value;
				data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
				
				report.writeNext(data);
				write = false;
			}
			
			coll = map.get("Procedure by Method");
			for(MapValue2 val : coll) {
				data[Headers.OTHER.ordinal()] = val.qualifier;
				data[Headers.DEBUG.ordinal()] = val.debug;
				data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
				data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
				data[Headers.PROCEDURE_BY_METHOD.ordinal()] = val.value;
				data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
				
				report.writeNext(data);
				write = false;
			}
			
			coll = map.get("Finding Site");
			for(MapValue2 val : coll) {
				data[Headers.OTHER.ordinal()] = val.qualifier;
				data[Headers.DEBUG.ordinal()] = val.debug;
				data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
				data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
				data[Headers.FINDING_SITE.ordinal()] = val.value;
				data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
				
				report.writeNext(data);
				write = false;
			}
			
			coll = map.get("Diagnostic Procedure");
			for(MapValue2 val : coll) {
				data[Headers.OTHER.ordinal()] = val.qualifier;
				data[Headers.DEBUG.ordinal()] = val.debug;
				data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
				data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
				data[Headers.FINDING_SITE.ordinal()] = val.value;
				data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
				
				report.writeNext(data);
				write = false;
			}
			
			if(write)
		    	report.writeNext(data);
			
			clearHeaderValues(data);
		}
	}
	
	private String getVerbTemporal(String input) {
		String ret = "";
	
		if(input != null && input.length() > 0) {
			String[] temp = input.split("\\|");
			
			if(temp.length > 0 && temp[0].indexOf("_") > -1)
				ret = temp[0];
			else if(temp.length > 1 && temp[1].indexOf("_") > -1)
				ret = temp[1];
		}
		
		return ret;
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
