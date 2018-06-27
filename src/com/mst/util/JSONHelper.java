package com.mst.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mst.model.ICDQueryOutput;

public class JSONHelper {

	public static void main(String[] args) {
		List<Object> icdQueryOutputList = Arrays.asList(new ICDQueryOutput("1","2","3","4","5"),new ICDQueryOutput("2","3","4","5","6"));
		String jsonFileName = "ICDQueryOutput.json";
		writeToFile(icdQueryOutputList, jsonFileName);
	}
	public static void writeToFile(List<Object> objectList, String jsonFileName) {
		// used to pretty print result
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String strJson = gson.toJson(objectList);
		FileWriter writer = null;
		try {
			writer = new FileWriter(jsonFileName);
			writer.write(strJson);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
