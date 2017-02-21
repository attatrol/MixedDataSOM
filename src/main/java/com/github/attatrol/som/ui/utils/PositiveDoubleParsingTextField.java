package com.github.attatrol.som.ui.utils;

import com.github.attatrol.preprocessing.ui.misc.PositiveDoubleTextField;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * This is a text field which safely produces {@link DoubleProperty} from
 * its input text.
 * @author atta_troll
 *
 */
public class PositiveDoubleParsingTextField extends PositiveDoubleTextField {

    /**
     * Double type value property.
     */
    private SimpleObjectProperty<Double> value = new SimpleObjectProperty<>();

    /**
     * Text used to replace input text in case if it can not be parced into double.
     */
    private double previousValue = 0.;

    /**
     * Default ctor.
     */
    public PositiveDoubleParsingTextField() {
        super();
        focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
                if (!newValue && oldValue) {
                    try {
                        final double newDouble = parse();
                        previousValue = value.get();
                        value.set(newDouble);
                    }
                    catch (Exception ex) {
                        setText(Double.toString(previousValue));
                    }
                }
            }
        });
    }

    /**
     * @return Double type value property of the text field
     */
    public SimpleObjectProperty<Double> getValueProperty() {
        return value;
    }

    /**
     * Use this method instead of {@link #setText(String)}.
     * @param newValue new double value
     */
    public void setTextAndValue(double newValue) {
        value.set(newValue);
        final String text = Double.toString(newValue);
        setText(text);
        previousValue = newValue; 
    }

    /**
     * Parses text and checks parsed value validity.
     * @return text parsed value
     * @throws Exception if parsed value is invalid or unacceptable
     */
    protected double parse() throws Exception {
        final String text = getText();
        return Double.parseDouble(text);
    }
}
