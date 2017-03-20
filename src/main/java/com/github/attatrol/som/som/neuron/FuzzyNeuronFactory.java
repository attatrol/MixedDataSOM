package com.github.attatrol.som.som.neuron;

import java.util.Map;

import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.som.som.topology.Point;

/**
 * A factory for some fuzzy neuron.
 * @author atta_troll
 *
 */
public interface FuzzyNeuronFactory<V extends FuzzyNeuron> {

    /**
     * Factory method for a fuzzy neuron.
     * @param initialWeights
     * @param position
     * @param tokenTypes
     * @param sampleFrequencies
     * @return
     */
    V createNeuron(Object[] initialWeights, Point position, TokenType[] tokenTypes,
            Map<Object, Double>[] sampleFrequencies);

}
