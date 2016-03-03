package com.itachi1706.cheesecakeutilities.Updater.Util;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.itachi1706.cheesecakeutilities.R;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.Util in Cheesecake Utilities.
 */
public class NotifyUserUtil {
    public static void showShortDismissSnackbar(View currentLayout, String message){
        Snackbar.make(currentLayout, message, Snackbar.LENGTH_SHORT)
                .setAction(R.string.snackbar_action_dismiss, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    public static void createShortToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
