package com.mst.tools;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mst.model.DependentPhraseMetadata;
import com.mst.model.GenericToken;
import com.mst.model.NounPhraseMetadata;
import com.mst.model.PrepPhraseMetadata;
import com.mst.model.PrepPhraseToken;
import com.mst.model.Sentence;
import com.mst.model.SentenceMetadata;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.VerbPhraseToken;
import com.mst.model.WordToken;
import com.mst.model.graph.Edge;
import com.mst.model.graph.GraphData;
import com.mst.model.graph.Vertex;
import com.mst.util.Constants;
import com.mst.util.Constants.GraphClass;
import com.mst.util.Constants.GraphProperty;
import com.opencsv.CSVReader;

public class GraphProcessor {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private SimpleDateFormat orientSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static MessageDigest md5Generator;
	private Map<String, List<String>> qlco = new HashMap<>();
	
	public enum EdgeDirection {
		incoming, outgoing;
	}
	
	public GraphProcessor() { 
		try {
			md5Generator = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
			logger.error("Error getting MessageDigest", e);
		}
		// TODO reading from a csv is temporary. possibly go with redis instead.
		try(CSVReader reader = new CSVReader(new FileReader("/home/mst-admin/qualifiers.csv"))) {	
			// consume the header row
			String[] row = reader.readNext();
			
			while((row = reader.readNext()) != null) {
				List<String> edges = new ArrayList<>();
				for(String edge : row[1].split("\\|")) {
					edges.add("f_" + edge.toLowerCase());
				}
				// column A
				qlco.put(row[0].toLowerCase(), edges);
				// each item of column C (synonyms)
				for(String item : row[2].split("\\|")) {
					qlco.put(item.toLowerCase(), edges);
				}
				// each item of column D (abbreviations)
				for(String item : row[3].split("\\|")) {
					qlco.put(item.toLowerCase(), edges);
				}
			}
		} catch(IOException e) {
			logger.error("Error reading qualifiers.csv", e);
		}
	}
	
