package com.mst.tools.test;

import java.util.ArrayList;

import com.mst.model.Sentence;
import com.mst.tools.Annotator;
import com.mst.tools.MetadataParser;

public class Util {
	public Sentence annotateSentence(String input) {
		Sentence output = new Sentence();
		
		try {
			Annotator ann = new Annotator();
			MetadataParser mdp = new MetadataParser();
			
			ArrayList<Sentence> sentences = ann.annotate(input, false);
			
			output = sentences.get(0);
			
			mdp.parseComplex(output);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return output;
	}
}
