package com.mst.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mst.model.GenericToken;
import com.mst.model.NounPhraseMetadata;
import com.mst.model.PrepPhraseMetadata;
import com.mst.model.PrepPhraseToken;
import com.mst.model.SemanticType;
import com.mst.model.Sentence;
import com.mst.model.SentenceMetadata;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.ontology.Finding;
import com.mst.model.ontology.FindingSite;
import com.mst.model.ontology.Findings;
import com.mst.util.Constants;
import com.mst.util.GsonFactory;
import com.mst.util.PostgreSQL;

public class StructuredOutputHelper {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public StructuredOutputHelper() { }
	
	public void buildStructuredOutput() {
		
		PostgreSQL postgres = new PostgreSQL();
		Gson gson = GsonFactory.build();
		
		long startTime = getTime();
		
		try {
			MongoClient mongoClient = new MongoClient("mongo01.medicalsearchtechnologies.com");
			DB db = mongoClient.getDB("arrowhead-phase2-processed");
			Boolean auth = db.authenticate("arrowhead", "Arrowhead4327*".toCharArray());
		
			DBCollection coll = db.getCollection("processed_imaging");
			
			DBCursor cursor = null;
			
			BasicDBObject query = new BasicDBObject("_id", new ObjectId("54fe46e6e4b0cd89c18a1bba"));
			cursor = coll.find();
			
			postgres.connect("constructors");
			ArrayList<Findings> results = new ArrayList<Findings>();
			
			while(cursor.hasNext()) {
				BasicDBObject obj = (BasicDBObject) cursor.next();

				Sentence sentence = gson.fromJson(obj.toString(), Sentence.class);
				//System.out.println(obj.toString());
//				Annotator ann = new Annotator();
//				ArrayList<Sentence> sentences = ann.annotate("Of note, there are multilevel degenerative changes within the thoracolumbar spine with more severe degenerative change at L5-S1 where there is moderate narrowing of the spinal canal and neural foramina.");
//				sentence = sentences.get(0);
//				MetadataParser mp = new MetadataParser();
//				mp.parseComplex(sentence);

				SentenceMetadata metadata = sentence.getMetadata();
				
				Findings findings = null;
				
				for(VerbPhraseMetadata phrase : metadata.getVerbMetadata()) {
					switch(phrase.getVerbClass()) {
						case ACTION:
						case LINKING_VERB:
						case VERB_OF_BEING:
							if(phrase.getSubj() != null && phrase.getVerb() != null && phrase.getSubjC() != null) {
								int subjIdx = phrase.getSubj().getPosition();
								int verbIdx = phrase.getVerb().getPosition();
								int subjcIdx = phrase.getSubjC().getPosition();
								ArrayList<SemanticType> subjST = sentence.getWordList().get(subjIdx).getSemanticTypeList();
								ArrayList<SemanticType> verbST = sentence.getWordList().get(verbIdx).getSemanticTypeList();
								ArrayList<SemanticType> subjcST = sentence.getWordList().get(subjcIdx).getSemanticTypeList();
								String[] values = null;
								
								// TODO
								// to overcome issue of multiple STs per token, possibly scan tokens for STs present in database,
								// or use a hard-coded list such as bpoc, bdsy, etc.
								
								// attempt to query by semantic type if not null or empty
								if(subjST.isEmpty() || subjcST.isEmpty()) {
									logger.info("Verb phrase missing a semantic type: {}\n{{}({}), {}({}), {}({})}", sentence.getFullSentence(), phrase.getSubj().getToken(), subjST.isEmpty() ? "" : subjST.get(0).getSemanticType(), phrase.getVerb().getToken(), verbST.isEmpty() ? "" : verbST.get(0).getSemanticType(), phrase.getSubjC().getToken(), subjcST.isEmpty() ? "" : subjcST.get(0).getSemanticType());
									
									if(phrase.getSubj().getToken().equalsIgnoreCase("There")) {
										subjST.add(new SemanticType("ther", "there"));
									}
								}
								
								if(subjST.isEmpty() || subjcST.isEmpty())
									continue;
								
								values = postgres.getVerbRelationshipData(subjST.get(0).getSemanticType(), phrase.getVerb().getToken(), subjcST.get(0).getSemanticType(), phrase.getSubjC().isNegated());

								if(values == null) {
									// no database result from ST query
									logger.info("No constructor entry for verb phrase: {}\n{{}({}), {}({}), {}({})}", sentence.getFullSentence(), phrase.getSubj().getToken(), subjST.get(0).getSemanticType(), phrase.getVerb().getToken(), "X", phrase.getSubjC().getToken(), subjcST.get(0).getSemanticType());
								} else {
									findings = new Findings();
									findings.fullSentence = sentence.getFullSentence();
									findings.date = sentence.getProcessDate();
									
									Finding finding = new Finding();
									
									finding._class = values[3];
									finding.value = values[4];
									finding.parent = values[5];
									finding.match = Arrays.toString(values);
									
									String findingSiteLocation = values[6];
									
									FindingSite site = new FindingSite();
									
									if(findingSiteLocation.equalsIgnoreCase("SUBJ")) {
										site.value = phrase.getSubj().getToken();
										
										if(phrase.getSubj().getNounPhraseIdx() > -1) {
											// subj is within a noun phrase
											setNounPhraseAttributes(site, phrase.getSubj().getNounPhraseIdx(), metadata.getNounMetadata());
										}
										
									} else if(findingSiteLocation.equalsIgnoreCase("PREP")) {
										// check the prep phrase modifying the subjc
										if(phrase.getSubjC().getPrepPhrasesIdx() != null) {											
											for(int ppIdx : phrase.getSubjC().getPrepPhrasesIdx()) {
												// get metadata object for this prep phrase index
												PrepPhraseMetadata ppmd = metadata.getPrepMetadata().get(ppIdx);
												
												// get the last token in the prep phrase
												PrepPhraseToken token = ppmd.getPhrase().get(ppmd.getPhrase().size()-1); 
												
												// if the last token of the prep phrase is a body part or body structure
												if(sentence.getWordList().get(token.getPosition()).containsSemanticType("(?i)bpoc|bdsy")) {
													site.value = token.getToken();
													
													if(token.getNounPhraseIdx() > -1) {
														setNounPhraseAttributes(site, token.getNounPhraseIdx(), metadata.getNounMetadata());
													}
													break;
												}
											}
										} else {
											site.value = "UNKNOWN - SubjC not modified by a PP";
										}
									}
									
									finding.findingSites.add(site);									
									findings.findings.add(finding);
								}
							}

							break;
						default:
							
							break;
					}
				
				}
				
				if(findings != null)
					results.add(findings);
				//} else {
					// goto IC with no verb
				//}
			}
		
			System.out.println(gson.toJson(results));
			
			System.out.println(formatTime((getTime() - startTime)/1000.0));
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			postgres.close();
		}
		
	}
	
