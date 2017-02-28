package com.github.attatrol.som.som;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.Record;
import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.som.som.functions.learning.LearningFunction;
import com.github.attatrol.som.som.functions.neighbourhood.NeighborhoodFunction;
import com.github.attatrol.som.som.neuron.AbstractNeuron;
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

    private final double globalPatronageFactor;

    private final long dataSourceSize;

    private NeighborhoodFunction neighborhoodFunction;

    private LearningFunction learningFunction;

    private Map<AbstractNeuron, Long> winCount = new HashMap<>();

    private Map<AbstractNeuron, Long> oldWinCount = new HashMap<>();

    Map<AbstractNeuron, List<Boolean>> patrons = new HashMap<>();

    public Som(List<AbstractNeuron> neurons, SomTopology topology,
            AbstractTokenDataSource<?> dataSource,
            DistanceFunction distanceFunction,
            NeighborhoodFunction neighborhoodFunction,
            LearningFunction learningFunction,
            double winnerHandicap,
            long dataSourceSize) {
        this.neurons = neurons;
        this.topology = topology;
        this.dataSource = dataSource;
        this.globalPatronageFactor = winnerHandicap;
        this.dataSourceSize = dataSourceSize;
        this.distanceFunction = distanceFunction;
        this.neighborhoodFunction = neighborhoodFunction;
        this.learningFunction = learningFunction;
        for (AbstractNeuron neuron : neurons) {
            winCount.put(neuron, 0L);
            oldWinCount.put(neuron, 0L);
        }
    }

    /**
     * Learns SOM through complete iteration over data source.
     * @return average error of SOM
     * @throws IOException on data source i/o error
     */
    public double learnEpoch(int epochNumber) throws IOException {
        final Map<Double, Double> speedFactorMap =
                calculateSpeedFactorMap(epochNumber);
        updatePressureAdjustments(speedFactorMap);
        Map<AbstractNeuron, Long> temp = winCount;
        winCount = oldWinCount;
        oldWinCount = temp;
        for (AbstractNeuron neuron : neurons) {
            winCount.put(neuron, 0L);
        }
        System.out.println("Epoch");
        double errorSum = 0.;
        long counter = 0L;
        dataSource.reset();
        while (dataSource.hasNext()) {
            errorSum += learn(dataSource.next(), speedFactorMap);
            counter++;
        }
        for (AbstractNeuron neuron : neurons) {
            neuron.markEpochEnd();
        }
        final double avgError = errorSum / counter;
        System.out.println(avgError + " " + counter);
        return avgError;
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
     * @return distance function, it is state-less.
     */
    public DistanceFunction getDistanceFunction() {
        return distanceFunction;
    }

    /**
     * Updates pressure adjustments according to the original algorithm.
     * @param speedFactorMap 
     * @param counter 
     */
    private void updatePressureAdjustments(Map<Double, Double> speedFactorMap) {
        Map<AbstractNeuron, AbstractNeuron> patronageMap = new HashMap<>();
        for (AbstractNeuron neuron : neurons) {
            double patronFactor = 0.;
            AbstractNeuron patron = null;
            for (AbstractNeuron neuron1 : neurons) {
                if (winCount.get(neuron) < winCount.get(neuron1)) {
                    final double distance = topology.getDistance(neuron1.getPosition(),
                            neuron.getPosition());
                    final double patronFactorCandidate = speedFactorMap.get(distance)
                            * speedFactorMap.get(distance)
                            * winCount.get(neuron1);
                    if (patronFactorCandidate > patronFactor) {
                        patronFactor = patronFactorCandidate;
                        patron = neuron1;
                    }
                }
            }
            patronageMap.put(neuron, patron);
        }
        patrons.clear();
        Map<AbstractNeuron, Set<AbstractNeuron>> patronsMap = new HashMap<>();
        for (Map.Entry<AbstractNeuron, AbstractNeuron> entry : patronageMap.entrySet()) {
            Set<AbstractNeuron> patroned = patronsMap.get(entry.getValue());
            if (patroned == null) {
                patroned = new HashSet<>();
                patronsMap.put(entry.getValue(), patroned);
            }
            patroned.add(entry.getKey());
        }
        for (AbstractNeuron patron : patronsMap.keySet()) {
            List<Boolean> vector = new ArrayList<>();
            Set<AbstractNeuron> patroned = patronsMap.get(patron);
            patrons.put(patron, vector);
            for (AbstractNeuron neuron : neurons) {
                vector.add(patroned.contains(neuron));
            }
        }
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
     * @param speedFactorMap map that contains speed factors for weight changes
     * @param counter record count in this epoch
     * distance for current epoch
     * @return distance between neuron and incoming vector
     */
    private double learn(Record<Object[]> record, Map<Double, Double> speedFactorMap) {
        final Object[] data = record.getData();
        AbstractNeuron bmu = neurons.get(0);
        double bmuDistance = distanceFunction.calculate(bmu.getWeights(), data);
        for (int i = 1; i < neurons.size(); i++) {
            final AbstractNeuron neuron = neurons.get(i);
            final double weightDistance = distanceFunction.calculate(neuron.getWeights(), data);
            if (weightDistance < bmuDistance) {
                bmuDistance = weightDistance;
                bmu = neuron;
            }
        }
        winCount.put(bmu, winCount.get(bmu) + 1);
        final Point bmuPosition = bmu.getPosition();
        final List<Boolean> patronedVector = patrons.get(bmu);
        for (int i = 0; i < neurons.size(); i++) {
            final AbstractNeuron neuron = neurons.get(i);
            final double distance = topology.getDistance(bmuPosition,
                    neuron.getPosition());
            final double patronageFactor = patronedVector != null && patronedVector.get(i) ? 1. + globalPatronageFactor : 1.;
            neuron.changeWeights(data, speedFactorMap.get(distance) * patronageFactor
                    , bmu == neuron);
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
