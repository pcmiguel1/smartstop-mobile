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

public class RecoverScreen extends AppCompatActivity {

    private EditText inputEmail;
    private ProgressBar progressBar;

    private String host = "10.72.122.13";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_screen);

        inputEmail = findViewById(R.id.input_email);
        progressBar = findViewById(R.id.progressRecover);

    }

    public void back(View view) {
        finish();
    }

    public void resetPassword(View view) {

        String email = inputEmail.getText().toString();

        if (!email.isEmpty()) {

            progressBar.setVisibility(view.VISIBLE);

            RequestQueue requestQueue = Volley.newRequestQueue(this);

            String url = "http://"+host+":3000/api/users/recover";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email", email);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressBar.setVisibility(view.GONE);
                            try {
                                Toast.makeText(RecoverScreen.this, response.getString("msg"), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RecoverScreen.this, LoginScreen.class);
                                startActivity(intent);
                                finish();
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
                                Toast.makeText(RecoverScreen.this, message, Toast.LENGTH_LONG).show();
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

        }
        else {
            Toast.makeText(this, "Fill in the spaces above!", Toast.LENGTH_LONG).show();
        }

    }
}