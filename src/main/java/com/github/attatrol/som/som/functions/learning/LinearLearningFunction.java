package com.github.attatrol.som.som.functions.learning;

/**
 * A classic learning function, linearly decreases with time flow.
 * @author atta_troll
 *
 */
public class LinearLearningFunction implements LearningFunction {

    private static final double GENERAL_FACTOR = .9;

    private double decreaseFactor;

    public LinearLearningFunction() {
        decreaseFactor = 10000;
    }

    public LinearLearningFunction(int epochNumber) {
        decreaseFactor = epochNumber;
    }

    @Override
    public double calculate(int epoch) {
        final double value = GENERAL_FACTOR * (1. - epoch / decreaseFactor);
        return value < EPS ? EPS : value;
    }

    public static class Factory implements LearningFunctionFactory<LinearLearningFunction> {

        @Override
        public LinearLearningFunction produceLearningFunction(int epochNumber) {
            return new LinearLearningFunction(epochNumber);
        }

        @Override
        public LinearLearningFunction produceLearningFunction() {
            return new LinearLearningFunction();
        }
    }
}
