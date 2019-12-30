package dev.handcraftedsoftware.hint;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import dev.handcraftedsoftware.hint.R;

/**
 * A partial circle which appears in the left corner as a background to the round number.
 * By Connor Reeder
 */

public class ArcShapeView extends View {
    private int arcColor;
    private Paint paint;
    private RectF bounds;

    public ArcShapeView(Context context) {
        super(context);
        arcColor = Color.BLACK;
        init();
    }

    public ArcShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ArcShapeView,0,0);
        try {
            arcColor = arr.getColor(R.styleable.ArcShapeView_arcColor,Color.BLACK);
        } finally {
            arr.recycle();
        }
        init();
    }
    private void init() {
        paint = new Paint();
        paint.setColor(arcColor);
    }
    @Override
    protected void onSizeChanged(int width,
                                 int height,
                                 int oldWidth,
                                 int oldHeight) {
        bounds = new RectF(-width,-height,width,height);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(bounds,0,90,true,paint);
    }
}
