package com.mst.model.metadataTypes;

import java.util.*;

public class MeasurementAnnotations {
    public static final String LENGTH = "Length";
    public static final String TRANSVERSE = "Transverse";
    public static final String AP = "AP";
    public static final String SHORT_AXIS = "Short axis";
    public static final String LONG_AXIS = "Long axis";

    //TODO read from file
    public static Map<String, List<String>> getAnnotations() {
        Map<String, List<String>> annotations = new LinkedHashMap<>();
        annotations.put(SHORT_AXIS, new ArrayList<>(Collections.singletonList("short axis")));
        annotations.put(LONG_AXIS, new ArrayList<>(Collections.singletonList("long axis")));
        annotations.put(LENGTH, new ArrayList<>(Arrays.asList("craniocaudad", "long", "in length", "length", "craniocaudal", "cc", "ht", "cephalocaudad", "cephalocaudal", "height", "head to toe")));
        annotations.put(TRANSVERSE, new ArrayList<>(Arrays.asList("transverse", "right to left", "left to right", "width")));
        annotations.put(AP, new ArrayList<>(Arrays.asList("ap", "anterior posterior", "depth", "front to back")));
        return annotations;
    }
}
