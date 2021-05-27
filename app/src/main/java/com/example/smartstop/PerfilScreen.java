package com.example.smartstop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PerfilScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_screen);
    }

    public void closePerfil(View view) {
        finish();
    }

    public void openVehicles(View view) {
        Intent intent = new Intent(PerfilScreen.this, VehiclesScreen.class);
        startActivity(intent);
        finish();
    }

    public void logout(View view) {
        Intent intent = new Intent(PerfilScreen.this, LoginScreen.class);
        startActivity(intent);
        finish();
    }

    public void openBookingHistory(View view) {
        Intent intent = new Intent(PerfilScreen.this, BookingHistoryScreen.class);
        startActivity(intent);
        finish();
    }

    public void openPaymentMethods(View view) {
        Intent intent = new Intent(PerfilScreen.this, PaymentsScreen.class);
        startActivity(intent);
        finish();
    }
}