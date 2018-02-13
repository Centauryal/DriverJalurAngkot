package com.centaury.driverjalurangkot.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Created by Centaury on 15/01/2018.
 */

public class SessionManager {

    //LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    Editor editor;
    Context mcontext;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preference file name
    private static final String PREF_NAME = "DriverLogin";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    private static final String KEY_IS_WAITING_FOR_SMS = "IsWaitingForSms";

    public SessionManager(Context context){
        this.mcontext = context;
        pref = mcontext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn){
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // commit change
        editor.commit();

        Log.d(TAG, "User Login session modified!");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public void clearSession() {
        editor.clear();
        editor.commit();
    }

    public boolean isWaitingForSms() {
        return pref.getBoolean(KEY_IS_WAITING_FOR_SMS, false);
    }

    public void setIsWaitingForSms(boolean isWaiting) {
        editor.putBoolean(KEY_IS_WAITING_FOR_SMS, isWaiting);
        editor.commit();
    }
}
