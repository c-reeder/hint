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
    float realWidth;
    float realHeight;
    float origX;
    float origY;
    Paint.FontMetrics metrics;

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
        float desiredWidth = (.75f * textWidth) + (2 * textWidth);
        float desiredHeight = (.75f * textHeight) + (2 * textHeight);
        realWidth = desiredWidth;
        realHeight = desiredHeight;


        if (widthMode == MeasureSpec.UNSPECIFIED) {
            Log.d(TAG, "Width UNSPECIFIED");
            viewWidth = desiredWidth;
        } else if (widthMode == MeasureSpec.EXACTLY) {
            Log.d(TAG, "Width EXACTLY");
            viewWidth = parentWidth;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            Log.d(TAG, "Width AT_MOST");
            viewWidth = desiredWidth < parentWidth ? desiredWidth : parentWidth;
        }

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            Log.d(TAG, "Height UNSPECIFIED");
            viewHeight = desiredHeight;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            Log.d(TAG, "Height EXACTLY");
            viewHeight = parentHeight;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            Log.d(TAG, "Height AT_MOST");
            viewHeight = desiredHeight < parentHeight ? desiredHeight : parentHeight;
        }

        //Do logic to center item if the width is larger than the "real" width
        if (viewWidth > realWidth) {
            switch (getTextAlignment()) {
                case TEXT_ALIGNMENT_CENTER:
                    //do main thing here using origX and origY
                    //also make sure to start using origX and origY in the onDraw body
                    origX = (viewWidth - realWidth) / 2;
                    break;
                case TEXT_ALIGNMENT_VIEW_START:
                case TEXT_ALIGNMENT_TEXT_START:
                    //left align
                    origX = 0;
                    break;
                case TEXT_ALIGNMENT_TEXT_END:
                case TEXT_ALIGNMENT_VIEW_END:
                    //right align
                    origX = viewWidth - realWidth;
                    break;
            }
        }

        //Do logic to center item if the height is larger than the "real" height
        if (viewHeight > realHeight) {
            //is this even necessary since this should be only horizontal??
        }

        Log.d(TAG, "setMeasuredDimension: " + (int)viewWidth + ", " + (int)viewHeight);
        setMeasuredDimension((int)viewWidth,(int)viewHeight);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.d(TAG, "onDraw: " + canvas.getWidth() + ", " + canvas.getHeight());

//        Log.d(TAG, "textWidth: " + textWidth + ", textHeight: " + textHeight);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(origX,origY,
                origX + (.75f * textWidth) + 2 * textWidth,
                origY + (.75f * textHeight) + 2 * textHeight,
                paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawText(text1,
                origX + (.25f * textWidth),
                origY + (.25f * textHeight) - metrics.top,
                paint);
        canvas.drawText(text2,
                origX + (.50f * textWidth) + textWidth,
                origY + (.50f * textHeight) + textHeight - metrics.top,
                paint);
        paint.setStrokeWidth(2);
        canvas.drawLine(origX,
                origY + (.75f * textHeight) + 2 * textHeight,
                origX + (.75f * textWidth) + 2 * textWidth,
                origY,
                paint);


    }

}
