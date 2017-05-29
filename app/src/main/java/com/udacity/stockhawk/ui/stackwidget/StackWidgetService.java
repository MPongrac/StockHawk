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

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.udacity.appmakermike.stockhawklibrary.stockhawklibrary.data.Contract;
import com.udacity.appmakermike.stockhawklibrary.stockhawklibrary.data.PrefUtils;
import com.udacity.stockhawk.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class StackWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        // Create cursor here.
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private String[] mSymbols;
    private String[] mSigns;
    private String[] mPrices;
    private String[] mChanges;

    private List<String> mListSymbols;
    private List<String> mListSigns;
    private List<String> mListPrices;
    private List<String> mListChanges;

    private static int mCount;

    @SuppressWarnings("CanBeFinal")
    private Context                  mContext;
    @SuppressWarnings({"CanBeFinal", "FieldCanBeLocal", "unused"})
    private int                      mAppWidgetId;
    @SuppressWarnings("CanBeFinal")
    private Map<String, WidgetItem>  mExistingWidgets;
    @SuppressWarnings("CanBeFinal")
    private Map<Integer, WidgetItem> mExistingWidgetPositions;

    private Cursor mCursor;

    /**
     * This is the standard, default RemoteViewsFactory method.
     *
     * @param context currently being used.
     * @param intent  being applied or used.
     */
    @SuppressLint("UseSparseArrays")
    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context.getApplicationContext();
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                          AppWidgetManager.INVALID_APPWIDGET_ID);

        mExistingWidgets = new HashMap<>();
        mExistingWidgetPositions = new HashMap<>();

        mSymbols = new String[0];
        mSigns = new String[0];
        mPrices = new String[0];
        mChanges = new String[0];

        mListSymbols = new ArrayList<String>();
        mListSigns = new ArrayList<String>();
        mListPrices = new ArrayList<String>();
        mListChanges = new ArrayList<String>();
    }

    /**
     * This is the standard, default onCreate method.
     */
    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        removeExistingWidgetMaps();
        createWidgetItems();

        // We sleep for 3 seconds here to show how the empty view appears in the interim.
        // The empty view is set in the StackWidgetProvider and should be a sibling of the
        // collection view.
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void removeExistingWidgetMaps(){
        if (mExistingWidgets != null) {
            mExistingWidgets.clear();
        }
        if (mExistingWidgetPositions != null) {
            mExistingWidgetPositions.clear();
        }
        resetDataLists();
    }

    private void resetDataLists() {
        if (mListSymbols != null){
            mListSymbols.clear();
        }
        if (mListSigns != null){
            mListSigns.clear();
        }
        if (mListPrices != null){
            mListPrices.clear();
        }
        if (mListChanges != null){
            mListChanges.clear();
        }
    }

    /**
     * This is a standard createWidgetItems method.
     * <p>
     * Here is where we maintain arrays and maps of data and widget item instances.
     */
    private void createWidgetItems() {
        String  symbol;
        String  sign;
        String  price;
        String  change;
        Integer position = -1;

        Set<String> stockPref  = PrefUtils.getStocks(mContext);
        int         prefSize   = stockPref.size();
        String[]    stockArray = stockPref.toArray(new String[prefSize]);

        int newMax = mCount;

        if (prefSize == mPrices.length && mCount == prefSize) {
            processBalancedLists(position, stockArray, newMax);
        } else if (mPrices.length > prefSize) {
            removeDataFromDeletedStock(position, prefSize, stockArray);
        } else if (mPrices.length < prefSize) {
            // We just added a valid stock to the list, but we do not have data for it
            // yet.
            Timber.d("The two arrays do not match in size.  "+
                             "We just added a valid stock to the list, " +
                             "but we do not have data for it yet.  " +
                             "mPrices.length: " + mPrices.length +
                             " prefSize: " + prefSize +
                             " mCount: " + mCount
            );

        } else {

            Timber.e("The two arrays do not match in size.  mPrices.length: " +
                             mPrices.length + " prefSize: " + prefSize +
                             " mCount: " + mCount
            );
        }
    }

    private void removeDataFromDeletedStock(Integer position, int prefSize, String[] stockArray) {
        // Make sure that there is something to process.
        if (position == -1) {
            return;
        }
        String symbol;// We just removed a valid stock from the list.
        String prefSymbol;// We just removed a valid stock from the list.
        // That means that we have data for a stock that should not be there.
        Timber.d("The two arrays do not match in size.  \n"+
                         "We just removed a valid stock from the list.  \n" +
                         "That means that we have data for a stock that " +
                         "should not be there.  \n" +
                         "mPrices.length: " + mPrices.length +
                         " prefSize: " + prefSize +
                         " mCount: " + mCount
        );
        prefSymbol = stockArray[position];
        Integer key_del = null;
        for (int i = 0; i < mListSymbols.size(); i++) {
            symbol = mListSymbols.get(i);

            if (mListSymbols.contains(symbol) &&
                    symbol.compareToIgnoreCase(prefSymbol) == 0) {
                key_del = i;
                Timber.d("Removing " + i + " " + symbol +
                                 " position: " + position + " prefSymbol: " + prefSymbol);
                mExistingWidgetPositions.remove(key_del);
                mExistingWidgets.remove(symbol);
                removeDataArrayEntriesAtIndex(i);
            }
        }
        if (key_del != null){
            processBalancedLists(position, stockArray, mCount);
        }
    }

    private void removeDataFromDeletedStock2(Integer position, int prefSize, String[]
            stockArray) {
        String symbol;// We just removed a valid stock from the list.
        // That means that we have data for a stock that should not be there.
        Timber.d("The two arrays do not match in size.  \n"+
                         "We just removed a valid stock from the list.  \n" +
                         "That means that we have data for a stock that " +
                         "should not be there.  \n" +
                         "mPrices.length: " + mPrices.length +
                         " prefSize: " + prefSize +
                         " mCount: " + mCount
        );
        Integer key_del = null;
        Integer index_del = null;
        for (int i = 0; i < mSymbols.length; i++) {
            symbol = mSymbols[i];
            if (mExistingWidgets.containsKey(symbol) &&
                    mExistingWidgetPositions != null &&
                    !mExistingWidgetPositions.isEmpty()) {
                WidgetItem widgetItem = mExistingWidgets.get(symbol);
                if (widgetItem != null) {
                    if (mExistingWidgetPositions.containsValue(widgetItem)) {
                        for (Integer key : mExistingWidgetPositions.keySet()) {
                            if (mExistingWidgetPositions.get(key).symbol.compareTo
                                    (symbol) == 0) {
                                key_del = key;
                                index_del = i;
                            }
                        }
                    }
                }

            }
        }
        if (key_del != null){
            String symbol_del = mExistingWidgetPositions.get(key_del).symbol;
            Timber.d("Removing " + symbol_del);
            mExistingWidgetPositions.remove(key_del);
            mExistingWidgets.remove(symbol_del);
            removeDataArrayEntriesAtIndex(index_del);
            processBalancedLists(position, stockArray, mCount);
        }
    }

    private void processBalancedLists(Integer position, String[] stockArray, int newMax) {
        String symbol;
        String price;
        String sign;
        String change;

        for (int ii = 0; ii < mCount; ii++) {
            int i     = -1;
            int match = -1;
            while (i < mCount - 1 && i < stockArray.length - 1
                    && ii < mListSymbols.size() - 1 && -1 == match) {
                i++;
                if (mListSymbols.get(ii).compareToIgnoreCase(stockArray[i]) == 0) {
                    match = i;
                }
            }
            // The stock was added to the list, but not found in the system.
            if (-1 == match) {
                newMax--;
                continue;
            }
            if (match >= newMax) {
                match = newMax - 1;
            }
            i = match;
            if (mListPrices.size() > 0) {
                symbol = mListSymbols.get(i);
                price = mListPrices.get(i);
                sign = mListSigns.get(i);
                change = sign + mListChanges.get(i);
            } else {
                // This is just dummy data to fill the initial layout, number formatting, and
                // color coding.  The real data will be loaded via the onDataSetChanged()
                // method.
                symbol = stockArray[i];
                price = "" + i;
                sign = (i % 2 > 0 ? "-" : "+");
                change = sign + (i);
            }

            if (!mExistingWidgets.containsKey(symbol)) {
                WidgetItem widgetItem = new WidgetItem(symbol, price, change);
                mExistingWidgets.put(symbol, widgetItem);
                mExistingWidgetPositions.put(position, widgetItem);
                position++;
            }
        }
    }

    private void removeDataArrayEntriesAtIndex(Integer index_to_del) {
        String[] Symbols;
        String[] Signs;
        String[] Prices;
        String[] Changes;

        Symbols = mSymbols.clone();
        Signs = mSigns.clone();
        Prices = mPrices.clone();
        Changes = mChanges.clone();

        Integer array_size = Symbols.length;
        Integer new_array_size = array_size - 1;

        for (int i = 0; i < array_size; i++) {
            if (i >= index_to_del) {
                if (i > index_to_del) {
                    mSymbols[i-1] = Symbols[i];
                    mSigns[i-1] = Signs[i];
                    mPrices[i-1] = Prices[i];
                    mChanges[i-1] = Changes[i];
                }
                // We want to ignore the index_to_del entry.
            } else {
                mSymbols[i] = Symbols[i];
                mSigns[i] = Signs[i];
                mPrices[i] = Prices[i];
                mChanges[i] = Changes[i];
            }
        }
        mSymbols[new_array_size - 1] = null;
        mSigns[new_array_size - 1] = null;
        mPrices[new_array_size - 1] = null;
        mChanges[new_array_size - 1] = null;

        mCount = new_array_size;
    }

    /**
     * This is the standard, default onDestroy method.
     * <p>
     * It is used to clean up any elements before quitting the app widget.
     */
    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mExistingWidgets.clear();
        mExistingWidgetPositions.clear();

        mSymbols = null;
        mSigns = null;
        mPrices = null;
        mChanges = null;

        mListSymbols.clear();
        mListSigns.clear();
        mListPrices.clear();
        mListChanges.clear();

        mListSymbols = null;
        mListSigns = null;
        mListPrices = null;
        mListChanges = null;
    }

    /**
     * This is the standard, default getCount method.
     *
     * @return the current widget item count.
     */
    public int getCount() {
        return mCount;
    }

    /**
     * This is the standard, default getViewAt method.
     * <p>
     * It creates widget item elements and fills them with data, adds click
     * functionality, and adds extra details to an intent for future use.
     *
     * @param position being requested.
     * @return the RemoteViews object containing the requested positioned view.
     */
    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);

        try {
//            WidgetItem widgetItem = mExistingWidgetPositions.get(position);
//            if (widgetItem != null) {
//                rv.setTextViewText(R.id.widget_item_symbol, widgetItem.symbol);
//                rv.setTextViewText(R.id.widget_item_price, widgetItem.currPrice);
//                rv.setTextViewText(R.id.widget_item_price_change, widgetItem.currPriceChange);
//                rv.setTextColor(R.id.widget_item_price_change, widgetItem.currChangeColor);
//
//                // Next, we set a fill-intent which will be used to fill-in the pending intent template
//                // which is set on the collection view in StackWidgetProvider.
//                Bundle extras = new Bundle();
//                extras.putInt(StackWidgetProvider.EXTRA_ITEM, position);
//                Intent fillInIntent = new Intent();
//                fillInIntent.putExtras(extras);
//                rv.setOnClickFillInIntent(R.id.widget_item_symbol, fillInIntent);
//            } else {
//                // ?Remove the remote view?
//                Timber.d("Remove widget item " + position
//                                 + (mExistingWidgetPositions.get(position) ==
//                                       null ? " " + mListSymbols.get(position) :
//                                       " " + mExistingWidgetPositions.get(position).symbol)
//                                 + " from remote view?");
//                Timber.d(mExistingWidgetPositions.toString());
//            }

            String currPrice = mListPrices.get(position);
            if (currPrice != null && !currPrice.isEmpty()) {
                String sign = mListSigns.get(position);
                String change = sign + mListChanges.get(position);
                rv.setTextViewText(R.id.widget_item_symbol, mListSymbols.get(position));
                rv.setTextViewText(R.id.widget_item_price, currPrice);
                rv.setTextViewText(R.id.widget_item_price_change, change);
                rv.setTextColor(R.id.widget_item_price_change,
                                WidgetItem.getChangeColor(change));

                // Next, we set a fill-intent which will be used to fill-in the pending intent template
                // which is set on the collection view in StackWidgetProvider.
                Bundle extras = new Bundle();
                extras.putInt(StackWidgetProvider.EXTRA_ITEM, position);
                Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                rv.setOnClickFillInIntent(R.id.widget_item_symbol, fillInIntent);
            } else {
                // ?Remove the remote view?
                Timber.d("Remove widget item " + position
                                 + (mExistingWidgetPositions.get(position) ==
                                            null ? " " + mListSymbols.get(position) :
                                    " " + mExistingWidgetPositions.get(position).symbol)
                                 + " from remote view?");
                Timber.d(mExistingWidgetPositions.toString());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Timber.e("getViewAt [" + position + "] " + e.getMessage());
            rv = null;
        }

        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.
        try {
//            Timber.d("Loading view " + position);
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Return the remote views object.
        return rv;
    }

    /**
     * This is the standard, default getLoadingView method.
     *
     * @return null always.
     */
    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.)
        // If you return null here, you will get the default loading view.
        return null;
    }

    /**
     * This is the standard, default getViewTypeCount method.
     *
     * @return 1 always.
     */
    public int getViewTypeCount() {
        return 1;
    }

    /**
     * This is the standard, default getItemId method.
     *
     * @param position The position of the item.
     * @return the provided position value.
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * This is the standard, default hasStableIds method.
     *
     * @return Always returns true.
     */
    public boolean hasStableIds() {
        return true;
    }

    /**
     * This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
     * on the collection view corresponding to this factory. You can do heaving lifting in
     * here, synchronously. For example, if you need to process an image, fetch something
     * from the network, etc., it is ok to do it here, synchronously. The widget will
     * remain in its current state while work is being done here, so you don't need to
     * worry about locking up the widget.
     * <p>
     * It fills local arrays with data retrieved from the provider and then creates the
     * associated widget items.
     */
    public void onDataSetChanged() {
        Timber.d("onDataSetChanged...");
        getStockListDetails();
        // If we have price information, then we have valid data.
        if (mPrices.length > 0) {
            createWidgetItems();
        }
    }

    /**
     * This method gets the list of stock details from the data provider and fills
     * local arrays with the results.
     */
    private void getStockListDetails() {

        if (mCursor == null) {
            mCursor = mContext.getContentResolver().query(Contract.Quote.URI
                    , null, null, null, Contract.Quote._ID);
        }

        if (mCursor == null) {
            // Since this is a service for a widget and there is no layout available,
            // I cannot use a Snackbar in this instance.
            Toast.makeText(mContext, R.string.no_data_cursor,
                           Toast.LENGTH_LONG).show();

            return;
        }

        resetDataLists();

        int cursorCnt = mCursor.getCount();

        if (cursorCnt > 0) {
            mSymbols = new String[cursorCnt];
            mPrices = new String[cursorCnt];
            mChanges = new String[cursorCnt];
            mSigns = new String[cursorCnt];

            for (int i = 0; i < cursorCnt; i++) {
                mCursor.moveToPosition(i);
                mSymbols[i] = mCursor.getString(Contract.Quote.POSITION_SYMBOL);

                // Get the new values from the system.
                mPrices[i] = mCursor.getString(Contract.Quote.POSITION_PRICE);

                // Now, calculate the new values.
                mChanges[i] = mCursor.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

                // Set the specific change sign values.
                mSigns[i] = mContext.getString(R.string.plus_sign);
                if (mChanges[i].startsWith(mContext.getString(R.string.minus_sign))) {
                    mSigns[i] = mContext.getString(R.string.empty_sign);
                }
                mListSymbols.add(i, mSymbols[i]);
                mListSigns.add(i, mSigns[i]);
                mListPrices.add(i, mPrices[i]);
                mListChanges.add(i, mChanges[i]);
            }

            if (mCount != cursorCnt){
                mCount = cursorCnt;
            }
            Timber.d("New stock count: " + mCount + " old: " + cursorCnt);

        }
    }
}