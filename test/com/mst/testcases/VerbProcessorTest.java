package com.mst.testcases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.model.gentwo.Verb;
import com.mst.model.gentwo.VerbProcessingInput;
import com.mst.model.gentwo.VerbTense;
import com.mst.model.gentwo.VerbType;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.VerbProcessorImpl;
import com.mst.testHelpers.NGramsHardCodedProvider;
import com.mst.testHelpers.TestDataProvider;
import com.mst.testHelpers.VerbProcessingInputProvider;
import static org.junit.Assert.*;

public class VerbProcessorTest {

	VerbProcessingInput input = new VerbProcessingInputProvider().getInput();
	NGramsSentenceProcessorImpl processor = new NGramsSentenceProcessorImpl();
	VerbProcessorImpl verbProcessor = new VerbProcessorImpl();
	
	@Test 
	public void action_pluralInfinitivePresentTest(){
		
		testSentence("The cyst measures 3.5 cm",VerbTense.PluralInfinitivePresent);
	}
	
	@Test 
	public void action_infinitivePresentTest(){
		testSentence("The cyst measure 3.5 cm",VerbTense.InfinitivePresent);
	}
	
	@Test 
	public void action_pastTest(){
		testSentence("The cyst measured 3.5 cm",VerbTense.Past);
	}
		
	@Test 
	public void action_presentTest(){
		testSentence("The cyst measuring 3.5 cm",VerbTense.Present);
		
	}
	
	@Test(expected = Exception.class)
	public void test_nullinput() throws Exception{
		verbProcessor.process(new ArrayList<WordToken>(), null);
	}

	private void testSentence(String sentenceText, VerbTense tense){
		Sentence sentence = TestDataProvider.getSentences(sentenceText).get(0);
		processor.process(sentence, new NGramsHardCodedProvider().getNGrams());
		
		List<WordToken> result;
		try {
			result = verbProcessor.process(sentence.getModifiedWordList(),input);
	
			
			long count = result.stream().filter((a)-> a.getVerb()!=null).count();
			WordToken token = result.stream().filter((a)->a.getVerb()!=null).findFirst().get();
			assertEquals(5, result.size());
			assertEquals(1, count);
			assertEquals("AV",token.getPos());
			assertEquals("measure", token.getToken());
			
			Verb verb = token.getVerb();
			assertEquals("AV",token.getPos());
			assertEquals("AV",token.getPos());
			assertEquals(tense, verb.getVerbTense());
			assertEquals(VerbType.AV, verb.getVerbType());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void print(VerbProcessingInput input ){
		input.getActionVerbTable().getVerbsByWord().forEach((k,v)->{
			System.out.println("Tense: " + v.getVerbTense() + " value " + k);
		});
	}
}
