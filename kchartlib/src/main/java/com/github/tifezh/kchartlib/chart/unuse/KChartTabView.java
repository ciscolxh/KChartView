package com.github.tifezh.kchartlib.chart.unuse;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.tifezh.kchartlib.R;
import com.github.tifezh.kchartlib.chart.TabView;
import com.github.tifezh.kchartlib.utils.ViewUtil;


/**
 * K线图中间位置的TabBar
 *
 * @author tifezh
 * @date 2016/5/17
 */
public class KChartTabView extends RelativeLayout implements View.OnClickListener {

    LinearLayout mLlContainer;
    TextView mTvFullScreen = (TextView) findViewById(R.id.tv_fullScreen);
    private TabSelectListener mTabSelectListener = null;
    /**
     * 当前选择的index
     */
    private int mSelectedIndex = 0;
    private ColorStateList mColorStateList;
    private int mIndicatorColor;

    public KChartTabView(Context context) {
        this(context,null);

    }

    public KChartTabView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public KChartTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_tab, null, false);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewUtil.dp2px(getContext(), 0));
        view.setLayoutParams(layoutParams);
        addView(view);
        mLlContainer = findViewById(R.id.ll_container);
        //切换横竖屏
        mTvFullScreen = findViewById(R.id.tv_fullScreen);
        mTvFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) getContext();
                boolean isVertical = (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
                if (isVertical) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                }
            }
        });
        mTvFullScreen.setSelected(true);
        if(mColorStateList!=null)
        {
            mTvFullScreen.setTextColor(mColorStateList);
        }
    }

    @Override
    public void onClick(View v) {
        //这个是TabView设置选中状态的
        if (mSelectedIndex >= 0 && mSelectedIndex < mLlContainer.getChildCount()) {
            mLlContainer.getChildAt(mSelectedIndex).setSelected(false);
        }
        mSelectedIndex = mLlContainer.indexOfChild(v);
        v.setSelected(true);
        mTabSelectListener.onTabSelected(mSelectedIndex);
    }

    public interface TabSelectListener {
        /**
         * 选择tab的位置序号
         *
         * @param position
         */
        void onTabSelected(int position);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 添加选项卡
     *
     * @param text 选项卡文字
     */
    public void addTab(String text) {
        TabView tabView=new TabView(getContext());
        tabView.setOnClickListener(this);
        tabView.setText(text);
        tabView.setTextColor(mColorStateList);
        tabView.setIndicatorColor(mIndicatorColor);
        mLlContainer.addView(tabView);
        //第一个默认选中
        if (mLlContainer.getChildCount() == 1) {
            tabView.setSelected(true);
            mSelectedIndex = 0;
            onTabSelected(mSelectedIndex);
        }
    }

    /**
     * 设置选项卡监听
     *
     * @param listener TabSelectListener
     */
    public void setOnTabSelectListener(TabSelectListener listener) {
        this.mTabSelectListener = listener;
        //默认选中上一个位置
        if (mLlContainer.getChildCount() > 0 && mTabSelectListener != null) {
            mTabSelectListener.onTabSelected(mSelectedIndex);
        }
    }

    private void onTabSelected(int position) {
        if (mTabSelectListener != null) {
            mTabSelectListener.onTabSelected(position);
        }
    }

    public void setTextColor(ColorStateList color)
    {
        mColorStateList=color;
        for(int i=0;i<mLlContainer.getChildCount();i++)
        {
            TabView tabView= (TabView) mLlContainer.getChildAt(i);
            tabView.setTextColor(mColorStateList);
        }
        if(mColorStateList!=null)
        {
            mTvFullScreen.setTextColor(mColorStateList);
        }
    }

    public void setIndicatorColor(int color)
    {
        mIndicatorColor=color;
        for(int i=0;i<mLlContainer.getChildCount();i++)
        {
            TabView tabView= (TabView) mLlContainer.getChildAt(i);
            tabView.setIndicatorColor(mIndicatorColor);
        }
    }

}
