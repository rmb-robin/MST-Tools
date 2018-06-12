package com.mst.util;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mst.model.sentenceProcessing.WordToken;

public class Constants {
	
	public static Map<String, String> semanticTypes = new HashMap<String, String>();
	public static Map<String, String> verbOverrides = new HashMap<String, String>();
	
	static {
		// TODO why am I not getting these from Redis?
		semanticTypes.put("[age]","age");
		semanticTypes.put("[number]","number");
		semanticTypes.put("[proper noun]","propn");
		semanticTypes.put("[DATE]","DATE");
		semanticTypes.put("[tnm]","tnmstage");
		semanticTypes.put("[time]","time");
		semanticTypes.put("[gene_neg]","gene-neg");
		semanticTypes.put("[gene_pos]","gene-pos");
		
		verbOverrides.put("left", "JJ");
		verbOverrides.put("pelvis", "NN"); // why does Stanford EVER tag pelvis as a verb???
	}
	
	public enum Source {
		PUBMED_ABSTRACT {
			@Override
			public String getMongoCollection() {
				return "annotations";
			}
		},
		PUBMED_FULLARTICLE {
			@Override
			public String getMongoCollection() {
				return "annotations";
			}
		}, 
		CT_SCAN {
			@Override
			public String getMongoCollection() {
				return "annotations";
			}
		},
		IMAGING {
			@Override
			public String getMongoCollection() {
				return "annotations";
			}
		},
		VISIT_NOTES {
			@Override
			public String getMongoCollection() {
				return "annotations";
			}
		},
		UNKNOWN {
			@Override
			public String getMongoCollection() {
				return "test";
			}
		},
		MEDS {
			@Override
			public String getMongoCollection() {
				return "discreet";
			}
		};
		public String getMongoCollection() {
			return "test";
		}
	}
	
	public enum VerbClass {
		LINKING_VERB, VERB_OF_BEING, INFINITIVE, ACTION, MODAL_AUX, PREPOSITIONAL;
	}
	
	public enum DependentPhraseClass {
		BEGINS_SENTENCE, PRECEDED_BY_COMMA, FOLLOWED_BY_VERB, OTHER;
	}
	
	public enum StructuredNotationReturnValue {
		SOURCE, VALUE, ST, NONE, SOURCE_SOLO;
	}
	
	public enum ModByPPClass {
		SUBJ, VB, SUBJC, NONE, PP, NP;
	}
	
	public enum GraphClass {
		Token, Source, Practice, Study, ID, Date, Sentence, Discrete,
		precedes, has_subj, has_subjc, has_pp_modifier, has_pp_object, has_source, has_study, has_id, has_date, has_sentence, has_practice,
		has_token, has_pp_sibling, has_np_sibling, has_dp_sibling, within_dp, mod_by, within_np, has_np_object, has_vb_pp_modifier, has_subj_pp_modifier, has_subjc_pp_modifier,
		has_discrete, 
		f_laterality, f_finding_site, f_linear_uom, f_disease_quantity, f_header, f_imaging_procedure, f_measurement, f_presence, f_absence, f_procedure,
		f_location, f_is_a, f_absolute_value, f_related
	}
	
	public enum GraphProperty {
		pos, negated, st;
	}
	
