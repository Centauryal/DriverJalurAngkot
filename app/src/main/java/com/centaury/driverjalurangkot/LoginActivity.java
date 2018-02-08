package com.centaury.driverjalurangkot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;


import com.centaury.driverjalurangkot.app.AppConfig;
import com.centaury.driverjalurangkot.app.AppController;
import com.centaury.driverjalurangkot.helper.SQLiteHandler;
import com.centaury.driverjalurangkot.helper.SessionManager;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    @BindView(R.id.link_to_register)
    TextView RegisterScreen;

    private TextInputLayout inputLayoutDriver, inputLayoutPassword;
    private EditText InputDriver, InputPassword;
    private Button BtnLogin;

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.login);

        ButterKnife.bind(this);

        inputLayoutDriver = (TextInputLayout)findViewById(R.id.inputlayoutlogin);
        inputLayoutPassword = (TextInputLayout)findViewById(R.id.inputlayoutpassword);
        InputDriver = (EditText) findViewById(R.id.inputlogin);
        InputPassword = (EditText) findViewById(R.id.inputpassword);
        BtnLogin = (Button)findViewById(R.id.btnLogin);

        InputDriver.addTextChangedListener(new MyTextWatcher(InputDriver));
        InputPassword.addTextChangedListener(new MyTextWatcher(InputPassword));

        //progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite Database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session Manager
        session = new SessionManager(getApplicationContext());

        // Check if driver is already logged in or not
        if (session.isLoggedIn()){
            // Driver is already logged in. Take him to main activity
            Intent pindah = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(pindah);
            finish();
        }

        BtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginForm();
            }
        });

        RegisterScreen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    /*Validate Form*/
    private void loginForm(){
        if (!validateLogin()){
            return;
        }

        if (!validatePassword()){
            return;
        }

        checklogForm();

        Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);

    }

    /*Check validate form*/
    private void checklogForm(){
        String login = InputDriver.getText().toString().trim();
        String password = InputPassword.getText().toString().trim();

        // Check for empty data in the form
        if (!login.isEmpty() && !password.isEmpty()){
            // login user
            checkLogin(login, password);
        } else {
            // Prompt user to enter credentials
            Toast.makeText(getApplicationContext(), "Please enter the credentials!", Toast.LENGTH_LONG).show();
        }
    }

    private Boolean validateLogin(){
        if (InputDriver.getText().toString().trim().isEmpty()){
            inputLayoutDriver.setError(getString(R.string.err_msg_login));
            requestFocus(InputDriver);
            return false;
        } else {
            inputLayoutDriver.setErrorEnabled(false);
        }
        return true;
    }

    private Boolean validatePassword(){
        if (InputPassword.getText().toString().trim().isEmpty()){
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(InputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }
        return true;
    }

    private void requestFocus (View view){
        if (view.requestFocus()){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        public MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (view.getId()){
                case R.id.inputlogin:
                    validateLogin();
                    break;
                case R.id.inputpassword:
                    validatePassword();
                    break;
            }

        }
    }

    /*
    function to verify login details in mysql db
    */
    private void checkLogin(final String hp, final String password) {
        // Tag used to cancel request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObjt = new JSONObject(response);
                    boolean error = jObjt.getBoolean("error");

                    // check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        String uid = jObjt.getString("uid");

                        JSONObject driver = jObjt.getJSONObject("driver");
                        String nama = driver.getString("nama");
                        String hp = driver.getString("hp");
                        String nopol = driver.getString("nopol");
                        String created_at = driver.getString("created_at");

                        // inserting row in drivers table
                        db.addDriver(nama, hp, nopol, uid, created_at);

                        // Launch Main Activity
                        Intent launch = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(launch);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObjt.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // Json Error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error:" + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }){

            @Override
            protected Map<String, String> getParams(){
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("hp", hp);
                params.put("password", password);

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (!pDialog.isShowing())
            pDialog.dismiss();
    }

}