	public GraphData processSentence(Sentence s) {
		GraphData graphSentence = new GraphData();
		// this list of vertices will be accessed to build GraphEntries while going through the metadata
		List<Vertex> vertices = new ArrayList<>();
		List<Edge> edges = new ArrayList<>();
		
		Vertex newV = null;
		
		for(WordToken word : s.getWordList()) {
			String uuid = md5(s.getFullSentence() + word.getPosition());   // TODO get hash from somewhere else (mongo oid + position?)
			newV = new Vertex(GraphClass.Token, uuid, word.getToken().toLowerCase(), word.getPosition());
			
			Map<String, Object> props = new HashMap<>();
			
			props.put(GraphProperty.pos.toString(), word.getPOS());
			if(word.getSemanticType() != null) {
				props.put(GraphProperty.st.toString(), word.getSemanticType());
			}
			
			// low-level boolean values from the WordToken object.
			List<String> booleans = getTokenBooleans(word);
			for(String bool : booleans) {
				props.put(bool, true);
			}
			
			newV.setProps(props);
			
			vertices.add(newV);
		}
		
		// sentence vertices
		String sentenceUuid = md5(s.getPractice()+s.getStudy()+s.getFullSentence()); // TODO use mongo oid???
		
		Vertex vSource = new Vertex(GraphClass.Source, md5(s.getSource()), s.getSource(), 0);
		Vertex vPractice = new Vertex(GraphClass.Practice, md5(s.getPractice()), s.getPractice(), 0);
		Vertex vStudy = new Vertex(GraphClass.Study, md5(s.getStudy()), s.getStudy(), 0);
		Vertex vID = new Vertex(GraphClass.ID, md5(s.getPractice()+s.getStudy()+s.getId()), s.getId(), 0);
		String date = orientSDF.format(s.getProcedureDate());
		Vertex vProcedureDate = new Vertex(GraphClass.Date, md5(s.getPractice()+s.getStudy()+date.toString()), date.toString(), 0);
		Vertex vSentence = new Vertex(GraphClass.Sentence, sentenceUuid, s.getFullSentence(), (int) s.getPosition());
		
		for(String key : s.getMetadata().getSimpleMetadata().keySet()) {
			vSentence.getProps().put(key, s.getMetadata().getSimpleMetadata().get(key));
		}
				
		// sentence edges
		// old way
		/*
		edges.add(new Edge(GraphClass.has_source, vPractice, vSource));
		edges.add(new Edge(GraphClass.has_study, vPractice, vStudy));
		edges.add(new Edge(GraphClass.has_id, vStudy, vID));
		edges.add(new Edge(GraphClass.has_date, vID, vProcedureDate));
		edges.add(new Edge(GraphClass.has_sentence, vProcedureDate, vSentence));
		*/
		// new way - sentence links to everything
		edges.add(new Edge(GraphClass.has_source, vSentence, vSource));
		edges.add(new Edge(GraphClass.has_practice, vSentence, vPractice));
		edges.add(new Edge(GraphClass.has_study, vSentence, vStudy));
		edges.add(new Edge(GraphClass.has_id, vSentence, vID));
		edges.add(new Edge(GraphClass.has_date, vSentence, vProcedureDate));
		
		Vertex oldV = null;
		
		// link sentence to individual tokens via edges with idx property
		for(Vertex v : vertices) {
			Edge e = new Edge(GraphClass.has_token, vSentence, v);
			e.getProps().put("idx", v.getIdx());
			edges.add(e);
			
			if(oldV != null) {
				Edge e2 = new Edge(GraphClass.precedes, oldV, v);
				e2.getProps().put("idx", v.getIdx()-1);
				edges.add(e2);
			}
			
			oldV = v;
		}
		
		// add "global" vertices to list. Note this must occur after the above vertex loop or you'll add a bunch of junk edges
		vertices.add(vSource);
		vertices.add(vPractice);
		vertices.add(vStudy);
		vertices.add(vID);
		vertices.add(vProcedureDate);
		vertices.add(vSentence);
		
		SentenceMetadata metadata = s.getMetadata();
		
		vSentence.getProps().put("nounPhraseCount", metadata.getNounMetadata().size());
		vSentence.getProps().put("prepPhraseCount", metadata.getPrepMetadata().size());
		vSentence.getProps().put("verbPhraseCount", metadata.getVerbMetadata().size());
		vSentence.getProps().put("depPhraseCount", metadata.getDependentMetadata().size());
		vSentence.getProps().put("orphanCount", metadata.getOrphans().size());
		
		// do other metadata processing
		for(VerbPhraseMetadata vpm : metadata.getVerbMetadata()) {
			for(VerbPhraseToken vb : vpm.getVerbs()) {
				// not sure yet how to model compound verb relationships so passing null for now
				createVerbEdges(vb, vb.getPosition(), null, vertices, edges, metadata);
			}
			
			// relate the first verb of the phrase to all subjects
			for(VerbPhraseToken subj : vpm.getSubjects()) {
				createVerbEdges(subj, vpm.getVerbs().get(0).getPosition(), GraphClass.has_subj, vertices, edges, metadata);
				// Frame edge - first verb to subj
				GraphClass gClass = subj.isNegated() || vpm.getVerbs().get(0).isNegated() ? GraphClass.f_absence : GraphClass.f_presence;
				edges.add(new Edge(gClass, vertices.get(vpm.getVerbs().get(0).getPosition()), vertices.get(subj.getPosition())));
			}
			
			// relate each subject complement to the final token of the verb phrase
			for(VerbPhraseToken subjc : vpm.getSubjC()) {
				VerbPhraseToken lastVerb = vpm.getVerbs().get(vpm.getVerbs().size()-1);
				
				// if subjc = lastVerb, use first verb instead. Ex. "Central vasculature is congested."
				if(subjc.getToken().equalsIgnoreCase(lastVerb.getToken())) {
					lastVerb = vpm.getVerbs().get(0);
				}
				
				createVerbEdges(subjc, lastVerb.getPosition(), GraphClass.has_subjc, vertices, edges, metadata);
				// Frame edge - last verb to subjc
				GraphClass gClass = subjc.isNegated() || lastVerb.isNegated() ? GraphClass.f_absence : GraphClass.f_presence;
				edges.add(new Edge(gClass, vertices.get(lastVerb.getPosition()), vertices.get(subjc.getPosition())));
			}
		}
		
		// intra-prep phrase relationships
		for(PrepPhraseMetadata ppm : metadata.getPrepMetadata()) {
			// prep term
			int prepIdx = ppm.getPhrase().get(0).getPosition();
			int precedingToken = prepIdx-1;
			WordToken prep = s.getWordList().get(prepIdx);
			/*
			for(int i=0; i < ppm.getPhrase().size(); i++) {
				int idx = ppm.getPhrase().get(i).getPosition();
				
				if(ppm.isNegated()) {
					vertices.get(idx).getProps().put(GraphProperty.negated.toString(), true);
				}
				
				// edge between preposition and object(s)
				if(ppm.getPhrase().get(i).isObject()) {
					edges.add(new Edge(GraphClass.has_pp_object, vertices.get(prepIdx), vertices.get(idx)));
					// edge between token preceding preposition and object(s)
					if(precedingToken >= 0)
						edges.add(new Edge(GraphClass.has_pp_object, vertices.get(precedingToken), vertices.get(idx)));
				}
				
				if(i > 0) {
					// edges between each pp member
					edges.add(new Edge(GraphClass.has_pp_sibling, vertices.get(idx-1), vertices.get(idx)));	
				}
				
				// Frame edges
				if(prep.getToken().matches("in|of|within")) {
					//if(ppm.get)
				}
			}
			*/
			int i = 0;
			
			for(PrepPhraseToken ppt : ppm.getPhrase()) {
				int idx = ppt.getPosition();
				
				if(ppm.isNegated()) {
					vertices.get(idx).getProps().put(GraphProperty.negated.toString(), true);
				}
				
				// edge between preposition and object(s)
				if(ppt.isObject()) {
					edges.add(new Edge(GraphClass.has_pp_object, vertices.get(prepIdx), vertices.get(idx)));
					// edge between token preceding preposition and object(s)
					if(precedingToken >= 0)
						edges.add(new Edge(GraphClass.has_pp_object, vertices.get(precedingToken), vertices.get(idx)));
				}
				
				if(i > 0) {
					// edges between each pp member
					edges.add(new Edge(GraphClass.has_pp_sibling, vertices.get(idx-1), vertices.get(idx)));	
				}
				
				// Frame edges
				// prep is in|of|within, phrase obj is a bpoc, PP is modifying a dysn
				if(prep.getToken().matches("in|of|within")) {
					if(ppt.isObject()) {
						edges.add(new Edge(GraphClass.f_location, vertices.get(prepIdx-1), vertices.get(idx)));
					}
					/*
					if(ppt.isObject() && s.getWordList().get(idx).getSemanticType().equalsIgnoreCase("bpoc")) {
						if(s.getWordList().get(prepIdx-1).getSemanticType().equalsIgnoreCase("dysn")) {
							// f_finding_site frame
							edges.add(new Edge(GraphClass.f_finding_site, vertices.get(prepIdx-1), vertices.get(idx)));
						} else if(s.getWordList().get(prepIdx-1).getSemanticType().equalsIgnoreCase("diap")) {
							// f_procedure frame
							edges.add(new Edge(GraphClass.f_procedure, vertices.get(prepIdx-1), vertices.get(idx)));
						}
					}
					*/
				}
				
				i++;
			}
		}
		
		processDependentPhrases(edges, vertices, metadata);
		
		// Frames unbounded by a known phrase type
		for(int i=0; i < s.getWordList().size(); i++) {
			WordToken word = s.getWordList().get(i);
			WordToken prev = Constants.getToken(s.getWordList(), i-1);
			WordToken next = Constants.getToken(s.getWordList(), i+1);
			
			if(word.getSemanticType().equalsIgnoreCase("bpoc")) {
				if(prev.getToken().matches("left|right|bilateral")) {
					edges.add(new Edge(GraphClass.f_laterality, vertices.get(prev.getPosition()-1), vertices.get(word.getPosition()-1)));
				}
			} else if(word.getToken().equalsIgnoreCase(":")) {
				edges.add(new Edge(GraphClass.f_header, vertices.get(prev.getPosition()-1), vertices.get(word.getPosition()-1)));				
			} else if(word.getToken().matches("measur.*")) {
				if(next.getPOS().equalsIgnoreCase("CD") && !prev.isPrepPhraseObject()) {
					edges.add(new Edge(GraphClass.f_measurement, vertices.get(word.getPosition()-2), vertices.get(next.getPosition()-1)));
				}
			}
		}
		
		processNounPhrases(edges, vertices, s.getWordList(), metadata);
		
		graphSentence.setVertices(vertices);
		graphSentence.setEdges(edges);
		
		return graphSentence;
	}
	
