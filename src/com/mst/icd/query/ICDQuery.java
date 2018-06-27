package com.mst.icd.query;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.mst.filter.FriendOfFriendServiceImpl;
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

public class ICDQuery {

	private static Logger logger = Logger.getLogger(ICDQuery.class);

	private SentenceDiscoveryProcessorImpl sentenceDiscoveryProcessorImpl = null;
	private HashSet<String> outputSet = new HashSet<>();
	private List<Object> icdQueryOutputList = new ArrayList<>();
	private final String NEGATED = "NEGATED";
	private final String EXISTING_CODE = "EXISTING CODE";

	private final String ALGO_NEGATION = "negation";
	private final String ALGO_EXISTING_CODE = "existingCode";
	private final String ALGO_PREPOSITION_MATCH = "prepositionMatch";
	private final String ALGO_SINGLE_TOKEN_ICD_CODE = "singleTokenICDCode";
	private final String ALGO_CODE_VALIDATION_TOKEN_TOKEN = "codeValidationTokenToken";
	private final String ALGO_ALL = "all";

	private int fileCounter = 1;
	private HashSet<String> negationSpecialCharsSet = new HashSet<>();
	private HashSet<String> algorithmSet = null;
	private static HashSet<String> onlyICDCodesSet = null;
	private int noOfJSONObjectsInOutputFile = Integer
			.valueOf(Utility.getProperty(Constants.QUERY_NO_OF_OBJECTS_PER_JSON_OUTPUT_FILE));
	private static HashMap<String, String> singleTokenICDCodesMap = null;

	private static String onlyICDCodesPath = Utility.getProperty(Constants.QUERY_ICD_CODE_ONLY_FILE_PATH);
	private static String singleTokenICDCodesPath = Utility
			.getProperty(Constants.QUERY_SINGLE_WORD_ICD_CODES_FILE_PATH);
	private String outputDirectoryPath = Utility.getProperty(Constants.QUERY_OUTPUT_DIRECTORY_PATH);
	private String inputFileName = Utility.getProperty(Constants.QUERY_INPUT_FILE_PATH);

	static {
		loadOnlyICDCodes();
		loadSingleTokenICDCodes();
	}

	public ICDQuery() {
		init();
	}

	public static void main(String s[]) {
		String searchToken = "shortness";
		String orgId = Utility.getProperty(Constants.QUERY_INPUT_ORG_ID);
		String edgeName = WordEmbeddingTypes.tokenToken;
		ICDQuery icdQuery = new ICDQuery();
		// icdQuery.processToken(orgId, edgeName, searchToken);
	}

	private void init() {
		Utility.printAllProperties();
		sentenceDiscoveryProcessorImpl = new SentenceDiscoveryProcessorImpl();
		sentenceDiscoveryProcessorImpl.setMongoDatastoreProvider(new SentenceServiceMongoDatastoreProvider(
				Utility.getProperty(Constants.MONGO_DB_IP), Utility.getProperty(Constants.MONGO_DB_NAME)));

		// load special characters to be ignored
		char chars[] = Utility.getProperty(Constants.QUERY_NEGATION_SPECIAL_CHAR_SET).toCharArray();
		for (char c : chars)
			negationSpecialCharsSet.add(String.valueOf(c));

		// load list of algorithms to be executed.
		algorithmSet = Stream.of(Utility.getProperty(Constants.QUERY_EXECUTION_ALGORITHMS).split(","))
				.collect(Collectors.toCollection(HashSet::new));
		// algorithmSet.stream().forEach(System.out::println);

	}

