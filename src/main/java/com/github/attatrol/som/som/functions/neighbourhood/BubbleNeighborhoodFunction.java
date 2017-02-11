package com.github.attatrol.som.som.functions.neighbourhood;

/**
 * It is constant 1 for a whole neighborhood, radius decreases over time lonearly.
 * @author atta_troll
 *
 */
public class BubbleNeighborhoodFunction implements NeighborhoodFunction {

    private double initialNeighborhoodRadius;

    private double epochNumber;

    /**
     * Worst case ctor, use when nothing is known about SOM.
     */
    public BubbleNeighborhoodFunction() {
        initialNeighborhoodRadius = 4;
        epochNumber = 10000;
    }

    /**
     * Constructor to be used when number of epochs is unknown.
     * @param neuronNumber total number of neurons
     */
    public BubbleNeighborhoodFunction(int neuronNumber) {
        epochNumber = 10000;
        initialNeighborhoodRadius = Math.round(Math.sqrt(neuronNumber));
    }

    /**
     * Default ctor.
     * @param epochNumber number of learning epochs
     * @param neuronNumber total number of neurons
     */
    public BubbleNeighborhoodFunction(int epochNumber, int neuronNumber) {
        this.epochNumber = epochNumber;
        initialNeighborhoodRadius = Math.round(Math.sqrt(neuronNumber));
    }

    @Override
    public double calculate(double distance, int epoch) {
        double radius = initialNeighborhoodRadius * (1. - epoch / epochNumber);
        if (radius < MINIMAL_RADIUS) {
            radius = MINIMAL_RADIUS;
        }
        final double value = distance < radius ? 1. : 0.;
        return value < EPS ? EPS : value;
    }

    /**
     * Factory for bubble neighborhood function.
     * 
     * @author atta_troll
     *
     */
    public static class Factory implements NeighborhoodFunctionFactory<BubbleNeighborhoodFunction> {

        @Override
        public BubbleNeighborhoodFunction produceNeighborhoodFunction(int epochNumber,
                int neuronNumber) {
            return new BubbleNeighborhoodFunction(epochNumber, neuronNumber);
        }

        @Override
        public BubbleNeighborhoodFunction produceNeighborhoodFunction(int neuronNumber) {
            return new BubbleNeighborhoodFunction(neuronNumber);
        }
    }
}
