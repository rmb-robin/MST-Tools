package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.sentenceprocessing.AdditionalExistenceEdgeProcessor;
import com.mst.interfaces.sentenceprocessing.DistinctTokenRelationshipDeterminer;
import com.mst.interfaces.sentenceprocessing.DynamicEdgeCreationProcessor;
import com.mst.interfaces.sentenceprocessing.ExistenceToExistenceNoConverter;
import com.mst.interfaces.sentenceprocessing.NegationTokenRelationshipProcessor;
import com.mst.interfaces.sentenceprocessing.NgramsSentenceProcessor;
import com.mst.interfaces.sentenceprocessing.PartOfSpeechAnnotator;
import com.mst.interfaces.sentenceprocessing.PrepPhraseRelationshipProcessor;
import com.mst.interfaces.sentenceprocessing.PrepositionPhraseProcessor;
import com.mst.interfaces.sentenceprocessing.RelationshipProcessor;
import com.mst.interfaces.sentenceprocessing.SemanticTypeSentenceAnnotator;
import com.mst.interfaces.sentenceprocessing.MeasurementProcessor;
import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.interfaces.sentenceprocessing.VerbExistanceProcessor;
import com.mst.interfaces.sentenceprocessing.VerbPhraseProcessor;
import com.mst.interfaces.sentenceprocessing.VerbProcessor;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.AdditionalExistenceEdgeProcessorImpl;
import com.mst.model.sentenceProcessing.FailedSentence;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingFailures;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

import static com.mst.model.metadataTypes.EdgeNames.measurement;

public class SentenceProcessingControllerImpl implements SentenceProcessingController {
    private NgramsSentenceProcessor ngramProcessor;
    private PrepositionPhraseProcessor prepPhraseProcessor;
    private PartOfSpeechAnnotator partOfSpeechAnnotator;
    private SemanticTypeSentenceAnnotator stAnnotator;
    private RelationshipProcessor nounRelationshipProcessor;
    private PrepPhraseRelationshipProcessor prepRelationshipProcessor;
    private VerbPhraseProcessor verbPhraseProcessor;
    private VerbProcessor verbProcessor;
    private SentenceFactory sentenceFactory;
    private MeasurementProcessor measurementProcessor;
    private NegationTokenRelationshipProcessor negationTokenRelationshipProcessor;
    private VerbExistanceProcessor verbExistanceProcessor;
    private AdditionalExistenceEdgeProcessor additionalExistenceEdgeProcessor;
    private SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput;
    private ExistenceToExistenceNoConverter existenceToExistenceNoConverter;
    private DistinctTokenRelationshipDeterminer distinctTokenRelationshipDeterminer;
    private DynamicEdgeCreationProcessor dynamicEdgeCreationProcessor;

    public SentenceProcessingControllerImpl() {
        ngramProcessor = new NGramsSentenceProcessorImpl();
        prepPhraseProcessor = new PrepositionPhraseProcessorImpl();
        partOfSpeechAnnotator = new PartOfSpeechAnnotatorImpl();
        stAnnotator = new SemanticTypeSentenceAnnotatorImpl();
        nounRelationshipProcessor = new NounRelationshipProcessor();
        prepRelationshipProcessor = new PrepPhraseRelationshipProcessorImpl();
        verbPhraseProcessor = new VerbPhraseProcessorImpl();
        verbProcessor = new VerbProcessorImpl();
        sentenceFactory = new SentenceFactory();
        measurementProcessor = new MeasurementProcessorImpl();
        negationTokenRelationshipProcessor = new NegationTokenRelationshipProcessorImpl();
        verbExistanceProcessor = new VerbExistanceProcessorImpl();
        additionalExistenceEdgeProcessor = new AdditionalExistenceEdgeProcessorImpl();
        existenceToExistenceNoConverter = new ExistenceToExistenceNoConverterImpl();
        distinctTokenRelationshipDeterminer = new DistinctTokenRelationshipDeterminerImpl();
        dynamicEdgeCreationProcessor = new DynamicEdgeCreationProcessorImpl();
    }

