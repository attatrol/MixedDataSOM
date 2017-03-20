package com.github.attatrol.som.som.neuron;

import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.som.som.topology.Point;

/**
 * Prototype for any neuron
 * @author atta_troll
 *
 */
public abstract class AbstractNeuron {

    /**
     * Current neuron weights, mutable.
     */
    protected Object[] weights;

    /**
     * Topological position of the neuron.
     */
    protected final Point position;

    /**
     * Token types of the weights, not subject of change.
     */
    protected TokenType[] tokenTypes;

    /**
     * Simple value ctor.
     * @param weights starting weights of a neuron
     * @param position topological position of a neuron
     * @param tokenTypes weights token types
     */
    public AbstractNeuron(Object[] weights, Point position, TokenType[] tokenTypes) {
        this.weights = weights;
        this.position = position;
        this.tokenTypes = tokenTypes;
    }

    /**
     * @return position of neuron
     */
    public Point getPosition() {
        return position;
    }

    /**
     * @return weights of the neuron
     */
    public Object[] getWeights() {
        return weights;
    }

    /**
     * Swaps weights of 2 neurons.
     * @param other other neuron
     */
    public abstract void swapWeights(AbstractNeuron other);

    /**
     * Main procedure of a learning cycle.
     * @param newWeights
     * @param diminishingFactor
     * @param isBmu
     */
    public abstract void changeWeights(Object[] newWeights, double diminishingFactor, boolean isBmu);

    /**
     * Tells neuron that the epoch has ended.
     */
    public abstract void markEpochEnd();

    /**
     * Assigns new weights to the neuron.
     * @param newWeights new weights values
     */
    public abstract void setNewWeights(Object[] newWeights);

    /**
     * Neurons are mapped 1-to-1 to their position, so return
     * position's hash code.<p/>
     * {@inheritDoc}
     * 
     */
    @Override
    public int hashCode() {
        return position.hashCode();
    }

    @Override
    public String toString() {
        /*
        StringBuilder sb = new StringBuilder();
        sb.append("Neuron = [");
        for (Object weight : weights) {
            sb.append(weight).append(", ");
        }
        return sb.append("]").toString();
        */
        return position.toString();
    }
}
