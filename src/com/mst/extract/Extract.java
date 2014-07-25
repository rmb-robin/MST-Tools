package com.mst.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mst.model.PubMedArticleList;
import com.mst.model.SemanticType;
import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.tools.POSTagger;
import com.mst.util.MongoDB;

public class Extract {

	private final String PREP_PHRASE_ST_CSV_HEADERS = "Article,Relationship,Prec. Term,Prec. Term ST,Prep Phrase Term,Prep Phrase Term ST,Prep Phrase,Sentence";
	private final String NOUN_PHRASE_CSV_HEADERS = "Noun Phrase,Annotated Sentence,Original Sentence";
	private final String NOUN_PHRASE_CSV_HEADERS_WHITELIST = "Keywords," + NOUN_PHRASE_CSV_HEADERS;
	private final String dq = "\"";
	private final String DELIM = ",";
	private final String NEW_LINE = System.getProperty("line.separator");
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Map<String, Integer> stCounts = new HashMap<String, Integer>();
	private Map<String, Integer> modifierCounts = new HashMap<String, Integer>();
	private Map<String, Integer> articleIdCounts = new HashMap<String, Integer>();
	private Map<String, Integer> npAnnCounts = new HashMap<String, Integer>();
	
	public enum NounPositionTypes {
		ANYWHERE(1), HEAD(2), HEAD_MINUS_ONE(3), HEAD_MINUS_N(4);
		
		private int value;
		
		private NounPositionTypes(int value) {
			this.value = value;
		}
		
		public static NounPositionTypes getValue(String value) {
			NounPositionTypes ret = null;
			
			for(NounPositionTypes npt : NounPositionTypes.values()) {
				if(npt.value == Integer.parseInt(value)) {
					ret = npt;
					break;
				}
			}
			return ret;
		}
	}
	
	public Extract() { }

