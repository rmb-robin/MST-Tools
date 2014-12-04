package com.mst.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mst.model.SemanticType;
import com.mst.model.TokenPosition;
import com.mst.model.WordToken;
import com.mst.tools.Tokenizer;
import com.mst.util.Props;
import com.mst.util.StanfordNLP;
import com.mst.util.Utils;

//import edu.stanford.nlp.tagger.maxent.MaxentTagger;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class POSTagger {

	private String MAXENT_PATH = "";
	private String PYTHON_INPUT_FILE = "";
	private String CMD = "";
	private int verbCount=0, prepCount=0, nounCount=0;
	private List<TokenPosition> negation = new ArrayList<TokenPosition>();
	private boolean beginsWithPreposition = false;
	
	//private HashMap<String, String> PennTreebankPOSTagest = new HashMap<String, String>();
	private ArrayList<WordToken> taggedWordList = null;
	
	//private MaxentTagger stanfordTagger = null;
	private boolean useStanford = false;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public POSTagger() {
		this(false);
	}

	public POSTagger(boolean useStanford) {
		try {
			init();
			
			if(useStanford) {
				this.useStanford = useStanford;
				//stanfordTagger = new MaxentTagger(Props.getProperty("stanford_maxent_path"));
			}
			
		} catch(Exception e) {
			logger.error("POSTagger(): {}", e);
		}
	}
	
	private void init() {
		MAXENT_PATH = Props.getProperty("maxent_path");
		PYTHON_INPUT_FILE = MAXENT_PATH + "pyInput.txt";
		CMD = "python " + MAXENT_PATH + "POSwrapper_rpc.py";
	}
	
	public ArrayList<WordToken> identifyPartsOfSpeech(String input) {
		Tokenizer t = new Tokenizer();
		
		// split words
		ArrayList<WordToken> wordList = t.splitWords(input);
		
		return identifyPartsOfSpeechLegacy(wordList);
	}
	
	public boolean identifyPartsOfSpeech(ArrayList<WordToken> wordList) {
		boolean ret = true;
		
		try {
			if(useStanford) {
				StanfordNLP stanford = new StanfordNLP();
				taggedWordList = stanford.identifyPartsOfSpeech(wordList);
	
			} else {
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
					if(pyArray[i].startsWith("VB"))
						verbCount++;
					else if(pyArray[i].startsWith("NN"))
						nounCount++;
					else if(pyArray[i].matches("IN|TO")) {
						prepCount++;
						if(i == 0) { // first word of sentence
							beginsWithPreposition = true;
						}
					}
					
					if(wordList.get(i).matchesNegation()) {
						negation.add(new TokenPosition(wordList.get(i).getToken(), i));
					}
				}
		
				taggedWordList = wordList;
			}
			
		} catch(Exception e) {
			ret = false;
			logger.error("identifyPartsOfSpeech(): {}", e);
		}
		return ret;
	}

	public ArrayList<WordToken> getTaggedWordList() {
		return taggedWordList;
	}
	
	public int getVerbCount() {
		return verbCount;
	}
	
	public int getPrepCount() {
		return prepCount;
	}
		
	public int getNounCount() {
		return nounCount;
	}
		
	public boolean beginsWithPreposition() {
		return beginsWithPreposition;
	}
	
	public List<TokenPosition> getNegation() {
		return this.negation;
	}
	
	// TODO this doesn't belong here
	public String getAnnotatedMarkup(ArrayList<WordToken> wordList) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> markup = new ArrayList<String>();

		//boolean insideVOB = false;
		boolean ppBegin = true;
		boolean npBegin = true;
		boolean tokenAdded = false;

		String vobOpen = "<vob>", vobClose = "</vob>";
		String lverbSpan = "<lv>";
		String prepSpan = "<pp>";
		String nounSpan = "<np>";
		String stSpan = "<st><sup>";
		String posSpan = "<pos>/";

		for(WordToken word : wordList) {
			try {
				String token = word.isPunctuation() ? word.getToken() : word.getToken() + posSpan + word.getPOS() + "</pos>";;
				
				String st = "";
				for(SemanticType x : word.getSemanticTypeList()) {
					// additional check to avoid returning extraneous semantic types, e.g. "right" returning 'bpoc' for the phrase "right kidney"
					// see MetaMapWrapper.findAllWordTokenIndices()
					if(x.getToken().equalsIgnoreCase(word.getToken()))
						st += "," + x.getSemanticType();
				}
				
				if(st != "")
					token += stSpan + st.substring(1) + "</sup></st>";
				
				// VERBS OF BEING *********************
				if(word.isVerbOfBeingSubject()) {
					markup.add(token + vobOpen + "/SUBJ" + vobClose);
					tokenAdded = true;
				} else if(word.isVerbOfBeing()) {
					markup.add(vobOpen + "[" + vobClose + token + vobOpen + "]" + vobClose);
					//markup.add(vobSpan + token + "</vob>");
					//insideVOB = true;
					tokenAdded = true;
				}// else if(!word.isVerbOfBeingMember && insideVOB) {
				//	String temp = markup.get(markup.size() - 1);
				//	markup.set(markup.size() - 1, temp + vobSpan + "]</vob>");
				//	insideVOB = false;
				//}
				
				if(word.isVerbOfBeingSubjectComplement()) {
					markup.add(tokenAdded ? vobOpen + "/SUBJC" + vobClose : token + vobOpen + "/SUBJC" + vobClose);
					tokenAdded = true;
				}
				
				// LINKING VERBS *********************
				if(word.isLinkingVerbSubject()) {
					markup.add(tokenAdded ? lverbSpan + "/SUBJ</lv>" : token + lverbSpan + "/SUBJ</lv>");
					tokenAdded = true;
				} else if(word.isLinkingVerb()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						markup.set(markup.size() - 1, lverbSpan + "[</lv>" + temp + lverbSpan + "]</lv>" + temp);
					} else {
						markup.add(lverbSpan + "[</lv>" + token + lverbSpan + "]</lv>");
						tokenAdded = true;
					}
				} else if(word.isLinkingVerbSubjectComplement()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						markup.set(markup.size() - 1, temp + lverbSpan + "/SUBJC</lv>");
					} else {
						markup.add(token + lverbSpan + "/SUBJC]</lv>");
						tokenAdded = true;
					}
					//insideVOB = false;
				}
				
				// PREP PHRASES *********************
				if(word.isPrepPhraseMember()) {
					if(word.isPrepPhraseObject()) {
						markup.add(tokenAdded ? prepSpan + "/OBJ</pp>" : token + prepSpan + "/OBJ</pp>");
						tokenAdded = true;
					} else {
						if(ppBegin) {
							if(tokenAdded) {
								String temp = markup.get(markup.size() - 1);
								markup.set(markup.size() - 1, prepSpan + "[</pp>" + temp);
							} else {
								markup.add(prepSpan + "[</pp>" + token);
								tokenAdded = true;
							}
							ppBegin = false;
						}
					}
				} else if(word.isPrepPhraseObject()) {
					markup.add(tokenAdded ? prepSpan + "/OBJ]</pp>" : token + prepSpan + "/OBJ]</pp>");
					tokenAdded = true;
					ppBegin = true;
				}
	
				// NOUN PHRASES  *********************
				if(word.isNounPhraseModifier()) {
					if(npBegin) {
						if(tokenAdded) {
							String temp = markup.get(markup.size() - 1);
							markup.set(markup.size() - 1, nounSpan + "[</np>"	+ temp);
						} else {
							markup.add(nounSpan + "[</np>" + token);
							tokenAdded = true;
						}
						npBegin = false;
					}
				} else if(word.isNounPhraseHead()) {
					markup.add(tokenAdded ? nounSpan + "/HEAD]</np>" : token + nounSpan + "/HEAD]</np>");
					npBegin = true;
					tokenAdded = true;
				}
	
				if(!tokenAdded) {
					markup.add(token);
				}
				tokenAdded = false;
				
			} catch(Exception e) {
				logger.error("getAnnotatedMarkup(): {}", e);
			}
		}
		
		for(String item : markup) {
			sb.append(item).append(" ");
		}
		
		return sb.toString();
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
