package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.AdditionalExistenceEdgeProcessor;
import com.mst.interfaces.sentenceprocessing.DistinctTokenRelationshipDeterminer;
import com.mst.interfaces.sentenceprocessing.DynamicEdgeCreationProcessor;
import com.mst.interfaces.sentenceprocessing.NegationTokenRelationshipProcessor;
import com.mst.interfaces.sentenceprocessing.NgramsSentenceProcessor;
import com.mst.interfaces.sentenceprocessing.PartOfSpeechAnnotator;
import com.mst.interfaces.sentenceprocessing.RecommendedNegativeRelationshipFactoryImpl;
import com.mst.interfaces.sentenceprocessing.SemanticTypeSentenceAnnotator;
import com.mst.interfaces.sentenceprocessing.SentenceDiscoveryProcessor;
import com.mst.interfaces.sentenceprocessing.MeasurementProcessor;
import com.mst.interfaces.sentenceprocessing.VerbExistanceProcessor;
import com.mst.interfaces.sentenceprocessing.VerbProcessor;
import com.mst.interfaces.sentenceprocessing.WordEmbeddingProcessor;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.metadataTypes.TokenBypassTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.AdditionalExistenceEdgeProcessorImpl;
import com.mst.model.sentenceProcessing.RecommandedNounPhraseResult;
import com.mst.model.sentenceProcessing.RecommandedSubjectAnnotatorImpl;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.WordToken;

public class SentenceDiscoveryProcessorImpl implements SentenceDiscoveryProcessor {
    private SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput;
    private SentenceFactory sentenceFactory;
    private NgramsSentenceProcessor ngramProcessor;
    private PartOfSpeechAnnotator partOfSpeechAnnotator;
    private SemanticTypeSentenceAnnotator stAnnotator;
    private MeasurementProcessor measurementProcessor;
    private WordEmbeddingProcessor wordEmbeddingProcessor;
    private VerbProcessor verbProcessor;
    private RecommendedNounPhraseProcessorImpl recommendedNounPhraseProcessor;
    private RecommendedNegativeRelationshipFactoryImpl negativeRelationshipFactory;
    private VerbExistanceProcessor verbExistanceProcessor;
    private IterationRuleProcesser iterationRuleProcesser;
    private DistinctTokenRelationshipDeterminer distinctTokenRelationshipDeterminer;
    private AdditionalExistenceEdgeProcessor additionalExistenceEdgeProcessor;
    private NegationTokenRelationshipProcessor negationTokenRelationshipProcessor;
    private DynamicEdgeCreationProcessor dynamicEdgeCreationProcessor;
    private RecommendationEdgesVerificationProcessor recommendationEdgesVerificationProcessor;
    private TokenRankProcessor tokenRankProcessor;

    public SentenceDiscoveryProcessorImpl() {
        sentenceFactory = new SentenceFactory();
        ngramProcessor = new NGramsSentenceProcessorImpl();
        partOfSpeechAnnotator = new PartOfSpeechAnnotatorImpl();
        stAnnotator = new SemanticTypeSentenceAnnotatorImpl();
        measurementProcessor = new MeasurementProcessorImpl();
        wordEmbeddingProcessor = new WordEmbeddingProcesseorImpl();
        verbProcessor = new VerbProcessorImpl();
        recommendedNounPhraseProcessor = new RecommendedNounPhraseProcessorImpl();
        new RecommandedSubjectAnnotatorImpl();
        negativeRelationshipFactory = new RecommendedNegativeRelationshipFactoryImpl();
        verbExistanceProcessor = new VerbExistanceProcessorImpl();
        iterationRuleProcesser = new IterationRuleProcesser();
        distinctTokenRelationshipDeterminer = new DistinctTokenRelationshipDeterminerImpl();
        additionalExistenceEdgeProcessor = new AdditionalExistenceEdgeProcessorImpl();
        negationTokenRelationshipProcessor = new NegationTokenRelationshipProcessorImpl();
        dynamicEdgeCreationProcessor = new DynamicEdgeCreationProcessorImpl();
        recommendationEdgesVerificationProcessor = new RecommendationEdgesVerificationProcessor();
        tokenRankProcessor = new TokenRankProcessorImpl();
    }

