package com.github.tifezh.kchartlib.chart.entity;

/**
 * KDJ指标(随机指标)接口
 * @author tifezh
 * @see <a href="https://baike.baidu.com/item/KDJ%E6%8C%87%E6%A0%87/6328421?fr=aladdin&fromid=3423560&fromtitle=kdj"/>相关说明</a>
 * @date  2016/6/10.
 */
public interface IKDJ {

    /**
     * K值
     * @return K
     */
    float getK();

    /**
     * D值
     * @return D
     */
    float getD();

    /**
     * J值
     * @return J
     */
    float getJ();

}
