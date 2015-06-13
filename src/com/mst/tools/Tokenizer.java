package com.mst.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mst.model.SentenceToken;
import com.mst.model.WordToken;

public class Tokenizer {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private Map<String, String> translateMap = new HashMap<String, String>();
	//https://www.regex101.com/r/dB1gO3/5
	private String YEAR_OLD_REGEX = "(?<=\\d)((?i)[\\s-]*YEAR[\\s-]*OLD|\\s*yr?\\.?\\/?o\\.?)(?!m)"; 
	private String YEAR_OLD_REPL = "-year-old"; 
	private String YEAR_OLD_SEX_REGEX = "(?<=\\d)yom";
	private String YEAR_OLD_SEX_REPL = "-year-old male";
	
	public Tokenizer() { 
		init();
	}
	
	private void init() {
		
		translateMap.put("abd","abdominal");
		translateMap.put("aggresive","aggressive");
		translateMap.put("asap","atypical small acinar hyperplasia");
		translateMap.put("bengin","benign");
		translateMap.put("bx","biopsy");
		translateMap.put("boney","bony");
		translateMap.put("ca","cancer");
		translateMap.put("c/o","complains of");
		translateMap.put("cabg","coronary artery bypass graft");
		translateMap.put("bladder scope","cystourethroscopy");
		translateMap.put("cysto","cystourethroscopy");
		translateMap.put("cystoscopy","cystourethroscopy");
		translateMap.put("degaralix","degarilex");
		translateMap.put("degarelix","degarilex");
		translateMap.put("degarleix","degarilex");
		translateMap.put("degerelix","degarilex");
		translateMap.put("dx","diagnosed");
		translateMap.put("d/w","discussed with");
		translateMap.put("f/u","follow up");
		translateMap.put("follow-up","follow up");
		translateMap.put("followup","follow up");
		translateMap.put("gl","gleason");
		translateMap.put("gleason's","gleason");
		translateMap.put("gleasono","gleason");
		translateMap.put("hg","high grade");
		translateMap.put("hgpin","high grade prostatic intraepithelial neoplasia");
		translateMap.put("hx","history");
		translateMap.put("h/o","history of");
		translateMap.put("homronal","hormonal");
		translateMap.put("hormoal","hormonal");
		translateMap.put("hormonl","hormonal");
		translateMap.put("horomonal","hormonal");
		translateMap.put("inj","injection");
		translateMap.put("imrt","intensity modulated radiation therapy");
		translateMap.put("lh/rh","lhrh");
		translateMap.put("lhrr","lhrh");
		translateMap.put("luporn","lupron");
		translateMap.put("luppron","lupron");
		translateMap.put("lupro","lupron");
		translateMap.put("luproj","lupron");
		translateMap.put("lupronn","lupron");
		translateMap.put("luq","lupron");
		translateMap.put("luron","lupron");
		translateMap.put("lurpn","lupron");
		translateMap.put("lurpon","lupron");
		translateMap.put("lymphnode","lymph node");
		translateMap.put("metastatis","metastasis");
		translateMap.put("metasteses","metastasis");
		translateMap.put("metastisis","metastasis");
		translateMap.put("metastses","metastasis");
		translateMap.put("mets","metastasis");
		translateMap.put("metasatic","metastatic");
		translateMap.put("metastasi","metastatic");
		translateMap.put("metastatic","metastatic");
		translateMap.put("metastatic","metastatic");
		translateMap.put("metastic","metastatic");
		translateMap.put("metastic","metastatic");
		translateMap.put("metastsaic","metastatic");
		translateMap.put("metatatic","metastatic");
		translateMap.put("metatstatic","metastatic");
		translateMap.put("metstasis","metastatic");
		translateMap.put("mos","month");
		translateMap.put("neg","negative");
		translateMap.put("nodal","node");
		translateMap.put("nodle","node");
		translateMap.put("nodular","node");
		translateMap.put("nodule","node");
		translateMap.put("ov","office visit");
		translateMap.put("onthe","on the");
		translateMap.put("osseuos","osseous");
		translateMap.put("osseus","osseous");
		translateMap.put("osseus","osseous");
		translateMap.put("pth","parathyroid hormone");
		translateMap.put("path","pathology");
		translateMap.put("pt","patient");
		translateMap.put("pt's","patient's");
		translateMap.put("rx","prescribed");
		translateMap.put("prostae","prostate");
		translateMap.put("prostat","prostate");
		translateMap.put("prosttae","prostate");
		translateMap.put("protate","prostate");
		translateMap.put("cap","prostate cancer");
		translateMap.put("p ca","prostate cancer");
		translateMap.put("+pni","prostatic intraepithelial neoplasia");
		translateMap.put("pin","prostatic intraepithelial neoplasia");
		translateMap.put("re-started","restarted");
		translateMap.put("robot","robotic");
		translateMap.put("ralp","robotic-assisted laparoscopic radical prostatectomy");
		translateMap.put("skelatal","skeletal");
		translateMap.put("s/p","status post");
		translateMap.put("testosteorne","testosterone");
		translateMap.put("testosterones","testosterone");
		translateMap.put("testostonene","testosterone");
		translateMap.put("trus bx","transrectal ultrasound prostate biopsy");
		translateMap.put("tx","treated");
		translateMap.put("vs","versus");
		translateMap.put("wt","weight");
		translateMap.put("wbbs","whole body bone scan");
		translateMap.put("xofiga","xofigo");
		translateMap.put("zofigo","xofigo");
		translateMap.put("xytiga","zytiga");

	}
	
//	public ArrayList<SentenceToken> splitSentences2(String article) {
//		// \xe2\x80\xa2 U+2022 = BULLET (•)
//		// \xe2\x80\x82 U+2002 = EN SPACE
//		// \xe2\x80\x9c U+201C = LEFT DOUBLE QUOTATION MARK (“)
//		// \xe2\x80\x9d U+201D = RIGHT DOUBLE QUOTATION MARK (”)
//		
//		String allowedCharsBetweenSentences = "[\\s\\u2022\\u2002\\u201c]";
//		// # main sentence split (with spaces) 
//		String regex1 = "(?<=\\.)\\s*([*\"\\])]|\u201D)?\\s+(?:" + allowedCharsBetweenSentences + ")*\\s*(?=[A-Z0-9(\"\\[])";
//		// # some articles don't have spaces between periods and next sentence
//		String regex2 = "(?<=\\.)\\s*(?=[A-Z][a-z]{2})";
//		Pattern falseMatchRegex = Pattern.compile("(vs|v\\.s|i\\.v)\\.$");
//		Pattern[] patterns = { Pattern.compile(regex1), Pattern.compile(regex2) };
//		
//		ArrayList<SentenceToken> sentences = new ArrayList<SentenceToken>();
//		int i = 0;
//		sentences.add(new SentenceToken(0, article.length(), article, null, ++i));
//
//		for(Pattern p : patterns) {
//			ArrayList<SentenceToken> acc = new ArrayList<SentenceToken>();
//
//			for(SentenceToken s : sentences) {
//				int lastSentenceBegin = s.getBegin();
//				
//				Matcher matcher = p.matcher(s.getToken());
//				while(matcher.find()) {
//					int sentenceEnd = s.getBegin() + matcher.start();
//					int sentenceBeg = s.getBegin() + matcher.end();
//
//					String newSentence = article.substring(lastSentenceBegin, sentenceEnd);
//					
//					if(falseMatchRegex.matcher(newSentence).matches()) {
//						break; // SRD: skip sentence altogether?
//					}
//					// # if sentence is bracketed by quotes, parentheses, or brackets, its closing one will not be included
//	                // # since _split_regexes[0]'s matcher for that can't go in the lookbehind since it's optional
//					if(Pattern.matches("^[\"\\(\\[].*", newSentence)) { // matches ",(,[ at the beginning
//						newSentence = article.substring(lastSentenceBegin, ++sentenceEnd);
//					}
//					acc.add(new SentenceToken(lastSentenceBegin, sentenceEnd, newSentence, null, ++i));
//				    lastSentenceBegin = sentenceBeg;
//				}
//				if(lastSentenceBegin > 0) {
//	                // add final match
//					acc.add(new SentenceToken(lastSentenceBegin, s.getEnd(), article.substring(lastSentenceBegin, s.getEnd()), null, ++i));	                
//				} else {
//					// add the full string if no match
//					acc.add(new SentenceToken(s.getBegin(), s.getEnd(), s.getToken(), null, ++i));
//				}
//			}
//			sentences = acc;
//		}
//		
//		return sentences;
//	}
	
