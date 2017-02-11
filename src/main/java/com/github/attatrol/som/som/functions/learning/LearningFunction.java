package com.github.attatrol.som.som.functions.learning;

/**
 * Learning function defines overall decrease in neuron's weight
 * changes over time. With every next time epoch its value decreases.<br/>
 * It can have positive values only.<br/>
 * <b>Its codomain should be in [0, 1] range.</b>
 * 
 * @author atta_troll
 *
 */
@FunctionalInterface
public interface LearningFunction {

    /**
     * Minimal possible value for learning function.
     */
    double EPS = .0;

    /**
     * Calculates value of learning function.
     * @param epoch current learning epoch (time coordinate).
     * @return value of function
     */
    double calculate(int epoch);

}
