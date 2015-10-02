package com.mst.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.util.Props;
import com.mst.util.StanfordNLP;
import com.mst.util.Utils;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class POSTagger {

	private String MAXENT_PATH = "";
	private String PYTHON_INPUT_FILE = "";
	private String CMD = "";
	//private HashMap<String, String> PennTreebankPOSTagest = new HashMap<String, String>();
	private boolean useStanford = false;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private Map<String, String> translateMap = new HashMap<String, String>();
	private static MaxentTagger tagger = null;
	
	public POSTagger() {
		this(false);
	}

	public POSTagger(boolean useStanford) {
		try {
			init();
			
			if(Boolean.parseBoolean(Props.getProperty("use_stanford_pos"))) {
			//if(useStanford) {
				this.useStanford = true;
				if(tagger == null) {
					tagger = new MaxentTagger(Props.getProperty("stanford_maxent_path"));
				}
			}
		} catch(Exception e) {
			logger.error("POSTagger(): {}", e);
		}
	}
	
	private void init() {
		MAXENT_PATH = Props.getProperty("maxent_path");
		PYTHON_INPUT_FILE = MAXENT_PATH + "pyInput.txt";
		CMD = "python " + MAXENT_PATH + "POSwrapper_rpc.py";
		
		translateMap.put(",", "comma");
		translateMap.put("#", "hash");
		translateMap.put(":", "colon");
	}
	
	public boolean identifyPartsOfSpeech(Sentence sentence) {
		boolean ret = true;
		
		try {
			ArrayList<WordToken> words = sentence.getWordList();
			
			if(words == null || words.isEmpty()) {
				ret = false;
			} else {
				if(useStanford) {
					StanfordNLP stanford = new StanfordNLP(tagger);
					sentence.setWordList(stanford.identifyPartsOfSpeech(words));
					//System.out.println("POS result: " + sentence.getWordList().get(0).getPOS());
				} else {
					StringBuilder sb = new StringBuilder();
					
					// build a Python input string
					for(WordToken word : words) {
						String xlate = translateMap.get(word.getToken());
						if(xlate == null)
							sb.append(word.getToken());
						else
							sb.append(xlate);
						sb.append("<>");  // attempting to pick a delimiter that will never come across as a word
					}
					// write to a file for use as input by the python script
					writeToFile(sb.toString());
		
					String pyList = Utils.execCmd(CMD).trim();
					
					String[] pyArray = pyList.split("<>");
					
					for(int i=0; i < pyArray.length; i++) {
						words.get(i).setPOS(pyArray[i]);
					}
					
					sentence.setWordList(words);
				}
			}
		} catch(Exception e) {
			//System.out.println("ERROR in identifyPartsOfSpeech: " + e.toString());
			ret = false;
			logger.error("identifyPartsOfSpeech(): {}\n{}", e, sentence.getFullSentence());
			
			e.printStackTrace();
		}
		return ret;
	}
	
	public void setUseStanford(boolean value) {
		useStanford = value;
	}
	
	// TODO this doesn't belong here
	/*public String getAnnotatedMarkup(ArrayList<WordToken> wordList) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> markup = new ArrayList<String>();

		//boolean insideVOB = false;
		boolean ppBegin = true;
		boolean npBegin = true;
		boolean tokenAdded = false;
		//boolean insideDepPhrase = false;
		
		String vobOpen = "<vob>", vobClose = "</vob>";
		String lverbSpan = "<lv>"; // linking verb
		//String averbSpan = "<av>"; // action verb
		String prepSpan = "<pp>";
		String nounSpan = "<np>";
		String dpSpan = "<dp>";
		String stSpan = "<st><sup>";
		String posSpan = "<pos>/";

		int index = 0;
		
		for(WordToken word : wordList) {
			try {
				//String token = word.isPunctuation() ? word.getToken() : word.getToken() + posSpan + word.getPOS() + "</pos>";
				String token = word.getToken() + posSpan + word.getPOS() + "</pos>";
				
				//String st = "";
//				for(SemanticType x : word.getSemanticTypeList()) {
//					// additional check to avoid returning extraneous semantic types, e.g. "right" returning 'bpoc' for the phrase "right kidney"
//					// see MetaMapWrapper.findAllWordTokenIndices()
//					if(x.getToken().equalsIgnoreCase(word.getToken()))
//						st += "," + x.getSemanticType();
//				}
				
				if(word.getSemanticType() != null)
					token += stSpan + word.getSemanticType() + "</sup></st>";
					//token += stSpan + st.substring(1) + "</sup></st>";
					
				
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
					markup.add(tokenAdded ? "<lv>/SUBJ</lv>" : token + "<lv>/SUBJ</lv>");
					tokenAdded = true;
				} else if(word.isLinkingVerb()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						markup.set(markup.size() - 1, "<lv>[</lv>" + temp + "<lv>]</lv>" + temp);
					} else {
						markup.add("<lv>[</lv>" + token + "<lv>]</lv>");
						tokenAdded = true;
					}
				} else if(word.isLinkingVerbSubjectComplement()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						markup.set(markup.size() - 1, temp + "<lv>/SUBJC</lv>");
					} else {
						markup.add(token + "<lv>/SUBJC</lv>");
						tokenAdded = true;
					}
				}
				
				// ACTION VERBS *********************
				if(word.isActionVerbSubject()) {
					markup.add(tokenAdded ? "<av>/SUBJ</av>" : token + "<av>/SUBJ</av>");
					tokenAdded = true;
				} else if(word.isActionVerb()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						markup.set(markup.size() - 1, "<av>[</av>" + temp + "<av>]</av>");
					} else {
						markup.add("<av>[</av>" + token + "<av>]</av>");
						tokenAdded = true;
					}
				} else if(word.isActionVerbDirectObject()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						markup.set(markup.size() - 1, temp + "<av>/OBJ</av>");
					} else {
						markup.add(token + "<av>/OBJ</av>");
						tokenAdded = true;
					}
				}
				
				// INFINITIVE VERBS *********************
				if(word.isInfinitiveHead()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						//markup.set(markup.size() - 1, "<iv>[</iv>" + temp);
						markup.set(markup.size() - 1, "<iv>" + temp);
					} else {
						//markup.add("<iv>[</iv>" + token);
						markup.add("<iv>" + token);
						tokenAdded = true;
					}
				} else if(word.isInfinitiveVerb()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						//markup.set(markup.size() - 1, temp + "<iv>]</iv>");
						markup.set(markup.size() - 1, temp + "</iv>");
					} else {
						//markup.add(token + "<iv>]</iv>");
						markup.add(token + "</iv>");
						tokenAdded = true;
					}
				}
				
				// PREPOSITIONAL VERBS *********************
				if(word.isPrepositionalVerb()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						markup.set(markup.size() - 1, "<pv>" + temp + "</pv>");
					} else {
						markup.add("<pv>" + token + "</pv>");
						tokenAdded = true;
					}
				}
				
				// MODAL AUX VERBS *********************
				if(word.isModalAuxVerb()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						markup.set(markup.size() - 1, "<mv>" + temp);
					} else {
						markup.add("<mv>" + token);
						tokenAdded = true;
					}
				} else if(word.isModalAuxVerb()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						markup.set(markup.size() - 1, temp + "</mv>");
					} else {
						markup.add(token + "</mv>");
						tokenAdded = true;
					}
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
					//markup.add(tokenAdded ? prepSpan + "]</pp>" : token + prepSpan + "]</pp>");
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
					markup.add(tokenAdded ? nounSpan + "]</np>" : token + nounSpan + "]</np>");
					//markup.add(tokenAdded ? nounSpan + "/HEAD]</np>" : token + nounSpan + "/HEAD]</np>");
					npBegin = true;
					tokenAdded = true;
				}
	
				// DEPENDENT PHRASES  *********************
				if(word.isDependentPhraseBegin()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);
						markup.set(markup.size() - 1, dpSpan + temp);
					} else {
						markup.add(dpSpan + token);
						tokenAdded = true;
					}
				}
				if(word.isDependentPhraseEnd()) {
					if(tokenAdded) {
						String temp = markup.get(markup.size() - 1);

						markup.set(markup.size() - 1, temp + "</dp>");
					} else {
						markup.add(token + "</dp>");
						tokenAdded = true;
					}
				}
				
				if(!tokenAdded) {
					markup.add(token);
				}
				tokenAdded = false;
				
			} catch(Exception e) {
				logger.error("getAnnotatedMarkup(): {}", e);
			}
			index++;
		}
		
		for(String item : markup) {
			sb.append(item).append(" ");
		}
		
		return sb.toString();
	}*/
 	
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
