

package com.rainbow.kam.bt_scanner.Tools.Design;

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

/**
 * @author Diogo Bernardino
 * @email mail@diogobernardino.com
 * @date 04/2014
 */
public class CircleCounter extends View {


    /**
     * View starts at 6 o'clock
     */
    private final static float START_DEGREES = 90;


    /**
     * Default background
     */
    private int mBackgroundCenter;
    private int mBackgroundRadius;


    /**
     * Current degrees
     */
    private int mOneDegrees;
    private int mTwoDegrees;
    private int mThreeDegrees;


    /**
     * Current real value
     */
    private int mOneValue = 0;


    /**
     * Range of view
     */
    private int mRange;


    /**
     * Thickness of flows
     */
    private float mOneWidth;
    private float mTwoWidth;
    private float mThreeWidth;


    /**
     * Size of text
     */
    private float mTextSize;
    private float mMetricSize;


    /**
     * Color of bars
     */
    private int mOneColor;
    private int mTwoColor;
    private int mThreeColor;


    /**
     * Color of text
     */
    private int mTextColor = -1;
    private int mBackgroundColor;


    /**
     * Paint objects
     */
    private Paint mOnePaint;
    private Paint mTwoPaint;
    private Paint mThreePaint;
    private Paint mBackgroundPaint;
    private Paint mTextPaint;
    private Paint mMetricPaint;


    /**
     * Bounds of each flow
     */
    private RectF mOneBounds;
    private RectF mTwoBounds;
    private RectF mThreeBounds;


    /**
     * Text position
     */
    private float mTextPosY;
    private float mMetricPosY;
    private float mMetricPaddingY;


    /**
     * Metric in use
     */
    private String mMetricText;


    /**
     * Typeface of text
     */
    private Typeface mTypeface;


    /**
     * Handler to update the view
     */
    private SpeedHandler mSpinHandler;



