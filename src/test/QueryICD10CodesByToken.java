package test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mst.filter.FriendOfFriendServiceImpl;
import com.mst.interfaces.filter.FriendOfFriendService;
import com.mst.model.ICDQueryOutput;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.ShouldMatchOnSentenceEdgesResult;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.ICD;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscoveries;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.SentenceDiscoveryProcessorImpl;
import com.mst.util.Constants;
import com.mst.util.JSONHelper;
import com.mst.util.SentenceServiceMongoDatastoreProvider;
import com.mst.util.Utility;

public class QueryICD10CodesByToken {
	private SentenceDiscoveryProcessorImpl sentenceDiscoveryProcessorImpl = null;
	private StringBuffer outputBuffer = new StringBuffer();
	private HashSet<String> outputSet = new HashSet<>();
	private List<Object> icdQueryOutputList = new ArrayList<>();
	public final String NEGATED = "NEGATED";
	public final String EXISTING_CODE = "EXISTING CODE";
	public int fileCounter = 1;
	public HashSet<String> negationSpecialCharsSet = new HashSet<>();// Stream.of(";",
																		// "°").collect(Collectors.toCollection(HashSet::new));

	private static HashSet<String> onlyICDCodesSet = null;
	public int noOfJSONObjectsInOutputFile = Integer
			.valueOf(Utility.getProperty(Constants.QUERY_NO_OF_OBJECTS_PER_JSON_OUTPUT_FILE));
	private static HashMap<String, String> singleTokenICDCodesMap = null;

	public static String onlyICDCodesPath = Utility.getProperty(Constants.QUERY_ICD_CODE_ONLY_FILE_PATH);
	public static String singleTokenICDCodesPath = Utility.getProperty(Constants.QUERY_SINGLE_WORD_ICD_CODES_FILE_PATH);
	public String outputDirectoryPath = Utility.getProperty(Constants.QUERY_OUTPUT_DIRECTORY_PATH);
	public String inputFileName = Utility.getProperty(Constants.QUERY_INPUT_FILE_PATH);

	static {
		loadOnlyICDCodes();
		loadSingleTokenICDCodes();
	}

	public static void main(String s[]) {
		QueryICD10CodesByToken queryICD10CodesByToken = new QueryICD10CodesByToken();
		queryICD10CodesByToken.init();
		queryICD10CodesByToken.processTokensFromFile();
		// queryICD10CodesByToken.executeTest("atherosclerosis");
		// atherosclerosis
	}

	// @Before
	public void init() {
		Utility.printAllProperties();
		sentenceDiscoveryProcessorImpl = new SentenceDiscoveryProcessorImpl();
		sentenceDiscoveryProcessorImpl.setMongoDatastoreProvider(new SentenceServiceMongoDatastoreProvider(
				Utility.getProperty(Constants.MONGO_DB_IP), Utility.getProperty(Constants.MONGO_DB_NAME)));

		char chars[] = Utility.getProperty(Constants.QUERY_NEGATION_SPECIAL_CHAR_SET).toCharArray();
		for (char c : chars)
			negationSpecialCharsSet.add(String.valueOf(c));
	}

