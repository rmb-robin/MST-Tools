package com.mst.metadataProviders;

import java.io.File;
import java.util.List;



public class TestHl7Provider {

	private String getFullFilePath(String file){
		return System.getProperty("user.dir") + File.separator + "testData" + File.separator + file;
	}
	
	
	public String getInput(){
		String filePath =getFullFilePath("testHl7.txt");
		return TestDataProvider.getFileText(filePath);
	}
	
	public List<String> getAllValues(){
		String filePath =getFullFilePath("hl7Values.txt");
		return TestDataProvider.readLines(filePath);
	}
}
