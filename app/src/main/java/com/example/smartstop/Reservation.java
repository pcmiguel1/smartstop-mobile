package com.example.smartstop;

public class Reservation {

    private int reservationId;
    private String reservationDate;
    private String reservationLastDay;
    private String reservationStartDay;
    private int parkSlotId;
    private int vehicleId;
    private String vehicleModel;
    private String vehicleRegistration;
    private int vehicleType;
    private int parkId;
    private String parkSlotNumber;

    public Reservation(int reservationId, String reservationDate, String reservationLastDay, String reservationStartDay, int parkSlotId, int vehicleId, String vehicleModel, String vehicleRegistration, int vehicleType, int parkId, String parkSlotNumber) {
        this.reservationId = reservationId;
        this.reservationDate = reservationDate;
        this.reservationLastDay = reservationLastDay;
        this.reservationStartDay = reservationStartDay;
        this.parkSlotId = parkSlotId;
        this.vehicleId = vehicleId;
        this.vehicleModel = vehicleModel;
        this.vehicleRegistration = vehicleRegistration;
        this.vehicleType = vehicleType;
        this.parkId = parkId;
        this.parkSlotNumber = parkSlotNumber;
    }

    public void setReservationLastDay(String reservationLastDay) {
        this.reservationLastDay = reservationLastDay;
    }

    public int getReservationId() {
        return reservationId;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public String getReservationLastDay() {
        return reservationLastDay;
    }

    public String getReservationStartDay() {
        return reservationStartDay;
    }

    public int getParkSlotId() {
        return parkSlotId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public int getVehicleType() {
        return vehicleType;
    }

    public int getParkId() {
        return parkId;
    }

    public String getParkSlotNumber() {
        return parkSlotNumber;
    }
}
