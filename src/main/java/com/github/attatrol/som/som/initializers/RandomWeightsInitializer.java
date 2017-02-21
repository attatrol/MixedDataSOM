
package com.github.attatrol.som.som.initializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
 * This initializer does:<br/>
 * 1. Finds all possible values (ranges) of tokens.<br/>
 * 2. Randomly picks them as neuron weights.br/> <b>Must be used with non-empty data sources, data
 * set must be checked to have at least single record in it before usage of this.</b>
 * 
 * @author atta_troll
 *
 */
public class RandomWeightsInitializer implements SomInitializer {

    @Override
    public synchronized Som createSom(TokenDataSourceAndMisc tdsm,
            DistanceFunction distanceFunction, SomTopology topology,
            NeighborhoodFunction neighborhoodFunction, LearningFunction learningFunction)
            throws IOException {
        final TokenType[] tokenTypes = tdsm.getTokenTypes();
        final AbstractTokenDataSource<?> dataSource = tdsm.getTokenDataSource();
        final AbstractRandomValueProducer<?>[] initialValueProducers =
                getRandomValueProducers(dataSource, tokenTypes);
        final List<Point> neuronPositions = topology.getNeuronPositions();
        final int recordLength = dataSource.getRecordLength();
        final Map<Object, Double>[] sampleFrequencies = SampleFrequencyCalculator
                .getSampleFrequencies(dataSource, tokenTypes);
        List<AbstractNeuron> neurons = new ArrayList<>();
        for (Point position : neuronPositions) {
            Object[] weights = new Object[recordLength];
            for (int i = 0; i < recordLength; i++) {
                weights[i] = initialValueProducers[i].produceValue();
            }
            neurons.add(new FuzzyNeuron(weights, position, tokenTypes, sampleFrequencies));
        }
        return new Som(neurons, topology, dataSource, distanceFunction, neighborhoodFunction,
                learningFunction);
    }

    /**
     * Creates random value producers for each token
     * @param dataSource current data source
     * @param tokenTypes array of token types
     * @return array of random value producers
     */
    private AbstractRandomValueProducer<?>[]
            getRandomValueProducers(AbstractTokenDataSource<?> dataSource, TokenType[] tokenTypes) {
        final int recordLength = dataSource.getRecordLength();
        AbstractRandomValueProducer<?>[] initialValueProducers =
                new AbstractRandomValueProducer<?>[recordLength];
        for (int i = 0; i < recordLength; i++) {
            switch (tokenTypes[i]) {
            case INTEGER:
                initialValueProducers[i] = new IntegerRandomValueProducer(dataSource, i);
                break;
            case FLOAT:
                initialValueProducers[i] = new DoubleRandomValueProducer(dataSource, i);
                break;
            case BINARY:
            case BINARY_DIGITAL:
                initialValueProducers[i] = new BooleanRandomValueProducer(dataSource, i);
                break;
            case CATEGORICAL_STRING:
                initialValueProducers[i] =
                        new CategoricalRandomValueProducer<String>(dataSource, i);
                break;
            default:
                initialValueProducers[i] = new StubRandomValueProducer(dataSource, i);
                break;
            }
        }
        return initialValueProducers;
    }

    /**
     * Base class for any random value producer.
     * @author atta_troll
     *
     * @param <V> tupe of token value
     */
    private abstract class AbstractRandomValueProducer<V> {

        /**
         * Current data source.
         */
        protected final AbstractTokenDataSource<?> dataSource;

        /**
         * Token index in record.
         */
        protected final int index;

        /**
         * Randomizer instance.
         */
        protected final Random random = new Random();

        public AbstractRandomValueProducer(AbstractTokenDataSource<?> dataSource, int index) {
            this.dataSource = dataSource;
            this.index = index;
        }

        public abstract V produceValue() throws IOException;
    }

    /**
     * Random value producer for any categorical data.
     * @author atta_troll
     *
     * @param <V> type of categorical data
     */
    private class CategoricalRandomValueProducer<V> extends AbstractRandomValueProducer<V> {

