package com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;
import yahoofinance.histquotes.HistoricalQuote;

import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.SERIAL_DATE_FORMAT;

/**
 * Created by MPongrac on 02/05/2017.
 * This class is designed to add new functionality to an existing class
 * defined in the imported YahooFinance API.
 */

public class MPHistoricalQuote extends HistoricalQuote {

    @SuppressWarnings("WeakerAccess")
    public static final String SERIAL_VALUE_SEPARATOR = ";";

    /**
     * This is a standard constructor.
     */
    @SuppressWarnings("WeakerAccess")
    public MPHistoricalQuote() {
        super();
    }

    /**
     * This allows us to take existing HQs and convert them.
     * @param hq A standard Historical Quote object.
     */
    @SuppressWarnings("WeakerAccess")
    public MPHistoricalQuote(HistoricalQuote hq) {
        super();
        this.setSymbol(hq.getSymbol());
        this.setOpen(hq.getOpen());
        this.setDate(hq.getDate());
        this.setVolume(hq.getVolume());
        this.setAdjClose(hq.getAdjClose());
        this.setClose(hq.getClose());
        this.setHigh(hq.getHigh());
        this.setLow(hq.getLow());
    }

    /**
     * Serializes the current object into a specific string format.
     * @return The serialized version of the current object.
     */
    @SuppressWarnings("WeakerAccess")
    public String serializeHistoricalQuote() {
        String ser = "";

        ser = ser + "\"" + this.getSymbol() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + convertCalendar2String(this.getDate()) + "\"" +
                SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + this.getOpen() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + this.getHigh() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + this.getLow() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + this.getClose() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + this.getVolume() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + this.getAdjClose() + "\"\n";

        return ser;
    }

    /*
        These are helpful utility methods that can be helpful for serialization and
        de-serialization of Historical Quote data.
     */

    /**
     * Validate and convert a string into a Calendar object.
     * <p>
     * This is helpful when de-serializing Historical Quotes.
     * The following input date formats are supported:
     * DD/MM/YYYY, DD-MM-YYYY, and DD.MM.YYYY
     *
     * @param strVal A date string to be converted to a Calendar object.
     * @return The resulting Calendar object.
     */
    @Nullable
    public static Calendar convertString2Calendar(String strVal) {
        Timber.d("convertString2Calendar: " + strVal);
        Calendar calDate = Calendar.getInstance();
        String[] parts   = strVal.split("-");
        if (parts.length == 1) {
            parts = strVal.split("/");
        }
        if (parts.length == 1) {
            parts = strVal.split(".");
        }
        if (parts.length == 1) {
            Timber.e("convertString2Calendar: " +
                             "Expected Format: " + SERIAL_DATE_FORMAT + " " +
                             "Invalid Date Value (not enough " +
                             "parts or invalid separator /.- are allowed): " + strVal);
            return null;
        }
        String strVal2 = strVal;
        // The string could be turned around the wrong way: 18/04/2015
        if (Integer.valueOf(parts[2]) > 1900) {
            String dy = parts[0];
            String yr = parts[2];

            parts[2] = dy;
            parts[0] = yr;
            strVal2 = parts[2] + "-" + parts[1]  + "-" + parts[0];
        }

//        Timber.d("convertString2Calendar: parts[2] day   " + parts[2]);
//        Timber.d("convertString2Calendar: parts[1] month " + parts[1]);
//        Timber.d("convertString2Calendar: parts[0] year  " + parts[0]);

        if (Integer.valueOf(parts[0]) < 1900) {
            Timber.e("convertString2Calendar: " +
                             "Expected Format: " + SERIAL_DATE_FORMAT + " " +
                             "Invalid Date Value (The year" +
                             " is before 1900): " + strVal2);
            return null;
        }

        Date dt = Date.valueOf(strVal2);

//        Timber.d("convertString2Calendar: Date.valueOf(strVal2) " +
//                         dt);
//

        calDate.setTime(dt);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(SERIAL_DATE_FORMAT);
        Timber.d("convertString2Calendar: formattedCalendarDate " +
                         sdf.format(calDate.getTime()));
        return calDate;
    }

