package com.github.attatrol.som.ui;

import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

class SomTileNumericChart extends LineChart<Number, Number> {


    public SomTileNumericChart(double[] values, double[] thresholds, double step) {
        super(new NumberAxis(thresholds[0], thresholds[thresholds.length - 1], step),
                new NumberAxis(0., 1., .1));
        setAnimated(false);
        setLegendVisible(false);
        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        getData().add(series);
        for (int i = 0; i < values.length; i++) {
            final XYChart.Data<Number, Number> point = new XYChart.Data<>(thresholds[i],
                    values[i]);
            series.getData().add(point);
        }
        setTitle(SomI18nProvider.INSTANCE.getValue("result.chart.numeric.title"));
    }
}
