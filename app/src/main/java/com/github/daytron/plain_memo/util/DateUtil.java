package com.github.daytron.plain_memo.util;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for handling dates.
 */
public class DateUtil {

    private DateUtil(){}

    public static String getTimeStringLocale(Context context, Date date) {
        return DateFormat.getTimeFormat(context).format(date);
    }

    public static String getDateStringLocale(Context context, Date date) {
        return DateFormat.getDateFormat(context).format(date);
    }

    public static int compareToToday(Date givenDate) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        Calendar date = Calendar.getInstance();
        date.setTime(givenDate);

        Calendar dayMinus2 = Calendar.getInstance();
        dayMinus2.add(Calendar.DAY_OF_YEAR, -1);
        dayMinus2.set(Calendar.HOUR_OF_DAY, 0);
        dayMinus2.set(Calendar.MINUTE, 0);
        dayMinus2.set(Calendar.SECOND, 0);
        dayMinus2.set(Calendar.MILLISECOND, 0);

        Calendar dayMinus8 = Calendar.getInstance();
        dayMinus8.add(Calendar.DAY_OF_MONTH, -8);
        dayMinus8.set(Calendar.HOUR_OF_DAY, 0);
        dayMinus8.set(Calendar.MINUTE, 0);
        dayMinus8.set(Calendar.SECOND, 0);
        dayMinus8.set(Calendar.MILLISECOND, 0);

        if (DateUtils.isToday(givenDate.getTime())) {
            return 0;
        } else if (date.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)
                && date.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)){
            // Yesterday
            return 1;
        } else if (date.before(dayMinus2) && date.after(dayMinus8)) {
            // Within last 7 days (from today) and before yesterday
            return 2;
        } else if (date.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
            // Within the same year and before last 7 days including today
            return 3;
        } else {
            return 4;
        }
    }
}
