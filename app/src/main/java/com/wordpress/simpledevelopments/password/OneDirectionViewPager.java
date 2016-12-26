package com.wordpress.simpledevelopments.password;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * OneDirectionViewPager
 * Connor Reeder
 * 12/23/16.
 */

public class OneDirectionViewPager extends ViewPager {

    private static final String TAG = "OneDirectionViewPager";

    private float oldXVal;
    private GestureDetector gestureDetector;
    private SwypeController swypeController;

    public OneDirectionViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
           @Override
           public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
               float delta = e2.getX() - e1.getX();
               if (delta < 0) {
                   //Log.d(TAG, "Slide!!!");
                   //Flip to Next Word
                   if (swypeController.canSwype())
                       setCurrentItem(getCurrentItem() + 1, true);
                   else
                       Log.d(TAG, "Cannot skip word anymore!!!");
               }
               return false;
           }
        });
    }
    public void setSwypeController(SwypeController swypeController) {
        this.swypeController = swypeController;
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public interface SwypeController {
        public boolean canSwype ();
    }
}
