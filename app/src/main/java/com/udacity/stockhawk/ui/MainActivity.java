package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.udacity.appmakermike.stockhawklibrary.stockhawklibrary.data.PrefUtils;
import com.udacity.appmakermike.stockhawklibrary.stockhawklibrary.sync.QuoteSyncJob;
import com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartActivity;
import com.udacity.stockhawk.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.MPHistoricalQuote.serializeStringArrayHistoricalQuoteList;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.ARG_PARAM_STOCK_HISTORY;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.ARG_PARAM_STOCK_SYMBOL;
import static com.udacity.appmakermike.stockhawklibrary.stockhawklibrary.data.Contract.Quote;

@SuppressWarnings("DanglingJavadoc")
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
                                                               SwipeRefreshLayout.OnRefreshListener,
                                                               StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;
    @SuppressWarnings({"WeakerAccess", "unused", "CanBeFinal"})
    @BindView(R.id.recycler_view)
    RecyclerView       stockRecyclerView;
    @SuppressWarnings({"WeakerAccess", "unused", "CanBeFinal"})
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressWarnings({"WeakerAccess", "unused", "CanBeFinal"})
    @BindView(R.id.error)
    TextView           error;
    private StockAdapter adapter;

    // This is the standard format that we receive from YahooFinance.
    private List<HistoricalQuote> mParamHistory;

    private Calendar from;
    private Calendar to;
    private Interval myInterval;
    @SuppressWarnings({"Convert2Diamond", "CanBeFinal"})
    private Map<String, Stock>                 stocks    = new HashMap<String, Stock>();
    @SuppressWarnings({"Convert2Diamond", "CanBeFinal"})
    private Map<String, List<HistoricalQuote>> histories = new HashMap<String, List<HistoricalQuote>>();
    private String currStockSymbol;

    // This is for the about functionality.
    private static String mLang;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static String mCountry;

    // These are for the popup functionality.
    private        android.support.v4.widget.SwipeRefreshLayout main_layout;
    private static PopupWindow                                  popupWindow;
    private static DisplayMetrics                               metrics;
    private static String                                       mPopupMsg;

    private static Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appContext = getApplicationContext();

        setContentView(R.layout.activity_main);

        // Added logic to implement missing functionality.
        mLang = Locale.getDefault().getLanguage();
        mCountry = Locale.getDefault().getCountry();
        getMetrics();

        ButterKnife.bind(this);

        adapter = new StockAdapter(appContext, this);
        stockRecyclerView.setAdapter(adapter);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);

        from = Calendar.getInstance();
        to = Calendar.getInstance();
        from.add(Calendar.YEAR, -2); // from 2 years ago

//        myInterval = Interval.DAILY;
//        myInterval = Interval.WEEKLY;
        myInterval = Interval.MONTHLY;

        stocks.clear();

        onRefresh();

        QuoteSyncJob.initialize(appContext);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                // Remove the given stock from the shared preferences instance.
                PrefUtils.removeStock(appContext, symbol);
                PrefUtils.addInvalidStock(appContext, symbol);
                // This removes all data, for the given stock, from the database.
                getContentResolver().delete(Quote.makeUriForStock(symbol), null, null);
                // Now, force an update of the UI and remaining data.
                QuoteSyncJob.syncImmediately(appContext);
