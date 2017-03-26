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
        super(new NumberAxis(1, epochNumber, 1.), new NumberAxis(0., 1., .05));
        this.epochNumber = epochNumber;
        epochsInTick = ((double) epochNumber)
                / SetupSomPane.CHART_NUMBER_OF_POINTS < 1.
                ? 1 : (int) Math.floor(((double) epochNumber)
                        / SetupSomPane.CHART_NUMBER_OF_POINTS);
        setCreateSymbols(true);
        setAnimated(false);
        setLegendVisible(true);
        setCreateSymbols(false);
        NumberAxis xAxis = (NumberAxis) getXAxis();
        xAxis.setLabel(SomI18nProvider.INSTANCE.getValue("main.chart.xaxis.label"));
        xAxis.setForceZeroInRange(false);
        xAxis.setTickUnit(epochsInTick);
        NumberAxis yAxis = (NumberAxis) getYAxis();
        yAxis.setLabel(SomI18nProvider.INSTANCE.getValue("main.chart.yaxis.label"));
        yAxis.setForceZeroInRange(false);
        setTitle(SomI18nProvider.INSTANCE.getValue("main.chart.label"));
    }

    /**
     * Use this when you have to preserve data from previous chart
     * @param epochNumber number of epochs
     * @param oldChart chart which data should be shown
     */
    public AvgErrorChart(int epochNumber, AvgErrorChart oldChart) {
        this(epochNumber);
        getData().addAll(oldChart.getData());
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
         * Associated chart.
         */
        private final AvgErrorChart chart;

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
            this.chart = chart;
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
                final double avgSum = sum / cycleCounter;
                final XYChart.Data<Number, Number> point = new XYChart.Data<>(xCounter,
                        avgSum);
                Platform.runLater(() -> {currentSeries.getData().add(point);
                    chart.setTitle(String.format(SomI18nProvider.INSTANCE
                        .getValue("main.chart.label.inprocess"), avgSum));
                });
                xCounter += cycleCounter;
                cycleCounter = 0;
                sum = 0;
            }
        }
    }
}
