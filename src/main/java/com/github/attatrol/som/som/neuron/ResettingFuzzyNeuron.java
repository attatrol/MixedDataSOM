package com.github.attatrol.som.som.neuron;

import java.util.Map;

import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.som.som.topology.Point;

/**
 * This type of neuron resets fuzzy weights as if they were set in the beginning.
 * @author atta_troll
 *
 */
public class ResettingFuzzyNeuron extends FuzzyNeuron {

    public ResettingFuzzyNeuron(Object[] initialWeights, Point position, TokenType[] tokenTypes,
            Map<Object, Double>[] sampleFrequencies) {
        super(initialWeights, position, tokenTypes, sampleFrequencies);
    }

    /**
     * Resets fuzzy weights every epoch, setting them as if the learning process just started.
     */
    @Override
    public void markEpochEnd() {
        for (int i = 0; i < tokenTypes.length; i++) {
            if (tokenTypes[i] != TokenType.FLOAT && tokenTypes[i] != TokenType.INTEGER) {
                for (Map.Entry<Object, Double> entry : weightFuzzySets[i].entrySet()) {
                    entry.setValue(0.);
                }
                weightFuzzySets[i].put(weights[i], INITIAL_CATEGORICAL_WEIGHT);
                weightsPower[i] = INITIAL_CATEGORICAL_WEIGHT;
            }
        }
    }

    /**
     * Factory for a fuzzy neuron.
     * @author atta_troll
     *
     */
    public static class Factory implements FuzzyNeuronFactory<ResettingFuzzyNeuron> {

        @Override
        public ResettingFuzzyNeuron createNeuron(Object[] initialWeights, Point position,
                TokenType[] tokenTypes, Map<Object, Double>[] sampleFrequencies) {
            return new ResettingFuzzyNeuron(initialWeights, position, tokenTypes, sampleFrequencies);
        }
    }
}
