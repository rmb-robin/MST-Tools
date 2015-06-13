package com.mst.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mst.model.MetaMapToken;
import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.model.ontology.SyntacticObject;
import com.mst.model.sql.OntologyEntry;
import com.mst.model.sql.SnomedTreeValue;

public class PostgreSQL {

	//private final String URL = "jdbc:postgresql://173.255.220.116/mmflexicon";
	//private final String USER = "mmflexicon";
    //private final String PASSWORD = null;
	private Connection con = null;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public PostgreSQL() {
		
	}
	
	public PostgreSQL(String database) {
		connect(database);
	}
	
	public Connection getConnection() {
		return con;
	}
	
	public boolean connect(String database) {
		try {
			if(database == null || database.length() == 0) {
				// for use on the tomcat server
				InitialContext ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/postgres");
				con = ds.getConnection();
			} else {
				// for local testing
				con = DriverManager.getConnection(Props.getProperty("postgres_host") + database, Props.getProperty("postgres_user"), Props.getProperty("postgres_pw"));
			}
		} catch(Exception e) {
			logger.error("Error establishing a connection to PostgreSQL. \n{}", e);
			e.printStackTrace();
		}
		return (con != null);
	}
	
	public void close() {
		try {
			if(con != null)
				con.close();
		} catch (SQLException e) {
			//e.printStackTrace();
			logger.error("close(): \n{}", e);
		}
	}
	
