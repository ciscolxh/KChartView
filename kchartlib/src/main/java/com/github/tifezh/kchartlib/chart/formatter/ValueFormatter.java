package com.github.tifezh.kchartlib.chart.formatter;

import android.annotation.SuppressLint;

import com.github.tifezh.kchartlib.chart.base.IValueFormatter;

/**
 * Value格式化类
 *
 * @author tifezh
 * @date 2016/6/21
 */

public class ValueFormatter implements IValueFormatter {
    @SuppressLint("DefaultLocale")
    @Override
    public String format(float value) {
        return String.format("%.2f", value);
    }
}
