package com.mst.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mst.util.Constants;
import com.mst.util.DateNormalizer;

public class SentenceCleaner {
	
	final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private Pattern YEAR_OLD_REGEX = Pattern.compile("(?<=\\d)((?i)[\\s-]*YEAR[\\s-]*OLD|\\s*yr?\\.?\\/?o\\.?)(?!m)"); 
	private String YEAR_OLD_REPL = "-year-old"; 
	private Pattern YEAR_OLD_SEX_REGEX = Pattern.compile("(?<=\\d)yom");
	private String YEAR_OLD_SEX_REPL = "-year-old male";
	
	private Pattern UPPER_WITH_COLON;
	private Pattern EXPLICIT_WITH_COLON;
	private Pattern EXPLICIT_WITHOUT_COLON;
	private Pattern EMPTY_PARENS;
	private Pattern PROBABILITIES;
	
	private static DateNormalizer dateNormalizer = new DateNormalizer();
	
	public SentenceCleaner() {
		
		// **** Section Header Regex ****
		// not all section headers are in uppercase - we take the most common cases here and match them explicitly
		// TODO add (?i) to this since it's added in both uses?
		String pubmedHeaders = "abstract|introduction and hypothesis|background and aims?|background|aim of the study|aims?|introduction|" +
                				 "purposes?|context|(?:materials? and )?methods?|objectives?|results?|conclusions?";
		String wichitaHeaders = "REFERRED HERE|REASON FOR VISIT|SUBJECTIVE2|PREVIOUS TESTS?|ASSESSMENT|CLINICAL IMPRESSION?|PLANS?|CHIEF COMPLAINTS?|HISTORY OF PRESENT ILLNESS|" +
                				"PAST MEDICAL/SURGICAL HISTORY|REPORTED|OTHER|DIAGNOSES|PROCEDURAL|SURGICAL|CURRENT MEDICATIONS?|ALLERGIES|PERSONAL HISTORY|FAMILY HISTORY|" + 
                				"TB-MOTHER|REVIEW OF SYSTEMS|CARDIOVASCULAR|PULMONARY|MUSCULOSKELETAL|PHYSICAL FINDINGS?|VITAL SIGNS|STANDARD MEASUREMENTS|RECTAL|NOTES|" + 
                				"THERAPY|MEANINGFUL USE MEASURES|PLANNING|PROBLEM LIST|FACILITY|SIGNATURES";
		
		String genesisHeaders = "Impression|Current Plans?|Future Plans?|Story";
		
		String allHeaders = "(?i)(" + pubmedHeaders + "|" + wichitaHeaders + "|" + genesisHeaders + ")";
		
		String withColon = "^\\s*%s:\\s*:?\\s*";
		String withoutColon = "^\\s*%s\\s+";
		
		UPPER_WITH_COLON = Pattern.compile(String.format(withColon, "[A-Z\\s&]+"));  //https://www.regex101.com/r/eP3rD3/1

		EXPLICIT_WITH_COLON = Pattern.compile(String.format(withColon, allHeaders));  //https://www.regex101.com/r/nM5eN3/1
		// some section headers don't have colons - we will match these against the explicit headers
	    // and require the next word to begin with a capital letter to guard against false positives
		EXPLICIT_WITHOUT_COLON = Pattern.compile(String.format(withoutColon, allHeaders));  //https://www.regex101.com/r/sM1xT6/1
		
		
		// **** Probability Regex ****
		// # matches positive or negative integer, float, percent or fractional number
        // # TODO: add comma in integer part of number
		String numberPattern = "-?\\$?\\d*[\\.\\/]?\\d+[%%]?";
		//String multipleSpaces = "\\s{2,}";
		
		StringBuilder sb = new StringBuilder();
		sb.append("p(-interaction)?\\s*[<>=]+\\s*%1$s\\s*(x|\\xd7|\\xc3\\x97)?(\\s*%1$s\\(%1$s\\))?"); // P-Value
		sb.append("|");
		sb.append("%1$s\\s*(confidence interval)?\\s*([(\\[]CI[)\\]]|CI)\\s*[;,=]?\\s*%1$s\\s*(to|-)\\s*%1$s"); // Confidence Interval
		sb.append("|");
		sb.append("(hazard ratio)?\\s*([(\\[]HR[)\\]]|HR)\\s*%1$s"); // HR
		sb.append("|");
		sb.append("(risk ratio)?\\s*([(\\[]RR[)\\]]|RR)\\s*[=]?\\s*%1$s"); // Risk Ratio
		sb.append("|");
		sb.append("(\\s*[\\w\\s]+\\s+)?n\\s*[<=]+\\s*%1$s"); // Number of Patients
		sb.append("|");
		sb.append("%1$s\\s*patient[\\s\\-]years?"); // Patient-Years
		sb.append("|");
		sb.append("range\\s*%1$s\\s*(-|to)\\s*%1$s"); // Range TODO: both numbers need to have optional dosage matchers added afterwards
	
		String completePattern = String.format(sb.toString(), numberPattern);
		
		PROBABILITIES = Pattern.compile(String.format("[;,]?\\s*(?i)(%s)", completePattern));
		EMPTY_PARENS = Pattern.compile(String.format("\\s*[(\\[]%1$s(and|respectively)?%1$s[)\\]]", "[\\s;,]*"));
	}
	
