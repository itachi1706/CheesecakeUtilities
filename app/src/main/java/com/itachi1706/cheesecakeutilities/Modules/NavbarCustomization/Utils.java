package com.itachi1706.cheesecakeutilities.Modules.NavbarCustomization;

import android.os.Build;

class Utils {

    static final boolean IS_AT_LEAST_MARSHMALLOW = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    static final String NAVBAR_SHOW_CLOCK = "navbar_clock";
    static final String NAVBAR_SHOW_IMAGE = "navbar_image";
    static final String NAVBAR_SHOW_APPNAME = "navbar_appname";
}
