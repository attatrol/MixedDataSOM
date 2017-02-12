package com.github.attatrol.som;

import java.util.Locale;

import com.github.attatrol.som.ui.SetupSomPane;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SomApplication extends Application {

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(new SetupSomPane()));
        primaryStage.setTitle(SomI18nProvider.INSTANCE.getValue("main.title"));
        primaryStage.show();
    }

    /**
     * Entry point
     * @param strings not in use
     */
    public static final void main(String...strings) {
        launch(strings);
    }


}