	public static final String MONGO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	// https://www.regex101.com/r/vL8jA2/2
	public static final Pattern DATE_REGEX_ST = Pattern.compile("^\\d\\d?(-|\\/)(\\d\\d?(-|\\/))?\\d\\d(\\d{2})?$");
	// https://www.regex101.com/r/pX0gC8/4
	public static final Pattern GLEASON_REGEX = Pattern.compile("(Gleason|GG|Gl)\\s*(score(\\s*of)?:?\\s*|grade\\s*)?(\\d\\s*\\+\\s*\\d|\\d\\s*\\(?\\d\\s*\\+\\s*\\d\\)?|\\s*\\d+)", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern SKYLINE_PSA_1 = Pattern.compile("(?i)PSA\\s*(\\d\\d\\/\\d\\d\\/\\d\\d\\d\\d)\\s*-?\\s*<?(\\d\\d?\\.?\\d{1,2})"); // PSA 05/01/2008 - 0.38
	public static final Pattern SKYLINE_PSA_2 = Pattern.compile("(?i)(Recent|\\d\\d\\/\\d\\d\\/\\d\\d\\d\\d)\\s*PSA\\s*-?\\s*<?(\\d\\d?\\.?\\d{1,2})"); // 02/27/2009 PSA 0.1
	
	public static final Pattern AGE_REGEX = Pattern.compile("\\d{1,3}-year-old");
	
	public static final Pattern PUNC = Pattern.compile("!|\"|#|\\$|%|&|'|\\(|\\)|\\*|\\+|,|-|—|\\.|/|:|;|<|=|>|\\?|@|\\[|\\]|\\^|_|`|\\{|\\||\\}|~");
	public static final Pattern NUMERIC = Pattern.compile("\\d*\\.?\\d*");
	
	public static final Pattern VERBS_OF_BEING = Pattern.compile("has|having|am|are|is|was|were|be|being|been|had|have", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern LINKING_VERBS = Pattern.compile("appear|become|becomes|feels|felt|is|looks|prove|remained|remains|seemed|seems|abandoned|accepted|accepting|accessed|accompanied|achieved|acting|added|adjusted|administered|admitted|advanced|advised|affected|aggravated|aggravating|allowed|ambulating|analysed|answered|anticipated|applied|appreciated|approved|arranged|asked|asking|assessed|associated|attempted|attributed|available|awaiting|awakening|based|battling|becoming|began|beginning|begun|biopsied|bothered|broken|brought|bumped|called|cancelled|cared|carried|catheterized|caused|causing|challenging|changed|changing|characterized|checked|checking|chosen|clamped|cleaned|clear|cleared|clearing|climbed|climbing|clogged|co-existing|combined|come|comes|coming|commenced|compared|complained|complaining|complains|complete|completed|complicated|concerened|concerned|concerning|condylomata|confined|confirmed|confused|considered|considering|constipated|consulted|contacted|contemplating|continue|continued|continues|continuing|contraindicated|contributing|controlled|converted|corrected|counseled|counselled|covered|crept|cured|cut|cutting|dealing|decided|deciding|declined|declining|decreased|decreasing|deemed|deferred|dehydrated|delayed|delivered|demonstrated|denied|depressed|described|describing|desired|detailed|detected|determined|developed|developing|diagnosed|dilated|diminished|discharged|discontinued|discouraged|discussed|disinclined|documented|doing|done|doubled|draining|drawn|dribbling|drinking|dropped|dropping|dying|eating|ejaculated|elected|electing|emanating|emphasized|employed|emptying|encountered|encouraged|encouraging|engaged|enrolled|entered|estimated|evaluated|exacerbated|exacerbating|examined|excellent|excluded|exercising|exhausted|expected|experienced|experiencing|explained|exploring|expressed|factoring|failed|failing|fallen|fatigued|faxed|feeling|felt|filled|fine|finished|finishing|fired|fixed|fluctuated|fluctuating|flushed|focused|followed|following|forcing|forming|found|frustrated|frustrating|functioning|gained|gaining|getting|give|given|giving|going|gone|gotten|graded|great|handling|happened|happening|headed|heading|healed|healing|held|helped|helping|highlighted|hindered|hit|holding|hopes|hospitalized|hurting|identified|imaged|improved|improving|inclined|included|increased|increasing|indicated|informed|initiated|injected|inquiring|inserted|instilled|instructed|interpreted|interrupted|invaded|investigated|involved|involving|jumped|known|lacking|leaking|leaning|learned|learning|leaving|left|lessened|limiting|lingering|listed|loaded|located|looking|losing|lost|made|magnified|maintained|managed|managing|married|measured|meeting|met|missed|missing|moderated|monitored|monitoring|motivated|moved|moving|needed|normalized|noted|noticed|noting|observed|obtained|occas|occurring|offered|omitted|oozing|operating|opposed|opted|ordered|passed|passing|pending|performed|persisted|pine|placed|planing|planned|planning|plannng|pleased|pointed|postponed|prepared|prepped|prescribed|presented|preserved|problems|progressed|Progressing|prolonged|provided|pulled|pushing|put|putting|quit|ran|randomized|ranged|read|readmitted|reassured|recalled|received|receiving|rechecked|recommended|recommending|recounseled|recovered|recovering|recuperating|recurred|reduced|referred|refilled|refused|refusing|regaining|regarding|reinserted|reiterated|rejected|related|relieved|remained|remaining|remembered|reminded|removed|repaired|repeated|replaced|repleted|reported|reporting|requested|required|resolved|resolving|responded|responding|restarted|re-started|resulted|resumed|retaining|retired|retiring|retracted|returned|returning|revealed|reviewed|revised|riding|risen|rising|rremaining|ruled|run|running|sacrificed|said|satisfied|saturated|scheduled|screened|seeing|seen|seing|selected|sending|sent|set|setting|settled|shooting|shown|signficant|sitting|sleeping|slowed|slowing|smoked|smoking|spared|spoken|spotting|squinching|stabilized|standing|stared|started|starting|stated|stayed|staying|stones|stooling|stopped|stopping|straining|struck|subsided|suffering|suggested|suggesting|summarized|supplemented|supposed|suppressed|suspected|suspended|switched|switching|taken|taking|talked|tansitioned|targeted|terminated|thickened|thinking|thought|threatening|thrilled|tied|tingling|tired|told|tolerated|tolerating|transferred|transferring|transitioned|transitioning|treated|treating|trending|trialed|tried|troubled|trying|undergoing|undergone|undertaken|updated|urinating|used|using|vaporized|viewed|visualized|voided|voiding|waiting|waking|washing|watched|watching|weakened|weaning|wearing|well|wetting|wiped|woken|worked|working|worn|worried|worsened|worsening|wrestling|written|show|start", Pattern.CASE_INSENSITIVE);
	
	// SRD 1/7/2016 - added 'does' as a modal
	public static final Pattern MODAL_AUX_VERB = Pattern.compile("can|could|did|may|must|should|will|would|shall|might|does", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern NEGATION = Pattern.compile("no|not|without|none|negative|neither|nor", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern ARTICLE = Pattern.compile("a|an|the", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern PREPOSITION = Pattern.compile("IN|TO");
	
	public static final Pattern VERB_SUBJ_SUBJC_EXCLUSIONS = Pattern.compile("a|also|an|and|another|as|be|but|cm|could|elsewhere|has|left|may|mm|otherwise|right|seen|to|which|would", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern PREPOSITIONS = Pattern.compile("after|although|among|as|at|before|between|by|during|for|from|in|of|on|over|per|than|through|to|while|with|within|without|off|since|until|along|around|throughout|under|above|near|behind|across|into|upon|via", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern DEPENDENT_SIGNALS = Pattern.compile("after|although|as|because|before|consequently|finally|first|furthermore|how|however|if|indeed|later|meanwhile|moreover|nevertheless|otherwise|overall|provided|second|since|so|than|that|then|therefore|though|thus|unless|until|what|whatever|when|whence|whenever|where|whereas|wherever|whether|which|whichever|while|whither|who|whoever|whom|whomever|whomsoever|whose|whosoever|why", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern CONJUNCTIVE_ADVERBS = Pattern.compile("furthermore|however|moreover|nevertheless|therefore", Pattern.CASE_INSENSITIVE); // left out "in contrast"

	public static final Pattern INTERSECTION_PREPOSITIONS_AND_DEPENDENT = Pattern.compile("after|as|before|since|than|until", Pattern.CASE_INSENSITIVE);
	
	//public static final Pattern TNM_STAGING_REGEX =          Pattern.compile("(?i)p?T[0-4]\\s*[abcd]*(\\s*p?N[0-3abcdx iv])*(\\s*MX*[01]*[abc]*)*");
	//public static final Pattern TNM_STAGING_REGEX_NO_SPACE = Pattern.compile("(?i)p?T[0-4][abc]*(p?N[0-3abcx iv])*(MX*[01]*[abc]*)*");
	// https://www.regex101.com/r/xU1lA6/2
	public static final Pattern TNM_STAGING_REGEX = Pattern.compile("(?i)p?T[0-4x][abcruy]*[\\s,]*(p?N[0-3x][abcruy]*)*[\\s*|,|&|and]*\\s*(M[01x])*[\\s,]*(R[0-2])*");
	public static final Pattern TNM_STAGING_REGEX_NO_SPACE = Pattern.compile("(?i)p?T[0-4x][abcruy]*[,]*(p?N[0-3x][abcruy]*)*[|,|&|and]*(M[01x])*[,]*(R[0-2])*");
	
	// https://www.regex101.com/r/oG3fE2/1
	public static final Pattern MULTIDIMENSIONAL_MEASUREMENT_REGEX = Pattern.compile("\\.?\\d+\\.?\\d*\\s*[x|X]\\s*\\.?\\d+\\.?\\d*\\s*([x|X]\\s*\\.?\\d+\\.?\\d*)?");
	public static final Pattern SINGLE_DIMENSION_MEASUREMENT_REGEX = Pattern.compile("\\.?\\d+\\.?\\d*(mm|cm)+");
    public static final Pattern CARDINAL_NUMBER_REGEX = Pattern.compile("\\.?\\d+\\.?\\d*");
	public static final Pattern UNIT_OF_MEASURE_REGEX = Pattern.compile("(mm|cm)");
	public static final Pattern MEASUREMENT_DELIMITER_REGEX = Pattern.compile("\\s*[x|X]\\s");

	public static final Pattern SALUTATION_REGEX = Pattern.compile("Dr\\.|Mr\\.|Ms\\.|Mrs\\.");
	
	//public static final Pattern GENE_POS = Pattern.compile("(?i).*\\-(positive|pos)");
	//public static final Pattern GENE_NEG = Pattern.compile("(?i).\\-(negative|neg)");
	
	// https://www.regex101.com/r/zA0iG2/1
	public static final Pattern TIME_REGEX = Pattern.compile("\\b[0-2]?\\d(:[0-5]\\d){1,2}(\\s*[A|P]M)?\\b", Pattern.CASE_INSENSITIVE);
	
//	private static final String MONGO_DB_HOST = Props.getProperty("mongo_host");
//	private static final String MONGO_DB = Props.getProperty("mongo_db");
//	private static final String REDIS_DB_HOST = Props.getProperty("redis_host");

	
//	public enum MongoDB {
//	    INSTANCE;
//	    
//	    private MongoClient mongo;
//	    private DB db;
//
//	    MongoDB() {
//	    	System.out.println("Established MongoDB connection");
//	    	System.out.println(MONGO_DB_HOST + " / " + MONGO_DB + " / " + Props.getProperty("mongo_user") + " / " + Props.getProperty("mongo_pw"));
//
//	        try {
//	            mongo = new MongoClient(MONGO_DB_HOST);
//	        } catch (UnknownHostException e) {
//	            e.printStackTrace();
//	        }
//
//	        db = mongo.getDB(MONGO_DB);
//	        
//	        //if(MONGO_DB_HOST.equalsIgnoreCase("mongo01.medicalsearchtechnologies.com")) {
//	        //	System.out.println("Authenticating to Digital Ocean server...");
//			//	boolean auth = db.authenticate(Props.getProperty("mongo_user"), Props.getProperty("mongo_pw").toCharArray());
//			//	System.out.println("MongoDB auth: " + auth);
//	        //}
//	    }
	    
//	    public DB getDB() {
//	    	return db;
//	    }
//	    
//	    public DBCollection getCollection(String collection) {
//	    	return db.getCollection(collection);
//	    }
//
//	    public void close(){
//	        mongo.close();
//	    }
//	}
	
	public enum RedisDBx {
	    INSTANCE;
	    
	    private Jedis jedis;

	    RedisDBx() {
	        try {
	            jedis = new Jedis("10.210.192.4");
		    	System.out.println("Established Redis connection");
	        } catch(Exception e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public Jedis getInstance() {
	    	return jedis;
	    }

	    public void close(){
	        jedis.close();
	    }
	}

	public enum MyJedisPool {
	    INSTANCE;
	    
	    private JedisPool pool;
	    
	    MyJedisPool() {
	        try {
	        	JedisPoolConfig config = new JedisPoolConfig();
	        	pool = new JedisPool(config, "10.210.192.4");
		    	System.out.println("Established Jedis Pool.");
	        } catch(Exception e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public Jedis getResource() {
	    	return pool.getResource();
	    }

	    public void close(){
	       pool.close();
	    }
	}
	
	public static long getTime() {
		Calendar cal = Calendar.getInstance();
		return cal.getTimeInMillis();
	}
	
	public static String formatTime(double totalSecs) {
		int hours = (int) (totalSecs / 3600);
		int minutes = (int) ((totalSecs % 3600) / 60);
		int seconds = (int) (totalSecs % 60);

		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	public static WordToken getToken(List<WordToken> wordList, int index) {
		WordToken token = new WordToken();
		try {
			token = wordList.get(index);
		} catch(IndexOutOfBoundsException oob) { }
		return token;
	}
}
