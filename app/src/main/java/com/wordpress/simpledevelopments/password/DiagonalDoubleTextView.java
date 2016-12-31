package com.wordpress.simpledevelopments.password;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by connor on 12/27/16.
 */

public class DiagonalDoubleTextView extends View {

    private static final String TAG = "DiagonalDoubleTextView";

    private String text1;
    private String text2;

    private int textSize;

    Paint paint;
    float textWidth;
    float textHeight;
    float viewWidth;
    float viewHeight;
    Paint.FontMetrics metrics;
    boolean exactWidth;
    boolean exactHeight;

    public DiagonalDoubleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.getTheme().obtainStyledAttributes(attrs,R.styleable.DiagonalDoubleTextView,0,0);
        try {
            text1 = arr.getString(R.styleable.DiagonalDoubleTextView_text1);
            if (text1 == null)
                text1 = "text1";
            text2 = arr.getString(R.styleable.DiagonalDoubleTextView_text2);
            if (text2 == null)
                text2 = "text2";
            textSize = arr.getDimensionPixelSize(R.styleable.DiagonalDoubleTextView_textSize,50);
        } finally {
            arr.recycle();
        }
        init();
    }
    public DiagonalDoubleTextView(Context context, String text1, String text2) {
        this(context,text1,text2,50);
    }
    public DiagonalDoubleTextView(Context context, String text1, String text2, int textSize) {
        super(context);
        this.text1 = text1;
        this.text2 = text2;
        this.textSize = textSize;
        init();
    }
    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);
        paint.setTextSize(textSize);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.d(TAG, "onMeasure: " + parentWidth + ", " + parentHeight);

        metrics = paint.getFontMetrics();
        textHeight = metrics.bottom - metrics.top;
        textWidth = paint.measureText(text1) > paint.measureText(text2) ? paint.measureText(text1) : paint.measureText(text2);
        float minWidth = (.75f * textWidth) + (2 * textWidth);
        float minHeight = (.75f * textHeight) + (2 * textHeight);


        if (widthMode == MeasureSpec.UNSPECIFIED) {
            Log.d(TAG, "Width UNSPECIFIED");
            viewWidth = minWidth;
        } else if (widthMode == MeasureSpec.EXACTLY) {
            Log.d(TAG, "Width EXACTLY");
            viewWidth = parentWidth;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            Log.d(TAG, "Width AT_MOST");
            viewWidth = minWidth < parentWidth ? minWidth : parentWidth;
        }

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            Log.d(TAG, "Height UNSPECIFIED");
            viewHeight = minHeight;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            Log.d(TAG, "Height EXACTLY");
            viewHeight = parentHeight;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            Log.d(TAG, "Height AT_MOST");
            viewHeight = minHeight < parentHeight ? minHeight : parentHeight;
        }

        //Do logic to center item if the width is larger than the "real" width
        if (viewWidth != minWidth) {
            exactWidth = true;
        } else {
            exactWidth = false;
        }

        //Do logic to center item if the height is larger than the "real" height
        if (viewHeight != minHeight) {
            //is this even necessary since this should be only horizontal??
            exactHeight = true;
        } else {
            exactHeight = false;
        }

        Log.d(TAG, "setMeasuredDimension: " + (int)viewWidth + ", " + (int)viewHeight);
        setMeasuredDimension((int)viewWidth,(int)viewHeight);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        // Draw Outer-bounding Rectangle
//        paint.setStyle(Paint.Style.STROKE);
//        canvas.drawRect(0,0,
//                viewWidth,
//                viewHeight,
//                paint);

        float x1 = 0;
        float y1 = 0;
        float x2 = 0;
        float y2 = 0;

        if (exactWidth) {
            paint.setTextAlign(Paint.Align.CENTER);
            x1 = (.25f * viewWidth);
            x2 = (.75f * viewWidth);
        } else {
            paint.setTextAlign(Paint.Align.LEFT);
            x1 = (.25f * textWidth);
            x2 = (.50f * textWidth) + textWidth;
        }

        if (exactHeight) {
            y1 = (.25f * viewHeight) + (textHeight / 2);
            y2 = (.75f * viewHeight) + (textHeight / 2);
        } else {
            y1 = (.25f * textHeight) - metrics.top;
            y2 = (.50f * textHeight) + textHeight - metrics.top;
        }

        // Draw Text 1
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawText(text1,
                x1,
                y1,
                paint);

        // Draw Text 2
        canvas.drawText(text2,
                x2,
                y2,
                paint);

        // Draw diagonal line
        paint.setStrokeWidth(2);
        canvas.drawLine(0,
                viewHeight,
                viewWidth,
                0,
                paint);
    }

}
