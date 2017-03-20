package com.github.attatrol.som.benchmark;

import java.io.IOException;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.Record;
import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.som.som.SomClusterResult;
import com.github.attatrol.som.som.initializers.SampleFrequencyCalculator;

public class CalinskiHarabaszIndex implements ClusteringBenchmark {

    private double[] averageSquaredClusterDistances;

    private double averageSquaredOverallDistance;

    private double VRC;

    private long dataSourceSize;

    private boolean hasFailed;

    public CalinskiHarabaszIndex(SomClusterResult clusterResult, DistanceFunction distanceFunction,
            AbstractTokenDataSource<?> tokenDataSource) {
        final int clusterNumber = clusterResult.getClusterNumber();
        averageSquaredClusterDistances = new double[clusterNumber];
        try {
            calculateAverageSquaredDistances(clusterResult, distanceFunction, tokenDataSource);
            double Ac = 0.;
            int nonEmptyClusterNumber = 0;
            for (int i = 0; i < clusterNumber; i++) {
                final long clusterRecordNumber = clusterResult.getClusterSize(i);
                if (clusterRecordNumber != 0) {
                    nonEmptyClusterNumber++;
                    Ac += (clusterRecordNumber - 1)
                            * (averageSquaredOverallDistance - averageSquaredClusterDistances[i]);
                }
            }
            long NminusC = 1L;
            if (dataSourceSize != 0L && nonEmptyClusterNumber < dataSourceSize) {
                NminusC = dataSourceSize - nonEmptyClusterNumber;
            }
            Ac /= NminusC;
            if (nonEmptyClusterNumber > 1) {
                VRC = (averageSquaredOverallDistance + NminusC / (nonEmptyClusterNumber - 1) * Ac)
                        / (averageSquaredOverallDistance - Ac);
            }
            else {
                VRC = 0.;
            }
        }
        catch (IOException ex) {
            hasFailed = true;
        }
    }

    /**
     * Calculates all values that require 
     * @param clusterResult
     * @param distanceFunction
     * @param tokenDataSource
     * @return
     * @throws IOException
     */
    private void calculateAverageSquaredDistances(SomClusterResult clusterResult,
            DistanceFunction distanceFunction,
            AbstractTokenDataSource<?> tokenDataSource) throws IOException {
        dataSourceSize = SampleFrequencyCalculator.getDataSourceSize(tokenDataSource);
        for (long counter = 0L; counter < dataSourceSize; counter++) {
            tokenDataSource.reset();
            long localCounter = 0L;
            while (tokenDataSource.hasNext() && localCounter < counter) {
                tokenDataSource.next();
            }
            if (tokenDataSource.hasNext()) {
                final Record<Object[]> currentRecord = tokenDataSource.next();
                final int clusterIndex = clusterResult.getCluster(currentRecord);
                final Object[] currentData = currentRecord.getData();
                while (tokenDataSource.hasNext()) {
                    final Record<Object[]> record = tokenDataSource.next();
                    final double distance = distanceFunction.calculate(currentData, record.getData());
                    final double sqDistance = distance * distance;
                    averageSquaredOverallDistance += sqDistance;
                    if (clusterIndex == clusterResult.getCluster(record)) {
                        averageSquaredClusterDistances[clusterIndex] +=sqDistance;
                    }
                }
            }
        }
        if (dataSourceSize > 0) {
            averageSquaredOverallDistance /= dataSourceSize;
        }
        for (int i = 0; i < averageSquaredClusterDistances.length; i++) {
            final long clusterSize = clusterResult.getClusterSize(i);
            if (clusterSize != 0) {
                averageSquaredClusterDistances[i] /= clusterSize;
            }
        }
    }

    public double getValue() {
        return VRC;
    }

    public boolean hasFailed() {
        return hasFailed;
    }
}
