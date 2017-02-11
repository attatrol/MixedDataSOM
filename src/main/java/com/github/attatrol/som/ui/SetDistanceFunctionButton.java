package com.github.attatrol.som.ui;

import java.util.Optional;

import com.github.attatrol.preprocessing.distance.DistanceFunction;
import com.github.attatrol.preprocessing.ui.DistanceFunctionDialog;
import com.github.attatrol.som.ui.SetupSomPane.SetupFormState;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.scene.control.Button;

/**
 * This button calls for distance function setup dialog.
 * @author atta_troll
 *
 */
class SetDistanceFunctionButton extends Button {

    public SetDistanceFunctionButton(SetupSomPane form) {
        super(SomI18nProvider.INSTANCE.getValue("main.button.set.distance.function"));
        setOnAction(ev -> {
            final SetupFormState previousState = form.getInternalState();
            form.setInternalState(SetupFormState.DISTANCE_FUNCTION_IN_PRORESS_3);
            Optional<DistanceFunction> result =
                    (new DistanceFunctionDialog(form.getSomComponents().getTdsm()))
                    .showAndWait();
            if (result.isPresent()) {
                form.getSomComponents().setDistanceFunction(result.get());
                form.setInternalState(SetupFormState.DISTANCE_FUNCTION_SET_4);
            }
            else {
                form.setInternalState(previousState);
            }
        });
    }

}
