
package com.github.attatrol.som.ui;

import java.io.IOException;

import com.github.attatrol.preprocessing.distance.metric.EuclideanMetric;
import com.github.attatrol.preprocessing.ui.misc.UiUtils;
import com.github.attatrol.som.som.Som;
import com.github.attatrol.som.som.functions.learning.LearningFunction;
import com.github.attatrol.som.som.functions.neighbourhood.NeighborhoodFunction;
import com.github.attatrol.som.som.initializers.SomInitializer;
import com.github.attatrol.som.som.neuron.FuzzyNeuronFactory;
import com.github.attatrol.som.som.topology.SomTopology;
import com.github.attatrol.som.ui.SetupSomPane.SetupFormState;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.application.Platform;

/**
 * There may be different modes of SOM creation and learning, they depend on SOM parameters and user
 * chosen options.<br/>
 * <b>Why i do not use {@link javafx.concurrent.Task} here:</b><br/>
 * Runnables that are returned by {{@link #getLearningProcess(SetupSomPane)} are to
 * fill chart with their values, however they will still have to run
 * {@link Platform#runLater(Runnable)} at some point of their execution.
 * Also i cannot benefit from cool automata inside of Task as i have my own automata
 * in the main form of application.
 * 
 * @author atta_troll
 *
 */
enum SomMode {

    /**
     * User defined epoch number, so learning and neighborhood functions will be adjusted
     * accordingly, and end of learning process is defined by epoch counter.
     */
    EPOCH_NUMBER_SET {

        @Override
        public Runnable getСreationProcess(SetupSomPane form) {
            return new KnownEpochNumberSomCreationProcess(form);
        }

        @Override
        public Runnable getLearningProcess(SetupSomPane form) {
            return new EpochCounterLearningProcess(form);
        }

    },
    /**
     * User defined desired average error threshold, this means that learning and neighborhood
     * functions will be generated without any info about number of epochs, and and of learning
     * process is defined by reaching this error threshold.
     */
    AVERAGE_ERROR_SET {

        @Override
        public Runnable getСreationProcess(SetupSomPane form) {
            return new UnknownEpochNumberSomCreationProcess(form);
        }

        @Override
        public Runnable getLearningProcess(SetupSomPane form) {
            return new AverageErrorThresholdLearningProcess(form);
        }

    };

    /**
     * Returns runnable that contains process of SOM creation.
     * It must:<br/>
     * 1. register SOM instance in {@link SomComponents};<br/>
     * 2. set form state to {@link SetupFormState#SOM_CREATED_6};<br/>
     * 3. in case of a known error set form state to
     * {@link SetupFormState#SOM_CREATION_ERROR};<br/>
     * 4. in case of an unknown error set form state to {@link SetupFormState#UNKNOWN_ERROR}.
     * @param form main form
     * @return runnable that can do all above
     */
    public abstract Runnable getСreationProcess(SetupSomPane form);

    /**
     * Returns runnable that contains process of SOM learning.
     * It must:<br/>
     * 1. set form state to {@link SetupFormState#SOM_COMPLETED_8};<br/>
     * 2. in case of a known error set form state to
     * {@link SetupFormState#SOM_LEARNING_ERROR};<br/>
     * 3. in case of an unknown error set form state to {@link SetupFormState#UNKNOWN_ERROR}.
     * @param form main form
     * @return runnable that can do all above
     */
    public abstract Runnable getLearningProcess(SetupSomPane form);

