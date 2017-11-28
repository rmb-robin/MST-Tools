package com.mst.metadataProviders;

import java.io.File;

public class TestHl7Provider {

	private String getFullFilePath(String file){
		return System.getProperty("user.dir") + File.separator + "testData" + File.separator + file;
	}
	
	
	public String getInput(){
		String filePath =getFullFilePath("testHl7.txt");
		return TestDataProvider.getFileText(filePath);
	}
}
