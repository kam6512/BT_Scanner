

package com.rainbow.kam.bt_scanner.tools.design;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.rainbow.kam.bt_scanner.R;

public class CircleCounter extends View {


    private final static float START_DEGREES = 90;

    private int mBackgroundCenter;
    private int mBackgroundRadius;


    private int mOneDegrees;
    private int mTwoDegrees;
    private int mThreeDegrees;


    private int mOneValue = 0;


    private int mRange;


    private float mOneWidth;
    private float mTwoWidth;
    private float mThreeWidth;


    private float mTextSize;
    private float mMetricSize;


    private int mOneColor;
    private int mTwoColor;
    private int mThreeColor;


    private int mTextColor = -1;
    private int mBackgroundColor;


    private Paint mOnePaint;
    private Paint mTwoPaint;
    private Paint mThreePaint;
    private Paint mBackgroundPaint;
    private Paint mTextPaint;
    private Paint mMetricPaint;


    private RectF mOneBounds;
    private RectF mTwoBounds;
    private RectF mThreeBounds;


    private float mTextPosY;
    private float mMetricPosY;
    private float mMetricPaddingY;


    private String mMetricText;


    private Typeface mTypeface;


    @SuppressLint("Recycle")
    public CircleCounter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context.obtainStyledAttributes(attrs, R.styleable.CircularMeter));
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        setupBounds();
        setupPaints();
        setupTextPosition();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mOnePaint = null;
        mOneBounds = null;
        mTwoPaint = null;
        mTwoBounds = null;
        mBackgroundPaint = null;
        mTextPaint = null;
        mMetricPaint = null;
    }


    private void setupPaints() {

        mOnePaint = new Paint();
        mOnePaint.setColor(mOneColor);
        mOnePaint.setAntiAlias(true);
        mOnePaint.setStyle(Style.STROKE);
        mOnePaint.setStrokeWidth(getResources().getDimension(R.dimen.first));

        mTwoPaint = new Paint();
        mTwoPaint.setColor(mTwoColor);
        mTwoPaint.setAntiAlias(true);
        mTwoPaint.setStyle(Style.STROKE);
        mTwoPaint.setStrokeWidth(mTwoWidth);
        mTwoPaint.setStrokeWidth(getResources().getDimension(R.dimen.second));

        mThreePaint = new Paint();
        mThreePaint.setColor(mThreeColor);
        mThreePaint.setAntiAlias(true);
        mThreePaint.setStyle(Style.STROKE);
        mThreePaint.setStrokeWidth(mThreeWidth);
        mThreePaint.setStrokeWidth(getResources().getDimension(R.dimen.third));

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Style.FILL);

        mTextPaint = new Paint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setStyle(Style.FILL);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTypeface(mTypeface);
        mTextPaint.setTextAlign(Align.CENTER);

        mMetricPaint = new Paint();
        mMetricPaint.setColor(mTextColor);
        mMetricPaint.setStyle(Style.FILL);
        mMetricPaint.setAntiAlias(true);
        mMetricPaint.setTextSize(mMetricSize);
        mMetricPaint.setTypeface(mTypeface);
        mMetricPaint.setTextAlign(Align.CENTER);
    }


    private void setupBounds() {

        mBackgroundCenter = this.getLayoutParams().width / 2;
        mBackgroundRadius = mBackgroundCenter - this.getPaddingTop();

        mOneBounds = new RectF(this.getPaddingTop() + mOneWidth / 2,
                this.getPaddingLeft() + mOneWidth / 2,
                this.getLayoutParams().width - this.getPaddingRight()
                        - mOneWidth / 2, this.getLayoutParams().height
                - this.getPaddingBottom() - mOneWidth / 2);

        mTwoBounds = new RectF(
                this.getPaddingTop() + mTwoWidth / 2 + mOneWidth,
                this.getPaddingLeft() + mTwoWidth / 2 + mOneWidth,
                this.getLayoutParams().width - this.getPaddingRight()
                        - mTwoWidth / 2 - mOneWidth,
                this.getLayoutParams().height - this.getPaddingBottom()
                        - mTwoWidth / 2 - mOneWidth);

        mThreeBounds = new RectF(this.getPaddingTop() + mThreeWidth / 2
                + mTwoWidth + mOneWidth, this.getPaddingLeft() + mThreeWidth
                / 2 + mTwoWidth + mOneWidth, this.getLayoutParams().width
                - this.getPaddingRight() - mThreeWidth / 2 - mTwoWidth
                - mOneWidth, this.getLayoutParams().height
                - this.getPaddingBottom() - mThreeWidth / 2 - mTwoWidth
                - mOneWidth);
    }


    private void setupTextPosition() {
        Rect textBounds = new Rect();
        mTextPaint.getTextBounds("1", 0, 1, textBounds);
        mTextPosY = mOneBounds.centerY() + (textBounds.height() / 2f);
        mMetricPosY = mTextPosY + mMetricPaddingY;
    }


    private void init(TypedArray typedArray) {

        mTextSize = typedArray.getDimension(R.styleable.CircularMeter_textSize,
                getResources().getDimension(R.dimen.text_size));
        mTextColor = typedArray
                .getColor(R.styleable.CircularMeter_textColor, mTextColor);

        mMetricSize = typedArray.getDimension(R.styleable.CircularMeter_metricSize,
                getResources().getDimension(R.dimen.metric_size));
        mMetricText = typedArray.getString(R.styleable.CircularMeter_metricText);
        mMetricPaddingY = getResources().getDimension(R.dimen.metric_padding_y);

        mRange = typedArray.getInt(R.styleable.CircularMeter_range, 100);


        mOneWidth = getResources().getDimension(R.dimen.width);
        mTwoWidth = getResources().getDimension(R.dimen.width);
        mThreeWidth = getResources().getDimension(R.dimen.width);

        mOneColor = -1213350;
        mTwoColor = -7747644;
        mThreeColor = -1;

        mOneDegrees = 0;
        mTwoDegrees = 0;
        mThreeDegrees = 0;

        String aux = typedArray.getString(R.styleable.CircularMeter_typeface);
        if (aux != null)
            mTypeface = Typeface.createFromAsset(this.getResources()
                    .getAssets(), aux);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(mBackgroundCenter, mBackgroundCenter,
                mBackgroundRadius, mBackgroundPaint);

        canvas.drawArc(mOneBounds, START_DEGREES, mOneDegrees, false, mOnePaint);
        canvas.drawArc(mTwoBounds, START_DEGREES, mTwoDegrees, false, mTwoPaint);
        canvas.drawArc(mThreeBounds, START_DEGREES, mThreeDegrees, false,
                mThreePaint);

        canvas.drawText(Integer.toString(mOneValue), mOneBounds.centerX(),
                mTextPosY, mTextPaint);
        canvas.drawText(mMetricText, mOneBounds.centerX(), mMetricPosY,
                mMetricPaint);
    }


    public void setValues(int v1, int v2, int v3) {

        if (v1 <= mRange)
            mOneDegrees = Math.round(((float) v1 * 360) / mRange);
        else
            mOneDegrees = 360;

        if (v2 <= mRange)
            mTwoDegrees = Math.round(((float) v2 * 360) / mRange);
        else
            mTwoDegrees = 360;

        if (v3 <= mRange)
            mThreeDegrees = Math.round(((float) v3 * 360) / mRange);
        else
            mThreeDegrees = 360;

        mOneValue = v1;
        invalidate();
//        mSpinHandler.sendEmptyMessage(0);
    }


    public void setRange(int range) {
        mRange = range;
        invalidate();
    }


    public void setTextSize(float size) {
        mTextSize = size;
    }


    public void setMetricSize(float size) {
        mMetricSize = size;
    }


    public void setFirstColor(int color) {
        mOneColor = color;
        invalidate();
    }


    public void setSecondColor(int color) {
        mTwoColor = color;
        invalidate();
    }


    public void setThirdColor(int color) {
        mThreeColor = color;
        invalidate();
    }


    public void setTextColor(int color) {
        mTextColor = color;
    }


    public void setMetricText(String text) {
        mMetricText = text;
        invalidate();
    }


    @Override
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        invalidate();
    }


    public CircleCounter setTypeface(Typeface typeface) {
        mTypeface = typeface;
        return this;
    }


    private static class SpeedHandler extends Handler {

        private final CircleCounter act;


        public SpeedHandler(CircleCounter act) {
            super();
            this.act = act;
        }


        @Override
        public void handleMessage(Message msg) {
            act.invalidate();
            super.handleMessage(msg);
        }

    }

}