	public void processTokensFromFile() {
		String orgId = Utility.getProperty(Constants.QUERY_INPUT_ORG_ID);
		String edgeName = WordEmbeddingTypes.tokenToken;

		try (Stream<String> stream = Files.lines(Paths.get(inputFileName))) {
			stream.forEach(searchToken -> processToken(orgId, edgeName, searchToken));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (icdQueryOutputList != null && icdQueryOutputList.size() > 0)
			JSONHelper.writeToFile(icdQueryOutputList, outputDirectoryPath
					+ Utility.getProperty(Constants.QUERY_OUTPUT_FILENAME_PREFIX) + fileCounter++ + ".json");
	}

	public void processToken(String orgId, String edgeName, String searchToken) {

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

			logger.info("No of records received from ICD collection ( Org ID : " + orgId + ", Search Token : "
					+ searchToken + " ) : " + icdSentences.size());

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
				logger.info("Iterating ICD document (" + searchToken + ")(" + icdCounter++ + "/" + icdCollectionSize
						+ "): " + icd.getId());

				sentenceQueryInstance = new SentenceQueryInstance();
				sentenceQueryInput = new SentenceQueryInput();
				sentenceQueryInputTokens = new ArrayList<>();
				sentenceQueryInputEdges = new ArrayList<>();
				sentenceQueryInstances = new ArrayList<>();

				// set search token
				sentenceQueryInputTokens.add(searchToken);

				// set edges
				sentenceEdge = new EdgeQuery();
				sentenceEdge.setName(edgeName);
				sentenceQueryInputEdges.add(sentenceEdge);

				sentenceQueryInstance.setTokens(sentenceQueryInputTokens);
				sentenceQueryInstance.setEdges(sentenceQueryInputEdges);

				sentenceQueryInstances.add(sentenceQueryInstance);
				sentenceQueryInput.setSentenceQueryInstances(sentenceQueryInstances);
				sentenceQueryInput.setOrganizationId(orgId);

				// fire query to sentence discoveries here
				sentenceSentences = sentenceDiscoveryProcessorImpl.getSentences(sentenceQueryInput);
				logger.info("No of records received from SentenceDiscoveriesTest collection ( Org ID : " + orgId
						+ ", Search Token : " + searchToken + ", ICD Code : " + findICDCode(icd) + " ) : "
						+ sentenceSentences.size());

				for (SentenceDiscovery sentence : sentenceSentences) {

					logger.debug("Iterating Sentence Discoveries : " + sentence.getId());

					if (getSingleTokenICDCode(searchToken.toLowerCase()) != null) {
						writeOutput(sentence, getSingleTokenICDCode(searchToken.toLowerCase()), searchToken);
					} else {
						if (compareFromTokenToken(sentence, icd)) {
							if (algorithmSet.contains(ALGO_ALL)) {
								if (!negation(sentence, icd, searchToken)) {
									if (!existingCode(sentence, icd, searchToken)) {
										if (!codeValidationTokenToken(sentence, icd, searchToken)) {
											if (!prepositionMatches(sentence, icd, searchToken)) {
												compareIndex(sentence, icd, searchToken);
											}
										}
									}
								}
							} else {
								if (algorithmSet.contains(ALGO_NEGATION)) {
									negation(sentence, icd, searchToken);
								}
								if (algorithmSet.contains(ALGO_EXISTING_CODE)) {
									existingCode(sentence, icd, searchToken);
								}
								if (algorithmSet.contains(ALGO_CODE_VALIDATION_TOKEN_TOKEN)) {
									codeValidationTokenToken(sentence, icd, searchToken);
								}
								if (algorithmSet.contains(ALGO_PREPOSITION_MATCH)) {
									prepositionMatches(sentence, icd, searchToken);
								}
							}
						}
					}
				}

				// for (SentenceDiscovery sentence : sentenceSentences) {
				// logger.debug("Iterating Sentence Discoveries : " + sentence.getId());
				// if (getSingleTokenICDCode(searchToken.toLowerCase()) != null) {
				// writeOutput(sentence, getSingleTokenICDCode(searchToken.toLowerCase()),
				// searchToken);
				// } else {
				// if (compareFromTokenToken(sentence, icd)) {
				// if (!negation(sentence, icd, searchToken)) {
				// if (!existingCode(sentence, icd, searchToken)) {
				// if (!codeValidationTokenToken(sentence, icd, searchToken)) {
				// if (!prepositionMatches(sentence, icd, searchToken)) {
				// compareIndex(sentence, icd, searchToken);
				// }
				// }
				// }
				// }
				// }
				// }
				// }
			}
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Current size of the query output records : " + icdQueryOutputList.size());
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

			for (RecommendedTokenRelationship tokenRelationship : sentenceDiscovery.getWordEmbeddings()) {

				// Added this if condition for ticket EC-397
				logger.debug("negation() : " + tokenRelationship.getTokenRelationship().getEdgeName() + " | "
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

			if (recommendedTokenRelationship.getTokenRelationship().getFromToken().getToken().equals(searchToken)) {

				logger.debug("existingCode() : "
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

				for (RecommendedTokenRelationship tokenRelationship : sentenceDiscovery.getWordEmbeddings()) {

					if (!tokenRelationship.getTokenRelationship().getToToken().getToken().equals(searchToken)
							&& tokenRelationship.getTokenRelationship().getEdgeName()
									.equals(WordEmbeddingTypes.tokenToken)) {

						String toTokenInPrepMinusOne = null;
						String fromTokenInTokenToken = null;

						for (RecommendedTokenRelationship relationship : sentenceDiscovery.getWordEmbeddings()) {

							if (relationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.prepPlus)) {
								toTokenInPrepMinusOne = relationship.getTokenRelationship().getToToken().getToken();
							}

							if (relationship.getTokenRelationship().getEdgeName()
									.equals(WordEmbeddingTypes.tokenToken)) {
								fromTokenInTokenToken = relationship.getTokenRelationship().getFromToken().getToken();
							}
						}

						if (fromTokenInTokenToken != null && toTokenInPrepMinusOne != null
								&& !fromTokenInTokenToken.equals(toTokenInPrepMinusOne)) {

							if (comparePrepPlusMinus(sentenceDiscovery, icdSentence, searchToken)) {
								writeOutput(sentenceDiscovery, findICDCode(icdSentence), searchToken);
								prepositionMatchesFound = true;
								logger.debug("prepositionMatches(): returning : " + prepositionMatchesFound);
								return prepositionMatchesFound;// returning true;
							}
						}
					}
				}
			}
		}
		logger.debug("prepositionMatches(): returning : " + prepositionMatchesFound);
		return prepositionMatchesFound;// returning false;
	}

	private boolean codeValidationTokenToken(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence,
			String searchToken) {
		boolean foundMatch = false;
		for (RecommendedTokenRelationship recommendedTokenRelationship : sentenceDiscovery.getWordEmbeddings()) {
			logger.debug(" recommendedTokenRelationship :: "
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
					logger.debug(" icdRecommendedTokenRelationship :: "
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

			logger.debug(" recommendedTokenRelationship :: "
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

					logger.debug(" Discoveries :: " + prepMinusTokenRelationship.getEdgeName() + " | "
							+ prepMinusTokenRelationship.getFromToken().getToken() + " | "
							+ prepMinusTokenRelationship.getToToken().getToken() + " <<>> "
							+ friendOfPrepMinusTokenRelationship.getEdgeName() + " | "
							+ friendOfPrepMinusTokenRelationship.getFromToken().getToken() + " | "
							+ friendOfPrepMinusTokenRelationship.getToToken().getToken());

					edgeNames = new HashSet<>();
					for (RecommendedTokenRelationship icdRecommendedTokenRelationship : icdSentence
							.getWordEmbeddings()) {

						logger.debug(" icdRecommendedTokenRelationship :: "
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
								logger.debug(" ICD :: " + icdPrepMinusTokenRelationship.getEdgeName() + " | "
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
									logger.debug("comparePrepPlusMinus() : Returning " + foundMatch);
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

	private void compareIndex(SentenceDiscovery sentenceDiscovery, SentenceDiscovery icdSentence, String searchToken) {

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
			// make sure that the edgeName is token-token otherwise continue for the next
			// iteration in the loop
			if (!sentenceRecommendedTokenRelationship.getTokenRelationship().getEdgeName()
					.equalsIgnoreCase(WordEmbeddingTypes.tokenToken))
				continue;

			// iterate icd word embeddings
			for (RecommendedTokenRelationship icdRecommendedTokenRelationship : icd.getWordEmbeddings()) {
				String icdFromToken = icdRecommendedTokenRelationship.getTokenRelationship().getFromToken().getToken();

				if (sentenceFromToken.trim().equalsIgnoreCase(icdFromToken)) {
					isMatch = true;
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

	}

	private String findICDCode(SentenceDiscovery icdSentence) {
		String code = "Code Not Found";
		for (RecommendedTokenRelationship tokenRelationship : icdSentence.getWordEmbeddings()) {
			if (tokenRelationship.getTokenRelationship().getEdgeName().equals("has-icd"))
				code = tokenRelationship.getTokenRelationship().getToToken().getToken();
		}
		return code;
	}

	public static boolean isICDCodePresent(String code) {
		return onlyICDCodesSet.contains(code);
	}

	public static void loadOnlyICDCodes() {
		try (Stream<String> stream = Files.lines(Paths.get(onlyICDCodesPath))) {
			onlyICDCodesSet = stream.map(String::toLowerCase).collect(Collectors.toCollection(HashSet::new));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
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
			logger.error(e.getMessage());
		}
	}

	private void highestValueTokens() {

		String orgId = Utility.getProperty(Constants.QUERY_INPUT_ORG_ID);
		String edgeName = WordEmbeddingTypes.tokenToken;

		List<ICD> icdSentences = sentenceDiscoveryProcessorImpl.getAllICDDocuments();

		System.out.println("icdSentences.size() : " + icdSentences.size());
		List<WordToken> modifiedWordList = null;
		for (SentenceDiscovery icd : icdSentences) {
			modifiedWordList = icd.getModifiedWordList();
			modifiedWordList.sort((WordToken wordToken1, WordToken wordToken2) -> wordToken2.getPosition()
					- wordToken1.getPosition());
			// System.out.println("ICD ID : " +icd.getId() + " Highest Value Token : "
			// +modifiedWordList.get(0));
			processToken(orgId, edgeName, modifiedWordList.get(0).getToken());
		}
	}

}