    @SuppressLint("Recycle")
    public CircleCounter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context.obtainStyledAttributes(attrs, R.styleable.CircularMeter));
    }


    /**
     * Setting up variables on attach
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mSpinHandler = new SpeedHandler(this);
        setupBounds();
        setupPaints();
        setupTextPosition();
    }


    /**
     * Free variables on detached
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mSpinHandler = null;
        mOnePaint = null;
        mOneBounds = null;
        mTwoPaint = null;
        mTwoBounds = null;
        mBackgroundPaint = null;
        mTextPaint = null;
        mMetricPaint = null;
    }


    /**
     * Set up paint variables to be used in onDraw method
     */
    private void setupPaints() {

        mOnePaint = new Paint();
        mOnePaint.setColor(mOneColor);
        mOnePaint.setAntiAlias(true);
        mOnePaint.setStyle(Style.STROKE);
        mOnePaint.setStrokeWidth(mOneWidth);

        mTwoPaint = new Paint();
        mTwoPaint.setColor(mTwoColor);
        mTwoPaint.setAntiAlias(true);
        mTwoPaint.setStyle(Style.STROKE);
        mTwoPaint.setStrokeWidth(mTwoWidth);

        mThreePaint = new Paint();
        mThreePaint.setColor(mThreeColor);
        mThreePaint.setAntiAlias(true);
        mThreePaint.setStyle(Style.STROKE);
        mThreePaint.setStrokeWidth(mThreeWidth);

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


    /**
     * Set the bounds of the bars.
     */
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


    /**
     * Setting up text position
     */
    private void setupTextPosition() {
        Rect textBounds = new Rect();
        mTextPaint.getTextBounds("1", 0, 1, textBounds);
        mTextPosY = mOneBounds.centerY() + (textBounds.height() / 2f);
        mMetricPosY = mTextPosY + mMetricPaddingY;
    }


    /**
     * Parse the attributes passed to the view and default values.
     */
    private void init(TypedArray a) {

        mTextSize = a.getDimension(R.styleable.CircularMeter_textSize,
                getResources().getDimension(R.dimen.textSize));
        mTextColor = a
                .getColor(R.styleable.CircularMeter_textColor, mTextColor);

        mMetricSize = a.getDimension(R.styleable.CircularMeter_metricSize,
                getResources().getDimension(R.dimen.metricSize));
        mMetricText = a.getString(R.styleable.CircularMeter_metricText);
        mMetricPaddingY = getResources().getDimension(R.dimen.metricPaddingY);

        mRange = a.getInt(R.styleable.CircularMeter_range, 100);

        mOneWidth = getResources().getDimension(R.dimen.width);
        mTwoWidth = getResources().getDimension(R.dimen.width);
        mThreeWidth = getResources().getDimension(R.dimen.width);

        mOneColor = -1213350;
        mTwoColor = -7747644;
        mThreeColor = -1;

        mOneDegrees = 0;
        mTwoDegrees = 0;
        mThreeDegrees = 0;

        String aux = a.getString(R.styleable.CircularMeter_typeface);
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



	/*
	 * Setters
	 *
	 */

    /**
     * Set the next values to be drawn
     * @param v1
     * @param v2
     * @param v3
     */
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



    public CircleCounter setRange(int range) {
        mRange = range;
        return this;
    }

    public CircleCounter setFirstWidth(float width) {
        mOneWidth = width;
        return this;
    }

    public CircleCounter setSecondWidth(float width) {
        mTwoWidth = width;
        return this;
    }

    public CircleCounter setThirdWidth(float width) {
        mThreeWidth = width;
        return this;
    }

    public CircleCounter setTextSize(float size) {
        mTextSize = size;
        return this;
    }

    public CircleCounter setMetricSize(float size) {
        mMetricSize = size;
        return this;
    }

    public CircleCounter setFirstColor(int color) {
        mOneColor = color;
        return this;
    }

    public CircleCounter setSecondColor(int color) {
        mTwoColor = color;
        return this;
    }

    public CircleCounter setThirdColor(int color) {
        mThreeColor = color;
        return this;
    }

    public CircleCounter setTextColor(int color) {
        mTextColor = color;
        return this;
    }

    public CircleCounter setMetricText(String text) {
        mMetricText = text;
        return this;
    }

    @Override
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    public CircleCounter setTypeface(Typeface typeface) {
        mTypeface = typeface;
        return this;
    }



    /**
     * Handles display invalidates
     */
    private static class SpeedHandler extends Handler {

        private CircleCounter act;

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

//package com.rainbow.kam.bt_scanner.Tools.Design;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.graphics.Typeface;
//import android.os.Handler;
//import android.os.Message;
//import android.util.AttributeSet;
//import android.view.View;
//
//import com.rainbow.kam.bt_scanner.R;
//
//import java.lang.reflect.Type;
//
///**
// * Created by kam6512 on 2015-11-13.
// */
//public class CircleCounter extends View {
//    private final static float START_DEGREES = 90;
//
//    private int backGroundCenter;
//    private int backGroundRadius;
//
//    private int oneDegrees;
//    private int twoDegrees;
//    private int threeDegrees;
//
//    private int oneValue = 0;
//
//    private int range;
//
//    private float oneWidth;
//    private float twoWidth;
//    private float threeWidth;
//
//    private float textSize;
//    private float metricSize;
//
//    private int oneColor;
//    private int twoColor;
//    private int threeColor;
//
//    private int textColor = -1;
//    private int backgroundColor;
//
//    private Paint onePaint;
//    private Paint twoPaint;
//    private Paint threePaint;
//    private Paint backgroundPaint;
//    private Paint textPaint;
//    private Paint metricPaint;
//
//    private RectF oneBounds;
//    private RectF twoBounds;
//    private RectF threeBounds;
//
//    private float textPositionY;
//    private float metricPositionY;
//    private float metricPaddingY;
//
//    private String metricText;
//
//    private Typeface typeface;
//
//    private SpeedHandler speedHandler;
//
//    @SuppressLint("Recycle")
//    public CircleCounter(Context context, AttributeSet attributeSet) {
//        super(context, attributeSet);
//        init(context.obtainStyledAttributes(attributeSet, R.styleable.CircularMeter));
//    }
//
//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        speedHandler = new SpeedHandler(this);
//
//        setupBounds();
//        setupPaint();
//        setupTextPosition();
//    }
//
//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//
//        speedHandler = null;
//        onePaint = null;
//        oneBounds = null;
//        twoPaint = null;
//        twoBounds = null;
//        backgroundPaint = null;
//        textPaint = null;
//        metricPaint = null;
//    }
//
//    private void setupPaint() {
//        onePaint = new Paint();
//        onePaint.setColor(oneColor);
//        onePaint.setAntiAlias(true);
//        onePaint.setStyle(Paint.Style.STROKE);
//        onePaint.setStrokeWidth(oneWidth);
//
//        twoPaint = new Paint();
//        twoPaint.setColor(twoColor);
//        twoPaint.setAntiAlias(true);
//        twoPaint.setStyle(Paint.Style.STROKE);
//        twoPaint.setStrokeWidth(twoWidth);
//
//        threePaint = new Paint();
//        threePaint.setColor(threeColor);
//        threePaint.setAntiAlias(true);
//        threePaint.setStyle(Paint.Style.STROKE);
//        threePaint.setStrokeWidth(threeWidth);
//
//        backgroundPaint = new Paint();
//        backgroundPaint.setColor(backgroundColor);
//        backgroundPaint.setAntiAlias(true);
//        backgroundPaint.setStyle(Paint.Style.FILL);
//
//        textPaint = new Paint();
//        textPaint.setColor(textColor);
//        textPaint.setStyle(Paint.Style.FILL);
//        textPaint.setAntiAlias(true);
//        textPaint.setTextSize(textSize);
//        textPaint.setTypeface(typeface);
//        textPaint.setTextAlign(Paint.Align.CENTER);
//
//        metricPaint = new Paint();
//        metricPaint.setColor(textColor);
//        metricPaint.setStyle(Paint.Style.FILL);
//        metricPaint.setAntiAlias(true);
//        metricPaint.setTextSize(metricSize);
//        metricPaint.setTypeface(typeface);
//        metricPaint.setTextAlign(Paint.Align.CENTER);
//    }
//
//    private void setupBounds() {
//        backGroundCenter = this.getLayoutParams().width / 2;
//        backGroundRadius = backGroundCenter - this.getPaddingTop();
//
//        oneBounds = new RectF(
//                this.getPaddingTop() + oneWidth / 2,
//                this.getPaddingLeft() + oneWidth / 2,
//                this.getLayoutParams().width - this.getPaddingRight() - oneWidth / 2,
//                this.getLayoutParams().height - this.getPaddingBottom() - oneWidth / 2);
//
//        twoBounds = new RectF(
//                this.getPaddingTop() + twoWidth / 2 + oneWidth,
//                this.getPaddingLeft() + twoWidth / 2 + oneWidth,
//                this.getLayoutParams().width - this.getPaddingRight() - twoWidth / 2 - oneWidth,
//                this.getLayoutParams().height - this.getPaddingBottom() - twoWidth / 2 - oneWidth);
//
//        threeBounds = new RectF(
//                this.getPaddingTop() + threeWidth / 2 + twoWidth + oneWidth,
//                this.getPaddingLeft() + threeWidth / 2 + twoWidth + oneWidth,
//                this.getLayoutParams().width - this.getPaddingRight() - threeWidth / 2 - twoWidth - oneWidth,
//                this.getLayoutParams().height - this.getPaddingBottom() - threeWidth / 2 - twoWidth - oneWidth);
//    }
//
//    private void setupTextPosition() {
//        Rect textBounds = new Rect();
//        textPaint.getTextBounds("1", 0, 1, textBounds);
//        textPositionY = oneBounds.centerY() + (textBounds.height() / 2f);
//        metricPositionY = textPositionY + metricPaddingY;
//    }
//
//    private void init(TypedArray typedArray) {
//        textSize = typedArray.getDimension(R.styleable.CircularMeter_textSize, getResources().getDimension(R.dimen.textSize));
//        textColor = typedArray.getColor(R.styleable.CircularMeter_textColor, textColor);
//        metricSize = typedArray.getDimension(R.styleable.CircularMeter_metricSize, getResources().getDimension(R.dimen.metricSize));
//        metricText = typedArray.getString(R.styleable.CircularMeter_metricText);
//        metricPaddingY = getResources().getDimension(R.dimen.metricPaddingY);
//
//        range = typedArray.getInt(R.styleable.CircularMeter_range, 100);
//
//        oneWidth = getResources().getDimension(R.dimen.width);
//        twoWidth = getResources().getDimension(R.dimen.width);
//        threeWidth = getResources().getDimension(R.dimen.width);
//
//        oneColor = -1213350;
//        twoColor = -77747644;
//        threeColor = -1;
//
//        oneDegrees = 0;
//        twoDegrees = 0;
//        threeDegrees = 0;
//
//        String aux = typedArray.getString(R.styleable.CircularMeter_typeface);
//        if (aux != null) {
//            typeface = Typeface.createFromAsset(this.getResources().getAssets(), aux);
//        }
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        canvas.drawCircle(backGroundCenter, backGroundCenter, backGroundRadius, backgroundPaint);
//
//        canvas.drawArc(oneBounds, START_DEGREES, oneDegrees, false, onePaint);
//        canvas.drawArc(twoBounds, START_DEGREES, twoDegrees, false, twoPaint);
//        canvas.drawArc(threeBounds, START_DEGREES, threeDegrees, false, threePaint);
//
//        canvas.drawText(Integer.toString(oneValue), oneBounds.centerX(), textPositionY, textPaint);
//        canvas.drawText(metricText, oneBounds.centerX(), metricPositionY, metricPaint);
//    }
//
//
//    public void setValues(int valueOne, int valueTwo, int valueThree) {
//        if (valueOne <= range) {
//            oneDegrees = Math.round(((float) valueOne * 360) / range);
//        } else {
//            oneDegrees = 360;
//        }
//        if (valueTwo <= range) {
//            twoDegrees = Math.round(((float) valueTwo * 360) / range);
//        } else {
//            twoDegrees = 360;
//        }
//        if (valueThree <= range) {
//            threeDegrees = Math.round(((float) valueThree * 360) / range);
//        } else {
//            threeDegrees = 360;
//        }
//    }
//
//
//    public CircleCounter setRange(int range) {
//        this.range = range;
//        return this;
//    }
//
//    public CircleCounter setFirstWidth(float width) {
//        this.oneWidth = width;
//        return this;
//    }
//
//    public CircleCounter setSecondWidth(float width) {
//        this.twoWidth = width;
//        return this;
//    }
//
//    public CircleCounter setThirdWidth(float width) {
//        this.threeWidth = width;
//        return this;
//    }
//
//    public CircleCounter setTextSize(float size) {
//        this.textSize = size;
//        return this;
//    }
//
//    public CircleCounter setMetricSize(float size) {
//        this.metricSize = size;
//        return this;
//    }
//
//    public CircleCounter setFirstColor(int color) {
//        this.oneColor = color;
//        return this;
//    }
//
//    public CircleCounter setSecondColor(int color) {
//        this.twoColor = color;
//        return this;
//    }
//
//    public CircleCounter setThirdColor(int color) {
//        this.threeColor = color;
//        return this;
//    }
//
//    public CircleCounter setTextColor(int color) {
//        this.textColor = color;
//        return this;
//    }
//
//    public CircleCounter setMetricText(String text) {
//        this.metricText = text;
//        return this;
//    }
//
//    @Override
//    public void setBackgroundColor(int color) {
//        this.backgroundColor = color;
//    }
//
//    public CircleCounter setTypeface(Typeface typeface) {
//        this.typeface = typeface;
//        return this;
//    }
//
//    private static class SpeedHandler extends Handler {
//        private CircleCounter circleCounter;
//
//        public SpeedHandler(CircleCounter circleCounter) {
//            super();
//            this.circleCounter = circleCounter;
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            circleCounter.invalidate();
//            super.handleMessage(msg);
//        }
//    }
//}
