
package com.github.attatrol.som.som.functions.neighbourhood;

/**
 * Linear neighborhood function decreases linearly from 1 to 0 in neighborhood.
 * Radius of neighborhood decreases with time linearly.
 * <b>Must always return 1 if distance is zero!</b>
 * 
 * @author atta_troll
 *
 */
public class LinearNeighborhoodFunction implements NeighborhoodFunction {

    private double initialNeighborhoodRadius;

    private double epochNumber;

    /**
     * Worst case ctor, use when nothing is known about SOM.
     */
    public LinearNeighborhoodFunction() {
        epochNumber = 100000;
        initialNeighborhoodRadius = 4;
    }

    /**
     * Constructor to be used when number of epochs is unknown.
     * @param neuronNumber total number of neurons
     */
    public LinearNeighborhoodFunction(int neuronNumber) {
        epochNumber = 100000;
        initialNeighborhoodRadius = Math.round(Math.sqrt(neuronNumber));
    }

    /**
     * Default ctor.
     * @param epochNumber number of learning epochs
     * @param neuronNumber total number of neurons
     */
    public LinearNeighborhoodFunction(int epochNumber, int neuronNumber) {
        this.epochNumber = epochNumber;
        initialNeighborhoodRadius = Math.round(Math.sqrt(neuronNumber));
    }

    @Override
    public double calculate(double distance, int epoch) {
        double radius = initialNeighborhoodRadius * (1. - epoch / epochNumber);
        if (radius < MINIMAL_RADIUS) {
            radius = MINIMAL_RADIUS;
        }
        final double value = 1. - distance / radius;
        return value < EPS ? EPS : value;
    }

    /**
     * Factory for linear neighborhood function.
     * 
     * @author atta_troll
     *
     */
    public static class Factory implements NeighborhoodFunctionFactory<LinearNeighborhoodFunction> {

        @Override
        public LinearNeighborhoodFunction produceNeighborhoodFunction(int epochNumber,
                int neuronNumber) {
            return new LinearNeighborhoodFunction(epochNumber, neuronNumber);
        }

        @Override
        public LinearNeighborhoodFunction produceNeighborhoodFunction(int neuronNumber) {
            return new LinearNeighborhoodFunction(neuronNumber);
        }
    }
}
