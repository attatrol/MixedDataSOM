package com.github.attatrol.som.som.neuron;

import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.som.som.topology.Point;

public abstract class AbstractNeuron {

    protected Object[] weights;

    protected Point position;

    protected TokenType[] tokenTypes;

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

    public abstract void changeWeights(Object[] newWeights, double diminishingFactor, boolean isBmu);

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
        StringBuilder sb = new StringBuilder();
        sb.append("Neuron = [");
        for (Object weight : weights) {
            sb.append(weight).append(", ");
        }
        return sb.append("]").toString();
    }
}
