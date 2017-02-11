package com.github.attatrol.som.som.functions.learning;

/**
 * Simple factory for a learning function.
 * Must be implemented for every learning function for
 * use in view.
 * @author atta_troll
 *
 */
public interface LearningFunctionFactory<V extends LearningFunction> {

    /**
     * Preferred factory method.
     * @param epochNumber number of learning epochs
     * @return learning function instance
     */
    V produceLearningFunction(int epochNumber);

    /**
     * Method to be used when epoch number is unknown
     * @return learning function instance
     */
    V produceLearningFunction();

}
