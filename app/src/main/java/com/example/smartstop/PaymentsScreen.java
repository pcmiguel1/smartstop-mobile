package com.example.smartstop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PaymentsScreen extends AppCompatActivity {

    private ListView listViewPayments;
    private RequestQueue requestQueue;
    private ArrayList<PaymentMethod> paymentMethods;
    private MyAdapter adapter;

    private int tipos[] = {R.drawable.mastercard_150px, R.drawable.visa_150px};

    private int selectedItem;
    private int userId;

    private String host = MainActivity.HOST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_screen);

        requestQueue = Volley.newRequestQueue(PaymentsScreen.this);

        listViewPayments = findViewById(R.id.listView_payment);

        paymentMethods = new ArrayList<>();

        try {
            userId = MapScreen.USER_JSON_OBJECT.getInt("user_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getFavoritePaymentMethod();

        getPaymentMethods();
        System.out.println("selectedItem: " + selectedItem);

        listViewPayments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectedItem = paymentMethods.get(position).getId();
                adapter.notifyDataSetChanged();

                //setar o veiculo selecionado como favorito
                setFavoritePaymentMethod(selectedItem);
            }
        });

    }

    private void getFavoritePaymentMethod() {

        String url = "http://"+host+":3000/api/users/"+userId+"/paymentmethods/favorite";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                selectedItem = jsonObject.getInt("payment_method_id");
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

    private void setFavoritePaymentMethod(int newId) {

        String url = "http://"+host+":3000/api/paymentmethods/"+newId+"/favorite/add";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("oldId", MapScreen.vehicleSelectedId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(request_json);

    }

    private void getPaymentMethods() {

        String url = "http://"+host+":3000/api/users/"+userId+"/paymentmethods";

        adapter = new MyAdapter(PaymentsScreen.this, tipos, paymentMethods);
        listViewPayments.setAdapter(adapter);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response != null) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject data = jsonArray.getJSONObject(i);

                                    paymentMethods.add(new PaymentMethod(data.getInt("payment_method_id"), data.getString("payment_method_card_number"), data.getString("payment_method_expiry_date"), data.getInt("payment_method_cvv"), data.getInt("payment_method_user_id")));

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
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    int status = networkResponse.statusCode;
                    if (status == 404) {

                    }
                }
            }
        });

        requestQueue.add(request);

    }

    public void closePayment(View view) {
        Intent intent = new Intent(PaymentsScreen.this, MapScreen.class);
        startActivity(intent);
        finish();
    }

    public void addPayment(View view) {
        openAddPayment();
    }

    private void openAddPayment() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                PaymentsScreen.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_add_payment,
                        (LinearLayout) findViewById(R.id.bottomSheetPayment)
                );

        EditText cardNumber, expirationDate, cvv, cardHolderName;
        LinearLayout btnConfirmAdd = bottomSheetView.findViewById(R.id.btn_confirm_add);
        cardNumber = bottomSheetView.findViewById(R.id.card_number);
        expirationDate = bottomSheetView.findViewById(R.id.expiration_date);
        cvv = bottomSheetView.findViewById(R.id.cvv);
        cardHolderName = bottomSheetView.findViewById(R.id.cardholder_name);

        cardNumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                ImageView type = bottomSheetDialog.findViewById(R.id.img_payment_type);
                if(s.length() != 0) {
                    if (s.charAt(0) == '4') {
                        type.setImageResource(tipos[1]);
                    } else if (s.charAt(0) == '5') {
                        type.setImageResource(tipos[0]);
                    }
                }
            }
        });

        expirationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showExpirationDateDialog(expirationDate);

            }
        });

        btnConfirmAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputCardNumber = cardNumber.getText().toString();
                String inputExpirationDate = expirationDate.getText().toString();
                String inputCvv = cvv.getText().toString();
                String inputCardHolderName = cardHolderName.getText().toString();

                if (!inputCardNumber.isEmpty() && !inputExpirationDate.isEmpty() && !inputCvv.isEmpty() && !inputCardHolderName.isEmpty()) {

                    if (inputCardNumber.length() == 16 && inputCvv.length() == 3 && (inputCardNumber.charAt(0) == '5' || inputCardNumber.charAt(0) == '4')) {

                        addPayment(inputCardNumber, inputExpirationDate, Integer.parseInt(inputCvv));
                        bottomSheetDialog.dismiss();
                        Toast.makeText(PaymentsScreen.this, "Payment Method successfully added!", Toast.LENGTH_SHORT).show();

                    }
                    else {
                        Toast.makeText(PaymentsScreen.this, "Card is not valid!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(PaymentsScreen.this, "Fill in the spaces above!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    private void addPayment(String cardNumber, String expiryDate, int cvv) {

        String url = "http://"+host+":3000/api/users/"+userId+"/paymentmethods/new";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("card_number", cardNumber);
            jsonObject.put("expiry_date", expiryDate);
            jsonObject.put("cvv", cvv);
            jsonObject.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        if (response != null) {

                            try {
                                paymentMethods.add(new PaymentMethod(response.getInt("insertId"), cardNumber, expiryDate, cvv, userId));
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(request_json);

    }

    private void showExpirationDateDialog(final EditText dateInput) {

        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                month = month+1;
                String date = month+"/"+year;
                dateInput.setText(date);

            }

        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(PaymentsScreen.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, dateSetListener, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        datePickerDialog.getDatePicker().findViewById(getResources().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
        datePickerDialog.show();

    }

    class MyAdapter extends ArrayAdapter<PaymentMethod> {

        Context context;
        int rTipos[];

        MyAdapter (Context c, int tipo[], List<PaymentMethod> paymentMethods) {
            super(c, R.layout.row_payment, paymentMethods);
            this.context = c;
            this.rTipos = tipo;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = layoutInflater.inflate(R.layout.row_payment, parent, false);
            ImageView images = row.findViewById(R.id.img_payment_type);
            TextView myType = row.findViewById(R.id.payment_type);
            TextView myNumber = row.findViewById(R.id.number_payment);
            CardView myCardPayment = row.findViewById(R.id.card_payment);

            CardView dot1 = row.findViewById(R.id.number_dot1);
            CardView dot2 = row.findViewById(R.id.number_dot2);
            CardView dot3 = row.findViewById(R.id.number_dot3);
            CardView dot4 = row.findViewById(R.id.number_dot4);

            String number = String.valueOf(paymentMethods.get(position).getCard_number());

            if (number.charAt(0) == '4') { //Visa
                images.setImageResource(rTipos[1]);
                myType.setText("Visa");
            } else if (number.charAt(0) == '5') { //Mastercard
                images.setImageResource(rTipos[0]);
                myType.setText("Mastercard");
            }
            myNumber.setText(number.substring(number.length() - 4));

            if (paymentMethods.get(position).getId() == selectedItem) {

                myCardPayment.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                myNumber.setTextColor(getResources().getColor(R.color.colorAccent));
                dot1.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
                dot2.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
                dot3.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
                dot4.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));

            }

            return row;
        }

    }
}