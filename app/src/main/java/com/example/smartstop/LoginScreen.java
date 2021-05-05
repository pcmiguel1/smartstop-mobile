package com.example.smartstop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class LoginScreen extends AppCompatActivity {

    private ProgressBar progressBar;
    private EditText inputEmail, inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        progressBar = findViewById(R.id.progressLogin);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);

    }

    public void back(View view) {
        finish();
    }

    public void login(View view) {

        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()) {

            progressBar.setVisibility(view.VISIBLE);

            RequestQueue requestQueue = Volley.newRequestQueue(this);

            String url = "http://192.168.1.4:3000/api/users/login";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email", email);
                jsonObject.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressBar.setVisibility(view.GONE);
                            try {
                                if (response.getInt("success") == 1) {
                                    Intent intent = new Intent(LoginScreen.this, MapScreen.class);
                                    intent.putExtra("user", response.getJSONObject("data").toString());
                                    startActivity(intent);
                                    finish();
                                }
                                else if (response.getInt("success") == 3) { //se não tiver a conta ativada
                                    Toast.makeText(LoginScreen.this, response.getString("msg"), Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(LoginScreen.this, TokenScreen.class);
                                    intent.putExtra("email", email);
                                    intent.putExtra("password", password);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(LoginScreen.this, response.getString("msg"), Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(view.GONE);
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null) {
                        int status = networkResponse.statusCode;
                        if (status == 404) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                String message = data.optString("msg");
                                Toast.makeText(LoginScreen.this, message, Toast.LENGTH_LONG).show();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            });

            requestQueue.add(request_json);

        } else {
            Toast.makeText(this, "Fill in the spaces above!", Toast.LENGTH_LONG).show();
        }

    }

    public void openRecoverAccount(View view) {

        Intent intent = new Intent(LoginScreen.this, RecoverScreen.class);
        startActivity(intent);
        finish();

    }
}