package com.github.tifezh.kchartlib.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 时间工具类
 *
 * @author tifezh
 * @date 2016/4/27
 */
public class DateUtil {

    public static SimpleDateFormat longTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    public static SimpleDateFormat shortTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public static SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

}
