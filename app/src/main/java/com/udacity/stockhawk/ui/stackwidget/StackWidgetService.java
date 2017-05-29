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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.udacity.appmakermike.stockhawklibrary.data.Contract;
import com.udacity.stockhawk.R;

import timber.log.Timber;

public class StackWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        // Create cursor here.
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static int          mCount;
    @SuppressWarnings("CanBeFinal")
    private Context      mContext;
    private static Cursor       mCursor;


    /**
     * This is the standard, default RemoteViewsFactory method.
     *
     * @param context currently being used.
     * @param intent  being applied or used.
     */
    public StackRemoteViewsFactory(Context context,
                                   @SuppressWarnings("UnusedParameters") Intent intent) {
        mContext = context.getApplicationContext();
    }

    /**
     * This is the standard, default onCreate method.
     */
    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        getStockListDetails();

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

    /**
     * This is the standard, default onDestroy method.
     * <p>
     * It is used to clean up any elements before quitting the app widget.
     */
    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mCursor.close();
        mCursor = null;
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
        // text based on the position from the data available in the cursor.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        try {
            // Ensure that we are processing the correct entry.
            mCursor.moveToPosition(position);

            String currPrice = mCursor.getString(Contract.Quote.POSITION_PRICE);
            // Only if we have a price, is the stock valid.
            if (currPrice != null && !currPrice.isEmpty()) {
                // Set the price.
                rv.setTextViewText(R.id.widget_item_price,
                                   WidgetItem.formatPrice(
                                   mCursor.getString(Contract.Quote.POSITION_PRICE)));

                // Set the symbol.
                rv.setTextViewText(R.id.widget_item_symbol,
                                   mCursor.getString(Contract.Quote.POSITION_SYMBOL));

                // Set the change.
                String change =
                        WidgetItem.formatPriceChange(
                                mCursor.getString(Contract.Quote
                                .POSITION_ABSOLUTE_CHANGE));
                rv.setTextViewText(R.id.widget_item_price_change, change);
                rv.setTextColor(R.id.widget_item_price_change,
                                WidgetItem.getChangeColor(change));

                // Next, we set a fill-intent which will be used to fill-in the pending
                // intent template which is set on the collection view in
                // StackWidgetProvider.
                Bundle extras = new Bundle();
                extras.putInt(StackWidgetProvider.EXTRA_ITEM, position);
                Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                rv.setOnClickFillInIntent(R.id.widget_item_symbol, fillInIntent);
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
    }

    /**
     * This method gets the list of stock details from the data provider.
     */
    private void getStockListDetails() {

        // Always replace the cursor to ensure that the new data is being used.
        mCursor = mContext.getContentResolver().query(Contract.Quote.URI
                , null, null, null, Contract.Quote._ID);

        if (mCursor == null) {
            // Since this is a service for a widget and there is no layout available,
            // I cannot use a Snackbar in this instance.
            Toast.makeText(mContext, R.string.no_data_cursor,
                           Toast.LENGTH_LONG).show();
        } else {
            mCount = mCursor.getCount();
        }

    }
}