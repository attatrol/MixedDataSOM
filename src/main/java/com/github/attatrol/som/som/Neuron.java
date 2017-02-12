package com.github.attatrol.som.som;

import java.util.HashMap;
import java.util.Map;

import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.som.som.topology.Point;

/**
 * Neuron must work with numeric data exactly like
 * original Kohonen's SOM neuron should, but with mixed or categorical data
 * it will work like neurons described
 * @author atta_troll
 *
 */
public class Neuron {

    private Object[] weights;

    private double[] weightsPower;

    public Map<Object, Double>[] weightFuzzySets;

    public final Map<Object, Double>[] sampleFrequencies;

    private final Point position;

    private final TokenType[] tokenTypes;

    @SuppressWarnings("unchecked")
    public Neuron(Object[] initialWeights, Point position, TokenType[] tokenTypes,
            Map<Object, Double>[] sampleFrequencies) {
        weights = initialWeights;
        this.position = position;
        this.tokenTypes = tokenTypes;
        this.sampleFrequencies = sampleFrequencies;
        weightFuzzySets = new Map[tokenTypes.length];
        weightsPower = new double[tokenTypes.length];
        for (int i = 0; i < tokenTypes.length; i++) {
            weightFuzzySets[i] = new HashMap<Object, Double>();
        }
    }

    public Point getPosition() {
        return position;
    }

    Object[] getWeights() {
        return weights;
    }

    void changeWeights(Object[] newWeights, double diminishingFactor, boolean isBmu) {
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
                weightFuzzySets[i].put(newWeights[i], incomingValuePower);
                if (newWeights[i].equals(weights[i])) {
                    weightsPower[i] = incomingValuePower;
                }
                else if (incomingValuePower > weightsPower[i]) {
                    weightsPower[i] = incomingValuePower;
                    weights[i] = newWeights[i];
                    //System.out.println("Change!");
                }
                break;
            default:
                // do nothing
                break;
            }
        }
    }

    private double calculateIncomingTokenPower(Object newWeight, int index, double diminishingFactor,
            boolean isBmu) {
        Double value = weightFuzzySets[index].get(newWeight);
        if (value == null) {
            value = 0.;
        }
        value += sampleFrequencies[index].get(newWeight) * diminishingFactor;
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Neuron = [");
        for (Object weight : weights) {
            sb.append(weight).append(", ");
        }
        return sb.append("]").toString();
    }

    /**
     * Neurons are mapped 1-to-1 to their position, so return
     * position's hash code.<p/>
     * {@inheritDoc}
     * 
     */
    @Override
    public int hashCode() {
        return position.hashCode();
    }
}