	private void processNounPhrases(List<Edge> edges, List<Vertex> vertices, List<WordToken> words, SentenceMetadata metadata) {
		for(NounPhraseMetadata npm : metadata.getNounMetadata()) {
			// TODO add slices frame 
			
			GenericToken npHead = npm.getPhrase().get(npm.getPhrase().size()-1);
			String npHeadST = words.get(npHead.getPosition()).getSemanticType();
			
			// Frame edges
			// f_linear_uom needs to exist before f_disease_quantity, measuring
			if(npHeadST.equalsIgnoreCase("qlco-uom")) {
				// f_linear_uom frame
				if(words.get(npHead.getPosition()-1).getPOS().equalsIgnoreCase("CD")) {
					edges.add(new Edge(GraphClass.f_linear_uom, vertices.get(npHead.getPosition()-1), vertices.get(npHead.getPosition())));
				}
			}
			
			for(int i=0; i < npm.getPhrase().size(); i++) {
				int idx = npm.getPhrase().get(i).getPosition();
				
				if(npm.isNegated()) {
					vertices.get(idx).getProps().put(GraphProperty.negated.toString(), true);
				}
				
				if(i > 0) {
					// edges between each np member
					edges.add(new Edge(GraphClass.has_np_sibling, vertices.get(idx-1), vertices.get(idx)));
				}
				
				if(i < npm.getPhrase().size()-1) {
					// edges between each np member and the np head
					edges.add(new Edge(GraphClass.has_np_object, vertices.get(idx), vertices.get(npHead.getPosition())));
				}
				
				// Frame edges
				WordToken npMod = words.get(idx);
				
				if(npHeadST.matches("bpoc|neop.*") && npMod.getToken().matches("left|right|bilateral")) {
					// f_laterality frame
					Edge e = new Edge(GraphClass.f_laterality, vertices.get(idx), vertices.get(npHead.getPosition()));
					if(!edges.contains(e)) {
						edges.add(e);
					}
				} else if(npHeadST.matches("dysn|neop.*")) {
					if(npMod.getSemanticType().equalsIgnoreCase("bpoc")) {
						// f_finding_site frame
						edges.add(new Edge(GraphClass.f_finding_site, vertices.get(idx), vertices.get(npHead.getPosition())));
					} else if(npMod.getPOS().equalsIgnoreCase("CD")) {
						// f_disease_quantity frame
						// additional check for npMod not in f_linear_uom relationship
						if(findEdge(edges, EdgeDirection.outgoing, GraphClass.f_linear_uom, vertices.get(idx)) == -1) {
							edges.add(new Edge(GraphClass.f_disease_quantity, vertices.get(idx), vertices.get(npHead.getPosition())));
						}
					}
				}
				
				// 10/10/2016 - dynamic NP frames
				List<String> dynEdges = qlco.get(npMod.getToken());
				if(dynEdges != null) {
					for(String edge : dynEdges) {
						edges.add(new Edge(edge, vertices.get(npHead.getPosition()), vertices.get(idx)));
					}
				}
			}
			
			for(Integer ppIdx : npm.getPrepPhrasesIdx()) {
				// get the position of the FIRST token of the PP 
				// TODO just put the freakin idx/position in the metadata!
				
				int ppPos = metadata.getPrepMetadata().get(ppIdx).getPhrase().get(0).getPosition();

				edges.add(new Edge(GraphClass.has_pp_modifier, vertices.get(npHead.getPosition()), vertices.get(ppPos)));
			}
		}
	}
	
