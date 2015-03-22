package com.mst.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.mst.model.pubmed.PubMedArticle;
import com.mst.model.pubmed.PubMedArticleList;
import com.mst.model.pubmed.PubMedArticle.AbstractText;
import com.mst.model.Sentence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubMed {

	private final String Q_PUBMED = "qPubMed";
    private final String Q_PUBMED_AUDIT = "qPubMedAudit";
    private String PUBMED_TOOL = "Medical Search Technologies";
    private String PUBMED_EMAIL = "eric.plumb@gmail.com";
    private final int MIN_YEAR = 1950;
    private final int MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR);
    private int MAX_ARTICLES_PER_ITERATION = 200;;
    private int MAX_ARTICLES_TOTAL = 10000;;
    //private final String FILE_OUTPUT_DIR = "/Users/scottdaugherty/Documents/MedicalSearchTech/test_data/1_paragraph/";
    private String PMC_FULL_ARTICLE_URL = "http://www.pubmedcentral.nih.gov/oai/oai.cgi?verb=GetRecord&identifier=oai:pubmedcentral.nih.gov:%s&metadataPrefix=pmc";
    private List<String> existingPMIDs = null;
    
    private ArrayList<String> deletePMIDs = new ArrayList<String>();
    
    private Logger logger = LoggerFactory.getLogger(getClass());
	
	private ActiveMQ activeMQ = null;
	private Gson gson = new Gson();
	private MongoDB mongo = null;
	
	public PubMed() { 
		PUBMED_TOOL = Props.getProperty("pubmed_tool", PUBMED_TOOL);
        PUBMED_EMAIL = Props.getProperty("pubmed_email", PUBMED_EMAIL);
        MAX_ARTICLES_PER_ITERATION = Integer.parseInt(Props.getProperty("pubmed_articles_per_iteration", String.valueOf(MAX_ARTICLES_PER_ITERATION)));
        MAX_ARTICLES_TOTAL = Integer.parseInt(Props.getProperty("pubmed_articles_total", String.valueOf(MAX_ARTICLES_TOTAL)));
        activeMQ = new ActiveMQ();
        mongo = new MongoDB();
	}

