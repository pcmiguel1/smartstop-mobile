package com.example.smartstop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;

public class PerfilScreen extends AppCompatActivity {

    private String userName = "";
    private TextView textUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_screen);

        textUserName = findViewById(R.id.user_name);

        try {
            userName = MapScreen.USER_JSON_OBJECT.getString("user_fullname");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        textUserName.setText(userName.split(" ")[0]);

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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

    public void openSettings(View view) {
        Intent intent = new Intent(PerfilScreen.this, SettingsScreen.class);
        startActivity(intent);
        finish();
    }
}