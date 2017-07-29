package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects;

/**
 * Created by Kenneth on 28/7/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects in CheesecakeUtilities
 */

public class Record {

    private Long datetimeFrom, dateTimeTo;
    private Double mileageFrom, mileageTo;
    private String destination, purpose, vehicleNumber, vehicleId, vehicleClass;
    private Boolean trainingMileage;

    // Calculated fields
    private Double totalMileage;
    private Long totalTimeInMs;

    // Version Number
    private int version = -1;

    public Record() {

    }

    public Long getDatetimeFrom() {
        return datetimeFrom;
    }

    public void setDatetimeFrom(Long datetimeFrom) {
        this.datetimeFrom = datetimeFrom;
    }

    public Long getDateTimeTo() {
        return dateTimeTo;
    }

    public void setDateTimeTo(Long dateTimeTo) {
        this.dateTimeTo = dateTimeTo;
    }

    public Double getMileageFrom() {
        return mileageFrom;
    }

    public void setMileageFrom(Double mileageFrom) {
        this.mileageFrom = mileageFrom;
    }

    public Double getMileageTo() {
        return mileageTo;
    }

    public void setMileageTo(Double mileageTo) {
        this.mileageTo = mileageTo;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Boolean getTrainingMileage() {
        return trainingMileage;
    }

    public void setTrainingMileage(Boolean trainingMileage) {
        this.trainingMileage = trainingMileage;
    }

    public Double getTotalMileage() {
        return totalMileage;
    }

    private void setTotalMileage(Double totalMileage) {
        this.totalMileage = totalMileage;
    }

    public Boolean updateMileage() {
        this.setTotalMileage(this.mileageTo - this.mileageFrom);
        return true;
    }

    public Boolean updateTotalTime() {
        this.setTotalTimeInMs(this.dateTimeTo - this.datetimeFrom);
        this.totalTimeInMs = this.dateTimeTo - this.datetimeFrom;
        return true;
    }

    public Long getTotalTimeInMs() {
        return totalTimeInMs;
    }

    private void setTotalTimeInMs(Long totalTimeInMs) {
        this.totalTimeInMs = totalTimeInMs;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleClass() {
        return vehicleClass;
    }

    public void setVehicleClass(String vehicleClass) {
        this.vehicleClass = vehicleClass;
    }
}
