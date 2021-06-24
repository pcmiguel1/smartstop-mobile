package com.example.smartstop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookingHistoryScreen extends AppCompatActivity {

    private ListView listViewReservations;
    private RequestQueue requestQueue;
    private ArrayList<Reservation> reservations;
    private MyAdapter adapter;
    private TextView active, scheduled, expired;
    private Typeface face, face2;

    private int userId;
    private String history = "active";

    private String host = MainActivity.HOST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history_screen);

        requestQueue = Volley.newRequestQueue(BookingHistoryScreen.this);

        listViewReservations = findViewById(R.id.listView_books);

        reservations = new ArrayList<>();

        try {
            userId = MapScreen.USER_JSON_OBJECT.getInt("user_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        active = findViewById(R.id.active);
        scheduled = findViewById(R.id.scheduled);
        expired = findViewById(R.id.expired);

        face = ResourcesCompat.getFont(getApplicationContext(), R.font.montserratsemibold);
        face2 = ResourcesCompat.getFont(getApplicationContext(), R.font.montserratregular);

        active.setTypeface(face);
        scheduled.setTypeface(face2);
        expired.setTypeface(face2);

        getReservations();

    }

    public void closeHistory(View view) {
        finish();
    }

    public void BookingActive(View view) {

        active.setTypeface(face);
        scheduled.setTypeface(face2);
        expired.setTypeface(face2);
        history = "active";
        getReservations();

    }

    public void BookingScheduled(View view) {

        scheduled.setTypeface(face);
        active.setTypeface(face2);
        expired.setTypeface(face2);
        history = "scheduled";
        getReservations();

    }

    public void BookingExpired(View view) {

        expired.setTypeface(face);
        active.setTypeface(face2);
        scheduled.setTypeface(face2);
        history = "expired";
        getReservations();

    }

    private void getReservations() {

        String url = "http://"+host+":3000/api/users/"+userId+"/reservations/"+history;

        reservations.clear();

        adapter = new BookingHistoryScreen.MyAdapter(BookingHistoryScreen.this, reservations);
        listViewReservations.setAdapter(adapter);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response != null) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject data = jsonArray.getJSONObject(i);

                                    reservations.add(new Reservation(data.getInt("reservation_id"), data.getString("reservation_date"), data.getString("reservation_last_day"), data.getString("reservation_start_day"), data.getInt("reservation_park_slot_id"), data.getInt("vehicle_id"), data.getString("vehicle_model"), data.getString("vehicle_registration"), data.getInt("vehicle_type"), data.getInt("park_id"), data.getString("park_slots_number")));

                                }

                                adapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(request);

    }

    class MyAdapter extends ArrayAdapter<Reservation> {

        Context context;

        MyAdapter (Context c, List<Reservation> reservations) {
            super(c, R.layout.row_book, reservations);
            this.context = c;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = layoutInflater.inflate(R.layout.row_book, parent, false);
            TextView myMatricula = row.findViewById(R.id.car_registration);
            TextView myTime = row.findViewById(R.id.time_left);
            TextView info = row.findViewById(R.id.info_booking);
            Button extend = row.findViewById(R.id.btn_extend);
            Button details = row.findViewById(R.id.btn_details);
            LinearLayout btnsBook = row.findViewById(R.id.btns_book);

            myMatricula.setText(reservations.get(position).getVehicleRegistration());
            String lastDay = reservations.get(position).getReservationLastDay();
            String startDay = reservations.get(position).getReservationStartDay();

            long duration = 0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            try {
                Date date1 = new Date();
                Date date2 = sdf.parse(lastDay);
                Date date3 = sdf.parse(startDay);
                if (history.equals("scheduled")) {
                    info.setText("Ative when");
                    duration =  date3.getTime() - date1.getTime();
                }
                else if (history.equals("expired")) {
                    info.setText("Reservation expired");
                    myTime.setText("00h 00m 00s");
                    extend.setVisibility(View.GONE);
                    btnsBook.setWeightSum(1);
                }
                else {
                    duration =  date2.getTime() - date1.getTime();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Initialize countdown timer
            new CountDownTimer(duration, 1000) {

                @Override
                public void onTick(long l) {
                    //When tick
                    //Convert millisecond to minute and second
                    String sDuration = String.format(Locale.ENGLISH, "%02dh %02dm %02ds"
                            , TimeUnit.MILLISECONDS.toHours(l)
                        , TimeUnit.MILLISECONDS.toMinutes(l) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(l))
                        , TimeUnit.MILLISECONDS.toSeconds(l) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l)));
                    //Set converted String on textView
                    myTime.setText(sDuration);
                }

                @Override
                public void onFinish() {

                }

            }.start();

            extend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Calendar calendar = Calendar.getInstance();

                    TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                            try {
                                Date lastDate = df2.parse(reservations.get(position).getReservationLastDay());
                                calendar.setTime(lastDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            calendar.add(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.add(Calendar.MINUTE, minute);

                            String url = "http://"+host+":3000/api/reservations/"+reservations.get(position).getReservationId()+"/extend";

                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("last_day", df2.format(calendar.getTime()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                                    new com.android.volley.Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Toast.makeText(BookingHistoryScreen.this, "Extended time to: " + df.format(calendar.getTime()), Toast.LENGTH_SHORT).show();
                                            reservations.get(position).setReservationLastDay(df2.format(calendar.getTime()));
                                            adapter.notifyDataSetChanged();
                                        }
                                    }, new com.android.volley.Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            });

                            requestQueue.add(request_json);

                        }
                    };

                    TimePickerDialog timePickerDialog = new TimePickerDialog(BookingHistoryScreen.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true);
                    timePickerDialog.show();

                }
            });

            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String url = "http://"+host+":3000/api/reservations/"+1;

                    StringRequest request = new StringRequest(Request.Method.GET, url,
                            new com.android.volley.Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {

                                    if (response != null) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(response);

                                            String dateFrom = jsonObject.getString("reservation_start_day");
                                            String dateUntil = jsonObject.getString("reservation_last_day");


                                            Intent intent = new Intent(BookingHistoryScreen.this, ParkingCodeScreen.class);
                                            intent.putExtra("DateFrom", dateFrom);
                                            intent.putExtra("Duration", dateUntil);
                                            //intent.putExtra("Address", fullAddressFinal);
                                            startActivity(intent);


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }

                                }
                            }, new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(BookingHistoryScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    requestQueue.add(request);

                }
            });

            return row;
        }
    }

}