package com.example.lnabin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class helow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.helow);

        Thread thread  = new Thread(){

            @Override
            public void run() {

                try {
                    sleep(1000);
                    Intent intent = new Intent(getApplicationContext(),sign_in.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        thread.start();



}}