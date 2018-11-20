package com.lipcap.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    /*Convert string to date*/
    public static String getCurrentDate() {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yyyy",Locale.US);
        return inputDateFormat.format(new Date());
    }
}
