package com.mst.tools;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.mst.model.DependentPhraseMetadata;
import com.mst.model.GenericToken;
import com.mst.model.NounPhraseMetadata;
import com.mst.model.PrepPhraseMetadata;
import com.mst.model.PrepPhraseToken;
import com.mst.model.SentenceMetadata;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.VerbPhraseToken;
import com.mst.model.gentwo.Sentence;
import com.mst.model.gentwo.WordToken;
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
	
	private final String PLACEHOLDER = "{placeholder}";
	private final Pattern LOCATION_PREP = Pattern.compile("in|of|within|at");
	private final Pattern LATERALITY = Pattern.compile("left|right|bilateral"); // can't yet trust the 'latr' ST because of 'left'
	private final Pattern POPULATION = Pattern.compile("children|adults|patients|child|people|childhood");
	private final Pattern SPACIAL = Pattern.compile("below|above|behind|near|under|over");
	private final Pattern PERIOD_TIME = Pattern.compile("before|after|since");
	private final Pattern DOUBT = Pattern.compile("probable|probably|possibly");
	
	private final String PROP_EDGE_TYPE = "type";
	private final String PROP_EDGE_STATE = "state";
	private final String PROP_NEGATED = "negated";
	private final String PROP_VB_TENSE = "vb_tense";
	private final String PROP_VB_ST_NULL = "vb_st_null";
	private final String STATE_UNKNOWN = "unknown";
	private final String STATE_PRESENCE = "presence";
	private final String STATE_ABSENCE = "absence";
	private final String EDGE_UNKNOWN = "f_modifier";
	private final String EDGE_DOUBT = "f_probable";  // TODO this will be changed to an ontology lookup
	private final String VB_INDEX_PROP = "objectId";
	private final int VB_INDEX_PROP_DEFAULT = -1; 
	
	public enum EdgeDirection {
		incoming, outgoing;
	}
	
	private Map<String, Map<String, String>> verbLookup = new HashMap<>();
	
	public GraphProcessor() { 
		//verbLookup.put("suggests", ImmutableMap.of("has_synonym", "demonstrate"));
		//verbLookup.put("demonstrate", ImmutableMap.of("has_meaning", "presence"));
		//verbLookup.put("demonstrates", ImmutableMap.of("has_meaning", "presence"));
		verbLookup.put("suggest", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("suggests", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("demonstrate", ImmutableMap.of("has_meaning","presence"));
		verbLookup.put("demonstrates", ImmutableMap.of("has_meaning","presence"));
		verbLookup.put("reveal", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("reveals", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("confirm", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("confirms", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("affirm", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("affirms", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("correlate", ImmutableMap.of("has_meaning","presence"));
		verbLookup.put("correlates", ImmutableMap.of("has_meaning","presence"));
		verbLookup.put("measure", ImmutableMap.of("has_meaning","measure"));
		verbLookup.put("measures", ImmutableMap.of("has_meaning","measure"));
		verbLookup.put("exclude", ImmutableMap.of("has_meaning","absence"));
		verbLookup.put("excludes", ImmutableMap.of("has_meaning","absence"));
		verbLookup.put("represent", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("represents", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("perform", ImmutableMap.of("has_meaning","perform"));
		verbLookup.put("performed", ImmutableMap.of("has_meaning","perform"));
		verbLookup.put("register", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("registers", ImmutableMap.of("has_synonym","demonstrate"));
		verbLookup.put("appear", ImmutableMap.of("has_meaning","appear"));
		verbLookup.put("appears", ImmutableMap.of("has_meaning","appear"));
		
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
		Set<Edge> edges = new HashSet<>();
		
		try {
			// create a vertex for each token
			for(WordToken word : s.getModifiedWordList()) {
				String uuid = md5(UUID.randomUUID().toString()); // TODO get hash from somewhere else (mongo oid + position?)
				Vertex v = new Vertex(GraphClass.Token, uuid, word.getToken().toLowerCase(), word.getPosition());
				
				Map<String, Object> props = new HashMap<>();
				
				props.put(GraphProperty.pos.toString().toLowerCase(), word.getPos());
				if(word.getSemanticType() != null) {
					props.put(GraphProperty.st.toString().toLowerCase(), word.getSemanticType());
				}
				
				// low-level boolean values from the WordToken object.
				List<String> booleans = getTokenBooleans(word);
				for(String bool : booleans) {
					props.put(bool, true);
				}
				
				v.setProps(props);
				
				vertices.add(v);
			}
	
			String sentenceUUID = UUID.randomUUID().toString();
			
			Vertex vSource = new Vertex(GraphClass.Source, md5(s.getSource()), s.getSource(), 0);
			Vertex vPractice = new Vertex(GraphClass.Practice, md5(s.getPractice()), s.getPractice(), 0);
			Vertex vStudy = new Vertex(GraphClass.Study, md5(s.getPractice()+s.getStudy()), s.getStudy(), 0);
			Vertex vID = new Vertex(GraphClass.ID, md5(s.getPractice()+s.getStudy()+s.getId()), s.getId(), 0);
			String date = orientSDF.format(s.getProcedureDate());
			Vertex vProcedureDate = new Vertex(GraphClass.Date, md5(s.getPractice()+s.getStudy()+date.toString()), date.toString(), 0);
			Vertex vSentence = new Vertex(GraphClass.Sentence, sentenceUUID, s.getFullSentence(), (int) s.getPosition());
			Vertex vDiscrete = new Vertex(GraphClass.Discrete, md5(sentenceUUID + s.getId()), "", (int) 0);
			// add a vertex to act as a placeholder for verbs that are missing a subj or subjc 
			Vertex vPlaceholder = new Vertex(GraphClass.Token, md5(sentenceUUID + s.getModifiedWordList().size()), PLACEHOLDER, 0);
			
			// create properties for all simple metadata on Sentence vertex  
			for(String key : s.getMetadata().getSimpleMetadata().keySet()) {
				vSentence.getProps().put(key, s.getMetadata().getSimpleMetadata().get(key));
			}
					
			// sentence edges
			// new way - sentence links to everything
			edges.add(new Edge(GraphClass.has_source, vSentence, vSource));
			edges.add(new Edge(GraphClass.has_practice, vSentence, vPractice));
			edges.add(new Edge(GraphClass.has_study, vSentence, vStudy));
			edges.add(new Edge(GraphClass.has_id, vSentence, vID));
			edges.add(new Edge(GraphClass.has_date, vSentence, vProcedureDate));
			
			Vertex prevV = null;
			
			// link sentence to individual tokens via edges with idx property
			for(Vertex v : vertices) {
				Edge e = new Edge(GraphClass.has_token, vSentence, v);
				e.getProps().put("idx", v.getIdx());
				edges.add(e);
				
				if(prevV != null) {
					Edge e2 = new Edge(GraphClass.precedes, prevV, v);
					e2.getProps().put("idx", v.getIdx()-1);
					edges.add(e2);
				}
				
				prevV = v;
			}
			
			// add "global" vertices to list. Note this must occur after the above vertex loop or you'll add a bunch of junk edges
			vertices.add(vSource);
			vertices.add(vPractice);
			vertices.add(vStudy);
			vertices.add(vID);
			vertices.add(vProcedureDate);
			vertices.add(vSentence);
			vertices.add(vPlaceholder);
			
			SentenceMetadata metadata = s.getMetadata();
			
			vSentence.getProps().put("nounPhraseCount", metadata.getNounMetadata().size());
			vSentence.getProps().put("prepPhraseCount", metadata.getPrepMetadata().size());
			vSentence.getProps().put("verbPhraseCount", metadata.getVerbMetadata().size());
			vSentence.getProps().put("depPhraseCount", metadata.getDependentMetadata().size());
			vSentence.getProps().put("orphanCount", metadata.getOrphans().size());
			
			processVerbPhraseMetadata(edges, vertices, s.getModifiedWordList(), metadata, vPlaceholder);
			
			// this picks up prep phrases that are not related to a verb phrase
			processPrepPhraseMetadata(edges, vertices, s.getModifiedWordList(), metadata);
			
			createDependentPhraseStructureEdges(edges, vertices, metadata);
			
			// TODO move this into its own function
			// Frames unbounded by a known phrase type
			for(int i=0; i < s.getModifiedWordList().size(); i++) {
				WordToken word = s.getModifiedWordList().get(i);
				WordToken prev = Constants.getToken(s.getModifiedWordList(), i-1);
				WordToken next = Constants.getToken(s.getModifiedWordList(), i+1);
				
				if(word.getSemanticType().equalsIgnoreCase("bpoc")) {
					if(LATERALITY.matcher(prev.getToken()).matches()) {
						//edges.add(new Edge(GraphClass.f_laterality, vertices.get(prev.getPosition()-1), vertices.get(word.getPosition()-1)));
						Edge e = new Edge(GraphClass.f_related, vertices.get(prev.getPosition()-1), vertices.get(word.getPosition()-1));
						setDefaultEdgeProps(e, GraphClass.f_laterality.toString(), STATE_UNKNOWN, false, VB_INDEX_PROP_DEFAULT);
						edges.add(e);
					}
				} else if(word.getToken().equalsIgnoreCase(":")) {
					//edges.add(new Edge(GraphClass.f_header, vertices.get(prev.getPosition()-1), vertices.get(word.getPosition()-1)));
					Edge e = new Edge(GraphClass.f_related, vertices.get(prev.getPosition()-1), vertices.get(word.getPosition()-1));
					setDefaultEdgeProps(e, GraphClass.f_header.toString(), STATE_UNKNOWN, false, VB_INDEX_PROP_DEFAULT);
					edges.add(e);
				} else if(word.getToken().matches("measur.*")) {
					if(next.getPos().equalsIgnoreCase("CD") && !prev.isPrepPhraseObject()) {
						//edges.add(new Edge(GraphClass.f_measurement, vertices.get(word.getPosition()-2), vertices.get(next.getPosition()-1)));
						Edge e = new Edge(GraphClass.f_measurement, vertices.get(word.getPosition()-2), vertices.get(next.getPosition()-1));
						setDefaultEdgeProps(e, GraphClass.f_measurement.toString(), STATE_UNKNOWN, false, VB_INDEX_PROP_DEFAULT);
						edges.add(e);
					}
				}
			}
			
			processNounPhraseMetadata(edges, vertices, s.getModifiedWordList(), metadata);
			
			processDiscreteData(edges, vertices, s.getDiscrete(), vID, vDiscrete);
			
			//List<Edge> list = new ArrayList<>(edges);
			//list.sort(Comparator.comparing(e -> e.getId()));
			//list.sort((e1, e2) -> e1.getObjectId().compareTo(e2.getObjectId()));
			
//			Collections.sort(list, new Comparator<Edge>() {
//		        @Override
//		        public int compare(Edge e1, Edge e2) { 
//		        	System.out.println("e1: " + e1.getObjectId() + "; e2: " + e2.getObjectId());
//		            return e1.getObjectId().compareTo(e2.getObjectId());
//		        }
//		    });
			
			//edges = new TreeSet<>(list);
			
			graphSentence.setVertices(vertices);
			graphSentence.setEdges(edges);
			
		} catch(Exception e) {
			System.out.println(s.getFullSentence());
			logger.debug(s.getFullSentence());
			e.printStackTrace();
		}
		
		return graphSentence;
	}
	
	private Edge buildVerbFrameEdge(String name, List<Vertex> vertices, VerbPhraseToken subjc, VerbPhraseToken subj, Vertex placeholder) {
		Edge edge = null;
		
		Vertex from = null;
		Vertex to = null;
		
		if(!name.startsWith("f_"))
			name = "f_" + name;
		
		if(subjc == null || subjc.getToken().equalsIgnoreCase(PLACEHOLDER))
			from = placeholder;
		else
			from = vertices.get(subjc.getPosition());
		
		if(subj == null || subj.getToken().equalsIgnoreCase(PLACEHOLDER))
			to = placeholder;
		else
			to = vertices.get(subj.getPosition());
		
		edge = new Edge(name.toLowerCase(), from, to);
		
		return edge;
	}
	
	private void createVerbPhraseStructureEdges(Set<Edge> edges, List<Vertex> vertices, ArrayList<WordToken> words, SentenceMetadata metadata, Vertex placeholder) {
		
		for(VerbPhraseMetadata vpm : metadata.getVerbMetadata()) {
			
			// relate the first verb of the phrase to all subjects
			for(VerbPhraseToken vpt : vpm.getSubjects()) {
				createVerbEdges(vpt, vpm.getVerbs().get(0).getPosition(), GraphClass.has_subj, vertices, edges, metadata);
				
				// Frame edge - first verb to subj
				//GraphClass gClass = subj.isNegated() || vpm.getVerbs().get(0).isNegated() ? GraphClass.f_absence : GraphClass.f_presence;
				//edges.add(new Edge(gClass, vertices.get(vpm.getVerbs().get(0).getPosition()), vertices.get(subj.getPosition())));
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
				//GraphClass gClass = subjc.isNegated() || lastVerb.isNegated() ? GraphClass.f_absence : GraphClass.f_presence;
				//edges.add(new Edge(gClass, vertices.get(lastVerb.getPosition()), vertices.get(subjc.getPosition())));
			}
		}
	}
	
	private void createVerbPhraseFrameEdgesV2(Set<Edge> edges, List<Vertex> vertices, ArrayList<WordToken> words, SentenceMetadata metadata, Vertex placeholder) {
		
		int vbIndex = 0;
		
		for(VerbPhraseMetadata vpm : metadata.getVerbMetadata()) {
			//boolean modByDoubt = modByDoubt(vpm, words);
			vbIndex++;
			
			VerbInfo vi = new VerbInfo(vpm, words);
			
			processPrepPhrasesModifyingVerbComponents(vpm, edges, vertices, words, metadata, placeholder, vbIndex);
			
			// add placeholder for missing subject
			if(vpm.getSubjects().isEmpty()) {
				vpm.getSubjects().add(new VerbPhraseToken(PLACEHOLDER, 0));
			}
			
			// add placeholder for missing subject complement ONLY if the final verb isn't modified by a prep phrase (and would thus be acting as the subjc).
			if(vpm.getSubjC().isEmpty() && vpm.getVerbs().get(vpm.getVerbs().size()-1).getPrepPhrasesIdx().isEmpty()) {
				vpm.getSubjC().add(new VerbPhraseToken(PLACEHOLDER, 0));
			}
			
			// frames pertaining to the subject complement
			for(VerbPhraseToken subjc : vpm.getSubjC()) {
				String subjcST = words.get(subjc.getPosition()).getSemanticType();
				String frame = null;
				boolean subjcNumeric = words.get(subjc.getPosition()).isNumericPOS();
				
				// https://docs.google.com/spreadsheets/d/1aac5QCAUeiGR67FY9_vYE-ONEQ60oqMuf4XhrxSU77I/edit#gid=180671387
				// 4a
				// TODO this will eventually do an ontology lookup and not just be limited to qlco
				if(vi.infinitive.matches("is|are|was|were|has|have") && subjcST.startsWith("qlco")) {
					frame = subjc.getToken();
					
				// 4b
				} else if(vi.infinitive.matches("is|are|was|were") && subjcNumeric) {			
					frame = GraphClass.f_absolute_value.toString();
					
				// 6
				} else if(vi.infinitive.matches("appear|look|smell") && subjcST.startsWith("qlco")) {
					frame = vi.infinitive;
				}
				
				// now build a frame edge between this subjc and each subj
				for(VerbPhraseToken subj : vpm.getSubjects()) {
					if(frame != null) {
						// kind of a hack
						//if(frame.equalsIgnoreCase("f_presence") && vpm.isPhraseNegated())
						//	frame = "f_absence";
				
						//if(frame.matches("f_presence|f_absence") && modByDoubt) {
						//	frame = EDGE_DOUBT;
						//}
						
						Edge e = buildVerbFrameEdge(GraphClass.f_related.toString(), vertices, subjc, subj, placeholder);
						
						if(e != null) {
							setDefaultEdgeProps(e, frame, vpm.isPhraseNegated() ? STATE_ABSENCE : STATE_PRESENCE, vpm.isPhraseNegated(), vbIndex);
							e.getProps().put(PROP_VB_TENSE, vi.tense);
							if(vi.st.length() == 0)
								e.getProps().put(PROP_VB_ST_NULL, vpm.getVerbString());
							edges.add(e);
						}
					}
				}
				
				// process noun phrases related to the subjc
				if(subjc.getNounPhraseIdx() != -1) {
					createNounPhraseFrameEdgesV2(edges, vertices, words, metadata.getNounMetadata().get(subjc.getNounPhraseIdx()), vbIndex);
				}
			}
			
			// TODO subj = there, vb = is, subjc = PP; what two edges to link?
			
			// token following final verb
			WordToken vbPlusOne = Constants.getToken(words, vpm.getVerbs().get(vpm.getVerbs().size()-1).getPosition()+1);
			
			String vbOntEdge = getOntologyVerbMeaning(vi.infinitive);
			
			// frames pertaining to the subject 
			for(VerbPhraseToken subj : vpm.getSubjects()) {				
				String frame = null;
				
				// https://docs.google.com/spreadsheets/d/1aac5QCAUeiGR67FY9_vYE-ONEQ60oqMuf4XhrxSU77I/edit#gid=180671387
				// 1 on google sheet ("there is a...")
				if(subj.getToken().equalsIgnoreCase("there")
					&& vi.infinitive.equalsIgnoreCase("is|was") 
					&& vbPlusOne.getToken().equalsIgnoreCase("a")) {

					frame = vpm.isPhraseNegated() ? GraphClass.f_absence.toString() : GraphClass.f_presence.toString();
					
				// 2a, 2b, 2c ("there is fluid..." or "<any> has|have <any>")    not making any distinction for "evidence"
				// 5a 
				} else if((subj.getToken().equalsIgnoreCase("there") 
						&& vi.infinitive.matches("is|are|was|were"))  
						|| vi.infinitive.matches("has|have")) {
					frame = vpm.isPhraseNegated() ? GraphClass.f_absence.toString() : GraphClass.f_presence.toString();

				/* this frame logic that doesn't explicitly use either the subj or subjc value could exist in this loop or the subjc loop above */
				// 9a and 9b
				} else if(vi.isCompound && vpm.getVerbs().get(0).getToken().matches("is|are|was|were") && vi.infinitive.matches("identify|visualize|see")) {
					frame = vpm.isPhraseNegated() ? GraphClass.f_absence.toString() : GraphClass.f_presence.toString();
				
				// 3 ("Fido is a dog")
				} else if(vi.infinitive.matches("is|was") 
					   && vbPlusOne.getToken().equalsIgnoreCase("a")) {

					frame = GraphClass.f_is_a.toString();
				
				// verb ontology lookup
				} else if(vbOntEdge != null) {
					frame = vbOntEdge;		
				} else {
					// default the edge name to the current verb token
					frame = vi.infinitive;
				}
				/* ----- */
				
				for(VerbPhraseToken subjc : vpm.getSubjC()) {
					if(frame != null) {
						// kind of a hack
						//if(frame.equalsIgnoreCase("f_presence") && vpm.isPhraseNegated())
						//	frame = "f_absence";
						
						//if(frame.matches("f_presence|f_absence") && modByDoubt) {
						//	frame = EDGE_DOUBT;
						//}
						
						Edge e = buildVerbFrameEdge(GraphClass.f_related.toString(), vertices, subjc, subj, placeholder);
						
						if(e != null) {
							setDefaultEdgeProps(e, frame, vpm.isPhraseNegated() ? STATE_ABSENCE : STATE_PRESENCE, vpm.isPhraseNegated(), vbIndex);
							e.getProps().put(PROP_VB_TENSE, vi.tense);
							if(vi.st.length() == 0)
								e.getProps().put(PROP_VB_ST_NULL, vpm.getVerbString());
							edges.add(e);
						}
					}
				}
				
				// process noun phrases related to the subj
				if(subj.getNounPhraseIdx() != -1) {
					createNounPhraseFrameEdgesV2(edges, vertices, words, metadata.getNounMetadata().get(subj.getNounPhraseIdx()), vbIndex);
				}
			}
		}
	}
	
	private void processPrepPhrasesModifyingVerbComponents(VerbPhraseMetadata vpm, Set<Edge> edges, List<Vertex> vertices, ArrayList<WordToken> words, SentenceMetadata metadata, Vertex placeholder, int vbIndex) {
		
		List<PrepPhraseMetadata> ppm = metadata.getPrepMetadata();
		
		// process prep phrases modifying each subject
		for(VerbPhraseToken subj : vpm.getSubjects()) {
			// If a subject has no modifying prep phrases, check for prep phrases modifying the *final* subject.
			// What are the pitfalls of this?
			// Examples:
			//   "The man and woman in the garden are married."  -- relate garden to both man and woman
			//   "The man in the garden and woman in the house are married."  -- relate garden to the man and house to the woman
			//   "The man in the front room of the house and the woman in the garden are married."  -- relate room/house to the man and garden to the woman.
			
			// TODO how about PP chaining? Should house be related to room or man or both?
			List<Integer> modifyingPrepPhrases = subj.getPrepPhrasesIdx(); 
			
			if(modifyingPrepPhrases.isEmpty() && vpm.getSubjects().size() > 1)
				modifyingPrepPhrases = vpm.getSubjects().get(vpm.getSubjects().size()-1).getPrepPhrasesIdx();
			
			int prevPrepIdx = -1;
			
			for(int ppIdx : modifyingPrepPhrases) {
				// link each subject with each PP -- TODO how to handle chained prep phrases?
				createPrepPhraseFrameEdgesV2(edges, vertices, words, ppm.get(ppIdx), vertices.get(subj.getPosition()), vbIndex, metadata);
				
				// The idea here is to link the objects of each prep phrase to each other,
				// while still maintaining the vbIndex to which they are related.
				// If this occurs in processPrepPhraseMetadata() the vbIndex is lost.
				if(prevPrepIdx != -1) {
					int prevPrepObjIdx = ppm.get(prevPrepIdx).getPhrase().get(ppm.get(prevPrepIdx).getPhrase().size()-1).getPosition();
					createPrepPhraseFrameEdgesV2(edges, vertices, words, ppm.get(ppIdx), vertices.get(prevPrepObjIdx), vbIndex, metadata);
				}
				prevPrepIdx = ppIdx;
			}				
		}
		
		// process prep phrases modifying the final verb of the phrase (probably acting as the SUBJC)
		int prevPrepIdx = -1;
		
		for(int ppIdx=0; ppIdx < vpm.getVerbs().get(vpm.getVerbs().size()-1).getPrepPhrasesIdx().size(); ppIdx++) {
			if(vpm.getSubjects().isEmpty()) {
				// no subjects, use placeholder
				createPrepPhraseFrameEdgesV2(edges, vertices, words, ppm.get(ppIdx), placeholder, vbIndex, metadata);
			} else {
				for(VerbPhraseToken subj : vpm.getSubjects()) {
					// link each subject with each PP -- TODO how to handle chained prep phrases?
					createPrepPhraseFrameEdgesV2(edges, vertices, words, ppm.get(ppIdx), vertices.get(subj.getPosition()), vbIndex, metadata);
				}
			}
			
			// same comment as above in the vpm.getSubjects() loop
			if(prevPrepIdx != -1) {
				int prevPrepObjIdx = ppm.get(prevPrepIdx).getPhrase().get(ppm.get(prevPrepIdx).getPhrase().size()-1).getPosition();
				createPrepPhraseFrameEdgesV2(edges, vertices, words, ppm.get(ppIdx), vertices.get(prevPrepObjIdx), vbIndex, metadata);
			}
			prevPrepIdx = ppIdx;
		}
		
		// process prep phrases modifying each subject complement
		//// relate each subject complement to the final token of the verb phrase
		for(VerbPhraseToken subjc : vpm.getSubjC()) {
			prevPrepIdx = -1;
			
			for(int ppIdx : subjc.getPrepPhrasesIdx()) {
				createPrepPhraseFrameEdgesV2(edges, vertices, words, ppm.get(ppIdx), vertices.get(subjc.getPosition()), vbIndex, metadata);
				
				// same comment as above in the vpm.getSubjects() loop
				if(prevPrepIdx != -1) {
					int prevPrepObjIdx = ppm.get(prevPrepIdx).getPhrase().get(ppm.get(prevPrepIdx).getPhrase().size()-1).getPosition();
					createPrepPhraseFrameEdgesV2(edges, vertices, words, ppm.get(ppIdx), vertices.get(prevPrepObjIdx), vbIndex, metadata);
				}
				prevPrepIdx = ppIdx;
			}
		}
	}
	
	private boolean modByDoubt(VerbPhraseMetadata vpm, List<WordToken> words) {
		boolean ret = false;
		/*
		// incoming token is modified by a token that casts doubt
		for(VerbPhraseToken vpt : vpm.getSubjects()) {
			for(Integer idx : vpt.getModifierList()) {
				if(DOUBT.matcher(words.get(idx).getToken()).matches()) {
					ret = true;
					break;
				}
			}
		}
		
		if(ret == false) {
			for(VerbPhraseToken vpt : vpm.getSubjC()) {
				for(Integer idx : vpt.getModifierList()) {
					if(DOUBT.matcher(words.get(idx).getToken()).matches()) {
						ret = true;
						break;
					}
				}
			}
		}
		
		if(ret == false) {
			for(VerbPhraseToken vpt : vpm.getVerbs()) {
				for(Integer idx : vpt.getModifierList()) {
					if(DOUBT.matcher(words.get(idx).getToken()).matches()) {
						ret = true;
						break;
					}
				}
			}
		}
		*/
		return ret;
	}
	
	private String getOntologyVerbMeaning(String verb) {
		String ret = null;
		
		Map<String, String> ont = verbLookup.get(verb);
		
		if(ont != null) {
			String meaning = ont.get("has_meaning");
			if(meaning != null)
				ret = "f_" + meaning;
			else {
				String synonym = ont.get("has_synonym");
				ret = getOntologyVerbMeaning(synonym);
			}
		}
		
		if(ret == null)
			logger.warn("Verb has no 'has_meaning' defined: " + verb);
		
		return ret;
	}
	
	private void processVerbPhraseMetadata(Set<Edge> edges, List<Vertex> vertices, ArrayList<WordToken> words, SentenceMetadata metadata, Vertex placeholder) {
		createVerbPhraseStructureEdges(edges, vertices, words, metadata, placeholder);
		createVerbPhraseFrameEdgesV2(edges, vertices, words, metadata, placeholder);
	}
	
	private void processNounPhraseMetadata(Set<Edge> edges, List<Vertex> vertices, ArrayList<WordToken> words, SentenceMetadata metadata) {
		createNounPhraseStructureEdges(edges, vertices, words, metadata);
		
		for(NounPhraseMetadata npm : metadata.getNounMetadata()) {
			createNounPhraseFrameEdgesV2(edges, vertices, words, npm, VB_INDEX_PROP_DEFAULT);
		}
	}
	
	private void processPrepPhraseMetadata(Set<Edge> edges, List<Vertex> vertices, List<WordToken> words, SentenceMetadata metadata) {
		List<PrepPhraseMetadata> prepPhrases = metadata.getPrepMetadata();
		
		createPrepPhraseStructureEdges(edges, vertices, words, prepPhrases);
		
		for(PrepPhraseMetadata ppm : prepPhrases) {
			createPrepPhraseFrameEdgesV2(edges, vertices, words, ppm, null, VB_INDEX_PROP_DEFAULT, metadata);
		}
	}
	
	private void createNounPhraseStructureEdges(Set<Edge> edges, List<Vertex> vertices, List<WordToken> words, SentenceMetadata metadata) {
		for(NounPhraseMetadata npm : metadata.getNounMetadata()) {		
			GenericToken npHead = npm.getPhrase().get(npm.getPhrase().size()-1);
			
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
			}
			
			for(Integer ppIdx : npm.getPrepPhrasesIdx()) {
				// get the position of the FIRST token of the PP 
				// TODO just put the freakin idx/position in the metadata!
				
				int ppPos = metadata.getPrepMetadata().get(ppIdx).getPhrase().get(0).getPosition();

				edges.add(new Edge(GraphClass.has_pp_modifier, vertices.get(npHead.getPosition()), vertices.get(ppPos)));
			}
		}
	}
	
	private void createNounPhraseFrameEdgesV2(Set<Edge> edges, List<Vertex> vertices, List<WordToken> words, NounPhraseMetadata npm, int vbIndex) {
		//for(NounPhraseMetadata npm : metadata.getNounMetadata()) {
			// TODO add slices frame 
			
			GenericToken npHead = npm.getPhrase().get(npm.getPhrase().size()-1);
			String npHeadST = words.get(npHead.getPosition()).getSemanticType();
			Set<Integer> processedNPindexes = new HashSet<>();
			
			// Frame edges
			// f_linear_uom needs to exist before f_disease_quantity, measuring
//			if(npHeadST.equalsIgnoreCase("qlco-uom")) {
//				// f_linear_uom frame
//				if(words.get(npHead.getPosition()-1).getPOS().equalsIgnoreCase("CD")) {
//					Edge e = new Edge(GraphClass.f_related, vertices.get(npHead.getPosition()-1), vertices.get(npHead.getPosition()));
//					e.getProps().put(EDGE_TYPE, "f_linear_uom");
//					e.getProps().put(EDGE_STATE, STATE_UNKNOWN);
//					edges.add(e);
//					processedNPindexes.add(npHead.getPosition());
//					processedNPindexes.add(npHead.getPosition()-1);
//				}
//			}
			
			for(int i=0; i < npm.getPhrase().size()-1; i++) {
				String frame = null;
				int idx = npm.getPhrase().get(i).getPosition();
				
				// Frame edges
				WordToken npMod = words.get(idx);
				WordToken nextToken = Constants.getToken(new ArrayList(words), idx+1);

				// from/to default to npMod and npHead
				int fromIdx = idx;
				int toIdx = npHead.getPosition(); 
				
				if(npMod.getPos().equalsIgnoreCase("CD") && nextToken.getSemanticType().equalsIgnoreCase("qlco-uom")) {
					frame = "f_linear_uom";
					toIdx = fromIdx+1;
				} else if(npMod.getSemanticType().equalsIgnoreCase("qlco-uom")) {
						frame = "f_size";
				} else if(npHeadST.matches("bpoc|neop.*") && LATERALITY.matcher(npMod.getToken()).matches()) {
					frame = "f_laterality";
				} else if(npHeadST.matches("dysn|neop.*")) {
					if(npMod.getSemanticType().equalsIgnoreCase("bpoc")) {
						frame = "f_location";
					} else if(npMod.getPos().equalsIgnoreCase("CD")) {
						// additional check for npMod not in f_linear_uom relationship
						if(findEdgeByFrame(edges, EdgeDirection.outgoing, GraphClass.f_linear_uom, vertices.get(idx)) == -1) {
							frame = "f_disease_quantity"; 
						}
					}
				}
				
				if(frame != null ) {					
					Edge e = new Edge(GraphClass.f_related, vertices.get(fromIdx), vertices.get(toIdx));
					setDefaultEdgeProps(e, frame, STATE_UNKNOWN, npm.isNegated(), vbIndex);
					edges.add(e);
					
					processedNPindexes.add(idx);
				}
				
				// 10/10/2016 - dynamic NP qlco frames
				// TODO is this really working as desired?
				List<String> dynFrames = qlco.get(npMod.getToken());
				if(dynFrames != null) {
					for(String f : dynFrames) {
						Edge e = new Edge(GraphClass.f_related, vertices.get(npHead.getPosition()), vertices.get(idx));
						setDefaultEdgeProps(e, f, STATE_UNKNOWN, npm.isNegated(), vbIndex);
						edges.add(e);
						
						processedNPindexes.add(idx);
					}
				}
			}
			
			// create f_unknown frames
			for(int i=0; i < npm.getPhrase().size()-1; i++) {
				GenericToken npToken = npm.getPhrase().get(i);
				if(!processedNPindexes.contains(npToken.getPosition())) {
					Edge e = new Edge(GraphClass.f_related, vertices.get(npHead.getPosition()), vertices.get(npToken.getPosition()));
					setDefaultEdgeProps(e, EDGE_UNKNOWN, STATE_UNKNOWN, npm.isNegated(), vbIndex);
					edges.add(e);
				}
			}
		//}
	}
	
	private void setDefaultEdgeProps(Edge e, String edgeType, String edgeState, boolean negated, int vbIndex) {

		if(!edgeType.startsWith("f_"))
			edgeType = "f_" + edgeType;
				
		e.getProps().put(PROP_EDGE_TYPE, edgeType);
		e.getProps().put(PROP_EDGE_STATE, edgeState);
		e.getProps().put(PROP_NEGATED, negated);
		e.getProps().put(VB_INDEX_PROP, vbIndex);
	}
	
	private void createDependentPhraseStructureEdges(Set<Edge> edges, List<Vertex> vertices, SentenceMetadata metadata) {
		for(DependentPhraseMetadata dpm : metadata.getDependentMetadata()) {
			// edges between each dp member
			for(int i=1; i < dpm.getPhrase().size(); i++) {
				int idx = dpm.getPhrase().get(i).getPosition();
				edges.add(new Edge(GraphClass.has_dp_sibling, vertices.get(idx-1), vertices.get(idx)));
			}
		}
	}
	
	private void createPrepPhraseStructureEdges(Set<Edge> edges, List<Vertex> vertices, List<WordToken> words, List<PrepPhraseMetadata> metadata) {
		// intra-prep phrase relationships
		for(PrepPhraseMetadata ppm : metadata) {
			// prep term
			int prepIdx = ppm.getPhrase().get(0).getPosition();
			int precedingToken = prepIdx-1;
			
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
				
				i++;
			}
		}
	}
	
	private void createPrepPhraseFrameEdgesV2(Set<Edge> edges, List<Vertex> vertices, List<WordToken> words, PrepPhraseMetadata ppm, Vertex modifiedToken, int vbIndex, SentenceMetadata metadata) {
		int prepIdx = ppm.getPhrase().get(0).getPosition();
		String prep = words.get(prepIdx).getToken().toLowerCase(); // TODO add the token value to the metadata to make this cleaner
		// default the from index to the token preceding the preposition
		int fromIdx = modifiedToken == null ? prepIdx-1 : -1;
				
		for(PrepPhraseToken ppt : ppm.getPhrase()) {
			String frame = null;
			int toIdx = ppt.getPosition();
			
			if(ppt.isObject()) {
				WordToken obj = words.get(toIdx);
									
				if(prep.equalsIgnoreCase("in") && POPULATION.matcher(ppt.getToken()).matches()) {
					frame = "f_population";
				} else if(prep.equalsIgnoreCase("in") && obj.getSemanticType().equalsIgnoreCase("tempor-month")) {
					frame = "f_month";
				} else if(prep.equalsIgnoreCase("for") && obj.getSemanticType().equalsIgnoreCase("tempor")) {
					frame = "f_duration";
				} else if(prep.equalsIgnoreCase("in") && obj.getSemanticType().equalsIgnoreCase("tempor")) {
					frame = "f_future_event";
				} else if(prep.equalsIgnoreCase("within") && obj.getSemanticType().equalsIgnoreCase("tempor")) {
					frame = "f_within_time";
				} else if(prep.equalsIgnoreCase("on") && obj.getSemanticType().startsWith("drug")) {
					frame = "f_take";
				} else if(prep.equalsIgnoreCase("at") && obj.getToken().equalsIgnoreCase("noon")) {
					frame = "f_time";
					fromIdx = prepIdx;
				} else if(LOCATION_PREP.matcher(prep).matches()) {
					frame = GraphClass.f_location.toString();
				} else if(SPACIAL.matcher(prep).matches()) {
					frame = "f_spacial_relationship";
				} else if(PERIOD_TIME.matcher(prep).matches()) {
					frame = "f_period_time";
				} else {
					frame = EDGE_UNKNOWN;
				}
				
				if(ppt.getNounPhraseIdx() != -1) {
					createNounPhraseFrameEdgesV2(edges, vertices, words, metadata.getNounMetadata().get(ppt.getNounPhraseIdx()), vbIndex);
				}
			}
			
			if(frame != null ) {
				if(fromIdx < 0)
					fromIdx = 0; // prep phrase begins the sentence
				Edge e = new Edge(GraphClass.f_related, modifiedToken == null ? vertices.get(fromIdx) : modifiedToken, vertices.get(toIdx));
				setDefaultEdgeProps(e, frame, STATE_UNKNOWN, ppm.isNegated(), vbIndex);
				edges.add(e);
			}
		}
		
		// frame logic that must occur outside of the loop
		if(prep.equalsIgnoreCase("at") && words.get(prepIdx+1).getToken().equalsIgnoreCase("least")) {
			//Edge e = new Edge("f_minimum", vertices.get(prepIdx), vertices.get(prepIdx+1));
			Edge e = new Edge(GraphClass.f_related, vertices.get(prepIdx), vertices.get(prepIdx+1));
			setDefaultEdgeProps(e, "f_minimum", STATE_UNKNOWN, ppm.isNegated(), vbIndex);
			edges.add(e);
		}
	}
	
	private void processDiscreteData(Set<Edge> edges, List<Vertex> vertices, HashMap<String, String> discrete, Vertex fromVertex, Vertex toVertex) {
		for(String key : discrete.keySet()) {
			toVertex.getProps().put(key, discrete.get(key));
		}
		edges.add(new Edge(GraphClass.has_discrete, fromVertex, toVertex));
		vertices.add(toVertex);
	}
	
	private int findEdgeByFrame(Set<Edge> edges, EdgeDirection dir, GraphClass edgeClass, Vertex vertex) {
		int ret = -1;
		int counter = 0;
		
		for(Edge e : edges) {	
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
				if(e.get_Class().equalsIgnoreCase("f_related")) {
					for(String key : e.getProps().keySet()) {
						if(e.getProps().get(key).toString().equalsIgnoreCase(edgeClass.toString())) {
							ret = counter;
							break;
						}
					}
				}
			}
			counter++;
		}
		
		return ret;
	}
	
	private void createVerbEdges(VerbPhraseToken token, int vbPos, GraphClass edge, List<Vertex> vertices, Set<Edge> edges, SentenceMetadata metadata) {
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
	
	private class VerbInfo {
		String infinitive;
		String tense;
		String st;
		Boolean isCompound;
		
		public VerbInfo(VerbPhraseMetadata vpm, List<WordToken> words) {

			isCompound = vpm.getVerbs().size() > 1;;
			
			if(isCompound) {
				st = vpm.getSemanticType(); // ST comes from the metadata
			} else {
				st = words.get(vpm.getVerbs().get(0).getPosition()).getSemanticType(); // ST comes from the token
			}
			
			if(st == null)
				st = "";
			
			if(st != null && st.length() > 0) {
				String[] st2 = st.split("_");
				infinitive = st2[0]; // infinitive from the ST (inf_tense in Redis)
				if(st2.length > 1)
					tense = st2[1];
				else {
					logger.warn("Verb has no tense: " + vpm.getVerbString());
					tense = "unknown";
				}
			} else {
				logger.warn("Verb has no ST: " + vpm.getVerbString());
				infinitive = vpm.getVerbs().get(vpm.getVerbs().size()-1).getToken(); // final verb of phrase
				tense = "unknown";
			}
		}
	}
}