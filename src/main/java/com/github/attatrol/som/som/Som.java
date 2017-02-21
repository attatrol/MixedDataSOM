package com.github.attatrol.som.som;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.Record;
import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.som.som.functions.learning.LearningFunction;
import com.github.attatrol.som.som.functions.neighbourhood.NeighborhoodFunction;
import com.github.attatrol.som.som.neuron.AbstractNeuron;
import com.github.attatrol.som.som.neuron.FuzzyNeuron;
import com.github.attatrol.som.som.topology.Point;
import com.github.attatrol.som.som.topology.SomTopology;

/**
 * 
 * @author atta_troll
 *
 */
public class Som {

    private final SomTopology topology;

    private final List<AbstractNeuron> neurons;

    private final AbstractTokenDataSource<?> dataSource;

    private final DistanceFunction distanceFunction;

    private NeighborhoodFunction neighborhoodFunction;

    private LearningFunction learningFunction;

    public Som(List<AbstractNeuron> neurons, SomTopology topology,
            AbstractTokenDataSource<?> dataSource,
            DistanceFunction distanceFunction,
            NeighborhoodFunction neighborhoodFunction,
            LearningFunction learningFunction) {
        this.neurons = neurons;
        this.topology = topology;
        this.dataSource = dataSource;
        this.distanceFunction = distanceFunction;
        this.neighborhoodFunction = neighborhoodFunction;
        this.learningFunction = learningFunction;
    }

    /**
     * Learns SOM through complete iteration over data source.
     * @return average error of SOM
     * @throws IOException on data source i/o error
     */
    public double learnEpoch(int epochNumber) throws IOException {
        final Map<Double, Double> speedFactorMap =
                calculateSpeedFactorMap(epochNumber);
        double errorSum = 0.;
        long counter = 0L;
        dataSource.reset();
        while (dataSource.hasNext()) {
            errorSum += learn(dataSource.next(), speedFactorMap);
            counter++;
        }
        return errorSum / counter;
    }

    /**
     * Returns BMU for an input record. Method doesn't change SOM state and is
     * intended to be used by external consumers.
     * @param record input record
     * @return BMU
     */
    public AbstractNeuron getBmu(Record<Object[]> record) {
        final Object[] data = record.getData();
        AbstractNeuron bmu = neurons.get(0);
        double bmuDistance = distanceFunction.calculate(neurons.get(0).getWeights(), data);
        for (int i = 1; i < neurons.size(); i++) {
            final AbstractNeuron neuron = neurons.get(i);
            final double distance = distanceFunction.calculate(neuron.getWeights(), data);
            if (distance < bmuDistance) {
                bmuDistance = distance;
                bmu = neuron;
            }
        }
        return bmu;
    }

    /**
     * @return shallow copy of neuron list.
     */
    public List<AbstractNeuron> getNeurons() {
        return new ArrayList<>(neurons);
    }

    /**
     * Calculates speed factors for current epoch. They are presented as a map
     * where keys are distances between neurons.
     * @param epochNumber current epoch number
     * @return speed factors
     */
    private Map<Double, Double> calculateSpeedFactorMap(int epochNumber) {
        final double learningFunctionValue = learningFunction.calculate(epochNumber);
        final Map<Double, Double> decrementFactorMap =
                new HashMap<>();
        for (AbstractNeuron neuron1 : neurons) {
            for (AbstractNeuron neuron2 : neurons) {
                final double distance = topology.getDistance(neuron1.getPosition(),
                        neuron2.getPosition());
                if (!decrementFactorMap.containsKey(distance)) {
                    decrementFactorMap.put(distance,
                            neighborhoodFunction.calculate(distance, epochNumber)
                            * learningFunctionValue);
                }
            }
        }
        return decrementFactorMap;
    }

    /**
     * Executes single step of SOM learning.
     * @param data a record (incoming vector)
     * @param speedFactorMap map that contains speed factors for weight changes for every
     * distance for current epoch
     * @return distance between neuron and incoming vector
     */
    private double learn(Record<Object[]> record, Map<Double, Double> speedFactorMap) {
        final Object[] data = record.getData();
        AbstractNeuron bmu = neurons.get(0);
        double bmuDistance = distanceFunction.calculate(neurons.get(0).getWeights(), data);
        for (int i = 1; i < neurons.size(); i++) {
            final AbstractNeuron neuron = neurons.get(i);
            final double distance = distanceFunction.calculate(neuron.getWeights(), data);
            if (distance < bmuDistance) {
                bmuDistance = distance;
                bmu = neuron;
            }
        }
        final Point bmuPosition = bmu.getPosition();
        for (AbstractNeuron neuron : neurons) {
            final double distance = topology.getDistance(bmuPosition,
                    neuron.getPosition());
            neuron.changeWeights(data, speedFactorMap.get(distance), bmu == neuron);
        }
        return bmuDistance;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SOM =");
        for (AbstractNeuron neuron : neurons) {
            sb.append('\n').append(neuron);
        }
        return sb.append("]").toString();
    }
}
