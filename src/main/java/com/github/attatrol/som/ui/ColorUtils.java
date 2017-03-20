package com.github.attatrol.som.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.github.attatrol.preprocessing.datasource.parsing.TokenType;
import com.github.attatrol.preprocessing.ui.TokenDataSourceAndMisc;
import com.github.attatrol.som.som.initializers.SampleFrequencyCalculator;

import javafx.scene.paint.Color;

/**
 * Supplementary constants for {@link ResultPane} view.
 * @author atta_troll
 *
 */
final class ColorUtils {

    /**
     * Colors for most frequent colors.
     */
    public static final Color[] TILE_COLORS =
        {
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.GREEN,
            Color.BLUE,
            Color.VIOLET,
            Color.AQUAMARINE,
            Color.BEIGE,
            Color.BROWN,
            Color.CADETBLUE,
            Color.CHARTREUSE,
            Color.CRIMSON,
            Color.DARKGRAY,
            Color.HOTPINK,
            Color.GOLD,
            Color.MAGENTA,
            Color.NAVY,
            Color.YELLOWGREEN,
        };

    /**
     * Total number of unique colors for most frequent tokens.
     */
    public static final int TILE_COLORS_NUMBER = TILE_COLORS.length;

    /**
     * Color used for non-frequent tokens.
     */
    public static final Color OTHER_TILE_COLOR = Color.BLANCHEDALMOND;

    /**
     * Color used for tiles with dead neurons.
     */
    public static final Color DEAD_NEURON_COLOR = Color.BLACK;

    /**
     * Color used for tiles with dead neurons.
     */
    public static final Color LINE_COLOR = Color.WHITE;

    /**
     * There used for heat map construction.
     */
    private final static double BLUE_HUE = Color.BLUE.getHue() ;
    private final static double RED_HUE = Color.RED.getHue() ;

    /**
     * Not in use.
     */
    private ColorUtils() {
    }

    /**
     * Assigns unique colors for most frequent categorical values.
     * @param tdsm token data source
     * @return map value-color
     * @throws IOException on i/o error
     */
    public static Map<Object, Color>[] getTokenColorsByFrequency(TokenDataSourceAndMisc tdsm) throws IOException {
        final TokenType[] tokenTypes = tdsm.getTokenTypes();
        final Map<Object, Double>[] frequencies =
                SampleFrequencyCalculator.getSampleFrequencies(tdsm.getTokenDataSource(), tokenTypes);
        final int recordLength = tdsm.getTokenDataSource().getRecordLength();
        @SuppressWarnings("unchecked")
        Map<Object, Color>[] colors = new Map[recordLength];
        for (int i = 0; i < recordLength; i++) {
            colors[i] = new HashMap<>();
            if (SampleFrequencyCalculator.isCategoricalTokenType(tokenTypes[i])) {
                final TreeMap<Double, List<Object>> orderByFrequency = revertFrequencyMap(frequencies[i]);
                int counter = 0;
                all_title_colors_used:
                for(Double frequency : orderByFrequency.descendingKeySet()) {
                    List<Object> objects = orderByFrequency.get(frequency);
                    for (Object object : objects) {
                        colors[i].put(object, TILE_COLORS[counter]);
                        counter++;
                        if (counter == TILE_COLORS_NUMBER) {
                            break all_title_colors_used;
                        }
                    }
                }
            }
        }
        return colors;
    }

    /**
     * Reverts original frequency map, the reverted map may be traversed orderly.
     * @param frequencies frequency map
     * @return reverse tree map
     */
    public static TreeMap<Double, List<Object>> revertFrequencyMap(Map<Object, Double> frequencies) {
        final TreeMap<Double, List<Object>> orderByFrequency = new TreeMap<>();
        for (Map.Entry<Object, Double> entry : frequencies.entrySet()) {
            List<Object> objects = orderByFrequency.get(entry.getValue());
            if (objects == null) {
                objects = new ArrayList<>(1);
                objects.add(entry.getKey());
                orderByFrequency.put(entry.getValue(), objects);
            }
            else {
                objects.add(entry.getKey());
            }
        }
        return orderByFrequency;
    }

    /**
     * Calculates color temperature
     * @param normalizedValue numeric value bounded by 0 and 1
     * @return color temperature
     */
    public static Color getHeatMapColor(double normalizedValue) {
        if (normalizedValue > 1.) {
            normalizedValue = 1.;
        }
        else if (normalizedValue < 0.) {
            normalizedValue = 0.;
        }
        double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * normalizedValue;
        return Color.hsb(hue, 1.0, 1.0);
    }
}
