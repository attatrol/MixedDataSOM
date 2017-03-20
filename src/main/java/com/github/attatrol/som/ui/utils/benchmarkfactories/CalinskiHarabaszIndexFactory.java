package com.github.attatrol.som.ui.utils.benchmarkfactories;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.som.benchmark.CalinskiHarabaszIndex;
import com.github.attatrol.som.benchmark.ClusteringBenchmark;
import com.github.attatrol.som.som.SomClusterResult;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;

public class CalinskiHarabaszIndexFactory implements BenchmarkUiFactory {

    @Override
    public ClusteringBenchmark getBenchmark(AbstractTokenDataSource<?> tokenDataSource,
            String[] columnNames, SomClusterResult clusterResult,
            DistanceFunction distanceFunction) {
        return new CalinskiHarabaszIndex(clusterResult, distanceFunction, tokenDataSource);
    }

    @Override
    public String getSuccessFormat() {
        return SomI18nProvider.INSTANCE.getValue("benchmark.calinski.harabasz.success");
    }

    @Override
    public String getFailureFormat() {
        return SomI18nProvider.INSTANCE.getValue("benchmark.calinski.harabasz.failure");
    }

}
