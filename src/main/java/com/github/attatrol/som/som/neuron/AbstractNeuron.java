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
    protected Point position;

    /**
     * Token types of the weights, not subject of change.
     */
    protected TokenType[] tokenTypes;

    /**
     * Sinple value ctor.
     * @param weights
     * @param position
     * @param tokenTypes
     */
    public AbstractNeuron(Object[] weights, Point position, TokenType[] tokenTypes) {
        this.weights = weights;
        this.position = position;
        this.tokenTypes = tokenTypes;
    }

    public Point getPosition() {
        return position;
    }

    public Object[] getWeights() {
        return weights;
    } 

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
