
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

    private static boolean isCategoricalTokenType(TokenType tokenType) {
        return tokenType != TokenType.FLOAT && tokenType != TokenType.INTEGER;
    }
}