	public void buildStructuredOutputBeta() {
		
		PostgreSQL postgres = new PostgreSQL();
		Gson gson = GsonFactory.build();
		
		long startTime = getTime();
		
		try {
			MongoClient mongoClient = new MongoClient("mongo01.medicalsearchtechnologies.com");
			DB db = mongoClient.getDB("arrowhead-phase2-processed");
			Boolean auth = db.authenticate("arrowhead", "Arrowhead4327*".toCharArray());
		
			DBCollection coll = db.getCollection("processed_imaging");
			
			DBCursor cursor = null;
			
			BasicDBObject query = new BasicDBObject("_id", new ObjectId("54fe46e6e4b0cd89c18a1bba"));
			cursor = coll.find();
			
			postgres.connect("constructors");
			ArrayList<Findings> results = new ArrayList<Findings>();
			
			while(cursor.hasNext()) {
				BasicDBObject obj = (BasicDBObject) cursor.next();

				Sentence sentence = gson.fromJson(obj.toString(), Sentence.class);
				//System.out.println(obj.toString());
//				Annotator ann = new Annotator();
//				ArrayList<Sentence> sentences = ann.annotate("Of note, there are multilevel degenerative changes within the thoracolumbar spine with more severe degenerative change at L5-S1 where there is moderate narrowing of the spinal canal and neural foramina.");
//				sentence = sentences.get(0);
//				MetadataParser mp = new MetadataParser();
//				mp.parseComplex(sentence);

				SentenceMetadata metadata = sentence.getMetadata();
				
				Findings findings = null;
				
				for(VerbPhraseMetadata phrase : metadata.getVerbMetadata()) {
					switch(phrase.getVerbClass()) {
						case ACTION:
						case LINKING_VERB:
						case VERB_OF_BEING:
							if(phrase.getSubj() != null && phrase.getVerb() != null && phrase.getSubjC() != null) {
								int subjIdx = phrase.getSubj().getPosition();
								ArrayList<SemanticType> subjST = sentence.getWordList().get(subjIdx).getSemanticTypeList();
								String[] values = null;
								
								// TODO
								// to overcome issue of multiple STs per token, possibly scan tokens for STs present in database,
								// or use a hard-coded list such as bpoc, bdsy, etc.
								
								// first attempt to query by semantic type if not null or empty
								if(subjST != null && !subjST.isEmpty()) {
									values = postgres.getVerbRelationshipData(subjST.get(0).getSemanticType(), phrase.getVerb().getToken(), phrase.getSubjC().getToken(), phrase.getSubjC().isNegated());
								}								
								
								// if ST null/empty or if query returned no results, attempt to query by token
								if(values == null) {
									values = postgres.getVerbRelationshipData(phrase.getSubj().getToken(), phrase.getVerb().getToken(), phrase.getSubjC().getToken(), phrase.getSubjC().isNegated());
								}
								
								if(values == null) {
									// no database result from ST or token
								} else {
									findings = new Findings();
									findings.fullSentence = sentence.getFullSentence();
									findings.date = sentence.getProcessDate();
									
									Finding finding = new Finding();
									
									finding._class = values[3];
									finding.value = values[4];
									finding.parent = values[5];
									finding.match = Arrays.toString(values);
									
									String findingSiteLocation = values[6];
									
									FindingSite site = new FindingSite();
									
									if(findingSiteLocation.equalsIgnoreCase("SUBJ")) {
										site.value = phrase.getSubj().getToken();
										
										if(phrase.getSubj().getNounPhraseIdx() > -1) {
											// subj is within a noun phrase
											setNounPhraseAttributes(site, phrase.getSubj().getNounPhraseIdx(), metadata.getNounMetadata());
										}
										
									} else if(findingSiteLocation.equalsIgnoreCase("PREP")) {
										// check the prep phrase modifying the subjc
										if(phrase.getSubjC().getPrepPhrasesIdx() != null) {											
											for(int ppIdx : phrase.getSubjC().getPrepPhrasesIdx()) {
												// get metadata object for this prep phrase index
												PrepPhraseMetadata ppmd = metadata.getPrepMetadata().get(ppIdx);
												
												// get the last token in the prep phrase
												PrepPhraseToken token = ppmd.getPhrase().get(ppmd.getPhrase().size()-1); 
												
												// if the last token of the prep phrase is a body part or body structure
												if(sentence.getWordList().get(token.getPosition()).containsSemanticType("(?i)bpoc|bdsy")) {
													site.value = token.getToken();
													
													if(token.getNounPhraseIdx() > -1) {
														setNounPhraseAttributes(site, token.getNounPhraseIdx(), metadata.getNounMetadata());
													}
													break;
												}
											}
										} else {
											site.value = "UNKNOWN - SubjC not modified by a PP";
										}
									}
									
									finding.findingSites.add(site);									
									findings.findings.add(finding);
								}
							}

							break;
						default:
							
							break;
					}
				
				}
				
				if(findings != null)
					results.add(findings);
				//} else {
					// goto IC with no verb
				//}
			}
		
			System.out.println(gson.toJson(results));
			
			System.out.println(formatTime((getTime() - startTime)/1000.0));
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			postgres.close();
		}
		
	}
	
