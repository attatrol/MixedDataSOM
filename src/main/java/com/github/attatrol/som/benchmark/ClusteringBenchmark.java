package com.github.attatrol.som.benchmark;

/**
 * General interface for a clustering benchmark.
 * @author atta_troll
 *
 */
public interface ClusteringBenchmark {

    /**
     * @return get value of benchmark.
     */
    double getValue();

    /**
     * @return true if benchmarking has failed, its value is invalid.
     */
    boolean hasFailed();

}
