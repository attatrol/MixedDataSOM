package com.github.attatrol.som.ui;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.Record;
import com.github.attatrol.som.som.SomClusterResult;
import com.github.attatrol.som.som.neuron.AbstractNeuron;
import com.github.attatrol.som.som.topology.Point;
import com.github.attatrol.som.som.topology.SomTopology;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageProducer {

    private static final int SCALE = 100;


    public static Map<Point, Color> getColorScheme(List<AbstractNeuron> neurons,
            SomClusterResult clusterResult,
            Map<Object, Color> refColumnColorMap,
            AbstractTokenDataSource<?> dataSource,
            int refColumnIndex) throws IOException {
        Map<Point, Color> scheme = new HashMap<>();
        dataSource.reset();
        Map<AbstractNeuron, Map<Object, Long>> frequenciesByNeuron = new HashMap<>();
        for (AbstractNeuron neuron : neurons) {
            frequenciesByNeuron.put(neuron, new HashMap<>());
        }
        while (dataSource.hasNext()) {
            Record<Object[]> record = dataSource.next();
            AbstractNeuron neuron = clusterResult.getNeuron(record);
            Object refObject = record.getData()[refColumnIndex];
            Map<Object, Long> frequencies = frequenciesByNeuron.get(neuron);
            Long freq = frequencies.get(refObject);
            if (freq == null) {
                frequencies.put(refObject, 1L);
            }
            else {
                frequencies.put(refObject, freq + 1L);
            }
        }
        for (Map.Entry<AbstractNeuron, Map<Object, Long>> entry : frequenciesByNeuron.entrySet()) {
            Object mostFrequentObject = null;
            Long frequency = 0L;
            for (Map.Entry<Object, Long> entry1 : entry.getValue().entrySet()) {
                if (entry1.getValue() > frequency) {
                    mostFrequentObject = entry1.getKey();
                    frequency = entry1.getValue();
                }
            }
            final Color schemeColor = mostFrequentObject == null ? ColorUtils.DEAD_NEURON_COLOR
                    : refColumnColorMap.get(mostFrequentObject);
            scheme.put(entry.getKey().getPosition(), schemeColor);
        }
        return scheme;
    }

    public static RenderedImage produceImage(Map<Point, Color> colors, int height, int width) {
        Canvas canvas = new Canvas(width * SCALE, height * SCALE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        for (Map.Entry<Point, Color> entry : colors.entrySet()) {
            Point point  = entry.getKey();
            gc.setFill(entry.getValue());
            gc.fillRect(point.getX() * SCALE, point.getY() * SCALE, SCALE - 3, SCALE - 3);
        }
        WritableImage writableImage = new WritableImage(width * SCALE, height * SCALE);
        canvas.snapshot(null, writableImage);
        return SwingFXUtils.fromFXImage(writableImage, null);
    }

    public static double getVisualQualityIndex(SomTopology topology, Map<Point, Color> colors, int height, int width) {
        int differentColorBorderCount = 0;
        for (Map.Entry<Point, Color> entry : colors.entrySet()) {
            for (Map.Entry<Point, Color> entry1 : colors.entrySet()) {
                final double distance = topology.getDistance(entry.getKey(), entry1.getKey());
                if (distance < 1.01 && distance > .99 && !entry.getValue().equals(entry1.getValue())) {
                    differentColorBorderCount++;
                }
            }
        }
        final int possibleBorders = height * width * 2 - (height + width);
        return ((double) differentColorBorderCount) / possibleBorders / 2.;
    }
}
