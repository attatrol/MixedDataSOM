package com.github.attatrol.som.ui.utils.benchmarkfactories;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.som.benchmark.ClusteringBenchmark;
import com.github.attatrol.som.som.SomClusterResult;

public interface BenchmarkUiFactory {

    ClusteringBenchmark getBenchmark(AbstractTokenDataSource<?> tokenDataSource, String[] columnNames,
            SomClusterResult clusterResult, DistanceFunction distanceFunction);

    String getSuccessFormat();

    String getFailureFormat();

}
