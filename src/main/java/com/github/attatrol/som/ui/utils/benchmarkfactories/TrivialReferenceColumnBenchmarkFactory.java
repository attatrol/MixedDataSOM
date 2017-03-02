package com.github.attatrol.som.ui.utils.benchmarkfactories;

import java.util.Optional;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.preprocessing.ui.misc.GenericValueReturnDialog;
import com.github.attatrol.som.benchmark.ClusteringBenchmark;
import com.github.attatrol.som.benchmark.TrivialReferenceColumnBenchmark;
import com.github.attatrol.som.som.SomClusterResult;
import com.github.attatrol.som.ui.ResultPane;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.scene.control.ComboBox;

public class TrivialReferenceColumnBenchmarkFactory implements BenchmarkUiFactory {

    @Override
    public ClusteringBenchmark getBenchmark(AbstractTokenDataSource<?> tokenDataSource,
            String[] columnNames, SomClusterResult clusterResult,
            DistanceFunction distanceFunction) {
            Optional<Integer> columnIndex = (new ColumnChoosingDialog(columnNames, tokenDataSource.getRecordLength())).showAndWait();
            if (columnIndex.isPresent()) {
                return new TrivialReferenceColumnBenchmark(clusterResult, distanceFunction, tokenDataSource, columnIndex.get());
            }
            else {
                return null;
            }
    }

    @Override
    public String getSuccessFormat() {
        return SomI18nProvider.INSTANCE.getValue("benchmark.trivial.reference.success");
    }

    @Override
    public String getFailureFormat() {
        return SomI18nProvider.INSTANCE.getValue("benchmark.trivial.reference.failure");
    }

    private static class ColumnChoosingDialog extends GenericValueReturnDialog<Integer> {

        private ComboBox<String> titleChoosingComboBox = new ComboBox<>();

        public ColumnChoosingDialog(String[] titles, int recordSize) {
            setWidth(450);
            setTitle(SomI18nProvider.INSTANCE.getValue("benchmark.trivial.choose.column.title"));
            String[] columnTitles = new String[recordSize];
            for (int i = 0; i < recordSize; i++) {
                columnTitles[i] = ResultPane.getColumnName(i, titles);
            }
            titleChoosingComboBox.getItems().addAll(columnTitles);
            this.getDialogPane().setContent(titleChoosingComboBox);
        }

        @Override
        protected Integer createResult() {
            return titleChoosingComboBox.getSelectionModel().getSelectedIndex();
        }

        @Override
        protected void validate() throws Exception {
            if (titleChoosingComboBox.getSelectionModel().getSelectedIndex() < 0) {
                throw new IllegalArgumentException(SomI18nProvider
                        .INSTANCE.getValue("benchmark.trivial.reference.bad.title"));
            }
        }
    }
}