//                onRefresh();
            }
        }).attachToRecyclerView(stockRecyclerView);
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Bundle savedInstanceState = new Bundle();
//    }

    @Override
    public void onRefresh() {

        swipeRefreshLayout.setRefreshing(true);
        QuoteSyncJob.syncImmediately(appContext);

        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(swipeRefreshLayout, R.string.snackbar_no_connectivity,
                          Snackbar.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(appContext).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);
        } else if (PrefUtils.getInvalidStocks(appContext).size() > 0
                && PrefUtils.isStockInvalid(appContext, currStockSymbol, true)) {
            String msg = getString(R.string.stock_not_available).replace
                    ("%s", currStockSymbol);
            Timber.d("onRefresh() " + msg);
            error.setText(msg);
            PrefUtils.removeStock(appContext, currStockSymbol);
            currStockSymbol = "";
            error.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
            // Unfortunately, the error control does not get displayed.
//        } else if (!QuoteSyncJob.syncSuccessful) {
//            Timber.d("onRefresh() " + getText(R.string.api_history_not_available));
//            swipeRefreshLayout.setRefreshing(false);
//            error.setText(getText(R.string.api_history_not_available));
//            error.setVisibility(View.VISIBLE);
////            makeAppWait(5 * 1000); // 5 seconds
//            error.setVisibility(View.GONE);
        } else {
            error.setVisibility(View.GONE);
        }
    }


    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {
            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                displayPopupMessage(
                        getString(R.string.no_network_connection_available) + " \r\n" +
                        getString(R.string.snackbar_stock_added_no_connectivity, symbol));
            }
            PrefUtils.addStock(appContext, symbol);
            QuoteSyncJob.syncImmediately(appContext);
            currStockSymbol = symbol;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(appContext,
                                Quote.URI,
                                Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                                null, null, Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);

        if (data.getCount() != 0) {
            error.setVisibility(View.GONE);
        }
        adapter.setCursor(data);

        if (currStockSymbol != null && !currStockSymbol.isEmpty()) {
            boolean matched = false;
            // Search for the current stock symbol in the provided data.
            if (data.getCount() > 0) {
                data.moveToFirst();
                if (data.getString(Quote.POSITION_SYMBOL).matches(currStockSymbol)) {
                    matched = true;
                }
                while (data.moveToNext() && !matched) {
                    if (data.getString(Quote.POSITION_SYMBOL).matches(currStockSymbol)) {
                        matched = true;
                    }
                }
            }
            Timber.d("onLoadFinished - Searching data for " + currStockSymbol + "  " +
                             "matched: " + matched);
            if (!matched) {
                // It is not in the data, it should be in the invalid stock list.
                if (PrefUtils.isStockInvalid(appContext, currStockSymbol, true)) {
                    PrefUtils.removeStock(appContext, currStockSymbol);
                    displayPopupMessage(
                            getString(R.string.stock_not_available, currStockSymbol));
                }
            } else {
                // Ensure that it is in the list, if we found it in the data.
                if (!PrefUtils.getStocks(appContext).contains(currStockSymbol)) {
                    PrefUtils.addStock(appContext, currStockSymbol);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }

    /**
     * ##########################################################################
     *
     * Functionality added to meet requirements.
     *
     * ##########################################################################
     */

    /**
     * This is the standard onClick override functionality.
     *
     * @param symbol which was clicked.
     */
    @Override
    public void onClick(String symbol) {
        // This is here just in case the API service decides to stop providing data again.
//        if (showStockHistoryThread != null && showStockHistoryThread.isAlive()) {
//            //noinspection deprecation
//            showStockHistoryThread.stop();
//        }
        currStockSymbol = symbol;
        Thread showStockHistoryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                getSingleStockHistory();
                runOnUiThread(displayBarChartGraph);
            }
        });
        showStockHistoryThread.start();
    }

    /**
     * This method is used to obtain the historical data for the provided stock symbol.
     */
    private void getSingleStockHistory() {

        //noinspection UnusedAssignment
        Stock currStock = null;
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
            Timber.d("Symbol:     " + currStock.getSymbol());
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
            Timber.e("getSingleStockHistory: currStock is null.");
        }

    }

    /**
     * This is the runnable instance used to display a bar chart graph.
     */
    private final Runnable displayBarChartGraph = new Runnable() {
        @Override
        public void run() {
            Intent intentShowBarGraph = new Intent(
                    appContext,
                    StockBarChartActivity.class
            );
            intentShowBarGraph.putExtra(ARG_PARAM_STOCK_SYMBOL, currStockSymbol);

            String[] serializedSAHistory =
                    serializeStringArrayHistoricalQuoteList(mParamHistory);

            intentShowBarGraph.putExtra(ARG_PARAM_STOCK_HISTORY, serializedSAHistory);

            try {
                startActivity(intentShowBarGraph);
            }
            catch (Exception e) {
                e.printStackTrace();
                Timber.d("ERROR: onClickShowGraph(startActivity) - " + e.getMessage());
            }
        }
    };

    /***
     * ###################################################
     *          Main Menu Methods
     * ###################################################
     */

    /***
     * This is a standard menu method.
     *
     * @param menu Android provides this parameter based upon the view definition.
     * @return status
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    /***
     * This is a standard menu method.
     *
     * @param item The specific menu item that was selected.
     * @return status
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_change_units:
                PrefUtils.toggleDisplayMode(this);
                setDisplayModeMenuItemIcon(item);
                adapter.notifyDataSetChanged();
                return true;
            case R.id.action_about:
                return true;
            default:
                // Do nothing here.
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method switches between $ and % values.
     *
     * @param item that was clicked upon.
     */
    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }


    /***
     * ###################################################
     *          Main Menu Items
     * ###################################################
     */

    /***
     * Tell the user about the app.
     * <p>
     * @param item The selected menu item
     */
    public void showAbout(@SuppressWarnings("UnusedParameters") MenuItem item) {
        Intent intentAbout = new Intent(this, AboutActivity.class);

        // This is to ensure that we have a valid default in any case.
        String aboutPage = getString(R.string.def_about_page);


        // This is to see if there is an asset file associated with the default locale.
        if (!mLang.equals(getString(R.string.default_language)) &&
                doesAssetExist(getString(R.string.about_asset_base) +
                                       mLang + getString(R.string.html_extension))) {
            aboutPage = getString(R.string.asset_file_base) +
                    getString(R.string.about_asset_base) + mLang +
                    getString(R.string.html_extension);
        }

        intentAbout.putExtra(Intent.EXTRA_TEXT, aboutPage);

        if (intentAbout.resolveActivity(getPackageManager()) != null) {
            startActivity(intentAbout);
        } else {
            Timber.e("showAbout: The intent cannot be resolved.");
        }
    }

    /***
     * This is just a simple helper method to see if an asset exists or not.
     *
     * @param pathInAssetsDir The asset path and filename to be verified.
     * @return result
     */
    private boolean doesAssetExist(String pathInAssetsDir) {
        boolean result = false;

        try {
            result = Arrays.asList(this.getResources().getAssets().list("")).contains(pathInAssetsDir);
        }
        catch (IOException e) {
            e.printStackTrace();
            Timber.e("doesAssetExist: " + e.getMessage());
        }

        return result;
    }


    /***
     * ###################################################
     *                  Display Settings
     * ###################################################
     */

    /***
     * A centralized method for setting the member variable with the current
     * device settings.
     */
    private void getMetrics() {
        WindowManager wm = (WindowManager)
                getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);
    }

    /***
     * ###################################################
     *          Background Thread Items
     * ###################################################
     */

    /***
     * This provides a central means of letting the user know that no network
     * connection is currently available.
     */
    private void displayPopupMessage(String message) {
        mPopupMsg = message;
        threadPopup.start();
    }

    /***
     * This thread is used to display a popup to the user that there is no
     * network connection available.
     */
    private final Thread threadPopup = new Thread(new Runnable() {
        @Override
        public void run() {
            main_layout = (android.support.v4.widget.SwipeRefreshLayout)
                    findViewById(R.id.swipe_refresh);
            LayoutInflater layoutInflater = (LayoutInflater)
                    getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            // If null is not passed, the process does not work.
            @SuppressLint("InflateParams") ViewGroup viewGroup = (ViewGroup)
                    layoutInflater.inflate(R.layout.popup_basic, null);
            if (mPopupMsg != null && !mPopupMsg.isEmpty()) {
                TextView tvPopupMsg = (TextView) viewGroup.findViewById(R.id.tv_popup_msg);
                tvPopupMsg.setText(mPopupMsg);
            }
            // Make it 2/3 of the width and 1/3 of the height.
            int width  = (metrics.widthPixels / 3) * 2;
            int height = metrics.heightPixels / 3;
            popupWindow = new PopupWindow(viewGroup, width, height, true);

            viewGroup.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // We cannot do anything, so leave the app.
                    popupWindow.dismiss();
                    //MainActivity.this.finish();
                    return true;
                }
            });

            if (Thread.currentThread().getName().compareToIgnoreCase(
                    getString(R.string.main_thread_name)) != 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Try to somewhat center it.
                        int width  = (metrics.widthPixels / 3) / 2;
                        int height = metrics.heightPixels / 3;
                        popupWindow.showAtLocation(main_layout, Gravity.NO_GRAVITY,
                                                   width, height);
                    }
                });
            } else {
                // Try to somewhat center it.
                width = (metrics.widthPixels / 3) / 2;
                popupWindow.showAtLocation(main_layout, Gravity.NO_GRAVITY, width, height);
            }
        }
    });


}
