package com.wordpress.simpledevelopments.password;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by connor on 1/5/17.
 */

public class TenSpinner extends View {

    private static final String TAG = "TenSpinner";

    private static final int[] COLORS = {Color.BLUE, Color.CYAN, Color.DKGRAY,Color.GRAY,Color.GREEN,
                                        Color.LTGRAY,Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW};
    private enum Gravity {
        Left,
        Center,
        Right,
        Top,
        Bottom
    }

    private int viewWidth;
    private int viewHeight;
    private int rDiameter;
    private Gravity hGravity;
    private Gravity vGravity;
    private int spinOffset;
    private Paint spinnerPaint;
    private Paint textPaint;
    private float origX;
    private float origY;
    private int radius;


    private ValueAnimator valueAnimator;
    private ValueAnimator.AnimatorUpdateListener updateListener;
    private MyAnimatorListener animatorListener;
    private int pendingNextSpinCount;


    public TenSpinner(Context context) {
        super(context);
        rDiameter = 15;
        hGravity = Gravity.Left;
        vGravity = Gravity.Top;
        spinOffset = 0;
        init();
    }

    public TenSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.getTheme().obtainStyledAttributes(attrs,R.styleable.TenSpinner,0,0);
        try {
            rDiameter = arr.getDimensionPixelSize(R.styleable.TenSpinner_diameter, -1);
            hGravity = Gravity.values()[arr.getInt(R.styleable.TenSpinner_horizontalGravity, 0)];
            vGravity = Gravity.values()[arr.getInt(R.styleable.TenSpinner_verticalGravity, 3)];
            spinOffset = 0;
        } finally {
            arr.recycle();
        }
        init();
    }
    private void init() {
        spinnerPaint = new Paint();
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(100);
        pendingNextSpinCount = 0;
        animatorListener = new MyAnimatorListener();
        updateListener = new MyUpdateListener();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Measure Width
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (rDiameter < 0) {
            viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            int desiredWidth = getPaddingLeft() + getPaddingRight() + rDiameter;
            if (widthMode == MeasureSpec.EXACTLY) {
                viewWidth = MeasureSpec.getSize(widthMeasureSpec);
            } else if (widthMode == MeasureSpec.AT_MOST) {
                viewWidth = desiredWidth < MeasureSpec.getSize(widthMeasureSpec) ? desiredWidth : MeasureSpec.getSize(widthMeasureSpec);
            } else { //widthMode == MeasureSpec.UNSPECIFIED
                viewWidth = desiredWidth;
            }
        }


        //Measure Height
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (rDiameter < 0) {
            viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            int desiredHeight = getPaddingBottom() + getPaddingTop() + rDiameter;
            if (heightMode == MeasureSpec.EXACTLY) {
                viewHeight = MeasureSpec.getSize(heightMeasureSpec);
            } else if (heightMode == MeasureSpec.AT_MOST) {
                viewHeight = desiredHeight < MeasureSpec.getSize(heightMeasureSpec) ? desiredHeight : MeasureSpec.getSize(heightMeasureSpec);
            } else {
                viewHeight = desiredHeight;
            }
        }


        // Calculate values for spinner itself
        int hSpace = viewWidth - getPaddingLeft() - getPaddingRight();
        int vSpace = viewHeight - getPaddingTop() - getPaddingBottom();

        int possWidth;
        int possHeight;
        if (rDiameter < 0) {
            possWidth = hSpace;
            possHeight = vSpace;
        } else {
            possWidth = rDiameter < hSpace ? rDiameter : hSpace;
            possHeight = rDiameter < vSpace ? rDiameter : vSpace;
        }

        int actualDiameter = possWidth < possHeight ? possWidth : possHeight;
        radius = actualDiameter / 2;

        origX = getPaddingLeft();
        origY = getPaddingTop();

        if (actualDiameter < hSpace) {
            switch (hGravity) {
                case Left:
                    break;
                case Center:
                    origX += (hSpace - actualDiameter) / 2;
                    break;
                case Right:
                    origX += hSpace - actualDiameter;
                    break;
            }
        }
        if (actualDiameter < vSpace) {
            switch (vGravity) {
                case Top:
                    break;
                case Center:
                    origY += (vSpace - actualDiameter) / 2;
                    break;
                case Bottom:
                    origY += vSpace - actualDiameter;
                    break;
            }
        }



        setMeasuredDimension(viewWidth,viewHeight);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawCircle(origX + radius,origY + radius, radius,spinnerPaint);
        RectF rectF = new RectF(origX,origY - radius * 2,origX + radius * 4,origY + radius * 2);
        //RectF rectF = new RectF(origX,origY,origX + radius * 2,origY + radius * 2);

        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float textHeight = metrics.bottom - metrics.top;

        for (int i = 0; i < 10; i++) {
            spinnerPaint.setColor(COLORS[i]);
            canvas.drawArc(rectF,spinOffset + i * 36 + 117,36,true, spinnerPaint);
            //canvas.drawArc(rectF,spinOffset + i * 36 + 72,36,true, spinnerPaint);
        }

        canvas.rotate(81 + spinOffset, origX + 2 * radius, origY);
        for (int i = 0; i < 10; i++) {
            String currText = "" + (i + 1);
            float textWidth = textPaint.measureText(currText);
            canvas.drawText(currText,origX + 2 * radius - (textWidth / 2),origY + 2 * radius - textHeight, textPaint);
            canvas.rotate(36, origX + 2 * radius, origY);
        }


    }
    public void spinToNext() {
        if (valueAnimator != null) {
            pendingNextSpinCount++;
        } else {
            valueAnimator = ValueAnimator.ofInt(spinOffset,spinOffset + 36);
            valueAnimator.setDuration(1000);
            valueAnimator.addUpdateListener(updateListener);
            valueAnimator.addListener(animatorListener);
            valueAnimator.start();
        }

    }
    public void resetSpinner() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            pendingNextSpinCount = 0;
        }
        valueAnimator = ValueAnimator.ofInt(spinOffset,0);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(updateListener);
        valueAnimator.addListener(animatorListener);
        valueAnimator.start();
    }
    public void setSpinner(int value) {

        if (valueAnimator != null) {
            valueAnimator.cancel();
            pendingNextSpinCount = 0;
            valueAnimator = null;
        }

        int index = 10 - (value % 10);

        if (index == 10) {
            resetSpinner();
            return;
        }

        spinOffset = index * 36;
        invalidate();
    }
    private class MyAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (pendingNextSpinCount > 0) {
                pendingNextSpinCount--;
                valueAnimator = ValueAnimator.ofInt(spinOffset,spinOffset + 36);
                valueAnimator.setDuration(1000);
                valueAnimator.addUpdateListener(updateListener);
                valueAnimator.addListener(animatorListener);
                valueAnimator.start();
            } else {
                valueAnimator = null;
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
    private class MyUpdateListener implements  ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            spinOffset = (Integer) valueAnimator.getAnimatedValue();
            invalidate();
        }
    }
}
