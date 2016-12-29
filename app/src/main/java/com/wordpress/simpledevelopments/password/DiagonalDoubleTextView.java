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
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.d(TAG, "onMeasure: " + parentWidth + ", " + parentHeight);

        metrics = paint.getFontMetrics();
        textHeight = metrics.bottom - metrics.top;
        textWidth = paint.measureText(text1) > paint.measureText(text2) ? paint.measureText(text1) : paint.measureText(text2);
        float desiredWidth = (.75f * textWidth) + (2 * textWidth);
        float desiredHeight = (.75f * textHeight) + (2 * textHeight);
        viewWidth = desiredWidth < parentWidth ? desiredWidth : parentWidth;
        viewHeight = desiredHeight < parentHeight ? desiredHeight :parentHeight;
        Log.d(TAG, "setMeasuredDimension: " + (int)viewWidth + ", " + (int)viewHeight);
        setMeasuredDimension((int)viewWidth,(int)viewHeight);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: " + canvas.getWidth() + ", " + canvas.getHeight());
        //canvas.drawLine(0,canvas.getHeight(),canvas.getWidth(),0,linePaint);

        //canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(), linePaint);
        //
        // linePaint.setTextAlign(Paint.Align.CENTER);
        //Log.d(TAG, "onDraw measureText(My Text): " + linePaint.measureText(text1));



        Log.d(TAG, "textWidth: " + textWidth + ", textHeight: " + textHeight);

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0,0,viewWidth,viewHeight,paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawText(text1, (.25f * textWidth), (.25f * textHeight) - metrics.top, paint);
        canvas.drawText(text2, .50f * textWidth + textWidth,.50f * textHeight + textHeight - metrics.top,paint);
        paint.setStrokeWidth(2);
        canvas.drawLine(0,viewHeight,viewWidth,0,paint);



        Log.d(TAG, "ascent: " + metrics.ascent);
        Log.d(TAG, "bottom: " + metrics.bottom);
        Log.d(TAG, "descent: " + metrics.descent);
        Log.d(TAG, "leading: " + metrics.leading);
        Log.d(TAG, "top: " + metrics.top);

    }

}
