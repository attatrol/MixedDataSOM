package com.github.attatrol.som.som.topology;

import com.github.attatrol.preprocessing.distance.metric.Metric;

/**
 * An upgrade of {@link RectangleTopology}, toroidal topology provides an example of
 * a border-less topology.
 * @author atta_troll
 *
 */
public class ToroidalTopology extends RectangleTopology {

    /**
     * Default ctor
     * @param maxX length if border of cross-section circle
     * @param maxY radius of torus
     * @param metric metric used to calculate distance
     */
    ToroidalTopology(int maxX, int maxY, Metric metric) {
        super(maxX, maxY, metric);
    }

    @Override
    protected void setDistanceMatrix() {
        double[] differences = new double[2];
        for (int x1 = 0; x1 < getMaxX(); x1++) {
            for (int y1 = 0; y1 < getMaxY(); y1++) {
                for (int x2 = 0; x2 < getMaxX(); x2++) {
                    for (int y2 = 0; y2 < getMaxY(); y2++) {
                        final double diffX1 = Math.abs(x1 - x2);
                        final double diffX2 = getMaxX() - diffX1;
                        final double diffY1 = Math.abs(y1 - y2);
                        final double diffY2 = getMaxY() - diffY1;
                        differences[0] = diffX1 < diffX2 ? diffX1 : diffX2;
                        differences[1] = diffY1 < diffY2 ? diffY1 : diffY2;
                        distanceMatrix[x1][y1][x2][y2] =
                                getMetric().calculate(differences);
                    }
                }
            }
        }
    }

    /**
     * Factory for current topology
     * @author atta_troll
     *
     */
    public static class Factory implements RectangleTopologyFactory<ToroidalTopology> {

        @Override
        public ToroidalTopology createTopology(int width, int height, Metric metric)
            throws IllegalArgumentException {
            if (width < 1) {
                throw new IllegalArgumentException("Width is not positive number");
            }
            if (height < 1) {
                throw new IllegalArgumentException("Height is not positive number");
            }
            return new ToroidalTopology(width, height, metric);
        }
    }
}