	public ArrayList<SentenceToken> splitSentences(String article) {
		// \xe2\x80\xa2 U+2022 = BULLET (•)
		// \xe2\x80\x82 U+2002 = EN SPACE
		// \xe2\x80\x9c U+201C = LEFT DOUBLE QUOTATION MARK (“)
		// \xe2\x80\x9d U+201D = RIGHT DOUBLE QUOTATION MARK (”)
		// U+00A0 = NO-BREAK SPACE
		//System.out.println(article);
		article = article.replaceAll(YEAR_OLD_REGEX, YEAR_OLD_REPL);
		article = article.replaceAll(YEAR_OLD_SEX_REGEX, YEAR_OLD_SEX_REPL);
		article = article.replaceAll("[A|a]pprox\\.", "approximately ");
		//System.out.println(article);
		String allowedCharsBetweenSentences = "[\\s\u2022\u2002\u201c\u00a0]";
		String titles = "((?i)(?<!Dr\\.|Mr\\.|\bMs\\.|Mrs\\.|mg\\.)";
		// # main sentence split (with spaces) 
		// https://www.regex101.com/r/qP7hH5/3
		String regex1 = titles + "(?<=\\.|\\?|!)\\s*([*\"\\])]|\u201D)?\\s+(?:" + allowedCharsBetweenSentences + ")*\\s*(?=[A-Z0-9(\"\\[]))";

		// # some articles don't have spaces between periods and next sentence
		//String regex2 = "(?<=\\.)\\s*(?=[A-Z][a-z]{2})";  // positive lookbehind that matches a . followed by zero or more spaces followed by a positive lookahead of at least two alphas
		String regex2 = titles + "(?<=\\.)\\s*(?=[A-Za-z]))"; // allows for sentences to start with a single letter

		Pattern falseMatchRegex = Pattern.compile("(vs|v\\.s|i\\.v)\\.$");
		Pattern[] patterns = { Pattern.compile(regex1), Pattern.compile(regex2) };
		
		ArrayList<SentenceToken> sentences = new ArrayList<SentenceToken>();
		int i = 0;
		// start with full paragraph text
		sentences.add(new SentenceToken(0, article.length(), article, null, ++i));

		// logic copied verbatim from python. a few optimizations
		for(Pattern p : patterns) {
			ArrayList<SentenceToken> acc = new ArrayList<SentenceToken>();

			// The first time through there will only be one entry in sentences. Second pass will go through all split
			// sentences looking for instances of pattern regex2
			for(SentenceToken s : sentences) {
				int lastSentenceBegin = s.getBegin();
				
				Matcher matcher = p.matcher(s.getToken());
				while(matcher.find()) {
					try {
						int sentenceEnd = s.getBegin() + matcher.start();
						int sentenceBeg = s.getBegin() + matcher.end();
	
						String newSentence = article.substring(lastSentenceBegin, sentenceEnd);
						
						if(falseMatchRegex.matcher(newSentence).matches()) {
							break;
						}
						// # if sentence is bracketed by quotes, parentheses, or brackets, its closing one will not be included
		                // # since _split_regexes[0]'s matcher for that can't go in the lookbehind since it's optional
						if(Pattern.matches("^[\"\\(\\[].*", newSentence)) { // matches ",(,[ at the beginning
							newSentence = article.substring(lastSentenceBegin, ++sentenceEnd);
						}
						acc.add(new SentenceToken(lastSentenceBegin, sentenceEnd, newSentence, null, ++i));
					    lastSentenceBegin = sentenceBeg;
					    
					} catch(Exception e) {
						lastSentenceBegin = 0;
						logger.error("splitSentences(): Sentence: {} \n Regex Pattern: {} \n {}", s.getToken(), p.toString(), e);
					}
				}
				if(lastSentenceBegin > 0) {
	                // add final match
					acc.add(new SentenceToken(lastSentenceBegin, s.getEnd(), article.substring(lastSentenceBegin, s.getEnd()), null, ++i));
				} else {
					// add the full string if no match
					acc.add(new SentenceToken(s.getBegin(), s.getEnd(), s.getToken(), null, ++i));
				}
			}
			sentences = acc;
		}
		
		return sentences;
	}
	
