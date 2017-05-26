package com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.udacity.appmakermike.stockbarchartlibrary.R;

public class StockBarChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_bar_chart);

        // Get passed intent
        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(StockBarChartConstants.ARG_PARAM_STOCK_SYMBOL)
                && intent.hasExtra(StockBarChartConstants.ARG_PARAM_STOCK_HISTORY)) {
            // Extract the passed values
            String   stock_symbol = intent.getStringExtra(StockBarChartConstants.ARG_PARAM_STOCK_SYMBOL);
            String[] hist_ser     = intent.getStringArrayExtra(StockBarChartConstants.ARG_PARAM_STOCK_HISTORY);

            // Create the new fragment instance and add it to the UI.
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment fragment = StockBarChartFragment.newInstance(stock_symbol,
                                                                  hist_ser);
            ft.replace(R.id.fragment_stock_bar_chart, fragment);
            ft.commit();
        }
    }



}
