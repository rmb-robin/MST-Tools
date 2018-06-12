package com.mst.metadataProviders;

import java.util.List;
import java.io.File;
import java.util.HashSet;

import com.mst.interfaces.sentenceprocessing.PartOfSpeechAnnotatorEntityProvider;
import com.mst.model.sentenceProcessing.PartOfSpeechAnnotatorEntity;


public class PartOfSpeechHardcodedAnnotatorEntityProvider implements PartOfSpeechAnnotatorEntityProvider {

	private PartOfSpeechAnnotatorEntity entity;
	
	public PartOfSpeechAnnotatorEntity getPartOfSpeechAnnotatorEntity() {
		entity = new PartOfSpeechAnnotatorEntity();
		
		populatePartOfSpeech(getFullFilePath("pos_prepsignal.txt"),"IN");
		populatePartOfSpeech(getFullFilePath("pos_ds.txt"), "DP");
		populatePartOfSpeech(getFullFilePath("pos_nt.txt"), "NEG");
		populatePartOfSpeech(getFullFilePath("pos_cc.txt"), "CC");
		populatePartOfSpeech(getFullFilePath("pos_DET.txt"), "DET");
		populatePartOfSpeech(getFullFilePath("pos_punction.txt"), "PUNCTUATION");
		return entity;
	}

	private String getFullFilePath(String fileName){
		return System.getProperty("user.dir") + File.separator + "testData" + File.separator + fileName;
	}

	
	private void populatePartOfSpeech(String fileName, String key){
		entity.getAnnotators().put(key, getPOSValues(fileName));
	}

	private HashSet<String> getPOSValues(String fileName){
		List<String> lines = TestDataProvider.readLines(fileName);
		return new HashSet<String>(lines);
	}

}

