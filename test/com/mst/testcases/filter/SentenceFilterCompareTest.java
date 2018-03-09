package com.mst.testcases.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mst.metadataProviders.TestHl7Provider;
import com.mst.model.SentenceQuery.SentenceQueryInput;

public class SentenceFilterCompareTest {

	public String callPOSTService(String endpoint, String body, String input) {
		String ret = null;

		HttpURLConnection conn = null;

		try {
			URL url = new URL(endpoint);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");

			OutputStreamWriter streamWriter = new OutputStreamWriter(conn.getOutputStream());

			streamWriter.write(input);
			streamWriter.flush();

			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			} catch (IOException ioe) {
				;
				System.out.println(ioe.getMessage());
			}

			String response = null;
			StringBuffer buffer = new StringBuffer();

			while ((response = br.readLine()) != null) {
				buffer.append(response);
			}

			return buffer.toString();

		} catch (Exception e) {
			Exception t = e;
			System.out.println(e.getMessage());
		} finally {
			if (conn != null)
				conn.disconnect();
		}

		return ret;
	}

	public Object convertJsonToObject(String content, Class<?> clazz)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Object ret = mapper.readValue(content, clazz);
		return ret;
	}

	public JsonNode convertToJsonNode(String content) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();

		JsonNode ret = mapper.readTree(content);
		return ret;
	}

	private Map<String, String> getSentencesFromResult(String results)
			throws JsonParseException, JsonMappingException, IOException {
		JsonNode origNode = convertToJsonNode(results);
		Map<String, String> ret = new HashMap<String, String>();
		List<String> ids = new ArrayList<String>();

		List<JsonNode> test = origNode.findValues("sentenceId");
		for (JsonNode one : test) {
			ids.add(one.toString());
		}
		test = origNode.findValues("sentence");
		int i = 0;
		for (JsonNode one : test) {
			ret.put(ids.get(i++), one.asText());
		}
		return ret;
	}

	@Test
	public void loadRawHl7IntoAPI() throws Exception {
		File input = new File("C:/Users/Bryan/inputs");
		File output = new File("C:/Users/Bryan/outputs");
		if (input.exists()) {
			processfile(input, output);
		}

	}

	private final static String origEndPoint = "http://10.210.192.4:8080/mst-sentence-service.hotfix/webapi/sentence/query/";
	private final static String newEndPoint = "http://10.210.192.4:8080/mst-sentence-service.confidence/webapi/sentence/query/";

	private void processfile(File input, File output) throws Exception {

		if ( input.exists() && input.isDirectory() ) {
			for ( File file : input.listFiles() ) {
				processfile(file, output);
			}
			return;
		}
		System.out.println("Processing : " + input.getCanonicalPath());
	
		String sentenceInput = null;
		try {
			sentenceInput = readSentenceQueryInput(input);
			SentenceQueryInput siObj = (SentenceQueryInput) convertJsonToObject(sentenceInput, SentenceQueryInput.class);
		} catch ( Exception e) {
			System.out.println("Failure to convert input to query: " + e.getMessage());
			return;
		}
		output.mkdirs();
		Map<String, String> originalSentences  = null;
		try {
			originalSentences = getSentences(origEndPoint, sentenceInput, new File(output.getAbsolutePath() + File.separator + input.getName()+".originalResults.toJson"));
		} catch (Exception e) {
			System.out.println("Failure processing original sentence results " + e.getMessage());
		}
		Map<String, String> newSentences  = null;
		try {
			newSentences = getSentences(newEndPoint, sentenceInput, new File(output.getAbsolutePath() + File.separator + input.getName()+".newResults.toJson"));
		} catch (Exception e) {
			System.out.println("Failure processing original sentence results " + e.getMessage());
		}

		StringBuffer myString = new StringBuffer();
		for (String id : originalSentences.keySet()) {
			if (!newSentences.containsKey(id)) {
				myString.append("Missing id:" + id + " sentence: " + originalSentences.get(id) + "\n");
			}
		}
		File resultsFile = new File(output.getAbsolutePath() + File.separator + input.getName()+".compareResults");
		Files.write(resultsFile.toPath(), myString.toString().getBytes(), StandardOpenOption.CREATE);
	}

	private Map<String, String> getSentences(String myEndpoint, String query, File outputFile) throws JsonParseException, JsonMappingException, IOException {
		Map<String, String> result = null;
		
		String body = new TestHl7Provider().getInput();
		String resultsStr = prettyPrint(callPOSTService(myEndpoint, body, query));
		result = getSentencesFromResult(resultsStr);
		Files.write(outputFile.toPath(), resultsStr.getBytes(), StandardOpenOption.CREATE);
		
		System.out.println("Outputing file to: " + outputFile.getAbsolutePath());
		return result;
	}

	public String prettyPrint(String content) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		Object test = mapper.readValue(content, Object.class);
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(test);
	}

	public String readSentenceQueryInput(File input) throws IOException {
		String content = new String(Files.readAllBytes(input.toPath()));
		System.out.println("content: " + content);
		return content;
	}
}
