package com.github.attatrol.som.ui;

import com.github.attatrol.som.ui.SetupSomPane.SetupFormState;
import com.github.attatrol.som.ui.i18n.SomI18nProvider;

import javafx.scene.control.Button;

/**
 * SOM learning button, controller included.
 * @author atta_troll
 *
 */
class LearnSomButton extends Button {

    public LearnSomButton(SetupSomPane form) {
        super(SomI18nProvider.INSTANCE.getValue("main.button.learn.som"));
        setOnAction(ev -> {
            form.setInternalState(SetupFormState.SOM_LEARNING_IN_PROGRESS_7);
            final Thread learningThread = new Thread(form.getSomComponents()
                    .getLastCreatedSomMode().getLearningProcess(form));
            learningThread.setDaemon(true);
            learningThread.start();
        });
    }

}
