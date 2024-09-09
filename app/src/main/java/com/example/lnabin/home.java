package com.example.lnabin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class home  extends AppCompatActivity {
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);

        findViewById(R.id.E_W).setOnClickListener(e->{

            Intent intent = new Intent(getApplicationContext(),Water_Electricity.class);
            startActivity(intent);


        });
        findViewById(R.id.Educt).setOnClickListener(e->{

            Intent intent = new Intent(getApplicationContext(),Education_Survey.class);
            startActivity(intent);


        });
        findViewById(R.id.Helth).setOnClickListener(e->{

            Intent intent = new Intent(getApplicationContext(),HealthSurvey.class);
            startActivity(intent);


        });
        findViewById(R.id.Scur).setOnClickListener(e->{

            Intent intent = new Intent(getApplicationContext(),Security_Survey.class);
            startActivity(intent);


        });
        findViewById(R.id.Org).setOnClickListener(e->{

            Intent intent = new Intent(getApplicationContext(),organizationSurvey.class);
            startActivity(intent);


        });
        findViewById(R.id.Another).setOnClickListener(e->{

            Intent intent = new Intent(getApplicationContext(),Tangible_Service_Survey.class);
            startActivity(intent);
        });
        findViewById(R.id.exit).setOnClickListener(e->{

            Intent intent = new Intent(getApplicationContext(),sign_in.class);
            startActivity(intent);
            finish();
        });







    }
}

