package com.icarus1.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.icarus1.R;
import com.icarus1.util.Debug;

public class Panel extends CardView {

    private Animation slideInAnimation;
    private Animation slideOutAnimation;

    public Panel(Context context) {
        super(context);
        init(context, null);
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

        LayoutInflater.from(context).inflate(R.layout.view_panel, this);
        findViewById(R.id.imageButton).setOnClickListener(v -> hide());

        if (attrs != null) {
            try(
                TypedArray b = context.getTheme().obtainStyledAttributes(
                    attrs, new int[]{R.attr.backgroundColor}, 0, 0
                )
            ) {
                setCardBackgroundColor(b.getColor(0, 0));
            }

            Direction direction = Direction.BOTTOM;
            try(
                TypedArray a = context.getTheme().obtainStyledAttributes(
                        attrs,
                        R.styleable.Panel,
                        0, 0
                )
            ) {
                int directionEnumIndex = a.getInteger(R.styleable.Panel_direction, 0);
                direction = Direction.values()[directionEnumIndex];
            }
            setDirection(direction);
        } else {
            setDirection(Direction.BOTTOM);
        }

    }

    public void setDirection(Direction direction) {

        slideInAnimation = AnimationUtils.loadAnimation(getContext(), direction.toAnimIn());
        slideOutAnimation = AnimationUtils.loadAnimation(getContext(), direction.toAnimOut());

        slideInAnimation.setAnimationListener(new OnSlideInAnimation());
        slideOutAnimation.setAnimationListener(new OnSlideOutAnimation());

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

    public enum Direction {

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