	// extracts semantic type relationship between modifiers and terms of prep phrase
	public ArrayList<String> extractPrepPhraseSTRelationship(ArrayList<Sentence> sentenceList, boolean showHeaderRow, ArrayList<String> semTypeBlacklist, 
			ArrayList<String> wordBlacklist, ArrayList<String> semTypeWhitelist, ArrayList<String> wordWhitelist, int rowLimit) {
		//TODO how to choose which sem type from subject word?
		//TODO test verb, normal, begin, prep at EOL sentences
		ArrayList<String> outputList = new ArrayList<String>();
		POSTagger tagger = new POSTagger();
		//int rowCount = 0;
		
		//String[] stArray = { "spco", "mnob", "popg", "food", "mobd", "hcpp", "inpr", "plnt" };
		//String[] wordArray = { "patients" };
		
		if(semTypeBlacklist != null) {
			for(int i=0; i < semTypeBlacklist.size(); i++) {
				semTypeBlacklist.set(i, semTypeBlacklist.get(i).replace("*", "[a-z]{4}"));
			}
		}
		
		if(semTypeWhitelist != null) {
			for(int i=0; i < semTypeWhitelist.size(); i++) {
				semTypeWhitelist.set(i, semTypeWhitelist.get(i).replace("*", "[a-z]{4}"));
			}
		}
		
		//if(showHeaderRow)
		//	outputList.add(PREP_PHRASE_ST_CSV_HEADERS);
		
		for(Sentence sentence : sentenceList) {

			ArrayList<WordToken> words = sentence.getWordList();
			
			for(int i=0; i < words.size(); i++) {
				int precedingIdx = 0, prepStartOffset = 1;
				String precedingToken = "", precedingSemType = "";
				boolean bContinue = false;
				
				// if the first word begins a prep phrase (no preceeding_token)
				if(i==0 && words.get(i).isPrepPhraseMember()) {
					precedingIdx = i;
					prepStartOffset = 0;
					precedingToken = "BEGIN";
					precedingSemType = "begin";
					bContinue = true;
				} else {
					ArrayList<SemanticType> precedingSemTypeList = words.get(i).getSemanticTypeList();
					// if not at last word AND (current word has a semantic type OR is a verb)
					if(i < words.size()-1 && (!precedingSemTypeList.isEmpty() || words.get(i).getPOS().startsWith("VB"))) {
						precedingIdx = i;
						precedingToken = words.get(precedingIdx).getToken();
						// semantic type takes precedence?  <-- appears to be what Eric's code does 
						//TODO possibly loop through sem types of preceeding token looking for whitelisted?
						precedingSemType = (!precedingSemTypeList.isEmpty() ? precedingSemTypeList.get(precedingSemTypeList.size()-1).getSemanticType() : "verb");
						
						// get prep phrase start offset or bail if a token has sem types
						for(int j=i+1; j < words.size(); j++) {
							if(words.get(j).isPrepPhraseMember()) {							
								bContinue = true;
								break;
							}
						 	if(!words.get(j).getSemanticTypeList().isEmpty()) {
						 		i = j-1; // next loop will increment this so it matches j
						 		break;
						 	}
							prepStartOffset++;
						}
					}
				}
				
				if(bContinue) {
					StringBuilder prepPhrase = new StringBuilder();
					
					// build prep phrase fragment
					for(int j = precedingIdx + prepStartOffset; j < words.size(); j++) {
						prepPhrase.append(words.get(j).getToken()).append(" ");
						if(words.get(j).isPrepPhraseObject() && !words.get(j).isPrepPhraseMember())
							break;
					}
					
					// loop through tokens until the prep phrase object is reached
					for(int j = precedingIdx + prepStartOffset; j < words.size(); j++) {
						// create csv row for each semantic type of prep phrase token (not all tokens will have a ST)
						for(SemanticType semType : words.get(j).getSemanticTypeList()) {
							boolean buildCSVRow = true;
							
							// process semantic type blacklist (exclusions)					
							if(semTypeBlacklist != null && semTypeBlacklist.size() > 0) {
								String stPair = precedingSemType + "-" + semType.getSemanticType();
								for(String stBlack : semTypeBlacklist) {
									if(stPair.matches(stBlack)) {
										buildCSVRow = false;
										break;
									}
								}
							}
							
							// process word blacklist (exclusions)
							if(buildCSVRow == true && wordBlacklist != null && wordBlacklist.size() > 0) {
								if(wordBlacklist.contains(precedingToken) || wordBlacklist.contains(semType.getToken()))
									buildCSVRow = false;
							}
							
							// whitelists trump blacklists
							
							// word whitelist
							if(wordWhitelist != null && wordWhitelist.size() > 0) {
								if(!wordWhitelist.contains(precedingToken) && !wordWhitelist.contains(semType.getToken()))
									buildCSVRow = false;
								else
									buildCSVRow = true;
							}
							
							// process semantic type whitelist (inclusions)					
							if(semTypeWhitelist != null && semTypeWhitelist.size() > 0) {
								buildCSVRow = false;
								String stPair = precedingSemType + "-" + semType.getSemanticType();
								for(String stWhite : semTypeWhitelist) {
									if(stPair.matches(stWhite)) {
										buildCSVRow = true;
										break;
									}
								}
							}
							
							if(buildCSVRow) {
								outputList.add(buildCSVRow(sentence.getArticleId(), precedingSemType, precedingToken, semType.getSemanticType(), semType.getToken(), prepPhrase.toString().trim(), tagger.getPPAnnotatedSentence(null, null, words)));
								//rowCount++;
								//if(rowLimit > 0 && rowCount >= rowLimit)
								//	return outputList;
								
							}
						}
						// as currently implemented, more than one token can be defined as a prep phrase OBJ
						// see identifyPrepPhrases() in POSTagger.java
						if(words.get(j).isPrepPhraseObject() && !words.get(j).isPrepPhraseMember()) {
							i = j-1;
							break;
						}
					}
				}
			}
		}
		
		return outputList;
	}
	
