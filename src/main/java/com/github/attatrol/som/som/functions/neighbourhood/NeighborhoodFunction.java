
package com.github.attatrol.som.som.functions.neighbourhood;

/**
 * Neighborhood function (or smoothing kernel) is a function used to define
 * level of excitement of non-BMU (non-winner) neurons based on distance from
 * BMU neuron. It smoothly decreases when distance increases.<br/>
 * It may also smoothly decrease with increasing time epoch.<br/>
 * Its codomain is [0, 1].
 * <b>Must always return 1 if distance is zero!</b>
 * 
 * @author atta_troll
 *
 */
@FunctionalInterface
public interface NeighborhoodFunction {

    /**
     * Minimal possible value for neighborhood function.
     */
    double EPS = .0;

    /**
     * This is minimal radius of neigborhood
     */
    double MINIMAL_RADIUS = 0.99;

    /**
     * Calculates value of the neighborhood function
     * 
     * @param distance
     *        distance between neurons, remember that least distance between different neurons
     *        always equals 1
     * @param epoch
     *        current learning epoch (time coordinate)
     * @return value of the neighborhood function
     */
    double calculate(double distance, int epoch);

}
