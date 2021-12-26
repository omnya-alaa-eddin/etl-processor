package com.simon.etlProcessor.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;

class DateUtilTest {

    DateUtil util = new DateUtil();
    @Test
    void testDateToString_Fail() {
        Calendar cal = Calendar.getInstance();
        String formatted = util.dateToString(null);
        assertEquals(formatted, null);

    }

    @Test
    void testDateToString_Success() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String formatted = util.dateToString(date);

        assertEquals(
                (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH) + "-" + cal.get(Calendar.YEAR),
                formatted);

    }



    @Test
    void testGetlastMonthDate() {
        String month = util.getlastMonthDate(new Date());
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        assertEquals(month,
                cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH) + "-" + cal.get(Calendar.YEAR));

    }

}