	public boolean insertInto(String sql) {
		try {
			Statement st = con.createStatement();
			st.executeUpdate(sql); 
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	public String[] getVerbRelationshipData(String subj, String verb, String subjc, boolean negated) {
        PreparedStatement st = null;
        ResultSet rs = null;
        StringBuilder ret = null;
        
        try {
        	StringBuilder query = new StringBuilder();

        	String op1 = subj == null ? " is null " : " = ? ";
        	String op2 = verb == null ? " is null " : " = ? ";
        	String op3 = subjc == null ? " is null " : " = ? ";
        	
        	query.append("select vr.subject, vr.verb, vr.object, c.class, cv.value, c.parent, vr.id, a.attribute ")
		   		 .append("from verb_relationships vr ")
		   		 .append("join classes c on vr.class_id = c.id ")
		   		 .append("left join attributes a on vr.attribute_id = a.id ")
		   		 .append("join class_values cv on vr.class_value_id = cv.id ")
		   		 .append("where vr.subject ").append(op1)
		   		 .append("and vr.verb ").append(op2)
		   		 .append("and vr.object ").append(op3)
		   		 .append("and vr.negated = ?");
   		
        	st = con.prepareStatement(query.toString());
        	int index = 1;
        	if(subj != null)
        		st.setString(index++, subj.toLowerCase());
        	if(verb != null)
        		st.setString(index++, verb.toLowerCase());
        	if(subjc != null)
        		st.setString(index++, subjc.toLowerCase());
        	st.setBoolean(index++, negated);
        	
        	rs = st.executeQuery();

        	if(rs.next()) {
        		ret = new StringBuilder();
        		ret.append(rs.getString(1)).append("|")
        		   .append(rs.getString(2)).append("|")
        		   .append(rs.getString(3)).append("|")
        		   .append(rs.getString(4)).append("|")
        		   .append(rs.getString(5)).append("|")
        		   .append(rs.getString(6)).append("|")
        		   .append(rs.getString(7)).append("|")
        		   .append(rs.getString(8));
        	}
            	
        } catch(Exception e) {
        	logger.error("getVerbRelationshipData(): \n{}", e);
        	e.printStackTrace();

        } finally {
            try {
                if(rs != null)
                    rs.close();
                if(st != null)
                    st.close();
                //if(con != null)
                    //con.close();
            } catch(Exception e) {
            	logger.warn("getVerbRelationshipData(): Error closing database objects. \n{}", e);
            }
        }
        
        if(ret != null) {
        	return ret.toString().split("\\|");
        } else {
        	return null;
        }
    }
	
	public Map<String,String> getVerbRelationshipDataMap(String subj, String verb, String subjc, boolean negated) {
        PreparedStatement st = null;
        ResultSet rs = null;
        //StringBuilder ret = null;
        Map<String,String> ret = new HashMap<String,String>();
        
        try {
        	StringBuilder query = new StringBuilder();

        	String op1 = subj == null ? " is null " : " = ? ";
        	String op2 = verb == null ? " is null " : " = ? ";
        	String op3 = subjc == null ? " is null " : " = ? ";
        	
        	query.append("select vr.subject, vr.verb, vr.object, c.class, cv.value, vr.id, a.attribute ")
		   		 .append("from verb_relationships2 vr ")
		   		 .append("join classes c on vr.class_id = c.id ")
		   		 .append("left join attributes a on vr.attribute_id = a.id ")
		   		 .append("join class_values cv on vr.class_value_id = cv.id ")
		   		 .append("where vr.subject ").append(op1)
		   		 .append("and vr.verb ").append(op2)
		   		 .append("and vr.object ").append(op3)
		   		 .append("and vr.negated = ?");
   		
        	st = con.prepareStatement(query.toString());
        	int index = 1;
        	if(subj != null)
        		st.setString(index++, subj.toLowerCase());
        	if(verb != null)
        		st.setString(index++, verb.toLowerCase());
        	if(subjc != null)
        		st.setString(index++, subjc.toLowerCase());
        	st.setBoolean(index++, negated);
        	
        	rs = st.executeQuery();

        	if(rs.next()) {
        		//ret = new StringBuilder();
        		ret.put("subject", rs.getString(1));
        		ret.put("verb", rs.getString(2));
        		ret.put("object", rs.getString(3));
        		ret.put("class", rs.getString(4));
        		ret.put("value", rs.getString(5));
        		ret.put("rel_id", rs.getString(6));
        		ret.put("attribute", rs.getString(7));
        	}
            	
        } catch(Exception e) {
        	logger.error("getVerbRelationshipDataMap(): \n{}", e);
        	e.printStackTrace();

        } finally {
            try {
                if(rs != null)
                    rs.close();
                if(st != null)
                    st.close();
                //if(con != null)
                    //con.close();
            } catch(Exception e) {
            	logger.warn("getVerbRelationshipData(): Error closing database objects. \n{}", e);
            }
        }
        
       	return ret;
    }
	
	public Map<String,String> getPPStructuredOutputClassMap(String verbClass, String prepPhrase) {
        PreparedStatement st = null;
        ResultSet rs = null;
        Map<String,String> ret = new HashMap<String,String>();
        
        try {
        	StringBuilder query = new StringBuilder();
        	
        	query.append("select c.class, ppm.id, a.attribute ")
		   		 .append("from prep_phrase_mapping2 ppm ")
		   		 .append("join classes c on ppm.class_id = c.id ")
		   		 .append("join attributes a on ppm.attribute_id = a.id ")
		   		 .append("where ppm.verb_class = ? ")
		   		 .append("and ppm.prep_phrase = ?");
   		        	
        	st = con.prepareStatement(query.toString());
        	st.setString(1, verbClass);
        	st.setString(2, prepPhrase.toLowerCase());
        	
        	rs = st.executeQuery();

        	if(rs.next()) {
        		ret.put("class", rs.getString(1));
        		ret.put("id", rs.getString(2));
        		ret.put("attribute", rs.getString(3));
        	}
            	
        } catch(Exception e) {
        	logger.error("getPPStructuredOutputClassMap(): \n{}", e);
        	e.printStackTrace();

        } finally {
            try {
                if(rs != null)
                    rs.close();
                if(st != null)
                    st.close();
                //if(con != null)
                    //con.close();
            } catch(Exception e) {
            	logger.warn("getPPStructuredOutputClass(): Error closing database objects. \n{}", e);
            }
        }
        
       	return ret;
    }
	
	public String[] getPPStructuredOutputClass(String verbClass, String prepPhrase) {
        PreparedStatement st = null;
        ResultSet rs = null;
        StringBuilder ret = null;
        
        try {
        	StringBuilder query = new StringBuilder();
        	
        	query.append("select c.class, ppm.id, a.attribute ")
		   		 .append("from prep_phrase_mapping ppm ")
		   		 .append("join classes c on ppm.class_id = c.id ")
		   		 .append("join attributes a on ppm.attribute_id = a.id ")
		   		 .append("where ppm.verb_class = ? ")
		   		 .append("and ppm.prep_phrase = ?");
   		        	
        	st = con.prepareStatement(query.toString());
        	st.setString(1, verbClass);
        	st.setString(2, prepPhrase.toLowerCase());
        	
        	rs = st.executeQuery();

        	if(rs.next()) {
        		ret = new StringBuilder();
        		ret.append(rs.getString(1)).append("|")
        		   .append(rs.getString(2)).append("|")
        		   .append(rs.getString(3));
        	}
            	
        } catch(Exception e) {
        	logger.error("getPPStructuredOutputClass(): \n{}", e);
        	e.printStackTrace();

        } finally {
            try {
                if(rs != null)
                    rs.close();
                if(st != null)
                    st.close();
                //if(con != null)
                    //con.close();
            } catch(Exception e) {
            	logger.warn("getPPStructuredOutputClass(): Error closing database objects. \n{}", e);
            }
        }
        
        if(ret != null) {
        	return ret.toString().split("\\|");
        } else {
        	return null;
        }
    }
	
	/* 3/11/2015 The queries below are deprecated and only provided for reference */
	
	public ArrayList<SyntacticObject> getOntologyEntryByToken(String token) {
        PreparedStatement st = null;
        ResultSet rs = null;
        ArrayList<SyntacticObject> list = new ArrayList<SyntacticObject>();
        
        try {
        	StringBuilder query = new StringBuilder();

        	query.append("select so.value, pos.value as pos, so.snomed_id, vb.value as \"verb class\", vi.value as infinitive, src.value as src, so.last_modified, so.modified_by ");
        	query.append("from syn_object as so ");
        	query.append("left join syn_part_of_speech_xref as pos_xref on so.id = pos_xref.object_id ");
        	query.append("left join syn_part_of_speech as pos on pos_xref.part_of_speech_id = pos.id ");
            query.append("left join syn_source_xref as src_xref on so.id = src_xref.object_id ");
        	query.append("left join syn_source as src on src_xref.source_id = src.id ");
        	query.append("left join syn_verb_class_xref as vb_xref on so.id = vb_xref.object_id ");
            query.append("left join syn_verb_class as vb on vb_xref.verb_class_id = vb.id ");
        	query.append("left join syn_verb_infinitive_xref as vi_xref on so.id = vi_xref.object_id ");
            query.append("left join syn_verb_infinitives as vi on vi_xref.verb_infinitive_id = vi.id ");
        	if(token != null)
        		query.append("where so.value = ?");    
        	
        	st = con.prepareStatement(query.toString());
        	if(token != null)
        		st.setString(1, token);
        	rs = st.executeQuery();
        	
            while(rs.next()) {
            	SyntacticObject syn = new SyntacticObject();
            	
            	syn.value = rs.getString("value");
            	syn.part_of_speech = rs.getString("pos");
            	syn.snomed_id = rs.getString("snomed_id");
            	if(rs.getString("verb class") != null)
            		syn._class.add(rs.getString("verb class"));
            	syn.infinitive = rs.getString("infinitive");
            	if(rs.getString("src") != null)
            		syn.source.add(rs.getString("src"));
            	//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            	
            	syn.last_modified = rs.getTimestamp("last_modified");
            	syn.modified_by = rs.getString("modified_by");
            	
            	syn.snomed_tree = getOntologySnomedTree(syn.value);
            	
            	list.add(syn);
            }
            
        } catch(Exception e) {
        	logger.error("getOntologyEntryByToken(): \n{}", e);
        	e.printStackTrace();

        } finally {
            try {
                if(rs != null)
                    rs.close();
                if(st != null)
                    st.close();
                //if(con != null)
                    //con.close();
            } catch(Exception e) {
            	logger.warn("getOntologyEntryByToken(): Error closing database objects. \n{}", e);
            }
        }
        
        return list;
    }
	
	public ArrayList<SyntacticObject.SnomedTreeValue> getOntologySnomedTree(String token) {
        PreparedStatement st = null;
        ResultSet rs = null;
        ArrayList<SyntacticObject.SnomedTreeValue> tree = new ArrayList<SyntacticObject.SnomedTreeValue>();
        
        try {
        	StringBuilder query = new StringBuilder();

        	query.append("select tv.id, tv.value, t.tree_id, t.position from syn_snomed_tree as t ");
        	query.append("join syn_snomed_tree_values as tv on t.snomed_tree_value_id = tv.id ");
        	query.append("join syn_object as so on so.id = t.object_id ");
        	query.append("and so.value = ? ");
            query.append("order by tree_id, position");
        	
        	st = con.prepareStatement(query.toString());
        	st.setString(1, token);
        	rs = st.executeQuery();
        	
        	SyntacticObject syn = new SyntacticObject();
        	
            while(rs.next()) {
            	tree.add(syn.new SnomedTreeValue(rs.getInt("tree_id"), rs.getInt("position"), rs.getString("id"), rs.getString("value")));
            }
            
        } catch(Exception e) {
        	logger.error("getOntologySnomedTree(): \n{}", e);
        	e.printStackTrace();

        } finally {
            try {
                if(rs != null)
                    rs.close();
                if(st != null)
                    st.close();
                //if(con != null)
                    //con.close();
            } catch(Exception e) {
            	logger.warn("getOntologySnomedTree(): Error closing database objects. \n{}", e);
            }
        }
        
	    return tree;
        
    }
	
	public String metamapSemTypeOnly(String jsonSentence) {
        Statement st = null;
        ResultSet rs = null;
        ArrayList<MetaMapToken> metamapList = new ArrayList<MetaMapToken>();
        Gson gson = new Gson();
        Sentence sentence = new Sentence();
        
        try {
        	sentence = gson.fromJson(jsonSentence, Sentence.class);
        	
        	st = con.createStatement();

        	StringBuilder entryList = new StringBuilder();
        	StringBuilder query = new StringBuilder();
        	
        	for(WordToken word : sentence.getWordList()) {
        		if(word.getPOS().matches("NN|NNS"))
        			entryList.append(",'").append(word.getToken()).append("'");
        	}
        	// TODO convert to parameterized query
            query.append("SELECT DISTINCT entry, list_id ");
            query.append("FROM metamap_metamaplistentry ");
            query.append("WHERE entry IN (");
            query.append(entryList.toString().substring(1));
            query.append(") ORDER BY entry");
//System.out.println(query.toString());
            rs = st.executeQuery(query.toString());
            
            String oldEntry = "";
            List<String> semTypes = new ArrayList<String>();
            
            while(rs.next()) {
            	if(!rs.getString("entry").equals(oldEntry) && semTypes.size() > 0) {
            		metamapList.add(new MetaMapToken(oldEntry, null, null, null, semTypes, null));
            		semTypes = new ArrayList<String>();
            	}
            	oldEntry = rs.getString("entry");
            	semTypes.add(rs.getString("list_id"));
            }

            // add final semantic type list
            if(semTypes.size() > 0) {
        		metamapList.add(new MetaMapToken(oldEntry, null, null, null, semTypes, null));
        	}
            
        } catch(Exception e) {
        	logger.error("metamapSemTypeOnly(): \n{}", e);

        } finally {
            try {
                if(rs != null)
                    rs.close();
                if(st != null)
                    st.close();
                //if(con != null)
                    //con.close();
            } catch(Exception e) {
            	logger.warn("metamapSemTypeOnly(): Error closing database objects. \n{}", e);
            }
        }
        
        if(metamapList.size() > 0) {
	    	//sentence.setMetaMapList(metamapList);
	    	
	    	//if(writeToDB) {
				//MongoDB mongo = new MongoDB();
				//mongo.insertMetaMapData(gson.toJson(sentence));
			//}
	    }
        
        // remove WordList to cut down on json clutter
	    sentence.setWordList(null);
	    
	    return gson.toJson(sentence);
        
    }
	
	public String metamapFull(String jsonSentence, boolean writeToDB) {
        Statement st = null;
        ResultSet rs = null;
        ArrayList<MetaMapToken> metamapList = new ArrayList<MetaMapToken>();
        Gson gson = new Gson();
        Sentence sentence = new Sentence();
        
        try {
    		sentence = gson.fromJson(jsonSentence, Sentence.class);
    		
        	st = con.createStatement();

        	StringBuilder entryList = new StringBuilder();
        	StringBuilder query = new StringBuilder();
        	
        	for(WordToken word : sentence.getWordList()) {
        		if(word.getPOS().matches("NN|NNS"))
        			entryList.append(",'").append(word.getToken()).append("'");
        	}
        	
    		// TODO convert to parameterized query
            query.append("SELECT DISTINCT ls.list_id, ful.matched_text, ful.sources_list, ful.preferred_name, ful.concept_name, ful.concept_id "); 
            query.append("FROM metamap_metamaplistentry AS ls ");
            query.append("JOIN metamap_metamapnamedentity AS ful "); 
            query.append(" ON ls.list_id = ful.semantic_type ");
            query.append(" AND ls.entry = ful.matched_text ");
            query.append("WHERE ls.entry IN (");
            query.append(entryList.toString().substring(1));
            query.append(") ORDER BY ful.matched_text");
//System.out.println(query.toString());
            rs = st.executeQuery(query.toString());

            while(rs.next()) {	                
                //(String value, String conceptID, String conceptName, String preferredName, List<String> semanticTypes, List<String> sources)
                List<String> semTypes = new ArrayList<String>();
                semTypes.add(rs.getString("list_id"));
                
                List<String> sourcesList = new ArrayList<String>();
                String[] sources = rs.getString("sources_list").split(",");
                for(String source : sources) {
                	sourcesList.add(source);
                }
                metamapList.add(new MetaMapToken(rs.getString("matched_text"), rs.getString("concept_id"), rs.getString("concept_name"), rs.getString("preferred_name"), semTypes, sourcesList));
            }
        	
        } catch(Exception e) {
        	logger.error("metamapFull(): \n{}", e);

        } finally {
            try {
                if(rs != null)
                    rs.close();
                if(st != null)
                    st.close();
                //if(con != null)
                    //con.close();
            } catch(Exception e) {
            	logger.warn("metamapFull(): Error closing database objects. \n{}", e);
            }
        }
        
        if(metamapList.size() > 0) {
	    	//sentence.setMetaMapList(metamapList);
	    	
	    	if(writeToDB) {
				//MongoDB mongo = new MongoDB();
				//mongo.insertMetaMapData(gson.toJson(sentence));
			}
	    }
        
        // remove WordList to cut down on json clutter
	    sentence.setWordList(null);
	    
	    return gson.toJson(sentence);
    }	
}
