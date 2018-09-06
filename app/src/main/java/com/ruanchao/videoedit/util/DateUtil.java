package com.ruanchao.videoedit.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm");

    public static String timeToDate(long time){
        return mDateFormat.format(new Date(time));
    }
}
