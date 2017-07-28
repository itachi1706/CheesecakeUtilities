package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kenneth on 28/7/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker.Objects in CheesecakeUtilities
 */

public class Records {

    private List<Record> records;

    public Records() {

    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<Record> records) {
        this.records = records;
    }

    private class Record {

        private Long datetime;
        private Double mileageFrom, mileageTo;
        private String destination, purpose, vehicleNumber;
        private Boolean trainingMileage;

        public Record() {

        }

        public Long getDatetime() {
            return datetime;
        }

        public void setDatetime(Long datetime) {
            this.datetime = datetime;
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
    }
}
