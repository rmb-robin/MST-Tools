package com.mst.extract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.mst.model.SemanticType;
import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.tools.POSTagger;

public class Extract {

	private final String PREP_PHRASE_ST_CSV_HEADERS = "Article,Relationship,Prec. Term,Prec. Term ST,Prep Phrase Term,Prep Phrase Term ST,Prep Phrase,Sentence"; 
	private final String dq = "\"";
	private final String DELIM = ",";
	private final String NEW_LINE = System.getProperty("line.separator");
	
	private Map<String, Integer> stCounts = new HashMap<String, Integer>();
	private Map<String, Integer> modifierCounts = new HashMap<String, Integer>();
	private Map<String, Integer> articleIdCounts = new HashMap<String, Integer>();
	
	public Extract() { }

	// extracts semantic type relationship between modifiers and terms of prep phrase
	public ArrayList<String> extractPrepPhraseSTRelationship(ArrayList<Sentence> sentenceList, boolean showHeaderRow, ArrayList<String> semTypeBlacklist, ArrayList<String> wordBlacklist, ArrayList<String> semTypeWhitelist, ArrayList<String> wordWhitelist, int rowLimit) {
		//TODO how to choose which sem type from subject word?
		//TODO test verb, normal, begin, prep at EOL sentences
		ArrayList<String> outputList = new ArrayList<String>();
		POSTagger tagger = new POSTagger();
		int rowCount = 0;
		
		//String[] stArray = { "spco", "mnob", "popg", "food", "mobd", "hcpp", "inpr", "plnt" };
		//String[] wordArray = { "patients" };
		
		//ArrayList<String> semTypeBlacklist = null;
		//ArrayList<String> semTypeWhitelist = null;
		
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
		
//		if(semTypeBlacklistArr != null && semTypeBlacklistArr.length > 0) {
//			// replace incoming * with regex wildcard for any four characters. used later in extract to exclude semantic types from CSV output.
//			semTypeBlacklist = new ArrayList<String>();
//			for(int i=0; i < semTypeBlacklistArr.length; i+=2) {
//				semTypeBlacklist.add(semTypeBlacklistArr[i].replace("*", "[a-z]{4}") + "-" + semTypeBlacklistArr[i+1].replace("*", "[a-z]{4}"));
//			}
//		}
		
//		if(semTypeWhitelistArr != null && semTypeWhitelistArr.length > 0) {
//			// replace incoming * with regex wildcard for any four characters. used later in extract to restrict semantic types from CSV output.
//			semTypeWhitelist = new ArrayList<String>();
//			for(int i=0; i < semTypeWhitelistArr.length; i+=2) {
//				semTypeWhitelist.add(semTypeWhitelistArr[i].replace("*", "[a-z]{4}") + "-" + semTypeWhitelistArr[i+1].replace("*", "[a-z]{4}"));
//			}
//		}
		
		if(showHeaderRow)
			outputList.add(PREP_PHRASE_ST_CSV_HEADERS);
		
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
								rowCount++;
								if(rowLimit > 0 && rowCount >= rowLimit) {
									return outputList;
								}
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