	private void setNounPhraseAttributes(FindingSite site, int npIndex, List<NounPhraseMetadata> nounPhrases) {
		for(GenericToken token : nounPhrases.get(npIndex).getPhrase()) {
			if(token.getToken().matches("(?i)right|left")) 
				site.laterality = token.getToken();
			if(token.getToken().matches("(?i)upper|lower")) 
				site.location = token.getToken();
		}
	}
	
	private boolean sentenceContainsVOBorLVorAV(List<VerbPhraseMetadata> verbPhraseList) {
		boolean ret = false;
		
		for(VerbPhraseMetadata verbPhrase : verbPhraseList) {
			if(verbPhrase.getVerbClass() == Constants.VerbClass.ACTION ||
			   verbPhrase.getVerbClass() == Constants.VerbClass.LINKING_VERB ||
			   verbPhrase.getVerbClass() == Constants.VerbClass.VERB_OF_BEING) {
				ret = true;
				break;
			}
		}
		
		return ret;
	}

	private static long getTime() {
		Calendar cal = Calendar.getInstance();
		return cal.getTimeInMillis();
	}
	
	private static String formatTime(double totalSecs) {
		int hours = (int) (totalSecs / 3600);
		int minutes = (int) ((totalSecs % 3600) / 60);
		int seconds = (int) (totalSecs % 60);

		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
}

// multiple prep phrases modifying a SUBJC
// The unenhanced study/SUBJ [disclosed] a hyperdense mass [in the lower lateral aspect] [of the right kidney] [with an attenuation coefficient] [of 43 .2 units] 2 .0 x 1 .6 cm [in size] .
