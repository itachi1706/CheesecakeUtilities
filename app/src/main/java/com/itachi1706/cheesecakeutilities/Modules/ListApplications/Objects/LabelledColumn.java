package com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects;

/**
 * Created by Kenneth on 1/10/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects in CheesecakeUtilities
 */

public class LabelledColumn {

    private String label, field;

    public LabelledColumn(String label, String field) {
        this.label = label;
        this.field = field;
    }

    public LabelledColumn(String label, int field) {
        this.label = label;
        this.field = field + "";
    }

    public String getLabel() {
        return label;
    }

    public String getField() {
        return field;
    }
}
