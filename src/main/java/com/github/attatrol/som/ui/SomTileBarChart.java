package com.github.attatrol.som.ui;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Bar chart used to show content of some SOM tile. Not intended to be widely used.
 * @author atta_troll
 *
 */
class SomTileBarChart extends BarChart<String, Number> {

    /**
     * Ctor used by {@link ResultPane.CategoricalColorScheme}.
     * @param map
     */
    SomTileBarChart(Map<Object, Double> frequencies) {
        super(new CategoryAxis(), new NumberAxis(0., 1., .1));
        setAnimated(false);
        setLegendVisible(false);
        setTitle(SomI18nProvider.INSTANCE.getValue("result.chart.bar.title"));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        TreeMap<Double, List<Object>> reverseFrequencies = ColorUtils.revertFrequencyMap(frequencies);
        for (Double frequency : reverseFrequencies.descendingKeySet()) {
            for (Object object : reverseFrequencies.get(frequency)) {
                series.getData().add(new XYChart.Data<String, Number>(object.toString(), frequency));
            }
        }
        getData().add(series);
    }

}