//	public void camelGetArticles(String json) {
//		PubMedRequest pmr = gson.fromJson(json, PubMedRequest.class);
//		getArticles(pmr.getSearchTerm(), pmr.getMinYear(), pmr.getMaxYear(), pmr.getArticlesPerIteration(), pmr.getArticlesTotal());
//	}
	
	public void getArticles(String searchTerm) {
		getArticles(searchTerm, 0, 0, 0, 0, false, 0, false);
	}
	
	public void getArticles(String searchTerm, int minYear, int maxYear, int articlesPerIteration, int limit, boolean exactMatch, int offset, boolean getFullArticleText) {

		int articlesProcessed = 0;
		int countFromXML = limit;
		articlesPerIteration = (articlesPerIteration > 0 ? Math.min(articlesPerIteration, MAX_ARTICLES_PER_ITERATION) : MAX_ARTICLES_PER_ITERATION);
		limit = (limit > 0 ? Math.min(limit, MAX_ARTICLES_TOTAL) : MAX_ARTICLES_TOTAL);
		minYear = (minYear >= MIN_YEAR && minYear <= MAX_YEAR ? minYear : MIN_YEAR);
		maxYear = (maxYear >= MIN_YEAR && maxYear <= MAX_YEAR ? maxYear : MAX_YEAR);

		// PMIDs that exist in the mongoDB collection. Use this to prevent adding duplicate PubMed articles.
		// This is also done in MongoDB.java to account for the lag time between adding a msg onto the queue and it getting inserted
		// into the database (many hours in some cases).
		// Do the check here in addition to MongoDB.java to prevent unnecessary parsing/processing in the Camel processes. 
		existingPMIDs = mongo.getDistinctStringValues("article_id");

		if(exactMatch)
            searchTerm = (new StringBuilder("\"")).append(searchTerm).append("\"").toString();
		
		PubMedArticleList fullArticleList = new PubMedArticleList();
		fullArticleList.setSearchTerm(searchTerm);
		fullArticleList.setMinYear(minYear);
		fullArticleList.setMaxYear(maxYear);
		
		// results in a max return count of ARTICLES_TOTAL + ARTICLES_PER_ITERATION
		while(articlesProcessed < limit && articlesProcessed < countFromXML) {
			StringBuilder listUrl = new StringBuilder();
			
			listUrl.append("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed")
			   	   .append("&term=").append(searchTerm)
			       .append("&retmax=").append(articlesPerIteration)
			       .append("&retstart=").append(articlesProcessed + offset)
			       .append("&mindate=").append(minYear)
			       .append("&maxdate=").append(maxYear)
			       .append("&tool=").append(PUBMED_TOOL)
			       .append("&email=").append(PUBMED_EMAIL);
//logger.info(listUrl.toString());

			PubMedArticleList list = parseArticleIdListStax(getPubMedXML(listUrl.toString()));
//logger.info("list: " + list.getIdList().toString());			
			if(list != null && list.getIdList().size() > 0) {
				countFromXML = list.getCount();
				//if(list.getIdList().size() == 0) { // removed this after adding existingPMID count check
					//break; 
				//}

				StringBuilder detailUrl = new StringBuilder();
				
				detailUrl.append("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed")
						 .append("&rettype=xml&retmode=text")
						 .append("&id=").append(Joiner.on(",").join(list.getIdList()))
						 .append("&tool=").append(PUBMED_TOOL)
						 .append("&email=").append(PUBMED_EMAIL);
//logger.info(detailUrl.toString());	
				List<PubMedArticle> articles = parseArticleAbstractStax(getPubMedXML(detailUrl.toString()));
				
				if(articles.size() == 0)
					articlesProcessed += list.getIdList().size(); // some kind of error occured. increment based on # of pmids retrieved
				else {
					if(getFullArticleText) {
                        for(PubMedArticle article : articles) {
                            try {
                                for(PubMedArticle.ArticleId articleIdList : article.getArticleIdList()) {
                                    if(articleIdList.getIdType().equalsIgnoreCase("PMC")) {
                                        String url = String.format(PMC_FULL_ARTICLE_URL, articleIdList.getText().substring(3)); // strip "PMC" from ID 
                                        String xml = parseArticleFullTextStax(getPubMedXML(url), articleIdList.getText());
                                        if(xml != null) {
                                            article.setFullArticleText(xml);
                                            logger.info("getArticles(): Full text processed - PMID: {}; PMCID: {}", article.getPMID(), articleIdList.getText());
                                        }
                                        break;
                                    }
                                }
                            } catch(Exception e) {
                                logger.error("getArticles(): Error processing full text article for PMID {}\n{}", article.getPMID(), e);
                            }
                        }
                    }
					
					//writeArticleToFile(articles);
					writeArticleToJMS(Q_PUBMED, articles);
					articlesProcessed += articlesPerIteration; // increment based on number of articles actually processed
					
					for(PubMedArticle article : articles)
						fullArticleList.getIdList().add(article.getPMID());
				}
				
			} else {
				articlesProcessed += articlesPerIteration; // possible issue with getPubMedXML for pmids or parsing the IDs that came back.
			}
		}

		// write article list, associated with pmid, to a queue for future insertion into mongodb
		// this info will be used by the extract process to associate word tokens (db rows) with their originating search term
		if(fullArticleList.getIdList().size() > 0) {
			activeMQ.publishMessage(Q_PUBMED_AUDIT, gson.toJson(fullArticleList));
		}
		
		activeMQ.closeConnection();
		//logger.info("\"" + Joiner.on("\",\"").join(deletePMIDs) + "\"");
		//System.out.println(articlesProcessed);
	}
		
	private String getPubMedXML(String inUrl) {
		String ret = null;
		StringBuilder xml = new StringBuilder();
		URL url = null;
		BufferedReader reader = null;

		try	{
			url = new URL(inUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			//connection.setRequestProperty("accept-charset", "utf-8");
			//connection.setRequestProperty("content-type", "application/xml; charset=utf-8");
			connection.setRequestMethod("GET");

			connection.setReadTimeout(30*1000); // 30 second timeout
			connection.connect();

			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line = null;
			while((line = reader.readLine()) != null) {
				xml.append(line);
			}
			
			ret = xml.toString();
		} catch(FileNotFoundException fnfe) {
			logger.warn("getPubMedXML(): Full text article contained no data. URL: {}", inUrl);
	        ret = null;
	        
		} catch(Exception e) {
			logger.error("getPubMedXML(): URL: {} \n{}", inUrl, e);
	        ret = null;
		}
		finally {
			if(reader != null) {
				try {
					reader.close();
				}
				catch(IOException ioe) {
					logger.warn("getPubMedXML(): Error closing BufferedReader. {}", ioe);
				}
			}
		}

		return ret;
	}

	private PubMedArticleList parseArticleIdListStax(String xml) {
		PubMedArticleList list = null;
	
		if(xml != null) {
			list = new PubMedArticleList();
            //StringBuilder articleIds = new StringBuilder();
            ArrayList<String> articleIds = new ArrayList<String>();
            String tagContent = null;
            
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty("javax.xml.stream.isCoalescing", true); // ensures that tagContent won't be broken up into multiple pieces of XML (less-than symbol will cause this)
			
			Boolean processCount = true;
			
			try {
				byte[] byteArray = xml.getBytes("UTF-8");
				ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
				XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
				
				while(reader.hasNext()) {
					int event = reader.next();
	
					switch(event) {
						case XMLStreamConstants.CHARACTERS:
							tagContent = reader.getText().trim();
							break;
		
						case XMLStreamConstants.END_ELEMENT:
							if(reader.getLocalName().equals("Id") && !existingPMIDs.contains(tagContent)) {
								articleIds.add(tagContent);
								
							} else if(reader.getLocalName().equals("Count") && processCount) { // multiple <Count> nodes in XML. possible issue here if order changes
								list.setCount(Integer.parseInt(tagContent));
								processCount = false;
								
							} else if(reader.getLocalName().equals("RetMax")) {
								list.setRetMax(Integer.parseInt(tagContent));
								
							} else if(reader.getLocalName().equals("RetStart")) {
								list.setRetStart(Integer.parseInt(tagContent));
							}
							break;
					}
				}
			
				if(articleIds.size() > 0)
                    list.setIdList(articleIds);
				
			} catch(Exception e) {
				logger.error("Error parsing PubMed PMID list XML: \n{} \n{}", xml, e);
			}
        }
		return list;
	}

	public List<PubMedArticle> parseArticleAbstractStax(String xml) {	
		List<PubMedArticle> articles = new ArrayList<PubMedArticle>();
		List<PubMedArticle.AbstractText> abstracts = null;
		List<PubMedArticle.ArticleId> articleIdList = null;
		PubMedArticle pubMedArticle = null;
		String tagContent = null, abstractLabel = null, abstractNlmCategory = null;
		String articleIdType = null;
		boolean processPMID = true;
		
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty("javax.xml.stream.isCoalescing", true); // ensures that tagContent won't be broken up into multiple pieces of XML (less-than symbol will cause this)
		XMLStreamReader reader;

		String tempPMID = "";
		
		try {
			byte[] byteArray = xml.getBytes("UTF-8");
			ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);

			reader = factory.createXMLStreamReader(inputStream);

			while(reader.hasNext()) {	
				int event = reader.next();

				switch(event) {
					case XMLStreamConstants.START_ELEMENT: 
						if(reader.getLocalName().matches("PubmedArticle|PubmedBookArticle")) {
							pubMedArticle = new PubMedArticle();
							
						} else if(reader.getLocalName().equals("AbstractText")) {
							abstractLabel = reader.getAttributeValue("", "Label");
							abstractNlmCategory = reader.getAttributeValue("", "NlmCategory");
							
						} else if(reader.getLocalName().equals("Abstract")) {
							abstracts = new ArrayList<PubMedArticle.AbstractText>();
							
						} else if(reader.getLocalName().equals("ArticleIdList")) {
							articleIdList = new ArrayList<PubMedArticle.ArticleId>();
							
						} else if(reader.getLocalName().equals("ArticleId")) {
	                        articleIdType = reader.getAttributeValue("", "IdType");
						}
						break;
	
					case XMLStreamConstants.CHARACTERS:
						tagContent = reader.getText().trim();
						break;
	
					case XMLStreamConstants.END_ELEMENT:
						if(reader.getLocalName().equals("PMID")) {
							//if(tagContent.equals("8391753")) {
							//	System.out.println(xml);
							//}
							tempPMID = tagContent;
							if(processPMID) {
								pubMedArticle.setPMID(tagContent);
								processPMID = false;  // some articles have tons of PMID tags. ignore all but the first
							}
							
						} else if(reader.getLocalName().equals("AbstractText")) {
							PubMedArticle.AbstractText abstractText = pubMedArticle.new AbstractText(abstractLabel, abstractNlmCategory, tagContent);
							abstracts.add(abstractText);
							
						} else if(reader.getLocalName().equals("Abstract")) {
							pubMedArticle.setAbstractTextList(abstracts);
							
						} else if(reader.getLocalName().matches("PubmedArticle|PubmedBookArticle")) {
							// occasionally pmids have no <Abstract> tag. avoid adding if this is the case.
							if(pubMedArticle.getAbstractTextList() != null && pubMedArticle.getAbstractTextList().size() > 0)
								articles.add(pubMedArticle);
							if(!tempPMID.equals(pubMedArticle.getPMID())) {
								//logger.info("Delete: " + tempPMID + ", Keep: " + pubMedArticle.getPMID());
								deletePMIDs.add(tempPMID);
							}
							processPMID = true;
							
						} else if(reader.getLocalName().equals("ArticleId")) {
							articleIdList.add(pubMedArticle.new ArticleId(articleIdType, tagContent));
	                        break;
	                        
	                    } else if(reader.getLocalName().equals("ArticleIdList"))
	                        pubMedArticle.setArticleIdList(articleIdList);
						
						break;
	
					//case XMLStreamConstants.START_DOCUMENT:
					//	break;
				}
			}
		} catch(Exception e) {
			logger.error("Error parsing PubMed article abstract XML: \n{} \n{}", xml, e);
			//e.printStackTrace();
		}

		return articles;
	}
	
	private String parseArticleFullTextStax(String xml, String PMCID) {
        String ret = null;
        
        if(xml != null) {
            StringBuilder tagContent = new StringBuilder();
            StringBuilder paragraphs = new StringBuilder();
            boolean inBody = false;
            boolean inPara = false;
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty("javax.xml.stream.isCoalescing", true);
            
            xml = cleanFullArticle(xml);
            
            try {
                byte byteArray[] = xml.getBytes("UTF-8");
                ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
                XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
                		
                while(reader.hasNext()) {
                    int event = reader.next();
                    
                    switch(event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if(reader.getLocalName().equals("body")) {
                            inBody = true;
                        } else if(reader.getLocalName().equals("p") && inBody) {
                            inPara = true;
                        }
                        break;
                        
                    case XMLStreamConstants.CHARACTERS:
                        if(inPara)
                            tagContent.append(reader.getText());
                        
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if(reader.getLocalName().equals("body")) {
                            inBody = false;
                            inPara = false;
                        } else if(reader.getLocalName().equals("p") && inBody) {
                            paragraphs.append(tagContent).append(" ");
                            tagContent = new StringBuilder();
                            inPara = false;
                        }
                        break;
                    }
                }

                ret = paragraphs.toString();
            }
            catch(Exception e) {
                logger.error("Error parsing PubMed full article XML. PMCID: {}\n{}\n{}", PMCID, xml, e);
            }
        }
        return ret;
    }
	
	private String cleanFullArticle(String xml) {
        xml = xml.replaceAll("<sub>", "_");
        xml = xml.replaceAll("</sub>", "");
        xml = xml.replaceAll("<sup>", "^");
        xml = xml.replaceAll("</sup>", "");
        
        return xml;
    }
	
	private boolean writeArticleToJMS(String qName, List<PubMedArticle> articles) {
		boolean retVal = true;
		
		for(PubMedArticle article : articles) {
			try {
				existingPMIDs.add(article.getPMID());
				logger.info(article.getPMID());
				
				Sentence sentence = new Sentence();
				sentence.setId(article.getPMID());
				
				if(article.getFullArticleText() == null) {
					// concatenate full paragraph from individual <AbstractText> elements
					StringBuilder sb = new StringBuilder();
					for(AbstractText abstractText : article.getAbstractTextList()) {
						sb.append(abstractText.getText()).append(" ");
					}
					
					sentence.setFullSentence(sb.toString().trim());
				} else {
					sentence.setFullSentence(article.getFullArticleText());
				}
				
				activeMQ.publishMessage(qName, gson.toJson(sentence));
								
			} catch(Exception e) {
				logger.error("Error writing to JMS queue: {} \n{}", qName, e);
				//e.printStackTrace();
			}
        }
		
		return retVal;
	}
	
