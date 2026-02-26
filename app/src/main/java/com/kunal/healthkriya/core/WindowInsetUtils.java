package com.kunal.healthkriya.core;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class WindowInsetUtils {

    private WindowInsetUtils() {
    }

    public static void applySystemBarPadding(@NonNull View target,
                                             boolean includeTop,
                                             boolean includeBottom) {
        final int initialLeft = target.getPaddingLeft();
        final int initialTop = target.getPaddingTop();
        final int initialRight = target.getPaddingRight();
        final int initialBottom = target.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(target, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int topPadding = includeTop ? initialTop + bars.top : initialTop;
            int bottomPadding = includeBottom ? initialBottom + bars.bottom : initialBottom;
            view.setPadding(initialLeft, topPadding, initialRight, bottomPadding);
            return insets;
        });
        requestInsets(target);
    }

    public static void applyNavigationBarMargin(@NonNull View target, int extraBottomDp) {
        ViewGroup.LayoutParams layoutParams = target.getLayoutParams();
        if (!(layoutParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }

        final ViewGroup.MarginLayoutParams initialParams =
                (ViewGroup.MarginLayoutParams) layoutParams;
        final int initialLeft = initialParams.leftMargin;
        final int initialTop = initialParams.topMargin;
        final int initialRight = initialParams.rightMargin;
        final int initialBottom = initialParams.bottomMargin;
        final int extraBottomPx = Math.round(
                extraBottomDp * target.getResources().getDisplayMetrics().density
        );

        ViewCompat.setOnApplyWindowInsetsListener(target, (view, insets) -> {
            Insets navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams =
                        (ViewGroup.MarginLayoutParams) params;
                marginParams.leftMargin = initialLeft;
                marginParams.topMargin = initialTop;
                marginParams.rightMargin = initialRight;
                marginParams.bottomMargin = initialBottom + navInsets.bottom + extraBottomPx;
                view.setLayoutParams(marginParams);
            }
            return insets;
        });
        requestInsets(target);
    }

    private static void requestInsets(@NonNull View target) {
        if (target.isAttachedToWindow()) {
            ViewCompat.requestApplyInsets(target);
            return;
        }

        target.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View view) {
                view.removeOnAttachStateChangeListener(this);
                ViewCompat.requestApplyInsets(view);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View view) {
                // No-op
            }
        });
    }
}
