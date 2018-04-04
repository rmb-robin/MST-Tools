package com.mst.model.businessRule;

/**
 * Provides for a generic programmatic approach to comparing discrete data in business rules
 * to the DiscreteData within sentences.
 *
 * @author Brian Sheely
 * @version %I%, %G%
 */
public class DiscreteDataType {
    public enum DataType { NUMERIC_RANGE, STRING }
    private String name;
    private DataType dataType;
    private String minRangeValue;
    private String maxRangeValue;
    private String value;

    /**
     * Gets the name of the DiscreteData for which a business rule applies.
     * @return variable name used in the DiscreteData class
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the DiscreteData for which a business rule will apply.
     * @param name variable name used in the DiscreteData class
     * @see com.mst.model.discrete.DiscreteData
     */
    public void setName(String name) {
        this.name = name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getMinRangeValue() {
        return minRangeValue;
    }

    public void setMinRangeValue(String minRangeValue) {
        this.minRangeValue = minRangeValue;
    }

    public String getMaxRangeValue() {
        return maxRangeValue;
    }

    public void setMaxRangeValue(String maxRangeValue) {
        this.maxRangeValue = maxRangeValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