	public ArrayList<String> extractPrepPhraseSTRelationshipChunked(ArrayList<String> objectIds, ArrayList<String> semTypeBlacklist, 
			ArrayList<String> wordBlacklist, ArrayList<String> semTypeWhitelist, ArrayList<String> wordWhitelist, int limit, boolean writeToFile) {
		
		ArrayList<String> csvRows = new ArrayList<String>();
		BufferedWriter bw = null;
		MongoDB mongo = null;
		
		int maxChunkSize = 25; // number of PMIDs to retrieve from Mongo per iteration
		int chunkCount = 0; // total chunks processed
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				
		try {
			mongo = new MongoDB();
			
			ArrayList<PubMedArticleList> auditList = (ArrayList<PubMedArticleList>) mongo.getPubMedAuditByObjectId(objectIds); // list to hold entries from processed_article_camel_pubmed_audit
			ArrayList<String> pmidList = new ArrayList<String>();
			
			// combine pmid lists from all PubMedArticleLists
			for(PubMedArticleList pmal : auditList)
				for(String pmid : pmal.getIdList())
					pmidList.add(pmid);
			
			int numPMIDs = pmidList.size(); // total number of PMIDs returned from query
			
			if(limit <= 0 || limit > numPMIDs) {
				limit = numPMIDs; // limit not supplied, default to total number available
			} else if(limit < maxChunkSize)
				maxChunkSize = limit; // process limit supplied if less then max; however, limit can never exceed max
			
			if(writeToFile) {
				File file = new File("st_relationship_" + sdf.format(new Date()) + ".csv");
				if(!file.exists())
					file.createNewFile();
				
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				
				bw.write(PREP_PHRASE_ST_CSV_HEADERS + NEW_LINE);
			}
			
			while(chunkCount < limit) {
				logger.info("ST Extract Progress: " + chunkCount + "/" + limit);
				 // avoid OutOfBoundsException on .subList when chunkCount + maxChunkSize > limit
				int chunkSize = (chunkCount + maxChunkSize > limit ? limit - chunkCount : maxChunkSize);
				
				List<String> chunkList = pmidList.subList(chunkCount, chunkCount + chunkSize);
				chunkCount += chunkSize; //chunkList.size();
				
				ArrayList<Sentence> sentences = mongo.getAnnotatedSentences(chunkList, 0);
				ArrayList<String> lines = extractPrepPhraseSTRelationship(sentences, false, semTypeBlacklist, wordBlacklist, semTypeWhitelist, wordWhitelist, 0);
				
				for(String line : lines) {
					try {
						if(writeToFile)
							bw.write(line + NEW_LINE);
						else
							csvRows.add(line);
						
					} catch(IOException e) {
						logger.error("extractPrepPhraseSTRelationshipChunked(): Error writing to file or appending csvRows. \n{}", e);
					}
				}
			}
		} catch(Exception e) {
			logger.error("Extract:extractPrepPhraseSTRelationshipChunked(): \n{}", e);
		} finally {
			if(mongo != null)
				mongo.closeClient();
			if(bw != null)
				try {
					bw.close();
				} catch(IOException e) { 
					logger.warn("Extract:extractPrepPhraseSTRelationshipChunked(): Error closing BufferedReader:\n{}", e);
				}
		}
		
		return csvRows;
	}
	
