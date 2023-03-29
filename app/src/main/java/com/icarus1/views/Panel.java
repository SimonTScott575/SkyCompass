package com.icarus1.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.icarus1.R;

public class Panel extends CardView {

    private Animation slideInAnimation;
    private Animation slideOutAnimation;

    public Panel(Context context) {
        super(context);
        init(Direction.BOTTOM);
    }

    public Panel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Panel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        Direction direction;

        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.Panel,
            0, 0
        );

        try {
            int directionEnumIndex = a.getInteger(R.styleable.Panel_direction, 0);
            direction = Direction.values()[directionEnumIndex];
        } finally {
            a.recycle();
        }

        init(direction);

    }

    private void init(Direction direction) {

        LayoutInflater.from(getContext()).inflate(R.layout.view_panel, this);

        slideInAnimation = AnimationUtils.loadAnimation(getContext(), direction.toAnimIn());
        slideOutAnimation = AnimationUtils.loadAnimation(getContext(), direction.toAnimOut());

        slideInAnimation.setAnimationListener(new OnSlideInAnimation());
        slideOutAnimation.setAnimationListener(new OnSlideOutAnimation());

        findViewById(R.id.imageButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

    }

    public void show() {
        startAnimation(slideInAnimation);
    }

    public void hide() {
        startAnimation(slideOutAnimation);
    }

    class OnSlideInAnimation implements Animation.AnimationListener {

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

    class OnSlideOutAnimation implements Animation.AnimationListener {

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

    private enum Direction {

        BOTTOM,
        LEFT;

        public int toAnimIn() {
            switch(this) {
                case LEFT:
                    return R.anim.slide_in_right;
                case BOTTOM:
                    return R.anim.slide_in_up;
                default: return 0;
            }
        }
        public int toAnimOut() {
            switch(this) {
                case LEFT:
                    return R.anim.slide_out_left;
                case BOTTOM:
                    return R.anim.slide_out_down;
                default: return 0;
            }
        }

    }

}
