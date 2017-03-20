package com.github.attatrol.som.som.neuron;

import java.util.Map;

import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.som.som.topology.Point;

public class FrequencyControlledFuzzyNeuron extends FuzzyNeuron {

    protected double[] fuzzyWeightSum;

    public FrequencyControlledFuzzyNeuron(Object[] initialWeights, Point position,
            TokenType[] tokenTypes, Map<Object, Double>[] sampleFrequencies) {
        super(initialWeights, position, tokenTypes, sampleFrequencies);
        fuzzyWeightSum = new double[tokenTypes.length];
        for (int i = 0; i < tokenTypes.length; i++) {
            fuzzyWeightSum[i] = FuzzyNeuron.INITIAL_CATEGORICAL_WEIGHT;
        }
    }

    @Override
    public void swapWeights(AbstractNeuron other) {
        if (other instanceof FrequencyControlledFuzzyNeuron) {
            super.swapWeights(other);
            FrequencyControlledFuzzyNeuron fcfOther =
                    (FrequencyControlledFuzzyNeuron) other;
            double[] tempFuzzyWeightSum = fuzzyWeightSum;
            fuzzyWeightSum = fcfOther.fuzzyWeightSum;
            fcfOther.fuzzyWeightSum = tempFuzzyWeightSum;
        }
        else {
            throw new IllegalArgumentException("Swapping weights of incompatible neurons");
        }
    }

    /**
     * Only the last but one brunch changed.
     */
    @Override
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
                double incomingValuePower = calculateIncomingTokenPower(
                        newWeights[i], i, diminishingFactor, isBmu);
                Double oldValue = weightFuzzySets[i].get(newWeights[i]);
                if (oldValue == null) {
                    oldValue = 0.;
                }
                // single change
                fuzzyWeightSum[i] += incomingValuePower - oldValue;
                weightFuzzySets[i].put(newWeights[i], incomingValuePower);
                if (newWeights[i].equals(weights[i])) {
                    weightsPower[i] = incomingValuePower;
                }
                else if (incomingValuePower > weightsPower[i]) {
                    weightsPower[i] = incomingValuePower;
                    weights[i] = newWeights[i];
                    System.out.println("Change!");
                }
                break;
            default:
                // do nothing
                break;
            }
        }
    }

    protected double calculateIncomingTokenPower(Object newWeight, int index, double diminishingFactor,
            boolean isBmu) {
        Double value = weightFuzzySets[index].get(newWeight);
        if (value == null) {
            value = 0.;
        }
        value += getFequencyAdjustment(value, index) * diminishingFactor
                / sampleFrequencies[index].get(newWeight);
        return value;
    }

    private Double getFequencyAdjustment(Double value, int index) {
        final double adjustment = value / fuzzyWeightSum[index];
        final double function = 2. / 1. + Math.exp(-4. * adjustment);
        return function;
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