    /**
     * Convert a string formatted number into a BigDecimal object.
     * @param strVal A string value that should be converted to a BigDecimal object.
     * @return The resulting BigDecimal Object.
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public static java.math.BigDecimal convertString2BigDecimal(String strVal) {
        if (strVal == null) {
            return null;
        }
        return new java.math.BigDecimal(strVal);
    }

    /**
     * Convert a Calendar object into a string formatted date.
     * @param calendar The Calendar object to be converted to a formatted date string.
     * @return The formatted date string required for serialization.
     */
    @Nullable
    public static String convertCalendar2String(Calendar calendar) {
        String                                             strDate = null;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf     = new SimpleDateFormat(SERIAL_DATE_FORMAT);

        if (calendar != null) {
            strDate = sdf.format(calendar.getTime());
        }

        return strDate;
    }

    /**
     * Converts a serialized HQ string into a HistoricalQuote object.
     * @param serHQ The string representing a serialized HQ.
     * @return The resulting HQ object.
     */
    public static HistoricalQuote deserializeHistoricalQuote(String serHQ) {
        HistoricalQuote hq = new HistoricalQuote();
        // Remove the linefeed
        serHQ = serHQ.replace("\n", "");
        String[] cols = serHQ.split(SERIAL_VALUE_SEPARATOR);

        // Remove any existing quotes after splitting the array into its elements.
        cols[0] = cols[0].replace("\"", "");
        cols[1] = cols[1].replace("\"", "");
        cols[2] = cols[2].replace("\"", "");
        cols[3] = cols[3].replace("\"", "");
        cols[4] = cols[4].replace("\"", "");
        cols[5] = cols[5].replace("\"", "");
        cols[6] = cols[6].replace("\"", "");

        // Present the results for verification, when debugging.
        Timber.d("deserializeHistoricalQuote  serHQ: " + serHQ);
        Timber.d("deserializeHistoricalQuote  cols[0]: " + cols[0]);
        Timber.d("deserializeHistoricalQuote  cols[1]: " + cols[1]);
        Timber.d("deserializeHistoricalQuote  cols[2]: " + cols[2]);
        Timber.d("deserializeHistoricalQuote  cols[3]: " + cols[3]);
        Timber.d("deserializeHistoricalQuote  cols[4]: " + cols[4]);
        Timber.d("deserializeHistoricalQuote  cols[5]: " + cols[5]);
        Timber.d("deserializeHistoricalQuote  cols[6]: " + cols[6]);

        // There are versions of serialization with the stock symbol and without.
        // This is the reason for the separation.
        if (cols.length == 8) {
            cols[7] = cols[7].replace("\"", "");
            Timber.d("deserializeHistoricalQuote  cols[7]: " + cols[7]);
            hq.setSymbol(cols[0]);
            hq.setDate(convertString2Calendar(cols[1]));
            hq.setOpen(convertString2BigDecimal(cols[2]));
            hq.setHigh(convertString2BigDecimal(cols[3]));
            hq.setLow(convertString2BigDecimal(cols[4]));
            hq.setClose(convertString2BigDecimal(cols[5]));
            // If the adjusted close value is null, just use the standard close value.
            hq.setAdjClose(convertString2BigDecimal(cols[7].replace
                    ("null", cols[5])));
            hq.setVolume(Long.getLong(cols[6]));
        } else {
            hq.setSymbol("");
            hq.setDate(convertString2Calendar(cols[0]));
            hq.setOpen(convertString2BigDecimal(cols[1]));
            hq.setHigh(convertString2BigDecimal(cols[2]));
            hq.setLow(convertString2BigDecimal(cols[3]));
            hq.setClose(convertString2BigDecimal(cols[4]));
            // If the adjusted close value is null, just use the standard close value.
            hq.setAdjClose(convertString2BigDecimal(cols[6].replace
                    ("null", cols[4])));
            hq.setVolume(Long.getLong(cols[5]));
        }

        return hq;
    }

