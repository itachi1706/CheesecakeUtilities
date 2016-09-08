package com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator.JsonObjects;

import com.google.gson.JsonObject;

/**
 * Created by Kenneth on 7/9/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator.JsonObjects in CheesecakeUtilities
 */
public class Main {

    private JsonObject ageRangeText, pass;
    private Gender dataMale, dataFemale;

    public JsonObject getAgeRangeText() {
        return ageRangeText;
    }

    public JsonObject getPass() {
        return pass;
    }

    public Gender getDataMale() {
        return dataMale;
    }

    public Gender getDataFemale() {
        return dataFemale;
    }
}
