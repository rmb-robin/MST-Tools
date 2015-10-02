package com.mst.util;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mst.model.MapValue;

/*
 * Custom factory for Gson that registers a de/serializer that can handle MongoDB's ISODate type.
*/
public class GsonFactory {
	
	private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private final static String MONGO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.000'Z'";
	private final static Pattern MONGO_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
	
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
				Matcher matcher = MONGO_DATE_PATTERN.matcher(json.toString());
				matcher.find();
				
				SimpleDateFormat format = new SimpleDateFormat(MONGO_DATE_FORMAT);
				Date date = null;
				try {
					date = format.parse(matcher.group(0));
				} catch(ParseException e) { 
					e.printStackTrace();
				}
				
				return date;
			}
		};
		
		JsonSerializer<Multimap<String, String>> mmSerializer = new JsonSerializer<Multimap<String, String>>() {
			//@SuppressWarnings("serial")
			//private final Type t = new TypeToken<Map<String, String>>() {}.getType();
			  
			@Override
			public JsonElement serialize(Multimap<String, String> src, Type typeOfSrc, JsonSerializationContext context) {
				return context.serialize(src.asMap());
			}
		};
        
		JsonDeserializer<Multimap<String, ?>> mmDeserializer = new JsonDeserializer<Multimap<String, ?>>() {
			@Override
			public Multimap<String, ?> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
				
				Type mapType = new TypeToken<Map<String, Collection<MapValue>>>(){}.getType();
				Map<String, Collection<MapValue>> map = context.deserialize(json, mapType);
				
				Multimap<String, MapValue> multimap = ArrayListMultimap.create();
				
				for(Entry<String, Collection<MapValue>> e : map.entrySet()) {
		            Collection<MapValue> value = (Collection<MapValue>) e.getValue();
		            multimap.putAll(e.getKey(), value);
		        }
				
				return multimap;
			}
		};
		
		b.registerTypeAdapter(Date.class, ser);
		b.registerTypeAdapter(Date.class, deser);
		b.registerTypeAdapter(Multimap.class, mmSerializer);
		b.registerTypeAdapter(Multimap.class, mmDeserializer);
		
        return b.create();
    }
	
	public static Gson build(ExclusionStrategy exclude) {
        GsonBuilder b = new GsonBuilder()
        	.setExclusionStrategies(exclude);
        
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
				Matcher matcher = MONGO_DATE_PATTERN.matcher(json.toString());
				matcher.find();
				
				SimpleDateFormat format = new SimpleDateFormat(MONGO_DATE_FORMAT);
				Date date = null;
				try {
					date = format.parse(matcher.group(0));
				} catch(ParseException e) { 
					e.printStackTrace();
				}
				
				return date;
			}
		};
		
		JsonSerializer<Multimap<String, String>> mmSerializer = new JsonSerializer<Multimap<String, String>>() {
			//@SuppressWarnings("serial")
			//private final Type t = new TypeToken<Map<String, String>>() {}.getType();
			  
			@Override
			public JsonElement serialize(Multimap<String, String> src, Type typeOfSrc, JsonSerializationContext context) {
				return context.serialize(src.asMap());
			}
		};
        
		JsonDeserializer<Multimap<String, ?>> mmDeserializer = new JsonDeserializer<Multimap<String, ?>>() {
			@Override
			public Multimap<String, ?> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
				
				Type mapType = new TypeToken<Map<String, Collection<MapValue>>>(){}.getType();
				Map<String, Collection<MapValue>> map = context.deserialize(json, mapType);
				
				Multimap<String, MapValue> multimap = ArrayListMultimap.create();
				
				for(Entry<String, Collection<MapValue>> e : map.entrySet()) {
		            Collection<MapValue> value = (Collection<MapValue>) e.getValue();
		            multimap.putAll(e.getKey(), value);
		        }
				
				return multimap;
			}
		};
		
		b.registerTypeAdapter(Date.class, ser);
		b.registerTypeAdapter(Date.class, deser);
		b.registerTypeAdapter(Multimap.class, mmSerializer);
		b.registerTypeAdapter(Multimap.class, mmDeserializer);
		
        return b.create();
    }
	
//	private <V> Type multimapTypeToMapType(Type type) {
//        final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
//        assert typeArguments.length == 2;
//        @SuppressWarnings("unchecked")
//        final TypeToken<Map<String, Collection<V>>> mapTypeToken = new TypeToken<Map<String, Collection<V>>>() {}
//        .where(new TypeParameter<V>() {}, (TypeToken<V>) TypeToken.of(typeArguments[1]));
//        return mapTypeToken.getType();
//    }
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