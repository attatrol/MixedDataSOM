package com.github.attatrol.som.som.neuron;

import java.util.Map;

import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.som.som.topology.Point;

public class FrequencyControlledFuzzyNeuron extends FuzzyNeuron {

    private static final double MINIMAL_FREQUENCY_ADJUSTMENT_RATIO = 0.1;

    protected final double[] fuzzyWeightSum;

    public FrequencyControlledFuzzyNeuron(Object[] initialWeights, Point position,
            TokenType[] tokenTypes, Map<Object, Double>[] sampleFrequencies) {
        super(initialWeights, position, tokenTypes, sampleFrequencies);
        fuzzyWeightSum = new double[tokenTypes.length];
        for (int i = 0; i < tokenTypes.length; i++) {
            fuzzyWeightSum[i] = FuzzyNeuron.INITIAL_CATEGORICAL_WEIGHT;
        }
    }

    @Override
    public void changeWeights(Object[] newWeights, double diminishingFactor, boolean isBmu) {
        for (int i = 0; i < tokenTypes.length; i++) {
            switch (tokenTypes[i]) {
            case FLOAT:
                final double value = (Double) weights[i];
                weights[i] = value + diminishingFactor * ((Double) newWeights[i] - value);
                break;
            case INTEGER:
                int intValue = (Integer) weights[i];
                weightsPower[i] += diminishingFactor * ((Integer) newWeights[i] - intValue);
                while (weightsPower[i] < 0) {
                    intValue--;
                    weightsPower[i] += 1;
                }
                while (weightsPower[i] > 1) {
                    intValue++;
                    weightsPower[i] -= 1;
                }
                weights[i] = intValue;
                break;
            case BINARY:
            case BINARY_DIGITAL:
            case CATEGORICAL_STRING:
                final double incomingValuePower = calculateIncomingTokenPower(
                        newWeights[i], i, diminishingFactor, isBmu);
                fuzzyWeightSum[i] += incomingValuePower - weightFuzzySets[i].get(newWeights[i]);
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

    @Override
    protected double calculateIncomingTokenPower(Object newWeight, int index, double diminishingFactor,
            boolean isBmu) {
        Double value = weightFuzzySets[index].get(newWeight);
        if (value == null) {
            value = 0.;
        }
        value += diminishingFactor * getFequencyAdjustment(newWeight, value, index);
        return value;
    }

    private Double getFequencyAdjustment(Object newWeight, Double value, int index) {
        final double adjustment = sampleFrequencies[index].get(newWeight) - value / fuzzyWeightSum[index];
        return Math.max(adjustment, MINIMAL_FREQUENCY_ADJUSTMENT_RATIO);
    }

    /**
     * Factory for a frequency controlled fuzzy neuron.
     * @author atta_troll
     *
     */
    public static class Factory implements FuzzyNeuronFactory<FrequencyControlledFuzzyNeuron> {

        @Override
        public FrequencyControlledFuzzyNeuron createNeuron(Object[] initialWeights, Point position,
                TokenType[] tokenTypes, Map<Object, Double>[] sampleFrequencies) {
            return new FrequencyControlledFuzzyNeuron(initialWeights, position, tokenTypes, sampleFrequencies);
        }
    }
}
