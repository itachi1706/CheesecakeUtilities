package com.itachi1706.cheesecakeutilities.huh.modules.navbarCustomization;

import android.os.Build;

class Utils {

    static final boolean IS_AT_LEAST_MARSHMALLOW = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    static final boolean IS_OREO_AND_ABOVE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    static final String NAVBAR_SHOW_CLOCK = "navbar_clock";
    static final String NAVBAR_SERVICE_ENABLED = "service_enabled";
    static final String NAVBAR_SHOW_IMAGE = "navbar_image";
    static final String NAVBAR_SHOW_IMAGE_TYPE = "navbar_image_type";
    static final String NAVBAR_SHOW_APPNAME = "navbar_appname";
    static final String NAVBAR_SHOW_STATIC_COLOR = "navbar_static_color";
    static final String NAVBAR_IMAGE_TYPE_RANDOM_IMG = "random";
    static final String NAVBAR_IMAGE_TYPE_APP = "app";
    static final String NAVBAR_IMAGE_TYPE_STATIC = "static";
}
