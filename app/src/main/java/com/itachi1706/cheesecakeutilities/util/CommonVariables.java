package com.itachi1706.cheesecakeutilities.util;

import static android.graphics.Color.rgb;

/**
 * Created by Kenneth on 3/19/2016.
 * for com.itachi1706.cheesecakeutilities.Util in CheesecakeUtilities
 */
public class CommonVariables {

    private CommonVariables() {
        throw new UnsupportedOperationException("Should not create instance of utility classes. Please use static variables and methods instead");
    }

    public static final String BASE_API_URL = "https://api.itachi1706.com/api/";
    public static final String BASE_SERVER_URL = BASE_API_URL + "appupdatechecker.php?action=androidretrievedata&packagename=";
    public static final String PERM_MAN_TAG = "PermMan";
}
