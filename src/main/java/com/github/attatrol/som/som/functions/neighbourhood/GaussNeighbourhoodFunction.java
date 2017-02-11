
package com.github.attatrol.som.som.functions.neighbourhood;

/**
 * A default neighborhood function is a Gauss neighborhood function.<br/>
 * <b>Must always return 1 if distance is zero!</b>
 * 
 * @author atta_troll
 *
 */
public class GaussNeighbourhoodFunction implements NeighborhoodFunction {

    private double epochNumber;

    /**
     * Ctor with approximate values for epochNumber coefficient.
     */
    public GaussNeighbourhoodFunction() {
        epochNumber = 100000;
    }

    /**
     * Default ctor.
     * @param totalEpochNumber total number of learning epochs
     */
    public GaussNeighbourhoodFunction(int totalEpochNumber) {
        epochNumber = totalEpochNumber;
    }

    @Override
    public double calculate(double distance, int epoch) {
        double radius = 1 - epoch / epochNumber;
        if (radius < MINIMAL_RADIUS) {
            radius = MINIMAL_RADIUS;
        }
        final double value = Math.exp(-distance / radius);
        return value < EPS ? EPS : value;
    }

    /**
     * Factory for Gauss neighborhood function.
     * @author atta_troll
     *
     */
    public static class Factory implements NeighborhoodFunctionFactory<GaussNeighbourhoodFunction> {

        @Override
        public GaussNeighbourhoodFunction produceNeighborhoodFunction(int epochNumber,
                int neuronNumber) {
            return new GaussNeighbourhoodFunction(epochNumber);
        }

        @Override
        public GaussNeighbourhoodFunction produceNeighborhoodFunction(int neuronNumber) {
            return new GaussNeighbourhoodFunction();
        }
    }
}
