package com.github.tifezh.kchartlib.chart.base;

/**
 * Value格式化接口
 *
 * @author tifezh
 * @date 2016/6/21
 */

public interface IValueFormatter {
    /**
     * 格式化value
     *
     * @param value 传入的value值
     * @return 返回字符串
     */
    String format(float value);
}
