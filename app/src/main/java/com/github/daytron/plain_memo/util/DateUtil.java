package com.github.daytron.plain_memo.util;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Date;

/**
 * Created by ryan on 28/10/15.
 */
public class DateUtil {

    private DateUtil(){}

    public static String getTimeStringLocale(Context context, Date date) {
        return DateFormat.getTimeFormat(context).format(date);
    }

    public static String getDateStringLocale(Context context, Date date) {
        return DateFormat.getDateFormat(context).format(date);
    }
}
