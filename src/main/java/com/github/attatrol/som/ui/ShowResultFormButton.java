package com.github.attatrol.som.ui;

import java.io.IOException;

import com.github.attatrol.preprocessing.ui.TokenDataSourceAndMisc;
import com.github.attatrol.preprocessing.ui.misc.UiUtils;
import com.github.attatrol.som.som.Som;
import com.github.attatrol.som.som.SomClusterResult;
import com.github.attatrol.som.ui.SetupSomPane.SetupFormState;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * This button shows a form with a result.
 * @author atta_troll
 *
 */
class ShowResultFormButton extends Button {

    public ShowResultFormButton(SetupSomPane form) {
        super(SomI18nProvider.INSTANCE.getValue("main.button.show.result"));
        setOnAction(ev -> {
            form.setInternalState(SetupFormState.RESULT_FORM_CRREATION_IN_PROGRESS_9);
            final Thread resultFormCreationThread = new Thread(new ResultFormProducer(form));
            resultFormCreationThread.setDaemon(true);
            resultFormCreationThread.start();
        });
    }

    /**
     * Helper runnable, used to produce result form.
     * @author atta_troll
     *
     */
    private static class ResultFormProducer implements Runnable {

        private SetupSomPane form;

        public ResultFormProducer(SetupSomPane form) {
            this.form = form;
        }

        @Override
        public void run() {
            final SomComponents somData = form.getSomComponents();
            final Som som = somData.getSom();
            final TokenDataSourceAndMisc tdsm = somData.getTdsm();
            try {
                final SomClusterResult clusterResult = SomClusterResult.produceClusterResult(som,
                        tdsm.getTokenDataSource());
                Platform.runLater(() -> {
                    final ResultPane resultPane = new ResultPane(tdsm, clusterResult, som);
                    final Stage stage = new Stage();
                    stage.setScene(new Scene(resultPane));
                    stage.setResizable(true);
                    stage.setTitle(SomI18nProvider.INSTANCE.getValue("result.title"));
                    stage.show();
                    form.setInternalState(SetupFormState.SOM_COMPLETED_8);
                });
            }
            catch (IOException ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.RESULT_FORM_PRODUCTION_ERROR);
                    UiUtils.showExceptionMessage(ex);
                });
            }
            catch (Exception ex) {
                Platform.runLater(() -> {
                    form.setInternalState(SetupFormState.UNKNOWN_ERROR);
                    UiUtils.showExceptionMessage(ex);
                });
            }
        }
        
    }
}
