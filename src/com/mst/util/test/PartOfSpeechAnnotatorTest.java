package com.mst.util.test;

import org.junit.Test;

import com.mst.model.gentwo.PartOfSpeechAnnotatorEntity;

public class PartOfSpeechAnnotatorTest {

	
	@Test
	public void annotate(){
		PartOfSpeechHardcodedAnnotatorEntityProvider provider = new PartOfSpeechHardcodedAnnotatorEntityProvider();
		PartOfSpeechAnnotatorEntity entity = provider.getPartOfSpeechAnnotatorEntity();
	}
	
}
