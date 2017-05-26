package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.udacity.stockhawk.R;

import timber.log.Timber;

/**
 * A placeholder fragment containing a simple web view.
 */
public class AboutActivityFragment extends Fragment {

    /**
     * This is the standard required default constructor.
     */
    public AboutActivityFragment() {
    }

    /**
     * This method is where the current about page is loaded into a web view and
     * presented to the user.
     *
     * @param inflater currently in use.
     * @param container currently in use.
     * @param savedInstanceState currently in use.
     * @return the newly created fragment view containing the about page in a web view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        Intent intent = getActivity().getIntent();
        String currAboutPage = getString(R.string.def_about_page);
        if (intent != null) //noinspection DanglingJavadoc
        {
            /***
             * This is one method of passing information through an intent
             * to the target activity fragment.
             *
             * In this case we have used a standard Intent.EXTRA_TEXT identifier,
             * however, we could have created and used any other constant.
             */
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                currAboutPage = intent.getStringExtra(Intent.EXTRA_TEXT);
            }

        }

        WebView wv_about = (WebView) view.findViewById(R.id.wv_about);

        if (wv_about == null) {
            Timber.e("onCreateView: WebView could not be found/created!");
            Snackbar.make(container, getString(R.string.current_about_page) + " '" +
                                  currAboutPage +
                                  "' " +
                                  getString(R.string.was_not_found),
                          Snackbar.LENGTH_LONG).show();
            return view;
        }
        wv_about.getSettings().setBuiltInZoomControls(true);
        wv_about.getSettings().setLoadWithOverviewMode(true);
        wv_about.getSettings().setUseWideViewPort(true);
        if (savedInstanceState != null) {
            wv_about.restoreState(savedInstanceState);

        } else {
            wv_about.loadUrl(currAboutPage);
        }

        return view;
    }
}