    /**
     * Performs range checks over SOM components if they are valid for a SOM creation.
     * 
     * @param somData
     *        SOM components
     * @throws IllegalArgumentException
     *         on invalid SOM components
     */
    private static void checkSomComponents(SomComponents somData)
            throws IllegalArgumentException {
        if (somData.getRectangleWidth() < 1) {
            throw new IllegalArgumentException(
                    SomI18nProvider.INSTANCE.getValue("ui.exception.bad.width"));
        }
        if (somData.getRectangleHeight() < 1) {
            throw new IllegalArgumentException(
                    SomI18nProvider.INSTANCE.getValue("ui.exception.bad.height"));
        }
        if (somData.getTdsm() == null) {
            throw new IllegalArgumentException(
                    SomI18nProvider.INSTANCE.getValue("ui.exception.tds.missing"));
        }
        if (somData.getDistanceFunction() == null) {
            throw new IllegalArgumentException(
                    SomI18nProvider.INSTANCE.getValue("ui.exception.distance.missing"));
        }
        if (somData.getLearningFunctionFactory() == null) {
            throw new IllegalArgumentException(
                    SomI18nProvider.INSTANCE.getValue("ui.exception.learning.missing"));
        }
        if (somData.getNeighborhoodFunctionFactory() == null) {
            throw new IllegalArgumentException(
                    SomI18nProvider.INSTANCE.getValue("ui.exception.neighborhood.missing"));
        }
        if (somData.getFuzzyNeuronFactory() == null) {
            throw new IllegalArgumentException(
                    SomI18nProvider.INSTANCE.getValue("ui.exception.neuron.factory.missing"));
        }
        if (somData.getSomInitializer() == null) {
            throw new IllegalArgumentException(
                    SomI18nProvider.INSTANCE.getValue("ui.exception.init.missing"));
        }
        if (somData.getTopologyFactory() == null) {
            throw new IllegalArgumentException(
                    SomI18nProvider.INSTANCE.getValue("ui.exception.topology.missing"));
        }
    }

    /**
     * Content of thread of SOM creation in case if epoch number is known.
     * 
     * @author atta_troll
     *
     */
    private class KnownEpochNumberSomCreationProcess implements Runnable {

        /**
         * Main form
         */
        private SetupSomPane form;

        /**
         * Default ctor.
         * 
         * @param form
         *        main form
         */
        public KnownEpochNumberSomCreationProcess(SetupSomPane form) {
            this.form = form;
        }