    public void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput) {
        this.sentenceProcessingMetaDataInput = sentenceProcessingMetaDataInput;
    }

    public List<SentenceDiscovery> process(SentenceTextRequest request) throws Exception {
        List<Sentence> sentences = sentenceFactory.getSentences(request.getText(), "", "", request.getSource());
        List<SentenceDiscovery> discoveries = new ArrayList<>();
        for (Sentence sentence : sentences) {
            sentence = ngramProcessor.process(sentence, sentenceProcessingMetaDataInput.getNgramsInput());
            List<WordToken> tokens = stAnnotator.annotate(sentence.getModifiedWordList(), sentenceProcessingMetaDataInput.getSemanticTypes());
            sentence.setTokenRelationships(new ArrayList<>());
            tokens = partOfSpeechAnnotator.annotate(tokens, sentenceProcessingMetaDataInput.getPartOfSpeechAnnotatorEntity());
            tokens = verbProcessor.process(tokens, sentenceProcessingMetaDataInput.getVerbProcessingInput());
            tokens = filterTokens(tokens);
            List<RecommendedTokenRelationship> wordEmbeddings = wordEmbeddingProcessor.process(tokens);
            RecommandedNounPhraseResult nounPhraseResult = recommendedNounPhraseProcessor.process(wordEmbeddings);
            sentence.setModifiedWordList(tokens);
            SentenceDiscovery discovery = convert(sentence, nounPhraseResult);
            List<RecommendedTokenRelationship> negationRelationships = negationTokenRelationshipProcessor.processDiscovery(tokens);
            discovery.getWordEmbeddings().addAll(negationRelationships);
            discovery.getWordEmbeddings().addAll(recommendedNounPhraseProcessor.addEdges(discovery.getWordEmbeddings(), sentenceProcessingMetaDataInput.getNounRelationshipsInput()));
            discovery.getWordEmbeddings().addAll(negativeRelationshipFactory.create(discovery.getModifiedWordList()));
            discovery.getWordEmbeddings().addAll(iterationRuleProcesser.process(discovery.getWordEmbeddings(), sentenceProcessingMetaDataInput.getIterationRuleProcesserInput()));
            discovery.getWordEmbeddings().addAll(verbExistanceProcessor.processDiscovery(discovery));
            /**
             * calls recommendationEdgesVerificationProcesser and passes the discovery and wordEmbeddings to set the tokenRankings
             */
           // recommendationEdgesVerificationProcessor.process(discovery, discovery.getWordEmbeddings());
            discoveries.add(discovery);
            List<RecommendedTokenRelationship> edges = recommendedNounPhraseProcessor.setNamedEdges(discovery.getWordEmbeddings(), sentenceProcessingMetaDataInput.getNounRelationshipsInput()); //should always be last
            dynamicEdgeCreationProcessor.processDiscovery(sentenceProcessingMetaDataInput.getDynamicEdgeCreationRules(), discovery);
            measurementProcessor.process(tokens, request.isConvertMeasurements());
            edges = distinctTokenRelationshipDeterminer.getDistinctRecommendedRelationships(edges);
            discovery.setWordEmbeddings(edges);
            RecommendedTokenRelationship relationship = additionalExistenceEdgeProcessor.processDiscovery(discovery);
            if (relationship != null)
                discovery.getWordEmbeddings().add(relationship);
            /**
             * calls recommendationEdgesVerificationProcesser and passes the discovery and wordEmbeddings to set the tokenRankings
             */
            tokenRankProcessor.setTokenRankings(discovery);
            //recommendationEdgesVerificationProcessor.process(discovery, discovery.getWordEmbeddings());
            //discoveries.add(discovery);
        }
        return discoveries;
    }

    private List<WordToken> filterTokens(List<WordToken> tokens) {
        List<WordToken> result = new ArrayList<>();
        HashSet<String> byPassPOS = new HashSet<>();
        byPassPOS.add(PartOfSpeachTypes.DET);
        byPassPOS.add(PartOfSpeachTypes.PUNCTUATION);
        HashSet<String> specialCharactersBypass = TokenBypassTypes.values;
        for (WordToken token : tokens) {
            if (token.getPos() != null && byPassPOS.contains(token.getPos()))
                continue;
            if (specialCharactersBypass.contains(token.getToken()))
                continue;
            token.setPosition(result.size()+1);
            result.add(token);
        }
        return result;
    }

    private SentenceDiscovery convert(Sentence sentence, RecommandedNounPhraseResult nounPhraseResult) {
        SentenceDiscovery sentenceDiscovery = new SentenceDiscovery();
        sentenceDiscovery.setWordEmbeddings(nounPhraseResult.getRecommandedTokenRelationships());
        sentenceDiscovery.setNounPhraseIndexes(nounPhraseResult.getNounPhraseIndexes());
        sentenceDiscovery.setModifiedWordList(sentence.getModifiedWordList());
        sentenceDiscovery.setNormalizedSentence(getNormalizeSentenceFromTokens(sentence.getModifiedWordList()));
        sentenceDiscovery.setOriginalWords(sentence.getOriginalWords());
        sentenceDiscovery.setOrigSentence(sentence.getOrigSentence());
        sentenceDiscovery.setProcessingDate(sentence.getProcessDate());
        sentenceDiscovery.setSource(sentence.getSource());
        sentenceDiscovery.setStudy(sentence.getStudy());
        sentenceDiscovery.setPractice(sentence.getPractice());
        return sentenceDiscovery;
    }

    private String getNormalizeSentenceFromTokens(List<WordToken> tokens) {
        StringBuilder result = new StringBuilder();
        for (WordToken token : tokens) {
            result.append(token.getToken()).append(" ");
        }
        return result.toString();
    }
}