//	private boolean writeArticleToFile(List<PubMedArticle> articles) {
//		boolean retVal = true;
//		
//		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
//		SimpleDateFormat sdf = new SimpleDateFormat("-HH-mm-ss-SSS");
//		
//		for(PubMedArticle article : articles) {
//			try {
//				String fileName = FILE_OUTPUT_DIR + article.getPMID() + sdf.format(new Date()) + ".txt";
//				File file = new File(fileName);
//	 
//				if(!file.exists()) {
//					file.createNewFile();
//				}
//	 
//				FileWriter fw = new FileWriter(file.getAbsoluteFile());
//				BufferedWriter bw = new BufferedWriter(fw);
//				
//				// concatenate full paragraph from individual <AbstractText> elements
//				StringBuilder sb = new StringBuilder();
//				for(AbstractText abstractText : article.getAbstractTextList()) {
//					sb.append(abstractText.getText()).append(" ");
//				}
//				
//				Sentence sentence = new Sentence();
//				sentence.setArticleId(article.getPMID());
//				sentence.setFullSentence(sb.toString().trim());
//				
//				bw.write(gson.toJson(sentence));
//				
//				bw.close();
//				
//				File doneFile = new File(fileName + ".done");
//				doneFile.createNewFile();
//				
//				
//			} catch(Exception e) {
//				retVal = false;
//				e.printStackTrace();
//			}
//		}
//	
//		return retVal;
//	}
	
