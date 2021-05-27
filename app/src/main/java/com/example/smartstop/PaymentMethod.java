package com.example.smartstop;

public class PaymentMethod {

    private int id;
    private String card_number;
    private String expiry_date;
    private int cvv;
    private int user_id;

    public PaymentMethod(int id, String card_number, String expiry_date, int cvv, int user_id) {
        this.id = id;
        this.card_number = card_number;
        this.expiry_date = expiry_date;
        this.cvv = cvv;
        this.user_id = user_id;
    }

    public int getId() {
        return id;
    }

    public String getCard_number() {
        return card_number;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public int getCvv() {
        return cvv;
    }

    public int getUser_id() {
        return user_id;
    }
}
