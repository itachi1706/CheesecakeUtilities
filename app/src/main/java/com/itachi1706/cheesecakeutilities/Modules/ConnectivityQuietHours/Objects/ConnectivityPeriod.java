package com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Objects;

/**
 * Created by Kenneth on 28/4/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.ConnectivityQuietHours.Objects in CheesecakeUtilities
 */

public class ConnectivityPeriod {

    int startHr, startMin, endHr, endMin;

    public ConnectivityPeriod(int startHr, int startMin, int endHr, int endMin) {
        this.startHr = startHr;
        this.startMin = startMin;
        this.endHr = endHr;
        this.endMin = endMin;
    }

    public ConnectivityPeriod(String serializedObject) {
        String[] tmp = serializedObject.split(":");
        int tmpSize = tmp.length;
        this.endHr = 0;
        this.endMin = 0;
        this.startHr = 0;
        this.startMin = 0;
        if (tmpSize >= 4) this.endMin = Integer.parseInt(tmp[3]);
        if (tmpSize >= 3) this.endHr = Integer.parseInt(tmp[2]);
        if (tmpSize >= 2) this.startMin = Integer.parseInt(tmp[1]);
        if (tmpSize >= 1) this.startHr = Integer.parseInt(tmp[0]);
    }

    public ConnectivityPeriod(int startHr, int startMin) {
        this.startHr = startHr;
        this.startMin = startMin;
        this.endHr = 0;
        this.endMin = 0;
    }

    public int getStartHr() {
        return startHr;
    }

    public void setStartHr(int startHr) {
        this.startHr = startHr;
    }

    public int getStartMin() {
        return startMin;
    }

    public void setStartMin(int startMin) {
        this.startMin = startMin;
    }

    public int getEndHr() {
        return endHr;
    }

    public void setEndHr(int endHr) {
        this.endHr = endHr;
    }

    public int getEndMin() {
        return endMin;
    }

    public void setEndMin(int endMin) {
        this.endMin = endMin;
    }

    public String toString() {
        return startHr + ":" + startMin + " - " + endHr + ":" + endMin;
    }

    public String serialize() {
        return startHr + ":" + startMin + ":" + endHr + ":" + endMin;
    }
}
