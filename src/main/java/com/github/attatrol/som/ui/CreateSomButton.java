
package com.github.attatrol.som.ui;

import com.github.attatrol.som.ui.SetupSomPane.SetupFormState;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.scene.control.Button;

/**
 * SOM creation button, controller included.
 * @author atta_troll
 *
 */
class CreateSomButton extends Button {

    /**
     * Default ctor.
     * @param form related form
     */
    public CreateSomButton(SetupSomPane form) {
        super(SomI18nProvider.INSTANCE.getValue("main.button.create.som"));
        setOnAction(ev -> {
            form.setInternalState(SetupFormState.SOM_CREATION_IN_PROGRESS_5);
            Thread creationThread = new Thread(SomMode.EPOCH_NUMBER_SET.getСreationProcess(form));
            creationThread.setDaemon(true);
            creationThread.start();
        });
    }
}
