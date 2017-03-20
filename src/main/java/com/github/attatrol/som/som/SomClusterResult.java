package com.github.attatrol.som.som;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.Record;
import com.github.attatrol.som.som.neuron.AbstractNeuron;

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
    private final Map<Integer, AbstractNeuron> clusterToNeuronMap;

    /**
     * Maps cluster indexes to neurons.
     */
    private final Map<AbstractNeuron, Integer> neuronToClusterMap;

    /**
     * Private ctor, use {{@link #produceClusterResult(Som, AbstractTokenDataSource)}
     * @param clusterMap
     * @param clusterToNeuronMap
     * @param neuronToClusterMap
     */
    public SomClusterResult(Map<Long, Integer> clusterMap, Map<Integer, AbstractNeuron> clusterToNeuronMap,
            Map<AbstractNeuron, Integer> neuronToClusterMap) {
        this.clusterMap = clusterMap;
        this.clusterToNeuronMap = clusterToNeuronMap;
        this.neuronToClusterMap = neuronToClusterMap;
    }

    /**
     * Assigns actual cluster index to the record.
     * @param record record from data source
     * @return cluster index
     */
    public Integer getCluster(Record<Object[]> record) {
        return clusterMap.get(record.getIndex());
    }

    /**
     * Returns neuron that holds the record
     * @param record record record from data source
     * @return neuron
     */
    public AbstractNeuron getNeuron(Record<Object[]> record) {
        return clusterToNeuronMap.get(getCluster(record));
    }

    /**
     * @return total number of clusters, including empty ones
     */
    public int getClusterNumber() {
        return clusterMap.size();
    }

    /**
     * Generates list of records that belong to this cluster
     * @param dataSource data source
     * @param clusterIndex index of cluster
     * @param numberOfRecords limit of records to be put in list
     * @return list of records
     * @throws IOException in case of internal data source failure
     */
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

    /**
     * Generates list of records that belong to this neuron.
     * @param dataSource data source
     * @param neuron neuron
     * @param numberOfRecords limit of records to be put in list
     * @return list of records
     * @throws IOException in case of internal data source failure
     */
    public List<Record<Object[]>> getClusterRecords(AbstractTokenDataSource<?> dataSource,
            AbstractNeuron neuron, int numberOfRecords) throws IOException {
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

    /**
     * @param neuron neuron
     * @return size of cluster associated with this neuron
     */
    public long getClusterSize(AbstractNeuron neuron) {
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
        Map<Integer, AbstractNeuron> clusterToNeuronMap = new HashMap<>();
        final List<AbstractNeuron> neurons = som.getNeurons();
        for (int i = 0; i < neurons.size(); i++) {
            clusterToNeuronMap.put(i, neurons.get(i));
        }
        Map<AbstractNeuron, Integer> neuronToClusterMap = new HashMap<>();
        clusterToNeuronMap.forEach((cluster, neuron) -> neuronToClusterMap.put(neuron, cluster));
        dataSource.reset();
        while (dataSource.hasNext()) {
            final Record<Object[]> record = dataSource.next();
            final AbstractNeuron bmu = som.getBmu(record);
            clusterMap.put(record.getIndex(), neuronToClusterMap.get(bmu));
        }
        return new SomClusterResult(clusterMap, clusterToNeuronMap, neuronToClusterMap);
    }
}
