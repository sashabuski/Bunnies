package com.HopIn;

import java.io.Serializable;

/**
 * This class is the user object that is created when the user creates their account.
 * Contains the static information about the user.
 * The object is then used within a UserLocation object upon the user sending their
 * live location on the map.
 *
 */
public class User implements Serializable {

    String email;
    String password;
    String fName;
    String lName;
    String carModel;
    String carNumber;
    // Long longitude;
    // Long latitude;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }


    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", fName='" + fName + '\'' +
                ", lName='" + lName + '\'' +
                ", carModel='" + carModel + '\'' +
                ", carNumber='" + carNumber + '\'' +
                '}';
    }

    public User(){
        this.email = null;
        this.password = null;
        String fName = null;
        String lName = null;
        String carModel = null;
        String carNumber = null;
    };

    public User(String email, String password){
        this.email = email;
        this.password = password;
        String fName = null;
        String lName = null;
        String carModel = null;
        String carNumber = null;
        //Long longitude = null;
       // Long latitude = null;
    }
    public User(String email){
        this.email = email;
        this.password = null;
        String fName = null;
        String lName = null;
        String carModel = null;
        String carNumber = null;
       // Long longitude = null;
       // Long latitude = null;
    }
}
