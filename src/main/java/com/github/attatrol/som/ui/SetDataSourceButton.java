package com.github.attatrol.som.ui;

import java.util.Optional;

import com.github.attatrol.preprocessing.ui.TokenDataSourceAndMisc;
import com.github.attatrol.preprocessing.ui.TokenDataSourceDialog;
import com.github.attatrol.som.ui.SetupSomPane.SetupFormState;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.scene.control.Button;

/**
 * This button calls dialog for data source setup.
 * @author atta_troll
 *
 */
class SetDataSourceButton extends Button {

    public SetDataSourceButton(SetupSomPane form) {
        super(SomI18nProvider.INSTANCE.getValue("main.button.set.data.source"));
        setOnAction(ev -> {
            final SetupFormState previousState = form.getInternalState();
            form.setInternalState(SetupFormState.DATA_SOURCE_IN_PRORESS_1);
            Optional<TokenDataSourceAndMisc> result =
                    (new TokenDataSourceDialog()).showAndWait();
            if (result.isPresent()) {
                form.getSomComponents().setTdsm(result.get());
                form.setInternalState(SetupFormState.DATA_SOURCE_SET_2);
            }
            else {
                form.setInternalState(previousState);
            }
        });
    }

}
