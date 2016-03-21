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
import com.mst.model.Finding;
import com.mst.model.GenericToken;
import com.mst.model.MapValue;
import com.mst.model.PrepPhraseToken;
import com.mst.model.Sentence;
import com.mst.model.SentenceMetadata;
import com.mst.model.StructuredData;
import com.mst.model.StructuredData2_0;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.VerbPhraseToken;
import com.mst.model.WordToken;
import com.mst.model.discreet.Meds;
import com.mst.model.discreet.Patient;
import com.mst.util.Constants;
import com.mst.util.Constants.StructuredNotationReturnValue;
import com.mst.util.GsonFactory;
import com.mst.util.Props;
import com.mst.util.Utils;
import com.opencsv.CSVWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class StructuredOutputHelper {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private CSVWriter report;

	private Map<String, Integer> stCounts = new HashMap<>(); // missing semantic type counts report
	private Map<String, Integer> relCounts = new HashMap<>(); // missing relationship counts report
	private Set<String> relByToken = new HashSet<>(); // missing relationships by token
	private Set<String> stByToken = new HashSet<>(); // missing ST by token
	private Set<String> unprocessedSentences = new HashSet<>(); // sentences for which we found no structured data
	private Set<String> foundSTByToken = new HashSet<>();
	private Set<String> processedData = new HashSet<>();
	private final List<String> parentFindings = Arrays.asList("admin of drug", "clinical finding", "diagnostic procedure", "procedure by method", "symptom");
	private Set<String> missingSTs = new HashSet<>();
	private Set<String> missingFTs = new HashSet<>();
	
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
		DUPE,
		SENTENCE;
	};
	
	public StructuredOutputHelper(boolean writeLogs) {
		
		System.out.println("StructuredOutputHelper constructor fired... writeLogs: " + writeLogs);
		
		this.writeLogs = writeLogs;
	}
	
	public StructuredData process2(Sentence sentence, boolean writeToMongo) {
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
		
		StructuredData output = buildStructuredOutput2(sentence, writeToMongo);
		
		if(writeLogs) {
			writeLogs(buildReportPath("scott", "test"));
			try {
				report.close();
			} catch(Exception e) { e.printStackTrace(); }
		}
		return output;
	}
	
	//public void testStructured2_0(Sentence sentence) {
	//	buildStructuredData2_0(sentence, false);
	//}
	
	// this only exists to support showing JSON on the annotation tool website because I don't have
	// time to figure out the Jersey built-in JSON unmarshalling in JAXB
	public String processReturnJSON(Sentence sentence, boolean writeToMongo) {
		return gson.toJson(buildStructuredOutput2(sentence, writeToMongo));
	}
	
	// also used by the web interface. will eventually become the default.
	public String processStructured2_0(Sentence sentence, boolean writeToMongo) {
		return gson.toJson(buildStructuredData2_0(sentence));
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
	
	public void buildStructuredReportOnly(String practice, String study, int limit, boolean includeMeds, List<String> whitelist) {
		CSVWriter structuredReport = null;
		
		try {
			String fileId = practice + "_" + study + "_" + new SimpleDateFormat("MM-dd-yyyy-HH:mm").format(new Date());
			
			try {
				structuredReport = new CSVWriter(new FileWriter(buildReportPath(practice, study) + "structured_" + fileId + ".csv"), ',');
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			String[] headers = new String[Headers.values().length];
			int j = 0;
			for(Headers header : Headers.values()) {
				headers[j] = header.name();
				j++;
			}
			structuredReport.writeNext(headers);
			
			if(includeMeds) {
//					getMeds(practice);
			}
			
			else {
				DBCollection coll = Constants.MongoDB.INSTANCE.getCollection("structured");
				
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
					StructuredData structured = gson.fromJson(obj.toString(), StructuredData.class);
					
					processStructuredEntry(structured, structuredReport, whitelist);
				}
				
				cursor.close();
			}
					
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			//Constants.MongoDB.INSTANCE.close();
			try {
				structuredReport.close();
			} catch(Exception e) { e.printStackTrace(); }
		}
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
		
		//return constructors.get(key);
	}
	
	private StructuredData buildStructuredOutput2(Sentence sentence, boolean writeToMongo) {
		
		StructuredData structured = new StructuredData();
		
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
				Multimap<String, MapValue> related = ArrayListMultimap.create();
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
									String value = "";
									//if(verbPhrase.getSubj().getNounPhraseIdx() != -1) {
										// if SUBJC is within a noun phrase, store entire phrase as value
									//	value = metadata.getNounMetadata().get(verbPhrase.getSubj().getNounPhraseIdx()).getNounPhraseString();
									//	processedNounPhrases.add(verbPhrase.getSubj().getNounPhraseIdx());
									//} else {
										// otherwise, store just the SUBJC token
										value = verbPhrase.getSubj().getToken();
									//}
									related.put(attribute[0], new MapValue(value, verbPhrase.isPhraseNegated() ? ABSENCE_QUALIFIER : attribute[1], subjST + "|" + verbST, verbPhrase.isPhraseNegated(), "related", Joiner.on(',').join(negSource)));
									logFound(verbPhrase.getSubj().getToken(), subjST, subjST+"|"+verbST, verbPhrase.getSubj().getToken()+"|"+verbPhrase.getVerbString(), "VP", sentence.getFullSentence());
								} else {
									// no constructor for this ST pair
									logMissing(relCounts, subjST + "|" + verbST);
									logMissingEx(relByToken, verbPhrase.getSubj().getToken(), verbPhrase.getVerbString(), subjST, verbST, sentence.getFullSentence(), "VP");
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
										related.put(attribute[0], new MapValue(subjc.getToken(), verbPhrase.isPhraseNegated() ? ABSENCE_QUALIFIER : attribute[1], verbST + "|" + subjcST, verbPhrase.isPhraseNegated(), "related", Joiner.on(',').join(negSource)));
										logFound(subjc.getToken(), subjcST, verbST+"|"+subjcST, verbPhrase.getVerbString()+"|"+subjc.getToken(),"VP", sentence.getFullSentence());
									} else {
										logMissing(relCounts, subjcST + "|" + verbST);
										logMissingEx(relByToken, verbPhrase.getVerbString(), subjc.getToken(), verbST, subjcST, sentence.getFullSentence(), "VP");
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
								processPrepPhrase(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), verbST, "related", negSource);
							}
							// 1d) process noun phrases related to subj
							if(verbPhrase.getSubj().getNounPhraseIdx() > -1) {
								processNounPhrase(words, metadata, related, processedNounPhrases, verbPhrase.getSubj().getNounPhraseIdx(), sentence.getFullSentence(), null, false, "related", negSource);
							}
						}
				
						// 1e) process prep phrases related to (final) verb (of phrase)
						for(int ppIdx : verbPhrase.getVerbs().get(verbPhrase.getVerbs().size()-1).getPrepPhrasesIdx()) {
							processPrepPhrase(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), verbST, "related", negSource);
						}
						
						for(VerbPhraseToken subjc : verbPhrase.getSubjC()) {
							// 1f) process prep phrases related to subjc
							for(int ppIdx : subjc.getPrepPhrasesIdx()) {
								processPrepPhrase(words, metadata, related, processedNounPhrases, processedPrepPhrases, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), words.get(subjc.getPosition()).getSemanticType(), "related", negSource);
							}
							
							// 1g) process noun phrases related to subjc
							if(subjc.getNounPhraseIdx() > -1) {
								processNounPhrase(words, metadata, related, processedNounPhrases, subjc.getNounPhraseIdx(), sentence.getFullSentence(), verbST, verbPhrase.isPhraseNegated(), "related", negSource);
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
				Multimap<String, MapValue> temp = ArrayListMultimap.create();
				processPrepPhrase(words, metadata, temp, processedNounPhrases, processedPrepPhrases, i, sentence.getFullSentence(), false, null, "unrelated", negSource);
				if(!temp.isEmpty())
					structured.data.add(temp);
			}
			
			// 3) loop through noun phrases to catch any that aren't grammatically-related to a verb phrase
			negSource.clear();
			for(int i=0; i < metadata.getNounMetadata().size(); i++) {
				Multimap<String, MapValue> temp = ArrayListMultimap.create();
				processNounPhrase(words, metadata, temp, processedNounPhrases, i, sentence.getFullSentence(), null, false, "unrelated", negSource);
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
							Multimap<String, MapValue> temp = ArrayListMultimap.create();
							temp.put(attribute[0], new MapValue(words.get(i).getToken(), negated ? ABSENCE_QUALIFIER : attribute[1], "|" + rightST, negated, "unrelated", null));
							
							structured.data.add(temp);
						}
					}
				}
			}
			
			// 5) process various regex patterns
			processRegex(structured.data, sentence.getFullSentence());
			
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
	
	public Set<String> getMissingSemanticTypes() { return missingSTs; }
	
	public Set<String> getMissingFindingTypes() { return missingFTs; }
	
	private String getSemanticType(ArrayList<WordToken> words, int index, String source) {
		String st = words.get(index).getSemanticType();
		
//		if(st == null) {
//			//missingSTs.add(words.get(index).getToken() + "<>" + source);
//			missingSTs.add(words.get(index).getToken());
//		}
		if(st == null) {
			st = "unk";
		}
		
		return st;
	}
	
	private String getFindingType(String st) {
		String ft = null;
		
		// don't bother looking up verbs; ex. demonstrate_present
		if(st != null && st.indexOf('_') == -1) {
			try(Jedis jedis = Constants.MyJedisPool.INSTANCE.getResource()) {
				ft = jedis.get("st2:"+st);
			}
			
			if(ft == null) {
				//missingFTs.add(st);
				ft = "UNKNOWN";
			}
		}
		
		return ft;
	}
	
	// 12/15/2015 - 3rd attempt at processing structured data without left|right constructors.
	// attempt to enforce a hierarchy to the results. Ex Admin of Drug is always the parent of a Temporal finding
	public List<StructuredData2_0> buildStructuredData2_0(Sentence sentence) {
		
		List<StructuredData2_0> structuredList = new ArrayList<>();
		StructuredData2_0 structured = new StructuredData2_0();
		
		structured.patientId = sentence.getId();
		structured.practice = sentence.getPractice();
		structured.study = sentence.getStudy();
		structured.date = sentence.getProcedureDate();
		structured.sentence = sentence.getFullSentence();
		structured.discreet = sentence.getDiscreet();
		
		// maintain lists of token indexes that have been processed to avoid duplicate findings.
		List<Integer> processedTokens = new ArrayList<Integer>();
		List<Finding> findings = new ArrayList<>();
		
		Set<String> negSource = new HashSet<>();
		
		try {
			SentenceMetadata metadata = sentence.getMetadata();
			
//				/* Structured Metadata */
//				//structured.metadata.put("verbPhraseCount", String.valueOf(metadata.getVerbMetadata().size()));
//				
//				// Subject of a verb phrase equal to another verb phrase's subject complement (object). This is invalid and is tracked for algorithm cleanup purposes.
//				for(VerbPhraseMetadata vpm1 : metadata.getVerbMetadata()) {
//					if(vpm1.getSubj() != null) {
//						int subjIdx = vpm1.getSubj().getPosition();
//						for(VerbPhraseMetadata vpm2 : metadata.getVerbMetadata()) {
//							for(VerbPhraseToken subjc : vpm2.getSubjC()) {
//								if(subjIdx == subjc.getPosition()) {
//									//structured.metadata.put("subjEqSubjC", "Y");
//									break;
//								}
//							}
//						}
//					}
//				}	
//				/* End Structured Metadata */
			
			ArrayList<WordToken> words = sentence.getWordList();
		
			// 1) loop through verb phrases
			for(VerbPhraseMetadata verbPhrase : metadata.getVerbMetadata()) {
				negSource.clear();
				
				// track which component of the verb phrase is negated
				if(verbPhrase.getSubj() != null && verbPhrase.getSubj().isNegated())
					negSource.add("SUBJ");
				for(VerbPhraseToken token : verbPhrase.getSubjC())
					if(token.isNegated())
						negSource.add("SUBJC");
				for(VerbPhraseToken token : verbPhrase.getVerbs())
					if(token.isNegated())
						negSource.add("VB");
				
				switch(verbPhrase.getVerbClass()) {
					case ACTION:
					case LINKING_VERB:
					case VERB_OF_BEING:
					case MODAL_AUX:

						// Step 1: Do a little pre-work to store the semantic types for the verb phrase subject and verb.
						//         Verb phrase subject and verb will each have, at most, one semantic type.
						//         Because there can be more than one subject complement (object) in a verb phrase, each can have its own semantic type.
						String subjST = (verbPhrase.getSubj() != null) ? getSemanticType(words, verbPhrase.getSubj().getPosition(), "VB-SUBJ") : null;
						
						String verbST = null;
						// verbST will be either null, that of the only token, that of the entire phrase (if present), or that of
						// the final token (if entire phrase has no ST)
						if(verbPhrase.getVerbs().size() == 1) {
							verbST = getSemanticType(words, verbPhrase.getVerbs().get(0).getPosition(), "VB");
						} else {
							verbST = verbPhrase.getSemanticType();
							
							if(verbST == null)
								verbST = getSemanticType(words, verbPhrase.getVerbs().get(verbPhrase.getVerbs().size()-1).getPosition(), "VB");
						}
						if(verbST == null || verbST.indexOf('_') == -1) {
							// TODO issue where our verb list (in Constants.java) contains a token that Stanford correctly tags as a non-verb based on context. Ex. "left nodule without change"
							// the underscore check is a bandaid for that issue
							// override ST if none specified
							verbST = verbPhrase.getVerbString() + "_unknown";
						}
					
						// 12/15/2015 - New approach! 
						// No longer require that the verb have a semantic type in order to continue processing of SUBJ and SUBJ (and their related NPs and PPs)
						// This will result in findings that don't have complete verb information. Should be able to find these by querying the database for verbTense = 'unknown' 
						// since the raw verb (phrase) will be stored in the verb field.
						
						List<Finding> parents = new ArrayList<>();
						
						// 1a) generate finding for subj
						if(subjST != null) {
							String type = getFindingType(subjST);
							if(type != null) {
								Finding subject = new Finding(type, verbPhrase.getSubj().getToken(), "VB-SUBJ", negSource, verbST.split("_")[0], verbST.split("_")[1], subjST);	
								
								processedTokens.add(verbPhrase.getSubj().getPosition());
							
								parents.add(subject);
								
								// TODO should these be outside the type != null condition?
								// process noun phrases related to subj
								if(verbPhrase.getSubj().getNounPhraseIdx() > -1) {
									parents = processNounPhrase2_0(words, metadata, parents, processedTokens, verbPhrase.getSubj().getNounPhraseIdx(), sentence.getFullSentence(), verbST, false, "VB-SUBJ", negSource);									
								}
								
								// process prep phrases related to subj
								for(int ppIdx : verbPhrase.getSubj().getPrepPhrasesIdx()) {
									parents = processPrepPhrase2_0(words, metadata, parents, processedTokens, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), verbST, "VB-SUBJ", negSource);
								}
							}
						}
						
						// process prep phrases related to (final) verb (of phrase)
						// Ex. He is on Lupron.
						for(int ppIdx : verbPhrase.getVerbs().get(verbPhrase.getVerbs().size()-1).getPrepPhrasesIdx()) {					
							parents = processPrepPhrase2_0(words, metadata, parents, processedTokens, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), verbST, "VB", negSource);
						}

						// 1b) generate finding for subjc(s)
						for(VerbPhraseToken subjc : verbPhrase.getSubjC()) {
							String subjcST = getSemanticType(words, subjc.getPosition(), "VB-OBJ");
												
							if(subjcST != null) {
								String type = getFindingType(subjcST);
								if(type != null) {
									
									// process noun phrases related to subjc
									if(subjc.getNounPhraseIdx() > -1) {
										parents = processNounPhrase2_0(words, metadata, parents, processedTokens, subjc.getNounPhraseIdx(), sentence.getFullSentence(), verbST, verbPhrase.isPhraseNegated(), "VB-OBJ", negSource);
									} else {
										Finding subjcFinding = new Finding(type, subjc.getToken(), "VB-OBJ", negSource, verbST.split("_")[0], verbST.split("_")[1], subjcST);
										
										processedTokens.add(subjc.getPosition());
										
										if(parents.size() == 0) {
											parents.add(subjcFinding);
										} else {
											for(int i=0; i < parents.size(); i++) {
												if(parentFindings.contains(type.toLowerCase())) {
													subjcFinding.children.add(parents.get(i));
													parents.set(i, subjcFinding);
												} else {
													parents.get(i).children.add(subjcFinding);
												}
											}
										}
									}
									
									// process prep phrases related to subjc
									for(int ppIdx : subjc.getPrepPhrasesIdx()) {
										parents = processPrepPhrase2_0(words, metadata, parents, processedTokens, ppIdx, sentence.getFullSentence(), verbPhrase.isPhraseNegated(), verbST, "VB-OBJ", negSource);
									}
								}
							}
						}
						
						for(Finding parent : parents)
							findings.add(parent);
					
					case PREPOSITIONAL:
					case INFINITIVE:
						
					default:
						
						break;
				}
			}
			
			// 2) loop through noun phrases to catch any that aren't grammatically-related to a verb phrase
			// TODO negation
			negSource.clear();
			for(int i=0; i < metadata.getNounMetadata().size(); i++) {
				// defer the processing of noun phrases that appear within a prep phrase to #3 below
				if(!metadata.getNounMetadata().get(i).isWithinPP()) {
					// this chunk of code will handle random noun phrases and those that are modified by a prep phrase
					List<Finding> temp = new ArrayList<>();
					processNounPhrase2_0(words, metadata, temp, processedTokens, i, sentence.getFullSentence(), "null_null", false, null, negSource);
					
					// process any prep phrases modifying the noun phrase
					for(int j : metadata.getNounMetadata().get(i).getPrepPhrasesIdx()) {
						temp = processPrepPhrase2_0(words, metadata, temp, processedTokens, j, sentence.getFullSentence(), false, "null_null", null, negSource);
					}
					
					for(Finding finding : temp)
						findings.add(finding);
				}
			}
			
			// 3) loop through prep phrases to catch any that aren't grammatically-related to a verb phrase
			negSource.clear();
			for(int i=0; i < metadata.getPrepMetadata().size(); i++) {
				List<Finding> temp = new ArrayList<>();
				
				// determine if token preceding the prep phrase is a finding candidate
				List<PrepPhraseToken> prepPhrase = metadata.getPrepMetadata().get(i).getPhrase();
				int ppPos = prepPhrase.get(0).getPosition();
				
				if(ppPos > 0 && !processedTokens.contains(ppPos-1)) {
					// prep phrase does not begin the fragment AND we haven't already processed the preceding token (by virtue of it being within a noun phrase, for example)
					String precedingToken = words.get(ppPos - 1).getToken();
					String precedingTokenST = getSemanticType(words, ppPos - 1, "PP");
					
					if(Utils.checkForNegation(words, i)) {
						negSource.add("PP");
					}
					
					String type = getFindingType(precedingTokenST);
					if(type != null) {
						Finding child = new Finding(type, precedingToken, "PP", negSource, null, null, precedingTokenST);
						temp.add(child);
						processedTokens.add(ppPos-1);
					}
				}
				
				temp = processPrepPhrase2_0(words, metadata, temp, processedTokens, i, sentence.getFullSentence(), false, "null_null", null, negSource);
				for(Finding finding : temp)
					findings.add(finding);
			}

			negSource.clear();
			
			// 4) Final sweep through the sentence to pick up phrases not bounded by a known type (NP, PP, etc) such as "PSA not stable"

			//if(metadata.getVerbMetadata().isEmpty() && metadata.getPrepMetadata().isEmpty() && metadata.getNounMetadata().isEmpty()) {
				// account for punctuation ending the sentence
				int length = words.get(words.size()-1).isPunctuation() ? words.size() - 1 : words.size(); 
				
				String source = (metadata.getVerbMetadata().isEmpty() && metadata.getPrepMetadata().isEmpty() && metadata.getNounMetadata().isEmpty()) ? "FRAG-NO-METADATA" : "FRAG";
				
				for(int i=0; i < length; i++) {
					if(!processedTokens.contains(i)) {
						String tokenST = getSemanticType(words, i, "FRAG");
	
						// somewhat crude way to detect negation in a fragment.
						// if a token matches a negation signal, note the fragment as negated
						// Ex. "PSA not stable."
						try {
							//if(Constants.NEGATION.matcher(words.get(i-1).getToken()).matches()) {
							if(Utils.checkForNegation(words, i)) {
								negSource.add("FRAG");
							}
						} catch(IndexOutOfBoundsException oob) { }
						
						if(tokenST != null) {
							String type = getFindingType(tokenST);
							
							if(type != null) {
								Finding finding = new Finding(type, words.get(i).getToken(), source, negSource, null, null, tokenST);
								findings.add(finding);
								
								processedTokens.add(i);
							}
						}
					}
				}
			//}

			// 5) process various regex patterns
			processRegex2_0(findings, sentence.getFullSentence());
			
			//structured.findings = findings;
			
			// quick and dirty way to multiple findings into distinct StructuredData2_0 objects
			for(Finding finding : findings) {
				StructuredData2_0 struct = StructuredData2_0.getInstance(structured);
				
				if(Constants.AGE_REGEX.matcher(finding.value.toString()).matches()) {
					String[] arr = finding.value.toString().split("_");
					finding.value = arr[0];
				}
				
				struct.findings.add(finding);
				struct.notation = finding.getNotationString(StructuredNotationReturnValue.VALUE);
				struct.flat = finding.flatten();
				structuredList.add(struct);
			}
			
		} catch(Exception e) {
			logger.error("buildStructuredOutput2(): {}", e);
			logger.error(sentence.getFullSentence());
			e.printStackTrace();
		}
		
		return structuredList;
	}
	
	private List<Finding> processPrepPhrase2_0(ArrayList<WordToken> words, SentenceMetadata metadata, List<Finding> parents, List<Integer> processedTokens, int ppIdx, String fullSentence, boolean verbNegated, String verbST, String source, Set<String> negSource) {
		// in 2.0, prep phrases kind of act like connective tissue between verb components and noun phrases
		
		List<PrepPhraseToken> prepPhrase = metadata.getPrepMetadata().get(ppIdx).getPhrase();
		
		source = (source == null ? "PP" : source + "-PP");
		
		if(metadata.getVerbMetadata().size() == 0)
			source += "-noVB";
		
		String prepTokenST = getSemanticType(words, prepPhrase.get(0).getPosition(), source);
		
		List<Finding> newParents = new ArrayList<>();
		List<Integer> processedNounPhrases = new ArrayList<>();
		
		// loop through each member of the prep phrase after the initial preposition
		for(int i=1; i < prepPhrase.size(); i++) {
			PrepPhraseToken ppToken = prepPhrase.get(i);
			int npIdx = ppToken.getNounPhraseIdx();
			
			// is the token within a noun phrase that we haven't yet processed?
			if(npIdx > -1 && !processedNounPhrases.contains(npIdx)) {
				//int oldCount = countFindings(parents);
				
				//parents = processNounPhrase2_1(words, metadata, parents, processedTokens, npIdx, fullSentence, verbST, false, source, negSource);
				List<Finding> temp = processNounPhrase2_0(words, metadata, parents, processedTokens, npIdx, fullSentence, verbST, false, source, negSource);
				processedNounPhrases.add(npIdx);
				
				// only add to newParents if processNounPhrase2_0() altered the parents list
				//if(countFindings(parents) > oldCount) {
					for(Finding item : temp) {
						newParents.add(item);
					}
				//}
			} else {
				if(words.get(ppToken.getPosition()).isPrepPhraseObject() && // only process PP objects 
				  !processedTokens.contains(ppToken.getPosition())) {       // avoid re-processing a token
					
					String tokenST = getSemanticType(words, ppToken.getPosition(), source);
					String token = words.get(ppToken.getPosition()).getToken();

					if(tokenST != null) {
						String type = getFindingType(tokenST);
						if(type != null) {
							if(metadata.getPrepMetadata().get(ppIdx).isNegated()) {
								negSource.add("PP");
							}
							
							String temp = verbST + "|" + prepTokenST + "|" + tokenST;
							
							Finding child = new Finding(type, token, source, negSource, verbST.split("_")[0], verbST.split("_")[1], temp);
							
							if(parents.size() == 0) {
								newParents.add(child);
							} else {
								for(Finding parent : parents) {
									// the idea here is to reorganize parents/children ONLY if the PP object in question is a top-level finding AND
									// parent hasn't already been set to a top-level finding.
									// Ex. He was given Lupron for cancer. <-- we don't want cancer (found in a PP) to trump Lupron.
									if(parentFindings.contains(type.toLowerCase()) && !parentFindings.contains(parent.type.toLowerCase())) { // PP object is a top-level finding
										child.children.add(parent); // former parent becomes the child of the new finding
										parent = child; // set the new finding as the parent
									} else {
										parent.children.add(child);
									}
									newParents.add(parent);
								}
							}
							processedTokens.add(ppToken.getPosition());
						}
					}
				}
			}
		}
		
		if(newParents.isEmpty())
			return parents;
		else
			return newParents;
	}
	
	private int countFindings(List<Finding> findings) {
		int count = 0;
		for(Finding finding : findings) {
			count++; // account for the parent
			count += getChildCount(count, finding);
		}
		return count;
	}
	
	private int getChildCount(int count, Finding finding) {
		for(Finding child : finding.children) {
			count++;
			return getChildCount(count, child);
		}
		return count;
	}
	
	private List<Finding> processNounPhrase2_0(ArrayList<WordToken> words, SentenceMetadata metadata, List<Finding> parents, List<Integer> processedTokens, int npIdx, String fullSentence, String verbST, boolean verbNegated, String source, Set<String> negSource) {

		if(metadata.getNounMetadata().get(npIdx).isNegated())
			negSource.add("NP");
		
		source = (source == null ? "NP" : source + "-NP");

		if(metadata.getVerbMetadata().size() == 0)
			source += "-noVB";
		
		List<GenericToken> nounPhrase = metadata.getNounMetadata().get(npIdx).getPhrase();
		//Finding parent = null;
		
//		GenericToken head = nounPhrase.get(nounPhrase.size()-1);
//		String headST = words.get(nounPhrase.get(nounPhrase.size()-1).getPosition()).getSemanticType();
		
		// NP head has a semantic type and we haven't already processed it (perhaps by virtue of it being a SUBJ or SUBJC of a verb phrase)
/*		if(headST != null && !processedTokens.contains(head.getPosition())) {
			String type = getAttributeFromRedis(headST);
			if(type != null) {
				// TODO verb and verbTense
				Finding parent = new Finding(type, head.getToken(), source, negSource, "fix me", "fix me", headST);
				parents.add(parent);
				processedTokens.add(head.getPosition());
			}
		}
*/
		Finding npFinding = null;
		
		// process noun phrase members independently with regard to parent/child nesting.
		// e.g. if the phrase consists of three tokens and the first and third modify the second, build a Finding object representative of that relationship.
		// after the noun phrase is processed, determine if its parent should become a child of the incoming parents list or become the new parent.
		
		for(int i=nounPhrase.size()-1; i >= 0; i--) {
			GenericToken token = nounPhrase.get(i);
			if(!processedTokens.contains(token.getPosition())) { // avoid re-processing a token
				String tokenST = getSemanticType(words, token.getPosition(), source);
				
				if(tokenST != null) {
					String type = getFindingType(tokenST);
					if(type != null) {
						Finding child = new Finding(type, token.getToken(), source, negSource, verbST.split("_")[0], verbST.split("_")[1], tokenST);
						
						processedTokens.add(token.getPosition());
						
						if(npFinding == null) {
							npFinding = child;
						} else {
							//for(int j=0; j < tempParent.size(); j++) {
								if(parentFindings.contains(type.toLowerCase())) {
									child.children.add(npFinding);
									npFinding = child;
								} else {
									npFinding.children.add(child);
								}
							//}
						}
					}
				}
			}
		}
		
		if(npFinding != null) {
			if(parents.size() == 0) {
				parents.add(npFinding);
			} else {
				// should the for loop be outside this if? don't we want it to apply to all parents?
				if(parentFindings.contains(npFinding.type.toLowerCase()) && !parentFindings.contains(parents.get(0).type.toLowerCase())) {
					// we've processed the noun phrase and determined that its top-level parent should be the parent of any previous findings	
					List<Finding> newParents = new ArrayList<>();
					for(Finding child : parents) {
						npFinding.children.add(child);
						newParents.add(npFinding);
					}
					parents = newParents;
				} else {
					for(int i=0; i < parents.size(); i++) { // using (Finding finding : parents) here doesn't retain changes when the parent object is returned 
						parents.get(i).children.add(npFinding);
					}
				}
			}
		}
		
		return parents;
	}
	
	private void processPrepPhrase(ArrayList<WordToken> words, SentenceMetadata metadata, Multimap<String, MapValue> results, List<Integer> processedNounPhrases, List<Integer> processedPrepPhrases, int ppIdx, String fullSentence, boolean verbNegated, String verbST, String source, Set<String> negSource) {
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
									results.put(attribute[0], new MapValue(metadata.getNounMetadata().get(npIdx).getNounPhraseString(), negated ? ABSENCE_QUALIFIER : (qualifier != null ? qualifier : attribute[1]), newPrepTokenST + "|" + objTokenST, negated, source, Joiner.on(',').join(negSource)));
									processedNounPhrases.add(npIdx);
									// exit the for so as to avoid processing the individual NP tokens
									break;
								} else {
									boolean negated = metadata.getPrepMetadata().get(ppIdx).isNegated() || verbNegated;
									results.put(attribute[0], new MapValue(ppToken.getToken(), negated ? ABSENCE_QUALIFIER : (qualifier != null ? qualifier : attribute[1]), newPrepTokenST + "|" + objTokenST, negated, source, Joiner.on(',').join(negSource)));
								}
								
							} else {
								// TODO if npIdx > 1 possibly query individual tokens against the preposition if no result from full phrase
								logMissing(relCounts, prepTokenST + "|" + objTokenST);
								logMissingEx(relByToken, prepPhrase.get(0).getToken(), objToken, prepTokenST, objTokenST, fullSentence, "PP");
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
	
	private void processNounPhrase(ArrayList<WordToken> words, SentenceMetadata metadata, Multimap<String, MapValue> list, List<Integer> processedNounPhrases, int npIdx, String fullSentence, String leftST, boolean leftNegated, String source, Set<String> negSource) {

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
					list.put(attribute[0], new MapValue(npString, negated ? ABSENCE_QUALIFIER : attribute[1], leftST + "|" + npST, negated, source, Joiner.on(',').join(negSource)));
					processedNounPhrases.add(npIdx);
					logFound(npString, npST, leftST+"|"+npST, "", "NP", fullSentence);
				} else {
					// catch situation such as "no bone pain" fragment. on hold because there is no way to know which structured attribute to use (e.g. Clinical Finding).
					// possibly the only way to handle this is with a constructor.
					//if(metadata.getNounMetadata().get(npIdx).isNegated()) {
					//	list.put(attribute[0], new MapValue(npString, attribute[1], leftST + "|" + npST, true));
					//}
					logMissing(relCounts, leftST + "|" + npST);
					logMissingEx(relByToken, "", npString, leftST, npST, fullSentence, "NP");
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
								
								list.put(attribute[0], new MapValue(nounPhrase.get(i).getToken(), negated ? ABSENCE_QUALIFIER : attribute[1], tokenST + "|" + finalTokenST, negated, source, Joiner.on(',').join(negSource)));
								processedNounPhrases.add(npIdx);
								logFound(nounPhrase.get(i).getToken(), tokenST, tokenST+"|"+finalTokenST, nounPhrase.get(i).getToken()+"|"+finalToken, "NP", fullSentence);
							} else {
								logMissing(relCounts, tokenST + "|" + finalTokenST);
								logMissingEx(relByToken, nounPhrase.get(i).getToken(), finalToken, tokenST, finalTokenST, fullSentence, "NP");
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
								list.put(attribute[0], new MapValue(npToken.getToken(), negated ? ABSENCE_QUALIFIER : attribute[1], "|" + rightST, negated, source, Joiner.on(',').join(negSource)));
								processedNounPhrases.add(npIdx);
							}
						}
					}
					
				}
			}
		}
	}
	
	public void processRegex2_0(List<Finding> findings, String sentence) {
		// PSA/Gleason regex processing. Find instances that could not be picked up by a constructor.
		
		Pattern psaRegex1 = Pattern.compile("PSA\\s*\\d\\d\\/\\d\\d\\/\\d\\d:?\\s*<?\\d\\d?\\.?\\d{1,2}"); //PSA 09/26/13: 0.46PSA 01/29/14: 0.18PSA 06/19/14: 0.05.
		//Pattern psaRegex2 = Pattern.compile("PSA\\s*of\\s*\\d?\\d\\.\\d+\\s*((on|in)\\s*(\\d\\d\\/\\d\\d\\/\\d{2}|\\d{4}))?");
		//Pattern chesapeakePSA1 = Pattern.compile("(?i)PSA(\\s*\\(Most Recent\\))?\\s*\\(\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*\\)"); // PSA (Most Recent) (5.4)   PSA (2.5)   PSA (Most Recent) (undetectable)  PSA 0.27 (skyline)   https://www.regex101.com/r/dV6zN8/1
		                                          //(?i)PSA(\\s*\\(Most Recent\\))?\\s*\\(\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*\\)
		Pattern chesapeakePSA1 = Pattern.compile(  "(?i)PSA(\\s*\\(Most Recent\\))?\\s*\\(\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*\\)"); // PSA (Most Recent) (5.4)   PSA (2.5)   PSA (Most Recent) (undetectable)  PSA 0.27 (skyline)   https://www.regex101.com/r/dV6zN8/1
		Pattern chesapeakePSA2 = Pattern.compile("PSA=\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*(ng\\/ml collected|from)?\\s*(\\d\\d?\\/\\d\\d?\\/\\d{2,4})"); // PSA=12.9 ng/ml collected 12/11/13   PSA=0.8 from 1/27/14     https://www.regex101.com/r/lO3fI5/1
		Pattern chesapeakePSA3 = Pattern.compile("(\\d\\d?\\/\\d\\d?\\/\\d{2,4}):\\s*(-->)?\\s*PSA=\\s*(\\d\\d?\\.?\\d\\d?)"); // 11/11/11: --> PSA= 2.5   12/04/08: PSA=2.2ng/ml   https://www.regex101.com/r/qF6xD5/1
		Pattern chesapeakePSA4 = Pattern.compile("PSA\\s*\\(?(\\d\\d?\\.?\\d\\d?)( ng\\/ml)?\\s*(on )?(\\d\\d?\\/\\d\\d?\\/\\d{2,4}|\\d\\d?\\/\\d\\d?)"); // https://www.regex101.com/r/bO8oC6/2
		Pattern chesapeakePSA5 = Pattern.compile("PSA\\s*(level of|of|down to|up to|is|was|stable at)\\s*(\\d\\d?\\.?\\d\\d?)(?!\\/)(\\s*(on|in|from))?(\\s*\\d\\d?\\/\\d\\d?\\/\\d{2,4}|\\s*\\d\\d?\\/\\d{2,4})?");
		
		final String GLEASON_LABEL = "Gleason";
		final String PSA_LABEL = "PSA";
		final String DIAP_LABEL = "Diagnostic Procedure";
		final String ABSV_LABEL = "Absolute Value";
		final String DATE_LABEL = "Known Event Date";
		
		final String SOURCE = "REGEX";
		
		Matcher matcher = Constants.GLEASON_REGEX.matcher(sentence);

		while(matcher.find()) {
			String val = matcher.group(matcher.groupCount()); // get last group, which should be the Gleason value
			if(val != null) {
				val = val.trim();
				
				//Multimap<String, MapValue> mm = ArrayListMultimap.create();
				//mm.put(DIAP_LABEL, new MapValue(GLEASON_LABEL, SOURCE));
				//mm.put(ABSV_LABEL, new MapValue(parseGleasonValue(val), null, Constants.GLEASON_REGEX.toString(), SOURCE));
				
				Finding finding = new Finding(DIAP_LABEL, GLEASON_LABEL, SOURCE, null, null, null, Constants.GLEASON_REGEX.toString());
				finding.children.add(new Finding(ABSV_LABEL, parseGleasonValue(val), SOURCE, null, null, null, null));
				
				findings.add(finding);
			}
		}

		matcher = chesapeakePSA1.matcher(sentence);

		while(matcher.find()) {
			//Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			if(matcher.groupCount() == 2) {
				//mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
				//mm.put(ABSV_LABEL, new MapValue(matcher.group(2), null, chesapeakePSA1.toString(), SOURCE));
				//findings.add(mm);
				
				Finding finding = new Finding(DIAP_LABEL, PSA_LABEL, SOURCE, null, null, null, chesapeakePSA1.toString());
				finding.children.add(new Finding(ABSV_LABEL, matcher.group(2).trim(), SOURCE, null, null, null, null));
				
				findings.add(finding);
			}
		}

		matcher = chesapeakePSA2.matcher(sentence);

		while(matcher.find()) {
			//Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			//mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
			//mm.put(ABSV_LABEL, new MapValue(matcher.group(1), null, chesapeakePSA2.toString(), SOURCE));
			//mm.put(DATE_LABEL, new MapValue(matcher.group(3), SOURCE));
			//findings.add(mm);
			
			Finding finding = new Finding(DIAP_LABEL, PSA_LABEL, SOURCE, null, null, null, chesapeakePSA2.toString());
			finding.children.add(new Finding(ABSV_LABEL, matcher.group(1).trim(), SOURCE, null, null, null, null));
			finding.children.add(new Finding(DATE_LABEL, matcher.group(3).trim(), SOURCE, null, null, null, null));
			
			findings.add(finding);
		}

		matcher = chesapeakePSA3.matcher(sentence);

		while(matcher.find()) {
			//Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			//mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
			//mm.put(ABSV_LABEL, new MapValue(matcher.group(3).trim(), null, chesapeakePSA3.toString(), SOURCE));
			//mm.put(DATE_LABEL, new MapValue(matcher.group(1).trim(), SOURCE));
			//findings.add(mm);
			
			Finding finding = new Finding(DIAP_LABEL, PSA_LABEL, SOURCE, null, null, null, chesapeakePSA3.toString());
			finding.children.add(new Finding(ABSV_LABEL, matcher.group(3).trim(), SOURCE, null, null, null, null));
			finding.children.add(new Finding(DATE_LABEL, matcher.group(1).trim(), SOURCE, null, null, null, null));
			
			findings.add(finding);
		}

		matcher = chesapeakePSA4.matcher(sentence);

		while(matcher.find()) {
//			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
//			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
//			mm.put(ABSV_LABEL, new MapValue(matcher.group(1).trim(), null, chesapeakePSA4.toString(), SOURCE));
//			if(matcher.groupCount() > 2)
//				mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim(), SOURCE));
//			//try {
//			//	mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim()));
//			//} catch(Exception e) { System.out.println(e.toString() + "\n\t" + sentence + "\n\t" + chesapeakePSA4.toString()); }
//			
//			findings.add(mm);
			
			Finding finding = new Finding(DIAP_LABEL, PSA_LABEL, SOURCE, null, null, null, chesapeakePSA4.toString());
			finding.children.add(new Finding(ABSV_LABEL, matcher.group(1).trim(), SOURCE, null, null, null, null));
			if(matcher.groupCount() > 2)
				finding.children.add(new Finding(DATE_LABEL, matcher.group(matcher.groupCount()).trim(), SOURCE, null, null, null, null));
			
			findings.add(finding);
		}

		matcher = chesapeakePSA5.matcher(sentence);

		while(matcher.find()) {
//			Multimap<String, MapValue> mm = ArrayListMultimap.create();
//			
//			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
//			mm.put(ABSV_LABEL, new MapValue(matcher.group(2).trim(), null, chesapeakePSA5.toString(), SOURCE));
//			//for(int i=0; i <= matcher.groupCount(); i++) {
//			//	System.out.println(i + " - " + matcher.group(i));
//			//}
//			if(matcher.group(matcher.groupCount()) != null)
//				mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim(), SOURCE));
//			
//			findings.add(mm);
			
			Finding finding = new Finding(DIAP_LABEL, PSA_LABEL, SOURCE, null, null, null, chesapeakePSA5.toString());
			finding.children.add(new Finding(ABSV_LABEL, matcher.group(2).trim(), SOURCE, null, null, null, null));
			if(matcher.group(matcher.groupCount()) != null)
				finding.children.add(new Finding(DATE_LABEL, matcher.group(matcher.groupCount()).trim(), SOURCE, null, null, null, null));
			
			findings.add(finding);
		}
		
		matcher = Constants.SKYLINE_PSA_1.matcher(sentence);

		while(matcher.find()) {
//			Multimap<String, MapValue> mm = ArrayListMultimap.create();
//			
//			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
//			mm.put(ABSV_LABEL, new MapValue(matcher.group(2).trim(), null, Constants.SKYLINE_PSA_1.toString(), SOURCE));
//			mm.put(DATE_LABEL, new MapValue(matcher.group(1).trim(), SOURCE));
//			
//			findings.add(mm);
			
			Finding finding = new Finding(DIAP_LABEL, PSA_LABEL, SOURCE, null, null, null, Constants.SKYLINE_PSA_1.toString());
			finding.children.add(new Finding(ABSV_LABEL, matcher.group(2).trim(), SOURCE, null, null, null, null));
			finding.children.add(new Finding(DATE_LABEL, matcher.group(1).trim(), SOURCE, null, null, null, null));
			
			findings.add(finding);
		}
		
		matcher = Constants.SKYLINE_PSA_2.matcher(sentence);

		while(matcher.find()) {
//			Multimap<String, MapValue> mm = ArrayListMultimap.create();
//			
//			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
//			mm.put(ABSV_LABEL, new MapValue(matcher.group(2).trim(), null, Constants.SKYLINE_PSA_2.toString(), SOURCE));
//			mm.put(DATE_LABEL, new MapValue(matcher.group(1).trim(), SOURCE));
//			
//			findings.add(mm);
			
			Finding finding = new Finding(DIAP_LABEL, PSA_LABEL, SOURCE, null, null, null, Constants.SKYLINE_PSA_2.toString());
			finding.children.add(new Finding(ABSV_LABEL, matcher.group(2).trim(), SOURCE, null, null, null, null));
			finding.children.add(new Finding(DATE_LABEL, matcher.group(1).trim(), SOURCE, null, null, null, null));
			
			findings.add(finding);
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

	public void processRegex(List<Multimap<String, MapValue>> findings, String sentence) {
		// PSA/Gleason regex processing. Find instances that could not be picked up by a constructor.
		
		Pattern psaRegex1 = Pattern.compile("PSA\\s*\\d\\d\\/\\d\\d\\/\\d\\d:?\\s*<?\\d\\d?\\.?\\d{1,2}"); //PSA 09/26/13: 0.46PSA 01/29/14: 0.18PSA 06/19/14: 0.05.
		//Pattern psaRegex2 = Pattern.compile("PSA\\s*of\\s*\\d?\\d\\.\\d+\\s*((on|in)\\s*(\\d\\d\\/\\d\\d\\/\\d{2}|\\d{4}))?");
		//Pattern chesapeakePSA1 = Pattern.compile("(?i)PSA(\\s*\\(Most Recent\\))?\\s*\\(\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*\\)"); // PSA (Most Recent) (5.4)   PSA (2.5)   PSA (Most Recent) (undetectable)  PSA 0.27 (skyline)   https://www.regex101.com/r/dV6zN8/1
		                                          //(?i)PSA(\\s*\\(Most Recent\\))?\\s*\\(\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*\\)
		Pattern chesapeakePSA1 = Pattern.compile(  "(?i)PSA(\\s*\\(Most Recent\\))?\\s*\\(\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*\\)"); // PSA (Most Recent) (5.4)   PSA (2.5)   PSA (Most Recent) (undetectable)  PSA 0.27 (skyline)   https://www.regex101.com/r/dV6zN8/1
		Pattern chesapeakePSA2 = Pattern.compile("PSA=\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*(ng\\/ml collected|from)?\\s*(\\d\\d?\\/\\d\\d?\\/\\d{2,4})"); // PSA=12.9 ng/ml collected 12/11/13   PSA=0.8 from 1/27/14     https://www.regex101.com/r/lO3fI5/1
		Pattern chesapeakePSA3 = Pattern.compile("(\\d\\d?\\/\\d\\d?\\/\\d{2,4}):\\s*(-->)?\\s*PSA=\\s*(\\d\\d?\\.?\\d\\d?)"); // 11/11/11: --> PSA= 2.5   12/04/08: PSA=2.2ng/ml   https://www.regex101.com/r/qF6xD5/1
		Pattern chesapeakePSA4 = Pattern.compile("PSA\\s*\\(?(\\d\\d?\\.?\\d\\d?)( ng\\/ml)?\\s*(on )?(\\d\\d?\\/\\d\\d?\\/\\d{2,4}|\\d\\d?\\/\\d\\d?)"); // https://www.regex101.com/r/bO8oC6/2
		Pattern chesapeakePSA5 = Pattern.compile("PSA\\s*(level of|of|down to|up to|is|was|stable at)\\s*(\\d\\d?\\.?\\d\\d?)(?!\\/)(\\s*(on|in|from))?(\\s*\\d\\d?\\/\\d\\d?\\/\\d{2,4}|\\s*\\d\\d?\\/\\d{2,4})?");
		
		final String GLEASON_LABEL = "Gleason";
		final String PSA_LABEL = "PSA";
		final String DIAP_LABEL = "Diagnostic Procedure";
		final String ABSV_LABEL = "Absolute Value";
		final String DATE_LABEL = "Known Event Date";
		
		final String SOURCE = "REGEX";
		
		Matcher matcher = Constants.GLEASON_REGEX.matcher(sentence);

		while(matcher.find()) {
			String val = matcher.group(matcher.groupCount()); // get last group, which should be the Gleason value
			if(val != null) {
				val = val.trim();
				
				Multimap<String, MapValue> mm = ArrayListMultimap.create();
				
				mm.put(DIAP_LABEL, new MapValue(GLEASON_LABEL, SOURCE));
				mm.put(ABSV_LABEL, new MapValue(parseGleasonValue(val), null, Constants.GLEASON_REGEX.toString(), SOURCE));
				
				findings.add(mm);
			}
		}

		matcher = chesapeakePSA1.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			if(matcher.groupCount() == 2) {
				mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
				mm.put(ABSV_LABEL, new MapValue(matcher.group(2), null, chesapeakePSA1.toString(), SOURCE));
			
				findings.add(mm);
			}
		}

		matcher = chesapeakePSA2.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(1), null, chesapeakePSA2.toString(), SOURCE));
			mm.put(DATE_LABEL, new MapValue(matcher.group(3), SOURCE));
			
			findings.add(mm);
		}

		matcher = chesapeakePSA3.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(3).trim(), null, chesapeakePSA3.toString(), SOURCE));
			mm.put(DATE_LABEL, new MapValue(matcher.group(1).trim(), SOURCE));
			
			findings.add(mm);
		}

		matcher = chesapeakePSA4.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(1).trim(), null, chesapeakePSA4.toString(), SOURCE));
			if(matcher.groupCount() > 2)
				mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim(), SOURCE));
			//try {
			//	mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim()));
			//} catch(Exception e) { System.out.println(e.toString() + "\n\t" + sentence + "\n\t" + chesapeakePSA4.toString()); }
			
			findings.add(mm);
		}

		matcher = chesapeakePSA5.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(2).trim(), null, chesapeakePSA5.toString(), SOURCE));
			//for(int i=0; i <= matcher.groupCount(); i++) {
			//	System.out.println(i + " - " + matcher.group(i));
			//}
			if(matcher.group(matcher.groupCount()) != null)
				mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim(), SOURCE));
			
			findings.add(mm);
		}
		
		matcher = Constants.SKYLINE_PSA_1.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(2).trim(), null, Constants.SKYLINE_PSA_1.toString(), SOURCE));
			mm.put(DATE_LABEL, new MapValue(matcher.group(1).trim(), SOURCE));
			
			findings.add(mm);
		}
		
		matcher = Constants.SKYLINE_PSA_2.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(2).trim(), null, Constants.SKYLINE_PSA_2.toString(), SOURCE));
			mm.put(DATE_LABEL, new MapValue(matcher.group(1).trim(), SOURCE));
			
			findings.add(mm);
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
	
	private void logMissingEx(Set<String> set, String leftToken, String rightToken, String leftST, String rightST, String sentence, String type) {
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
	
	private void processStructuredEntry(StructuredData structured, CSVWriter csv, List<String> whitelist) {
		try {
			String[] data = new String[Headers.values().length];
			Arrays.fill(data, "");
			
			data[Headers.PATIENT_ID.ordinal()] = structured.patientId;
			data[Headers.VISIT_DATE.ordinal()] = structured.date != null ? sdf.format(structured.date) : "";
			data[Headers.SENTENCE.ordinal()] = structured.sentence;

			for(Multimap<String, MapValue> related : structured.data) {
				processMapEntries2(related, data, csv, true, whitelist);
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void report2(StructuredData structured, SentenceMetadata metadata) {
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
			
			for(Multimap<String, MapValue> related : structured.data) {
				processMapEntries2(related, data, report, false, new ArrayList<String>());
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
	
	private void processMapEntries2(Multimap<String, MapValue> map, String[] data, CSVWriter csv, boolean noDupes, List<String> whitelist) {
		
		if(!map.isEmpty()) {
			boolean write = false;
			
			for(Map.Entry<String, MapValue> entry : map.entries()) {
				MapValue vals = entry.getValue();
				
				if(whitelist.isEmpty() || whitelist.contains(entry.getKey().toLowerCase())) {
					write = true;
					
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
			}
	
			Collection<MapValue> coll;
			
			if(whitelist.isEmpty() || whitelist.contains("admin of drug")) {
				coll = map.get("Admin of Drug");
				for(MapValue val : coll) {
					data[Headers.OTHER.ordinal()] = val.qualifier;
					data[Headers.DEBUG.ordinal()] = val.debug;
					data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
					data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
					data[Headers.ADMIN_OF_DRUG.ordinal()] = val.value;
					data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
					
					writeData(data, csv, noDupes);
					//write = false;
				}
			}
			
			if(whitelist.isEmpty() || whitelist.contains("procedure by method")) {
				coll = map.get("Procedure by Method");
				for(MapValue val : coll) {
					data[Headers.OTHER.ordinal()] = val.qualifier;
					data[Headers.DEBUG.ordinal()] = val.debug;
					data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
					data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
					data[Headers.PROCEDURE_BY_METHOD.ordinal()] = val.value;
					data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
					
					writeData(data, csv, noDupes);
					//write = false;
				}
			}
			
			if(whitelist.isEmpty() || whitelist.contains("finding site")) {
				coll = map.get("Finding Site");
				for(MapValue val : coll) {
					data[Headers.OTHER.ordinal()] = val.qualifier;
					data[Headers.DEBUG.ordinal()] = val.debug;
					data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
					data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
					data[Headers.FINDING_SITE.ordinal()] = val.value;
					data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
					
					writeData(data, csv, noDupes);
					//write = false;
				}
			}
			
			if(whitelist.isEmpty() || whitelist.contains("diagnostic procedure")) {
				coll = map.get("Diagnostic Procedure");
				for(MapValue val : coll) {
					data[Headers.OTHER.ordinal()] = val.qualifier;
					data[Headers.DEBUG.ordinal()] = val.debug;
					data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
					data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
					data[Headers.DIAGNOSTIC_PROCEDURE.ordinal()] = val.value;
					data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
					
					writeData(data, csv, noDupes);
					//write = false;
				}
			}
			if(write) {
		    	writeData(data, csv, noDupes);
			}
			
			clearHeaderValues(data);
		}
	}
	
	private void writeData(String[] data, CSVWriter csv, boolean noDupes) {
		String str = Joiner.on(',').useForNull("").join(data);
		if(noDupes) {
			// 11/4/2015
			// this is a quick and dirty way to limit dupes in the structured data report.
			// a better way would be to limit the dupe structured.data entry from being created in the first place.
			if(!processedData.contains(str)) {
				csv.writeNext(data);
				processedData.add(str);
			}
		} else {
			// 11/5/2015
			// Jan decided that he doesn't want to limit dupes. He wants to see the duplicate rows and have them filterable.
			if(!processedData.contains(str)) {
				data[Headers.DUPE.ordinal()] = "N";
				processedData.add(str);
			} else {
				data[Headers.DUPE.ordinal()] = "Y";
			}
			csv.writeNext(data);
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

//public StructuredData processST(Sentence sentence, boolean writeToMongo) {
//// this is used by the camel process
////System.out.println("Calling StructuredOutputHelper.process2() for sentence: " + sentence.getFullSentence());
//
//if(writeLogs) {
//	initReports("scott", "test");
//	
//	String[] headers = new String[Headers.values().length];
//	int j = 0;
//	for(Headers header : Headers.values()) {
//		headers[j] = header.name();
//		j++;
//	}
//	report.writeNext(headers);
//}
//
//StructuredData output = buildStructuredData(sentence, writeToMongo);
//
//if(writeLogs) {
//	writeLogs(buildReportPath("scott", "test"));
//	try {
//		report.close();
//	} catch(Exception e) { e.printStackTrace(); }
//}
//return output;
//}
