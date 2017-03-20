
package com.github.attatrol.som.som.initializers;

import java.io.IOException;

import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.preprocessing.ui.TokenDataSourceAndMisc;
import com.github.attatrol.som.som.Som;
import com.github.attatrol.som.som.functions.learning.LearningFunction;
import com.github.attatrol.som.som.functions.neighbourhood.NeighborhoodFunction;
import com.github.attatrol.som.som.neuron.FuzzyNeuronFactory;
import com.github.attatrol.som.som.topology.RectangleTopology;
import com.github.attatrol.som.som.topology.SomTopology;

/**
 * Creates an untrained SOM out of some data source and with some metric function. Main task is to
 * create proper initial neuron weights.<br/>
 * <b>Must be used with non-empty data sources, data set must be checked to have at least single
 * record in it before usage of this.</b>
 * 
 * @author atta_troll
 *
 */
public interface SomInitializer {

    /**
     * Creates untrained SOM.
     * 
     * @param tdsm
     *        token data source ands misc
     * @param distanceFunction
     *        distance function used to measure distance between data source records
     * @param topology
     *        topology of SOM
     * @param neighborhoodFunction
     *        neighborhood function of SOM
     * @param learningFunction
     *        learning function of SOM
     * @param neuronFactory
     *        factory for a certain neuron type
     * @param overMedianWeakFactor
     *        defines upper size threshold of a weak neurons
     * @param overMedianStrongFactor
     *        defines lower size threshold of strong neurons
     * @return SOM instance
     * @throws IOException
     *         on internal i/o data source error
     */
    Som createSom(TokenDataSourceAndMisc tdsm, DistanceFunction distanceFunction,
            SomTopology topology, NeighborhoodFunction neighborhoodFunction,
            LearningFunction learningFunction, FuzzyNeuronFactory<?> neuronFactory,
            double overMedianWeakFactor, double overMedianStrongFactor) throws IOException;

    /**
     * Checks if data source is non empty, then calls
     * {@link #createSom(TokenDataSourceAndMisc, DistanceFunction, RectangleTopology, NeighborhoodFunction)}
     * 
     * @param tdsm
     *        token data source ands misc
     * @param distanceFunction
     *        distance function used to measure distance between data source records
     * @param topology
     *        topology of SOM
     * @param neighborhoodFunction
     *        neighborhood function of SOM
     * @param learningFunction
     *        learning function of SOM
     * @param neuronFactory
     *        factory for a certain neuron type
     * @param overMedianWeakFactor
     *        defines upper size threshold of a weak neurons
     * @param overMedianStrongFactor
     *        defines lower size threshold of strong neurons
     * @return SOM instance
     * @throws IOException
     *         on internal i/o data source error
     * @throws IllegalStateException
     *         if data source is empty
     */
    default Som checkDataSourceAndCreateSom(TokenDataSourceAndMisc tdsm,
            DistanceFunction distanceFunction, SomTopology topology,
            NeighborhoodFunction neighborhoodFunction, LearningFunction learningFunction,
            FuzzyNeuronFactory<?> neuronFactory, double overMedianWeakFactor,
            double overMedianStrongFactor) throws IOException, IllegalStateException {
        tdsm.getTokenDataSource().reset();
        if (tdsm.getTokenDataSource().hasNext()) {
            return createSom(tdsm, distanceFunction, topology, neighborhoodFunction,
                    learningFunction, neuronFactory, overMedianWeakFactor, overMedianStrongFactor);
        }
        else {
            throw new IllegalStateException("Empty data source is not allowed!");
        }
    }

}
