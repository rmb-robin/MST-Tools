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
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mst.model.MapValue;

/*
 * Custom factory for Gson that registers a de/serializer that can handle MongoDB's ISODate type and Multimaps.
*/
public class GsonFactory {
	
	private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private final static String MONGO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.000'Z'";
	private final static Pattern MONGO_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
	
	private final static JsonSerializer<Date> ser = new JsonSerializer<Date>() {
		@Override
		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
			Date d = (Date) src;
        	SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
	        JsonObject jo = new JsonObject();
	        jo.addProperty("$date", format.format(d));
			return jo;
		}
	};
	
	private final static JsonDeserializer<Date> deser = new JsonDeserializer<Date>() {
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
	
	private final static JsonSerializer<Multimap<String, ?>> mmSerializer = new JsonSerializer<Multimap<String, ?>>() {
		//private final Type t = new TypeToken<Map<String, ?>>() {}.getType();
		
		@Override
		public JsonElement serialize(Multimap<String, ?> src, Type typeOfSrc, JsonSerializationContext context) {
			//return context.serialize(src.asMap());
			
			// for string values, the above single-line return is sufficient.
			
			// for numeric values, it's a different story.
			// guava multimaps have one key and a collection of values. when allowed to serialize as above, numeric values in the collection are treated as strings,
			// e.g. "[3.1]". The left and right brackets are actually present when obtaining the value based on the key.
			// the code below strips these brackets and loops through the members of the array, creating a new array with the appropriate data type.
			// the whole point of all of this is to enable mongo queries on absolute values that can take advantage of strongly-typed data (less than, greater than, etc)
			
			final Map<String, ?> map = src.asMap();
			
			JsonObject jsonObject = new JsonObject();
			
			for(String key : map.keySet()) {
				String val = map.get(key).toString();

				//if(key.equalsIgnoreCase("Absolute Value")) {
					val = val.substring(1, val.length()-1); // strip [ and ]
					String[] array = val.split(",");
					JsonArray jsarray = new JsonArray();
					
					for(String item : array) {
						item = item.trim();
						if(Constants.NUMERIC.matcher(item).matches()) {
							jsarray.add(new JsonPrimitive(Float.parseFloat(item)));
						} else {
							jsarray.add(new JsonPrimitive(item));
						}
					}
					jsonObject.add(key, jsarray);
					
				//} else {
				//	jsonObject.addProperty(key, val);
				//}
			}

			return jsonObject;
		}
	};
	
	private final static JsonDeserializer<Multimap<String, ?>> mmDeserializer = new JsonDeserializer<Multimap<String, ?>>() {
		@Override
		public Multimap<String, ?> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			
			// old version from 1.0 of structured data
			/*
			Type mapType = new TypeToken<Map<String, Collection<MapValue>>>(){}.getType();
			Map<String, Collection<MapValue>> map = context.deserialize(json, mapType);
			
			Multimap<String, MapValue> multimap = ArrayListMultimap.create();
			
			for(Entry<String, Collection<MapValue>> e : map.entrySet()) {
	            Collection<MapValue> value = (Collection<MapValue>) e.getValue();
	            multimap.putAll(e.getKey(), value);
	        }
			*/
			
			Type mapType = new TypeToken<Map<String, Collection<String>>>(){}.getType();
			Map<String, Collection<String>> map = context.deserialize(json, mapType);
			
			Multimap<String, String> multimap = ArrayListMultimap.create();
			
			for(Entry<String, Collection<String>> e : map.entrySet()) {
	            Collection<String> value = (Collection<String>) e.getValue();
	            multimap.putAll(e.getKey(), value);
	        }
			
			return multimap;
		}
	};
	
	public static Gson build() {
        GsonBuilder b = new GsonBuilder();
		
		b.registerTypeAdapter(Date.class, ser);
		b.registerTypeAdapter(Date.class, deser);
		b.registerTypeAdapter(Multimap.class, mmSerializer);
		b.registerTypeAdapter(Multimap.class, mmDeserializer);
		
        return b.create();
    }
	
	public static Gson build(ExclusionStrategy exclude) {
        GsonBuilder b = new GsonBuilder()
        	.setExclusionStrategies(exclude);
        
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