package com.mst.tools;

import com.mst.model.MapValue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mst.util.Constants;

public class RegexProcessor {

	public void process(List<Multimap<String, MapValue>> related, String sentence) {
		// PSA/Gleason regex processing. Find instances that could not be picked up by a constructor.
		
		Pattern ggRegex = Pattern.compile(Constants.GLEASON_REGEX);
		Pattern psaRegex1 = Pattern.compile("PSA\\s*\\d\\d\\/\\d\\d\\/\\d\\d:?\\s*<?\\d\\d?\\.?\\d{1,2}"); //PSA 09/26/13: 0.46PSA 01/29/14: 0.18PSA 06/19/14: 0.05.
		//Pattern psaRegex2 = Pattern.compile("PSA\\s*of\\s*\\d?\\d\\.\\d+\\s*((on|in)\\s*(\\d\\d\\/\\d\\d\\/\\d{2}|\\d{4}))?");
		Pattern chesapeakePSA1 = Pattern.compile("(?i)PSA( \\(Most Recent\\))? \\(\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*\\)"); // PSA (Most Recent) (5.4)   PSA (2.5)   PSA (Most Recent) (undetectable)   https://www.regex101.com/r/dV6zN8/1
		Pattern chesapeakePSA2 = Pattern.compile("PSA=\\s*(\\d\\d?\\.\\d\\d?|[A-Za-z]*)\\s*(ng\\/ml collected|from)?\\s*(\\d\\d?\\/\\d\\d?\\/\\d{2,4})"); // PSA=12.9 ng/ml collected 12/11/13   PSA=0.8 from 1/27/14     https://www.regex101.com/r/lO3fI5/1
		Pattern chesapeakePSA3 = Pattern.compile("(\\d\\d?\\/\\d\\d?\\/\\d{2,4}):\\s*(-->)?\\s*PSA=\\s*(\\d\\d?\\.?\\d\\d?)"); // 11/11/11: --> PSA= 2.5   12/04/08: PSA=2.2ng/ml   https://www.regex101.com/r/qF6xD5/1
		Pattern chesapeakePSA4 = Pattern.compile("PSA\\s*\\(?(\\d\\d?\\.?\\d\\d?)( ng\\/ml)?\\s*(on )?(\\d\\d?\\/\\d\\d?\\/\\d{2,4}|\\d\\d?\\/\\d\\d?)"); // https://www.regex101.com/r/bO8oC6/2
		Pattern chesapeakePSA5 = Pattern.compile("PSA\\s*(level of|of|down to|up to|is|was|stable at)\\s*(\\d\\d?\\.?\\d\\d?)(?!\\/)(\\s*(on|in|from))?(\\s*\\d\\d?\\/\\d\\d?\\/\\d{2,4}|\\s*\\d\\d?\\/\\d{2,4})?");
		
		final String GLEASON_LABEL = "Gleason";
		final String PSA_LABEL = "PSA";
		final String DIAP_LABEL = "Diagnostic Procedure";
		final String ABSV_LABEL = "Absolute Value";
		final String DATE_LABEL = "Known Event Date";
		
		Matcher matcher = ggRegex.matcher(sentence);

		while(matcher.find()) {
			String val = matcher.group(matcher.groupCount()); // get last group, which should be the Gleason value
			if(val != null) {
				val = val.trim();
				
				Multimap<String, MapValue> mm = ArrayListMultimap.create();
				
				mm.put(DIAP_LABEL, new MapValue(GLEASON_LABEL));
				mm.put(ABSV_LABEL, new MapValue(parseGleasonValue(val), null, ggRegex.toString()));
				
				related.add(mm);
			}
		}

		matcher = chesapeakePSA1.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(2).trim(), null, chesapeakePSA1.toString()));
			
			related.add(mm);
		}

		matcher = chesapeakePSA2.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(1).trim(), null, chesapeakePSA2.toString()));
			mm.put(DATE_LABEL, new MapValue(matcher.group(3).trim()));
			
			related.add(mm);
		}

		matcher = chesapeakePSA3.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(3).trim(), null, chesapeakePSA3.toString()));
			mm.put(DATE_LABEL, new MapValue(matcher.group(1).trim()));
			
			related.add(mm);
		}

		matcher = chesapeakePSA4.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(1).trim(), null, chesapeakePSA4.toString()));
			if(matcher.groupCount() > 2)
				mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim()));
			
			related.add(mm);
		}

		matcher = chesapeakePSA5.matcher(sentence);

		while(matcher.find()) {
			Multimap<String, MapValue> mm = ArrayListMultimap.create();
			
			mm.put(DIAP_LABEL, new MapValue(PSA_LABEL));
			mm.put(ABSV_LABEL, new MapValue(matcher.group(2).trim(), null, chesapeakePSA5.toString()));
			//for(int i=0; i <= matcher.groupCount(); i++) {
			//	System.out.println(i + " - " + matcher.group(i));
			//}
			if(matcher.group(matcher.groupCount()) != null)
				mm.put(DATE_LABEL, new MapValue(matcher.group(matcher.groupCount()).trim()));
			
			related.add(mm);
		}
		
		matcher = psaRegex1.matcher(sentence);

		while(matcher.find()) {
			String[] vals = matcher.group().split(" "); 
			if(vals.length > 0) {
				try {
					//unrelated.put("Diagnostic Procedure", "PSA|" + vals[2] + "|" + (vals[1].indexOf(':') > -1 ? vals[1].substring(0, vals[1].length()-1) : vals[1].trim()));
					//structured.unrelated.put("Diagnostic Procedure Value", vals[2]);
					//structured.unrelated.put("Diagnostic Procedure Date", vals[1].indexOf(':') > -1 ? vals[1].substring(0, vals[1].length()-1) : vals[1].trim());
					
				} catch(Exception e) {
					System.out.println(matcher.group());
				}
			}
		}
	}
	
	private String parseGleasonValue(String in) {
		String out = "";
		
//		if(in.matches("^\\d\\s*\\+\\s*\\d\\s*=\\s*\\d\\d?$")) { // 4+4=8
//			String[] arr = in.split("=");
//			out = arr[1].trim();
//		} else if(in.matches("^\\d\\s*\\+\\s*\\d\\s*\\d\\d?$")) { // 3+4 1.8%
//			String[] arr = in.split("=");
//			out = arr[1].trim();
//		} else if(in.matches("^\\d\\s*\\(\\s*\\d\\s*\\+\\s*\\d\\)?$")) { // 7(4+3) and 7 (3+4
//			String[] arr = in.split("\\(");
//			out = arr[0].trim();
//		} else if(in.matches("^\\d\\s*\\+\\s*\\d$")) { // 3+3
//			String[] arr = in.split("\\+");
//			int left = Integer.valueOf(arr[0].trim());
//			int right = Integer.valueOf(arr[1].trim());
//			out = String.valueOf(left+right);
//		} else if(in.matches("^\\d\\s*\\d\\s*\\+\\s*\\d")) { // 7 3+4
//			String[] arr = in.split(" ");
//			out = String.valueOf(arr[0].trim());
//		} else {
//			out = in;
//		}
		Pattern plusRegex = Pattern.compile("\\d+\\s*\\+\\s*\\d+");
		
		Matcher matcher = plusRegex.matcher(in);
		if(matcher.find()) {
			String[] arr = matcher.group().split("\\+");
			int left = Integer.valueOf(arr[0].trim());
			int right = Integer.valueOf(arr[1].trim());
			out = String.valueOf(left+right);
		} else {		
			out = in;
		}

		return out;
	}
}
