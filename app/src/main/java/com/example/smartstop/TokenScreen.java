package com.example.smartstop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class TokenScreen extends AppCompatActivity {

    private EditText inputToken1, inputToken2, inputToken3, inputToken4;
    private ProgressBar progress_verify;

    private String host = MainActivity.HOST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_screen);

        inputToken1 = findViewById(R.id.input_token1);
        inputToken2 = findViewById(R.id.input_token2);
        inputToken3 = findViewById(R.id.input_token3);
        inputToken4 = findViewById(R.id.input_token4);
        progress_verify = findViewById(R.id.progressVerify);

        setupInputs();

    }

    private void setupInputs() {

        inputToken1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputToken2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputToken2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputToken3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputToken3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputToken4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public void verifyAccount(View view) {

        String token1 = inputToken1.getText().toString();
        String token2 = inputToken2.getText().toString();
        String token3 = inputToken3.getText().toString();
        String token4 = inputToken4.getText().toString();

        //Verificar se esta preenchido
        if (!token1.isEmpty() && !token2.isEmpty() && !token3.isEmpty() && !token4.isEmpty()) {

            if (getIntent().hasExtra("email") && getIntent().hasExtra("password")) {

                progress_verify.setVisibility(view.VISIBLE);

                RequestQueue requestQueue = Volley.newRequestQueue(this);

                String url = "http://"+host+":3000/api/users/verify";

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("email", getIntent().getStringExtra("email"));
                    jsonObject.put("password", getIntent().getStringExtra("password"));
                    jsonObject.put("token", token1+token2+token3+token4);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                progress_verify.setVisibility(view.GONE);
                                try {
                                    Toast.makeText(TokenScreen.this, response.getString("msg"), Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(TokenScreen.this, LoginScreen.class);
                                    startActivity(intent);
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress_verify.setVisibility(view.GONE);
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null) {
                            int status = networkResponse.statusCode;
                            if (status == 404) {
                                try {
                                    String responseBody = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(responseBody);
                                    String message = data.optString("msg");
                                    Toast.makeText(TokenScreen.this, message, Toast.LENGTH_LONG).show();
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

        } else {
            Toast.makeText(TokenScreen.this, "Please fill in the validation code!", Toast.LENGTH_LONG).show();
        }

    }

    public void back(View view) {
        finish();
    }
}