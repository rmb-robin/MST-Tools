package com.mst.tools.test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.mst.model.SentenceMetadata;

public class VerbHelperTest {
	Util util = new Util();
	
	@Test
	public final void testJJAsSubjCWithRB() {
		
		SentenceMetadata metadata = util.annotateSentence("The tumor stage was Widely metastatic.").getMetadata();	
		
		assertEquals("SUBJ must be 'stage'", "stage", metadata.getVerbMetadata().get(0).getSubj().getToken());
		assertEquals("Verb must be 'was'", "was", metadata.getVerbMetadata().get(0).getVerbs().get(0).getToken());		
		assertEquals("SUBJC must be 'metastatic'", "metastatic", metadata.getVerbMetadata().get(0).getSubjC().get(0).getToken());
	}
	
	@Test
	public final void testJJAsSubjC() {
		
		SentenceMetadata metadata = util.annotateSentence("The tumor stage is metastatic.").getMetadata();	
		
		assertEquals("SUBJ must be 'stage'", "stage", metadata.getVerbMetadata().get(0).getSubj().getToken());
		assertEquals("Verb must be 'is'", "is", metadata.getVerbMetadata().get(0).getVerbs().get(0).getToken());		
		assertEquals("SUBJC must be 'metastatic'", "metastatic", metadata.getVerbMetadata().get(0).getSubjC().get(0).getToken());
	}
	
	@Test
	public final void testRBAsSubjC() {
		// TODO this isn't the best example sentence for RB
		SentenceMetadata metadata = util.annotateSentence("His heart rate is increasing rapidly.").getMetadata();
		
		assertEquals("SUBJ must be 'rate'", "rate", metadata.getVerbMetadata().get(0).getSubj().getToken());
		assertEquals("Verb must be 'is increasing'", "is increasing", metadata.getVerbMetadata().get(0).getVerbString());		
		assertEquals("SUBJC must be 'rapidly'", "rapidly", metadata.getVerbMetadata().get(0).getSubjC().get(0).getToken());
	}
}
