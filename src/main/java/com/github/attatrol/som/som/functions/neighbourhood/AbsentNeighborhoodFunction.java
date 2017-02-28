package com.github.attatrol.som.som.functions.neighbourhood;

/**
 * Used if user avoid neighbourhood function at all.
 * @author atta_troll
 *
 */
public class AbsentNeighborhoodFunction implements NeighborhoodFunction {

    @Override
    public double calculate(double distance, int epoch) {
        return 1.;
    }

    /**
     * Factory for absent neighborhood function.
     * 
     * @author atta_troll
     *
     */
    public static class Factory implements NeighborhoodFunctionFactory<AbsentNeighborhoodFunction> {

        @Override
        public AbsentNeighborhoodFunction produceNeighborhoodFunction(int epochNumber,
                int neuronNumber) {
            return new AbsentNeighborhoodFunction();
        }

        @Override
        public AbsentNeighborhoodFunction produceNeighborhoodFunction(int neuronNumber) {
            return new AbsentNeighborhoodFunction();
        }
    }
}