	// article: represents a full article's text or multiple paragraphs of text
	public ArrayList<SentenceToken> splitSentences_old(String article) {
		// \xe2\x80\xa2 U+2022 = BULLET (•)
		// \xe2\x80\x82 U+2002 = EN SPACE
		// \xe2\x80\x9c U+201C = LEFT DOUBLE QUOTATION MARK (“)
		// \xe2\x80\x9d U+201D = RIGHT DOUBLE QUOTATION MARK (”)
		
		String allowedCharsBetweenSentences = "[\\s\\u2022\\u2002\\u201c]";
		// # main sentence split (with spaces) 
		String regex1 = "(?<=\\.)\\s*([*\"\\])]|\u201D)?\\s+(?:" + allowedCharsBetweenSentences + ")*\\s*(?=[A-Z0-9(\"\\[])";
		// # some articles don't have spaces between periods and next sentence
		String regex2 = "(?<=\\.)\\s*(?=[A-Z][a-z]{2})";
		Pattern falseMatchRegex = Pattern.compile("(vs|v\\.s|i\\.v)\\.$");
		Pattern[] patterns = { Pattern.compile(regex1), Pattern.compile(regex2) };
		
		// add the incoming paragraph as the only item in the List
		ArrayList<SentenceToken> sentences = new ArrayList<SentenceToken>();
		int i = 0;
		sentences.add(new SentenceToken(0, article.length(), article, null, ++i));

		// logic copied verbatim from python. not optimized
		for(Pattern p : patterns) {
			ArrayList<SentenceToken> acc = new ArrayList<SentenceToken>();

			for(SentenceToken s : sentences) {

				int lastSentenceBegin = s.getBegin();
				boolean matched = false;
				
				Matcher matcher = p.matcher(s.getToken());
				while(matcher.find()) {
					int sentenceEnd = s.getBegin() + matcher.start();
					int sentenceBeg = s.getBegin() + matcher.end();

					String newSentence = s.getToken().substring(lastSentenceBegin, sentenceEnd);
					
					if(falseMatchRegex.matcher(newSentence).matches()) {
						// SRD: skip sentence altogether?
						break;
					}
					// # if sentence is bracketed by quotes, parentheses, or brackets, its closing one will not be included
	                // # since _split_regexes[0]'s matcher for that can't go in the lookbehind since it's optional
					// SRD: anywhere in the sentence? should this test the beginning?
					if(Pattern.matches("\"\\(\\[", newSentence)) { // matches "([
						sentenceEnd += 1;
						newSentence = s.getToken().substring(lastSentenceBegin, sentenceEnd);
						//newSentence = matcher.group();
					}
				    matched = true;
				    acc.add(new SentenceToken(lastSentenceBegin, sentenceEnd, newSentence, null, ++i));
				    lastSentenceBegin = sentenceBeg;
				}
				if(matched) {
	                // add last one
					acc.add(new SentenceToken(lastSentenceBegin, s.getEnd(), article.substring(lastSentenceBegin, s.getEnd()), null, ++i));	                
				} else {
					acc.add(new SentenceToken(s.getBegin(), s.getEnd(), s.getToken(), null, ++i));
				}
			}
			sentences = acc;
		}
		
		return sentences;
	}
	
