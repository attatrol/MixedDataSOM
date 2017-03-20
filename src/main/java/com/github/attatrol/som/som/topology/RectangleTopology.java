package com.github.attatrol.som.som.topology;

import java.util.ArrayList;
import java.util.List;

import com.github.attatrol.preprocessing.distance.metric.Metric;

/**
 * Topology of SOM, used to calculate distances between neurons.
 * Assuming that all neurons are placed in nodes of a rectangular grid.
 * All realizations must be created via {@link RectangularTopologyFactory}
 * which ensures that only valid topology is created.
 * @author atta_troll
 *
 */
public class RectangleTopology implements SomTopology {

    /**
     * Limit of x coordinate of neurons, all
     * x coordinates belong to [0, maxX - 1].
     */
    private final int maxX;

    /**
     * Limit of y coordinate of neurons, all
     * y coordinates belong to [0, maxY - 1].
     */
    private final int maxY;

    /**
     * Metric used to produce distances.
     */
    private final Metric metric;

    /**
     * Matrix filled with distances between neurons.
     */
    protected final double[][][][] distanceMatrix;

    /**
     * Default ctor
     * @param maxX width of rectangle
     * @param maxY height of rectangle
     * @param metric metric used to calculate distance
     */
    RectangleTopology(int maxX, int maxY, Metric metric) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.metric = metric;
        distanceMatrix = new double[maxX][maxY][maxX][maxY];
        setDistanceMatrix();
    }

    @Override
    public double getDistance(Point point1, Point point2) {
        return distanceMatrix[(int) Math.round(point1.getX())]
                [(int) Math.round(point1.getY())]
                [(int) Math.round(point2.getX())]
                [(int) Math.round(point2.getY())];
    }

    @Override
    public List<Point> getNeuronPositions() {
        List<Point> positions = new ArrayList<>();
        for (int  i = 0; i < maxX; i++) {
            for (int j = 0; j < maxY; j++) {
                positions.add(new Point(i, j));
            }
        }
        return positions;
    }

    /**
     * @return width of rectangle
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * @return height of rectangle
     */
    public int getMaxY() {
        return maxY;
    }

    /**
     * @return metric used to calculate distance
     */
    public Metric getMetric() {
        return metric;
    }

    /**
     * Fills distance matrix with values.
     */
    protected void setDistanceMatrix() {
        final Integer[] point1 = new Integer[2];
        final Integer[] point2 = new Integer[2];
        for (int x1 = 0; x1 < maxX; x1++) {
            for (int y1 = 0; y1 < maxY; y1++) {
                for (int x2 = 0; x2 < maxX; x2++) {
                    for (int y2 = 0; y2 < maxY; y2++) {
                        point1[0] = x1;
                        point1[1] = y1;
                        point2[0] = x2;
                        point2[1] = y2;
                        distanceMatrix[x1][y1][x2][y2] =
                                metric.calculate(point1, point2);
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
    public static class Factory implements RectangleTopologyFactory<RectangleTopology> {

        @Override
        public RectangleTopology createTopology(int width, int height, Metric metric)
            throws IllegalArgumentException {
            if (width < 1) {
                throw new IllegalArgumentException("Width is not positive number");
            }
            if (height < 1) {
                throw new IllegalArgumentException("Height is not positive number");
            }
            return new RectangleTopology(width, height, metric);
        }
    }
}
