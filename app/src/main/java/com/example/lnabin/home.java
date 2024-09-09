package com.example.lnabin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class home extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);

        if (savedInstanceState != null) {
            // استرجاع البيانات إذا كانت موجودة
            String data = savedInstanceState.getString("key", "default");
            // قم باستخدام البيانات المسترجعة كما تحتاج
        }

        findViewById(R.id.E_W).setOnClickListener(e -> {
            Intent intent = new Intent(getApplicationContext(), Water_Electricity.class);
            startActivity(intent);
        });
        findViewById(R.id.Educt).setOnClickListener(e -> {
            Intent intent = new Intent(getApplicationContext(), Education_Survey.class);
            startActivity(intent);
        });
        findViewById(R.id.Helth).setOnClickListener(e -> {
            Intent intent = new Intent(getApplicationContext(), HealthSurvey.class);
            startActivity(intent);
        });
        findViewById(R.id.Scur).setOnClickListener(e -> {
            Intent intent = new Intent(getApplicationContext(), Security_Survey.class);
            startActivity(intent);
        });
        findViewById(R.id.Org).setOnClickListener(e -> {
            Intent intent = new Intent(getApplicationContext(), organizationSurvey.class);
            startActivity(intent);
        });
        findViewById(R.id.Another).setOnClickListener(e -> {
            Intent intent = new Intent(getApplicationContext(), Tangible_Service_Survey.class);
            startActivity(intent);
        });
        findViewById(R.id.exit).setOnClickListener(e -> {
            Intent intent = new Intent(getApplicationContext(), sign_in.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // حفظ البيانات المهمة
        outState.putString("key", "value");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // التعامل مع البيانات الجديدة إذا كان النشاط موجودًا بالفعل في القمة
    }
}