	// @Test
	public void processTokensFromFile() {
		try (Stream<String> stream = Files.lines(Paths.get(inputFileName))) {
			stream.forEach(token -> executeTest(token));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (icdQueryOutputList != null && icdQueryOutputList.size() > 0)
			JSONHelper.writeToFile(icdQueryOutputList, outputDirectoryPath
					+ Utility.getProperty(Constants.QUERY_OUTPUT_FILENAME_PREFIX) + fileCounter++ + ".json");
	}

	// @Test
	public void executeTest(String searchToken) {
		// String orgId = "59667d670bc1e91d43554492";
		String orgId = Utility.getProperty(Constants.QUERY_INPUT_ORG_ID);
		String edgeName = WordEmbeddingTypes.tokenToken;
		long t1 = System.currentTimeMillis();
		queryCollections(orgId, edgeName, searchToken);
		long t2 = System.currentTimeMillis();

		System.out.println("Time taken by querySentencesByToken() : " + (t2 - t1) / 1000 + "s");

		// if (icdQueryOutputList != null && icdQueryOutputList.size() > 0)
		// JSONHelper.writeToFile(icdQueryOutputList, outputDirectoryPath +
		// Utility.getProperty(Constants.QUERY_OUTPUT_FILENAME_PREFIX)
		// + fileCounter++ + ".json");
	}

	private void queryCollections(String orgId, String edgeName, String searchToken) {

		try {

			// create the query parameters
			SentenceQueryInstance icdSentenceQueryInstance = new SentenceQueryInstance();
			List<String> icdQueryInputTokens = new ArrayList<>();
			icdQueryInputTokens.add(searchToken);
			icdSentenceQueryInstance.setTokens(icdQueryInputTokens);

			List<SentenceQueryInstance> icdSentenceQueryInstances = new ArrayList<>();
			icdSentenceQueryInstances.add(icdSentenceQueryInstance);

			SentenceQueryInput icdSentenceQueryInput = new SentenceQueryInput();
			icdSentenceQueryInput.setSentenceQueryInstances(icdSentenceQueryInstances);

			// query the database
			List<ICD> icdSentences = sentenceDiscoveryProcessorImpl.getICDSentences(icdSentenceQueryInput);

			System.out.println("icdSentences.size() : " + icdSentences.size());

			// query the sentence discoveries collection
			SentenceQueryInstance sentenceQueryInstance = new SentenceQueryInstance();
			List<String> sentenceQueryInputTokens = null;
			List<EdgeQuery> sentenceQueryInputEdges = null;
			List<SentenceQueryInstance> sentenceQueryInstances = null;
			SentenceQueryInput sentenceQueryInput = null;
			EdgeQuery sentenceEdge = null;
			List<SentenceDiscoveries> sentenceSentences = null;
			int icdCollectionSize = icdSentences.size();
			int icdCounter = 1;
			// iterate the output of ICD collection
			for (SentenceDiscovery icd : icdSentences) {
				System.out.println("Iterating ICD document (" + searchToken + ")(" + icdCounter++ + "/"
						+ icdCollectionSize + "): " + icd.getId());
				sentenceQueryInstance = new SentenceQueryInstance();
				sentenceQueryInput = new SentenceQueryInput();
				sentenceQueryInputTokens = new ArrayList<>();
				sentenceQueryInputEdges = new ArrayList<>();
				sentenceQueryInstances = new ArrayList<>();
				// set search token
				sentenceQueryInputTokens.add(searchToken);
				// set edges
				sentenceEdge = new EdgeQuery();
				sentenceEdge.setName(edgeName); // use only token-token as the edgeName
				sentenceQueryInputEdges.add(sentenceEdge);

				sentenceQueryInstance.setTokens(sentenceQueryInputTokens);
				sentenceQueryInstance.setEdges(sentenceQueryInputEdges);

				sentenceQueryInstances.add(sentenceQueryInstance);
				sentenceQueryInput.setSentenceQueryInstances(sentenceQueryInstances);
				sentenceQueryInput.setOrganizationId(orgId);

				// fire query to sentence discoveries here
				sentenceSentences = sentenceDiscoveryProcessorImpl.getSentences(sentenceQueryInput);
				System.out.println("sentenceSentences size() : " + sentenceSentences.size());
				System.out.println("The ICD Code : " + findICDCode(icd));

				// for (SentenceDiscovery sentence : sentenceSentences) {
				// // System.out.println("Iterating sentence >>>>>>>>>>>>>>>>>>>>>>>>>>> " +
				// // sentence.getId());
				// if (getSingleTokenICDCode(searchToken.toLowerCase()) != null) {
				// writeOutput(sentence, getSingleTokenICDCode(searchToken.toLowerCase()),
				// searchToken);
				// } else {
				// if (compareFromTokenToken(sentence, icd)) {
				// if (!negation(sentence, icd, searchToken)) {
				// if (!existingCode(sentence, icd, searchToken)) {
				// if (!prepositionMatches(sentence, icd, searchToken)) {
				// algorithmStep3(sentence, icd, searchToken);
				// }
				// }
				// }
				// }
				// }
				// }

				for (SentenceDiscovery sentence : sentenceSentences) {
					System.out.println("Iterating sentence >>>>>>>>>>>>>>>>>>>>>>>>>>> " + sentence.getId());
					prepositionMatches(sentence, icd, searchToken);
					// codeValidationTokenToken(sentence, icd, searchToken);
				}
			}
		}
		// System.out.println("==================== FINAL OUTPUT (" + searchToken + ")
		// ====================");
		// outputSet.stream().forEach(System.out::println);
		catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("icdQueryOutputList.size() : " + icdQueryOutputList.size());
		if (icdQueryOutputList.size() > noOfJSONObjectsInOutputFile) {
			JSONHelper.writeToFile(icdQueryOutputList, outputDirectoryPath
					+ Utility.getProperty(Constants.QUERY_OUTPUT_FILENAME_PREFIX) + fileCounter++ + ".json");
			icdQueryOutputList = new ArrayList<>();
		}
	}

	private boolean negation(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence, String searchToken) {
		boolean negatedFound = false;
		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {
			if (!recommendedTokenRelationship.getTokenRelationship().getEdgeName()
					.equals(WordEmbeddingTypes.tokenToken))
				continue;

			// System.out.println("recommendedTokenRelationship.getTokenRelationship().getEdgeName()
			// : "
			// + recommendedTokenRelationship.getTokenRelationship().getEdgeName());
			for (RecommendedTokenRelationship tokenRelationship : sentenceDiscovery.getWordEmbeddings()) {

				// Added this if condition for ticket EC-397
				System.out.println("negation() : " + tokenRelationship.getTokenRelationship().getEdgeName() + " | "
						+ tokenRelationship.getTokenRelationship().getFromToken().getToken() + " | "
						+ tokenRelationship.getTokenRelationship().getToToken().getToken());
				if (tokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.tokenToken)
						&& (negationSpecialCharsSet
								.contains(tokenRelationship.getTokenRelationship().getFromToken().getToken())
								|| negationSpecialCharsSet
										.contains(tokenRelationship.getTokenRelationship().getToToken().getToken())))
					return negatedFound;// returning false;

				if (tokenRelationship.getTokenRelationship().getEdgeName().equals("negation")
						&& recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken()
								.equals(tokenRelationship.getTokenRelationship().getToToken().getToken())) {
					writeOutput(sentenceDiscovery, NEGATED, searchToken);
					negatedFound = true;
					return negatedFound;
				}
			}
		}
		return negatedFound;// returning false;
	}

	private boolean existingCode(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence,
			String searchToken) {
		boolean existingCodeFound = false;
		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {
			if (!recommendedTokenRelationship.getTokenRelationship().getEdgeName()
					.equals(WordEmbeddingTypes.tokenToken))
				continue;

			// System.out
			// .println("existingCode()::::::
			// recommendedTokenRelationship.getTokenRelationship().getEdgeName() : "
			// + recommendedTokenRelationship.getTokenRelationship().getEdgeName());

			if (recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken().equals(searchToken)) {
				// System.out.println("recommendedTokenRelationship.getTokenRelationship().getToToken().getToken()
				// : "
				// +
				// recommendedTokenRelationship.getTokenRelationship().getToToken().getToken());

				System.out.println("existingCode() : "
						+ recommendedTokenRelationship.getTokenRelationship().getToToken().getToken() + " | "
						+ isICDCodePresent(
								recommendedTokenRelationship.getTokenRelationship().getToToken().getToken()));

				// added below if condition for ticket EC-396
				if (isICDCodePresent(recommendedTokenRelationship.getTokenRelationship().getToToken().getToken())) {
					writeOutput(sentenceDiscovery, EXISTING_CODE, searchToken);
					existingCodeFound = true;
					return existingCodeFound;
				}
			}
		}
		return existingCodeFound;
	}

