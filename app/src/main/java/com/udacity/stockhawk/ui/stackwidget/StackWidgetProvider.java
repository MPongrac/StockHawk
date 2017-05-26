/*https://android.googlesource.com/platform/development/+/master/samples/StackWidget?autodive=0/
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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartActivity;
import com.udacity.appmakermike.stockhawklibrary.stockhawklibrary.data.PrefUtils;
import com.udacity.stockhawk.R;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.MPHistoricalQuote.serializeStringArrayHistoricalQuoteList;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.ARG_PARAM_STOCK_HISTORY;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.ARG_PARAM_STOCK_SYMBOL;
import static com.udacity.appmakermike.stockhawklibrary.stockhawklibrary.sync.QuoteSyncJob.ACTION_DATA_UPDATED;


public class StackWidgetProvider extends AppWidgetProvider {
    @SuppressWarnings("WeakerAccess")
    public static final  String SNACKBAR_ACTION           = "com.example.android.stackwidget.SNACKBAR_ACTION";
    public static final  String EXTRA_ITEM                = "com.example.android.stackwidget.EXTRA_ITEM";
    private static final int    HISTORY_DURATION_IN_YEARS = -2; // The past 2 years.

    // This is the standard format that we receive from YahooFinance.
    private static List<HistoricalQuote> mParamHistory;

    private static Calendar from;
    private static Calendar to;
    private static Interval myInterval;
    private static Stock    currStock;
    @SuppressWarnings({"Convert2Diamond", "CanBeFinal"})
    private static Map<String, Stock>                 stocks    = new HashMap<String, Stock>();
    @SuppressWarnings({"Convert2Diamond", "CanBeFinal"})
    private static Map<String, List<HistoricalQuote>> histories = new HashMap<String,
            List<HistoricalQuote>>();
    private static String  currStockSymbol;
    private static int[]   mAppWidgetIds;
    private static Context mContext;


    /**
     * This is the standard override functionality for onDeleted.
     * <p>
     * This is where we clean up all of our memory objects.
     *
     * @param context currently in use.
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        if (mContext != null) {
            mContext = null;
        }
        mParamHistory.clear();
        stocks.clear();
        histories.clear();
        mAppWidgetIds = null;
        currStock = null;
        from = null;
        to = null;
        myInterval = null;
    }

    /**
     * This is the standard override functionality for onReceive.
     *
     * @param context currently in use.
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        if (mContext == null) {
            mContext = context;
        }
    }

    /**
     * This is the standard override functionality for onReceive.
     * <p>
     * It processes the specific intents that are meant to be handled by this object.
     * This includes the general ACTION_DATA_UPDATED and the specific SNACKBAR_ACTION.
     * <p>
     * It is a portion of the link between the data provider and the widget items.
     *
     * @param context currently in use.
     * @param intent  currently being processed.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if (mContext == null) {
            mContext = context;
        }
        from = Calendar.getInstance();
        to = Calendar.getInstance();
        from.add(Calendar.YEAR, HISTORY_DURATION_IN_YEARS);

//        myInterval = Interval.DAILY;
//        myInterval = Interval.WEEKLY;
        myInterval = Interval.MONTHLY;

        AppWidgetManager mgr = AppWidgetManager.getInstance(mContext);
//        if (intent.getAction().equals(android.appwidget.action.APPWIDGET_UPDATE)) {
//
//        }

        if (intent.getAction().equals(SNACKBAR_ACTION)) {
            //noinspection UnusedAssignment
//            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
//                                                 AppWidgetManager.INVALID_APPWIDGET_ID);
            int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);

            currStockSymbol = (String) PrefUtils.getStocks(mContext).toArray()[viewIndex];
            showStockHistoryThread.start();
        }
        if (intent.getAction().equals(ACTION_DATA_UPDATED)) {
            // This is triggered by the filter under the StackWidgetProvider in the
            // manifest file.
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
                                                            StackWidgetProvider.class.getName());
            int[] appWidgetIds = mgr.getAppWidgetIds(thisAppWidget);
            mgr.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stack_view);

        }
        super.onReceive(context, intent);
    }

    /**
     * This is the standard override functionality for onUpdate.
     * <p>
     * Uses a remote adapter to update each of the widgets when data has been updated.
     *
     * @param context          currently in use.
     * @param appWidgetManager currently in use.
     * @param appWidgetIds     An integer array of widget IDs.
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        if (mContext == null) {
            mContext = context.getApplicationContext();
        }
        if (mAppWidgetIds == null) {
            mAppWidgetIds = appWidgetIds;
        }

        // Perform the getStocks call once and re-use the results.
        Set<String> stocks = PrefUtils.getStocks(mContext);

        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            currStockSymbol = stocks.toArray()[i].toString();

            // Here we setup the intent which points to the StackViewService which will
            // provide the views for this collection.
            Intent intent = new Intent(context, StackWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            //noinspection deprecation
            rv.setRemoteAdapter(appWidgetIds[i], R.id.stack_view, intent);

            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view.
            rv.setEmptyView(R.id.stack_view, R.id.empty_view);

            // Here we setup the a pending intent template. Individuals items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
            Intent snackbarIntent = new Intent(context, StackWidgetProvider.class);
            snackbarIntent.setAction(StackWidgetProvider.SNACKBAR_ACTION);
            snackbarIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent snackbarPendingIntent =
                    PendingIntent.getBroadcast(mContext, 0,
                                               snackbarIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.stack_view, snackbarPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * This method is used to obtain the historical data for the provided stock symbol.
     */
    private void getSingleStockHistory() {

        currStock = null;
        try {
            currStock = YahooFinance.get(currStockSymbol, from, to, myInterval);
            Timber.d("getSingleStockHistory: 1 " + currStock);
        }
        catch (IOException e) {
            e.printStackTrace();
            Timber.e(e.getMessage());
            return;
        }

        if (currStock != null) {
            if (!(stocks.containsKey(currStockSymbol))) {
                stocks.put(currStockSymbol, currStock);
            }
            try {
                if (!(histories.containsKey(currStockSymbol))) {
                    histories.put(currStockSymbol, currStock.getHistory());
                }
                //noinspection UnnecessaryLocalVariable
                List<HistoricalQuote> hist = histories.get(currStockSymbol);
                mParamHistory = hist;
            }
            catch (IOException e) {
                e.printStackTrace();
                Timber.e(e.getMessage());
            }
        } else {
            Timber.e("getSingleStockHistory: currStock is null for " + currStockSymbol +
                             ".");
        }

    }

    /**
     * This is a thread variable used to get the stock history and display the bar chart.
     */
    private final Thread showStockHistoryThread = new Thread(new Runnable() {
        @Override
        public void run() {
            getSingleStockHistory();
            displayBarChartGraph.run();
        }
    });

    /**
     * This is the runnable used in the thread above to display the bar chart using an
     * intent.
     */
    private final Runnable displayBarChartGraph = new Runnable() {
        @Override
        public void run() {

            Intent intentShowBarGraph = new Intent(mContext,
                                                   StockBarChartActivity.class);

            intentShowBarGraph.putExtra(ARG_PARAM_STOCK_SYMBOL, currStockSymbol);

            String[] serializedSAHistory =
                    serializeStringArrayHistoricalQuoteList(mParamHistory);

            intentShowBarGraph.putExtra(ARG_PARAM_STOCK_HISTORY, serializedSAHistory);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(mContext, 0, intentShowBarGraph, 0);

            try {
                pendingIntent.send();
            }
            catch (Exception e) {
                e.printStackTrace();
                Timber.d("ERROR: onClickShowGraph(startActivity) - " + e.getMessage());
            }
        }
    };

}