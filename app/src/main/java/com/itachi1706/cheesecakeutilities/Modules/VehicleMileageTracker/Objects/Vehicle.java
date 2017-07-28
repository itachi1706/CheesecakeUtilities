package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects;

/**
 * Created by Kenneth on 28/7/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects in CheesecakeUtilities
 */

public class Vehicle {

    private String vehicleClass; // Refer to VehicleClass class
    private String name, shortname;

    public Vehicle() {}

    public String getVehicleClass() {
        return vehicleClass;
    }

    public void setVehicleClass(String vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }
}
