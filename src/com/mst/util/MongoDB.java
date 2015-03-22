package com.mst.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.BSONObject;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.mst.model.SemanticType;
import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.model.ontology.SemanticObject;
import com.mst.model.pubmed.PubMedArticleList;
import com.mst.tools.NounHelper;
import com.mst.tools.POSTagger;
import com.mst.tools.PrepositionHelper;
//import com.sun.tools.javac.code.Source;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDB {

	private MongoClient mongoClient = null;
	private DB db = null;
	private boolean auth;
	private Gson gson = null;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static List<String> existingPMIDs = null;
	
	public MongoDB() {
		try {
			mongoClient = new MongoClient(Props.getProperty("mongo_host"));
			
			db = mongoClient.getDB(Props.getProperty("mongo_db"));
			//auth = db.authenticate(Props.getProperty("mongo_user"), Props.getProperty("mongo_pw").toCharArray());
			auth = true;
			gson = new Gson();
			
			// PMIDs that exist in the mongoDB collection. Use this to prevent adding duplicate PubMed articles.
			// this complements a similar bit of functionality in PubMed.java, which keeps track of existing PMIDs and those which it inserts
			// to prevent dupes from getting into the pipeline. Must duplicate here to account for processing lag time (possibly many hours)
			//if(existingPMIDs == null) {
				// TODO get this out of the constructor
			//	existingPMIDs = getDistinctStringValues("id");
			//}
		} catch(Exception e) {
			logger.error("Error establishing a connection to MongoDB. \n{}", e);
		}
	}
	
	public boolean insertJSON(String json, String collection) {
		boolean ret = false;
		//http://stackoverflow.com/questions/7724390/mongodbjava-parsing-json-via-com-mongodb-util-json-parse
		try {
			DBObject dbObject = (DBObject) JSON.parse(json);
			//DBObject dbObject = (DBObject) JSON.parse("{'value':'scott','last_modified':{$date:'2014-09-08T13:43:10.264Z'}}");
			DBCollection dbCollection = db.getCollection(collection);          
			dbCollection.save(dbObject);
			
			ret = true;
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public String insertTaggedSentence(String json) {
		if(auth) {
			try {
				Sentence sentence = gson.fromJson(json, Sentence.class);
				boolean firstWord = true;
				DBCollection processed = null;
				DBCollection metadata = db.getCollection("processed_sentence_metadata");
				
				Constants.Source source = Constants.Source.valueOf(sentence.getSource());
				processed = db.getCollection(source.getMongoCollection());
					
				// TODO checking for existence here is bad because it prevents multiple sentences with the same
				// article_id from being inserted. Need a way to protect against the situation where multiple data loads
				// have been kicked off that contain the same PMID and the processing lag can't catch the dupe ID.
				//if(!existingPMIDs.contains(sentence.getArticleId())) {
					for(WordToken word : sentence.getWordList()) {
						BasicDBObject doc = new BasicDBObject("id", sentence.getId()).
								append("source", sentence.getSource()).								
								append("sentence_id", sentence.getPosition()).
				                append("position", word.getPosition()).
				                append("token", word.getToken()).
				                append("normalized", word.getNormalizedForm()).
				                append("pos", word.getPOS());
						
								if(sentence.getClientId() != null)
									doc.append("client_id", sentence.getClientId());
								if(sentence.getProcedureDate() != null)
									doc.append("procedure_date", sentence.getProcedureDate());
									
				                if(word.isNounPhraseModifier())
				                	doc.append("npMod", word.isNounPhraseModifier());
				                if(word.isNounPhraseHead())
				                	doc.append("npHead", word.isNounPhraseHead());
				                if(word.isPrepPhraseMember())
				                	doc.append("ppMember", word.isPrepPhraseMember());
				                if(word.isPrepPhraseObject())
				                	doc.append("ppObj", word.isPrepPhraseObject());
				                if(word.isInfinitiveHead())
				                	doc.append("infHead", true);
				                if(word.isInfinitiveVerb())
				                	doc.append("inf", true);
				                if(word.isVerbOfBeing())
				                	doc.append("vob", true);
				                if(word.isVerbOfBeingSubject())
				                	doc.append("vobSubj", true);
				                if(word.isVerbOfBeingSubjectComplement())
				                	doc.append("vobSubjC", true);
				                if(word.isLinkingVerb())
				                	doc.append("lv", true);
				                if(word.isLinkingVerbSubject())
				                	doc.append("lvSubj", true);
				                if(word.isLinkingVerbSubjectComplement())
				                	doc.append("lvSubjC", true);
				                
				                if(word.getSemanticTypeList().size() > 0) {
				                	DBObject semTypes = (DBObject) JSON.parse(gson.toJson(word.getSemanticTypeList()));
				                	//System.out.println(gson.toJson(word.getSemanticTypeList()));
				                	doc.append("semantic_types", semTypes);
				                }
				                doc.append("date_processed", sentence.getProcessDate());
				                
				        processed.insert(doc);
						
						if(firstWord) {
		                	System.out.println(gson.toJson(sentence.getMetadata()));		                	
		                	DBObject dbObject = (DBObject) JSON.parse(gson.toJson(sentence.getMetadata()));
		                	dbObject.put("id", sentence.getId());
		                	dbObject.put("sentence_oid", doc.get("_id"));
		        			metadata.save(dbObject);
		                	System.out.println(dbObject.toString());
		                	firstWord = false;
		                }
						
						existingPMIDs.add(sentence.getId());
					}
				//}
			} catch(Exception e) {
				logger.error("insertTaggedSentence(): \n{}\n{}", json, e);
				e.printStackTrace();
			}
		}
		return "";
	}
	
	public String insertTaggedSentenceFull(Sentence sentence) {
		//System.out.println("Mongo auth value: " + auth);
		if(auth) {
			Gson gson = GsonFactory.build();
			
			try {
				Constants.Source source = Constants.Source.valueOf(sentence.getSource());
				DBCollection coll = db.getCollection(source.getMongoCollection());
				
				DBObject dbObject = (DBObject) JSON.parse(gson.toJson(sentence));
      			//coll.save(dbObject);
      			coll.insert(dbObject);
      			
			} catch(Exception e) {
				logger.error("insertTaggedSentenceFull(): \n{}", e);
				e.printStackTrace();
			}
		}
		return "";
	}
	
	public void closeClient() {
		if(mongoClient != null) {
			mongoClient.close();
			auth = false;
		}
	}
	
	public ArrayList<SemanticObject> getSemanticObjects(String domain) {
		ArrayList<SemanticObject> semanticObjects = new ArrayList<SemanticObject>();

		try {
			if(auth) {
				DBCollection coll = db.getCollection("ontology_semantic");
				DBCursor cursor = null;
				BasicDBObject query = null;
				BasicDBObject fields = new BasicDBObject("_id", 0); // don't return ObjectId;
				Gson gson = new Gson();
				
				query = new BasicDBObject("domain", domain).append("concept", "Finding Site");
				cursor = coll.find(query, fields);

				while(cursor.hasNext()) {
					BasicDBObject obj = (BasicDBObject) cursor.next();
					System.out.println(obj.toString());
					semanticObjects.add(gson.fromJson(obj.toString(), SemanticObject.class));
//					SemanticObject so = new SemanticObject();
//					
//					so.concept = obj.getString("concept");
//					so.category = obj.getString("category");
//					so.domain = domain;
//					
//					BasicDBList rules = (BasicDBList) obj.get("rules");
//
//					if(rules != null) {
//						for(BasicDBObject rule : rules.toArray(new BasicDBObject[0])) {
//							Rule r = so.new Rule();
//							r.type = rule.getString("type");
//							r.target = rule.getString("target");
//							r.position = rule.getInt("position");
//							BasicDBList values = (BasicDBList) obj.get("values");
//							//SemanticType st = gson.fromJson(o.toString(), SemanticType.class);
//						}
//					}
				
				}
				cursor.close();
			}
		} catch(Exception e) {
			logger.error("getAnnotatedSentences(): \n{}", e);
		}
		return semanticObjects;
	}
	
	public String insertMetaMapData(String json) {
//		if(auth) {
//			try {
//				Sentence sentence = gson.fromJson(json, Sentence.class);
//				DBCollection coll = db.getCollection("processed_article_camel_metamap");
//				
//				for(MetaMapToken meta : sentence.getMetaMapList()) {
//					BasicDBObject doc = new BasicDBObject("id", sentence.getId()).
//			                append("sentence_id", sentence.getPosition()).
//			                append("value", meta.getValue()).
//			                append("concept_id", meta.getConceptID()).
//			                append("concept_id", meta.getConceptName()).
//			                append("preferred_name", meta.getPreferredName()).
//							append("semantic_types", meta.getSemanticTypes()).
//							append("sources", meta.getSources());
//					
//					coll.insert(doc);
//				}
//			} catch(Exception e) {
//				logger.error("insertMetaMapData(): \n{}\n{}", json, e);
//			}
//		}
		return "";
	}
	
	public boolean insertPubMedAudit(String json) {
		if(auth) {
			try {
				PubMedArticleList idList = gson.fromJson(json, PubMedArticleList.class);
				DBCollection coll = db.getCollection("processed_article_camel_audit");
				
				BasicDBObject doc = new BasicDBObject("source", idList.getSource()).
						//append("client_id", idList.getClientId()).
						append("search_term", idList.getSearchTerm()).
						append("min_year", idList.getMinYear()).
						append("max_year", idList.getMaxYear()).
						append("id_list", idList.getIdList()).
		                append("date_processed", new Date());
				
				coll.insert(doc);
			} catch(Exception e) {
				logger.error("insertPubMedAudit(): \n{}\n{}", json, e);
			}
		}
		return true;
	}
	
	public ArrayList<ArrayList<String>> getDistinctTokenLists(ArrayList<String> articleIds) {
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		
		String excludeDigitsRegex = "^(?!(\\d+)$)"; // all single and multiple digits
		String excludePuncRegex = "^([^(\\p{P})\\+<>±≥\\^∼]+$)"; // punc and repeating punc. remove $ to restrict even more
		String excludeNumberCharRegex = "^(?!(\\d[A-Za-z])$)^(?!([A-Za-z]\\d+)$)"; // ex. 5A and A5   these are gene codes, maybe?
		StringBuilder finalRegex = new StringBuilder();
		
		// always exclude digits and punctuation
		finalRegex.append(excludeDigitsRegex).append(excludePuncRegex);

		try {
			if(auth) {
				DBCollection coll = db.getCollection("processed_article_camel");

				DBObject query = new BasicDBObject("id", new BasicDBObject("$in", articleIds));
				query.put("token", Pattern.compile(finalRegex.toString()));
				
				// all tokens (excluding punc and digits)
				BasicDBList list = (BasicDBList) coll.distinct("token", query);
				ret.add((ArrayList) list);
				
				// all tokens that have semantic types
				query.put("semantic_types", new BasicDBObject("$ne", null));
				list = (BasicDBList) coll.distinct("token", query);
				ret.add((ArrayList) list);
				
				// all tokens that do not have a semantic type and allow num/char (ex. 5A)
				query.removeField("semantic_types");
				query.put("semantic_types", null);
				list = (BasicDBList) coll.distinct("token", query);
				ret.add((ArrayList) list);
				
				// all tokens that do not have a semantic type and DO NOT allow num/char
				finalRegex.insert(0, excludeNumberCharRegex); // must come before other regexs to work properly
				query.removeField("token");
				query.put("token", Pattern.compile(finalRegex.toString()));
				list = (BasicDBList) coll.distinct("token", query);

				ret.add((ArrayList) list);
			}	
		} catch(Exception e) {
			logger.error("getDistinctTokenLists(): \n{}", e);
		}
		return ret;
	}
	
	public ArrayList<String> getAggregateTokenAndST() {
		ArrayList<String> ret = new ArrayList<String>();
		
		try {
			if(auth) {
				/*
				db.processed_article_camel.aggregate([
				{ $match: { semantic_types: { $ne: null } } },
				{ $group: { _id: { token: "$token", st: "$semantic_types" }, count: { $sum: 1 } } },
				{ $sort: { count: -1 } }
				])
				*/
				
				DBCollection coll = db.getCollection("processed_article_camel");
				//DBObject match = new BasicDBObject("$match", new BasicDBObject("semantic_types", new BasicDBObject("$ne", null)));
				//DBObject match = new BasicDBObject("$match", new BasicDBObject("id", "24827542"));
				
				DBObject groupFields = new BasicDBObject("_id", new BasicDBObject("token", "$token")
												 .append("st", "$semantic_types"));
				groupFields.put("count", new BasicDBObject("$sum", 1));
				DBObject group = new BasicDBObject("$group", groupFields);

				DBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));

				// run aggregation
				List<DBObject> pipeline = Arrays.asList(group, sort);
				AggregationOutput output = coll.aggregate(pipeline);
				
				String dq = "\"";
				String delim = ",";
				
//				for(DBObject result : output.results()) {  
//				    BSONObject bson = (BSONObject) result.get("_id");
//				    
//				    BasicDBList st = (BasicDBList) bson.get("st");
//				    for(Object o : st) {
//				    	BasicDBObject o2 = (BasicDBObject) o;
//				    
//				    	csv.append(dq).append(result.get("count")).append(dq).append(delim);
//				    	csv.append(dq).append(o2.get("token")).append(dq).append(delim);
//				    	csv.append(dq).append(o2.get("semanticType")).append(dq);
//					    csv.append("\n");
//					}
//				}
				
				for(DBObject result : output.results()) {  
				    BSONObject bson = (BSONObject) result.get("_id");
				    String token = (String) bson.get("token");
				    StringBuilder csv = new StringBuilder();
				    
				    csv.append(dq).append(result.get("count")).append(dq).append(delim);
				    csv.append(dq).append(token).append(dq).append(delim);
				    
				    StringBuilder sbST = new StringBuilder();
				    StringBuilder addlTokens = new StringBuilder();
				    BasicDBList st = (BasicDBList) bson.get("st");
				    
				    if(st != null) {
					    for(Object o : st) {
					    	BasicDBObject o2 = (BasicDBObject) o;
					    
					    	String thisToken = (String) o2.get("token");
					    	
						    if(thisToken.equalsIgnoreCase(token)) {
						    	sbST.append(o2.get("semanticType")).append(" / ");
						    } else {
						    	addlTokens.append(dq).append("x").append(dq).append(delim);
						    	addlTokens.append(dq).append(thisToken).append(dq).append(delim);
						    	addlTokens.append(dq).append(o2.get("semanticType")).append(dq).append("\n");
						    }
						}
				    }
				    
				    if(sbST.length() == 0) {
				    	// some tokens don't have any STs that match. e.g.: token: A2, sem type: imft/A2 B
				    	csv.append(dq).append("").append(dq);
				    	
				    } else if(sbST.length() > 0) {
				    	csv.append(dq).append(sbST.toString()).append(dq);
				    }
				    
				    ret.add(csv.toString());
				    
				    if(addlTokens.length() > 0) {
			    		//csv.append(addlTokens.toString());
			    		ret.add(addlTokens.deleteCharAt(addlTokens.length()-1).toString()); // remove trailing \n
				    }

				}
			}
			
			//System.out.println(csv.toString());	
			
		} catch(Exception e) {
			logger.error("getAnnotatedSentences(): \n{}", e);
			e.printStackTrace();
		}
		return ret;
	}
	
	public ArrayList<Sentence> getAnnotatedSentencesTest() {
		ArrayList<Sentence> sentenceList = new ArrayList<Sentence>();
	
		try {
			if(auth) {
				DBCollection coll = db.getCollection("processed_imaging");
				
				DBCursor cursor = null;
				BasicDBObject query = null;
				BasicDBObject fields = null;
				
				fields = new BasicDBObject("_id", 0); // don't return ObjectId
				query = new BasicDBObject("position", 0);
				cursor = coll.find(query, fields);
				
				while(cursor.hasNext()) {
					BasicDBObject obj = (BasicDBObject) cursor.next();
					//System.out.println(obj.toString());
					Sentence s = gson.fromJson(obj.toString(), Sentence.class);
					sentenceList.add(s);
				}
				
			}
		} catch(Exception e) {
			logger.error("getAnnotatedSentencesFull(): \n{}", e);
		}
		return sentenceList;
	}
	
	public ArrayList<Sentence> getAnnotatedSentencesChunk(ArrayList<String> articleIds, int chunkSize) {
		ArrayList<Sentence> sentenceList = new ArrayList<Sentence>();

		if(articleIds == null)
			return null;
		
		try {
			if(auth) {
				DBCollection coll = db.getCollection("processed_article_camel");
				
				DBCursor cursor = null;
				BasicDBObject query = null;
				BasicDBObject fields = null;
				int chunkCount = 0;
				
				fields = new BasicDBObject("_id", 0); // don't return ObjectId
				if(chunkSize > articleIds.size())
					chunkSize = articleIds.size();
				
				while(chunkCount < articleIds.size()) {
					List<String> chunkList = articleIds.subList(chunkCount, chunkCount + chunkSize);
					chunkCount += chunkList.size();
					
					query = new BasicDBObject("id", new BasicDBObject("$in", chunkList));
	
					cursor = coll.find(query, fields);	
					cursor.sort(new BasicDBObject("id", 1).append("sentence_id", 1).append("position", 1));
					
					ArrayList<WordToken> wordList = new ArrayList<WordToken>();
					Sentence sentence = null;
	
					int prevSentenceId = 1; // track when a sentence id changes
					String prevArticleId = "";
					boolean newSentence = true, firstRun = true;
					BasicDBObject obj = null;
	
					while(cursor.hasNext()) {
						obj = (BasicDBObject) cursor.next();
						
						if(firstRun) {
							prevSentenceId = obj.getInt("sentence_id");
							prevArticleId = obj.getString("id");
							firstRun = false;
						}
						
						int thisSentenceId = obj.getInt("sentence_id");
						String thisArticleId = obj.getString("id");
						
						if(newSentence) {
							sentence = new Sentence();
							sentence.setId(thisArticleId);
							sentence.setProcessDate(obj.getDate("process_date"));
							sentence.setFullSentence(null);
							sentence.setPosition(thisSentenceId);
							newSentence = false;
						}
	
						WordToken word = new WordToken(obj.getString("token"), obj.getString("normalized"), obj.getInt("position"));
						word.setPOS(obj.getString("pos"));
						word.setNounPhraseHead(obj.getBoolean("npHead"));
						word.setNounPhraseModifier(obj.getBoolean("npMod"));
						word.setPrepPhraseMember(obj.getBoolean("ppMember"));
						word.setPrepPhraseObject(obj.getBoolean("ppObj"));
						word.setInfinitiveHead(obj.getBoolean("infHead"));
						word.setVerbOfBeing(obj.getBoolean("vob"));
		                //word.isVerbOfBeingMember = obj.getBoolean("vobMember");
		                word.setVerbOfBeingSubject(obj.getBoolean("vobSubj"));
		                word.setVerbOfBeingSubjectComplement(obj.getBoolean("vobSubjC"));
		                word.setLinkingVerb(obj.getBoolean("lv"));
		                word.setLinkingVerbSubject(obj.getBoolean("lvSubj"));
		                word.setLinkingVerbSubjectComplement(obj.getBoolean("lvSubjC"));	
						
						BasicDBList dbList = (BasicDBList) obj.get("semantic_types");
	
						if(dbList != null) {
							for(Object o : dbList) {
								SemanticType st = gson.fromJson(o.toString(), SemanticType.class);
								word.getSemanticTypeList().add(st);
							}
						}
	
						if(!thisArticleId.equals(prevArticleId) || thisSentenceId != prevSentenceId) { // TODO check also for article_id change to accomodate single sentences
	
							sentence.setWordList(wordList);
							sentenceList.add(sentence);
	
							prevSentenceId = thisSentenceId;
							prevArticleId = thisArticleId;
	
							wordList = new ArrayList<WordToken>();
							newSentence = true;
						}
	
						wordList.add(word);
					}
	
					// add final entries
					sentence.setWordList(wordList);
					sentenceList.add(sentence);
	
					cursor.close();
				}
			}
			
		} catch(Exception e) {
			logger.error("getAnnotatedSentencesChunk(): \n{}", e);
		}
		return sentenceList;
	}
	
	public ArrayList<Sentence> getAnnotatedSentences(List<String> IDs, int limit) {
		ArrayList<Sentence> sentenceList = new ArrayList<Sentence>();

		try {
			if(auth) {
				DBCollection coll = db.getCollection("processed_article_camel");
				DBCursor cursor = null;
				BasicDBObject query = null;
				BasicDBObject fields = null;

				fields = new BasicDBObject("_id", 0);
				
				//if(articleIds == null)
				//	cursor = coll.find();
				//else {
					query = new BasicDBObject("id", new BasicDBObject("$in", IDs));
				//	if(sentenceId != null)
				//		query.append("sentence_id", sentenceId);

				if(limit == 0)	
					cursor = coll.find(query, fields);
				else
					cursor = coll.find(query, fields).limit(limit);
				//}

				cursor.sort(new BasicDBObject("id", 1).append("sentence_id", 1).append("position", 1));
				//query = new BasicDBObject("_id", new ObjectId("5255d10f0de1496494d31689"));
				
				ArrayList<WordToken> wordList = new ArrayList<WordToken>();
				Sentence sentence = null;

				int prevSentenceId = 1; // track when a sentence id changes
				String prevArticleId = "";
				boolean newSentence = true, firstRun = true;
				BasicDBObject obj = null;

				while(cursor.hasNext()) {
					obj = (BasicDBObject) cursor.next();
					
					if(firstRun) {
						prevSentenceId = obj.getInt("sentence_id");
						prevArticleId = obj.getString("id");
						firstRun = false;
					}
					
					int thisSentenceId = obj.getInt("sentence_id");
					String thisId = obj.getString("id");
					
					if(newSentence) {
						sentence = new Sentence();
						sentence.setId(thisId);
						sentence.setProcessDate(obj.getDate("process_date"));
						sentence.setFullSentence(null);
						sentence.setPosition(thisSentenceId);
						newSentence = false;
					}

					WordToken word = new WordToken(obj.getString("token"), obj.getString("normalized"), obj.getInt("position"));
					word.setPOS(obj.getString("pos"));
					word.setNounPhraseHead(obj.getBoolean("npHead"));
					word.setNounPhraseModifier(obj.getBoolean("npMod"));
					word.setPrepPhraseMember(obj.getBoolean("ppMember"));
					word.setPrepPhraseObject(obj.getBoolean("ppObj"));

					BasicDBList dbList = (BasicDBList) obj.get("semantic_types");

					if(dbList != null) {
						for(Object o : dbList) {
							SemanticType st = gson.fromJson(o.toString(), SemanticType.class);
							word.getSemanticTypeList().add(st);
						}
					}

					if(!thisId.equals(prevArticleId) || thisSentenceId != prevSentenceId) { // TODO check also for article_id change to accomodate single sentences

						sentence.setWordList(wordList);
						sentenceList.add(sentence);

						prevSentenceId = thisSentenceId;
						prevArticleId = thisId;

						wordList = new ArrayList<WordToken>();
						newSentence = true;
					}

					wordList.add(word);
				}

				// add final entries
				sentence.setWordList(wordList);
				sentenceList.add(sentence);

				//String json = gson.toJson(sentenceList);
				//System.out.println(json);
				cursor.close();
			}

		} catch(Exception e) {
			logger.error("getAnnotatedSentences(): \n{}", e);
		}
		return sentenceList;
	}
	
	public void cleanPMIDdupes() {
		try {
			if(auth) {
				DBCollection coll = db.getCollection("processed_article_camel");
				DBCursor cursor = null;
				BasicDBObject query = null;
				BasicDBObject fields = null;

				fields = new BasicDBObject("_id", 0).append("id", 1).append("date_processed", 1);
				
				query = new BasicDBObject("sentence_id", 1).append("position", 1);

				cursor = coll.find(query, fields);
				cursor.sort(new BasicDBObject("id", 1));

				String prevPMID = "";
				BasicDBObject obj = null;

				while(cursor.hasNext()) {
					obj = (BasicDBObject) cursor.next();
					
					if(obj.getString("id").equalsIgnoreCase(prevPMID)) {
						System.out.println("Delete: " + prevPMID + " / " + obj.getDate("date_processed"));
						coll.remove(new BasicDBObject().append("id", prevPMID).append("date_processed", obj.getDate("date_processed")));
					}
					
					prevPMID = obj.getString("id");
				}
				cursor.close();
			}

		} catch(Exception e) {
			logger.error("cleanPMIDdupes(): \n{}", e);
		}
	}
	
	public List<String> getDistinctStringValues(String field) {
		// TODO find a better data structure that supports sorting, binary search, and maintaining order upon insert
		List<String> list = new ArrayList<String>();

		try {
			if(auth) {				
				list = db.getCollection("processed_article_camel").distinct(field);
			}

		} catch(Exception e) {
			logger.error("getDistinctField(): \n{}", e);
		}
		return list;
	}
	
	public ArrayList<Sentence> getSentencesByObjectId(String collection, List<String> objectIds) {
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		Gson gson = GsonFactory.build();
		try {
			if(auth) {
				BasicDBList idList = new BasicDBList();
				for(String id : objectIds) {
					idList.add(new ObjectId(id));
				}
				DBObject inClause = new BasicDBObject("$in", idList);
				DBObject query = new BasicDBObject("_id", inClause);

				DBCursor cursor = db.getCollection(collection).find(query);
				
				while(cursor.hasNext()) {
					BasicDBObject obj = (BasicDBObject) cursor.next();
					Sentence s = gson.fromJson(obj.toString(), Sentence.class);
					sentences.add(s);
				}
				
				cursor.close();
				
//				DBObject result = db.getCollection(collection).findOne(new ObjectId(objectIds.get(0)));
//				Sentence s = gson.fromJson(result.toString(), Sentence.class);
//				sentences.add(s);
			}

		} catch(Exception e) {
			logger.error("getSentencesByObjectId(): \n{}", e);
			e.printStackTrace();
		}
		return sentences;
	}
	
	public ArrayList<PubMedArticleList> getPubMedAuditByObjectId(List<String> objectIds) {
		ArrayList<PubMedArticleList> list = new ArrayList<PubMedArticleList>();

		try {
			if(auth) {
				BasicDBList idList = new BasicDBList();
				for(String id : objectIds) {
					idList.add(new ObjectId(id));
				}
				DBObject inClause = new BasicDBObject("$in", idList);
				DBObject query = new BasicDBObject("_id", inClause);
				//BasicDBObject query = new BasicDBObject("_id", new ObjectId(objectId));
				DBCursor cursor = db.getCollection("processed_article_camel_pubmed_audit").find(query);
				
				while(cursor.hasNext()) {
					
					PubMedArticleList pmal = new PubMedArticleList();
					
					BasicDBObject obj = (BasicDBObject) cursor.next();
					
					pmal.setSearchTerm(obj.getString("search_term"));
					pmal.setMinYear(obj.getInt("min_year"));
					pmal.setMaxYear(obj.getInt("max_year"));
					pmal.setDateProcessed(obj.getDate("date_processed"));
						
					BasicDBList dbList = (BasicDBList) obj.get("id_list");
					pmal.setCount(dbList.size());
						
					for(Object o : dbList) {
						pmal.getIdList().add(o.toString());
					}
					
					list.add(pmal);
				}
				
				cursor.close();
			}

		} catch(Exception e) {
			logger.error("getPubMedAuditByObjectId(): \n{}", e);
		}
		return list;
	}
	
	public ArrayList<PubMedArticleList> getPubMedAuditBySource(List<String> sources) {
		ArrayList<PubMedArticleList> list = new ArrayList<PubMedArticleList>();

		try {
			if(auth) {
				DBObject inClause = new BasicDBObject("$in", sources);
				DBObject query = new BasicDBObject("source", inClause);
				//BasicDBObject query = new BasicDBObject("_id", new ObjectId(objectId));
				DBCursor cursor = db.getCollection("processed_article_camel_pubmed_audit").find(query);
				
				while(cursor.hasNext()) {
					
					PubMedArticleList pmal = new PubMedArticleList();
					
					BasicDBObject obj = (BasicDBObject) cursor.next();
					
					pmal.setSearchTerm(obj.getString("search_term"));
					pmal.setMinYear(obj.getInt("min_year"));
					pmal.setMaxYear(obj.getInt("max_year"));
					pmal.setDateProcessed(obj.getDate("date_processed"));
						
					BasicDBList dbList = (BasicDBList) obj.get("id_list");
					pmal.setCount(dbList.size());
						
					for(Object o : dbList) {
						pmal.getIdList().add(o.toString());
					}
					
					list.add(pmal);
				}
				
				cursor.close();
			}

		} catch(Exception e) {
			logger.error("getPubMedAuditBySource(): \n{}", e);
		}
		return list;
	}
	
	public ArrayList<PubMedArticleList> getAllPubMedAuditEntries() {
		ArrayList<PubMedArticleList> list = new ArrayList<PubMedArticleList>();

		try {
			if(auth) {
				DBCollection coll = db.getCollection("processed_article_camel_pubmed_audit");
				DBCursor cursor = coll.find().sort(new BasicDBObject("date_processed", -1));
				
				while(cursor.hasNext()) {
					BasicDBObject obj = (BasicDBObject) cursor.next();
					
					PubMedArticleList listItem = new PubMedArticleList();
					
					listItem.setSearchTerm(obj.getString("search_term"));
					listItem.setMinYear(obj.getInt("min_year"));
					listItem.setMaxYear(obj.getInt("max_year"));
					listItem.setDateProcessed(obj.getDate("date_processed"));
					
					BasicDBList dbList = (BasicDBList) obj.get("id_list");
					listItem.setCount(dbList.size());
					
					listItem.setTag(obj.getObjectId("_id").toString());
					//for(Object o : dbList) {
					//	listItem.getIdList().add(o.toString());
					//}
					
					list.add(listItem);
				}
				
				cursor.close();
			}

		} catch(Exception e) {
			logger.error("getAllPubMedAuditEntries(): \n{}", e);
		}
		return list;
	}
	
	//db.processed_article_camel_ct.find({article_id: "723E64BC-964B-4D70-8207-442B8CB86738"}).sort({sentence_id: 1, position: 1})
	public List<String> getDistinctCTIDsByKeyword(List<String> keywords) {
		List<String> articleIds = null;

		try {
			if(auth) {
				DBObject inClause = new BasicDBObject("$in", keywords);
				DBObject query = new BasicDBObject("token", inClause);

				articleIds = db.getCollection("processed_article_camel_ct").distinct("id", query);
			}

		} catch(Exception e) {
			logger.error("getDistinctCTIDsByKeyword(): \n{}", e);
		}
		return articleIds;
	}
	
	
	public String getAnnotatedAsCSV() {
		String csvOut = "";
		
		try {
			if(auth) {
				POSTagger tagger = new POSTagger();
				NounHelper nouns = new NounHelper();
				PrepositionHelper preps = new PrepositionHelper();
				DBCollection coll = db.getCollection("processed_article_scott");
				BasicDBObject query = null;
				BasicDBObject fields = null;

				query = new BasicDBObject("id", "23899605");
				//query = new BasicDBObject("_id", new ObjectId("5255d10f0de1496494d31689"));
				fields = new BasicDBObject("_id", 0);

				DBCursor cursor = coll.find(query, fields)
						.sort(new BasicDBObject("sentence_id", 1)); //TODO multiple sort fields
				//.limit(35);

				ArrayList<WordToken> wordList = new ArrayList<WordToken>();
				int oldSentenceId = 1;
				BasicDBObject obj = null;
				StringBuilder csv = new StringBuilder();

				csv.append("ArticleId,NP-Annotated,PP-Annotated\n");
				
				while(cursor.hasNext()) {
					obj = (BasicDBObject) cursor.next();

					int sentenceId = obj.getInt("sentence_id");

					WordToken word = new WordToken(obj.getString("token"), obj.getString("normalized"), obj.getInt("position"));
					word.setNounPhraseHead(obj.getBoolean("npHead"));
					word.setNounPhraseModifier(obj.getBoolean("npMod"));
					word.setPrepPhraseMember(obj.getBoolean("ppMember"));
					word.setPrepPhraseObject(obj.getBoolean("ppObj"));

					if(sentenceId != oldSentenceId) {
						oldSentenceId++;
						csv.append(obj.get("id")).append(",").append(nouns.getNPAnnotatedSentence(null, null, wordList, true)).append(",");
						csv.append(preps.getPPAnnotatedSentence(null, null, wordList)).append("\n");
						wordList = new ArrayList<WordToken>();
					}

					wordList.add(word);
				}
				csv.append(obj.get("id")).append(",").append(nouns.getNPAnnotatedSentence(null, null, wordList, true)).append(",");
				csv.append(preps.getPPAnnotatedSentence(null, null, wordList)).append("\n");

				csvOut = csv.toString();
				//System.out.println(gson.toJson(wordList));
				//System.out.println(tagger.getNPAnnotatedSentence(null, null, wordList));		
				cursor.close();
				
			}
		} catch(Exception e) {
			logger.error("getAnnotatedAsCSV(): \n{}", e);
		}

		return csvOut;
	}

	
}
