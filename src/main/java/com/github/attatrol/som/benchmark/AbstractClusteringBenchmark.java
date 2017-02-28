package com.github.attatrol.som.benchmark;

import java.io.IOException;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.som.som.SomClusterResult;

public interface AbstractClusteringBenchmark {

    double calculate(SomClusterResult clusterResult, DistanceFunction distanceFunction,
            AbstractTokenDataSource<?> tokenDataSource) throws IOException;

}