	private void processDependentPhrases(List<Edge> edges, List<Vertex> vertices, SentenceMetadata metadata) {
		for(DependentPhraseMetadata dpm : metadata.getDependentMetadata()) {
			// edges between each dp member
			for(int i=1; i < dpm.getPhrase().size(); i++) {
				int idx = dpm.getPhrase().get(i).getPosition();
				edges.add(new Edge(GraphClass.has_dp_sibling, vertices.get(idx-1), vertices.get(idx)));
			}
		}
	}
	
	private int findEdge(List<Edge> edges, EdgeDirection dir, GraphClass edgeClass, Vertex vertex) {
		int ret = -1;
		
		for(int i=0; i < edges.size(); i++) {
			Edge e = edges.get(i);
		
			String uuid = "";
			
			switch(dir) {
				case incoming:
					uuid = e.getToVertex();
					break;
					
				case outgoing:
					uuid = e.getFromVertex();
					break;
			}
			
			if(uuid.equals(vertex.getUUID())) {
				if(e.get_Class().equalsIgnoreCase(edgeClass.toString())) {
					ret = i;
					break;
				}
			}
		}
		
		return ret;
	}
	
	private void createVerbEdges(VerbPhraseToken token, int vbPos, GraphClass edge, List<Vertex> vertices, List<Edge> edges, SentenceMetadata metadata) {
		// relate token to the vbPos, e.g. subj -> first vb token; subjc -> last vb token
		if(edge != null)
			edges.add(new Edge(edge, vertices.get(vbPos), vertices.get(token.getPosition())));
	
		// relate prep phrases to the token
		if(!token.getPrepPhrasesIdx().isEmpty()) {
			// TODO there should probably be a change to metadata where multiple prep phrases do not ALL modify the subjc
			// but instead modify each other.
			for(Integer ppIdx : token.getPrepPhrasesIdx()) {
				// get the position of the FIRST token of the PP 
				// TODO just put the freakin idx/position in the metadata!
				int ppPos = metadata.getPrepMetadata().get(ppIdx).getPhrase().get(0).getPosition();

				// use incoming GraphClass to determine modifying PP edge
				if(edge == null)
					edge = GraphClass.has_vb_pp_modifier;
				else if(edge == GraphClass.has_subj)
					edge = GraphClass.has_subj_pp_modifier;
				else if(edge == GraphClass.has_subjc)
					edge = GraphClass.has_subjc_pp_modifier;
				
				edges.add(new Edge(edge, vertices.get(token.getPosition()), vertices.get(ppPos)));
			}
		}
		
		// DPs; create an edge between the vb token and the first token of the DP
		//if(token.getDepPhraseIdx() != -1) {
		//	DependentPhraseMetadata dpm = metadata.getDependentMetadata().get(token.getDepPhraseIdx());
		//	int dpStartPos = dpm.getPhrase().get(0).getPosition();
		//	edges.add(new Edge(GraphClass.within_dp, vertices.get(token.getPosition()), vertices.get(dpStartPos)));
		//}
		
		for(int modPos : token.getModifierList()) {
			edges.add(new Edge(GraphClass.mod_by, vertices.get(token.getPosition()), vertices.get(modPos)));
		}
		
		if(token.getNounPhraseIdx() != -1) {
			NounPhraseMetadata npm = metadata.getNounMetadata().get(token.getNounPhraseIdx());
			int npStartPos = npm.getPhrase().get(0).getPosition();
			edges.add(new Edge(GraphClass.within_np, vertices.get(token.getPosition()), vertices.get(npStartPos)));
		}
		
		// negation
		if(token.isNegated()) {
			vertices.get(token.getPosition()).getProps().put(GraphProperty.negated.toString(), true);
		}
	}
	
