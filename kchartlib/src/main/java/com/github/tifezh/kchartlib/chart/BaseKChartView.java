package com.github.tifezh.kchartlib.chart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.github.tifezh.kchartlib.R;
import com.github.tifezh.kchartlib.chart.draw.VolumeDraw;
import com.github.tifezh.kchartlib.chart.entity.IKLine;
import com.github.tifezh.kchartlib.chart.formatter.TimeFormatter;
import com.github.tifezh.kchartlib.chart.formatter.ValueFormatter;
import com.github.tifezh.kchartlib.chart.base.IAdapter;
import com.github.tifezh.kchartlib.chart.base.IChartDraw;
import com.github.tifezh.kchartlib.chart.base.IDateTimeFormatter;
import com.github.tifezh.kchartlib.chart.base.IValueFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * k线图
 *
 * @author tian
 * @date 2016/5/3
 */
public abstract class BaseKChartView extends ScrollAndScaleView {

    private int mChildDrawPosition = 0;

    private float mTranslateX = Float.MIN_VALUE;

    private int mWidth = 0;

    private int mTopPadding;

    private int mBottomPadding;

    private float mMainScaleY = 1;

    private float volumeScaleY = 1;

    private float mChildScaleY = 1;

    private float mDataLen = 0;

    private float mMainMaxValue = Float.MAX_VALUE;

    private float mMainMinValue = Float.MIN_VALUE;

    private float volumeMaxValue = Float.MAX_VALUE;

    private float volumeMinValue = Float.MIN_VALUE;

    private float mChildMaxValue = Float.MAX_VALUE;

    private float mChildMinValue = Float.MIN_VALUE;

    private int mStartIndex = 0;

    private int mStopIndex = 0;

    private float mPointWidth = 6;

    /**
     * 表格行
     */
    private int mGridRows = 4;

    /**
     * 表格列
     */
    private int mGridColumns = 4;

    private Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mSelectedLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint c60Paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mSelectedIndex;


