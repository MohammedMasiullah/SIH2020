package com.example.safetyappv1;

public class CreateUser {

    public String name,password,code,email,isSharing,UserId,phone;
    public double latitude;
    public double longitude;

    public CreateUser(String name, String password, String code, String email, String isSharing,String phone, double latitude, double longitude,String userId) {
        this.name = name;
        this.password = password;
        this.code = code;
        this.email = email;
        this.isSharing = isSharing;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        UserId = userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public CreateUser()
    {

    }
}
