package com.mst.util.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.mst.model.Sentence;
import com.mst.tools.Annotator;

public class AnnotatorTest {
	
	@Test
	public final void testPOSOverrides() {
		Annotator ann;
		
		try {
			ann = new Annotator();
			
			ArrayList<Sentence> sentences = ann.annotate("this,(test)- sucks", false);
			assertEquals("Punctuation POS is token itself", true, sentences.get(0).getWordList().get(1).getToken().equalsIgnoreCase(","));
			assertEquals("Punctuation POS is token itself", true, sentences.get(0).getWordList().get(2).getToken().equalsIgnoreCase("("));
			assertEquals("Punctuation POS is token itself", true, sentences.get(0).getWordList().get(4).getToken().equalsIgnoreCase(")"));
			assertEquals("Punctuation POS is token itself", true, sentences.get(0).getWordList().get(5).getToken().equalsIgnoreCase("-"));
			
			sentences = ann.annotate("{braces are cool}", false);
			assertEquals("Punctuation POS is token itself", true, sentences.get(0).getWordList().get(0).getPOS().equalsIgnoreCase("{"));
			assertEquals("Punctuation POS is token itself", true, sentences.get(0).getWordList().get(4).getPOS().equalsIgnoreCase("}"));
			
			sentences = ann.annotate("I have to scan this again.", false);
			assertEquals("Scan as VB POS", true, sentences.get(0).getWordList().get(3).getPOS().equalsIgnoreCase("VB"));
			
			sentences = ann.annotate("The bone scan was successful.", false);
			assertEquals("Bone scan - override POS to NN", true, sentences.get(0).getWordList().get(2).getPOS().equalsIgnoreCase("NN"));
			
			sentences = ann.annotate("The ct scan shows a cyst.", false);
			assertEquals("Override preceding diap ST POS to NN", true, sentences.get(0).getWordList().get(2).getPOS().equalsIgnoreCase("NN"));
			assertEquals("Override 'show(s)' in all cases to VB", true, sentences.get(0).getWordList().get(3).getPOS().equalsIgnoreCase("VB"));
			
			sentences = ann.annotate("I will show you!", false); // seems a bit extreme
			assertEquals("Override 'show(s)' in all cases to VB", true, sentences.get(0).getWordList().get(2).getPOS().equalsIgnoreCase("VB"));
			
			sentences = ann.annotate("CT and Dexa scans are expensive.", false);
			assertEquals("Override 'ct|dexa' in all cases to NN", true, sentences.get(0).getWordList().get(0).getPOS().equalsIgnoreCase("NN"));
			assertEquals("Override 'ct|dexa' in all cases to NN", true, sentences.get(0).getWordList().get(2).getPOS().equalsIgnoreCase("NN"));
			
			sentences = ann.annotate("He survived by eating grass.", false); // this is technically a false positive result
			assertEquals("Override -ing in a PP to JJ", true, sentences.get(0).getWordList().get(3).getPOS().equalsIgnoreCase("JJ"));
			
			sentences = ann.annotate("He slept on tufted grass.", false);
			assertEquals("Override -ed in a PP to JJ", true, sentences.get(0).getWordList().get(3).getPOS().equalsIgnoreCase("JJ"));
			
			sentences = ann.annotate("He survived by eat grass.", false); // another false positive result but it tests the case
			assertEquals("Override non -ed/-ing in a PP to NN", true, sentences.get(0).getWordList().get(3).getPOS().equalsIgnoreCase("NN"));
			
			sentences = ann.annotate("I like to run.", false);
			assertEquals("No VB overrides", true, sentences.get(0).getWordList().get(3).getPOS().equalsIgnoreCase("VB"));
			
			sentences = ann.annotate("Have one of the following as determined by information collected from their medical records, telephone interviews or from a referring physician:.", false);
			assertEquals("VBG override to JJ - following", true, sentences.get(0).getWordList().get(4).getPOS().equalsIgnoreCase("JJ"));
			assertEquals("VBG override to JJ - referring", true, sentences.get(0).getWordList().get(20).getPOS().equalsIgnoreCase("JJ"));
			
			sentences = ann.annotate("No transformed lymphoma from indolent to aggressive.", false);
			assertEquals("VBN override to JJ - transformed", true, sentences.get(0).getWordList().get(1).getPOS().equalsIgnoreCase("JJ"));
			
			sentences = ann.annotate("Consent by the physician from the clinic where the subject was identified, or listed as the treating physician by the tumor registry or surgical pathology report.", false);
			assertEquals("VBG override to JJ - treating", true, sentences.get(0).getWordList().get(17).getPOS().equalsIgnoreCase("JJ"));
			
			sentences = ann.annotate("Subject is capable of understanding and complying with parameters of the protocol and able to sign and date the informed consent.", false);
			assertEquals("VBN override to JJ - informed", false, sentences.get(0).getWordList().get(19).isLinkingVerb());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}   
	}
}
