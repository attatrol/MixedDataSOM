package com.github.attatrol.som.som;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.Record;

/**
 * Result of applying a trained SOM to a data source.
 * Data source instances now are mapped to neurons, so this class
 * holds such map.
 * @author atta_troll
 *
 */
public class SomClusterResult {

    /**
     * Maps record indexes to cluster indexes.
     */
    private final Map<Long, Integer> clusterMap;

    /**
     * Maps neurons to cluster indexes.
     */
    private final Map<Integer, Neuron> clusterToNeuronMap;

    /**
     * Maps cluster indexes to neurons.
     */
    private final Map<Neuron, Integer> neuronToClusterMap;

    /**
     * Private ctor, use {{@link #produceClusterResult(Som, AbstractTokenDataSource)}
     * @param clusterMap
     * @param clusterToNeuronMap
     * @param neuronToClusterMap
     */
    public SomClusterResult(Map<Long, Integer> clusterMap, Map<Integer, Neuron> clusterToNeuronMap,
            Map<Neuron, Integer> neuronToClusterMap) {
        this.clusterMap = clusterMap;
        this.clusterToNeuronMap = clusterToNeuronMap;
        this.neuronToClusterMap = neuronToClusterMap;
    }

    /**
     * 
     * @param record
     * @return
     */
    public Integer getCluster(Record<Object[]> record) {
        return clusterMap.get(record.getIndex());
    }

    public Neuron getNeuron(Record<Object[]> record) {
        return clusterToNeuronMap.get(getCluster(record));
    }

    public List<Record<Object[]>> getClusterRecords(AbstractTokenDataSource<?> dataSource,
            int clusterIndex, int numberOfRecords) throws IOException {
        List<Record<Object[]>> records = new ArrayList<>(numberOfRecords);
        int counter = 0;
        dataSource.reset();
        while (dataSource.hasNext() && counter < numberOfRecords ) {
            final Record<Object[]> record = dataSource.next();
            if (clusterIndex == getCluster(record)) {
                records.add(record);
                counter++;
            }
        }
        return records;
    }

    public List<Record<Object[]>> getClusterRecords(AbstractTokenDataSource<?> dataSource,
            Neuron neuron, int numberOfRecords) throws IOException {
        return getClusterRecords(dataSource, neuronToClusterMap.get(neuron), numberOfRecords);
    }

    public long getClusterSize(int clusterIndex) {
        long counter = 0;
        for (Map.Entry<Long, Integer> entry : clusterMap.entrySet()) {
            if (entry.getValue() == clusterIndex) {
                counter++;
            }
        }
        return counter;
    }

    public long getClusterSize(Neuron neuron) {
        return getClusterSize(neuronToClusterMap.get(neuron));
    }
    /**
     * Factory method for SOM cluster result
     * @param som SOM
     * @param dataSource data source in use
     * @return SOM cluster result instance
     * @throws IOException on data source i/o error
     */
    public static SomClusterResult produceClusterResult(Som som,
            AbstractTokenDataSource<?> dataSource) throws IOException {
        Map<Long, Integer> clusterMap = new HashMap<>();
        Map<Integer, Neuron> clusterToNeuronMap = new HashMap<>();
        final List<Neuron> neurons = som.getNeurons();
        for (int i = 0; i < neurons.size(); i++) {
            clusterToNeuronMap.put(i, neurons.get(i));
        }
        Map<Neuron, Integer> neuronToClusterMap = new HashMap<>();
        clusterToNeuronMap.forEach((cluster, neuron) -> neuronToClusterMap.put(neuron, cluster));
        dataSource.reset();
        while (dataSource.hasNext()) {
            final Record<Object[]> record = dataSource.next();
            final Neuron bmu = som.getBmu(record);
            clusterMap.put(record.getIndex(), neuronToClusterMap.get(bmu));
        }
        return new SomClusterResult(clusterMap, clusterToNeuronMap, neuronToClusterMap);
    }
}
