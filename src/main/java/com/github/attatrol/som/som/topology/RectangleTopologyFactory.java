package com.github.attatrol.som.som.topology;

import com.github.attatrol.preprocessing.distance.metric.Metric;

/**
 * Topology factory makes sure all created topologies are valid.
 * @author atta_troll
 *
 */
@FunctionalInterface
public interface RectangleTopologyFactory<V extends RectangleTopology> {

    /**
     * Creates a topology.
     * @param width width of rectangular grid
     * @param height height of rectangular grid
     * @param some metric used to calculate distances
     * @return a topology instance
     * @throws IllegalArgumentException on bad input parameters
     */
    V createTopology(int width, int height, Metric metric) throws IllegalArgumentException;
}