	private String md5(String string) {
		String ret = string;
	  
		try {
			md5Generator.reset();
			md5Generator.update(string.getBytes("UTF-8"));
			BigInteger bi = new BigInteger(1, md5Generator.digest());
			ret = bi.toString(16);
		} catch(Exception e) {
			e.printStackTrace();
		}
		  
		return ret;
	}
	
	private List<String> getTokenBooleans(WordToken word) {
		List<String> out = new ArrayList<>();

		if(word.isNounPhraseHead())
			out.add("npHead");
		if(word.isNounPhraseModifier())
			out.add("npMod");
		if(word.isPrepPhraseMember())
			out.add("ppMember");
		if(word.isPrepPhraseBegin())
			out.add("ppBegin");
		if(word.isPrepPhraseObject())
			out.add("ppObj");
		if(word.isInfinitiveHead())
			out.add("infHead");
		if(word.isInfinitiveVerb())
			out.add("inf");
		if(word.isVerbOfBeing())
			out.add("vob");
		if(word.isVerbOfBeingSubject())
			out.add("vobSubj");
		if(word.isVerbOfBeingSubjectComplement())
			out.add("vobSubjC");
		if(word.isLinkingVerb())
			out.add("lv");
		if(word.isLinkingVerbSubject())
			out.add("lvSubj");
		if(word.isLinkingVerbSubjectComplement())
			out.add("lvSubjC");
		if(word.isActionVerb())
			out.add("av");
		if(word.isActionVerbSubject())
			out.add("avSubj");
		if(word.isActionVerbDirectObject())
			out.add("avSubjC");
		if(word.isPrepositionalVerb())
			out.add("prepVerb");
		if(word.isModalAuxVerb())
			out.add("mv");
		if(word.isModalSubject())
			out.add("mvSubj");
		if(word.isModalSubjectComplement())
			out.add("mvSubjC");
		if(word.isDependentPhraseBegin())
			out.add("dpBegin");
		if(word.isDependentPhraseMember())
			out.add("dpMember");
		if(word.isDependentPhraseEnd())
			out.add("dpEnd");
		if(word.isCorefernece())
			out.add("coref");
		if(word.isConjunctiveAdverb())
			out.add("conjAdv");
		
		return out;
	}
	
