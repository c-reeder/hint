package com.wordpress.simpledevelopments.password;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by connor on 1/6/17.
 */

public class ArcShapeView extends View {
    private ArcShape arcShape;
    private ShapeDrawable shapeDrawable;
    private static final String TAG = "ArcShapeView";
    private int arcColor;
    private Paint paint;

    public ArcShapeView(Context context) {
        super(context);
        arcColor = Color.BLACK;
        init();
    }

    public ArcShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.getTheme().obtainStyledAttributes(attrs,R.styleable.ArcShapeView,0,0);
        try {
            arcColor = arr.getColor(R.styleable.ArcShapeView_arcColor,Color.BLACK);
        } finally {
            arr.recycle();
        }
        init();
    }
    private void init() {
        arcShape = new ArcShape(0,90);
        shapeDrawable = new ShapeDrawable(arcShape);
        paint = new Paint();
        paint.setColor(arcColor);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        RectF rectF = new RectF(-canvas.getWidth(),-canvas.getHeight(),canvas.getWidth(),canvas.getHeight());
        canvas.drawArc(rectF,0,90,true,paint);
    }

    public int getArcColor() {
        return arcColor;
    }

    public void setArcColor(int arcColor) {
        this.arcColor = arcColor;
        paint.setColor(arcColor);
        invalidate();
    }
}
