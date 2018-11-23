package com.maps.subwaytransit.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CollapseBehavior<V extends ViewGroup> extends CoordinatorLayout.Behavior<V>{
    public CollapseBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        if (isBottomSheet(dependency)) {
            BottomSheetBehavior behavior = ((BottomSheetBehavior) ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior());

            int peekHeight = behavior.getPeekHeight();
            // The default peek height is -1, which
            // gets resolved to a 16:9 ratio with the parent
            final int actualPeek = peekHeight >= 0 ? peekHeight : (int) (((parent.getHeight() * 1.0) / (16.0)) * 9.0);
            if (dependency.getTop() >= actualPeek) {
                // Only perform translations when the
                // view is between "hidden" and "collapsed" states
                final int dy = dependency.getTop() - parent.getHeight();
                ViewCompat.setTranslationY(child, dy/2);
                return true;
            }
        }

        return false;
    }

    private static boolean isBottomSheet(@NonNull View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof CoordinatorLayout.LayoutParams) {
            return ((CoordinatorLayout.LayoutParams) lp).getBehavior() instanceof BottomSheetBehavior;
        }

        return false;
    }
}