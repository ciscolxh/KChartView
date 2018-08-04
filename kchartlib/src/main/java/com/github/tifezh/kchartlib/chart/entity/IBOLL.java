package com.github.tifezh.kchartlib.chart.entity;

/**
 * 布林线指标接口
 * @author tifezh
 * @see <a href="https://baike.baidu.com/item/%E5%B8%83%E6%9E%97%E7%BA%BF%E6%8C%87%E6%A0%87/3325894"/>相关说明</a>
 * @date 2016/6/10.
 * 想要实现布林线 必须重写三个方法
 * 上轨线  中轨线  下轨线
 */

public interface IBOLL {

    /**
     * 上轨线
     * @return r
     */
    float getUp();

    /**
     * 中轨线
     * @return r
     */
    float getMb();

    /**
     * 下轨线
     * @return r
     */
    float getDn();
}
