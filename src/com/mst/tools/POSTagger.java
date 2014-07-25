package com.mst.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.mst.model.PrepPhraseToken;
import com.mst.model.WordToken;
import com.mst.tools.Tokenizer;
import com.mst.util.Props;
import com.mst.util.Utils;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class POSTagger {

	// !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
	private final String PUNC = "!|\"|#|\\$|%|&|'|\\(|\\)|\\*|\\+|,|-|\\.|/|:|;|<|=|>|\\?|@|\\[|\\\\|]|\\^|_|`|\\{|\\||}|~";
	private final String PUNC_ALLOW_PARENS = "!|\"|#|\\$|%|&|'|\\)|\\*|\\+|,|-|\\.|/|:|;|<|=|>|\\?|@|\\[|\\\\|]|\\^|_|`|\\{|\\||}|~";
	private String MAXENT_PATH = "";
	private String PYTHON_INPUT_FILE = "";
	private String CMD = "";

	//private HashMap<String, String> PennTreebankPOSTagest = new HashMap<String, String>();
	private ArrayList<WordToken> taggedWordList = null;
	
	private MaxentTagger tagger = null;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public POSTagger() {
		loadTagset();
		try {
			MAXENT_PATH = Props.getProperty("maxent_path");
			PYTHON_INPUT_FILE = MAXENT_PATH + "pyInput.txt";
			CMD = "python " + MAXENT_PATH + "POSwrapper_rpc.py";
			
		} catch(Exception e) {
			logger.error("POSTagger(): {}", e);
		}
	}

	public POSTagger(boolean loadStanford) {
		//loadTagset();
		try {
			MAXENT_PATH = Props.getProperty("maxent_path");
			PYTHON_INPUT_FILE = MAXENT_PATH + "pyInput.txt";
			CMD = "python " + MAXENT_PATH + "POSwrapper_rpc.py";
			
			if(loadStanford) {
				tagger = new MaxentTagger("/Users/scottdaugherty/Downloads/stanford-postagger-2014-06-16/models/english-left3words-distsim.tagger");
			}
			
		} catch(Exception e) {
			logger.error("POSTagger(): {}", e);
		}
	}
	
	public ArrayList<WordToken> identifyPartsOfSpeech(String input) {
		Tokenizer t = new Tokenizer();
		
		// split words
		ArrayList<WordToken> wordList = t.splitWords(input);
		
		return identifyPartsOfSpeechLegacy(wordList);
	}
	
	public boolean identifyPartsOfSpeechStanford(ArrayList<WordToken> wordList) {
		boolean ret = true;
		
		try {
			//MaxentTagger tagger = new MaxentTagger("/Users/scottdaugherty/Downloads/stanford-postagger-2014-06-16/models/english-bidirectional-distsim.tagger");
			
			StringBuilder sb = new StringBuilder();
			
			for(WordToken word : wordList) {
				sb.append(word.getNormalizedForm());
				sb.append(" ");
			}
			
			String result = tagger.tagTokenizedString(sb.toString());
			String[] tagged = result.split(" ");
			
			for(int i=0; i < wordList.size(); i++) {
				String[] split = tagged[i].split("_");
				if(wordList.get(i).getToken().matches(",|(|)"))
					wordList.get(i).setPOS(wordList.get(i).getToken());
				else
					wordList.get(i).setPOS(split[split.length-1]);
			}
	
			taggedWordList = wordList;
			
		} catch(Exception e) {
			ret = false;
			logger.error("identifyPartsOfSpeechStanford(): {}", e);
		}
		
		return ret;
	}
	
	public boolean identifyPartsOfSpeech(ArrayList<WordToken> wordList) {
		boolean ret = true;
		
		try {
			StringBuilder sb = new StringBuilder();
			
			// build a Python input string
			for(WordToken word : wordList) {
				sb.append(word.getNormalizedForm());
				sb.append("<>");  // attempting to pick a delimiter that will never come across as a word
			}
			// write to a file for use as input by the python script
			writeToFile(sb.toString());

			String pyList = Utils.execCmd(CMD).trim();
			
			String[] pyArray = pyList.split("<>");
			
			for(int i=0; i < pyArray.length; i++) {
				wordList.get(i).setPOS(pyArray[i]);
			}
	
			taggedWordList = wordList;
			
		} catch(Exception e) {
			ret = false;
			logger.error("identifyPartsOfSpeech(): {}", e);
		}
		return ret;
	}

	public boolean identifyNounPhrases(ArrayList<WordToken> wordList) {
		boolean ret = true;
		if(taggedWordList == null) {
			identifyPartsOfSpeech(wordList);
		}
		
		int headIndex = 0;
		try {
			//TODO don't mark as head if surrounded by parens?
			for(int i=taggedWordList.size()-1; i >= 0; i--) {
				if(taggedWordList.get(i).getPOS().matches("NN|NNS")) {
					if(headIndex == 0) {
						headIndex = i;
					} else {
						taggedWordList.get(headIndex).setNounPhraseHead(true);
						taggedWordList.get(i).setNounPhraseModifier(true);
					}
				} else if(taggedWordList.get(i).getPOS().matches("JJ|RB|" + PUNC)) {
					if(headIndex > 0) {
						taggedWordList.get(headIndex).setNounPhraseHead(true);
						taggedWordList.get(i).setNounPhraseModifier(true);
					}
				} else {
					headIndex = 0;
				}
			}
		} catch(Exception e) {
			ret = false;
			logger.error("identifyNounPhrases() {}", e);
		}
		return ret;
	}
	
	public boolean identifyPrepPhrases(ArrayList<WordToken> wordList) {
		boolean ret = true;
		
		// TODO I suspect that there are some logic problems with this code (identifying OBJ, etc)
		List<String> defaultPrepositionList = Arrays.asList("after","although","among","as","at","before","between","by","during",
															"for","from","in","of","on","over","per","than","that","to","while",
															"with","within","without");
		if(taggedWordList == null) {
			identifyPartsOfSpeech(wordList);
		}
		
		try {
			
			List<Integer> comprisingTokenIndex = new ArrayList<Integer>();
			
			for(int i=0; i < taggedWordList.size(); i++) {
				WordToken currentWord = taggedWordList.get(i);
				
				if(defaultPrepositionList.contains(currentWord.getToken().toLowerCase())) {
					// loop through remaining words in the sentence
					for(int j=i+1; j < taggedWordList.size(); j++) {
						String nextWord = taggedWordList.get(j).getToken();
						String nextPOS = taggedWordList.get(j).getPOS(); 
						
						if(nextPOS.matches("IN|TO|VB.*")) // break on preposition or verb
							break;
						else if(nextWord.equals(";")) // a semicolon always stops the phrase
							break;
						else if(nextWord.equals(",")) // a comma stops the phrase
							// unless it is preceded and followed by an adjective
							if(!(taggedWordList.get(j-1).getPOS().equals("JJ") && taggedWordList.get(j+1).getPOS().equals("JJ")))
								break;
						
						// no breaks; add index of current word to list of comprising tokens
						comprisingTokenIndex.add(j);
						
						// stop on a noun unless the following token is NOT a coordinating conjunction or other noun
						// size() check to avoid OutOfBounds exception
						if(nextPOS.matches("NN|NNS") && j < taggedWordList.size()-1 &&
						    !taggedWordList.get(j+1).getPOS().matches("CC|NN|NNS"))
							break;		
					}
	
					if(comprisingTokenIndex.size() > 0) {
						// prepend initial matched preposition
						comprisingTokenIndex.add(0, i);
						
						// loop through list of indexes that make up the prep phrase
						for(int j=0; j < comprisingTokenIndex.size(); j++) {
							int index = comprisingTokenIndex.get(j);
													
							if(j == comprisingTokenIndex.size()-1) { // last item in list
								do {
									taggedWordList.get(index).setPrepPhraseObject(true);
									index--;
								} while(taggedWordList.get(index).getPOS().matches("CC|,|NN|NNS"));
								
							} else {
								// set as prep phrase member (for grouping with brackets)
								taggedWordList.get(index).setPrepPhraseMember(true);
							}
						}
						
						comprisingTokenIndex.clear();
					}
				}
			}
		} catch(Exception e) {
			ret = false;
			logger.error("identifyPrepPhrases(): {}", e);
		}
		
		return ret;
	}
	
	public String getNPAnnotatedSentence(String keyword, String extractionTerm, ArrayList<WordToken> taggedWordList, boolean annotate) {
		StringBuilder sb = new StringBuilder();
		boolean showOpenBracket = true;
		
		try {
			// TODO KW comes after HEAD?
			// TODO assign OBJ to NP-annotated
			for(int i=0; i < taggedWordList.size(); i++) {
				if(annotate) {
					String prefix = "/";
					
					if(taggedWordList.get(i).nounPhraseModifier() && showOpenBracket) {
						sb.append("{");
						showOpenBracket = false;
					}
					
					sb.append(taggedWordList.get(i).getToken());
					
					if(keyword != null && taggedWordList.get(i).getToken().matches(keyword.concat("(.*)"))) {
						sb.append(prefix).append("KW");
						prefix = "+";
					}
					
					if(extractionTerm != null && taggedWordList.get(i).getToken().matches(extractionTerm)) {
						sb.append(prefix).append("EXT");
						prefix = "+";
					}
					
					if(taggedWordList.get(i).nounPhraseHead()) {
						sb.append(prefix).append("HEAD}");
						showOpenBracket = true;
					}
				} else {
					// pass annotate == false to return the sentence with no markup
					sb.append(taggedWordList.get(i).getToken());
				}
				
				if(i < taggedWordList.size()-1 
						&& !taggedWordList.get(i+1).getToken().matches(PUNC_ALLOW_PARENS)
						&& !taggedWordList.get(i).getToken().matches("\\("))
					sb.append(" ");
			}
		} catch(Exception e) {
			logger.error("getNPAnnotatedSentence(): {}", e);
			sb.append("*** Unable to build NP-annotated sentence. See error log for details. ***");
		}
		
		return sb.toString();
	}
	
	public String getNPAnnotatedSentence(String keyword, String extractionTerm, boolean annotate) throws Exception {
		if(taggedWordList == null) {
			throw new Exception("Please execute tagSentence() before attempting to getNPAnnotatedSentence().");
		}
		
		return getNPAnnotatedSentence(keyword, extractionTerm, taggedWordList, annotate);
	}
	
	public String getPPAnnotatedSentence(String keyword, String extractionTerm, ArrayList<WordToken> taggedWordList) {
		StringBuilder sb = new StringBuilder();
		boolean showOpenBracket = true;
		
		try {
			for(int i=0; i < taggedWordList.size(); i++) {
				String prefix = "/";
				
				if(taggedWordList.get(i).isPrepPhraseMember() && showOpenBracket){
					sb.append("[");
					showOpenBracket = false;
				}
				sb.append(taggedWordList.get(i).getToken());
				
				if(keyword != null && taggedWordList.get(i).getToken().matches(keyword.concat("(.*)"))) {
					sb.append(prefix).append("KW");
					prefix = "+";
				}
				
				if(extractionTerm != null && taggedWordList.get(i).getToken().matches(extractionTerm)) {
					sb.append(prefix).append("EXT");
					prefix = "+";
				}
				
				if(taggedWordList.get(i).isPrepPhraseObject()) {
					sb.append(prefix).append("OBJ");
					if(!taggedWordList.get(i).isPrepPhraseMember()) {
						sb.append("]");
						showOpenBracket = true;
					}
				}
				if(i < taggedWordList.size()-1 && !taggedWordList.get(i+1).getToken().matches(PUNC))
					sb.append(" ");
			}
		} catch(Exception e) {
			logger.error("getPPAnnotatedSentence(): {}", e);
			sb.append("*** Unable to build PP-annotated sentence. See error log for details. ***");
		}

		return sb.toString();
	}
	
	public String getPPAnnotatedSentence(String keyword, String extractionTerm) throws Exception {
		if(taggedWordList == null) {
			throw new Exception("Please execute tagSentence() before attempting to getPPAnnotatedSentence().");
		}
		
		return getPPAnnotatedSentence(keyword, extractionTerm, taggedWordList);
	}
	
	// Attempt to mimic Eric's output for prep phrase identification. Not used by the camel processes.
	/*
	Homebrew prepositional phrase identifier.  Should be superseded by an effective chunker or constituency parser.

	Algorithm:
	1) Identify prepositions in a sentence.  This can be done either from a list of prepositions passed to the __init__ method,
	    or by identifying every word token tagged as IN or TO (if preposition_list == None).
	2) Start a phrase which currently contains only the preposition.
	3) For each token succeeding the preposition:
	   a) If the token is a verb, stop and return the phrase, excluding the verb.
	   b) If the token is a preposition, stop and return the phrase, excluding the preposition.
	   c) If the token is a comma or semicolon, stop and return the phrase, excluding the token, UNLESS it is a comma and both
	       the token immediately before and after are adjectives.
	   d) If the token is a noun, stop and return the phrase INCLUDING the noun,
	   e) UNLESS the token after the noun is another noun or a coordinating conjunction, in which case include the noun or conjunction
	       and continue the phrase.
	4) Identify the token preceding the prepositional phrase and attach it to the returned annotation - some consumers are only
	    interested in phrases which follow e.g. a noun or adverb.
	*/
	public List<PrepPhraseToken> identifyPrepPhrasesLegacy(ArrayList<WordToken> wordList) {
		// TODO I suspect that there are some logic problems with this code (identifying OBJ, etc)
		// Camel doesn't at this point care about the PrePhraseToken list, only the boolean tags added to taggedWordList
		List<PrepPhraseToken> prepPhraseTokens = new ArrayList<PrepPhraseToken>();
		
		List<String> defaultPrepositionList = Arrays.asList("after","although","among","as","at","before","between","by","during",
															"for","from","in","of","on","over","per","than","that","to","while",
															"with","within","without");
		if(taggedWordList == null) {
			identifyPartsOfSpeech(wordList);
		}
		
		List<Integer> comprisingTokenIndex = new ArrayList<Integer>();
		
		for(int i=0; i < taggedWordList.size(); i++) {
			WordToken currentWord = taggedWordList.get(i);
			
			if(defaultPrepositionList.contains(currentWord.getToken().toLowerCase())) {
				// loop through remaining words in the sentence
				for(int j=i+1; j < taggedWordList.size(); j++) {
					String nextWord = taggedWordList.get(j).getToken();
					String nextPOS = taggedWordList.get(j).getPOS(); 
					
					// break on preposition or verb
					if(nextPOS.matches("IN|TO|VB.*"))
						break;
					// a semicolon always stops the phrase
					else if(nextWord.equals(";"))
						break;
					// a comma stops the phrase
					else if(nextWord.equals(","))
						// unless it is preceded and followed by an adjective
						if(!(taggedWordList.get(j-1).getPOS().equals("JJ") && taggedWordList.get(j+1).getPOS().equals("JJ")))
							break;
					
					// no breaks; add index of current word to list of comprising tokens
					comprisingTokenIndex.add(j);
					
					// stop on a noun unless the following token is NOT coordinating conjunction or other noun
					// size() check to avoid OutOfBounds exception
					if(nextPOS.matches("NN|NNS") && j < taggedWordList.size()-1 &&
					    !taggedWordList.get(j+1).getPOS().matches("CC|NN|NNS"))
						break;		
				}

				if(comprisingTokenIndex.size() > 0) {
					List<String> comprisingTokens = new ArrayList<String>();
					StringBuilder value = new StringBuilder();
					
					// prepend initial matched preposition
					comprisingTokenIndex.add(0, i);
					
					// loop through list of indexes that make up the prep phrase
					for(int j=0; j < comprisingTokenIndex.size(); j++) {
						int index = comprisingTokenIndex.get(j);
						
						value.append(taggedWordList.get(index).getToken()).append(" "); // ex. "from their local environment"
						comprisingTokens.add(taggedWordList.get(index).getToken() + "/" + taggedWordList.get(index).getPOS()); // ex. ["from/IN", "their/PRP$", "local/JJ", "environment/NN"]
						
						if(j == comprisingTokenIndex.size()-1) { // last item in list
							// tag OBJs of prep phrase if conjunction or comma
							do {
								taggedWordList.get(index).setPrepPhraseObject(true);
								index--;
							} while(taggedWordList.get(index).getPOS().matches("CC|,"));
							
							// if final word is a noun, tag as OBJ
							// TODO potential issue if more than one OBJ exists after the CC
							if(taggedWordList.get(index).getPOS().matches("NN|NNS"))
								taggedWordList.get(index).setPrepPhraseObject(true);
							
						} else {
							// set as prep phrase member (for grouping with brackets)
							taggedWordList.get(index).setPrepPhraseMember(true);
						}
					}
					
					//int begin = currentWord.getBegin();
					//int end = begin + value.length()-1; // account for trailing space
					// set preceding token if not at beginning of sentence
					//String precedingToken = i>0 ? taggedWordList.get(i-1).getToken() + "/" + taggedWordList.get(i-1).getPOS() : "";
					
					//prepPhraseTokens.add(new PrepPhraseToken(begin, end, currentWord.getToken(), "TODO", value.toString().trim(), precedingToken, comprisingTokens));
					comprisingTokenIndex.clear();
				}
			}
		}
		
//		for(PrepPhraseToken ppt : prepPhraseTokens) {
//			System.out.println("preposition_token: " + ppt.getToken());
//			System.out.println("begin: " + ppt.getBegin());
//			System.out.println("end: " + ppt.getEnd());
//			System.out.println("preceding_token: " + ppt.getPrecedingToken());
//			System.out.println("value: " + ppt.getValue());
//			String cts = "";
//			for(String ct : ppt.getComprisingTokens()) {
//				cts += " " + ct;
//			}
//			System.out.println("comprising tokens: " + cts.trim() + "\n");
//		}
		
		return prepPhraseTokens;
	}
	
	public ArrayList<WordToken> getTaggedWordList() {
		return taggedWordList;
	}
	
	public ArrayList<WordToken> identifyNounPhrasesLegacy(ArrayList<WordToken> wordList) {

		if(taggedWordList == null) {
			identifyPartsOfSpeech(wordList);
		}
		
		int headIndex = 0;
		try {
			//TODO don't mark as head if surrounded by parens?
			for(int i=taggedWordList.size()-1; i >= 0; i--) {
				if(taggedWordList.get(i).getPOS().matches("NN|NNS")) {
					if(headIndex == 0) {
						headIndex = i;
					} else {
						taggedWordList.get(headIndex).setNounPhraseHead(true);
						taggedWordList.get(i).setNounPhraseModifier(true);
					}
				} else if(taggedWordList.get(i).getPOS().matches("JJ|RB|" + PUNC)) {
					if(headIndex > 0) {
						taggedWordList.get(headIndex).setNounPhraseHead(true);
						taggedWordList.get(i).setNounPhraseModifier(true);
					}
				} else {
					headIndex = 0;
				}
			}
		} catch(Exception e) {
			System.out.println("Error in identifyNounPhrases(): " + e.toString());
			Gson gson = new Gson();
			System.out.println(gson.toJson(wordList));
		}
		return taggedWordList;
	}
	
	// Attempt to mimic Eric's output for prep phrase identification. Not used by the camel processes.
	public ArrayList<WordToken> identifyPartsOfSpeechLegacy(ArrayList<WordToken> wordList) {
		StringBuilder sb = new StringBuilder();
		
		try {
			// build a Python input string
			for(WordToken word : wordList) {
				sb.append(word.getNormalizedForm());
				sb.append("<>");  // attempting to pick a delimiter that will never come across as a word
			}
			// write to a file for use as input by the python script
			writeToFile(sb.toString());
	
			//Utils ju = new Utils();
	
			String pyList = Utils.execCmd(CMD).trim();
			String[] pyArray = pyList.split("<>");
			
			for(int i=0; i < pyArray.length; i++) {
				wordList.get(i).setPOS(pyArray[i]);
				//String posDesc = PennTreebankPOSTagest.get(pyArray[i]);
				//wordList.get(i).setPOSDesc(posDesc == null ? "" : posDesc);
			}
	
			taggedWordList = wordList;
		} catch(Exception e) {
			System.out.println(e.toString());
		}
		return taggedWordList;
	}
	
 	private boolean writeToFile(String content) {
		boolean retVal = true;

		try {
			File file = new File(PYTHON_INPUT_FILE);
 
			if(!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();

		} catch(IOException ioe) {
			retVal = false;
			logger.error("Please ensure that 'maxent_path' is configured properly in mst-tools.properties and that the props file exists in the same " +
						 "directory as mst-tools-x.x.x.jar. {}", ioe);
		} catch(Exception e) {
			retVal = false;
			logger.error("writeToFile(): {}", e);
		}
		
		return retVal;
	}

 	private void loadTagset() {
//		PennTreebankPOSTagest.put("CC", "Coordinating conjunction");
//		PennTreebankPOSTagest.put("CD", "Cardinal number");
//		PennTreebankPOSTagest.put("DT", "Determiner");
//		PennTreebankPOSTagest.put("EX", "Existential there");
//		PennTreebankPOSTagest.put("FW", "Foreign word");
//		PennTreebankPOSTagest.put("IN", "Preposition or subordinating conjunction");
//		PennTreebankPOSTagest.put("JJ", "Adjective");
//		PennTreebankPOSTagest.put("JJR", "Adjective, comparative");
//		PennTreebankPOSTagest.put("JJS", "Adjective, superlative");
//		PennTreebankPOSTagest.put("LS", "List item marker");
//		PennTreebankPOSTagest.put("MD", "Modal");
//		PennTreebankPOSTagest.put("NN", "Noun, singular or mass");
//		PennTreebankPOSTagest.put("NNS", "Noun, plural");
//		PennTreebankPOSTagest.put("NNP", "Proper noun, singular");
//		PennTreebankPOSTagest.put("NNPS", "Proper noun, plural");
//		PennTreebankPOSTagest.put("PDT", "Predeterminer");
//		PennTreebankPOSTagest.put("POS", "Possessive ending");
//		PennTreebankPOSTagest.put("PRP", "Personal pronoun");
//		PennTreebankPOSTagest.put("PRP$", "Possessive pronoun");
//		PennTreebankPOSTagest.put("RB", "Adverb");
//		PennTreebankPOSTagest.put("RBR", "Adverb, comparative");
//		PennTreebankPOSTagest.put("RBS", "Adverb, superlative");
//		PennTreebankPOSTagest.put("RP", "Particle");
//		PennTreebankPOSTagest.put("SYM", "Symbol");
//		PennTreebankPOSTagest.put("TO", "to");
//		PennTreebankPOSTagest.put("UH", "Interjection");
//		PennTreebankPOSTagest.put("VB", "Verb, base form");
//		PennTreebankPOSTagest.put("VBD", "Verb, past tense");
//		PennTreebankPOSTagest.put("VBG", "Verb, gerund or present participle");
//		PennTreebankPOSTagest.put("VBN", "Verb, past participle");
//		PennTreebankPOSTagest.put("VBP", "Verb, non-3rd person singular present");
//		PennTreebankPOSTagest.put("VBZ", "Verb, 3rd person singular present");
//		PennTreebankPOSTagest.put("WDT", "Wh-determiner");
//		PennTreebankPOSTagest.put("WP", "Wh-pronoun");
//		PennTreebankPOSTagest.put("WP$", "Possessive wh-pronoun");
//		PennTreebankPOSTagest.put("WRB", "Wh-adverb");
	}
}
