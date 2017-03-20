package com.github.attatrol.som.som.functions.neighbourhood;

/**
 * A simple factory for a neighborhood function.
 * Must be implemented for every neighborhood function for
 * use in view.
 * @author atta_troll
 *
 */
public interface NeighborhoodFunctionFactory<V extends NeighborhoodFunction> {

    /**
     * Preferred factory method
     * @param epochNumber number of epochs
     * @param neuronNumber number of neurons
     * @return neighborhood function factory
     */
    V produceNeighborhoodFunction(int epochNumber, int neuronNumber);

    /**
     * Factory method to be used when epoch number is unknown
     * @param neuronNumber number of neurons
     * @return neighborhood function factory
     */
    V produceNeighborhoodFunction(int neuronNumber);
}
