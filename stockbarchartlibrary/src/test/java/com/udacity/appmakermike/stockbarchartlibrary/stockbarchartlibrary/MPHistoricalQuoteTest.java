package com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary;

import android.annotation.SuppressLint;

import com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.MPHistoricalQuote;

import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.udacity.appmakermike.stockbarchartlibrary.stockbarchartlibrary.StockBarChartConstants.SERIAL_DATE_FORMAT;
import static org.junit.Assert.assertEquals;

/**
 * Created by MPongrac on 03/05/2017.
 * This test class is designed to ensure that integrity is maintained.
 */

public class MPHistoricalQuoteTest {

    private DateFormat df;
    private final String inDate = "2015-04-22";

    @SuppressLint("SimpleDateFormat")
    @Before
    public void Setup(){
       df  = new SimpleDateFormat(SERIAL_DATE_FORMAT);
    }

    // This test is here just to ensure that everything works.
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void convertString2Calendar_Format_DDMMYYYY_Dashes_OK () throws Exception {

//        Perform the test.
        Calendar calendar = MPHistoricalQuote.convertString2Calendar(inDate);

//        Verify the results.
        assertEquals(inDate, df.format(calendar != null ? calendar.getTime() : null));
    }

    @Test
    public void convertCalendar2String_Format_DDMMYYYY_Dashes_OK () throws Exception {
//        Create the calendar to start.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Date.valueOf(inDate));

//        Perform the test.
        String outDate = MPHistoricalQuote.convertCalendar2String(calendar);

//        Verify the results.
        assertEquals(inDate, outDate);
    }


}
