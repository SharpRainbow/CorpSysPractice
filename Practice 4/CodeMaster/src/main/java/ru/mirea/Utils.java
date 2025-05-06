package ru.mirea;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static String getTimeStamp() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    public static String getUniqueFileName(String fileName) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return String.format("%s_%s", formatter.format(date), fileName);
    }

}
