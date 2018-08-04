package com.github.tifezh.kchartlib.chart.entity;

/**
 * MACD指标(指数平滑移动平均线)接口
 * @author tifezh
 * @see <a href="https://baike.baidu.com/item/MACD指标"/>相关说明</a>
 * @date on 2016/6/10.
 */

public interface IMACD {


    /**
     * DEA值
     * @return DEA
     */
    float getDea();

    /**
     * DIF值
     * @return DIF
     */
    float getDif();

    /**
     * MACD值
     * @return MACD
     */
    float getMacd();

}
