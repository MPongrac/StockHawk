package com.udacity.appmakermike.stockhawklibrary.stockhawklibrary.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.udacity.appmakermike.stockhawklibrary.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

@SuppressWarnings("DanglingJavadoc")
public final class PrefUtils {

    /**
     * This is the required standard default constructor.
     */
    private PrefUtils() {
    }

    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        boolean initialized = prefs.getBoolean(initializedKey, false);

        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            editor.putStringSet(stocksKey, defaultStocks);
            editor.apply();
            return defaultStocks;
        }
        return prefs.getStringSet(stocksKey, new HashSet<String>());

    }

    private static void editStockPref(Context context, String symbol, Boolean add) {
        String key = context.getString(R.string.pref_stocks_key);
        Set<String> stocks = getStocks(context);

        Timber.d("Before: " + stocks.toString());
        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }
        Timber.d("After:  " + stocks.toString());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, stocks);
        editor.apply();
    }

    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);
    }

    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }

    /**
     * #######################################################################
     *
     * Functionality that I added to assist with stock not found messages.
     *
     * It is a duplicate of the existing stock logic, but uses different keys.
     *
     * #######################################################################
     */

    /**
     * An externally available method used to identify whether a symbol is invalid
     * while providing the option of deleting it after determining its existence.
     *
     * @param context currently in use.
     * @param symbol to be processed.
     * @param remove A boolean value which indicates whether or not the symbol should
     *               be removed, or not, if found.
     * @return a boolean value which indicates whether, or not, the symbol was found.
     */
    public static Boolean isStockInvalid(Context context, String symbol,
                                         @SuppressWarnings("SameParameterValue")
                                                 Boolean remove){
        Boolean ret = false;

        if (getInvalidStocks(context).contains(symbol)) {
            ret = true;
            if (remove) {
                removeInvalidStock(context, symbol);
            }
        }
        return ret;
    }

    /**
     * This method returns a string set of the existing invalid stock symbols.
     *
     * @param context currently in use.
     * @return a string set of the existing invalid stock symbols.
     */
    public static Set<String> getInvalidStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_invalid_stocks_key);
        String initializedKey = context.getString(
                R.string.pref_invalid_stocks_initialized_key);

        HashSet<String> defaultStocks = new HashSet<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        boolean initialized = prefs.getBoolean(initializedKey, false);

        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            editor.putStringSet(stocksKey, defaultStocks);
            editor.apply();
            return defaultStocks;
        }
        return prefs.getStringSet(stocksKey, new HashSet<String>());

    }

    /**
     * The internal method used by the externally available methods to manage invalid
     * stock shared preferences.
     *
     * @param context currently in use.
     * @param symbol to be processed.
     * @param add A boolean indicator used to identify whether to add or remove the
     *            provided symbol to/from the existing shared preferences.
     */
    private static void editInvalidStockPref(Context context, String symbol, Boolean
            add) {
        String key = context.getString(R.string.pref_invalid_stocks_key);
        Set<String> invalid_stocks = getStocks(context);

        if (add) {
            invalid_stocks.add(symbol);
        } else {
            invalid_stocks.remove(symbol);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, invalid_stocks);
        editor.apply();
    }

    /**
     * Externally available method for adding symbols to invalid stock shared
     * preferences.
     *
     * @param context currently in use.
     * @param symbol to be processed.
     */
    public static void addInvalidStock(Context context, String symbol) {
        editInvalidStockPref(context, symbol, true);
    }

    /**
     * Externally available method for removing symbols from invalid stock shared
     * preferences.
     *
     * @param context currently in use.
     * @param symbol to be processed.
     */
    @SuppressWarnings("WeakerAccess")
    public static void removeInvalidStock(Context context, String symbol) {
        editInvalidStockPref(context, symbol, false);
    }

}
