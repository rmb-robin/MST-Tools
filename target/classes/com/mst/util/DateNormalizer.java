package com.mst.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateNormalizer {
    private static final Map<String, String> dateFormatRegexps = new LinkedHashMap<String, String>() {{
        // 07-04-2014
        put("\\b\\d{1,2}-\\d{1,2}-\\d{4}\\b", "MM-dd-yyyy"); // altered by SRD from dd-MM-yyyy
        // 2015-07-04
        put("\\b\\d{4}-\\d{1,2}-\\d{1,2}\\b", "yyyy-MM-dd");
        // 2015/7/4
        put("\\b\\d{4}/\\d{1,2}/\\d{1,2}\\b", "yyyy/MM/dd");
        // 7/4/2014
        put("\\b\\d{1,2}/\\d{1,2}/\\d{4}\\b", "MM/dd/yyyy");
        // 7/4/14
        put("\\b\\d{1,2}/\\d{1,2}/\\d{2}\\b", "MM/dd/yy");
        // Jan 1 2014; January 1 2014
        put("\\b(Jan(uary)?|Feb(ruary)?|Mar(ch)?|Apr(il)?|May|June?|July?|Aug(ust)?|Sep(tember)?|Oct(ober)?|Nov(ember)?|Dec(ember)?) \\d{1,2} \\d{4}\\b", "MMM dd yyyy");
        // Jan 1, 2014; January 1, 2014
        put("\\b(Jan(uary)?|Feb(ruary)?|Mar(ch)?|Apr(il)?|May|June?|July?|Aug(ust)?|Sep(tember)?|Oct(ober)?|Nov(ember)?|Dec(ember)?) \\d{1,2}, \\d{4}\\b", "MMM dd, yyyy");
        // Jan 2014; January 2014
        put("\\b(Jan(uary)?|Feb(ruary)?|Mar(ch)?|Apr(il)?|May|June?|July?|Aug(ust)?|Sep(tember)?|Oct(ober)?|Nov(ember)?|Dec(ember)?) \\d{4}\\b", "MMM yyyy");
        // Jan, 2014; January, 2014
        put("\\b(Jan(uary)?|Feb(ruary)?|Mar(ch)?|Apr(il)?|May|June?|July?|Aug(ust)?|Sep(tember)?|Oct(ober)?|Nov(ember)?|Dec(ember)?), \\d{4}\\b", "MMM, yyyy");
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

    private static final Map<Pattern, SimpleDateFormat> DATE_FORMAT_PATTERNS = new LinkedHashMap<Pattern, SimpleDateFormat>();
    private static final SimpleDateFormat NORMALIZED_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    //private static MessageDigest md5Generator;

    public DateNormalizer() {
        for (Map.Entry<String, String> entry : dateFormatRegexps.entrySet()) {
            DATE_FORMAT_PATTERNS.put(
                    Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE),
                    new SimpleDateFormat(entry.getValue()));
        }
        System.out.println("DateNormalizer constructor fired.");
        //md5Generator = MessageDigest.getInstance("MD5");
    }

//    private String md5(String string) {
//        md5Generator.reset();
//        md5Generator.update(string.getBytes());
//        BigInteger bi = new BigInteger(1, md5Generator.digest());
//        return bi.toString(16);
//    }

    public String normalize(String text) {

        Map<String, String> replacements = new HashMap<String, String>();
        int counter = 0;
        
        for (Map.Entry<Pattern, SimpleDateFormat> entry : DATE_FORMAT_PATTERNS.entrySet()) {
            Pattern pattern = entry.getKey();
            SimpleDateFormat format = entry.getValue();
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                try {
                    String match = matcher.group(0);
                    //String key = this.md5(match);
                    String key = String.format("{{%d}}", counter++);
                    replacements.put(key, NORMALIZED_FORMAT.format(format.parse(match)));
                    text = text.replace(match, key);
                    //System.out.println(text);
                } catch (ParseException pe) {
                    // ignore exceptions
                }
            }
        }

        for (String placeholder : replacements.keySet()) {
            text = text.replace(placeholder, replacements.get(placeholder));
        }

        return text;
    }

    public static void main(String[] args) {
        try {
            DateNormalizer dp = new DateNormalizer();
            System.out.println(dp.normalize(args[0]));
        } catch (Exception nsae) {
            System.err.println(nsae.toString());
        }
    }
}
