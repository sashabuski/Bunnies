package com.HopIn;

import java.io.Serializable;

/**
 * PickupPt object used within ride object and used for rider to specify pickup point
 * before requesting ride.
 *
 */

public class PickupPt implements Serializable {

    private Double latitude;
    private Double longitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public PickupPt(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PickupPt() {}


}