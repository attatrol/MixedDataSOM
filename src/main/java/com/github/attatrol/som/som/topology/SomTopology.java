package com.github.attatrol.som.som.topology;

import java.util.List;

/**
 * SOM is a single layer ANN, however,
 * it depends on distances between neurons, so we have to define
 * positions of neurons as their topology. All issues with neuron
 * positions should be processed by this.<br/>
 * 
 * @author atta_troll
 *
 */
public interface SomTopology {

    /**
     * Produces list of neuron positions
     * @return positions of neurons
     */
    List<Point> getNeuronPositions();

    /**
     * Calculates distance between 2 points.
     * @param point1 1st neuron
     * @param point2 2nd neuron
     * @return resulting distance
     */
    double getDistance(Point point1, Point point2);

}
