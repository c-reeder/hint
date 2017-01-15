package com.wordpress.simpledevelopments.password;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 12/23/16.
 */

public class TextPagerAdapter extends PagerAdapter {

    private Context context;
    private List<String> textList;
    private static final String TAG = "TextPagerAdapter";
    private ViewGroup currentView;
    private boolean ready;
    private OnReadyListener listener;


    public TextPagerAdapter(Context context, List<String> textList) {
        this.context = context;
        this.textList = textList;
    }
    public TextPagerAdapter(Context context, String[] textList) {
        this.context = context;
        this.textList = new ArrayList<>(textList.length);
        for (int i = 0; i < textList.length; i++) {
            this.textList.add(textList[i]);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup newbie = (ViewGroup) inflater.inflate(R.layout.single_text, container, false);
        TextView singleTextView = (TextView) newbie.findViewById(R.id.singleTextView);
        singleTextView.setText(textList.get(position));
        container.addView(newbie);
        return newbie;
    }

    @Override
    public void destroyItem (ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }


    @Override
    public int getItemPosition(Object object) {
        if (textList.contains((View)object)) {
            return textList.indexOf((View)object);
        } else {
            return POSITION_NONE;
        }
    }

    @Override
    public int getCount() {
        return textList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        currentView = (ViewGroup) object;
        super.setPrimaryItem(container,position,object);
        if (!ready) {
            ready = true;
            listener.onTextPagerAdapterReady();
        }
    }
    public ViewGroup getCurrentView() {
        return currentView;
    }

    public interface OnReadyListener {
        public void onTextPagerAdapterReady();
    }
    public void setReadyListener(OnReadyListener listener) {
        this.listener = listener;
    }

}
