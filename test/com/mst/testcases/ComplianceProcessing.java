package com.mst.testcases;

import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.sentenceProcessing.TokenRelationship;
import org.junit.Test;

import java.util.List;

import static com.mst.model.metadataTypes.ComplianceBucketName.*;
import static org.junit.Assert.*;



public class ComplianceProcessing {
    private BaseUtility baseUtility;

    public ComplianceProcessing() {
        baseUtility = new BaseUtility();
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
    }

    @Test
    public void Itn1() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in thyroid measures 1 cm. Recommend ultrasound.",true,17, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_1));
        assertEquals(BUCKET_1, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Itn2() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in thyroid measures 2 cm. Recommend sonography.",true,30, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_2));
        assertEquals(BUCKET_2, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Itn3() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in thyroid measures 3 cm. Recommend ultrasound.",true,65, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_3));
        assertEquals(BUCKET_3, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Itn4() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in thyroid measures 0.9 cm. No followup.",true,24, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_4));
        assertEquals(BUCKET_4 + ", " + BUCKET_7, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Itn5() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in thyroid measures 1 cm. No followup.",true,35, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_5));
        assertEquals(BUCKET_5 + ", " + BUCKET_8, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Itn7() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in thyroid measures .1 cm. Recommend ultrasound.",true,19, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_7));
        assertTrue(baseUtility.isCompliant(request, BUCKET_4));
        assertEquals(BUCKET_4 + ", " + BUCKET_7, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Itn8() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in thyroid measures 1 cm. Recommend ultrasound.",true,45, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_8));
        assertEquals(BUCKET_5 + ", " + BUCKET_8, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Et6() {
        SentenceRequest request = baseUtility.getSentenceRequest("Enlarged prominent isthmus. Recommend ultrasound.",true,75, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_6));
        assertEquals(BUCKET_6, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc1() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 1 cm. No followup.",true,43, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_1));
        assertEquals(BUCKET_1 + ", " + BUCKET_9, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc2() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 4 cm. No followup.",true,43, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_2));
        assertEquals(BUCKET_2 + ", " + BUCKET_10, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc3() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 6 cm. Recommend annual ultrasound.",true,43, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_3));
        assertEquals(BUCKET_3, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc4() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 8 cm. Recommend surgical evaluation.",true,43, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_4));
        assertEquals(BUCKET_4, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc5() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 1 cm. No follow-up.",true,60, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_5));
        assertEquals(BUCKET_5 + ", " + BUCKET_11, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc6() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 3 cm. No follow-up.",true,62, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_6));
        assertEquals(BUCKET_6 + ", " + BUCKET_12, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc7() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 5 cm. Recommend annual ultrasound.",true,65, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_7));
        assertEquals(BUCKET_7, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc8() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 11 cm. Recommend surgical evaluation.",true,70, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_8));
        assertEquals(BUCKET_8, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc9() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 2 cm. Recommend annual ultrasound.",true,37, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_9));
        assertEquals(BUCKET_1 + ", " + BUCKET_9, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc10() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 4 cm. Recommend annual ultrasound.",true,40, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_10));
        assertEquals(BUCKET_2 + ", " + BUCKET_10, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc11() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 1 cm. Recommend annual ultrasound.",true,60, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_11));
        assertEquals(BUCKET_5 + ", " + BUCKET_11, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Ovc12() {
        SentenceRequest request = baseUtility.getSentenceRequest("Cyst in ovary measures 3 cm. Recommend annual ultrasound.",true,74, "F");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_12));
        assertEquals(BUCKET_6 + ", " + BUCKET_12, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Aaa1() {
        SentenceRequest request = baseUtility.getSentenceRequest("Abdominal aneurysm measuring 2.9 cm. Recommend followup every 5 years.",true,50, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_1));
        assertEquals(BUCKET_1, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Aaa2() {
        SentenceRequest request = baseUtility.getSentenceRequest("Abdominal aneurysm measuring 3 cm. Recommend followup every 3 years.",true,50, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_2));
        assertEquals(BUCKET_2, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Aaa3() {
        SentenceRequest request = baseUtility.getSentenceRequest("Abdominal aneurysm measuring 3.5 cm. Recommend followup every 12 months.",true,50, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_3));
        assertEquals(BUCKET_3, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Aaa4() {
        SentenceRequest request = baseUtility.getSentenceRequest("Abdominal aneurysm measuring 4 cm. Recommend followup every 12 months.",true,50, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_4));
        assertEquals(BUCKET_4 + ", " + BUCKET_5, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Aaa5() {
        SentenceRequest request = baseUtility.getSentenceRequest("Abdominal aneurysm measuring 4 cm. Recommend vascular consultation.",true,50, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_5));
        assertEquals(BUCKET_4 + ", " + BUCKET_5, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Aaa6() {
        SentenceRequest request = baseUtility.getSentenceRequest("Abdominal aneurysm measuring 5 cm. Recommend followup every 6 months.",true,50, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_6));
        assertEquals(BUCKET_6 + ", " + BUCKET_7, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Aaa7() {
        SentenceRequest request = baseUtility.getSentenceRequest("Abdominal aneurysm measuring 5 cm. Recommend vascular consultation.",true,50, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_7));
        assertEquals(BUCKET_6 + ", " + BUCKET_7, discreteData.getBucketName());
        assertEquals(String.valueOf(true) + ", " + String.valueOf(true), discreteData.getIsCompliant());
    }

    @Test
    public void Aaa8() {
        SentenceRequest request = baseUtility.getSentenceRequest("Abdominal aneurysm measuring 6 cm. Recommend followup with vascular surgeon.",true,50, "M");
        DiscreteData discreteData = request.getDiscreteData();
        assertTrue(baseUtility.isCompliant(request, BUCKET_8));
        assertEquals(BUCKET_8, discreteData.getBucketName());
        assertEquals(String.valueOf(true), discreteData.getIsCompliant());
    }
}