    private IAdapter mAdapter;

    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mItemCount = getAdapter().getCount();
            notifyChanged();
        }

        @Override
        public void onInvalidated() {
            mItemCount = getAdapter().getCount();
            notifyChanged();
        }
    };
    /**
     * 当前点的个数
     */
    private int mItemCount;

    /**
     * 交易量部分
     */
    private IChartDraw volumeDraw;

    /**
     * 主图部分
     */
    private IChartDraw mMainDraw;

    /**
     * 副图部分
     */
    private IChartDraw mChildDraw;
    /**
     * 子View的集合
     */
    private List<IChartDraw> mChildDraws = new ArrayList<>();

    private IValueFormatter mValueFormatter;

    private IDateTimeFormatter mDateTimeFormatter;

    private ValueAnimator mAnimator;

    private long mAnimationDuration = 500;

    private float mOverScrollRange = 0;

    private OnSelectedChangedListener mOnSelectedChangedListener = null;

    private Rect mMainRect;

    private Rect volumeRect;

    private Rect mChildRect;

    private float mLineWidth;

    public BaseKChartView(Context context) {
        this(context, null);
    }

    public BaseKChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseKChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        c60Paint.setColor(ContextCompat.getColor(getContext(), R.color.chart_grid_line));
        c60Paint.setTextSize(dp2px(30f));
        mDetector = new GestureDetectorCompat(getContext(), this);
        mScaleDetector = new ScaleGestureDetector(getContext(), this);
        mTopPadding = (int) getResources().getDimension(R.dimen.chart_top_padding);
        mBottomPadding = (int) getResources().getDimension(R.dimen.chart_bottom_padding);

        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setDuration(mAnimationDuration);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });


    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        initRect(w, h);
        setTranslateXFromScrollX(mScrollX);
    }

    private void initRect(int w, int h) {

        //当前屏幕的高度
        int displayHeight = h - mTopPadding - mBottomPadding;

        //主图的高度
        int mMainHeight = (int) (displayHeight * 0.6f);
        //子布局的高度
        int volumeHeight = (int) (displayHeight * 0.2f);

        int mChildHeight = (int) (displayHeight * 0.2);

        //绘制主图部分
        mMainRect = new Rect(0, mTopPadding, mWidth, mTopPadding + mMainHeight);

        //绘制子图部分
        volumeRect = new Rect(0, mMainRect.bottom, mWidth, mMainRect.bottom + volumeHeight);

        //绘制副图部分
        mChildRect = new Rect(0, volumeRect.bottom, mWidth, volumeRect.bottom + mChildHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(mBackgroundPaint.getColor());
        if (mWidth == 0 || mMainRect.height() == 0 || mItemCount == 0) {
            return;
        }
        //计算
        calculateValue();

        canvas.save();

        canvas.scale(1, 1);
        //画出表格背景
        drawGird(canvas);
        //画出一个Logo
        drawC60(canvas);
        //画出长按时候的线条
        drawK(canvas);
        //画出刻度，以及底部时间
        drawText(canvas);
        //画出日线等基线
        drawValue(canvas, isLongPress ? mSelectedIndex : mStopIndex);

        canvas.restore();
    }

    private void drawC60(Canvas canvas) {
        canvas.drawText("C60", dp2px(50f), mMainRect.bottom - dp2px(20f), c60Paint);
    }

    public float getMainY(float value) {
        return (mMainMaxValue - value) * mMainScaleY + mMainRect.top;
    }

    public float getChildY(float value) {
        return (mChildMaxValue - value) * mChildScaleY + mChildRect.top + dp2px(10f);
    }

    public float getVolumeY(float value) {
        return (volumeMaxValue - value) * volumeScaleY + volumeRect.top + dp2px(10f);
    }


    /**
     * 解决text居中的问题
     */
    public float fixTextY(float y) {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        return (y + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent);
    }

    /**
     * 画表格
     *
     * @param canvas canvas
     */
    private void drawGird(Canvas canvas) {
        //-----------------------上方k线图------------------------
        //行高
        float rowSpace = mMainRect.height() / mGridRows;
        for (int i = 0; i <= mGridRows; i++) {
            canvas.drawLine(0, rowSpace * i + mMainRect.top, mWidth, rowSpace * i + mMainRect.top, mGridPaint);
        }
        //-----------------------深度图------------------------
        canvas.drawLine(0, volumeRect.bottom, mWidth, volumeRect.bottom, mGridPaint);

        //-----------------------副图------------------------
        canvas.drawLine(0, mChildRect.bottom, mWidth, mChildRect.bottom, mGridPaint);
        //纵向的grid
        float columnSpace = mWidth / mGridColumns;
        for (int i = 0; i <= mGridColumns; i++) {
            //画竖线  横坐标不变  纵坐标起点为 主图的上边  终点为幅图的下边
            canvas.drawLine(columnSpace * i, mMainRect.top, columnSpace * i, mChildRect.bottom, mGridPaint);
        }


    }

    /**
     * 画k线图
     *
     * @param canvas canvas
     */
    private void drawK(Canvas canvas) {
        //保存之前的平移，缩放
        canvas.save();
        canvas.translate(mTranslateX * mScaleX, 0);
        canvas.scale(mScaleX, 1);
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            Object currentPoint = getItem(i);
            float currentPointX = getX(i);
            Object lastPoint = i == 0 ? currentPoint : getItem(i - 1);
            float lastX = i == 0 ? currentPointX : getX(i - 1);
            if (mMainDraw != null) {
                mMainDraw.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i);
            }

            if (volumeDraw != null) {
                volumeDraw.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i);
            }

            if (mChildDraw != null) {
                mChildDraw.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i);
            }

        }
        //画选择线
        if (isLongPress) {
            IKLine point = (IKLine) getItem(mSelectedIndex);
            float x = getX(mSelectedIndex);
            float y = getMainY(point.getClosePrice());
            canvas.drawLine(-mTranslateX, y, -mTranslateX + mWidth / mScaleX, y, mSelectedLinePaint);
            canvas.drawLine(x, mMainRect.top, x, mChildRect.bottom, mSelectedLinePaint);
        }
        //还原 平移缩放
        canvas.restore();
    }

    /**
     * 画文字
     *
     * @param canvas canvas
     */
    private void drawText(Canvas canvas) {
        //获取字体参数
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;

        float baseLine = (textHeight - fm.bottom - fm.top) / 2;
        //--------------画上方k线图的刻度值-------------
        if (mMainDraw != null) {
            float maxWidth = mTextPaint.measureText(formatValue(mMainMaxValue));
            float minWidth = mTextPaint.measureText(formatValue(mMainMinValue));
            canvas.drawText(formatValue(mMainMaxValue), mMainRect.right - dp2px(10f) - maxWidth, baseLine + mMainRect.top, mTextPaint);
            canvas.drawText(formatValue(mMainMinValue), mMainRect.right - dp2px(10f) - minWidth, mMainRect.bottom - textHeight + baseLine, mTextPaint);
            float rowValue = (mMainMaxValue - mMainMinValue) / mGridRows;
            float rowSpace = mMainRect.height() / mGridRows;
            for (int i = 1; i < mGridRows; i++) {
                String text = formatValue(rowValue * (mGridRows - i) + mMainMinValue);
                float width = mTextPaint.measureText(text);
                canvas.drawText(text, mMainRect.right - dp2px(10f) - width, fixTextY(rowSpace * i + mMainRect.top), mTextPaint);
            }
        }
        //--------------画交易量图的值-------------
        if (volumeDraw != null) {
            float maxWidth = mTextPaint.measureText(volumeDraw.getValueFormatter().format(volumeMaxValue));
            canvas.drawText(volumeDraw.getValueFormatter().format(volumeMaxValue), volumeRect.right - dp2px(10f) - maxWidth, volumeRect.top + baseLine + dp2px(10f), mTextPaint);
        }

        //--------------副图不需要刻度值-------------


        //--------------画时间---------------------
        float columnSpace = mWidth / mGridColumns;
        float y = mChildRect.bottom + baseLine;

        float startX = getX(mStartIndex) - mPointWidth / 2;
        float stopX = getX(mStopIndex) + mPointWidth / 2;

        for (int i = 1; i < mGridColumns; i++) {
            float translateX = xToTranslateX(columnSpace * i);
            if (translateX >= startX && translateX <= stopX) {
                int index = indexOfTranslateX(translateX);
                String text = formatDateTime(mAdapter.getDate(index));
                canvas.drawText(text, columnSpace * i - mTextPaint.measureText(text) / 2, y, mTextPaint);
            }
        }

        float translateX = xToTranslateX(0);
        if (translateX >= startX && translateX <= stopX) {
            canvas.drawText(formatDateTime(getAdapter().getDate(mStartIndex)), 0, y, mTextPaint);
        }
        translateX = xToTranslateX(mWidth);
        if (translateX >= startX && translateX <= stopX) {
            String text = formatDateTime(getAdapter().getDate(mStopIndex));
            canvas.drawText(text, mWidth - mTextPaint.measureText(text), y, mTextPaint);
        }
        if (isLongPress) {
            IKLine point = (IKLine) getItem(mSelectedIndex);
            String text = formatValue(point.getClosePrice());
            float r = textHeight / 2;
            y = getMainY(point.getClosePrice());
            float x;
            if (translateXtoX(getX(mSelectedIndex)) < getChartWidth() / 2) {
                x = 0;
                canvas.drawRect(x, y - r, mTextPaint.measureText(text), y + r, mBackgroundPaint);
            } else {
                x = mWidth - mTextPaint.measureText(text);
                canvas.drawRect(x, y - r, mWidth, y + r, mBackgroundPaint);
            }
            canvas.drawText(text, x, fixTextY(y), mTextPaint);
        }
    }

    /**
     * 画值
     *
     * @param canvas   canvas
     * @param position 显示某个点的值
     */
    private void drawValue(Canvas canvas, int position) {
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float baseLine = (textHeight - fm.bottom - fm.top) / 2;
        if (position >= 0 && position < mItemCount) {
            if (mMainDraw != null) {
                float y = mMainRect.top + baseLine - textHeight;
                float x = 0;
                mMainDraw.drawText(canvas, this, position, x, y);
            }
            if (volumeDraw != null) {
                float y = volumeRect.top + baseLine;
                float x = 0;
                volumeDraw.drawText(canvas, this, position, x, y);
            }

            if (mChildDraw != null) {
                float y = mChildRect.top + baseLine;
                float x = 0;
                mChildDraw.drawText(canvas, this, position, x, y);
            }
        }
    }

    public int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 格式化值
     */
    public String formatValue(float value) {
        if (getValueFormatter() == null) {
            setValueFormatter(new ValueFormatter());
        }
        return getValueFormatter().format(value);
    }

    /**
     * 重新计算并刷新线条
     */
    public void notifyChanged() {
        if (mItemCount != 0) {
            mDataLen = (mItemCount - 1) * mPointWidth;
            checkAndFixScrollX();
            setTranslateXFromScrollX(mScrollX);
        } else {
            setScrollX(0);
        }
        invalidate();
    }

    private void calculateSelectedX(float x) {
        mSelectedIndex = indexOfTranslateX(xToTranslateX(x));
        if (mSelectedIndex < mStartIndex) {
            mSelectedIndex = mStartIndex;
        }
        if (mSelectedIndex > mStopIndex) {
            mSelectedIndex = mStopIndex;
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
        int lastIndex = mSelectedIndex;
        calculateSelectedX(e.getX());
        if (lastIndex != mSelectedIndex) {
            onSelectedChanged(this, getItem(mSelectedIndex), mSelectedIndex);
        }
        invalidate();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        setTranslateXFromScrollX(mScrollX);
    }

    @Override
    protected void onScaleChanged(float scale, float oldScale) {
        checkAndFixScrollX();
        setTranslateXFromScrollX(mScrollX);
        super.onScaleChanged(scale, oldScale);
    }

    /**
     * 计算当前的显示区域
     */
    private void calculateValue() {
        if (!isLongPress()) {
            mSelectedIndex = -1;
        }
        mMainMaxValue = Float.MIN_VALUE;
        mMainMinValue = Float.MAX_VALUE;
        volumeMaxValue = Float.MIN_VALUE;
        volumeMinValue = Float.MAX_VALUE;
        mChildMaxValue = Float.MIN_VALUE;
        mChildMinValue = Float.MAX_VALUE;
        //页面显示的第一个索引
        mStartIndex = indexOfTranslateX(xToTranslateX(0));
        //页面显示的最后一个索引
        mStopIndex = indexOfTranslateX(xToTranslateX(mWidth));

        //找出当前页面的最小值和最大值
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            IKLine point = (IKLine) getItem(i);
            if (mMainDraw != null) {
                mMainMaxValue = Math.max(mMainMaxValue, mMainDraw.getMaxValue(point));
                mMainMinValue = Math.min(mMainMinValue, mMainDraw.getMinValue(point));
            }
            if (volumeDraw != null) {
                volumeMaxValue = Math.max(volumeMaxValue, volumeDraw.getMaxValue(point));
                volumeMinValue = Math.min(volumeMinValue, volumeDraw.getMinValue(point));
            }

            if (mChildDraw != null) {
                mChildMaxValue = Math.max(mChildMaxValue, mChildDraw.getMaxValue(point));
                mChildMinValue = Math.min(mChildMinValue, mChildDraw.getMinValue(point));
            }
        }

        if (mMainMaxValue != mMainMinValue) {
            float padding = (mMainMaxValue - mMainMinValue) * 0.05f;
            mMainMaxValue += padding;
            mMainMinValue -= padding;
        } else {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mMainMaxValue += Math.abs(mMainMaxValue * 0.05f);
            mMainMinValue -= Math.abs(mMainMinValue * 0.05f);
            if (mMainMaxValue == 0) {
                mMainMaxValue = 1;
            }
        }

        if (volumeMaxValue == volumeMinValue) {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            volumeMaxValue += Math.abs(volumeMaxValue * 0.05f);
            volumeMinValue -= Math.abs(volumeMinValue * 0.05f);
            if (volumeMaxValue == 0) {
                volumeMaxValue = 1;
            }
        }

        if (mChildMaxValue == mChildMinValue) {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mChildMaxValue += Math.abs(mChildMaxValue * 0.05f);
            mChildMinValue -= Math.abs(mChildMinValue * 0.05f);
            if (mChildMaxValue == 0) {
                mChildMaxValue = 1;
            }
        }

        mMainScaleY = mMainRect.height() * 1f / (mMainMaxValue - mMainMinValue);
        //
        volumeScaleY = (volumeRect.height() - dp2px(10f)) * 1f / (volumeMaxValue - volumeMinValue);

        mChildScaleY = (mChildRect.height() - dp2px(10f)) * 1f / (mChildMaxValue - mChildMinValue);

        if (mAnimator.isRunning()) {
            float value = (float) mAnimator.getAnimatedValue();
            mStopIndex = mStartIndex + Math.round(value * (mStopIndex - mStartIndex));
        }
    }

    /**
     * 获取平移的最小值
     *
     * @return
     */
    private float getMinTranslateX() {
        return -mDataLen + mWidth / mScaleX - mPointWidth / 2;
    }

    /**
     * 获取平移的最大值
     *
     * @return 平移最大值
     */
    private float getMaxTranslateX() {
        if (!isFullScreen()) {
            return getMinTranslateX();
        }
        return mPointWidth / 2;
    }

    @Override
    public int getMinScrollX() {
        return (int) -(mOverScrollRange / mScaleX);
    }

    @Override
    public int getMaxScrollX() {
        return Math.round(getMaxTranslateX() - getMinTranslateX());
    }

    public int indexOfTranslateX(float translateX) {
        return indexOfTranslateX(translateX, 0, mItemCount - 1);
    }

    /**
     * 在主区域画线
     *
     * @param startX    开始点的横坐标
     * @param stopX     开始点的值
     * @param stopX     结束点的横坐标
     * @param stopValue 结束点的值
     */
    public void drawMainLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getMainY(startValue), stopX, getMainY(stopValue), paint);
    }

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    public void drawChildLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getChildY(startValue), stopX, getChildY(stopValue), paint);
    }


    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    public void drawVolumeLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getVolumeY(startValue), stopX, getVolumeY(stopValue), paint);
    }



    /**
     * 根据索引获取实体
     *
     * @param position 索引值
     * @return 对象
     */
    public Object getItem(int position) {
        if (mAdapter != null) {
            return mAdapter.getItem(position);
        } else {
            return null;
        }
    }

    /**
     * 根据索引索取x坐标
     *
     * @param position 索引值
     * @return 索引
     */
    public float getX(int position) {
        return position * mPointWidth;
    }

    /**
     * 获取适配器
     *
     * @return adapter
     */
    public IAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 设置子图的绘制方法
     *
     * @param position position
     */
    public void setChildDraw(int position) {
        this.mChildDraw = mChildDraws.get(position);
        mChildDrawPosition = position;
        //重新绘制图像
        invalidate();
    }

    /**
     * 设置副图
     *
     * @param volumeDraw View
     */
    public void setVolumeDraw(VolumeDraw volumeDraw) {
        this.volumeDraw = volumeDraw;
        //重新绘制图像
        invalidate();
    }

    /**
     * 给子区域添加画图方法
     *
     * @param childDraw IChartDraw
     */
    public void addVolumeDraw( IChartDraw childDraw) {
        mChildDraws.add(childDraw);
    }

    /**
     * scrollX 转换为 TranslateX
     *
     * @param scrollX TranslateX
     */
    private void setTranslateXFromScrollX(int scrollX) {
        mTranslateX = scrollX + getMinTranslateX();
    }

    /**
     * 获取ValueFormatter
     *
     * @return
     */
    public IValueFormatter getValueFormatter() {
        return mValueFormatter;
    }

    /**
     * 设置ValueFormatter
     *
     * @param valueFormatter value格式化器
     */
    public void setValueFormatter(IValueFormatter valueFormatter) {
        this.mValueFormatter = valueFormatter;
    }

    /**
     * 获取DatetimeFormatter
     *
     * @return 时间格式化器
     */
    public IDateTimeFormatter getDateTimeFormatter() {
        return mDateTimeFormatter;
    }

    /**
     * 设置dateTimeFormatter
     *
     * @param dateTimeFormatter 时间格式化器
     */
    public void setDateTimeFormatter(IDateTimeFormatter dateTimeFormatter) {
        mDateTimeFormatter = dateTimeFormatter;
    }

    /**
     * 格式化时间
     *
     * @param date date
     */
    public String formatDateTime(Date date) {
        if (getDateTimeFormatter() == null) {
            setDateTimeFormatter(new TimeFormatter());
        }
        return getDateTimeFormatter().format(date);
    }

    /**
     * 获取主区域的 IChartDraw
     *
     * @return IChartDraw
     */
    public IChartDraw getMainDraw() {
        return mMainDraw;
    }

    /**
     * 设置主区域的 IChartDraw
     *
     * @param mainDraw IChartDraw
     */
    public void setMainDraw(IChartDraw mainDraw) {
        mMainDraw = mainDraw;
    }

    /**
     * 二分查找当前值的index
     *
     * @return 查找出屏幕上当前显示出来的内容的 index
     */
    public int indexOfTranslateX(float translateX, int start, int end) {
        if (end == start) {
            return start;
        }
        if (end - start == 1) {
            float startValue = getX(start);
            float endValue = getX(end);
            return Math.abs(translateX - startValue) < Math.abs(translateX - endValue) ? start : end;
        }
        int mid = start + (end - start) / 2;
        float midValue = getX(mid);
        if (translateX < midValue) {
            return indexOfTranslateX(translateX, start, mid);
        } else if (translateX > midValue) {
            return indexOfTranslateX(translateX, mid, end);
        } else {
            return mid;
        }
    }

    /**
     * 设置数据适配器
     */
    public void setAdapter(IAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
            mItemCount = mAdapter.getCount();
        } else {
            mItemCount = 0;
        }
        notifyChanged();
    }

    /**
     * 开始动画
     */
    public void startAnimation() {
        if (mAnimator != null) {
            mAnimator.start();
        }
    }

    /**
     * 设置动画时间
     */
    public void setAnimationDuration(long duration) {
        if (mAnimator != null) {
            mAnimator.setDuration(duration);
        }
    }

    /**
     * 设置表格行数
     */
    public void setGridRows(int gridRows) {
        if (gridRows < 1) {
            gridRows = 1;
        }
        mGridRows = gridRows;
    }

    /**
     * 设置表格列数
     */
    public void setGridColumns(int gridColumns) {
        if (gridColumns < 1) {
            gridColumns = 1;
        }
        mGridColumns = gridColumns;
    }

    /**
     * view中的x转化为TranslateX
     *
     * @param x
     * @return
     */
    public float xToTranslateX(float x) {
        return -mTranslateX + x / mScaleX;
    }

    /**
     * translateX转化为view中的x
     *
     * @param translateX
     * @return
     */
    public float translateXtoX(float translateX) {
        return (translateX + mTranslateX) * mScaleX;
    }

    /**
     * 获取上方padding
     */
    public float getTopPadding() {
        return mTopPadding;
    }

    /**
     * 获取图的宽度
     *
     * @return
     */
    public int getChartWidth() {
        return mWidth;
    }

    /**
     * 是否长按
     */
    public boolean isLongPress() {
        return isLongPress;
    }

    /**
     * 获取选择索引
     */
    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public Rect getVolumeRect() {
        return volumeRect;
    }

    /**
     * 设置选择监听
     */
    public void setOnSelectedChangedListener(OnSelectedChangedListener l) {
        this.mOnSelectedChangedListener = l;
    }

    public void onSelectedChanged(BaseKChartView view, Object point, int index) {
        if (this.mOnSelectedChangedListener != null) {
            mOnSelectedChangedListener.onSelectedChanged(view, point, index);
        }
    }

    /**
     * 数据是否充满屏幕
     *
     * @return
     */
    public boolean isFullScreen() {
        return mDataLen >= mWidth / mScaleX;
    }

    /**
     * 设置超出右方后可滑动的范围
     */
    public void setOverScrollRange(float overScrollRange) {
        if (overScrollRange < 0) {
            overScrollRange = 0;
        }
        mOverScrollRange = overScrollRange;
    }

    /**
     * 设置上方padding
     *
     * @param topPadding
     */
    public void setTopPadding(int topPadding) {
        mTopPadding = topPadding;
    }

    /**
     * 设置下方padding
     *
     * @param bottomPadding
     */
    public void setBottomPadding(int bottomPadding) {
        mBottomPadding = bottomPadding;
    }

    /**
     * 设置表格线宽度
     */
    public void setGridLineWidth(float width) {
        mGridPaint.setStrokeWidth(width);
    }

    /**
     * 设置表格线颜色
     */
    public void setGridLineColor(int color) {
        mGridPaint.setColor(color);
    }

    /**
     * 设置选择线宽度
     */
    public void setSelectedLineWidth(float width) {
        mSelectedLinePaint.setStrokeWidth(width);
    }

    /**
     * 设置表格线颜色
     */
    public void setSelectedLineColor(int color) {
        mSelectedLinePaint.setColor(color);
    }

    /**
     * 设置文字颜色
     */
    public void setTextColor(int color) {
        mTextPaint.setColor(color);
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
    }

    /**
     * 设置背景颜色
     */
    @Override
    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
    }


    /**
     * 选中点变化时的监听
     */
    public interface OnSelectedChangedListener {
        /**
         * 当选点中变化时
         *
         * @param view  当前view
         * @param point 选中的点
         * @param index 选中点的索引
         */
        void onSelectedChanged(BaseKChartView view, Object point, int index);
    }

    /**
     * 获取文字大小
     */
    public float getTextSize() {
        return mTextPaint.getTextSize();
    }

    /**
     * 获取曲线宽度
     */
    public float getLineWidth() {
        return mLineWidth;
    }

    /**
     * 设置曲线的宽度
     */
    public void setLineWidth(float lineWidth) {
        mLineWidth = lineWidth;
    }

    /**
     * 设置每个点的宽度
     */
    public void setPointWidth(float pointWidth) {
        mPointWidth = pointWidth;
    }

    public Paint getGridPaint() {
        return mGridPaint;
    }

    public Paint getTextPaint() {
        return mTextPaint;
    }

    public Paint getBackgroundPaint() {
        return mBackgroundPaint;
    }

    public Paint getSelectedLinePaint() {
        return mSelectedLinePaint;
    }
}
