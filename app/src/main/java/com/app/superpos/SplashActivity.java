package com.app.superpos;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.app.superpos.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {


    public static int splashTimeOut = 1000;
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String email = sp.getString(Constant.SP_EMAIL, "");
        String shopName = sp.getString(Constant.SP_SHOP_NAME, "");

        setContentView(R.layout.activity_splash);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this,  MainActivity.class);
            if (email.length() >= 3 && shopName.length()>=3 ) {
                intent = new Intent(SplashActivity.this,  MainActivity.class);
            }
            startActivity(intent);
            finish();
        }, splashTimeOut);
    }
}