    /**
     * Serialize a HQ object into a specific string format.
     *
     * This is necessary since the standard serialization functionality does not work
     * properly with this object type.
     *
     * @param hq A standard HistoricalQuote object to be serialized.
     * @return The string formatted serialization of the provided HQ instance.
     */
    @SuppressWarnings("WeakerAccess")
    public static String serializeHistoricalQuote(HistoricalQuote hq) {
        String ser = "";

        ser = ser + "\"" + hq.getSymbol() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + convertCalendar2String(hq.getDate()) + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + hq.getOpen() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + hq.getHigh() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + hq.getLow() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + hq.getClose() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + hq.getVolume() + "\"" + SERIAL_VALUE_SEPARATOR;
        ser = ser + "\"" + hq.getAdjClose() + "\"\n";

        return ser;
    }

    /**
     * Take a list of HistoricalQuote objects and serialize them into a string array.
     * @param hist A list of HistoricalQuote objects.
     * @return The resulting serialized string array.
     */
    @SuppressWarnings("unused")
    public static String[] serializeStringArrayHistoricalQuoteList(List<HistoricalQuote> hist) {
        // These checks are in case we are offline and have no historical data.
        if (hist == null) {
            return null;
        }
        if (hist.size() < 1) {
            return null;
        }
        String[] ret = new String[hist.size()];

        for (int i = 0; i < hist.size(); i++) {
            try {
                MPHistoricalQuote mpHq = new MPHistoricalQuote(hist.get(i));
                ret[i] = mpHq.serializeHistoricalQuote() + "\n";
//                Timber.d("serializeStringArrayHistoricalQuoteList hist.get(i)" + hist.get(i));
//                Timber.d("serializeStringArrayHistoricalQuoteList ret[i]" + ret[i]);
            }
            catch (Exception e) {
                e.printStackTrace();
                Timber.e(e.getMessage());
            }
        }

        return ret;
    }

    /**
     * Takes a serialized version of an MPHistoricalQuote object and restores it to an
     * object state.
     * @param serMPHQ The serialized MPHistoricalQuote object string.
     * @return The resulting object instance.
     */
    @SuppressWarnings("unused")
    public static MPHistoricalQuote deserializeMPHistoricalQuote(String serMPHQ) {
        MPHistoricalQuote hq = new MPHistoricalQuote();
        // Remove the linefeed
        serMPHQ = serMPHQ.replace("\n", "");
        String[] cols = serMPHQ.split(SERIAL_VALUE_SEPARATOR);

        if (cols.length == 8) {
            hq.setSymbol(cols[0]);
            hq.setDate(convertString2Calendar(cols[1]));
            hq.setOpen(convertString2BigDecimal(cols[2]));
            hq.setHigh(convertString2BigDecimal(cols[3]));
            hq.setLow(convertString2BigDecimal(cols[4]));
            hq.setClose(convertString2BigDecimal(cols[5]));
            hq.setAdjClose(convertString2BigDecimal(cols[7]));
            hq.setVolume(Long.getLong(cols[6]));
        } else {
            hq.setSymbol("");
            hq.setDate(convertString2Calendar(cols[0]));
            hq.setOpen(convertString2BigDecimal(cols[1]));
            hq.setHigh(convertString2BigDecimal(cols[2]));
            hq.setLow(convertString2BigDecimal(cols[3]));
            hq.setClose(convertString2BigDecimal(cols[4]));
            hq.setAdjClose(convertString2BigDecimal(cols[6]));
            hq.setVolume(Long.getLong(cols[5]));
        }

        return hq;
    }

    /**
     * Take a list of HistoricalQuote objects and serialize them into an array list of
     * type string.
     *
     * @param hist A list of HistoricalQuote objects.
     * @return The resulting array list of type string.
     */
    @SuppressWarnings("unused")
    public static ArrayList<String> serializeArrayListStringHistoricalQuoteList
    (List<HistoricalQuote> hist) {
        //noinspection Convert2Diamond
        ArrayList<String> ret = new ArrayList<String>();

        for (int i = 0; i < hist.size(); i++) {
            ret.add(MPHistoricalQuote.serializeHistoricalQuote(hist.get(i)) + "\n");
        }

        return ret;
    }

}
