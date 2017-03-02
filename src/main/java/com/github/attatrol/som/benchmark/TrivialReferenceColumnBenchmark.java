package com.github.attatrol.som.benchmark;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.datasource.Record;
import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.som.som.SomClusterResult;

/**
 * Check is based on quality of partition of tokens from reference column.
 * Index belongs to range from 0. to 1., the higher is better.
 * @author atta_troll
 *
 */
public class TrivialReferenceColumnBenchmark implements ClusteringBenchmark {

    private double partitionIndex;

    private boolean hasFailed;

    public TrivialReferenceColumnBenchmark(SomClusterResult clusterResult, DistanceFunction distanceFunction,
            AbstractTokenDataSource<?> tokenDataSource, int refColumnIndex) {
        final int clusterNumber = clusterResult.getClusterNumber();
        try {
            calculateClusterPartitionIndexes(clusterResult, tokenDataSource, refColumnIndex, clusterNumber);
        }
        catch (IOException ex) {
            hasFailed = true;
        }
    }

    private void calculateClusterPartitionIndexes(SomClusterResult clusterResult,
            AbstractTokenDataSource<?> tokenDataSource, int refColumnIndex, int clusterNumber)
            throws IOException {
        @SuppressWarnings("unchecked")
        Map<Object, Long>[] clusterOccurrences = new Map[clusterNumber];
        for (int  i = 0; i < clusterNumber; i++) {
            clusterOccurrences[i] = new HashMap<>();
        }
        tokenDataSource.reset();
        long counter = 0L;
        while (tokenDataSource.hasNext()) {
            counter++;
            final Record<Object[]> record = tokenDataSource.next();
            final Object token = record.getData()[refColumnIndex];
            final int clusterIndex = clusterResult.getCluster(record);
            Long occurrence = clusterOccurrences[clusterIndex].get(token);
            clusterOccurrences[clusterIndex].put(token, occurrence != null ? occurrence + 1 : 1L);
        }
        long[] maxOccurrences = new long[clusterNumber];
        for (int  i = 0; i < clusterNumber; i++) {
            for (Map.Entry<Object, Long> entry : clusterOccurrences[i].entrySet()) {
                final long occurrence = entry.getValue();
                if (occurrence > maxOccurrences[i]) {
                    maxOccurrences[i] = occurrence;
                }
            }
        }
        long totalDominantClusterTokenNumber = 0L;
        for (int  i = 0; i < clusterNumber; i++) {
            totalDominantClusterTokenNumber += maxOccurrences[i];
        }
        if (counter != 0L) {
            partitionIndex = ((double) totalDominantClusterTokenNumber) / counter;
        }
        else {
            partitionIndex = 1.;
        }
    }

    @Override
    public double getValue() {
        return partitionIndex;
    }

    @Override
    public boolean hasFailed() {
        return hasFailed;
    }

}
