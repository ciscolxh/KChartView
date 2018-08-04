package com.github.tifezh.kchartlib.chart.entity;

/**
 * 蜡烛图实体接口
 *
 * @author tifezh
 * @date 2016/6/9
 */

public interface ICandle {

    /**
     * 开盘价
     * @return r
     */
    float getOpenPrice();

    /**
     * 最高价
     * @return r
     */
    float getHighPrice();

    /**
     * 最低价
     * @return r
     */
    float getLowPrice();

    /**
     * 收盘价
     * @return r
     */
    float getClosePrice();

    /**
     * 五(月，日，时，分，5分等)均价
     * @return r
     */
    float getMA5Price();

    /**
     * 十(月，日，时，分，5分等)均价
     * @return r
     */
    float getMA10Price();

    /**
     * 二十(月，日，时，分，5分等)均价
     * @return r
     */
    float getMA20Price();
}
