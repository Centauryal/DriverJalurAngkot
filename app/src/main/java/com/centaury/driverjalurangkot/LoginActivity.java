package com.centaury.driverjalurangkot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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

import com.android.volley.Request;
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

    @BindView(R.id.resend_sms)
    TextView ResendSMS;

    @BindView(R.id.counttimer)
    TextView CountTimerResend;

    private TextInputLayout inputLayoutDriver;
    private EditText InputDriver, InputSMS;
    private Button BtnLogin, VerifySMS;

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    String unique_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.login);

        ButterKnife.bind(this);

        inputLayoutDriver = (TextInputLayout)findViewById(R.id.inputlayoutlogin);
        InputDriver = (EditText) findViewById(R.id.inputlogin);
        InputSMS = (EditText) findViewById(R.id.inputsms);
        BtnLogin = (Button)findViewById(R.id.btnLogin);
        VerifySMS = (Button) findViewById(R.id.btn_verify_sms);

        viewPager = (ViewPager) findViewById(R.id.viewPagerVertical);

        InputDriver.addTextChangedListener(new MyTextWatcher(InputDriver));

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

        unique_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        adapter = new ViewPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        BtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginForm();
            }
        });

        VerifySMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checklogForm();
            }
        });

        RegisterScreen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });

        ResendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = InputDriver.getText().toString().trim();
                setResendSMS(login, unique_id);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideDialog();
    }

    /*Validate Form*/
    private void loginForm(){
        if (!validateLogin()){
            return;
        }
        viewPager.setCurrentItem(1);
        countTimerResend();

    }

    /*Check validate form*/
    private void checklogForm(){
        String login = InputDriver.getText().toString().trim();
        String password = InputSMS.getText().toString().trim();

        // Check for empty data in the form
        if (!login.isEmpty() && !password.isEmpty()){
            // login user
            checkLogin(login, password, unique_id);
            Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
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
            }

        }
    }

    /*
    function to verify login details in mysql db
    */
    private void checkLogin(final String hp, final String password, final String device) {
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

                    // user successfully logged in
                    // Create login session
                    session.setLogin(true);

                    // Now store the user in SQLite
                    JSONObject driver = jObjt.getJSONObject("data");
                    String hp = driver.getString("hp");
                    String nama = driver.getString("nama");
                    String nopol = driver.getString("nopol");

                    // inserting row in drivers table
                    db.addDriver(nama, hp, nopol);

                    // Launch Main Activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                } catch (JSONException e) {
                    // Json Error
                    e.printStackTrace();
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
                params.put("deviceid", device);

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        hideDialog();
    }

    private void setResendSMS(final String hp, final String deviceid) {
        // Tag used to cancel request
        String tag_string_req = "req_resendsms";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_RESENDSMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                // boolean flag saying device is waiting for sms
                session.setIsWaitingForSms(true);

                Toast.makeText(getApplicationContext(), "SMS Telah Dikirim, Mohon Tunggu!", Toast.LENGTH_LONG).show();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            /**
             * Passing user parameters to our server
             * @return
             */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("hp", hp);
                params.put("deviceid", deviceid);

                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        public Object instantiateItem(View collection, int position) {

            int resId = 0;
            switch (position) {
                case 0:
                    resId = R.id.layout_login;
                    break;
                case 1:
                    resId = R.id.layout_verify;
                    break;
            }
            return findViewById(resId);
        }
    }

    private void countTimerResend(){
        new CountDownTimer(30000, 1000){
            public void onTick(long millisUntilFinished){
                CountTimerResend.setText("" + millisUntilFinished / 1000);
                ResendSMS.setEnabled(false);
                ResendSMS.setTextColor(Color.GRAY);
            }
            public void onFinish(){
                CountTimerResend.setVisibility(View.INVISIBLE);
                ResendSMS.setEnabled(true);
                ResendSMS.setTextColor(Color.BLUE);
            }
        }.start();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (!pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onBackPressed() {

        if (viewPager.getCurrentItem()== 1) {
            viewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
            finish();
        }
    }
}