	public ArrayList<String> extractNounPhrasesChunked(ArrayList<String> objectIds, ArrayList<String> wordWhitelist, NounPositionTypes position, int limit, boolean writeToFile) {
		
		ArrayList<String> csvRows = new ArrayList<String>();
		BufferedWriter bw = null;
		MongoDB mongo = null;
		
		int maxChunkSize = 25; // number of PMIDs to retrieve from Mongo per iteration
		int chunkCount = 0; // total chunks processed
		String keywords = "", header = "";
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				
		try {
			mongo = new MongoDB();
			
			ArrayList<PubMedArticleList> auditList = (ArrayList<PubMedArticleList>) mongo.getPubMedAuditByObjectId(objectIds); // list to hold entries from processed_article_camel_pubmed_audit
			ArrayList<String> pmidList = new ArrayList<String>();
			
			// combine pmid lists from all PubMedArticleLists
			for(PubMedArticleList pmal : auditList)
				for(String pmid : pmal.getIdList())
					pmidList.add(pmid);
			
			int numPMIDs = pmidList.size(); // total number of PMIDs returned from query
			
			if(limit <= 0 || limit > numPMIDs) {
				limit = numPMIDs; // limit not supplied, default to total number available
			} else if(limit < maxChunkSize)
				maxChunkSize = limit; // process limit supplied if less then max; however, limit can never exceed max
			
			if(wordWhitelist.size() > 0) {
				// whitelist supplied, so write a header that includes Keywords and build kw list
				header = NOUN_PHRASE_CSV_HEADERS_WHITELIST + NEW_LINE;
				StringBuilder sb = new StringBuilder();
				for(String s : wordWhitelist) {
					sb.append(s).append(", ");
				}
				keywords = "\"" + sb.toString().substring(0, sb.toString().length() - 2) + "\",";
			} else {
				header = NOUN_PHRASE_CSV_HEADERS + NEW_LINE;
			}
			
			if(writeToFile) {
				File file = new File("np_annotated_" + sdf.format(new Date()) + ".csv");
				if(!file.exists())
					file.createNewFile();
				
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				
				bw.write(header);
			}
			
			while(chunkCount < limit) {
				logger.info("NP Extract Progress: " + chunkCount + "/" + limit);
				 // avoid OutOfBoundsException on .subList when chunkCount + maxChunkSize > limit
				int chunkSize = (chunkCount + maxChunkSize > limit ? limit - chunkCount : maxChunkSize);
				
				List<String> chunkList = pmidList.subList(chunkCount, chunkCount + chunkSize);
				chunkCount += chunkSize; //chunkList.size();
				
				ArrayList<Sentence> sentences = mongo.getAnnotatedSentences(chunkList, 0);
				ArrayList<String> lines = extractNounPhrases(sentences, false, wordWhitelist, position);
				
				for(String line : lines) {
					try {
						if(writeToFile)
							bw.write(keywords + line + NEW_LINE);
						else
							csvRows.add(keywords + line);
						
					} catch (IOException e) {
						logger.error("extractNounPhrasesChunked(): Error writing to file or appending csvRows. \n{}", e);
					}
				}
			}
		} catch(Exception e) {
			logger.error("Extract:extractNounPhrasesChunked(): \n{}", e);
		} finally {
			if(mongo != null)
				mongo.closeClient();
			if(bw != null)
				try {
					bw.close();
				} catch(IOException e) { 
					logger.warn("Extract:extractNounPhrasesChunked(): Error closing BufferedReader:\n{}", e);
				}
		}
		
		return csvRows;
	}
	
	public ArrayList<String> extractNounPhrases(ArrayList<Sentence> sentenceList, boolean showHeaderRow, ArrayList<String> wordWhitelist, NounPositionTypes position) {
		ArrayList<String> outputList = new ArrayList<String>();
		POSTagger tagger = new POSTagger();
		
		if(showHeaderRow)
			outputList.add(NOUN_PHRASE_CSV_HEADERS);
		
		if(position == null)
			position = NounPositionTypes.ANYWHERE;
		
		for(Sentence s : sentenceList) {
			String origSentence = tagger.getNPAnnotatedSentence(null, null, s.getWordList(), false);
			String annSentence = tagger.getNPAnnotatedSentence(null, null, s.getWordList(), true);
					
			int startPos = annSentence.indexOf("{");
			
			while(startPos != -1 && startPos < annSentence.length()) {
				StringBuilder csvRow = new StringBuilder();
				boolean buildCSVrow = true;
				
				int endPos = annSentence.indexOf("/HEAD", startPos);
				
				if(endPos > -1) {
					String nounPhrase = annSentence.substring(startPos+1, endPos);
					
					if(wordWhitelist != null && wordWhitelist.size() > 0) {
						
						buildCSVrow = false;
						String[] nounPhraseArr = nounPhrase.split(" ");
						
						for(String word : wordWhitelist) {
							String wordRegex = "(?i)(.*)" + word + "(.*)";
							
							switch(position) {
								case ANYWHERE:
									if(nounPhrase.matches(wordRegex)) {
										buildCSVrow = true;
										break;
									}							
									break;
								case HEAD:
									if(nounPhraseArr[nounPhraseArr.length-1].matches(wordRegex)) {
										buildCSVrow = true;
										break;
									}
									break;
								case HEAD_MINUS_ONE:
									if(nounPhraseArr.length >= 2 && nounPhraseArr[nounPhraseArr.length-2].matches(wordRegex)) {
										buildCSVrow = true;
										break;
									}
									break;
								case HEAD_MINUS_N:									
									if(!buildCSVrow && nounPhraseArr.length >= 3) {
										for(int i = nounPhraseArr.length-3; i >= 0; i--) {
											if(nounPhraseArr[i].matches(wordRegex)) {
												buildCSVrow = true;
												break;
											}
										}
									}
									break;
							}
						}
					}
					
					if(buildCSVrow) {
						csvRow.append(dq).append(nounPhrase).append(dq).append(DELIM).
						append(dq).append(annSentence).append(dq).append(DELIM).
						append(dq).append(origSentence).append(dq).append(DELIM).
						append(dq).append(s.getArticleId()).append(dq);
					
						outputList.add(csvRow.toString());
						trackCounts(nounPhrase, npAnnCounts);
						trackCounts(s.getArticleId(), articleIdCounts);
					}
					
					startPos = annSentence.indexOf("{", endPos);
					
				} else {
					startPos = -1; // added by SRD to deal with an issue where MongoDB queries were limited to 500 rows and the data happened
								   // to end on a phrase that had begun but had no } tag 
				}
			}
		}
		
		return outputList;
	}
	
