package com.mst.testcases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.utils.Assert.AssertionFailedException;

import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.model.gentwo.PartOfSpeechAnnotatorEntity;
import com.mst.model.gentwo.Verb;
import com.mst.model.gentwo.VerbProcessingInput;
import com.mst.model.gentwo.VerbTense;
import com.mst.model.gentwo.VerbType;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.PartOfSpeechAnnotatorImpl;
import com.mst.sentenceprocessing.SemanticTypeSentenceAnnotatorImpl;
import com.mst.sentenceprocessing.VerbProcessorImpl;
import com.mst.testHelpers.NGramsHardCodedProvider;
import com.mst.testHelpers.PartOfSpeechHardcodedAnnotatorEntityProvider;
import com.mst.testHelpers.SemanticTypeHardCodedProvider;
import com.mst.testHelpers.TestDataProvider;
import com.mst.testHelpers.VerbProcessingInputProvider;
import static org.junit.Assert.*;

public class VerbProcessorTest {

	VerbProcessingInput input = new VerbProcessingInputProvider().getInput();
	NGramsSentenceProcessorImpl processor = new NGramsSentenceProcessorImpl();
	VerbProcessorImpl verbProcessor = new VerbProcessorImpl();
	NGramsHardCodedProvider ngramsProvider = new NGramsHardCodedProvider();
	
	PartOfSpeechAnnotatorEntity entity = new PartOfSpeechHardcodedAnnotatorEntityProvider().getPartOfSpeechAnnotatorEntity();
	PartOfSpeechAnnotatorImpl annotatorImpl = new PartOfSpeechAnnotatorImpl();		
	
	
	@Test 
	public void infinitivePhraseTest() throws Exception{
		testInfinitiveVerbs("She recently had a breast ultrasound; however, she does not continue to get mammograms.","IN","infinitiveSignal");	
		testInfinitiveVerbs("She cannot continue to have hormone injections following the allergic reaction.","IN","infinitiveSignal");	
		testInfinitiveVerbs("To do an MRI next is the next step.","IN","infinitiveSignal");	
		//testInfinitiveVerbs("Her physician may start to infuse the therapy so I will have to take her to her doctor's office tomorrow.","IN","infinitiveSignal");	
		testInfinitiveVerbs("The patient has been taken to the operating room","IN","IN");	
		
		
	
				 
			//		to=IN, infuse=action verb; to=IN, take=action verb	to=infinitive signal, infuse=infinitive verb; to=infinitive signal, take=infinitive verb
		
		
	}
	
	@Test 
	public void action_pluralInfinitivePresentTest(){
		
		testActionVerb("The cyst measures 3.5 cm",VerbTense.PluralInfinitivePresent);
	}
	
	@Test 
	public void action_infinitivePresentTest(){
		testActionVerb("The cyst measure 3.5 cm",VerbTense.InfinitivePresent);
	}
	
	@Test 
	public void action_pastTest(){
		testActionVerb("The cyst measured 3.5 cm",VerbTense.Past);
	}
		
	@Test 
	public void action_presentTest(){
		testActionVerb("The cyst measuring 3.5 cm",VerbTense.Present);
		
	}
	
	@Test(expected = Exception.class)
	public void test_nullinput() throws Exception{
		verbProcessor.process(new ArrayList<WordToken>(), null);
	}

	@Test(expected = Exception.class)
	public void test_nullwordTokens() throws Exception{
		verbProcessor.process(null, new VerbProcessingInput());
	}
	
	@Test 
	public void test_linkModalVerbs() throws Exception{
		testLinkingModalVerb("I am going to the doctor",VerbTense.Present,VerbType.LV,"am", "existence");
		testLinkingModalVerb("there is a cyst",VerbTense.Present,VerbType.LV,"is", null);
		testLinkingModalVerb("i was at the doctor",VerbTense.Past,VerbType.LV,"was", null);
		testLinkingModalVerb("i can have a cyst",VerbTense.Present,VerbType.MV,"can-have", "possibility");
		testLinkingModalVerb("i can have had a cyst",VerbTense.Past,VerbType.LV,"can-have-had", "possibility");
	}
	
	
	private void testInfinitiveVerbs(String sentenceText,String posBefore, String posAfter) throws Exception{
		Sentence sentence = TestDataProvider.getSentences(sentenceText).get(0);
		sentence = processor.process(sentence, new NGramsHardCodedProvider().getNGrams());
	
		List<WordToken> modifiedTokens  = annotatorImpl.annotate(sentence.getModifiedWordList(), entity);
		List<WordToken> result;

		for(WordToken w : modifiedTokens)
		{
			if(w.getToken().toLowerCase().equals("to"))
				assertInfinitivePhrase(posBefore, w);
		}
		
		result = verbProcessor.process(modifiedTokens,input);	
	
		WordToken token=null;
		int index = 0;
		for(WordToken w : result)
		{
			if(w.getToken().toLowerCase().equals("to")){
				assertInfinitivePhrase(posAfter, w);
				token = result.get(index+1);
				if(token.getVerb()==null)
				{
					index+=1;
					continue;
				}
				
				assertEquals("IV",token.getPos());
				Verb verb = token.getVerb();
				assertEquals("IV",token.getPos());;
				assertEquals(VerbType.IV, verb.getVerbType());
			}
			index +=1;
		}
	}

	private void assertInfinitivePhrase(String pos, WordToken token){
		assertEquals(pos, token.getPos());
	}
	
	
	private void testLinkingModalVerb(String sentenceText, VerbTense tense,VerbType verbType, String verbWord, String state) throws Exception{
		Sentence sentence = TestDataProvider.getSentences(sentenceText).get(0);
		processor.process(sentence, new NGramsHardCodedProvider().getNGrams());
		
		List<WordToken> result;
			result = verbProcessor.process(sentence.getModifiedWordList(),input);
	
			
			long count = result.stream().filter((a)-> a.getVerb()!=null).count();
			WordToken token = result.stream().filter((a)->a.getVerb()!=null).findFirst().get();
			
			assertEquals(1, count);
			assertEquals(verbType.toString(),token.getPos());
			assertEquals(verbWord, token.getToken());
			
			Verb verb = token.getVerb();
		
			assertEquals(tense, verb.getVerbTense());
			assertEquals(verbType, verb.getVerbType());
			assertEquals(state, verb.getVerbState());
	
	}
	
	private void testActionVerb(String sentenceText, VerbTense tense){
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
