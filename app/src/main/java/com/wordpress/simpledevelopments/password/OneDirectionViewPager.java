package com.wordpress.simpledevelopments.password;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * OneDirectionViewPager
 * Connor Reeder
 * 12/23/16.
 */

public class OneDirectionViewPager extends ViewPager {

    private static final String TAG = "OneDirectionViewPager";

    private float oldXVal;

    public OneDirectionViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent (MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            oldXVal = event.getX();
            return super.onTouchEvent(event);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float delta = event.getX() - oldXVal;
            if (delta > 0) {
                //Swipe Backwards
                return false;
            } else if (delta < 0) {
                //Swipe Forwards
                return super.onTouchEvent(event);
            } else {
                //Tap or something non-moving
                return false;
            }
        }
        return super.onTouchEvent(event);
    }
}