        @Override
        public void run() {
            try {
                final SomComponents somData = form.getSomComponents();
                checkSomComponents(somData);
                final int width = somData.getRectangleWidth();
                final int height = somData.getRectangleHeight();
                final int epochNumber = somData.getNumberOfEpochs();
                if (epochNumber < 1) {
                    throw new IllegalArgumentException(
                            SomI18nProvider.INSTANCE.getValue("ui.exception.bad.epoch"));
                }
                final SomTopology topology = somData.getTopologyFactory().createTopology(width,
                        height, new EuclideanMetric());
                final NeighborhoodFunction neighborhoodFunction =
                        somData.getNeighborhoodFunctionFactory()
                                .produceNeighborhoodFunction(epochNumber, width * height);
                final LearningFunction learningFunction =
                        somData.getLearningFunctionFactory().produceLearningFunction(epochNumber);
                final FuzzyNeuronFactory<?> neuronFactory = somData.getFuzzyNeuronFactory();
                final double winnerHandicapFactor = somData.getWinnerLoweringFactor();
                final SomInitializer somInitializer = somData.getSomInitializer();
                final Som som = somInitializer.checkDataSourceAndCreateSom(somData.getTdsm(),
                        somData.getDistanceFunction(), topology, neighborhoodFunction,
                        learningFunction, neuronFactory, winnerHandicapFactor);
                somData.setSom(som);
                Platform.runLater(() -> form.setInternalState(SetupFormState.SOM_CREATED_6));
            }
            catch (IllegalStateException | IOException ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.SOM_CREATION_ERROR);
                    UiUtils.showExceptionMessage(ex);
                });
            }
            catch (IllegalArgumentException ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.DISTANCE_FUNCTION_SET_4);
                    UiUtils.showExceptionMessage(ex);
                });
            }
            catch (Exception ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.UNKNOWN_ERROR);
                    UiUtils.showExceptionMessage(ex);
                });
            }
        }

    }

    /**
     * SOM creation process in case if epoch number is unknown.
     * @author atta_troll
     *
     */
    private class UnknownEpochNumberSomCreationProcess implements Runnable {

        private SetupSomPane form;

        public UnknownEpochNumberSomCreationProcess(SetupSomPane form) {
            this.form = form;
        }

        @Override
        public void run() {
            try {
                final SomComponents somData = form.getSomComponents();
                checkSomComponents(somData);
                final int width = somData.getRectangleWidth();
                final int height = somData.getRectangleHeight();
                final SomTopology topology = somData.getTopologyFactory().createTopology(width,
                        height, new EuclideanMetric());
                final NeighborhoodFunction neighborhoodFunction =
                        somData.getNeighborhoodFunctionFactory()
                                .produceNeighborhoodFunction(width * height);
                final LearningFunction learningFunction =
                        somData.getLearningFunctionFactory().produceLearningFunction();
                final FuzzyNeuronFactory<?> neuronFactory = somData.getFuzzyNeuronFactory();
                final double winnerHandicapFactor = somData.getWinnerLoweringFactor();
                final SomInitializer somInitializer = somData.getSomInitializer();

                final Som som = somInitializer.checkDataSourceAndCreateSom(somData.getTdsm(),
                        somData.getDistanceFunction(), topology, neighborhoodFunction,
                        learningFunction, neuronFactory, winnerHandicapFactor);
                somData.setSom(som);
                Platform.runLater(() -> form.setInternalState(SetupFormState.SOM_CREATED_6));
            }
            catch (IllegalStateException | IOException ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.SOM_CREATION_ERROR);
                    UiUtils.showExceptionMessage(ex);
                });
            }
            catch (IllegalArgumentException ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.DISTANCE_FUNCTION_SET_4);
                    UiUtils.showExceptionMessage(ex);
                });
            }
            catch (Exception ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.UNKNOWN_ERROR);
                    UiUtils.showExceptionMessage(ex);
                });
            }
        }
    }

    /**
     * Learning process that lasts for a defined number of epochs.
     * @author atta_troll
     *
     */
    private static class EpochCounterLearningProcess implements Runnable {

        private SetupSomPane form;

        public EpochCounterLearningProcess(SetupSomPane form) {
            this.form = form;
        }

        @Override
        public void run() {
            final SomComponents somData = form.getSomComponents();
            final Som som = somData.getSom();
            somData.setLearnSomAbortFlag(false);
            final int epochNumber = somData.getLastCreatedNumberOfEpochs();
            int epochCounter = 0;
            final AvgErrorChart.ChartFiller chartFiller = form.getNewChartFiller();
            try {
                while (++epochCounter <= epochNumber
                        && !somData.isLearnSomAbortFlag()) {
                    final double avgError = som.learnEpoch(epochCounter);
                    chartFiller.registerEpoch(avgError);
                    //System.out.println(String.format("Epoch: %d, Error: %f",
                            //epochCounter, avgError));
                    //System.out.println(som);
                }
                chartFiller.dumpResidualToChart();
                Platform.runLater(() -> form.setInternalState(SetupFormState.SOM_COMPLETED_8));
            }
            catch (IOException ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.SOM_LEARNING_ERROR);
                    UiUtils.showExceptionMessage(ex);
                });
            }
            catch (Exception ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.UNKNOWN_ERROR);
                    UiUtils.showExceptionMessage(ex);
                });
            }
        }
    }

    /**
     * Learning process that stops when a defined threshold of average error is reached.
     * @author atta_troll
     *
     */
    private static class AverageErrorThresholdLearningProcess implements Runnable {

        private SetupSomPane form;

        public AverageErrorThresholdLearningProcess(SetupSomPane form) {
            this.form = form;
        }

        @Override
        public void run() {
            final SomComponents somData = form.getSomComponents();
            final Som som = somData.getSom();
            somData.setLearnSomAbortFlag(false);
            final double desiredAngError = somData.getLastCreatedDesiredAverageError();
            final double chartEndEpoch = somData.getLastCreatedNumberOfEpochs();
            int epochCounter = 0;
            double currentAvgError = 0;
            final AvgErrorChart.ChartFiller chartFiller = form.getNewChartFiller();
            try {
                do {
                    ++epochCounter;
                    currentAvgError = som.learnEpoch(epochCounter);
                    if (epochCounter <= chartEndEpoch) {
                        chartFiller.registerEpoch(currentAvgError);
                    }
                }
                while (currentAvgError > desiredAngError && !somData.isLearnSomAbortFlag());
                chartFiller.dumpResidualToChart();
                Platform.runLater(() -> form.setInternalState(SetupFormState.SOM_COMPLETED_8));
            }
            catch (IOException ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.SOM_LEARNING_ERROR);
                    UiUtils.showExceptionMessage(ex);
                });
            }
            catch (Exception ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.UNKNOWN_ERROR);
                    UiUtils.showExceptionMessage(ex);
                });
            }
        }
    }
}
