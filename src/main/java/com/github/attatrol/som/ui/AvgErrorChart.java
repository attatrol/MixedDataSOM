package com.github.attatrol.som.ui;

import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
// * Chart used to show average error per epoch during SOM learning process.
 * @author atta_troll
 *
 */
class AvgErrorChart extends LineChart<Number, Number> {

    /**
     * Y axis is constant as error is bounded in [0, 1]
     */
    private static final NumberAxis Y_AXIS = new NumberAxis(0., 1., .02);
    static {
        Y_AXIS.setLabel(SomI18nProvider.INSTANCE.getValue("main.chart.yaxis.label"));
        Y_AXIS.setForceZeroInRange(false);
    }

    private final int epochNumber;

    /**
     * Number of epochs in tick.
     */
    private final int epochsInTick;

    /**
     * Default ctor.
     * @param epochNumber total number of epochs.
     */
    public AvgErrorChart(int epochNumber) {
        super(new NumberAxis(1, epochNumber, 1.), Y_AXIS);
        this.epochNumber = epochNumber;
        epochsInTick = ((double) epochNumber)
                / SetupSomPane.CHART_NUMBER_OF_POINTS < 1.
                ? 1 : (int) Math.floor(((double) epochNumber)
                        / SetupSomPane.CHART_NUMBER_OF_POINTS);
        setCreateSymbols(true);
        setAnimated(false);
        setLegendVisible(true);
        NumberAxis xAxis = (NumberAxis) getXAxis();
        xAxis.setLabel(SomI18nProvider.INSTANCE.getValue("main.chart.xaxis.label"));
        xAxis.setForceZeroInRange(false);
        xAxis.setTickUnit(epochsInTick);
        this.setTitle(SomI18nProvider.INSTANCE.getValue("main.chart.label"));
    }

    /**
     * @return epochs per one tick of X axis
     */
    public int getEpochStep() {
        return epochsInTick;
    }

    /**
     * @return epoch number which can be shown on this graph
     */
    public int getEpochNumber() {
        return epochNumber;
    }

    /**
     * Class used to feed data into chart during learning process.
     * @author atta_troll
     *
     */
    public static class ChartFiller {

        /**
         * Number of epochs in one tick.
         */
        private final int epochsInTick;

        /**
         * This current chart series.
         */
        private final XYChart.Series<Number, Number> currentSeries =
                new XYChart.Series<Number, Number>();

        /**
         * Inner tick cycle counter.
         */
        private int cycleCounter;

        /**
         * X axis counter.
         */
        private int xCounter = 1;

        /**
         * Average error sum accumulator.
         */
        private double sum;

        /**
         * Default ctor.
         * @param chart average error chart
         */
        public ChartFiller(AvgErrorChart chart) {
            this.epochsInTick = chart.getEpochStep();
            Platform.runLater(() -> chart.getData().add(currentSeries));
        }

        /**
         * Registers next average error value.
         * @param avgError
         */
        public void registerEpoch(double avgError) {
            sum +=avgError;
            cycleCounter++;
            if (cycleCounter == epochsInTick) {
                dumpResidualToChart();
            }
        }

        /**
         * Dumps all collected avg error data into point on chart.
         * Used internally to dump data into chart from tick to tick.
         * Should be used externally in cases when learning process already ended.
         */
        public void dumpResidualToChart() {
            if (cycleCounter != 0) {
                sum /= cycleCounter;
                final XYChart.Data<Number, Number> point = new XYChart.Data<>(xCounter,
                        sum);
                Platform.runLater(() -> currentSeries.getData().add(point));
                xCounter += cycleCounter;
                cycleCounter = 0;
                sum = 0;
            }
        }
    }
}
