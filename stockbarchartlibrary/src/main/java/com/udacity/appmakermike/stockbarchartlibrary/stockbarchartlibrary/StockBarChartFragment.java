package com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary;
/*
 As can be seen in the imports section below, this project makes use of the
 suggested libraries (YahooFinance and MPAndroidChart );
 */

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.udacity.appmakermike.stockbarchartlibrary.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

import timber.log.Timber;
import yahoofinance.histquotes.HistoricalQuote;

import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.MPHistoricalQuote.convertCalendar2String;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.ARG_PARAM_STOCK_HISTORY;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.ARG_PARAM_STOCK_SYMBOL;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.DATA_FONT_SIZE;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.DESCRIPTION_FONT_SIZE;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.LEGEND_FONT_SIZE;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.NULL_FLOAT_VALUE;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.VISIBLE_X_RANGE;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.X_AXIS_FONT_SIZE;
import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.Y_AXIS_FONT_SIZE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link StockBarChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StockBarChartFragment extends Fragment {

    private String   mParamStockSymbol;
    private String[] mSerParamHistory;
    private BarChart mBarChart;
    private BarData  mBarData;

    public StockBarChartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param stock_symbol Parameter 1.
     * @param history      Parameter 2.
     * @return A new instance of fragment StockBarChartFragment.
     */
    public static StockBarChartFragment newInstance(String stock_symbol,
                                                    String[] history) {
        StockBarChartFragment fragment = new StockBarChartFragment();
        Bundle                args     = new Bundle();
        args.putString(ARG_PARAM_STOCK_SYMBOL, stock_symbol);
        args.putSerializable(ARG_PARAM_STOCK_HISTORY, history);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_stock_bar_graph, container, false);

        TextView mTvStockSymbol = (TextView) v.findViewById(R.id.tvStockSymbol);

        mTvStockSymbol.setText(mParamStockSymbol);

        mBarChart = (BarChart) v.findViewById(R.id.bar_chart);
        mBarChart.setData(mBarData);
        customizeBarChart();

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamStockSymbol = getArguments().getString(ARG_PARAM_STOCK_SYMBOL);
            if (getArguments()
                    .getSerializable(ARG_PARAM_STOCK_HISTORY) != null) {
                mSerParamHistory = getArguments()
                        .getStringArray(ARG_PARAM_STOCK_HISTORY);
                Timber.d("History set... ");
            }
            if (mSerParamHistory != null) {
                loadSerHistoricalDataArrays(mSerParamHistory);
                Timber.d("History loaded... ");
            }
        }
    }

    /***
     * Load the serialized historical data provided via intent into the graph.
     * @param hist The historical data provided by the intent.
     */
    private void loadSerHistoricalDataArrays(String[] hist) {
        Timber.d("loadSerHistoricalDataArrays: ");
        if (hist == null) {
            Timber.d("loadSerHistoricalDataArrays - *** hist is null ***");
            return;
        }
        if (hist.length <= 0) {
            Timber.d("loadSerHistoricalDataArrays - *** hist is empty ***");
            return;
        }

        ArrayList<BarDataSet> dataSets;
        @SuppressWarnings("Convert2Diamond")
        ArrayList<BarEntry> valueSetOpen = new ArrayList<BarEntry>();
        @SuppressWarnings("Convert2Diamond")
        ArrayList<BarEntry> valueSetClose = new ArrayList<BarEntry>();
        @SuppressWarnings("Convert2Diamond")
        ArrayList<BarEntry> valueSetHigh = new ArrayList<BarEntry>();
        @SuppressWarnings("Convert2Diamond")
        ArrayList<BarEntry> valueSetLow = new ArrayList<BarEntry>();
        @SuppressWarnings("Convert2Diamond")
        ArrayList<String> xAxis = new ArrayList<String>();

        int max = hist.length;

        for (int i = 0; i < max; i++) {
            try {
                HistoricalQuote hq = MPHistoricalQuote.deserializeHistoricalQuote(hist[i]);

                xAxis.add(convertCalendar2String(hq.getDate()));

                BarEntry barEntryOpen =
                        new BarEntry(convertString2Float(hq.getOpen().toString()), i);
                valueSetOpen.add(barEntryOpen);

                BarEntry barEntryClose =
                        new BarEntry(convertString2Float(hq.getClose().toString()), i);
                valueSetClose.add(barEntryClose);

                BarEntry barEntryHigh =
                        new BarEntry(convertString2Float(hq.getHigh().toString()), i);
                valueSetHigh.add(barEntryHigh);

                BarEntry barEntryLow =
                        new BarEntry(convertString2Float(hq.getLow().toString()), i);
                valueSetLow.add(barEntryLow);

                Timber.d(" Date:  " + convertCalendar2String(hq.getDate()));
                Timber.d(" Close: " + hq.getClose());
                Timber.d(" High:  " + hq.getHigh());
                Timber.d(" Low:   " + hq.getLow());
                Timber.d(" Open:  " + hq.getOpen());
            }
            catch (NoSuchMethodError | Exception e) {
                e.printStackTrace();
                Timber.e("Error extracting historical data:\n" + e.getMessage());
            }
        }

        BarDataSet barDataSetOpen  = new BarDataSet(valueSetOpen, getString(R.string.data_set_open));
        BarDataSet barDataSetLow   = new BarDataSet(valueSetLow, getString(R.string.data_set_low));
        BarDataSet barDataSetHigh  = new BarDataSet(valueSetHigh, getString(R.string.data_set_high));
        BarDataSet barDataSetClose = new BarDataSet(valueSetClose, getString(R.string.data_set_close));

        barDataSetOpen.setColor(Color.GREEN);
        barDataSetLow.setColor(Color.RED);
        barDataSetHigh.setColor(Color.CYAN);
        barDataSetClose.setColor(Color.BLUE);

        dataSets = new ArrayList<>();
        dataSets.add(barDataSetOpen);
        dataSets.add(barDataSetLow);
        dataSets.add(barDataSetHigh);
        dataSets.add(barDataSetClose);

        if (valueSetOpen.size() > 0) {
            mBarData = new BarData(xAxis, dataSets);
        }
    }

    /***
     * This is a utility designed to ensure that a valid float value is always returned.
     * @param strVal The value to be converted.
     * @return Either a zero value or a true float value.
     */
    private Float convertString2Float(String strVal) {
        if (strVal == null) {
            return NULL_FLOAT_VALUE;
        }
        return Float.parseFloat(strVal);
    }

    /**
     * This is where the bar chart appearance and
     * functionality are configured.
     */
    private void customizeBarChart() {
        // Establish standard, fixed text values
        mBarChart.setContentDescription(getString(R.string.bar_chart_content_description));
        mBarChart.setNoDataText(getString(R.string.bar_chart_no_data_msg));
        mBarChart.setDescription(getString(R.string.bar_chart_description));
        mBarChart.setDescriptionTextSize(DESCRIPTION_FONT_SIZE);

        // Enable chart features.
        mBarChart.setTouchEnabled(true);
        mBarChart.setDragEnabled(true);
        mBarChart.setScaleEnabled(true);
        mBarChart.setDrawGridBackground(true);

        // Enable pinch zoom to avoid scaling x and y axis separately.
        mBarChart.setPinchZoom(true);

        // Alternative background color
        mBarChart.setBackgroundColor(Color.DKGRAY);

        // Get legend object.
        Legend l = mBarChart.getLegend();

        // Customize legend
        l.setForm(Legend.LegendForm.SQUARE);
        l.setTextColor(Color.WHITE);
        l.setTextSize(LEGEND_FONT_SIZE);

        XAxis x1 = mBarChart.getXAxis();
        x1.setTextColor(Color.WHITE);
        x1.setDrawGridLines(false);
        x1.setAvoidFirstLastClipping(true);
        x1.setTextSize(X_AXIS_FONT_SIZE);

        YAxis y1 = mBarChart.getAxisLeft();
        y1.setTextColor(Color.WHITE);
        y1.setDrawGridLines(true);
        y1.setTextSize(Y_AXIS_FONT_SIZE);

        YAxis y12 = mBarChart.getAxisRight();
        y12.setEnabled(false);

        // Set the data value text size.
        mBarData = mBarChart.getData();
        if (mBarData != null) {
            mBarData.setValueTextSize(DATA_FONT_SIZE);

            // Limit number of visible entries.
            mBarChart.setVisibleXRange(VISIBLE_X_RANGE);

            // Scroll to the last entry.
            mBarChart.moveViewToX(mBarData.getXValCount() - (VISIBLE_X_RANGE));
        }

        // Enable chart data change notifications.
        mBarChart.notifyDataSetChanged();
    }

}
