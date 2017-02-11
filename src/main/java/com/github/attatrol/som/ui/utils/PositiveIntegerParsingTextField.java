package com.github.attatrol.som.ui.utils;

import com.github.attatrol.preprocessing.ui.misc.PositiveNumericTextField;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * This is a text field which safely produces {@link IntegerProperty} from
 * its input text.
 * @author atta_troll
 *
 */
public class PositiveIntegerParsingTextField extends PositiveNumericTextField {

    /**
     * Integer type value property.
     */
    private SimpleObjectProperty<Integer> value = new SimpleObjectProperty<>();

    /**
     * Text used to replace input text in case if it can not be parced into double.
     */
    private Integer oldText = 0;

    /**
     * Default ctor.
     */
    public PositiveIntegerParsingTextField() {
        super();
        focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
                if (!newValue && oldValue) {
                    try {
                        final int newInt = Integer.parseInt(getText());
                        oldText = value.get();
                        value.set(newInt);
                    }
                    catch (NumberFormatException | NullPointerException ex) {
                        setText(Integer.toString(oldText));
                    }
                }
            }
        });
    }

    /**
     * @return Integer type value property of the text field
     */
    public SimpleObjectProperty<Integer> getValueProperty() {
        return value;
    }

    /**
     * Use this method instead of {@link #setText(String)}.<br/>
     * For convenience purposes do not use negative integer, you actually may use them here
     * but our class called <i>PositiveInteger</>ParsingTextField.
     * @param newValue new integer value
     */
    public void setTextAndValue(int newValue) {
        value.set(newValue);
        final String text = Integer.toString(newValue);
        setText(text);
        oldText = newValue; 
    }
}