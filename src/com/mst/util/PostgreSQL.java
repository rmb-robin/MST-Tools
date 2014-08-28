package com.mst.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mst.model.MetaMapToken;
import com.mst.model.Sentence;
import com.mst.model.WordToken;

public class PostgreSQL {

	//private final String URL = "jdbc:postgresql://173.255.220.116/mmflexicon";
	//private final String USER = "mmflexicon";
    //private final String PASSWORD = null;
	private Connection con = null;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public PostgreSQL(String database) {
		connect(database);
	}
	
	public boolean connect(String database) {
		try {
			con = DriverManager.getConnection(Props.getProperty("postgres_host") + database, Props.getProperty("postgres_user"), Props.getProperty("postgres_pw"));
			
		} catch(SQLException e) {
			logger.error("Error establishing a connection to PostgreSQL. \n{}", e);
		}
		return (con != null);
	}
	
	public void close() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
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
	    	sentence.setMetaMapList(metamapList);
	    	
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
	    	sentence.setMetaMapList(metamapList);
	    	
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