    public void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput) {
        this.sentenceProcessingMetaDataInput = sentenceProcessingMetaDataInput;
    }

    public List<Sentence> processSentences(SentenceRequest request) throws Exception {
        List<Sentence> sentences = new ArrayList<>();
        for (String sentenceText : request.getSentenceTexts()) {
            Sentence sentence = sentenceFactory.getSentence(sentenceText, request.getStudy(), request.getPractice(), request.getSource());
            sentence = processSentence(sentence, request.isConvertMeasurements());
            sentences.add(sentence);
        }
        return sentences;
    }

    public SentenceProcessingResult reprocessSentences(List<Sentence> sentences) {
        return processSentences(sentences, true);
    }

    public SentenceProcessingResult processText(SentenceTextRequest request) {
        List<Sentence> sentences = sentenceFactory.getSentences(request.getText(), request.getStudy(), request.getPractice(), request.getSource());
        return processSentences(sentences, request.isConvertMeasurements());
    }

    private SentenceProcessingResult processSentences(List<Sentence> sentences, boolean isConvertMeasurements) {
        SentenceProcessingResult result = new SentenceProcessingResult();
        for (Sentence sentence : sentences) {
            try {
                processSentence(sentence, isConvertMeasurements);
                sentence.setProcessDate();
            } catch (Exception ex) {
                if (result.getFailures() == null)
                    result.setFailures(new SentenceProcessingFailures());
                FailedSentence failedSentence = new FailedSentence();
                failedSentence.setError(ex.getLocalizedMessage());
                failedSentence.setSentence(sentence.getOrigSentence());
                result.getFailures().getFailedSentences().add(failedSentence);
                sentence.setDidFail(true);
            }
        }
        result.setSentences(sentences);
        return result;
    }

    private Sentence processSentence(Sentence sentence, boolean isConvertMeasurements) throws Exception {
        sentence = ngramProcessor.process(sentence, sentenceProcessingMetaDataInput.getNgramsInput());
        List<WordToken> tokens = stAnnotator.annotate(sentence.getModifiedWordList(), sentenceProcessingMetaDataInput.getSemanticTypes());
        tokens = partOfSpeechAnnotator.annotate(tokens, sentenceProcessingMetaDataInput.getPartOfSpeechAnnotatorEntity());
        tokens = verbProcessor.process(tokens, sentenceProcessingMetaDataInput.getVerbProcessingInput());
        sentence.getTokenRelationships().addAll(nounRelationshipProcessor.process(tokens, sentenceProcessingMetaDataInput.getNounRelationshipsInput()));
        tokens = prepPhraseProcessor.process(tokens, sentenceProcessingMetaDataInput.getPhraseProcessingInput());
        sentence.getTokenRelationships().addAll(prepRelationshipProcessor.process(tokens, sentenceProcessingMetaDataInput.getPhraseRelationshipMappings()));
        tokens = verbPhraseProcessor.process(tokens, sentenceProcessingMetaDataInput.getVerbPhraseInput());
        List<TokenRelationship> negationRelationships = negationTokenRelationshipProcessor.process(tokens);
        sentence.getTokenRelationships().addAll(negationRelationships);
        sentence.getTokenRelationships().addAll(verbExistanceProcessor.process(sentence));
        TokenRelationship additionalExistence = additionalExistenceEdgeProcessor.process(sentence);
        if (additionalExistence != null)
            sentence.getTokenRelationships().add(additionalExistence);
        sentence.setTokenRelationships(existenceToExistenceNoConverter.convertExistenceNo(negationRelationships, sentence.getTokenRelationships()));
        List<WordToken> modified = sentence.getModifiedWordList();
        Map<String, List<TokenRelationship>> map = sentence.getTokenRelationsByNameMap();
        sentence.getTokenRelationships().addAll(dynamicEdgeCreationProcessor.process(sentenceProcessingMetaDataInput.getDynamicEdgeCreationRules(), map, modified));
        removeMeasurementTokenRelationshipsNotCreatedByMeasurementProcessor(sentence.getTokenRelationships());
        List<TokenRelationship> measurementRelationships = measurementProcessor.process(tokens, isConvertMeasurements);
        sentence.getTokenRelationships().addAll(measurementRelationships);
        sentence.setModifiedWordList(tokens);
        List<TokenRelationship> distinctTokenRelations = distinctTokenRelationshipDeterminer.getDistinctTokenRelationships(sentence);
        sentence.setTokenRelationships(distinctTokenRelations);
        return sentence;
    }

    private void removeMeasurementTokenRelationshipsNotCreatedByMeasurementProcessor(List<TokenRelationship> tokenRelationships) {
        tokenRelationships.removeIf(tokenRelationship -> tokenRelationship.getEdgeName().equals(measurement) && tokenRelationship.getDescriptor() == null);
    }
}
