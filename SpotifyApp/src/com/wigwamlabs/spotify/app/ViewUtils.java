package com.wigwamlabs.spotify.app;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

final class ViewUtils {
    public static void hideSoftInput(View anyView) {
        final Context context = anyView.getContext();
        final InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(anyView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showSoftInput(View view) {
        final Context context = view.getContext();
        final InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
