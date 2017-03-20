package com.github.attatrol.som.ui.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.github.attatrol.preprocessing.ui.i18n.I18nProvider;

/**
 * Localizes UI of this application. Eager singleton
 * @author atta_troll
 *
 */
public class SomI18nProvider implements I18nProvider {

    /**
     * Singleton instance.
     */
    public static final SomI18nProvider INSTANCE = new SomI18nProvider();

    public static final String UNKNOWN = "VALUE_MISSING";

    /**
     * Current bundle.
     */
    private ResourceBundle currentBundle;

    /**
     * Default ctor.
     */
    private SomI18nProvider() {
        currentBundle = ResourceBundle.getBundle("somui");
    }

    @Override
    public String getValue(String key) {
        try {
            return currentBundle.getString(key);
        }
        catch (MissingResourceException ex) {
            return UNKNOWN;
        }
    }

    @Override
    public String getValue(Object object) {
        return getValue(String.format("name.%s", object.getClass().getName()));
    }
}
