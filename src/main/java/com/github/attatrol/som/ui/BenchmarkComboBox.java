
package com.github.attatrol.som.ui;

import java.io.IOException;

import com.github.attatrol.preprocessing.ui.misc.UiUtils;
import com.github.attatrol.som.benchmark.ClusteringBenchmark;
import com.github.attatrol.som.som.SomClusterResult;
import com.github.attatrol.som.ui.SetupSomPane.SetupFormState;
import com.github.attatrol.som.ui.i18n.SomI18nComboBox;
import com.github.attatrol.som.ui.utils.benchmarkfactories.BenchmarkUiFactory;

public class BenchmarkComboBox extends SomI18nComboBox<BenchmarkUiFactory> {

    public BenchmarkComboBox(SetupSomPane form) {
        super();
        this.setOnHidden((event) -> {
            form.setInternalState(SetupFormState.BENCHMARK_IN_PROGRESS_10);
            final SomComponents somData = form.getSomComponents();
            final BenchmarkUiFactory factory = getSelectionModel().getSelectedItem();
            try {
                if (factory != null) {
                    final ClusteringBenchmark benchmark = factory.getBenchmark(
                            somData.getTdsm().getTokenDataSource(), somData.getTdsm().getTitles(),
                            SomClusterResult.produceClusterResult(somData.getSom(),
                            somData.getTdsm().getTokenDataSource()),
                            somData.getDistanceFunction());
                    if (benchmark != null) {
                        String message = benchmark.hasFailed() ? factory.getFailureFormat()
                                : String.format(factory.getSuccessFormat(), benchmark.getValue());
                        UiUtils.showInfoMessage(message);
                    }
                }
                //getSelectionModel().clearSelection();
                form.setInternalState(SetupFormState.SOM_COMPLETED_8);
            }
            catch (IOException ex) {
                form.setInternalState(SetupFormState.RESULT_FORM_PRODUCTION_ERROR);
                UiUtils.showExceptionMessage(ex);
            }
        });
    }

}
