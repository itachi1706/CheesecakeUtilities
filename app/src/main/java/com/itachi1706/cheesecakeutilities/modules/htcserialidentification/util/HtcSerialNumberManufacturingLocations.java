package com.itachi1706.cheesecakeutilities.modules.htcserialidentification.util;

/**
 * Created by Kenneth on 3/20/2016.
 * for com.itachi1706.cheesecakeutilities.Util in CheesecakeUtilities
 */
public enum HtcSerialNumberManufacturingLocations {
    HT("HT", "Hsinchu, Taiwan"),
    SH("SH", "Shanghai, China"),
    FA("FA", "Taiwan (FA)"),
    UNKNOWN("UNKNOWN", "Unknown Location");

    private String code, location;

    HtcSerialNumberManufacturingLocations(String code, String location){
        this.code = code;
        this.location = location;
    }

    public static HtcSerialNumberManufacturingLocations fromCode(String code){
        for (HtcSerialNumberManufacturingLocations m : HtcSerialNumberManufacturingLocations.values()){
            if (m.getCode().equals(code))
                return m;
        }
        return UNKNOWN;
    }

    public String getCode() {
        return code;
    }

    public String getLocation() {
        return location;
    }
}
