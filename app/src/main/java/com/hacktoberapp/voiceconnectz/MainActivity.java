package com.hacktoberapp.voiceconnectz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.hacktoberapp.voiceconnectz.login.Login;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sharedPreferences = this.getSharedPreferences("TOKENS", Context.MODE_PRIVATE);
        String google_access_token = sharedPreferences.getString("google_access_token",null);
        String google_refresh_token = sharedPreferences.getString("google_refresh_token",null);

        if(google_access_token ==null && google_refresh_token==null){


            // Initiate the LOGIN

            System.out.println(
                    "NO TOKEN ..... PERFORM LOGIN"
            );

            Intent intent  = new Intent(MainActivity.this, Login.class);
            startActivity(intent);






        }




    }
}