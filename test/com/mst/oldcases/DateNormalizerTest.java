package com.mst.oldcases;
import static org.junit.Assert.*;

import org.junit.Test;

import com.mst.util.DateNormalizer;


public class DateNormalizerTest {
	
	@Test
	public final void testNormalize() {
		DateNormalizer normalizer;
		String normalized = "";
		
		try {
			normalizer = new DateNormalizer();
			
			normalized = normalizer.normalize("01-15-2015");
			assertEquals("Date must be 01/15/2015", "01/15/2015", normalized);
			
			normalized = normalizer.normalize("2015-01-15");
			assertEquals("Date must be 01/15/2015", "01/15/2015", normalized);
			
			normalized = normalizer.normalize("2015/1/15");
			assertEquals("Date must be 01/15/2015", "01/15/2015", normalized);
			
			normalized = normalizer.normalize("1/15/2015");
			assertEquals("Date must be 01/15/2015", "01/15/2015", normalized);
			
			normalized = normalizer.normalize("1/15/15");
			assertEquals("Date must be 01/15/2015", "01/15/2015", normalized);
			
			normalized = normalizer.normalize("Jan 15 2015");
			assertEquals("Date must be 01/15/2015", "01/15/2015", normalized);
			
			normalized = normalizer.normalize("January 15 2015");
			assertEquals("Date must be 01/15/2015", "01/15/2015", normalized);
			
			normalized = normalizer.normalize("Jan 15, 2015");
			assertEquals("Date must be 01/15/2015", "01/15/2015", normalized);
			
			normalized = normalizer.normalize("January 15, 2015");
			assertEquals("Date must be 01/15/2015", "01/15/2015", normalized);

			normalized = normalizer.normalize("Jan 2015");
			assertEquals("Date must be 01/01/2015", "01/01/2015", normalized);

			normalized = normalizer.normalize("January 2015");
			assertEquals("Date must be 01/01/2015", "01/01/2015", normalized);

			normalized = normalizer.normalize("Jan '15");
			assertEquals("Date must be 01/01/2015", "01/01/2015", normalized);

			normalized = normalizer.normalize("January '15");
			assertEquals("Date must be 01/01/2015", "01/01/2015", normalized);

			normalized = normalizer.normalize("Jan. 15 2015");
			assertEquals("Date must be 01/15/2015", "01/15/2015", normalized);

			normalized = normalizer.normalize("Jan. 15, 2015");
			assertEquals("Date must be 01/15/2015", "01/15/2015", normalized);

			normalized = normalizer.normalize("Jan. of 2015");
			assertEquals("Date must be 01/01/2015", "01/01/2015", normalized);
			
			normalized = normalizer.normalize("1/15");
			assertEquals("Date must be 01/01/2015", "01/01/2015", normalized);
			
			normalized = normalizer.normalize("1/2015");
			assertEquals("Date must be 01/01/2015", "01/01/2015", normalized);
			
			normalized = normalizer.normalize("1/1/2015 Jan '15 2015-1-1");
			assertEquals("Date must be 01/01/2015", "01/01/2015 01/01/2015 01/01/2015", normalized);
			
		} catch (Exception e) {
			e.printStackTrace();
		}   
	}
}

/*
private static final Map<String, String> dateFormatRegexps = new LinkedHashMap<String, String>() {{
        // Jan 1, 2014; January 1, 2014
        put("\\b(Jan(uary)?|Feb(ruary)?|Mar(ch)?|Apr(il)?|May|June?|July?|Aug(ust)?|Sep(tember)?|Oct(ober)?|Nov(ember)?|Dec(ember)?) \\d{1,2}, \\d{4}\\b", "MMM dd, yyyy");
        // Jan 2014; January 2014
        put("\\b(Jan(uary)?|Feb(ruary)?|Mar(ch)?|Apr(il)?|May|June?|July?|Aug(ust)?|Sep(tember)?|Oct(ober)?|Nov(ember)?|Dec(ember)?) \\d{4}\\b", "MMM yyyy");
        // Jan '14; January '14
        put("\\b(Jan(uary)?|Feb(ruary)?|Mar(ch)?|Apr(il)?|May|June?|July?|Aug(ust)?|Sep(tember)?|Oct(ober)?|Nov(ember)?|Dec(ember)?) \'\\d{2}\\b", "MMM ''yy");
        // Jan. 1 2014
        put("\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\. \\d{1,2} \\d{4}\\b", "MMM. dd yyyy");
        // Jan. 1, 2014
        put("\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\. \\d{1,2}, \\d{4}\\b", "MMM. dd, yyyy");
        // Jan. of 2014
        put("\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\. of \\d{4}\\b", "MMM. 'of' yyyy");

        // these need to go last because they will match substrings of other valid dates (e.g.)
        // 2/15 - this is likely to cause false positives with fractions
        put("\\b\\d{1,2}/\\d{2}\\b", "MM/yy");
        // 2/2015
        put("\\b\\d{1,2}/\\d{4}\\b", "MM/yyyy");
        // 19xx and 20xx - e.g. 2015 will map to Jan 1, 2015
        put("\\b(19|20)\\d{2}\\b", "yyyy");
    }};


*/