	private boolean prepositionMatches(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence,
			String searchToken) {
		boolean prepositionMatchesFound = false;
		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {

			if (recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken().equals(searchToken)
					&& recommendedTokenRelationship.getTokenRelationship().getEdgeName()
							.equals(WordEmbeddingTypes.prepMinus)) {

				// System.out.println("prepositionMatches(): Matched >> "
				// + recommendedTokenRelationship.getTokenRelationship().getEdgeName());

				for (RecommendedTokenRelationship tokenRelationship : sentenceDiscovery.getWordEmbeddings()) {

					// System.out.println("prepositionMatches(): 1 >> "
					// + tokenRelationship.getTokenRelationship().getToToken().getToken() + " | "
					// + tokenRelationship.getTokenRelationship().getEdgeName());
					if (!tokenRelationship.getTokenRelationship().getToToken().getToken().equals(searchToken)
							&& tokenRelationship.getTokenRelationship().getEdgeName()
									.equals(WordEmbeddingTypes.tokenToken)) {

						String toTokenInPrepMinusOne = null;
						String fromTokenInTokenToken = null;

						for (RecommendedTokenRelationship relationship : sentenceDiscovery.getWordEmbeddings()) {

							// System.out.println(
							// "prepositionMatches(): 2 >> " +
							// relationship.getTokenRelationship().getEdgeName());
							if (relationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.prepPlus)) {
								toTokenInPrepMinusOne = relationship.getTokenRelationship().getToToken().getToken();
							}

							if (relationship.getTokenRelationship().getEdgeName()
									.equals(WordEmbeddingTypes.tokenToken)) {
								fromTokenInTokenToken = relationship.getTokenRelationship().getFromToken().getToken();
							}
						}
						// System.out.println(
						// "prepositionMatches(): 3 >> " + fromTokenInTokenToken + " | " +
						// toTokenInPrepMinusOne);
						if (fromTokenInTokenToken != null && toTokenInPrepMinusOne != null
								&& !fromTokenInTokenToken.equals(toTokenInPrepMinusOne)) {

							if (comparePrepPlusMinus(sentenceDiscovery, icdSentence, searchToken)) {
								writeOutput(sentenceDiscovery, findICDCode(icdSentence), searchToken);
								prepositionMatchesFound = true;
								System.out.println("prepositionMatches(): returning : " + prepositionMatchesFound);
								return prepositionMatchesFound;// returning true;
							}
						}
					}
				}
			}
		}
		System.out.println("prepositionMatches(): returning : " + prepositionMatchesFound);
		return prepositionMatchesFound;// returning false;
	}

	private boolean codeValidationTokenToken(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence,
			String searchToken) {
		boolean foundMatch = false;
		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {
			System.out.println(" recommendedTokenRelationship :: "
					+ recommendedTokenRelationship.getTokenRelationship().getEdgeName() + " | "
					+ recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken() + " | "
					+ recommendedTokenRelationship.getTokenRelationship().getToToken().getToken());
			if (recommendedTokenRelationship.getTokenRelationship().getEdgeName().trim()
					.equals(WordEmbeddingTypes.tokenToken)
					&& recommendedTokenRelationship.getTokenRelationship().getToToken().getToken().trim()
							.equals(searchToken.trim())) {
				String sentenceFromToken = recommendedTokenRelationship.getTokenRelationship().getFromToken()
						.getToken();

				for (RecommendedTokenRelationship icdRecommendedTokenRelationship : icdSentence.getWordEmbeddings()) {
					System.out.println(" icdRecommendedTokenRelationship :: "
							+ icdRecommendedTokenRelationship.getTokenRelationship().getEdgeName() + " | "
							+ icdRecommendedTokenRelationship.getTokenRelationship().getFromToken().getToken() + " | "
							+ icdRecommendedTokenRelationship.getTokenRelationship().getToToken().getToken());

					if (icdRecommendedTokenRelationship.getTokenRelationship().getEdgeName()
							.equals(WordEmbeddingTypes.tokenToken)
							&& icdRecommendedTokenRelationship.getTokenRelationship().getToToken().getToken()
									.equals(searchToken))
						if (icdRecommendedTokenRelationship.getTokenRelationship().getFromToken().getToken()
								.equals(sentenceFromToken)) {
							writeOutput(sentenceDiscovery, findICDCode(icdSentence), searchToken);
							foundMatch = true;
							// System.out.println("codeValidationTokenToken(): returning : " + foundMatch);
							return foundMatch;// returning true;
						}
				}
			}
		}
		return foundMatch;
	}

	private boolean comparePrepPlusMinus(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence,
			String searchToken) {
		// fromtoken in prep-1 in icd should match fromtoken in prep-1 in discoveries
		// totoken in prep+1 in icd should match totoken in prep+1 in discoveries
		//
		// icd collection
		// city of chattanooga
		// prep-1 = city, formtoken, 1
		// prep-1 = of, totoken, 2
		//
		// prep+1= of, fromtoken, 2
		// prep+1 = chattanooga, totoken, 3
		boolean foundMatch = false;
		TokenRelationship prepMinusTokenRelationship = null, friendOfPrepMinusTokenRelationship = null,
				icdPrepMinusTokenRelationship = null, icdFriendOfPrepMinusTokenRelationship = null;
		HashSet<String> edgeNames = null;
		FriendOfFriendServiceImpl friendOfFriendServiceImpl = new FriendOfFriendServiceImpl();
		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {

			System.out.println(" recommendedTokenRelationship :: "
					+ recommendedTokenRelationship.getTokenRelationship().getEdgeName() + " | "
					+ recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken() + " | "
					+ recommendedTokenRelationship.getTokenRelationship().getToToken().getToken());

			if (recommendedTokenRelationship.getTokenRelationship().getEdgeName().trim()
					.equals(WordEmbeddingTypes.prepMinus)
					&& recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken().trim()
							.equals(searchToken.trim())) {

				edgeNames = new HashSet<>();
				prepMinusTokenRelationship = recommendedTokenRelationship.getTokenRelationship();
				edgeNames.add(WordEmbeddingTypes.prepPlus);
				ShouldMatchOnSentenceEdgesResult sentenceEdgesResult = friendOfFriendServiceImpl
						.findFriendOfFriendEdges(getTokenRelationships(sentenceDiscovery.getWordEmbeddings()),
								prepMinusTokenRelationship.getToToken().getToken(),
								recommendedTokenRelationship.getTokenRelationship(), edgeNames);
				if (sentenceEdgesResult == null)
					continue;
				else {
					friendOfPrepMinusTokenRelationship = sentenceEdgesResult.getRelationship();

					System.out.println(" Discoveries :: " + prepMinusTokenRelationship.getEdgeName() + " | "
							+ prepMinusTokenRelationship.getFromToken().getToken() + " | "
							+ prepMinusTokenRelationship.getToToken().getToken() + " ||||| "
							+ friendOfPrepMinusTokenRelationship.getEdgeName() + " | "
							+ friendOfPrepMinusTokenRelationship.getFromToken().getToken() + " | "
							+ friendOfPrepMinusTokenRelationship.getToToken().getToken());

					edgeNames = new HashSet<>();
					for (RecommendedTokenRelationship icdRecommendedTokenRelationship : icdSentence
							.getWordEmbeddings()) {

						System.out.println(" icdRecommendedTokenRelationship :: "
								+ icdRecommendedTokenRelationship.getTokenRelationship().getEdgeName() + " | "
								+ icdRecommendedTokenRelationship.getTokenRelationship().getFromToken().getToken()
								+ " | "
								+ icdRecommendedTokenRelationship.getTokenRelationship().getToToken().getToken());

						if (icdRecommendedTokenRelationship.getTokenRelationship().getEdgeName()
								.equals(WordEmbeddingTypes.prepMinus)
								&& icdRecommendedTokenRelationship.getTokenRelationship().getFromToken().getToken()
										.equals(searchToken)) {

							icdPrepMinusTokenRelationship = icdRecommendedTokenRelationship.getTokenRelationship();
							edgeNames.add(WordEmbeddingTypes.prepPlus);
							ShouldMatchOnSentenceEdgesResult icdEdgesResult = friendOfFriendServiceImpl
									.findFriendOfFriendEdges(getTokenRelationships(icdSentence.getWordEmbeddings()),
											icdPrepMinusTokenRelationship.getToToken().getToken(),
											icdRecommendedTokenRelationship.getTokenRelationship(), edgeNames);
							if (icdEdgesResult == null)
								continue;
							else {
								icdFriendOfPrepMinusTokenRelationship = icdEdgesResult.getRelationship();
								System.out.println(" ICD :: " + icdPrepMinusTokenRelationship.getEdgeName() + " | "
										+ icdPrepMinusTokenRelationship.getFromToken().getToken() + " | "
										+ icdPrepMinusTokenRelationship.getToToken().getToken() + " ||||| "
										+ icdFriendOfPrepMinusTokenRelationship.getEdgeName() + " | "
										+ icdFriendOfPrepMinusTokenRelationship.getFromToken().getToken() + " | "
										+ icdFriendOfPrepMinusTokenRelationship.getToToken().getToken());

								if (prepMinusTokenRelationship.getFromToken().getToken()
										.equals(icdPrepMinusTokenRelationship.getFromToken().getToken())
										&& friendOfPrepMinusTokenRelationship.getToToken().getToken().equals(
												icdFriendOfPrepMinusTokenRelationship.getToToken().getToken())) {
									foundMatch = true;
									System.out.println("comparePrepPlusMinus() : Returning " + foundMatch);
									return foundMatch;
								}

							}
						}
					}

				}
			}
		}
		return foundMatch;
	}

	private List<TokenRelationship> getTokenRelationships(List<RecommendedTokenRelationship> wordEmbeddings) {
		List<TokenRelationship> relationships = new ArrayList<>();
		wordEmbeddings.forEach(recommendedTokenRelationship -> {
			relationships.add(recommendedTokenRelationship.getTokenRelationship());
		});
		return relationships;
	}

	private boolean compareWordEmbeddings(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence,
			String searchToken) {

		// fromtoken in prep-1 in icd should match fromtoken in prep-1 in discoveries
		// totoken in prep+1 in icd should match totoken in prep+1 in discoveries
		//
		// icd collection
		// city of chattanooga
		// prep-1 = city, formtoken, 1
		// prep-1 = of, totoken, 2
		//
		// prep+1= of, fromtoken, 2
		// prep+1 = chattanooga, totoken, 3

		boolean foundMatch = false;
		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {
			if (recommendedTokenRelationship.getTokenRelationship().getEdgeName()
					.equals(WordEmbeddingTypes.prepMinus)) {

				for (RecommendedTokenRelationship icdTokenRelationship : icdSentence.getWordEmbeddings()) {
					if (icdTokenRelationship.getTokenRelationship().getEdgeName()
							.equals(WordEmbeddingTypes.prepMinus)) {

						if (recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken()
								.equals(icdTokenRelationship.getTokenRelationship().getFromToken().getToken())) {

							int prepMinusToTokenIdx = icdTokenRelationship.getTokenRelationship().getToToken()
									.getPosition();
							String prepMinusToToken = icdTokenRelationship.getTokenRelationship().getToToken()
									.getToken();

							for (RecommendedTokenRelationship icdRelationship : icdSentence.getWordEmbeddings()) {
								if (icdRelationship.getTokenRelationship().getEdgeName()
										.equals(WordEmbeddingTypes.prepPlus)
										&& icdRelationship.getTokenRelationship().getFromToken().getToken()
												.equals(prepMinusToToken)
										&& icdRelationship.getTokenRelationship().getFromToken()
												.getPosition() == prepMinusToTokenIdx) {

									for (RecommendedTokenRelationship recommendedRelationship : sentenceDiscovery
											.getWordEmbeddings()) {
										if (recommendedRelationship.getTokenRelationship().getToToken().getToken()
												.equals(icdRelationship.getTokenRelationship().getToToken()
														.getToken())) {
											foundMatch = true;
											System.out.println("compareWordEmbeddings() : returning " + foundMatch);
											return foundMatch;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return foundMatch;

	}

	private void algorithmStep3(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence,
			String searchToken) {

		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {

			// System.out.println("recommendedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge)
			// : " +
			// recommendedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge));
			if (recommendedTokenRelationship.getTokenRelationship().getToToken().getToken().equals(searchToken)
					&& recommendedTokenRelationship.getTokenRelationship().getEdgeName()
							.equals(WordEmbeddingTypes.tokenToken)) {

				// iterate the wordEmbedding again to find token in another wordEmbedding where
				// fromToken.token = toToken.token in another wordEmbedding
				for (RecommendedTokenRelationship tokenRelationship : sentenceDiscovery.getWordEmbeddings()) {
					if (recommendedTokenRelationship == tokenRelationship) {
						// System.out.println("Found same relationship object,so ignoring . . .");
						continue;
					}
					// System.out.println("Token (from/to) : "
					// +
					// recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken()
					// + "("
					// +
					// +recommendedTokenRelationship.getTokenRelationship().getFromToken().getPosition()
					// + ")/"
					// + tokenRelationship.getTokenRelationship().getToToken().getToken() + "("
					// + tokenRelationship.getTokenRelationship().getToToken().getPosition() + ")");
					// System.out.println("Token (from/to) : "
					// +
					// recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken()
					// + "/"
					// + tokenRelationship.getTokenRelationship().getToToken().getToken());

					if (recommendedTokenRelationship.getTokenRelationship().getFromToken()
							.getPosition() == tokenRelationship.getTokenRelationship().getToToken().getPosition()
							&& recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken()
									.equals(tokenRelationship.getTokenRelationship().getToToken().getToken())) {

						// System.out.println("recommendedTokenRelationship.getTokenRelationship() : "
						// + recommendedTokenRelationship.getTokenRelationship());
						// System.out.println(" tokenRelationship.getTokenRelationship() : "
						// + tokenRelationship.getTokenRelationship());
						writeOutput(sentenceDiscovery, findICDCode(icdSentence), searchToken);
					}
				}
			}
		}
	}

	private boolean compareFromTokenToken(SentenceDiscovery sentence, SentenceDiscovery icd) {
		boolean isMatch = false;
		// iterate sentence word embeddings
		for (RecommendedTokenRelationship sentenceRecommendedTokenRelationship : sentence.getWordEmbeddings()) {
			String sentenceFromToken = sentenceRecommendedTokenRelationship.getTokenRelationship().getFromToken()
					.getToken();
			String sentenceEdgeName = sentenceRecommendedTokenRelationship.getTokenRelationship().getEdgeName();
			// make sure that the edgeName is token-token otherwise continue for the next
			// iteration in the loop
			if (!sentenceEdgeName.equalsIgnoreCase(WordEmbeddingTypes.tokenToken)) {
				// System.out.println("if >>>> " + sentenceEdgeName);
				continue;
			}
			// else
			// System.out.println("else >>>>> "+sentenceEdgeName);
			// System.out.println("sentenceFromToken : " + sentenceFromToken);
			// iterate icd word embeddings
			for (RecommendedTokenRelationship icdRecommendedTokenRelationship : icd.getWordEmbeddings()) {
				String icdFromToken = icdRecommendedTokenRelationship.getTokenRelationship().getFromToken().getToken();
				// System.out.println("icdFromToken : " + icdFromToken);
				if (sentenceFromToken.trim().equalsIgnoreCase(icdFromToken)) {
					isMatch = true;
					// System.out.println("Found same fromToken : '" + sentenceFromToken + "' : [
					// sentenceDiscoveries : "
					// + sentence.getId() + ", icd : " + icd.getId() + " ]");
					return isMatch;
				}
			}
		}
		return isMatch;
	}

	private void writeOutput(SentenceDiscovery sentenceDiscovery, String icdCode, String searchToken) {
		StringBuffer tempBuffer = new StringBuffer();
		tempBuffer.append("SentenceDiscovery Object ID : ");
		tempBuffer.append(sentenceDiscovery.getId());
		tempBuffer.append(", Document ID : ");
		tempBuffer.append(sentenceDiscovery.getDiscreteData().getPatientAccount());
		tempBuffer.append(", Search Token : ");
		tempBuffer.append(searchToken);
		tempBuffer.append(", ICD Code : ");
		tempBuffer.append(icdCode);
		tempBuffer.append(", Normalized Sentence : ");
		tempBuffer.append(sentenceDiscovery.getNormalizedSentence());
		tempBuffer.append("\n");
		if (outputSet.add(tempBuffer.toString())) {
			ICDQueryOutput icdQueryOutput = new ICDQueryOutput(sentenceDiscovery.getId().toString(),
					sentenceDiscovery.getDiscreteData().getPatientAccount(), searchToken, icdCode,
					sentenceDiscovery.getNormalizedSentence());
			icdQueryOutputList.add(icdQueryOutput);
		}
		outputBuffer.append(tempBuffer.toString());
	}

	private String findICDCode(SentenceDiscovery icdSentence) {
		String code = "Code Not Found";
		for (RecommendedTokenRelationship tokenRelationship : icdSentence.getWordEmbeddings()) {
			if (tokenRelationship.getTokenRelationship().getEdgeName().equals("has-icd"))
				code = tokenRelationship.getTokenRelationship().getToToken().getToken();
		}
		return code;
	}

	//////////////////////////////////////////////////// CODE IN USE ENDS
	//////////////////////////////////////////////////// HERE///////////////////////////////////////////////////////

	private List<ICD> queryICDSentencesByToken() {
		// create the query parameters
		SentenceQueryInstance sentenceQueryInstance = new SentenceQueryInstance();
		List<String> tokens = new ArrayList<>();
		tokens.add("cyst");
		sentenceQueryInstance.setTokens(tokens);

		// set edges
		List<EdgeQuery> edges = new ArrayList<>();
		sentenceQueryInstance.setEdges(edges);
		EdgeQuery edge = null;
		edge = new EdgeQuery();
		edge.setName("existence");
		edges.add(edge);

		// String[] edgeNames = { EdgeNames.unitOfMeasure, EdgeNames.measurement,
		// EdgeNames.existence, EdgeNames.negation,
		// EdgeNames.possibility, EdgeNames.existenceNo, EdgeNames.existencePossibility,
		// EdgeNames.existenceMaybe,
		// EdgeNames.suppcare, EdgeNames.time, EdgeNames.simpleCystModifiers,
		// EdgeNames.simpleCystModifier,
		// EdgeNames.diseaseModifier, EdgeNames.diseaseLocation,
		// EdgeNames.hetrogeneous_finding_sites,
		// EdgeNames.enlarged_finding_sites, EdgeNames.hasICD, "token-token", "measure",
		// "laterality", "negation",
		// "disease modifier", "unit of measure", "simple cyst modifiers", "existence"
		// };
		//
		// for (String edgeName : edgeNames) {
		// edge = new EdgeQuery();
		// edge.setValues(new HashSet<String>());
		// edge.setName(edgeName);
		// edges.add(edge);
		// }

		List<SentenceQueryInstance> sentenceQueryInstances = new ArrayList<>();
		sentenceQueryInstances.add(sentenceQueryInstance);

		SentenceQueryInput input = new SentenceQueryInput();
		input.setSentenceQueryInstances(sentenceQueryInstances);
		input.setOrganizationId("5972aedebde4270bc53b23e3");

		// query the database
		List<ICD> results = sentenceDiscoveryProcessorImpl.getICDSentences(input);

		System.out.println("queryICDSentencesByToken() - results.size() : " + results.size());

		// for (SentenceDiscovery result : results) {
		// System.out.println(result.getId());
		// }
		return results;
	}

	private List<SentenceDiscoveries> querySentencesByToken() {
		// create the query parameters
		SentenceQueryInstance sentenceQueryInstance = new SentenceQueryInstance();
		List<String> tokens = new ArrayList<>();
		// tokens.add("neoplasm");
		// tokens.add("token-token");
		// tokens.add("mass");
		// tokens.add("regular");
		// tokens.add("and");
		// tokens.add("fever");
		tokens.add("cyst");
		sentenceQueryInstance.setTokens(tokens);

		// set edges
		List<EdgeQuery> edges = new ArrayList<>();
		sentenceQueryInstance.setEdges(edges);
		EdgeQuery edge = null;
		edge = new EdgeQuery();
		edge.setName("existence");
		edges.add(edge);

		// String [] edgeNames =
		// {EdgeNames.unitOfMeasure,EdgeNames.measurement,EdgeNames.existence,EdgeNames.negation,EdgeNames.possibility,EdgeNames.existenceNo,EdgeNames.existencePossibility,
		// EdgeNames.existenceMaybe,EdgeNames.suppcare,EdgeNames.time,EdgeNames.simpleCystModifiers,EdgeNames.simpleCystModifier,EdgeNames.diseaseModifier,EdgeNames.diseaseLocation,
		// EdgeNames.hetrogeneous_finding_sites,EdgeNames.enlarged_finding_sites,EdgeNames.hasICD,"token-token","measure","laterality","negation","disease
		// modifier","unit of measure","simple cyst modifiers","existence"};
		//
		// for (String edgeName:edgeNames)
		// {
		// edge = new EdgeQuery();
		// edge.setValues(new HashSet<String>());
		// edge.setName(edgeName);
		// edges.add(edge);
		// }

		List<SentenceQueryInstance> sentenceQueryInstances = new ArrayList<>();
		sentenceQueryInstances.add(sentenceQueryInstance);

		SentenceQueryInput input = new SentenceQueryInput();
		input.setSentenceQueryInstances(sentenceQueryInstances);
		input.setOrganizationId("5972aedebde4270bc53b23e3");

		// query the database
		List<SentenceDiscoveries> results = sentenceDiscoveryProcessorImpl.getSentences(input);

		System.out.println("querySentencesByToken() - results.size() : " + results.size());

		// for (SentenceDiscovery result : results) {
		// System.out.println(result.getId());
		// }
		return results;
	}

	private void algorithm(List<SentenceDiscoveries> sentencesByToken, List<ICD> icdSentencesByToken) {
		System.out.println("-----------------algorithm()---------------");

		// for (SentenceDiscovery result : sentencesByToken) {
		// System.out.println(result.getId());
		// }
		// for (SentenceDiscovery result : icdSentencesByToken) {
		// System.out.println(result.getId());
		// }

		// iterate each sentences
		for (SentenceDiscovery sentenceDiscoveries : sentencesByToken) {
			// System.out.println("sentenceDiscoveries : " + sentenceDiscoveries.getId());
			List<RecommendedTokenRelationship> sentenceWordEmbeddings = sentenceDiscoveries.getWordEmbeddings();

			// iterate sentence word embeddings
			for (RecommendedTokenRelationship sentenceRecommendedTokenRelationship : sentenceWordEmbeddings) {
				String sentenceFromToken = sentenceRecommendedTokenRelationship.getTokenRelationship().getFromToken()
						.getToken();
				String sentenceEdgeName = sentenceRecommendedTokenRelationship.getTokenRelationship().getEdgeName();
				// make sure that the edgeName is token-token otherwise continue for the next
				// iteration in the loop
				if (!sentenceEdgeName.equalsIgnoreCase(WordEmbeddingTypes.tokenToken)) {
					// System.out.println("if >>>> " + sentenceEdgeName);
					continue;
				}
				// else
				// System.out.println("else >>>>> "+sentenceEdgeName);
				System.out.println("sentenceFromToken : " + sentenceFromToken);
				// iterate each icd sentences
				for (SentenceDiscovery icd : icdSentencesByToken) {
					// System.out.println("icd : " + icd.getId());
					List<RecommendedTokenRelationship> icdWordEmbeddings = icd.getWordEmbeddings();

					// iterate icd word embeddings
					for (RecommendedTokenRelationship icdRecommendedTokenRelationship : icdWordEmbeddings) {
						String icdFromToken = icdRecommendedTokenRelationship.getTokenRelationship().getFromToken()
								.getToken();
						// String icdEdgeName =
						// icdRecommendedTokenRelationship.getTokenRelationship().getEdgeName();

						// System.out.println("icdFromToken : " + icdFromToken);
						if (sentenceFromToken.trim().equalsIgnoreCase(icdFromToken)) {
							System.out.println(
									"found same fromToken : '" + sentenceFromToken + "' : [ sentenceDiscoveries : "
											+ sentenceDiscoveries.getId() + ", icd : " + icd.getId() + " ]");
						}
					}

				}

			}

		}
	}

	public static boolean isICDCodePresent(String code) {
		return onlyICDCodesSet.contains(code);
	}

	public static void loadOnlyICDCodes() {
		System.out.println("onlyICDCodesPath : " + onlyICDCodesPath);
		try (Stream<String> stream = Files.lines(Paths.get(onlyICDCodesPath))) {
			onlyICDCodesSet = stream.map(String::toLowerCase).collect(Collectors.toCollection(HashSet::new));
			// System.out.println(onlyICDCodesSet.size());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public static String getSingleTokenICDCode(String token) {
		return singleTokenICDCodesMap.get(token);
	}

	public static void loadSingleTokenICDCodes() {
		singleTokenICDCodesMap = new HashMap<>();
		try (Stream<String> stream = Files.lines(Paths.get(singleTokenICDCodesPath))) {
			stream.forEach(line -> {
				singleTokenICDCodesMap.put(line.substring(line.indexOf(",") + 1).toLowerCase(),
						line.substring(0, line.indexOf(",")));
			});
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	private void algorithmStep5(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence,
			String searchToken) {

		String oppositeToken = "";
		String fromToken = "";
		String SEPARATOR = " ";

		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {

			// System.out.println("recommendedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge)
			// : " +
			// recommendedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge));
			if (recommendedTokenRelationship.getTokenRelationship().getToToken().getToken().equals(searchToken)
					&& recommendedTokenRelationship.getTokenRelationship().getEdgeName()
							.equals(WordEmbeddingTypes.tokenToken)) {

				oppositeToken = recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken();
				fromToken = recommendedTokenRelationship.getTokenRelationship().getToToken().getToken();

				// iterate the wordEmbedding again to find token in another wordEmbedding where
				// fromToken.token = toToken.token in another wordEmbedding
				for (RecommendedTokenRelationship tokenRelationship : sentenceDiscovery.getWordEmbeddings()) {
					// System.out.println("Matched Sentence : " + matchedTokens.toString());

					if (recommendedTokenRelationship == tokenRelationship) {
						// System.out.println("Found same relationship object,so ignoring . . .");
						continue;
					}

					if (!recommendedTokenRelationship.getTokenRelationship().getEdgeName()
							.equals(WordEmbeddingTypes.tokenToken)) {
						continue;
					}

					// System.out.println("tokenRelationship.getIsVerified() : " +
					// tokenRelationship.getIsVerified());

					if (!tokenRelationship.getIsVerified()) {
						System.out.println("tokenRelationship.getTokenRelationship().getToToken().getToken() : "
								+ tokenRelationship.getTokenRelationship().getToToken().getToken());
						System.out.println("oppositeToken : " + oppositeToken);
						if (oppositeToken.equals(tokenRelationship.getTokenRelationship().getToToken().getToken())) {
							oppositeToken = tokenRelationship.getTokenRelationship().getFromToken().getToken();
						}

					}
				}

				boolean edgeMatch = true;
				boolean allEdgesMatch = true;

				for (RecommendedTokenRelationship icdTokenRelationship : icdSentence.getWordEmbeddings()) {

					edgeMatch = true;
					if (!icdTokenRelationship.getTokenRelationship().getEdgeName()
							.equals(WordEmbeddingTypes.tokenToken))
						continue;

					for (RecommendedTokenRelationship sentenceTokenRelationship : sentenceDiscovery
							.getWordEmbeddings()) {
						if (!sentenceTokenRelationship.getTokenRelationship().getEdgeName()
								.equals(WordEmbeddingTypes.tokenToken))
							continue;

						if (!((icdTokenRelationship.getTokenRelationship().getFromToken().getToken()
								.equals(sentenceTokenRelationship.getTokenRelationship().getFromToken().getToken()))
								&& (icdTokenRelationship.getTokenRelationship().getToToken().getToken().equals(
										sentenceTokenRelationship.getTokenRelationship().getToToken().getToken())))) {
							edgeMatch = false;
							break;
						}
					}
					if (!edgeMatch) {
						allEdgesMatch = false;
						break;
					}
				}
				if (allEdgesMatch) {
					writeOutput(sentenceDiscovery, findICDCode(icdSentence), searchToken);
					return;
				} else {
					writeOutput(sentenceDiscovery, "No Code", searchToken);
					return;
				}
			}
		}
	}

	private void algorithmStep2(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence,
			String searchToken) {
		// System.out.println("algorithmStep2() : " + sentenceDiscovery.getId());
		FriendOfFriendService friendOfFriendService = new FriendOfFriendServiceImpl();
		List<TokenRelationship> relationships = new ArrayList<>();
		HashSet<String> edgeNames = new HashSet<>();
		ShouldMatchOnSentenceEdgesResult shouldMatchOnSentenceEdgesResult = null;
		// iterate the SentenceDiscovery WordEmbeddings collections for
		// TokenRelationship.
		sentenceDiscovery.getWordEmbeddings().forEach(obj -> relationships.add(obj.getTokenRelationship()));

		sentenceDiscovery.getWordEmbeddings().forEach(obj -> edgeNames.add(obj.getTokenRelationship().getEdgeName()));

		System.out.println(
				"sentenceDiscovery.getWordEmbeddings().size() : " + sentenceDiscovery.getWordEmbeddings().size());

		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {
			shouldMatchOnSentenceEdgesResult = friendOfFriendService.findFriendOfFriendEdges(relationships, searchToken,
					recommendedTokenRelationship.getTokenRelationship(), edgeNames);
			System.out.println(shouldMatchOnSentenceEdgesResult);
			if (shouldMatchOnSentenceEdgesResult == null)
				continue;
			if (shouldMatchOnSentenceEdgesResult.isMatch()) {
				System.out.println(
						"shouldMatchOnSentenceEdgesResult.getRelationship().getFromToken().getToken().equals(token) : "
								+ shouldMatchOnSentenceEdgesResult.getRelationship().getFromToken().getToken()
										.equals(searchToken));
				// token is a fromToken
				// if(shouldMatchOnSentenceEdgesResult.getRelationship().getFromToken().getToken().equals(token))
				// {
				for (RecommendedTokenRelationship tokenRelationship : sentenceDiscovery.getWordEmbeddings()) {

					if (tokenRelationship.getTokenRelationship().getToToken().getToken().equals(searchToken))
						System.out.println(tokenRelationship.getTokenRelationship().getToToken().getToken() + "//"
								+ searchToken + "//"
								+ tokenRelationship.getTokenRelationship().getToToken().getPosition() + "//"
								+ shouldMatchOnSentenceEdgesResult.getRelationship().getFromToken().getPosition());
					if (tokenRelationship.getTokenRelationship().getToToken().getToken().equals(searchToken)
							&& tokenRelationship.getTokenRelationship().getToToken()
									.getPosition() == shouldMatchOnSentenceEdgesResult.getRelationship().getFromToken()
											.getPosition()) {
						System.out.println("Found Same Index Opposite (toToken) Token....");
						writeOutput(sentenceDiscovery, findICDCode(icdSentence), searchToken);
					}
				}
			} else {
				System.out.println("-------------------NOT MATCHING-------------------");
			}
		}
	}

	private boolean negation_LastWorkingOn6_15(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence,
			String searchToken) {
		boolean negatedFound = false;
		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {
			if (!recommendedTokenRelationship.getTokenRelationship().getEdgeName()
					.equals(WordEmbeddingTypes.tokenToken))
				continue;

			// System.out.println("recommendedTokenRelationship.getTokenRelationship().getEdgeName()
			// : "
			// + recommendedTokenRelationship.getTokenRelationship().getEdgeName());
			for (RecommendedTokenRelationship tokenRelationship : sentenceDiscovery.getWordEmbeddings()) {
				if (tokenRelationship.getTokenRelationship().getEdgeName().equals("negation")
						&& recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken()
								.equals(tokenRelationship.getTokenRelationship().getToToken().getToken())) {
					writeOutput(sentenceDiscovery, NEGATED, searchToken);
					negatedFound = true;
					return negatedFound;
				}
			}
		}
		return negatedFound;
	}

	private boolean existingCode_LastWorkingOn6_15(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence,
			String searchToken) {
		boolean existingCodeFound = false;
		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {
			if (!recommendedTokenRelationship.getTokenRelationship().getEdgeName()
					.equals(WordEmbeddingTypes.tokenToken))
				continue;

			// System.out
			// .println("existingCode()::::::
			// recommendedTokenRelationship.getTokenRelationship().getEdgeName() : "
			// + recommendedTokenRelationship.getTokenRelationship().getEdgeName());

			if (recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken().equals(searchToken)) {
				writeOutput(sentenceDiscovery, EXISTING_CODE, searchToken);
				existingCodeFound = true;
				return existingCodeFound;
			}
		}
		return existingCodeFound;
	}

	// @Test
	public void highestValueTokens() {
		List<ICD> icdSentences = sentenceDiscoveryProcessorImpl.getAllICDDocuments();

		System.out.println("icdSentences.size() : " + icdSentences.size());
		List<WordToken> modifiedWordList = null;
		for (SentenceDiscovery icd : icdSentences) {
			modifiedWordList = icd.getModifiedWordList();
			modifiedWordList.sort((WordToken wordToken1, WordToken wordToken2) -> wordToken2.getPosition()
					- wordToken1.getPosition());
			// System.out.println("ICD ID : " +icd.getId() + " Highest Value Token : "
			// +modifiedWordList.get(0));
			executeTest(modifiedWordList.get(0).getToken());
		}
	}
}
