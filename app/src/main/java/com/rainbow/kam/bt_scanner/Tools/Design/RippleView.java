package com.rainbow.kam.bt_scanner.Tools.Design;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import com.rainbow.kam.bt_scanner.R;

/**
 * Created by sion on 2015-11-04.
 */
public class RippleView extends RelativeLayout {

    private int width;
    private int height;
    private int frameRate = 10;
    private int rippleDuration = 400;
    private int rippleAlpha = 90;
    private Handler rippleHandler;
    private int radiusMax = 0;
    private boolean animationRunning = false;
    private int timer = 0;
    private int timerEmpty = 0;
    private int durationEmpty = -1;
    private float x = -1;
    private float y = -1;
    private int zoomDuration;
    private float zoomScale;
    private ScaleAnimation scaleAnimation;
    private Boolean hasToZoom;
    private Boolean isCentered;
    private Integer rippleType;
    private Paint paint;
    private Bitmap originBitmap;
    private int rippleColor;
    private int ripplePadding;
    private GestureDetector gestureDetector;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };
//    private OnRippleCompleteListener onRippleCompleteListener;


    public RippleView(Context context) {
        super(context);
    }

    public RippleView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        init(context, attrSet);
    }

    public RippleView(Context context, AttributeSet attrSet, int defaultStyle) {
        super(context, attrSet, defaultStyle);
        init(context, attrSet);
    }

    private void init(final Context context, final AttributeSet attributeSet) {
        if (isInEditMode()) {
            return;
        }

        final TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RippleView);
    }
}
