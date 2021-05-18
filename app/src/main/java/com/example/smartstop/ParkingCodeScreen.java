package com.example.smartstop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ParkingCodeScreen extends AppCompatActivity {

    private ImageView qr_code;
    private TextView parkPass, parkSlot, parkDateFrom, parkDateTo, parkAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_code_screen);

        qr_code = findViewById(R.id.qrCode);
        parkPass = findViewById(R.id.park_pass);
        parkSlot = findViewById(R.id.park_slot);
        parkDateFrom = findViewById(R.id.park_dateFrom);
        parkDateTo = findViewById(R.id.park_dateTo);
        parkAddress = findViewById(R.id.park_address);

        //Load QRCode
        Picasso
                .get()
                .load("https://api.qrserver.com/v1/create-qr-code/?size=190x190&data=Example")
                .into(qr_code);

        if (getIntent().hasExtra("Address")) {
            parkAddress.setText(getIntent().getStringExtra("Address"));
        }

        if (getIntent().hasExtra("DateFrom")) {
            parkDateFrom.setText(getIntent().getStringExtra("DateFrom"));

            if (getIntent().hasExtra("Duration")) {

                parkDateTo.setText(getIntent().getStringExtra("Duration"));

            }

        }

    }

    public void closeParkingCode(View view) {
        finish();
    }
}