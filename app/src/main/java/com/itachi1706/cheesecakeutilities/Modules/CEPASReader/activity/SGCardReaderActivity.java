package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.activity;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;

import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.SGCardReaderApplication;

import androidx.annotation.Nullable;

public abstract class SGCardReaderActivity extends Activity {
    int mAppliedTheme;

    protected Integer getThemeVariant() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Integer variant = getThemeVariant();
        int baseTheme = SGCardReaderApplication.chooseTheme();
        int theme;
        mAppliedTheme = baseTheme;
        if (variant != null) {
            TypedArray a = obtainStyledAttributes(
                    baseTheme,
                    new int[] { variant });

            theme = a.getResourceId(0, baseTheme);
            a.recycle();
        } else
            theme = baseTheme;
        setTheme(theme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (SGCardReaderApplication.chooseTheme() != mAppliedTheme)
            recreate();
    }
}
