package com.github.attatrol.som.som.neuron;

import java.util.HashMap;
import java.util.Map;

import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.som.som.topology.Point;

/**
 * FuzzyNeuron must work with numeric data exactly like
 * original Kohonen's SOM neuron should, but with mixed or categorical data
 * it will work in another way: for each such weight a fuzzy set is formed, and
 * its defuzzification is used as a weight value.
 * @author atta_troll
 *
 */
public class FuzzyNeuron extends AbstractNeuron {

    protected final static double INITIAL_CATEGORICAL_WEIGHT = 1.5;

    protected double[] weightsPower;

    protected Map<Object, Double>[] weightFuzzySets;

    protected Map<Object, Double>[] sampleFrequencies;

    @SuppressWarnings("unchecked")
    public FuzzyNeuron(Object[] initialWeights, Point position, TokenType[] tokenTypes,
            Map<Object, Double>[] sampleFrequencies) {
        super(initialWeights, position, tokenTypes);
        this.sampleFrequencies = sampleFrequencies;
        weightFuzzySets = new Map[tokenTypes.length];
        weightsPower = new double[tokenTypes.length];
        for (int i = 0; i < tokenTypes.length; i++) {
            if (tokenTypes[i] != TokenType.FLOAT && tokenTypes[i] != TokenType.INTEGER) {
                weightFuzzySets[i] = new HashMap<Object, Double>();
                weightFuzzySets[i].put(weights[i], INITIAL_CATEGORICAL_WEIGHT);
                weightsPower[i] = INITIAL_CATEGORICAL_WEIGHT;
            }
            else if (tokenTypes[i] == TokenType.INTEGER) {
                weightsPower[i] = (Integer) weights[i];
            }
        }
    }

    @Override
    public void swapWeights(AbstractNeuron other) {
        if (other instanceof FuzzyNeuron) {
            FuzzyNeuron fuzzyOther = (FuzzyNeuron) other;
            Object[] tempWeights = weights;
            double[] tempWeightsPower = weightsPower;
            Map<Object, Double>[] tempWeightFuzzySets = weightFuzzySets;
            Map<Object, Double>[] tempSampleFrequencies = sampleFrequencies;
            TokenType[] tempTokenTypes = tokenTypes;
            weights = fuzzyOther.weights;
            weightsPower = fuzzyOther.weightsPower;
            weightFuzzySets = fuzzyOther.weightFuzzySets;
            sampleFrequencies = fuzzyOther.sampleFrequencies;
            tokenTypes = fuzzyOther.tokenTypes;
            fuzzyOther.weights = tempWeights;
            fuzzyOther.weightsPower = tempWeightsPower;
            fuzzyOther.weightFuzzySets = tempWeightFuzzySets;
            fuzzyOther.sampleFrequencies = tempSampleFrequencies;
            fuzzyOther.tokenTypes = tempTokenTypes;
        }
        else {
            throw new IllegalArgumentException("Swapping weights of incompatible neurons");
        }
    }

    public void changeWeights(Object[] newWeights, double diminishingFactor, boolean isBmu) {
        for (int i = 0; i < tokenTypes.length; i++) {
            switch (tokenTypes[i]) {
            case FLOAT:
                final double value = (Double) weights[i];
                weights[i] = value + diminishingFactor * ((Double) newWeights[i] - value);
                break;
            case INTEGER:
                weightsPower[i] += diminishingFactor * ((Integer) newWeights[i] - weightsPower[i]);
                weights[i] = (int) weightsPower[i];
                break;
            case BINARY:
            case BINARY_DIGITAL:
            case CATEGORICAL_STRING:
                final double incomingValuePower = calculateIncomingTokenPower(
                        newWeights[i], i, diminishingFactor, isBmu);
                weightFuzzySets[i].put(newWeights[i], incomingValuePower);
                if (newWeights[i].equals(weights[i])) {
                    weightsPower[i] = incomingValuePower;
                }
                else if (incomingValuePower > weightsPower[i]) {
                    weightsPower[i] = incomingValuePower;
                    weights[i] = newWeights[i];
                    // System.out.println("Change!");
                }
                break;
            default:
                // do nothing
                break;
            }
        }
    }

    /**
     * Does nothing on the epoch end.
     */
    @Override
    public void markEpochEnd() {
    }

    @Override
    public void setNewWeights(Object[] newWeights) {
        weights = newWeights;
        for (int i = 0; i < tokenTypes.length; i++) {
            if (tokenTypes[i] != TokenType.FLOAT && tokenTypes[i] != TokenType.INTEGER) {
                weightFuzzySets[i].clear();
                weightFuzzySets[i].put(weights[i], INITIAL_CATEGORICAL_WEIGHT);
                weightsPower[i] = INITIAL_CATEGORICAL_WEIGHT;
            }
            else if (tokenTypes[i] == TokenType.INTEGER) {
                weightsPower[i] = (Integer) weights[i];
            }
        }
    }

    protected double calculateIncomingTokenPower(Object newWeight, int index, double diminishingFactor,
            boolean isBmu) {
        Double value = weightFuzzySets[index].get(newWeight);
        if (value == null) {
            value = 0.;
        }
        value += diminishingFactor * sampleFrequencies[index].get(newWeight);
        return value;
    }

    /**
     * Factory for a fuzzy neuron.
     * @author atta_troll
     *
     */
    public static class Factory implements FuzzyNeuronFactory<FuzzyNeuron> {

        @Override
        public FuzzyNeuron createNeuron(Object[] initialWeights, Point position,
                TokenType[] tokenTypes, Map<Object, Double>[] sampleFrequencies) {
            return new FuzzyNeuron(initialWeights, position, tokenTypes, sampleFrequencies);
        }
    }

}