        private boolean isReady;

        private List<V> values;

        public CategoricalRandomValueProducer(AbstractTokenDataSource<?> dataSource, int index) {
            super(dataSource, index);
        }

        @Override
        public V produceValue() throws IOException {
            if (!isReady) {
                prepare();
            }
            return values.get(random.nextInt(values.size()));
        }

        /**
         * Creates a set of all unique values of target token.
         * @throws IOException on i/o data source error
         */
        @SuppressWarnings("unchecked")
        private void prepare() throws IOException {
            Set<V> possibleValues = new HashSet<>();
            dataSource.reset();
            while (dataSource.hasNext()) {
                possibleValues.add((V) dataSource.next().getData()[index]);
            }
            values = new ArrayList<>(possibleValues);
        }
    }

    /**
     * Random value producer for integer values.
     * @author atta_troll
     *
     */
    private class IntegerRandomValueProducer extends AbstractRandomValueProducer<Integer> {

        private boolean isReady;

        private int minValue;

        private int maxValue;

        public IntegerRandomValueProducer(AbstractTokenDataSource<?> dataSource, int index) {
            super(dataSource, index);
        }

        @Override
        public Integer produceValue() throws IOException {
            if (!isReady) {
                prepare();
            }
            Integer result = minValue + random.nextInt(maxValue - minValue + 1);
            return result;
        }

        /**
         * Figures out integer range in data source.
         * @throws IOException on i/o data source error
         */
        private void prepare() throws IOException {
            dataSource.reset();
            if (dataSource.hasNext()) {
                final Integer currentValue = (Integer) dataSource.next().getData()[index];
                minValue = currentValue;
                maxValue = currentValue;
            }
            while (dataSource.hasNext()) {
                final Integer currentValue = (Integer) dataSource.next().getData()[index];
                if (currentValue < minValue) {
                    minValue = currentValue;
                }
                else if (currentValue > maxValue) {
                    maxValue = currentValue;
                }
            }
        }
    }

    /**
     * Random value producer for double values.
     * @author atta_troll
     *
     */
    private class DoubleRandomValueProducer extends AbstractRandomValueProducer<Double> {

        private boolean isReady;

        private double minValue;

        private double maxValue;

        public DoubleRandomValueProducer(AbstractTokenDataSource<?> dataSource, int index) {
            super(dataSource, index);
        }

        @Override
        public Double produceValue() throws IOException {
            if (!isReady) {
                prepare();
            }
            return minValue + random.nextDouble() * (maxValue - minValue);
        }

        /**
         * Figures out range of double values in data source.
         * @throws IOException on i/o data source error
         */
        private void prepare() throws IOException {
            dataSource.reset();
            if (dataSource.hasNext()) {
                final Double currentValue = (Double) dataSource.next().getData()[index];
                minValue = currentValue;
                maxValue = currentValue;
            }
            while (dataSource.hasNext()) {
                final Double currentValue = (Double) dataSource.next().getData()[index];
                if (currentValue < minValue) {
                    minValue = currentValue;
                }
                else if (currentValue > maxValue) {
                    maxValue = currentValue;
                }
            }
        }
    }

    /**
     * Random value producer for boolean values.
     * @author atta_troll
     *
     */
    private class BooleanRandomValueProducer extends AbstractRandomValueProducer<Boolean> {

        public BooleanRandomValueProducer(AbstractTokenDataSource<?> dataSource, int index) {
            super(dataSource, index);
        }

        @Override
        public Boolean produceValue() throws IOException {
            return random.nextBoolean();
        }
    }

    /**
     * Stub random value producer, its {@link #produceValue()} method returns {@code null}.
     * @author atta_troll
     *
     */
    private class StubRandomValueProducer extends AbstractRandomValueProducer<Object> {

        public StubRandomValueProducer(AbstractTokenDataSource<?> dataSource, int index) {
            super(dataSource, index);
        }

        @Override
        public Object produceValue() throws IOException {
            return null;
        }

    }
}
