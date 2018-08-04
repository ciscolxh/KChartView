package com.github.tifezh.kchartlib.chart.base;

import java.util.Date;

/**
 * 时间格式化接口
 *
 * @author tifezh
 * @date 2016/6/21
 */

public interface IDateTimeFormatter {
    /**
     * 格式化时间
     * @param date date
     * @return time
     */
    String format(Date date);
}
