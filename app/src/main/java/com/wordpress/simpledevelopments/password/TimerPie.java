package com.wordpress.simpledevelopments.password;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
/**
 * A grey circle that shoes how much time is left as a form of timer.
 * Created by connor on 5/23/17.
 */

public class TimerPie extends View {

    private static final String TAG = "TimerPie";

    private Paint piePaint;
    private int angle;
    private int color;

    private TimerListener timerListener;
    private RectF bounds;

    public TimerPie(Context context) {
        super(context);
        color = Color.GRAY;
        init();
    }
    public TimerPie(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TimerPie,
                0, 0);
        color = a.getColor(R.styleable.TimerPie_color, Color.GRAY);
        init();
    }
    private void init() {
        piePaint = new Paint();
        piePaint.setColor(color);
        angle = 360;

    }
    @Override
    public void onSizeChanged(int width,
                              int height,
                              int oldWidth,
                              int oldHeight) {
        bounds = new RectF(0,0,width,height);
    }
    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawArc(bounds,270,-1 * angle,true,piePaint);
    }


    interface TimerListener {
        void onTimerComplete();
    }

    public void setTimerListener(TimerListener timerListener) {
        this.timerListener = timerListener;
    }

    public void setAngle(int angle) {
        this.angle = angle;
        invalidate();
    }
}
