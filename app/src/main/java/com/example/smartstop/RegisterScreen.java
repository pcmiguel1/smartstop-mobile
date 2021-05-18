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

public class RegisterScreen extends AppCompatActivity {

    private EditText inputFullName, inputEmail, inputPassword, inputConfirmPassword;
    private ProgressBar progressBar;

    private String host = "10.72.122.13";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        inputFullName = findViewById(R.id.input_fullname);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        inputConfirmPassword = findViewById(R.id.input_confirmPassword);

        progressBar = findViewById(R.id.progressRegister);

    }

    public void back(View view) {
        finish();
    }

    public void register(View view) {

        String fullname = inputFullName.getText().toString();
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String password_confirm = inputConfirmPassword.getText().toString();

        //Verificar se está tudo preenchido
        if (!fullname.isEmpty() && !email.isEmpty() && !password.isEmpty() && !password_confirm.isEmpty()) {

            //Verificar passwords
            if (password.equals(password_confirm)) {

                progressBar.setVisibility(view.VISIBLE);

                RequestQueue requestQueue = Volley.newRequestQueue(this);

                String url = "http://"+host+":3000/api/users";

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("fullname", fullname);
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
                                Intent intent = new Intent(RegisterScreen.this, TokenScreen.class);
                                intent.putExtra("email", email);
                                intent.putExtra("password", password);
                                startActivity(intent);
                                finish();
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
                                    Toast.makeText(RegisterScreen.this, message, Toast.LENGTH_LONG).show();
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
                progressBar.setVisibility(view.GONE);
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_LONG).show();
            }

        }
        else {
            Toast.makeText(this, "Fill in the spaces above!", Toast.LENGTH_LONG).show();
        }

    }

    public void openLogin(View view) {
        Intent intent = new Intent(RegisterScreen.this, LoginScreen.class);
        startActivity(intent);
    }
}