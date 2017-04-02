package com.github.attatrol.som;


    import java.util.Optional;

import com.github.attatrol.preprocessing.datasource.AbstractTokenDataSource;
import com.github.attatrol.preprocessing.ui.TokenDataSourceAndMisc;
import com.github.attatrol.preprocessing.ui.TokenDataSourceDialog;
import com.github.attatrol.preprocessing.ui.misc.UiUtils;
import com.github.attatrol.som.ui.TestRunner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

    /**
     * Test application that shows both dialogs.
     * @author atta_troll
     *
     */
    public class TestApplication extends Application {

        /**
         * {@inheritDoc}
         */
        @Override
        public void start(Stage primaryStage) throws Exception {
            primaryStage.setScene(new TestScene());
            primaryStage.show();
            primaryStage.setTitle("Test");
        }

        /**
         * Entry point
         * @param strings not in use
         */
        public static final void main(String...strings) {
            launch(strings);
        }

        private static class TestScene extends Scene {

            TestScene() {
                super(new HBox());
                HBox hBox = (HBox) getRoot();
                Button callDataSourceDialogButton = new Button("Call data source dialog");
                callDataSourceDialogButton.setOnAction(ev-> {
                    Optional<TokenDataSourceAndMisc> result =
                            (new TokenDataSourceDialog()).showAndWait();
                    if (result.isPresent()) {
                        AbstractTokenDataSource<?> dataSource = result.get().getTokenDataSource();
                        UiUtils.showInfoMessage(String.format("Hey, we have a data source\n%s", dataSource));
                        final CategoricalDistanceProducer distanceFactory = new CategoricalDistanceProducer(result.get());
                        try {
                            final TestRunner runner = new TestRunner(result.get(), distanceFactory);
                            runner.run();
                        } catch (Exception ex) {
                            UiUtils.showExceptionMessage(ex);
                        }
                    }
                    else {
                        UiUtils.showInfoMessage("Oh no, got no data source from dialog!");
                    }
                });
                hBox.getChildren().addAll(callDataSourceDialogButton);
            }
        }

    }

