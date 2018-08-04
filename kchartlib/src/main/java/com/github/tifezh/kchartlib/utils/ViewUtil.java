package com.github.tifezh.kchartlib.utils;

import android.content.Context;

/**
 *
 * @author tian
 * @date 2016/4/11
 */
public class ViewUtil {

    static public int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    static public int px2dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
}
