package com.oleg.mahjongclubbooster.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

public class ViewAnimationUtil {

	private static ValueAnimator slideAnimator(View view, int start, int end) {

		ValueAnimator animator = ValueAnimator.ofInt(start, end);

		animator.addUpdateListener(valueAnimator -> {
			//Update Height
			int value = (Integer) valueAnimator.getAnimatedValue();
			ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
			layoutParams.height = value;
			view.setLayoutParams(layoutParams);
		});
		return animator;
	}

	public static void expand(View view) {
		view.setVisibility(View.VISIBLE);

		final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(widthSpec, heightSpec);

		ValueAnimator mAnimator = slideAnimator(view, 0, view.getMeasuredHeight());
		mAnimator.start();
	}

	public static void collapse(View view) {
		int finalHeight = view.getHeight();

		ValueAnimator mAnimator = slideAnimator(view, finalHeight, 0);

		mAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(@NonNull Animator animation) {
			}

			@Override
			public void onAnimationEnd(@NonNull Animator animator) {
				//Height=0, but it set visibility to GONE
				view.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationCancel(@NonNull Animator animation) {
			}

			@Override
			public void onAnimationRepeat(@NonNull Animator animation) {
			}
		});
		mAnimator.start();
	}
}
