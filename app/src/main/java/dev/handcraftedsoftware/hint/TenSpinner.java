package dev.handcraftedsoftware.hint;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;


/**
 * Custom UI element which represents a spinnable wheel.
 * @author Connor Reeder
 */

public class TenSpinner extends View {
    private enum Gravity {
        Left,
        Center,
        Right,
        Top,
        Bottom
    }

    private int rDiameter;
    private Gravity hGravity;
    private Gravity vGravity;
    private int spinOffset;
    private Paint spinnerPaint;
    private Paint outlinePaint;
    private Paint textPaint;
    private float origX;
    private float origY;
    private int radius;


    private ValueAnimator valueAnimator;
    private ValueAnimator.AnimatorUpdateListener updateListener;
    private MyAnimatorListener animatorListener;
    private int offsetGoal;

    private RectF bounds;


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
        TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TenSpinner,0,0);
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
        Resources res = getResources();
        float textSize = res.getDimensionPixelSize(R.dimen.tenSpinnerText);
        float strokeWidth = res.getDimensionPixelSize(R.dimen.tenSpinnerStroke);
        textPaint.setTextSize(textSize);

        outlinePaint = new Paint();
        outlinePaint.setStrokeWidth(strokeWidth);
        outlinePaint.setColor(ContextCompat.getColor(getContext(), R.color.spinnerOutline));
        outlinePaint.setStyle(Paint.Style.STROKE);

        offsetGoal = 0;
        animatorListener = new MyAnimatorListener();
        updateListener = new MyUpdateListener();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewWidth;
        int viewHeight;
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
    public void onSizeChanged(int width,
                              int height,
                              int oldWidth,
                              int oldHeight) {
        bounds = new RectF(origX,origY - radius * 2,origX + radius * 4,origY + radius * 2);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float textHeight = metrics.bottom - metrics.top;

        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                spinnerPaint.setColor(ContextCompat.getColor(getContext(), R.color.spinnerFill));
            } else {
                spinnerPaint.setColor(Color.WHITE);
            }
            canvas.drawArc(bounds, spinOffset + i * 36 + 117,36,true, spinnerPaint);

        }

        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(spinOffset + i * 36 + 117);
            canvas.drawLine(origX + 2 * radius, origY, origX + 2 * radius + 2 * radius * (float)Math.cos(angle), origY + 2 * radius * (float)Math.sin(angle), outlinePaint);
        }


        canvas.drawCircle(origX + 2 * radius, origY, 2 * radius, outlinePaint);
        canvas.rotate(81 + spinOffset, origX + 2 * radius, origY);
        for (int i = 0; i < 10; i++) {
            String currText = "" + (i + 1);
            float textWidth = textPaint.measureText(currText);
            canvas.drawText(currText,origX + 2 * radius - (textWidth / 2),origY + (2 * radius) - (textHeight / 2), textPaint);
            canvas.rotate(36, origX + 2 * radius, origY);
        }


    }
    public void spinToNext() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            offsetGoal += 36;
        } else {
            offsetGoal = spinOffset + 36;
        }
        valueAnimator = ValueAnimator.ofInt(spinOffset,offsetGoal);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(updateListener);
        valueAnimator.addListener(animatorListener);
        valueAnimator.start();
    }
    public void resetSpinner() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
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
            valueAnimator = null;
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            spinOffset = (Integer) valueAnimator.getAnimatedValue();
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