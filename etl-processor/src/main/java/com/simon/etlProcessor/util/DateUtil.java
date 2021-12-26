package com.simon.etlProcessor.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static String dateToString(Date date) {
        try {
        SimpleDateFormat format1 = new SimpleDateFormat("MM-dd-yyyy");
        String formatted = format1.format(date);
        return formatted;
        } catch (Exception ex) {
            return null;
        }
    }

    public static Date stringToDate(String date, String format) {
        Date formated;
        try {
            formated = new SimpleDateFormat(format).parse(date);
            return formated;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String getlastMonthDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        return dateToString(cal.getTime());
    }
}
