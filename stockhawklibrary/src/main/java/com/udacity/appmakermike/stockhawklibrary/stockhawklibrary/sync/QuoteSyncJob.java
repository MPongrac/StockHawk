package com.udacity.appmakermike.stockhawklibrary.stockhawklibrary.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.udacity.appmakermike.stockhawklibrary.stockhawklibrary.data.Contract;
import com.udacity.appmakermike.stockhawklibrary.stockhawklibrary.data.PrefUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk" +
            ".ACTION_DATA_UPDATED";
    private static final int PERIOD = 900000; // Update every 15 minutes.
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        // This was added to eliminate unnecessary calls to the API when it is not
        // providing historical quote details.
        boolean apiHistoryAvailable = true;

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();

                Stock stock = quotes.get(symbol);
                StockQuote quote = stock.getQuote();

                // Make sure that all requested quotes have data.
                // It it has no price, then it has no data.
                if (quote.getPrice() != null) {
                    float price         = quote.getPrice().floatValue();
                    float change        = quote.getChange().floatValue();
                    float percentChange = quote.getChangeInPercent().floatValue();

                    // WARNING! Don't request historical data for a stock that doesn't exist!
                    // The request will hang forever X_x
                    List<HistoricalQuote> history = null;
                    try {
                        if (apiHistoryAvailable) {
                            history = stock.getHistory(from, to, Interval.MONTHLY);
                        }
                    }
                    catch (FileNotFoundException e) {
                        Timber.e("API getHistory - File Not Found:  " + e.getMessage());
                        apiHistoryAvailable = false;
                    }
                    catch (SocketTimeoutException e) {
                        Timber.e("API getHistory - Socket Timeout:  " + e.getMessage());
                        apiHistoryAvailable = false;
                    }

                    StringBuilder historyBuilder = new StringBuilder();

                    if (history != null) {
                        for (HistoricalQuote it : history) {
                            historyBuilder.append(it.getDate().getTimeInMillis());
                            historyBuilder.append(", ");
                            historyBuilder.append(it.getClose());
                            historyBuilder.append("\n");
                        }
                    }

                    ContentValues quoteCV = new ContentValues();
                    quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                    quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                    quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                    quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                    quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
                    quoteCVs.add(quoteCV);
                } else {
                    // Remove the invalid stock from the list.
                    PrefUtils.removeStock(context, symbol);
                    Timber.d("getQuotes() Stock " + symbol + " is not available " +
                                     "********");
                    PrefUtils.addInvalidStock(context, symbol);
                }
            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");
        JobInfo.Builder builder = new
                JobInfo.Builder(PERIODIC_ID,
                new ComponentName(context,
                QuoteJobService.class));

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF,
                                    JobInfo.BACKOFF_POLICY_EXPONENTIAL);

        JobScheduler scheduler = (JobScheduler) context.getSystemService(
                Context.JOB_SCHEDULER_SERVICE);

        // This is where it crashes.
        // The problem was that the services were defined in the app manifest instead
        // of the library manifest.
        scheduler.schedule(builder.build());
    }

    public static synchronized void initialize(final Context context) {
        schedulePeriodic(context);
        syncImmediately(context);
    }

    public static synchronized void syncImmediately(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {
            JobInfo.Builder builder = new
                    JobInfo.Builder(ONE_OFF_ID,
                                    new ComponentName(context,
                                                      QuoteJobService.class));

            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF,
                                        JobInfo.BACKOFF_POLICY_EXPONENTIAL);

            JobScheduler scheduler = (JobScheduler)
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());
        }
    }
}
