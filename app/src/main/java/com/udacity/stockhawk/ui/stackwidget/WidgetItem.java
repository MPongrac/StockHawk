/* https://android.googlesource.com/platform/development/+/master/samples/StackWidget?autodive=0/
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.udacity.stockhawk.ui.stackwidget;

import android.graphics.Color;

import java.text.DecimalFormat;


@SuppressWarnings("WeakerAccess")
public class WidgetItem {
    @SuppressWarnings("CanBeFinal")
    public String symbol;
    @SuppressWarnings("CanBeFinal")
    public String currPrice;
    @SuppressWarnings("CanBeFinal")
    public String currPriceChange;
    public int    currChangeColor;

    public static final String PRICE_FORMAT = "$####0.00";
    // This variable exists as a possibility of separate future format changes.
    public static final String PRICE_CHANGE_FORMAT = PRICE_FORMAT;

    private static final String COLOR_WIN  = "#00C853";
    private static final String COLOR_LOSS = "#D50000" ;

    public WidgetItem(String strSymbol, String price, String priceChange) {
        this.symbol = strSymbol;

        this.currPrice = formatPrice(price);

        // Check the sign of the price change before formatting it.
        currChangeColor = getChangeColor(priceChange);

        this.currPriceChange = formatPriceChange(priceChange);
    }

    public static int getChangeColor(String priceChange) {
        int changeColor = Color.parseColor(COLOR_WIN);
        if (priceChange.startsWith("-")) {
            changeColor = Color.parseColor(COLOR_LOSS);
        }
        return changeColor;
    }

    /**
     * This is a standard method to create formatting consistency.
     * @param price This is the numerical string value to be formatted.
     * @return the properly formatted string value.
     */
    public static String formatPrice(String price){
        String ret;

        DecimalFormat df = new DecimalFormat(PRICE_FORMAT);

        float fPrice;
        fPrice = Float.parseFloat( price);
        ret = df.format(fPrice);

        return ret;
    }

    /**
     * This is a standard method to create formatting consistency.
     * @param priceChange This is the numerical string value to be formatted.
     * @return the properly formatted string value.
     */
    public static String formatPriceChange(String priceChange){
        String ret;

        DecimalFormat df = new DecimalFormat(PRICE_CHANGE_FORMAT);
        String sign = "+";
        if (priceChange.startsWith("-")) {
            sign = "";
        }

        float fPriceChange;
        fPriceChange = Float.parseFloat(priceChange);
        ret = sign + df.format(fPriceChange);

        return ret;
    }
}
