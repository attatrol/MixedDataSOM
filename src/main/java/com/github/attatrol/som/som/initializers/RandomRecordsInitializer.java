package com.github.attatrol.som.som.initializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.preprocessing.ui.TokenDataSourceAndMisc;
import com.github.attatrol.som.som.Som;
import com.github.attatrol.som.som.functions.learning.LearningFunction;
import com.github.attatrol.som.som.functions.neighbourhood.NeighborhoodFunction;
import com.github.attatrol.som.som.neuron.AbstractNeuron;
import com.github.attatrol.som.som.neuron.FuzzyNeuron;
import com.github.attatrol.som.som.topology.Point;
import com.github.attatrol.som.som.topology.SomTopology;

/**
 * This initializer randomly picks some vector from data source and uses its values
 * to initialize neurons' weights.
 * <b>Must be used with non-empty data sources, data set must be checked to have at
 * least single record in it before usage of this.</b>
 * @author atta_troll
 *
 */
public class RandomRecordsInitializer implements SomInitializer {

    @Override
    public synchronized Som createSom(TokenDataSourceAndMisc tdsm, DistanceFunction distanceFunction,
            SomTopology topology, NeighborhoodFunction neighborhoodFunction,
            LearningFunction learningFunction)
            throws IOException {
        final AbstractTokenDataSource<?> dataSource = tdsm.getTokenDataSource();
        final long numberOfRecords = getNumberOfRecords(dataSource);
        final List<Point> neuronPositions = topology.getNeuronPositions();
        final TokenType[] tokenTypes = tdsm.getTokenTypes();
        final Map<Object, Double>[] sampleFrequencies = SampleFrequencyCalculator
                .getSampleFrequencies(dataSource, tokenTypes);
        List<AbstractNeuron> neurons = new ArrayList<>();
        for (Point position : neuronPositions) {
            neurons.add(new FuzzyNeuron(getRandomData(dataSource,
                    numberOfRecords), position, tokenTypes, sampleFrequencies));
        }
        return new Som(neurons, topology, dataSource, distanceFunction,
                neighborhoodFunction, learningFunction);
    }

    /**
     * Calculates number of records in data source.
     * @param dataSource data source
     * @return number of records in data source
     * @throws IOException on data source i/o error
     */
    private static long getNumberOfRecords(AbstractTokenDataSource<?> dataSource)
            throws IOException {
        dataSource.reset();
        long counter = 0;
        while (dataSource.hasNext()) {
            dataSource.next();
            counter++;
        }
        return counter;
    }

    /**
     * Randomly chooses record from data source
     * @param dataSource data source
     * @param numberOfRecords total number of records in data source
     * @return a random record
     * @throws IOException on data source i/o error
     */
    private Object[] getRandomData(AbstractTokenDataSource<?> dataSource,
            long numberOfRecords)
                    throws IOException {
        dataSource.reset();
        long counter = ThreadLocalRandom.current().nextLong(numberOfRecords);
        while(--counter > 0) {
            dataSource.next();
        }
        return dataSource.next().getData();
    }
}
