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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    @BindView(R.id.link_to_login)
    TextView LoginScreen;

    private TextInputLayout inputLayoutName, inputLayoutPhone, inputLayoutRegPass, inputLayoutReType;
    private EditText InputName,InputPhone,InputRegPass,InputRetype;
    private Button BtnRegister;

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle(R.string.register);

        ButterKnife.bind(this);

        inputLayoutName = (TextInputLayout) findViewById(R.id.inputlayoutname);
        inputLayoutPhone = (TextInputLayout) findViewById(R.id.inputlayoutphone);
        inputLayoutRegPass = (TextInputLayout) findViewById(R.id.inputlayoutregpass);
        inputLayoutReType = (TextInputLayout) findViewById(R.id.inputlayoutretype);

        InputName = (EditText) findViewById(R.id.inputname);
        InputPhone = (EditText) findViewById(R.id.inputphone);
        InputRegPass = (EditText) findViewById(R.id.inputregpass);
        InputRetype = (EditText) findViewById(R.id.inputretype);

        BtnRegister = (Button) findViewById(R.id.btnRegister);

        InputName.addTextChangedListener(new MyTextWatcher(InputName));
        InputPhone.addTextChangedListener(new MyTextWatcher(InputPhone));
        InputRegPass.addTextChangedListener(new MyTextWatcher(InputRegPass));
        InputRetype.addTextChangedListener(new MyTextWatcher(InputRetype));

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent pindah = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(pindah);
            finish();
        }

        BtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForm();
            }
        });

        LoginScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
    }

    /*Validate Form*/
    private void registerForm(){
        if (!validateFullName()){
            return;
        }
        if (!validatePhone()){
            return;
        }
        if (!validateRegPass()){
            return;
        }
        if (!validateRetype()){
            return;
        }

        checkRegForm();

        Toast.makeText(getApplicationContext(), "Registration Successful!", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);

    }

    /*Check validate form*/
    private void checkRegForm(){
        String name = InputName.getText().toString().trim();
        String phone = InputPhone.getText().toString().trim();
        String password = InputRegPass.getText().toString().trim();

        if (!name.isEmpty() && !phone.isEmpty() && !password.isEmpty()) {
            checkRegister(name, phone, password);
        } else {
            Toast.makeText(getApplicationContext(), "Please enter your details!", Toast.LENGTH_LONG).show();
        }
    }

    private Boolean validateFullName(){
        if (InputName.getText().toString().trim().isEmpty()){
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(InputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }
        return true;
    }

    private Boolean validatePhone(){
        if (InputPhone.getText().toString().trim().isEmpty()){
            inputLayoutPhone.setError(getString(R.string.err_msg_phone));
            requestFocus(InputPhone);
            return false;
        } else {
            inputLayoutPhone.setErrorEnabled(false);
        }
        return true;
    }

    private Boolean validateRegPass(){
        if (InputRegPass.getText().toString().trim().isEmpty()){
            inputLayoutRegPass.setError(getString(R.string.err_msg_password));
            requestFocus(InputRegPass);
            return false;
        } else {
            inputLayoutRegPass.setErrorEnabled(false);
        }
        return true;
    }

    private Boolean validateRetype(){
        String pass = InputRegPass.getText().toString();
        String mpass = InputRetype.getText().toString();

        if (InputRetype.getText().toString().trim().isEmpty()){
            inputLayoutReType.setError(getString(R.string.err_msg_retypepass));
            requestFocus(InputRetype);
            return false;
        } else {
            inputLayoutReType.setErrorEnabled(false);
            if (!pass.equals(mpass)){
                inputLayoutReType.setError(getString(R.string.err_msg_match));
                return false;
            }
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
                case R.id.inputname:
                    validateFullName();
                    break;
                case R.id.inputphone:
                    validatePhone();
                    break;
                case R.id.inputregpass:
                    validateRegPass();
                    break;
                case R.id.inputretype:
                    validateRetype();
                    break;
            }

        }
    }

    /*
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */
    private void checkRegister(final String name, final String phone, final String password){

        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObjt = new JSONObject(response);
                    boolean error = jObjt.getBoolean("error");

                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObjt.getString("uid");

                        JSONObject driver = jObjt.getJSONObject("driver");
                        String name = driver.getString("name");
                        String phone = driver.getString("phone");
                        String created_at = driver.getString("created_at");

                        // Inserting row in users table
                        db.addDriver(name, phone, uid, created_at);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObjt.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("phone", phone);
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
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
