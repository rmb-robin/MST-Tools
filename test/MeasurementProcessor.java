import org.junit.Test;

import com.mst.model.sentenceProcessing.WordToken;

import static com.mst.model.metadataTypes.MeasurementAnnotations.*;
import static org.junit.Assert.*;
import static com.mst.model.metadataTypes.Descriptor.*;

import java.util.List;

public class MeasurementProcessor {
    private BaseUtility baseUtility;

    public MeasurementProcessor() {
        baseUtility = new BaseUtility();
    }

    @Test
    public void testMMtoCM() {
        List<WordToken> wordTokens = baseUtility.getWordTokens("measuring 11 x 32 x 23 mm", true);
        assertEquals("1.1", wordTokens.get(1).getToken());
        assertEquals("3.2", wordTokens.get(2).getToken());
        assertEquals("2.3", wordTokens.get(3).getToken());
        assertEquals("cm", wordTokens.get(4).getToken());
        assertEquals(X_AXIS, wordTokens.get(1).getDescriptor());
        assertEquals(Y_AXIS, wordTokens.get(2).getDescriptor());
        assertEquals(Z_AXIS, wordTokens.get(3).getDescriptor());
    }

    @Test
    public void testMMtoCM2() {
        List<WordToken> words = baseUtility.getWordTokens("measuring 21x32x63mm", true);
        assertEquals("2.1", words.get(1).getToken());
        assertEquals("3.2", words.get(2).getToken());
        assertEquals("6.3", words.get(3).getToken());
        assertEquals("cm", words.get(4).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
        assertEquals(Y_AXIS, words.get(2).getDescriptor());
        assertEquals(Z_AXIS, words.get(3).getDescriptor());
    }

	@Test
	public void testMMtoCM3() {
        List<WordToken> words = baseUtility.getWordTokens("measures 31 x 32 mm", true);
		assertEquals("3.1", words.get(1).getToken());
        assertEquals("3.2", words.get(2).getToken());
		assertEquals("cm", words.get(3).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
        assertEquals(Y_AXIS, words.get(2).getDescriptor());
	}
	
	@Test
	public void testMMtoCM4() {
        List<WordToken> words = baseUtility.getWordTokens("measures 41mm", true);
		assertEquals("4.1", words.get(1).getToken());
		assertEquals("cm", words.get(2).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
	}

	@Test
	public void testTokenizeMeasurements() {
        List<WordToken> words = baseUtility.getWordTokens("measuring 5.1x.2x8.3cm", true);
        assertEquals("5.1", words.get(1).getToken());
        assertEquals(".2", words.get(2).getToken());
        assertEquals("8.3", words.get(3).getToken());
        assertEquals("cm", words.get(4).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
        assertEquals(Y_AXIS, words.get(2).getDescriptor());
        assertEquals(Z_AXIS, words.get(3).getDescriptor());

        words = baseUtility.getWordTokens("measuring 6.1 x 7.2 x 1.3 cm", true);
        assertEquals("6.1", words.get(1).getToken());
        assertEquals("7.2", words.get(2).getToken());
        assertEquals("1.3", words.get(3).getToken());
        assertEquals("cm", words.get(4).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
        assertEquals(Y_AXIS, words.get(2).getDescriptor());
        assertEquals(Z_AXIS, words.get(3).getDescriptor());

        words = baseUtility.getWordTokens("measuring 7.1 x 2 x 3cm", true);
        assertEquals("7.1", words.get(1).getToken());
        assertEquals("2", words.get(2).getToken());
        assertEquals("3", words.get(3).getToken());
        assertEquals("cm", words.get(4).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
        assertEquals(Y_AXIS, words.get(2).getDescriptor());
        assertEquals(Z_AXIS, words.get(3).getDescriptor());
	}

    @Test
    public void testMeasurementAnnotations() {
        List<WordToken> words = baseUtility.getWordTokens("measuring 1.1 cm in length x 3.2 cm width x 2.3 cm depth", true);
        assertEquals("1.1", words.get(1).getToken());
        assertEquals("3.2", words.get(6).getToken());
        assertEquals("2.3", words.get(10).getToken());
        assertEquals(LENGTH, words.get(1).getDescriptor());
        assertEquals(TRANSVERSE, words.get(6).getDescriptor());
        assertEquals(AP, words.get(10).getDescriptor());

        words = baseUtility.getWordTokens("Short axis measures 1.1 cm and long axis measures 3.2 cm", true);
        assertEquals("1.1", words.get(3).getToken());
        assertEquals("3.2", words.get(9).getToken());
        assertEquals(SHORT_AXIS, words.get(3).getDescriptor());
        assertEquals(LONG_AXIS, words.get(9).getDescriptor());

        words = baseUtility.getWordTokens("measuring 5.2x5 by approximately 11 cm in transverse, ap and length dimensions respectively", true);
        assertEquals("5.2", words.get(1).getToken());
        assertEquals("5", words.get(2).getToken());
        assertEquals("11", words.get(5).getToken());
        assertEquals(TRANSVERSE, words.get(1).getDescriptor());
        assertEquals(AP, words.get(2).getDescriptor());
        assertEquals(LENGTH, words.get(5).getDescriptor());
    }
}

		