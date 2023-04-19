package com.icarus1.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.icarus1.R;
import com.icarus1.databinding.ViewPanelBinding;

public class Panel extends CardView {

    private ViewPanelBinding binding;
    private Animation slideInAnimation;
    private Animation slideOutAnimation;

    public Panel(Context context) {
        super(context);
        init(context, null, 0);
    }

    public Panel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public Panel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }



    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        setClickable(true);

        LayoutInflater.from(context).inflate(R.layout.view_panel, this);
        binding = ViewPanelBinding.inflate(LayoutInflater.from(context), this, true);
        binding.viewPanelClose.findViewById(R.id.view_panel_close).setOnClickListener(v -> hide());

        if (attrs != null) {
            try(
                TypedArray b = context.getTheme().obtainStyledAttributes(
                    attrs, new int[]{R.attr.cardBackgroundColor}, defStyleAttr, R.style.view_panel_default
                )
            ) {
                setCardBackgroundColor(b.getColor(0, 0));
            }

            Direction direction = Direction.BOTTOM;
            try(
                TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.Panel,
                    defStyleAttr, R.style.view_panel_default
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

    private void blockInteraction(boolean block) {
        binding.viewPanelClickBlocker.setClickable(block);
    }

    class OnSlideInAnimation implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            blockInteraction(false);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

    }

    class OnSlideOutAnimation implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            blockInteraction(true);
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
