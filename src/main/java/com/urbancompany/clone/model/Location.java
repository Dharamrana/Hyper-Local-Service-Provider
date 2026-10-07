package com.urbancompany.clone.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class Location {
    @NotNull private Double latitude;
    @NotNull private Double longitude;
    private String address;      // full formatted address
    private String houseNo;      // e.g. "H.No 42, Lane 3"
    private String landmark;     // e.g. "Near Premnagar Market"
    private String pincode;      // e.g. "248007"
    private String city;         // e.g. "Premnagar, Dehradun"
    private String label;        // Home / Office / Other

    public Location() {}
    public Location(Double latitude, Double longitude) { this.latitude = latitude; this.longitude = longitude; }
    public Location(Double latitude, Double longitude, String address) {
        this.latitude = latitude; this.longitude = longitude; this.address = address; this.city = address;
    }
    public Location(Double latitude, Double longitude, String address, String pincode, String landmark) {
        this.latitude = latitude; this.longitude = longitude; this.address = address;
        this.pincode = pincode; this.landmark = landmark; this.city = address;
    }
    public Double getLatitude() { return latitude; } public void setLatitude(Double v) { this.latitude = v; }
    public Double getLongitude() { return longitude; } public void setLongitude(Double v) { this.longitude = v; }
    public String getAddress() { return address; } public void setAddress(String v) { this.address = v; }
    public String getHouseNo() { return houseNo; } public void setHouseNo(String v) { this.houseNo = v; }
    public String getLandmark() { return landmark; } public void setLandmark(String v) { this.landmark = v; }
    public String getPincode() { return pincode; } public void setPincode(String v) { this.pincode = v; }
    public String getCity() { return city; } public void setCity(String v) { this.city = v; }
    public String getLabel() { return label; } public void setLabel(String v) { this.label = v; }
}
