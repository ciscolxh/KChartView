package com.github.tifezh.kchartlib.chart.entity;

/**
 * 成交量接口
 *
 * @author hjm
 * @date 2017/11/14 17:46
 */

public interface IVolume {

    /**
     * 开盘价
     * @return 开盘价
     */
    float getOpenPrice();

    /**
     * 收盘价
     * @return 收盘价
     */
    float getClosePrice();

    /**
     * 成交量
     * @return 成交量
     */
    float getVolume();

    /**
     * 五(月，日，时，分，5分等)均量
     * @return 五日均线
     */
    float getMA5Volume();

    /**
     * 十(月，日，时，分，5分等)均量
     * @return 十日均线
     */
    float getMA10Volume();
}
