package com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects;

/**
 * Created by Kenneth on 3/12/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.AppRestore.Objects in CheesecakeUtilities
 */
public class RestoreAppsItemsFooter extends RestoreAppsItemsBase {
    private String fullpath, version;

    public RestoreAppsItemsFooter(String fullpath, String version) {
        super();
        this.fullpath = fullpath;
        this.version = version;
    }

    public String getFullpath() {
        return fullpath;
    }

    public void setFullpath(String fullpath) {
        this.fullpath = fullpath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
