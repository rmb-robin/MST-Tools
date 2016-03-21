package com.mst.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.mst.model.WordToken;

public class Utils {
	
	public static String execCmd(String cmd) throws Exception {
		StringBuilder sb = new StringBuilder();
		//sb.append("execCmd output:\n");
		String s = null;
		
		if(cmd != null && cmd.trim().length() > 0)
			try {
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while((s = stdInput.readLine()) != null) {
	                sb.append(s);
	                sb.append("\n");
	            }
			} catch(Exception e) {
				throw new Exception(e);
			}
		else {
			throw new Exception("Input command is required.");
		}
		
		return sb.toString();
	}
	
	public static boolean checkForNegation(ArrayList<WordToken> words, int index) {
		boolean negated = false;
		for(int i=index; i >= 0; i--) {
			if(Constants.NEGATION.matcher(words.get(i).getToken()).matches()) {
				negated = true;
				break;
			}
		}
		return negated;
	}
}
