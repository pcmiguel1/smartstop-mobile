package com.example.smartstop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class VehiclesScreen extends AppCompatActivity {

    private ListView listViewVehicles;
    private RequestQueue requestQueue;
    private ArrayList<Vehicle> vehicles;
    private MyAdapter adapter;
    int tipos[] = {R.drawable.categorybasic, R.drawable.categorybolt, R.drawable.categoryminivan, R.drawable.categorypremium};

    private int selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles_screen);

        requestQueue = Volley.newRequestQueue(VehiclesScreen.this);

        listViewVehicles = findViewById(R.id.listView_vehicles);

        vehicles = new ArrayList<>();

        try {
            getVehicles(MapScreen.USER_JSON_OBJECT.getInt("user_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        listViewVehicles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectedItem = position;
                adapter.notifyDataSetChanged();

                String registration = vehicles.get(position).getRegistration();
                String model = vehicles.get(position).getModel();
                openEditVehicle(model, registration);

            }
        });

    }

    private void getVehicles(int id) {

        String url = "http://10.72.120.186:3000/api/users/"+id+"/vehicles";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response != null) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject data = jsonArray.getJSONObject(i);

                                    vehicles.add(new Vehicle(data.getInt("vehicle_id"), data.getString("vehicle_model"), data.getString("vehicle_registration"), data.getInt("vehicle_type")));

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        adapter = new MyAdapter(VehiclesScreen.this, tipos, vehicles);
                        listViewVehicles.setAdapter(adapter);

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(VehiclesScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(request);

    }

    public void addVehicle(View view) {

        openAddVehicle();

    }

    class MyAdapter extends ArrayAdapter<Vehicle> {

        Context context;
        int rTipos[];

        MyAdapter (Context c, int tipo[], List<Vehicle> vehicles) {
            super(c, R.layout.row_vehicle, vehicles);
            this.context = c;
            this.rTipos = tipo;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = layoutInflater.inflate(R.layout.row_vehicle, parent, false);
            ImageView images = row.findViewById(R.id.car_type);
            TextView myMatricula = row.findViewById(R.id.matricula);
            TextView myModelo = row.findViewById(R.id.modelo_car);
            CardView myCardVehicle = row.findViewById(R.id.card_vehicle);

            images.setImageResource(rTipos[getItem(position).getType()]);
            myMatricula.setText(getItem(position).getRegistration());
            myModelo.setText(getItem(position).getModel());

            if (position == selectedItem) {

                myCardVehicle.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                myModelo.setTextColor(getResources().getColor(R.color.colorAccent));

            }

            return row;
        }
    }

    public void closeVehicles(View view) {
        finish();
    }

    private void openEditVehicle(String model, String registration) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                VehiclesScreen.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_edit_vehicle,
                        (LinearLayout) findViewById(R.id.bottomSheetParkInfo)
                );

        EditText vehicleModel, vehicleRegistration;

        vehicleModel = bottomSheetView.findViewById(R.id.vehicle_model);
        vehicleRegistration = bottomSheetView.findViewById(R.id.vehicle_registration);

        vehicleModel.setText(model);
        vehicleRegistration.setText(registration);

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    private void openAddVehicle() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                VehiclesScreen.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_add_vehicle,
                        (LinearLayout) findViewById(R.id.bottomSheetParkInfo)
                );

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }
}