	private boolean listContainsRegex(ArrayList<String> list, String regex) {
		boolean ret = false;
		for(String s : list) {
			if(s.matches(regex)) {
				ret = true;
				break;
			}
		}
		
		return ret;
	}
	
 	private String buildCSVRow(String articleId, String precedingSemType, String precedingToken, String semType, String token, String prepPhrase, String ppAnnotated) {
		StringBuilder csvRow = new StringBuilder();
		
		String stGroup = precedingSemType + "-" + semType;
		
		csvRow.append(dq).append(articleId).append(dq).append(DELIM).
		   append(dq).append(stGroup).append(dq).append(DELIM).
		   append(dq).append(precedingToken).append(dq).append(DELIM).
		   append(dq).append(precedingSemType).append(dq).append(DELIM).
		   append(dq).append(token).append(dq).append(DELIM).
		   append(dq).append(semType).append(dq).append(DELIM).
		   append(dq).append(prepPhrase.toString().trim()).append(dq).append(DELIM).
		   //append(dq).append(words.get(j).isPrepPhraseObject() ? "P" : "D").append(dq).append(DELIM).
		   append(dq).append(ppAnnotated).append(dq);
		
		trackCounts(stGroup, stCounts);
		trackCounts(precedingToken, modifierCounts);
		trackCounts(articleId, articleIdCounts);
		
		return csvRow.toString();
	}
	
	private void trackCounts(String key, Map<String, Integer> map) {
		Integer val = map.get(key);
		if(val == null) {
			map.put(key, 1);
		} else {
			map.put(key, ++val);
		}
	}
	
	public List<Entry<String, Integer>> getNpAnnCounts(boolean sorted) {
		Set<Entry<String, Integer>> set = npAnnCounts.entrySet();
        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
		
		if(sorted) {
	        Collections.sort( list, new Comparator<Map.Entry<String, Integer>>() {
	            public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 ) {
	                return (o2.getValue()).compareTo( o1.getValue() );
	            }
	        } );   
		}
		return list;
	}
	
	public List<Entry<String, Integer>> getSTCounts(boolean sorted) {
		Set<Entry<String, Integer>> set = stCounts.entrySet();
        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
		
		if(sorted) {
	        Collections.sort( list, new Comparator<Map.Entry<String, Integer>>() {
	            public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 ) {
	                return (o2.getValue()).compareTo( o1.getValue() );
	            }
	        } );   
		}
		return list;
	}

	public List<Entry<String, Integer>> getModifierCounts(boolean sorted) {
		Set<Entry<String, Integer>> set = modifierCounts.entrySet();
        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
		
		if(sorted) {
	        Collections.sort( list, new Comparator<Map.Entry<String, Integer>>() {
	            public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 ) {
	                return (o2.getValue()).compareTo( o1.getValue() );
	            }
	        } );   
		}
		return list;
	}
	
	public List<Entry<String, Integer>> getArticleIdCounts(boolean sorted) {
		Set<Entry<String, Integer>> set = articleIdCounts.entrySet();
        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
		
		if(sorted) {
	        Collections.sort( list, new Comparator<Map.Entry<String, Integer>>() {
	            public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 ) {
	                return (o2.getValue()).compareTo( o1.getValue() );
	            }
	        } );   
		}
		return list;
	}
	
}
