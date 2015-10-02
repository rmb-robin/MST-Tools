package com.mst.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mst.util.DateNormalizer;

public class SentenceCleaner {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	private String YEAR_OLD_REGEX = "(?<=\\d)((?i)[\\s-]*YEAR[\\s-]*OLD|\\s*yr?\\.?\\/?o\\.?)(?!m)"; 
	private String YEAR_OLD_REPL = "-year-old"; 
	private String YEAR_OLD_SEX_REGEX = "(?<=\\d)yom";
	private String YEAR_OLD_SEX_REPL = "-year-old male";
	
	private static DateNormalizer dateNormalizer = new DateNormalizer();
	
	public String cleanSentence(String sentence) {
		
		// age regex replacements act upon split sentences
		sentence = sentence.replaceAll(YEAR_OLD_REGEX, YEAR_OLD_REPL);
		sentence = sentence.replaceAll(YEAR_OLD_SEX_REGEX, YEAR_OLD_SEX_REPL);
		
		sentence = removeSectionHeader(sentence);

		sentence = removeProbabilityInfo(sentence);
			
		// TODO not sure if these are still necessary after the switch to Stanford POS
		// sometimes �double quotes� come across in the text of full articles from PubMed. These seem to cause issues for the POS tagger.
		//cleaned = cleaned.replaceAll("\"", "");
		//cleaned = cleaned.replaceAll("�", "");
		//cleaned = cleaned.replaceAll("�", "");
		
		sentence = dateNormalizer.normalize(sentence);
		
		return sentence;
	}
	
	private String removeSectionHeader(String input) {
		String output = input;
		
		try {
			/* define regex */
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

			String uppercaseWithColon = String.format(withColon, "[A-Z\\s&]+");  //https://www.regex101.com/r/eP3rD3/1

			String explicitWithColon = String.format(withColon, allHeaders);  //https://www.regex101.com/r/nM5eN3/1
			// some section headers don't have colons - we will match these against the explicit headers
		    // and require the next word to begin with a capital letter to guard against false positives
			String explicitWithoutColon = String.format(withoutColon, allHeaders);  //https://www.regex101.com/r/sM1xT6/1
			
			/* begin processing */
			output = input.replaceAll(uppercaseWithColon, "");
			// remove headers which are mixed-case and match one of the explicitly defined header patterns
			output = output.replaceAll(explicitWithColon, "");
			
			// remove headers which match an explicitly defined pattern but are not followed by a colon
			Pattern p = Pattern.compile(explicitWithoutColon);
			Matcher matcher = p.matcher(output);
			if(matcher.find()) {
				// confirm that the next character in the string is upper-case (i.e., starts the actual sentence) -
	            // we can't do this in the regex because it ignores case
				if(String.valueOf(output.charAt(matcher.end())).matches("[A-Z0-9]")) {
					output = output.replaceAll(explicitWithoutColon, ""); 
				}
			}
		} catch(Exception e) {
			logger.error("removeSectionHeader(): {}", e);
		}
		
		return output;
	}
	
	// this was ported from the original python code. Not exactly sure the use-case but probably related to PubMed articles.
	private String removeProbabilityInfo(String input) {
		String output = input;
		
		try {
			// # matches positive or negative integer, float, percent or fractional number
	        // # TODO: add comma in integer part of number
			String numberPattern = "-?\\$?\\d*[\\.\\/]?\\d+[%%]?";
			//String multipleSpaces = "\\s{2,}";
			String emptyParens = String.format("\\s*[(\\[]%1$s(and|respectively)?%1$s[)\\]]", "[\\s;,]*");
			
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
			String probabilities = String.format("[;,]?\\s*(?i)(%s)", completePattern);
			
			/* begin processing */
			output = input.replaceAll(probabilities, " ");
			//output = output.replaceAll(multipleSpaces, " "); // this will be done in Tokenizer.java
			output = output.replaceAll(emptyParens, "");
			
		} catch(Exception e) {
			logger.error("removeProbabilityInfo(): {}", e);
		}
		
		return output;
	}
}
