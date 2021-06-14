package com.example.smartstop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SettingsScreen extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private ProgressBar progressBar;

    private String host = MainActivity.HOST;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        progressBar = findViewById(R.id.progressSave);

        try {
            userId = MapScreen.USER_JSON_OBJECT.getInt("user_id");
            inputEmail.setHint(MapScreen.USER_JSON_OBJECT.getString("user_email"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void save(View view) {

        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if (!email.isEmpty() || !password.isEmpty()) {

            progressBar.setVisibility(view.VISIBLE);

            RequestQueue requestQueue = Volley.newRequestQueue(this);

            String url = "http://"+host+":3000/api/users/"+userId+"/edit";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email", email);
                jsonObject.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressBar.setVisibility(view.GONE);
                            Toast.makeText(SettingsScreen.this, "Account details saved successfully!", Toast.LENGTH_LONG).show();

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(view.GONE);
                }
            });

            requestQueue.add(request_json);

        } else {
            Toast.makeText(this, "Fill in the spaces above!", Toast.LENGTH_LONG).show();
        }

    }

    public void back(View view) {
        Intent intent = new Intent(SettingsScreen.this, MapScreen.class);
        startActivity(intent);
        finish();
    }
}