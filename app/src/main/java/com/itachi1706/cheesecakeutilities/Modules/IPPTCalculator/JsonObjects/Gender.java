package com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator.JsonObjects;

import com.google.gson.JsonObject;

/**
 * Created by Kenneth on 7/9/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator.JsonObjects in CheesecakeUtilities
 */
public class Gender {
    private JsonObject run, situps, pushups;

    public JsonObject getRun() {
        return run;
    }

    public JsonObject getSitups() {
        return situps;
    }

    public JsonObject getPushups() {
        return pushups;
    }
}