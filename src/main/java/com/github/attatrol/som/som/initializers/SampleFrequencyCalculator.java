
package com.github.attatrol.som.som.initializers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.parsing.TokenType;

/**
 * Calculates sample frequencies of tokens in data set.
 * 
 * @author atta_troll
 *
 */
public final class SampleFrequencyCalculator {

    private SampleFrequencyCalculator() {
    }

    /**
     * Creates sample frequencies. Numerical frequencies are unaccounted.
     * @param dataSource
     *        data source
     * @param types
     *        token types
     * @return sample frequencies
     * @throws IOException data source i/o error
     */
    public static Map<Object, Double>[] getSampleFrequencies(AbstractTokenDataSource<?> dataSource,
            TokenType[] types) throws IOException {
        final int recordLength = dataSource.getRecordLength();
        @SuppressWarnings("unchecked")
        final Map<Object, Long>[] occurrencies = new Map[recordLength];
        for (int i = 0; i < recordLength; i++) {
            occurrencies[i] = new HashMap<>();
        }
        long counter = 0;
        dataSource.reset();
        while (dataSource.hasNext()) {
            Object[] tokens = dataSource.next().getData();
            for (int i = 0; i < recordLength; i++) {
                if (isCategoricalTokenType(types[i])) {
                    final Long occurrence = occurrencies[i].get(tokens[i]);
                    if (occurrence == null) {
                        occurrencies[i].put(tokens[i], 1L);
                    }
                    else {
                        occurrencies[i].put(tokens[i], occurrence + 1);
                    }
                }
            }
            counter++;
        }
        @SuppressWarnings("unchecked")
        Map<Object, Double>[] frequencies = new Map[recordLength];
        for (int i = 0; i < recordLength; i++) {
            frequencies[i] = new HashMap<>();
            if (counter != 0L) {
                for (Map.Entry<Object, Long> entry : occurrencies[i].entrySet()) {
                    frequencies[i].put(entry.getKey(), ((double) entry.getValue()) / counter);
                }
            }
        }
        return frequencies;
    }

    /**
     * Calculates total number of records in a data source
     * @param dataSource data source
     * @return data source size
     * @throws IOException on i/o data source error
     */
    public static long getDataSourceSize(AbstractTokenDataSource<?> dataSource) throws IOException {
        long counter = 0L;
        dataSource.reset();
        while (dataSource.hasNext()) {
            dataSource.next();
            counter++;
        }
        return counter;
    }

    /**
     * POJO, holds min and max values of some column
     * @author atta_troll
     *
     */
    public static final class MinAndMax {

        private double min;

        private double max;

        public double getMin() {
            return min;
        }

        public void setMin(double min) {
            this.min = min;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
            this.max = max;
        }
    }

    public static MinAndMax[] getNumericTokenBounds(AbstractTokenDataSource<?> dataSource,
            TokenType[] types) throws IOException {
        final int recordLength = dataSource.getRecordLength();
        MinAndMax[] result = new MinAndMax[recordLength];
        for (int i = 0; i < recordLength; i++) {
            if (isNumericalTokenType(types[i])) {
                result[i] = new MinAndMax();
            }
        }
        dataSource.reset();
        if (dataSource.hasNext()) {
            Object[] tokens = dataSource.next().getData();
            for (int i = 0; i < recordLength; i++) {
                if (isNumericalTokenType(types[i])) {
                    double val = 0.;
                    if (types[i] == TokenType.INTEGER) {
                        val = (Integer) tokens[i];
                    }
                    else if (types[i] == TokenType.FLOAT) {
                        val = (Double) tokens[i];
                    }
                    result[i].setMax(val);
                    result[i].setMin(val);
                }
            }
        }
        while (dataSource.hasNext()) {
            Object[] tokens = dataSource.next().getData();
            for (int i = 0; i < recordLength; i++) {
                if (isNumericalTokenType(types[i])) {
                    double val = 0.;
                    if (types[i] == TokenType.INTEGER) {
                        val = (Integer) tokens[i];
                    }
                    else if (types[i] == TokenType.FLOAT) {
                        val = (Double) tokens[i];
                    }
                    if (val > result[i].getMax()) {
                        result[i].setMax(val);
                    }
                    else if (val < result[i].getMin()) {
                        result[i].setMin(val);
                    }
                }
            }
        }
        return result;
    }

    public static boolean isCategoricalTokenType(TokenType tokenType) {
        return tokenType != TokenType.FLOAT && tokenType != TokenType.INTEGER && tokenType != TokenType.MISSING;
    }

    public static boolean isNumericalTokenType(TokenType tokenType) {
        return tokenType == TokenType.INTEGER || tokenType == TokenType.FLOAT;
    }
}
