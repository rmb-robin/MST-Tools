package com.mst.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentenceCleaner {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	public String cleanSentence(String input) {
		String cleaned = null;
		
		cleaned = removeSectionHeader(input);

		if(cleaned != null) { 
			cleaned = removeProbabilityInfo(cleaned);
			
			if(cleaned != null) {
				// sometimes “double quotes” come across in the text of full articles from PubMed. These seem to cause issues for the POS tagger.
				cleaned = cleaned.replaceAll("\"", "");
				cleaned = cleaned.replaceAll("”", "");
				cleaned = cleaned.replaceAll("“", "");
			}
		}
		
		// null will be returned if an error occurred in either method
		return cleaned;
	}
	
	// ported from original python. no optimizations
	private String removeSectionHeader(String input) {
		String output = null;
		
		try {
			/* define regex */
			// not all section headers are in uppercase - we take the most common cases here and match them explicitly
			// TODO test this to ensure that ? don't get interpreted literally
			// TODO add (?i) to this since it's added in both uses?
			String explicitHeaders = "(abstract|introduction and hypothesis|background and aims?|background|aim of the study|aims?|introduction|" +
	                				 "purposes?|context|(?:materials? and )?methods?|objectives?|results?|conclusions?)";
			String sectionHeader = "^\\s*%s:\\s*:?\\s*";
			// start of sentence, zero+ spaces, replace one+ upper/space, colon, zero+ spaces, ONE colon, zero+ spaces
			String uppercaseHeader = String.format(sectionHeader, "[A-Z\\s&]+");
			// start of sentence, zero+ spaces, explicitHeaders, colon, zero+ spaces, ONE colon, zero+ spaces
			// Test: " abstract: : colon: hello this is some text"  <-- everything from "colon" on should be kept
			String mixedCaseHeader = String.format(sectionHeader, "(?i)".concat(explicitHeaders));
			// some section headers don't even have colons - we will match these against the explicit headers
		    // and require the next word to begin with a capital letter to guard against false positives
			String noColonHeader = String.format("^\\s*%s\\s+", "(?i)".concat(explicitHeaders));
			
			/* begin processing */
			output = input.replaceAll(uppercaseHeader, "");
			// remove headers which are mixed-case and match one of the explicitly defined header patterns
			output = output.replaceAll(mixedCaseHeader, "");
			
			// remove headers which match an explicitly defined pattern but are not followed by a colon
			Pattern p = Pattern.compile(noColonHeader);
			Matcher matcher = p.matcher(output);
			if(matcher.find()) {
				// confirm that the next character in the string is upper-case (i.e., starts the actual sentence) -
	            // we can't do this in the regex because it ignores case
				if(String.valueOf(output.charAt(matcher.end())).matches("[A-Z]")) {
					output = output.replaceAll(noColonHeader, ""); 
				}
			}
		} catch(Exception e) {
			logger.error("removeSectionHeader(): {}", e);
			output = null;
		}
		
		return output;
	}
	
	private String removeProbabilityInfo(String input) {
		String output = null;
		
		try {
			// # matches positive or negative integer, float, percent or fractional number
	        // # TODO: add comma in integer part of number
			String numberPattern = "-?\\$?\\d*[\\.\\/]?\\d+[%%]?";
			String multipleSpaces = "\\s{2,}";
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
			output = output.replaceAll(multipleSpaces, " ");
			output = output.replaceAll(emptyParens, "");
			
		} catch(Exception e) {
			logger.error("removeProbabilityInfo(): {}", e);
			output = null;
		}
		
		return output;
	}
}