	public ArrayList<WordToken> splitWords(String sentence) {
		ArrayList<WordToken> wordTokens = new ArrayList<WordToken>();
		
		String[] words = replaceChars(sentence, false, false);
		
		if(words.length > 0) {
			int i=0;
			
			for(String word : words) {
				String xlate = translateMap.get(word.toLowerCase());
				if(xlate == null)
					wordTokens.add(new WordToken(word, null, ++i));
				else {
					for(String token : xlate.split(" "))
						wordTokens.add(new WordToken(token, null, ++i));
				}
			}
		} else {
			//logger.error("com.mst.tools.Tokenizer.replaceChars() produced an empty array. \n Input sentence: {}", sentence);
			wordTokens = null;
		}
		
		return wordTokens;
	}
	
	private String[] replaceChars(String s, boolean possessive, boolean mallet) {
		String[] ret = {};
				
		if(s == null || s.length() == 0)
			return ret;
		
		try {
			s = s.trim();
			
			//s = s.replaceAll(YEAR_OLD_REGEX, YEAR_OLD_REPL);
			//s = s.replaceAll(YEAR_OLD_SEX_REGEX, YEAR_OLD_SEX_REPL);
			
			if(s.startsWith("\""))
				s = s.replaceFirst("\"", "\" ");
			else if(s.startsWith("'"))
				s = s.replaceFirst("'", "' ");
			
			s = s.replace(" \"", " \" ");
		    s = s.replace("(\"", "( \" ");
		    s = s.replace("[\"", "[ \" ");
		    s = s.replace("{\"", "{ \" ");
		    s = s.replace("<\"", "< \" ");
		    s = s.replace("  ", " ");
		    
		    s = s.replace("...", " ... ");
		    // possibly handle cases where one char becomes surrounded by space with regex
		    s = s.replace(",", " , ");
		    s = s.replace(":", " : ");
		    s = s.replace("@", " @ ");
		    s = s.replace("#", " # ");
		    s = s.replace("$", " $ ");
		    s = s.replace("%", " % ");
		    s = s.replace("&", " & ");
		    
		    //s = s.replace("Pt", "Patient");
		    
		    // this was copied verbatim from the original python.
		    // needs to be cleaned up. apparent goal is to add a space to betwen final token and period ending sentence.
		    // read through a sentence backwards, looking for a period only if it precedes certain chars
		    int pos = s.length()-1;
		    while(pos > 0) {
		    	String c = String.valueOf(s.charAt(pos));
		    	if(c.matches("\\[|\\]|\\)|}|>|\"|'")) // match any of [ ] ) } > " '
		    		pos -= 1;
		    	else
		    		break;
		    }
		    // if a period is not preceded by another period, replace
		    if(s.charAt(pos) == '.' && !(pos > 0 && s.charAt(pos-1) == '.'))
		    	s = s.substring(0, s.length()-1) + " .";
		    	//s = s.replace(".", " .");
		    
		    s = s.replace("?", " ? ");
		    s = s.replace("!", " ! ");
		    s = s.replace("[", " [ ");
		    s = s.replace("]", " ] ");
		    s = s.replace("(", " ( ");
		    s = s.replace(")", " ) ");
		    s = s.replace("{", " { ");
		    s = s.replace("}", " } ");
		    s = s.replace("<", " < ");
		    s = s.replace(">", " > ");
		    s = s.replace(";", " ; ");
		    s = s.replace("--", " -- ");
		    
		    s = s.replace("\"", " \" ");
	
		    s = s.replace(" '", " ' ");  // added 26 August 2012
		    s = s.replace("' ", " ' ");
		    
		    if(possessive) {
		        s = s.replace("'s ", " 's ");
		        s = s.replace("'S ", " 'S ");
		    }
		    
		    s = s.replace("'m ", " 'm ");
		    s = s.replace("'M ", " 'M ");
		    s = s.replace("'d ", " 'd ");
		    s = s.replace("'D ", " 'D ");
		    s = s.replace("'ll ", " 'll ");
		    s = s.replace("'re ", " 're ");
		    s = s.replace("'ve ", " 've ");
		    s = s.replace("n't ", " n't ");
		    s = s.replace("'LL ", " 'LL ");
		    s = s.replace("'RE ", " 'RE ");
		    s = s.replace("'VE ", " 'VE ");
		    s = s.replace("N'T ", " N'T ");
		    s = s.replace(" Cannot ", " Can not ");
		    s = s.replace(" cannot ", " can not ");
		    s = s.replace(" D'ye ", " D' ye ");
		    s = s.replace(" d'ye ", " d' ye ");
		    s = s.replace(" Gimme ", " Gim me ");
		    s = s.replace(" gimme ", " gim me ");
		    s = s.replace(" Gonna ", " Gon na ");
		    s = s.replace(" gonna ", " gon na ");
		    s = s.replace(" Gotta ", " Got ta ");
		    s = s.replace(" gotta ", " got ta ");
		    s = s.replace(" Lemme ", " Lem me ");
		    s = s.replace(" lemme ", " lem me ");
		    s = s.replace(" More'n ", " More 'n ");
		    s = s.replace(" more'n ", " more 'n ");
		    s = s.replace("'Tis ", " 'T is ");
		    s = s.replace("'tis ", " 't is ");
		    s = s.replace("'Twas ", " 'T was ");
		    s = s.replace("'twas ", " 't was ");
		    s = s.replace(" Wanna ", " Wan na ");
		    //s = s.replace(" wanna ", " wanna ");
		    
		    if(mallet) {
		    	s = s.replace("#", "hash")
		    		 .replace(":", "colon")
		    		 .replace(",", "comma");
		    }
		    
		    s = s.replaceAll("\\s+", " ");
		    
		    ret = s.trim().split(" ");
		    //System.out.println(s);
		} catch(Exception e) {
			logger.error("replaceChars(): Input sentence: {} \n {}", s, e);
		}

		return ret;
	}
}