	public String cleanSentenceMinimal(String sentence) {

		sentence = processAge(sentence);
		
		sentence = dateNormalizer.normalize(sentence);
		
		sentence = processTNM(sentence);
		
		sentence = processMeasurements(sentence);
		
		return sentence;
	}	
	
	public String cleanSentence(String sentence) {	
		// TODO does the order of these matter? can we call cleanSentenceMinimal then add the unique cleanup calls?
		sentence = processAge(sentence);
		
		sentence = removeSectionHeader(sentence);

		sentence = removeProbabilityInfo(sentence);
			
		// TODO not sure if these are still necessary after the switch to Stanford POS
		// sometimes �double quotes� come across in the text of full articles from PubMed. These seem to cause issues for the POS tagger.
		//cleaned = cleaned.replaceAll("\"", "");
		//cleaned = cleaned.replaceAll("�", "");
		//cleaned = cleaned.replaceAll("�", "");
		
		sentence = dateNormalizer.normalize(sentence);
		
		sentence = processTNM(sentence);
		
		sentence = processMeasurements(sentence);
		
		return sentence;
	}
	
	private String processAge(String sentence) {
		try {
			// age regex replacements act upon split sentences
			sentence = YEAR_OLD_REGEX.matcher(sentence).replaceAll(YEAR_OLD_REPL);
			sentence = YEAR_OLD_SEX_REGEX.matcher(sentence).replaceAll(YEAR_OLD_SEX_REPL);
		} catch(Exception e) {
			LOG.error("processAge():\n{}\n{}", sentence, e);
		}
		return sentence;
	}
	
	private String processTNM(String sentence) {
		try {
			Matcher tnm = Constants.TNM_STAGING_REGEX.matcher(sentence);
			if(tnm.find()) {
				if(!(tnm.group().equalsIgnoreCase("TX") || tnm.group().equalsIgnoreCase("TX."))) {
					sentence = sentence.replace(tnm.group(), tnm.group().replace(" ", "").replace('t', 'T').replace('n', 'N').replace('m', 'M') + " ");
				}
			}
		} catch(Exception e) {
			LOG.error("processTNM():\n{}\n{}", sentence, e);
		}
		return sentence;
	}
	
	private String processMeasurements(String sentence) {
		try {
			// patterns such as 1.8 x 2.4 x 1.5 cm
			Matcher measu = Constants.MEASUREMENT_REGEX.matcher(sentence);
			while(measu.find()) {
				sentence = sentence.replace(measu.group(), measu.group().replace(" ", "") + " ");
			}
		} catch(Exception e) {
			LOG.error("processMeasurements():\n{}\n{}", sentence, e);
		}
		return sentence;
	}
	
	private String removeSectionHeader(String sentence) {
		
		try {
			//sentence = sentence.replaceAll(uppercaseWithColon, "");
			sentence = UPPER_WITH_COLON.matcher(sentence).replaceAll("");
			
			// remove headers which are mixed-case and match one of the explicitly defined header patterns
			//sentence = sentence.replaceAll(explicitWithColon, "");
			sentence = EXPLICIT_WITH_COLON.matcher(sentence).replaceAll("");
			
			// remove headers which match an explicitly defined pattern but are not followed by a colon
			//Pattern p = Pattern.compile(explicitWithoutColon);
			Matcher matcher = EXPLICIT_WITHOUT_COLON.matcher(sentence);
			
			if(matcher.find()) {
				// confirm that the next character in the string is upper-case (i.e., starts the actual sentence) -
	            // we can't do this in the regex because it ignores case
				if(String.valueOf(sentence.charAt(matcher.end())).matches("[A-Z0-9]")) {
					//sentence = sentence.replaceAll(explicitWithoutColon, "");
					sentence = EXPLICIT_WITHOUT_COLON.matcher(sentence).replaceAll("");
				}
			}
		} catch(Exception e) {
			LOG.error("removeSectionHeader():\n{}\n{}", sentence, e);
		}
		
		return sentence;
	}
	
	// this was ported from the original python code. Not exactly sure the use-case but probably related to PubMed articles.
	private String removeProbabilityInfo(String sentence) {		
		try {
			/* begin processing */
			sentence = PROBABILITIES.matcher(sentence).replaceAll(" ");
			//output = output.replaceAll(multipleSpaces, " "); // this will be done in Tokenizer.java
			sentence = EMPTY_PARENS.matcher(sentence).replaceAll("");
			
		} catch(Exception e) {
			LOG.error("removeProbabilityInfo():\n{}\n{}", sentence, e);
		}
		
		return sentence;
	}
}