//	public PubMedArticleList getArticleList(String searchTerm, int minYear, int maxYear, int articleCount, int articleOffset) {
//	PubMedArticleList list = null;
//	StringBuilder listUrl = new StringBuilder();
//	
//	articleOffset = (articleOffset > 0 ? articleOffset : 0);
//	articleCount = (articleCount > 0 ? Math.min(articleCount, MAX_ARTICLES_PER_ITERATION) : MAX_ARTICLES_PER_ITERATION);
//	minYear = (minYear >= MIN_YEAR && minYear <= MAX_YEAR ? minYear : MIN_YEAR);
//	maxYear = (maxYear >= MIN_YEAR && maxYear <= MAX_YEAR ? maxYear : MAX_YEAR);
//	
//	listUrl.append("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed")
//	   	   .append("&term=").append(searchTerm)
//	       .append("&retmax=").append(articleCount)
//	       .append("&retstart=").append(articleOffset)
//	       .append("&mindate=").append(minYear)
//	       .append("&maxdate=").append(maxYear)
//	       .append("&tool=").append(PUBMED_TOOL)
//	       .append("&email=").append(PUBMED_EMAIL);
//
//	list = parseArticleIdListStax(getPubMedXML(listUrl.toString()));
//	
//	return list;
//}
}
