package com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects;

/**
 * Created by Kenneth on 3/12/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects in CheesecakeUtilities
 */
public class RestoreAppsItemsBase {

    private boolean isExpanded = false;

    public RestoreAppsItemsBase(){}

    public RestoreAppsItemsBase(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setIsExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }
}
