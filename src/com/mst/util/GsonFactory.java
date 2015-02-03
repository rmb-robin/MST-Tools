package com.mst.util;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/*
 * Custom factory for Gson that registers a de/serializer that can handle MongoDB's ISODate type.
*/
public class GsonFactory {
	
	private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private final static Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
	
	public static Gson build() {
        GsonBuilder b = new GsonBuilder();
        
        JsonSerializer<Date> ser = new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				Date d = (Date) src;
	        	SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
		        JsonObject jo = new JsonObject();
		        jo.addProperty("$date", format.format(d));
				return jo;
				
				
			}
		};
		
		JsonDeserializer<Date> deser = new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
				Matcher matcher = pattern.matcher(json.toString());
				matcher.find();
				
				SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
				Date date = null;
				try {
					date = format.parse(matcher.group(0));
				} catch(ParseException e) { }
				
				return date;
			}
		};
        
		b.registerTypeAdapter(Date.class, ser);
		b.registerTypeAdapter(Date.class, deser);
		
        return b.create();
    }
}

/*
public static Gson build(final List<String> fieldExclusions, final List<Class<?>> classExclusions) {
	GsonBuilder b = new GsonBuilder();
	b.addSerializationExclusionStrategy(new ExclusionStrategy() {
	    @Override
	    public boolean shouldSkipField(FieldAttributes f) {
	        return fieldExclusions == null ? false : fieldExclusions.contains(f.getName());
	    }
	
	    @Override
	    public boolean shouldSkipClass(Class<?> clazz) {
	        return classExclusions == null ? false : classExclusions.contains(clazz);
	    }
	});
return b.create();

}
*/