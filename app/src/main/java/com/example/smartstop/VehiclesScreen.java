package com.example.smartstop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class VehiclesScreen extends AppCompatActivity {

    private LinearLayout boxNoVehicles;
    private ListView listViewVehicles;
    private RequestQueue requestQueue;
    private ArrayList<Vehicle> vehicles;
    private MyAdapter adapter;
    int tipos[] = {R.drawable.categorybasic, R.drawable.categorybolt, R.drawable.categoryminivan, R.drawable.categorypremium};

    private int selectedItem;
    private int selectedType = 0;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles_screen);

        requestQueue = Volley.newRequestQueue(VehiclesScreen.this);

        boxNoVehicles = findViewById(R.id.box_no_vehicles);
        listViewVehicles = findViewById(R.id.listView_vehicles);

        vehicles = new ArrayList<>();

        try {
            userId = MapScreen.USER_JSON_OBJECT.getInt("user_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("userId: " + userId);

        getVehicles(userId);

        listViewVehicles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectedItem = position;
                adapter.notifyDataSetChanged();

                String registration = vehicles.get(position).getRegistration();
                String model = vehicles.get(position).getModel();
                openEditVehicle(vehicles.get(position).getId(), position, model, registration);

            }
        });

    }

    private void getVehicles(int id) {

        String url = "http://192.168.1.4:3000/api/users/"+id+"/vehicles";

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

                                System.out.println("sizeVehicles: " + vehicles.size());

                                if (!vehicles.isEmpty()) {

                                    listViewVehicles.setVisibility(View.VISIBLE);
                                    boxNoVehicles.setVisibility(View.INVISIBLE);
                                    adapter = new MyAdapter(VehiclesScreen.this, tipos, vehicles);
                                    listViewVehicles.setAdapter(adapter);

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    int status = networkResponse.statusCode;
                    if (status == 404) {
                        listViewVehicles.setVisibility(View.INVISIBLE);
                        boxNoVehicles.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        requestQueue.add(request);

    }

    public void addVehicle(View view) {

        openAddVehicle();

    }

    public void closeVehicles(View view) {
        finish();
    }

    private void openEditVehicle(int id, int position, String model, String registration) {

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

        //Botão de editar
        bottomSheetView.findViewById(R.id.btn_edit_vehicle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputModel = vehicleModel.getText().toString();
                String inputRegistration = vehicleRegistration.getText().toString();

                if (selectedType != 0 && !inputModel.isEmpty() && !inputRegistration.isEmpty()) {

                    editVehicle(id, position, inputModel, inputRegistration);
                    bottomSheetDialog.dismiss();
                    Toast.makeText(VehiclesScreen.this, "Vehicle successfully edited!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(VehiclesScreen.this, "Fill in the spaces above!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Botão para remover
        bottomSheetView.findViewById(R.id.btn_delete_vehicle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteVehicle(id, position);
                bottomSheetDialog.dismiss();
                Toast.makeText(VehiclesScreen.this, "Vehicle successfully removed!", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    private void editVehicle(int id, int position, String vehicleModel, String vehicleRegistration) {

        String url = "http://192.168.1.4:3000/api/vehicles/"+id+"/edit";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", vehicleModel);
            jsonObject.put("registration", vehicleRegistration);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (response != null) {

                            vehicles.get(position).setModel(vehicleModel);
                            vehicles.get(position).setModel(vehicleRegistration);
                            adapter.notifyDataSetChanged();

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(request_json);

    }

    private void deleteVehicle(int id, int position) {

        String url = "http://192.168.1.4:3000/api/vehicles/"+id+"/delete";

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response != null) {
                            vehicles.remove(position);
                            adapter.notifyDataSetChanged();

                            if (vehicles.size() == 0) {
                                listViewVehicles.setVisibility(View.INVISIBLE);
                                boxNoVehicles.setVisibility(View.VISIBLE);
                            }

                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(VehiclesScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(request);

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

        CardView type1, type2, type3, type4;
        EditText model, registration;
        LinearLayout btnConfirmAdd;
        type1 = bottomSheetView.findViewById(R.id.vehicle_type1);
        type2 = bottomSheetView.findViewById(R.id.vehicle_type2);
        type3 = bottomSheetView.findViewById(R.id.vehicle_type3);
        type4 = bottomSheetView.findViewById(R.id.vehicle_type4);
        model = bottomSheetView.findViewById(R.id.vehicle_model);
        registration = bottomSheetView.findViewById(R.id.vehicle_registration);
        btnConfirmAdd = bottomSheetView.findViewById(R.id.btn_confirm_add);


        type1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedType = 1;
                v.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                type2.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
                type3.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
                type4.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
            }
        });

        type2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedType = 2;
                v.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                type1.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
                type3.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
                type4.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
            }
        });

        type3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedType = 3;
                v.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                type1.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
                type2.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
                type4.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
            }
        });

        type4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedType = 4;
                v.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                type1.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
                type2.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
                type3.setBackgroundTintList(getResources().getColorStateList(R.color.colorG));
            }
        });

        btnConfirmAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputModel = model.getText().toString();
                String inputRegistration = registration.getText().toString();

                if (selectedType != 0 && !inputModel.isEmpty() && !inputRegistration.isEmpty()) {

                    addVehicle(inputModel, inputRegistration);
                    bottomSheetDialog.dismiss();
                    Toast.makeText(VehiclesScreen.this, "Vehicle successfully added!", Toast.LENGTH_SHORT).show();

                    if (listViewVehicles.getVisibility() != View.VISIBLE) {
                        listViewVehicles.setVisibility(View.VISIBLE);
                        boxNoVehicles.setVisibility(View.INVISIBLE);
                    }


                } else {
                    Toast.makeText(VehiclesScreen.this, "Fill in the spaces above!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    private void addVehicle(String model, String registration) {

        String url = "http://192.168.1.4:3000/api/users/"+userId+"/vehicles/new";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", model);
            jsonObject.put("registration", registration);
            jsonObject.put("type", selectedType);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        if (response != null) {

                            try {
                                vehicles.add(new Vehicle(response.getInt("insertId"), model, registration, selectedType));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            adapter.notifyDataSetChanged();

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(request_json);

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

            images.setImageResource(rTipos[vehicles.get(position).getType()-1]);
            myMatricula.setText(vehicles.get(position).getRegistration());
            myModelo.setText(vehicles.get(position).getModel());

            if (position == selectedItem) {

                myCardVehicle.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                myModelo.setTextColor(getResources().getColor(R.color.colorAccent));

            }

            return row;
        }
    }
}