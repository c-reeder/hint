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
    private SwipeController swipeController;

    public OneDirectionViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
           @Override
           public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
               float delta = e2.getX() - e1.getX();
               if (delta < 0) {
                   //Log.d(TAG, "Slide!!!");
                   //Flip to Next Word
                   if (swipeController.canSwipe()) {
                       setCurrentItem(getCurrentItem() + 1, true);
                        swipeController.onSwiped(getCurrentItem());
                   } else
                       Log.d(TAG, "Cannot swipe!!!");
               }
               return false;
           }
        });
    }
    public void setSwipeController(SwipeController swipeController) {
        this.swipeController = swipeController;
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * Interface definining the interaction between a OneDirectionViewPager and its containing Activity or Fragment
     */
    public interface SwipeController {
        /**
         * Determines whether swiping is currently allowed on this OneDirectionalViewPager
         * @return whether or not swiping is allowed
         */
        public boolean canSwipe();

        /**
         * Callback for when this OneDirectionViewPager has been swiped
         * @param newIndex the index of the OneDirectionViewPager after being swiped.
         */
        public void onSwiped(int newIndex);
    }
}
