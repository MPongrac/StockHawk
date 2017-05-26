package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.udacity.stockhawk.R;

import timber.log.Timber;

/***
 * 3. Attribution
 * <p>
 * You shall use the TMDb logo to identify your use of the TMDb APIs.
 * You shall place the following notice prominently on your application:
 * "This product uses the TMDb API but is not endorsed or certified by TMDb."
 * Any use of the TMDb logo in your application shall be less prominent than
 * the logo or mark that primarily describes the application and your use of the
 * TMDb logo shall not imply any endorsement by TMDb.
 * <p>
 * What are the attribution requirements?
 * <p>
 * You shall use the TMDb logo to identify your use of the TMDb APIs.
 * You shall place the following notice prominently on your application:
 * "This product uses the TMDb API but is not endorsed or certified by TMDb."
 * Any use of the TMDb logo in your application shall be less prominent than
 * the logo or mark that primarily describes the application and your use of the
 * TMDb logo shall not imply any endorsement by TMDb. When attributing TMDb,
 * the attribution must be within your application's "About" or "Credits" type section.
 * When using a TMDb logo, we require you to use one of our approved images.
 * <p>
 * Are there branding requirements?
 * Our logo should not be modified in color, aspect ratio, flipped or rotated except where otherwise noted.
 * Our logo can be white, black or the "TMDb green" that is used throughout our branding.
 * When referring to TMDb, you should use either the acronym "TMDb" or
 * the full name "The Movie Database". Any other name is not acceptable.
 * When linking back to our website, please point your link to "http://www.themoviedb.org/".
 * If you are putting our company name or logo on any merchandise or product
 * packaging please consult us beforehand for approval.
 * <p>
 * API Legal Notice
 * We do not claim ownership of any of the images or data in the API. We comply
 * with the Digital Millennium Copyright Act (DMCA) and expeditiously remove
 * infringing content when properly notified. Any data and/or images you upload
 * you expressly grant us a license to use. You are prohibited from using the
 * images and/or data in connection with libelous, defamatory, obscene,
 * pornographic, abusive or otherwise offensive content.
 */

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Timber.e("onCreate: " + e.getMessage());
        }
    }

}
