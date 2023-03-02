package com.icarus1.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.icarus1.R;

public class Panel extends CardView {

    private Animation slideUpAnimation;
    private Animation slideDownAnimation;

    public Panel(Context context) {
        super(context);
        init();
    }

    public Panel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Panel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        LayoutInflater.from(getContext()).inflate(R.layout.view_panel, this);

        slideUpAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        slideDownAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);

        slideUpAnimation.setAnimationListener(new OnSlideUpAnimation());
        slideDownAnimation.setAnimationListener(new OnSlideDownAnimation());

        findViewById(R.id.imageButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

    }

    public void show() {
        startAnimation(slideUpAnimation);
    }

    public void hide() {
        startAnimation(slideDownAnimation);
    }

    class OnSlideUpAnimation implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            setClickable(true);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

    }

    class OnSlideDownAnimation implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            setClickable(false);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            setVisibility(INVISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

    }

}
