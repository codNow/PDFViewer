package com.sasha.pdfviewer.utils;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

public class SnakeBarUtils {

    @SuppressLint("RestrictedApi")
    public static void showSnackbar(View rootView, String message, int duration) {
        Snackbar snackbar = Snackbar.make(rootView, message, duration);

        View snackbarView = snackbar.getView();
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbarView;

        // Set Snackbar layout params
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbarView.getLayoutParams();

        // Set Snackbar layout params
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        layout.setLayoutParams(params);

        snackbar.show();
    }

}