	/*
	public List<GraphEntry> processSentence(Sentence s) {
		List<GraphEntry> graph = new ArrayList<>();
		// this list of vertices will be accessed to build GraphEntries while going through the metadata
		List<Vertex> vertices = new ArrayList<>();
		
		Vertex oldV = null;
		Vertex newV = null;
		
		for(WordToken word : s.getWordList()) {
			newV = new Vertex(GraphClass.Token, word.getToken().toLowerCase(), word.getPosition());
			
			Map<String, Object> props = new HashMap<>();
			
			props.put(PROP_POS, word.getPOS());
			//props.put("lvSubj", true); TODO how to figure these out?
			
			newV.setProps(props);
			
			vertices.add(newV);
			
			if(oldV != null) {
				graph.add(new GraphEntry(oldV, newV, new Edge(GraphClass.precedes)));
			}
			
			oldV = newV;
		}
		
		SentenceMetadata metadata = s.getMetadata();
		
		// do other metadata processing
		for(VerbPhraseMetadata vpm : metadata.getVerbMetadata()) {
			// relate the first verb of the phrase to all of the subjects
			for(VerbPhraseToken subj : vpm.getSubjects()) {
				graph.add(new GraphEntry(vertices.get(vpm.getVerbs().get(0).getPosition()), 
						                 vertices.get(subj.getPosition()), 
						                 new Edge(GraphClass.has_subj)));
			}
			// relate each subject complement to the final token of the verb phrase
			for(VerbPhraseToken subjc : vpm.getSubjC()) {
				int vbPos = vpm.getVerbs().get(vpm.getVerbs().size()-1).getPosition();
				graph.add(new GraphEntry(vertices.get(vbPos), 
						                 vertices.get(subjc.getPosition()), 
						                 new Edge(GraphClass.has_subjc)));
				
				// TODO relate prep phrases to the subj
				
				// relate prep phrases to the subjc
				if(!subjc.getPrepPhrasesIdx().isEmpty()) {
					// TODO there should probably be a change to metadata where multiple prep phrases do not ALL modify the subjc
					// but instead modify each other.
					for(Integer ppIdx : subjc.getPrepPhrasesIdx()) {
						// get the position of the FIRST token of the PP 
						// TODO just put the freakin idx/position in the metadata!
						int ppPos = metadata.getPrepMetadata().get(ppIdx).getPhrase().get(0).getPosition();
						graph.add(new GraphEntry(vertices.get(vbPos), 
				                 				 vertices.get(ppPos), 
				                 				 new Edge(GraphClass.has_pp_modifier)));
					}
				}
			}
			
			// intra-prep phrase relationships
			for(PrepPhraseMetadata ppm : metadata.getPrepMetadata()) {
				// prep term and object relationship
				int ppIdx = ppm.getPhrase().get(0).getPosition();
				int objIdx = ppm.getPhrase().get(ppm.getPhrase().size()-1).getPosition();
				graph.add(new GraphEntry(vertices.get(ppIdx), 
        				 vertices.get(objIdx), 
        				 new Edge(GraphClass.has_pp_object)));
			}
		}
		
		
		
		return graph;
	}
	*/
}
