package com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary;

/**
 * Created by MPongrac on 02/05/2017.
 * This class contains all constants required for StockBarChart creation.
 */

@SuppressWarnings("WeakerAccess")
public class StockBarChartConstants {

    public static final String ARG_PARAM_STOCK_SYMBOL = "stock_symbol";
    public static final String ARG_PARAM_STOCK_HISTORY = "stock_history";

    // These elements are for the customizing the bar graph.
    public static final float NULL_FLOAT_VALUE      = 0f;
    public static final float DESCRIPTION_FONT_SIZE = 120f;
    public static final float DATA_FONT_SIZE        = 18f;
    public static final float LEGEND_FONT_SIZE      = 48f;
    public static final float X_AXIS_FONT_SIZE      = 24f;
    public static final float Y_AXIS_FONT_SIZE      = 24f;


    private static final float VISIBLE_DATA_ELEMENTS = 12f;
    private static final float BARS_PER_DATA_ELEMENT = 5f;
    public static final float VISIBLE_X_RANGE       =
            (float)(VISIBLE_DATA_ELEMENTS * BARS_PER_DATA_ELEMENT);


    //    public static final String SERIAL_DATE_FORMAT = "dd/MM/YYYY";
    public static final String SERIAL_DATE_FORMAT = "yyyy-MM-dd";

}
