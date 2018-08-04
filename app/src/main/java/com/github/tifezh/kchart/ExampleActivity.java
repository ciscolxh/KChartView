package com.github.tifezh.kchart;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.tifezh.kchart.chart.KChartAdapter;
import com.github.tifezh.kchart.chart.KLineEntity;
import com.github.tifezh.kchartlib.chart.BaseKChartView;
import com.github.tifezh.kchartlib.chart.KChartView;
import com.github.tifezh.kchartlib.chart.formatter.DateFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author macman
 */
public class ExampleActivity extends AppCompatActivity {


    @BindView(R.id.title_view)
    RelativeLayout mTitleView;

    /**
     * 这是一个K线图的
     */
    @BindView(R.id.kchart_view)
    KChartView mKChartView;

    @BindView(R.id.macd)
    TextView macd;

    @BindView(R.id.kdj)
    TextView kdj;

    @BindView(R.id.rsi)
    TextView rsi;

    @BindView(R.id.boll)
    TextView boll;

    private KChartAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        int type = getIntent().getIntExtra("type", 0);
        /*
         * type == 0 样式一
         * type == 1 样式二
         */
        if (type == 0) {
            setContentView(R.layout.activity_example);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //设置标题栏背景透明
                Window window = getWindow();
                window.setFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        } else {
            setContentView(R.layout.activity_example_light);
        }
        ButterKnife.bind(this);
        initView();
        initData();

        mTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isVertical = (ExampleActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
                if (isVertical) {
                    ExampleActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    ExampleActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                }
            }
        });

        macd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKChartView.setChildDraw(0);
            }
        });
        kdj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKChartView.setChildDraw(1);
            }
        });

        rsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKChartView.setChildDraw(2);
            }
        });

        boll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKChartView.setChildDraw(3);
            }
        });
    }

    private void initView() {
        //创建图表Adapter
        mAdapter = new KChartAdapter();
        mKChartView.setAdapter(mAdapter);
        mKChartView.setDateTimeFormatter(new DateFormatter());
        mKChartView.setGridRows(4);
        mKChartView.setGridColumns(4);
        mKChartView.setOnSelectedChangedListener(new BaseKChartView.OnSelectedChangedListener() {
            @Override
            public void onSelectedChanged(BaseKChartView view, Object point, int index) {
                KLineEntity data = (KLineEntity) point;
                Log.i("onSelectedChanged", "index:" + index + " closePrice:" + data.getClosePrice());
            }
        });
    }

    private void initData() {
        mKChartView.showLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<KLineEntity> data = DataRequest.getALL(ExampleActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.addFooterData(data);
                        mKChartView.startAnimation();
                        mKChartView.refreshEnd();
                    }
                });
            }
        }).start();
    }


    /**
     * 横竖屏切换监听
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mKChartView.setGridRows(3);
            mKChartView.setGridColumns(8);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mKChartView.setGridRows(4);
            mKChartView.setGridColumns(4);
        }
    }
}
