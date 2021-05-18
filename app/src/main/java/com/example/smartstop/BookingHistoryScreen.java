package com.example.smartstop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class BookingHistoryScreen extends AppCompatActivity {

    private TextView active, scheduled, expired;
    private Typeface face, face2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history_screen);

        active = findViewById(R.id.active);
        scheduled = findViewById(R.id.scheduled);
        expired = findViewById(R.id.expired);

        face = ResourcesCompat.getFont(getApplicationContext(), R.font.montserratsemibold);
        face2 = ResourcesCompat.getFont(getApplicationContext(), R.font.montserratregular);

    }

    public void closeHistory(View view) {
        finish();
    }

    public void BookingActive(View view) {

        active.setTypeface(face);
        scheduled.setTypeface(face2);
        expired.setTypeface(face2);

    }

    public void BookingScheduled(View view) {

        scheduled.setTypeface(face);
        active.setTypeface(face2);
        expired.setTypeface(face2);

    }

    public void BookingExpired(View view) {

        expired.setTypeface(face);
        active.setTypeface(face2);
        scheduled.setTypeface(face2);

    }

}