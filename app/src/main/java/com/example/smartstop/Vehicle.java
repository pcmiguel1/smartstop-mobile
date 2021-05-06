package com.example.smartstop;

public class Vehicle {

    private int id;
    private String model;
    private String registration;
    private int type;

    public Vehicle(int id, String model, String registration, int type) {
        this.id = id;
        this.model = model;
        this.registration = registration;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getRegistration() {
        return registration;
    }

    public int getType() {
        return type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public void setType(int type) {
        this.type = type;
    }
}
