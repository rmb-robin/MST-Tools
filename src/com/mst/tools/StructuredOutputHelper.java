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
				Multimap<String, MapValue> temp = ArrayListMultimap.create();
				processPrepPhrase2(words, metadata, temp, processedNounPhrases, processedPrepPhrases, i, sentence.getFullSentence(), false, null, "unrelated", negSource);
				if(!temp.isEmpty())
					structured.data.add(temp);
			}
			
			// 3) loop through noun phrases to catch any that aren't grammatically-related to a verb phrase
			negSource.clear();
			for(int i=0; i < metadata.getNounMetadata().size(); i++) {
				Multimap<String, MapValue> temp = ArrayListMultimap.create();
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
							Multimap<String, MapValue> temp = ArrayListMultimap.create();
							temp.put(attribute[0], new MapValue(words.get(i).getToken(), negated ? ABSENCE_QUALIFIER : attribute[1], "|" + rightST, negated, "unrelated", null));
							
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
	
	private void processPrepPhrase2(ArrayList<WordToken> words, SentenceMetadata metadata, Multimap<String, MapValue> results, List<Integer> processedNounPhrases, List<Integer> processedPrepPhrases, int ppIdx, String fullSentence, boolean verbNegated, String verbST, String source, Set<String> negSource) {
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
	
	private void processNounPhrase2(ArrayList<WordToken> words, SentenceMetadata metadata, Multimap<String, MapValue> list, List<Integer> processedNounPhrases, int npIdx, String fullSentence, String leftST, boolean leftNegated, String source, Set<String> negSource) {

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
	
	private void processRegex2(List<Multimap<String, MapValue>> list, String sentence) {
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
				
				Multimap<String, MapValue> mm = ArrayListMultimap.create();
				
				mm.put(DIAP_LABEL, new MapValue(GLEASON_LABEL, SOURCE));
				mm.put(ABSV_LABEL, new MapValue(parseGleasonValue(val), null, Constants.GLEASON_REGEX.toString(), SOURCE));
				
				list.add(mm);
			}
		}

		matcher = chesapeakePSA1.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(2), null, chesapeakePSA1.toString(), SOURCE));
			
			list.add(mm);
		}

		matcher = chesapeakePSA2.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(1), null, chesapeakePSA2.toString(), SOURCE));
			mm.put(DATE_LABEL, new MapValue(matcher.group(3), SOURCE));
			
			list.add(mm);
		}

		matcher = chesapeakePSA3.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL, SOURCE));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(3).trim(), null, chesapeakePSA3.toString(), SOURCE));
			mm.put(DATE_LABEL, new MapValue(matcher.group(1).trim(), SOURCE));
			
			list.add(mm);
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
			
			list.add(mm);
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
	
	private void processMapEntries2(Multimap<String, MapValue> map, String[] data) {
		
		if(!map.isEmpty()) {
			boolean write = true;
			
			for(Map.Entry<String, MapValue> entry : map.entries()) {
				MapValue vals = entry.getValue();
				
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
	
			Collection<MapValue> coll = map.get("Admin of Drug");
			for(MapValue val : coll) {
				data[Headers.OTHER.ordinal()] = val.qualifier;
				data[Headers.DEBUG.ordinal()] = val.debug;
				data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
				data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
				data[Headers.ADMIN_OF_DRUG.ordinal()] = val.value;
				data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
				
				//report.writeNext(data);
				writeData(data);
				write = false;
			}
			
			coll = map.get("Procedure by Method");
			for(MapValue val : coll) {
				data[Headers.OTHER.ordinal()] = val.qualifier;
				data[Headers.DEBUG.ordinal()] = val.debug;
				data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
				data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
				data[Headers.PROCEDURE_BY_METHOD.ordinal()] = val.value;
				data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
				
				//report.writeNext(data);
				writeData(data);
				write = false;
			}
			
			coll = map.get("Finding Site");
			for(MapValue val : coll) {
				data[Headers.OTHER.ordinal()] = val.qualifier;
				data[Headers.DEBUG.ordinal()] = val.debug;
				data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
				data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
				data[Headers.FINDING_SITE.ordinal()] = val.value;
				data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
				
				//report.writeNext(data);
				writeData(data);
				write = false;
			}
			
			coll = map.get("Diagnostic Procedure");
			for(MapValue val : coll) {
				data[Headers.OTHER.ordinal()] = val.qualifier;
				data[Headers.DEBUG.ordinal()] = val.debug;
				data[Headers.VERB.ordinal()] = getVerbTemporal(val.debug);
				data[Headers.ABSENCE.ordinal()] = val.negated ? "Y" : "";
				data[Headers.DIAGNOSTIC_PROCEDURE.ordinal()] = val.value;
				data[Headers.NEGATION_SRC.ordinal()] = val.negSource;
				
				//report.writeNext(data);
				writeData(data);
				write = false;
			}
			
			if(write) {
		    	//report.writeNext(data);
		    	writeData(data);
			}
			
			clearHeaderValues(data);
		}
	}
	
	private void writeData(String[] data) {
		// 11/4/2015
		// this is a quick and dirty way to limit dupes in the structured data report.
		// a better way would be to limit the dupe structured.data entry from being created.
		String str = Joiner.on(',').useForNull("").join(data);
		//if(!processedData.contains(str)) {
		//	report.writeNext(data);
		//	processedData.add(str);
		//}
		// 11/5/2015
		// Jan decided that he doesn't want to limit dupes. He wants to see the duplicate rows and have them filterable.
		if(!processedData.contains(str)) {
			data[Headers.DUPE.ordinal()] = "N";
			processedData.add(str);
		} else {
			data[Headers.DUPE.ordinal()] = "Y";
		}
		
		report.writeNext(data);
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
