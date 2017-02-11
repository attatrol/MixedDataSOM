package com.github.attatrol.som.som.functions.learning;

/**
 * Hyperbolic learning function value is inversely proportional to epoch number.
 * @author atta_troll
 *
 */
public class HyperbolicLearningFunction implements LearningFunction {

    private static final double GENERAL_FACTOR = .9;

    @Override
    public double calculate(int epoch) {
        final double value = GENERAL_FACTOR / epoch;
        return value < EPS ? EPS : value;
    }

    public static class Factory implements LearningFunctionFactory<HyperbolicLearningFunction> {

        @Override
        public HyperbolicLearningFunction produceLearningFunction(int epochNumber) {
            return new HyperbolicLearningFunction();
        }

        @Override
        public HyperbolicLearningFunction produceLearningFunction() {
            return new HyperbolicLearningFunction();
        }
    }
}
