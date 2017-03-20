
package com.github.attatrol.som.ui;

import java.io.IOException;

import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.preprocessing.ui.TokenDataSourceAndMisc;
import com.github.attatrol.som.som.Som;
import com.github.attatrol.som.som.functions.learning.LearningFunctionFactory;
import com.github.attatrol.som.som.functions.neighbourhood.NeighborhoodFunctionFactory;
import com.github.attatrol.som.som.initializers.SomInitializer;
import com.github.attatrol.som.som.neuron.FuzzyNeuronFactory;
import com.github.attatrol.som.som.topology.RectangleTopologyFactory;

/**
 * Components and parameters of SOM to be created are stored here.
 * Actually it is a state of UI.
 * 
 * @author atta_troll
 *
 */
class SomComponents {

    private TokenDataSourceAndMisc tdsm;

    private DistanceFunction distanceFunction;

    private LearningFunctionFactory<?> learningFunctionFactory;

    private NeighborhoodFunctionFactory<?> neighborhoodFunctionFactory;

    private RectangleTopologyFactory<?> topologyFactory;

    private FuzzyNeuronFactory<?> fuzzyNeuronFactory;

    private SomInitializer somInitializer;

    private Som som;

    private int numberOfEpochs;

    private int rectangleWidth;

    private int rectangleHeight;

    private double overMedianWeakFactor;

    private double overMedianStrongFactor;

    private int lastCreatedNumberOfEpochs;

    private int lastCreatedRectangleWidth;

    private int lastCreatedRectangleHeight;

    /**
     * Flag, used to determine if learning process should be stopped.
     */
    private boolean learnSomAbortFlag;

    public TokenDataSourceAndMisc getTdsm() {
        return tdsm;
    }

    public void setTdsm(TokenDataSourceAndMisc tdsm) {
        this.tdsm = tdsm;
    }

    public DistanceFunction getDistanceFunction() {
        return distanceFunction;
    }

    public void setDistanceFunction(DistanceFunction distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    public LearningFunctionFactory<?> getLearningFunctionFactory() {
        return learningFunctionFactory;
    }

    public void setLearningFunctionFactory(LearningFunctionFactory<?> learningFunctionFactory) {
        this.learningFunctionFactory = learningFunctionFactory;
    }

    public NeighborhoodFunctionFactory<?> getNeighborhoodFunctionFactory() {
        return neighborhoodFunctionFactory;
    }

    public void
            setNeighborhoodFunctionFactory(NeighborhoodFunctionFactory<?> neighborhoodFunctionFactory) {
        this.neighborhoodFunctionFactory = neighborhoodFunctionFactory;
    }

    public RectangleTopologyFactory<?> getTopologyFactory() {
        return topologyFactory;
    }

    public void setTopologyFactory(RectangleTopologyFactory<?> topologyFactory) {
        this.topologyFactory = topologyFactory;
    }

    public FuzzyNeuronFactory<?> getFuzzyNeuronFactory() {
        return fuzzyNeuronFactory;
    }

    public void setFuzzyNeuronFactory(FuzzyNeuronFactory<?> fuzzyNeuronFactory) {
        this.fuzzyNeuronFactory = fuzzyNeuronFactory;
    }

    public SomInitializer getSomInitializer() {
        return somInitializer;
    }

    public void setSomInitializer(SomInitializer somInitializer) {
        this.somInitializer = somInitializer;
    }

    public Som getSom() {
        return som;
    }

    public void setSom(Som som) {
        this.som = som;
    }

    public int getNumberOfEpochs() {
        return numberOfEpochs;
    }

    public void setNumberOfEpochs(int expectedNumberOfEpochs) {
        this.numberOfEpochs = expectedNumberOfEpochs;
    }

    public int getRectangleWidth() {
        return rectangleWidth;
    }

    public void setRectangleWidth(int rectangleWidth) {
        this.rectangleWidth = rectangleWidth;
    }

    public int getRectangleHeight() {
        return rectangleHeight;
    }

    public void setRectangleHeight(int rectangleHeight) {
        this.rectangleHeight = rectangleHeight;
    }

    public double getOverMedianWeakFactor() {
        return overMedianWeakFactor;
    }

    public void setOverMedianWeakFactor(double overMedianWeakFactor) {
        this.overMedianWeakFactor = overMedianWeakFactor;
    }

    public double getOverMedianStrongFactor() {
        return overMedianStrongFactor;
    }

    public void setOverMedianStrongFactor(double overMedianStrongFactor) {
        this.overMedianStrongFactor = overMedianStrongFactor;
    }

    public boolean isLearnSomAbortFlag() {
        return learnSomAbortFlag;
    }

    public void setLearnSomAbortFlag(boolean learnSomAbortFlag) {
        this.learnSomAbortFlag = learnSomAbortFlag;
    }

    public int getLastCreatedNumberOfEpochs() {
        return lastCreatedNumberOfEpochs;
    }

    public int getLastCreatedRectangleWidth() {
        return lastCreatedRectangleWidth;
    }

    public int getLastCreatedRectangleHeight() {
        return lastCreatedRectangleHeight;
    }

    /**
     * Erases all fields of this POJO.
     * Must be called only by
     * {@link SetupSomPane.SetupFormState#applyState(SetupSomPane)}.
     */
    public void erase() {
        try {
            if (tdsm != null) {
                tdsm.getTokenDataSource().close();
            }
        }
        catch (IOException ex) {
            // swallowing this exception
        }
        tdsm = null;
        distanceFunction= null;
        learningFunctionFactory = null;
        neighborhoodFunctionFactory = null;
        topologyFactory = null;
        somInitializer = null;
        fuzzyNeuronFactory = null;
        som = null;
    }

    /**
     * Saves volatile parameters of SOM after its creation for further
     * usage by UI. Must be called only by
     * {@link SetupSomPane.SetupFormState#applyState(SetupSomPane)}.
     */
    public void registerLastCreatedSomParameters() {
        lastCreatedNumberOfEpochs = numberOfEpochs;;
        lastCreatedRectangleWidth = rectangleWidth;
        lastCreatedRectangleHeight = rectangleHeight;
    }
}
