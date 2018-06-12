package com.mst.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateParseValidator {

	public static boolean isDate(String token) {
		 
	    if (token == null)
	      return false;
	 
	    //set the format to use as a constructor argument
	    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	    
	    if (token.trim().length() != dateFormat.toPattern().length())
	      return false;
	 
	    dateFormat.setLenient(false);
	    
	    try {
	      //parse the inDate parameter
	      dateFormat.parse(token.trim());
	    }
	    catch (ParseException pe) {
	      return false;
	    }
	    return true;
	  }
	
}
