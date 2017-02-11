package com.github.attatrol.som.ui.i18n;

import com.github.attatrol.preprocessing.ui.i18n.I18nComboBox;

/**
 * Locally used {@link I18nComboBox}.
 * @author atta_troll
 *
 * @param <V> combo box value parameter
 */
public class SomI18nComboBox<V> extends I18nComboBox<V> {

    public SomI18nComboBox() {
        super(SomI18nProvider.INSTANCE);
    }

}
