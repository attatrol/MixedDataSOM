package com.github.attatrol.som.ui.utils;

/**
 * Accepts only values between zero and 1.
 * @author atta_troll
 *
 */
public class ZeroToOneDoubleParsingTetField extends PositiveDoubleParsingTextField {

    public ZeroToOneDoubleParsingTetField() {
        super();
    }

    @Override
    protected double parse() throws Exception {
        final double parsedValue = super.parse();
        if (parsedValue <= 1.) {
            return parsedValue;
        }
        else {
            throw new IllegalArgumentException();
        }
    }
}
