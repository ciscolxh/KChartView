package com.github.tifezh.kchartlib.chart.entity;

import java.util.Date;

/**
 * 分时线
 *
 * @author tifezh
 * @date 2017/7/19
 */

public interface IMinuteLine {

    /**
     * 获取均价
     * @return 均价
     */
    float getAvgPrice();

    /**
     * 获取成交价
     * @return 成交价
     */
    float getPrice();

    /**
     * 该指标对应的时间
     * @return 时间
     */
    Date getDate();

    /**
     * 成交量
     * @return 成交量
     */
    float getVolume();
}
