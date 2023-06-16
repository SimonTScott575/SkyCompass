package com.skycompass.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;

public class ShowHideAnimation extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private static final float MAX_SCALE = 1.2f;
    private static final float START_BOUNCE = 1f - (MAX_SCALE-1f);

    private View view;
    private ShowHide showHide;
    private boolean showing;

    public ShowHideAnimation(View view) {
        this.view = view;
        this.setFloatValues(0f,1f);
        setDuration(300);
        addUpdateListener(this);
        addListener(this);
    }

    public void show() {
        if (!showing && !this.isRunning()) {
            showHide = ShowHide.SHOW;
            start();
        }
    }

    public void hide() {
        if (showing && !this.isRunning()) {
            showHide = ShowHide.HIDE;
            start();
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {

        float fraction = animation.getAnimatedFraction();

        view.setAlpha(showHide == ShowHide.SHOW ? fraction : 1f - fraction);

        float scale = showHide == ShowHide.SHOW ? fraction : 1f - fraction;
        if (scale > START_BOUNCE) {
            scale = MAX_SCALE - (scale - START_BOUNCE);
        } else {
            scale = MAX_SCALE * scale/START_BOUNCE;
        }
        view.setScaleX(scale);
        view.setScaleY(scale);

    }

    @Override
    public void onAnimationStart(Animator animation) {
        view.setAlpha(showHide == ShowHide.SHOW ? 1f : 0f);
        view.setScaleX(showHide == ShowHide.SHOW ? 1f : 0f);
        view.setScaleY(showHide == ShowHide.SHOW ? 1f : 0f);
        view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        showing = showHide == ShowHide.SHOW;
        view.setVisibility(showing ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    public enum ShowHide {
        SHOW,
        HIDE
    }

}
