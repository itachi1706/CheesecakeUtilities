package com.itachi1706.cheesecakeutilities.Objects;

/**
 * Created by Kenneth on 29/4/2017.
 * for com.itachi1706.cheesecakeutilities.Objects in CheesecakeUtilities
 */

public class DualLineString {

    private String main, sub;

    public DualLineString(String main, String sub) {
        this.main = main;
        this.sub = sub;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }
}
