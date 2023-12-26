package com.example.myapplication.Models;

public class User {
    private int _id;
    private String emial;
    private String password;
    private String first_name;
    private String last_name;
    private String address;
    private String image_url;
    private double remained_amount;

    public User(int _id, String emial, String password, String first_name, String last_name, String address, String image_url, double remained_amount) {
        this._id = _id;
        this.emial = emial;
        this.password = password;
        this.first_name = first_name;
        this.last_name = last_name;
        this.address = address;
        this.image_url = image_url;
        this.remained_amount = remained_amount;
    }

    public User(){
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getEmial() {
        return emial;
    }

    public void setEmial(String emial) {
        this.emial = emial;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public double getRemained_amount() {
        return remained_amount;
    }

    public void setRemained_amount(double remained_amount) {
        this.remained_amount = remained_amount;
    }

    @Override
    public String toString() {
        return "User{" +
                "_id=" + _id +
                ", emial='" + emial + '\'' +
                ", password='" + password + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", address='" + address + '\'' +
                ", image_url='" + image_url + '\'' +
                ", remained_amount=" + remained_amount +
                '}';
    }
}
