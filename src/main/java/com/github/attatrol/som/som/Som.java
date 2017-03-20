package com.github.attatrol.som.som;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

    private static final double LOCAL_NEIGHBORHOOD_RADIUS = 1.01;

    private final SomTopology topology;

    private final List<AbstractNeuron> neurons;

    private final AbstractTokenDataSource<?> dataSource;

    private final DistanceFunction distanceFunction;

    @SuppressWarnings("unused")
    private final long dataSourceSize;

    private NeighborhoodFunction neighborhoodFunction;

    private LearningFunction learningFunction;

    private Map<AbstractNeuron, Long> winCount = new HashMap<>();

    private Map<AbstractNeuron, Object[]> distantRecords = new HashMap<>();

    private final double medianClusterSize;

    private double overMedianWeakFactor;

    private double overMedianStrongFactor;

    private final Map<AbstractNeuron, List<AbstractNeuron>> localNeighborhoods = new HashMap<>();

    public Som(List<AbstractNeuron> neurons, SomTopology topology, AbstractTokenDataSource<?> dataSource,
            DistanceFunction distanceFunction, NeighborhoodFunction neighborhoodFunction,
            LearningFunction learningFunction, long dataSourceSize, double overMedianWeakFactor,
            double overMedianStrongFactor) {
        this.neurons = neurons;
        this.topology = topology;
        this.dataSource = dataSource;
        this.dataSourceSize = dataSourceSize;
        this.overMedianWeakFactor = overMedianWeakFactor;
        this.overMedianStrongFactor = overMedianStrongFactor;
        this.distanceFunction = distanceFunction;
        this.neighborhoodFunction = neighborhoodFunction;
        this.learningFunction = learningFunction;
        for (AbstractNeuron neuron : neurons) {
            winCount.put(neuron, 0L);
            List<AbstractNeuron> localNeighborhood = new ArrayList<>();
            for (AbstractNeuron neuron1 : neurons) {
                if (neuron1 != neuron && topology.getDistance(neuron.getPosition(),
                        neuron1.getPosition()) <= LOCAL_NEIGHBORHOOD_RADIUS) {
                    localNeighborhood.add(neuron1);
                }
            }
            localNeighborhoods.put(neuron, localNeighborhood);
        }
        medianClusterSize = ((double) dataSourceSize) / neurons.size();
    }

    /**
     * Learns SOM through complete iteration over data source.
     * 
     * @return average error of SOM
     * @throws IOException
     *             on data source i/o error
     */
    public double learnEpoch(int epochNumber) throws IOException {
        final Map<Double, Double> speedFactorMap = calculateSpeedFactorMap(epochNumber);
        // updatePatronageIndexes();
        devourWeakNeurons();
        distantRecords.clear();
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
     * 
     * @param record
     *            input record
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
     * Moves weak neurons towards strong ones (patrons), then assigns to them some values
     * from strong ones. Each strong neuron may affect only one weak.
     */
    private void devourWeakNeurons() {
        // 1. create sets of empty neurons and their patrons
        Set<AbstractNeuron> weakNeurons = new HashSet<>();
        TreeMap<Long, AbstractNeuron> patrons = new TreeMap<>();
        for (Map.Entry<AbstractNeuron, Long> entry : winCount.entrySet()) {
            final long winCount = entry.getValue();
            if (winCount <= overMedianWeakFactor * medianClusterSize) {
                weakNeurons.add(entry.getKey());
            } else if (winCount >= overMedianStrongFactor * medianClusterSize) {
                patrons.put(winCount, entry.getKey());
            }
        }
        // 2. pairing patrons to the closest empty neurons
        List<AbstractNeuron> patronList = new ArrayList<>();
        List<AbstractNeuron> patronedList = new ArrayList<>();
        for (Map.Entry<Long, AbstractNeuron> entry : patrons.entrySet()) {
            if (weakNeurons.isEmpty()) {
                break;
            } else {
                final AbstractNeuron patron = entry.getValue();
                Iterator<AbstractNeuron> iterator = weakNeurons.iterator();
                AbstractNeuron closestEmptyNeuron = iterator.next();
                double minDistance = topology.getDistance(patron.getPosition(), closestEmptyNeuron.getPosition());
                while (iterator.hasNext()) {
                    final AbstractNeuron emptyNeuron = iterator.next();
                    final double distance = topology.getDistance(patron.getPosition(), emptyNeuron.getPosition());
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestEmptyNeuron = emptyNeuron;
                    }
                }
                patronList.add(patron);
                patronedList.add(closestEmptyNeuron);
            }
        }
        for (AbstractNeuron patron : patronList) {
            if (distantRecords.get(patron) == null) {
                System.out.println("Very bad!");
            }
        }
        // 3. finding path from empty neurons to the patron's local neighborhood
        // (greedy algorithm)
        for (int j = 0; j < patronList.size(); j++) {
            final AbstractNeuron patron = patronList.get(j);
            final AbstractNeuron patronized = patronedList.get(j);
            List<AbstractNeuron> path = new ArrayList<>();
            AbstractNeuron current = patronized;
            while (current != patron) {
                path.add(current);
                final List<AbstractNeuron> localNeighborhood = localNeighborhoods.get(current);
                AbstractNeuron pathNeuron = localNeighborhood.get(0);
                double minDistance = topology.getDistance(pathNeuron.getPosition(), patron.getPosition());
                for (int i = 1; i < localNeighborhood.size(); i++) {
                    final AbstractNeuron neighbor = localNeighborhood.get(i);
                    final double distance = topology.getDistance(neighbor.getPosition(), patron.getPosition());
                    if (distance < minDistance) {
                        minDistance = distance;
                        pathNeuron = neighbor;
                    }
                }
                current = pathNeuron;
            }
            // 4. propagation of empty neuron towards patron
            for (int i = 0; i < path.size() - 1; i++) {
                AbstractNeuron swap1 = path.get(i);
                AbstractNeuron swap2 = path.get(i + 1);
                for (int k = j + 1; k < patronList.size(); k++) {
                    boolean patronSwap = false;
                    if (swap1 == patronList.get(k)) {
                        patronList.set(k, swap2);
                        patronSwap = true;
                    } else if (swap1 == patronedList.get(k)) {
                        patronedList.set(k, swap2);
                    }
                    if (swap2 == patronList.get(k)) {
                        patronList.set(k, swap1);
                        patronSwap = true;
                    } else if (swap2 == patronedList.get(k)) {
                        patronedList.set(k, swap1);
                    }
                    if (patronSwap) {
                        System.out.println(String.format("patron swap %s - %s", swap1, swap2));
                        final Object[] distant1 = distantRecords.get(swap1);
                        final Object[] distant2 = distantRecords.get(swap2);
                        if (distant1 != null) {
                            distantRecords.put(swap2, distant1);
                        }
                        if (distant2 != null) {
                            distantRecords.put(swap1, distant2);
                        }
                    }
                }
                swap1.swapWeights(swap2);
            }
            // 5. giving a value to the dead neurons
            final Object[] newWeights = distantRecords.get(patron);
            if (newWeights == null) {
                System.out.println("Bad");
            }
            patronized.setNewWeights(newWeights);
        }
    }

    /**
     * Calculates speed factors for current epoch. They are presented as a map
     * where keys are distances between neurons.
     * 
     * @param epochNumber
     *            current epoch number
     * @return speed factors
     */
    private Map<Double, Double> calculateSpeedFactorMap(int epochNumber) {
        final double learningFunctionValue = learningFunction.calculate(epochNumber);
        final Map<Double, Double> decrementFactorMap = new HashMap<>();
        for (AbstractNeuron neuron1 : neurons) {
            for (AbstractNeuron neuron2 : neurons) {
                final double distance = topology.getDistance(neuron1.getPosition(), neuron2.getPosition());
                if (!decrementFactorMap.containsKey(distance)) {
                    decrementFactorMap.put(distance,
                            neighborhoodFunction.calculate(distance, epochNumber) * learningFunctionValue);
                }
            }
        }
        return decrementFactorMap;
    }

    /**
     * Executes single step of SOM learning.
     * 
     * @param data
     *            a record (incoming vector)
     * @param speedFactorMap
     *            map that contains speed factors for weight changes
     * @param counter
     *            record count in this epoch distance for current epoch
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
        for (int i = 0; i < neurons.size(); i++) {
            final AbstractNeuron neuron = neurons.get(i);
            final double distance = topology.getDistance(bmuPosition, neuron.getPosition());
            neuron.changeWeights(data, speedFactorMap.get(distance), bmu == neuron);
        }
        Object[] distantData = distantRecords.get(bmu);
        if (distantData == null) {
            distantRecords.put(bmu, data);
        } else {
            final double currentDistance = distanceFunction.calculate(bmu.getWeights(), data);
            final double oldDistance = distanceFunction.calculate(bmu.getWeights(), distantData);
            if (currentDistance > oldDistance) {
                distantRecords.put(bmu, data);
            }
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
