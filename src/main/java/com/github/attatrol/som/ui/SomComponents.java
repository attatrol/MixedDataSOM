
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
 * Here components of SOM to be created are stored. And some additional data also stored here.
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

    private double desiredAverageError;

    private int rectangleWidth;

    private int rectangleHeight;

    private double winnerLoweringFactor;

    private SomMode chosenSomMode;

    private int lastCreatedNumberOfEpochs;

    private double lastCreatedDesiredAverageError;

    private int lastCreatedRectangleWidth;

    private int lastCreatedRectangleHeight;

    private SomMode lastCreatedSomMode;

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

    public double getDesiredAverageError() {
        return desiredAverageError;
    }

    public void setDesiredAverageError(double desiredAverageError) {
        this.desiredAverageError = desiredAverageError;
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

    public double getWinnerLoweringFactor() {
        return winnerLoweringFactor;
    }

    public void setWinnerLoweringFactor(double winnerLoweringFactor) {
        this.winnerLoweringFactor = winnerLoweringFactor;
    }

    public boolean isLearnSomAbortFlag() {
        return learnSomAbortFlag;
    }

    public void setLearnSomAbortFlag(boolean learnSomAbortFlag) {
        this.learnSomAbortFlag = learnSomAbortFlag;
    }

    public SomMode getChosenSomMode() {
        return chosenSomMode;
    }

    public void setChosenSomMode(SomMode chosenSomMode) {
        this.chosenSomMode = chosenSomMode;
    }

    public int getLastCreatedNumberOfEpochs() {
        return lastCreatedNumberOfEpochs;
    }

    public double getLastCreatedDesiredAverageError() {
        return lastCreatedDesiredAverageError;
    }

    public int getLastCreatedRectangleWidth() {
        return lastCreatedRectangleWidth;
    }

    public int getLastCreatedRectangleHeight() {
        return lastCreatedRectangleHeight;
    }

    public SomMode getLastCreatedSomMode() {
        return lastCreatedSomMode;
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
        // we have to setup this as control check box may not fire
        // change event
        chosenSomMode = SomMode.EPOCH_NUMBER_SET;
        lastCreatedSomMode = null;
    }

    /**
     * Saves volatile parameters of SOM after its creation for further
     * usage by UI. Must be called only by
     * {@link SetupSomPane.SetupFormState#applyState(SetupSomPane)}.
     */
    public void registerLastCreatedSomParameters() {
        lastCreatedNumberOfEpochs = numberOfEpochs;;
        lastCreatedDesiredAverageError = desiredAverageError;
        lastCreatedRectangleWidth = rectangleWidth;
        lastCreatedRectangleHeight = rectangleHeight;
        lastCreatedSomMode = chosenSomMode;

    }
}
