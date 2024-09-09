package com.example.lnabin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Security_Survey extends AppCompatActivity {

    private EditText etName, etArea, etSecurityImprovements, etAdditionalComments;
    private RadioGroup rgSecurityQuality, rgResponseTime, rgSafetyPerception, rgEmergencyServices;
    private Button btnSubmit;
    private DatabaseReference databaseReference;
    private TextView tvComplaintCount;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_survey);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("SecuritySurveys");

        // Initialize Views
        etName = findViewById(R.id.etName);
        etArea = findViewById(R.id.etArea);
        etSecurityImprovements = findViewById(R.id.etSecurityImprovements);
        etAdditionalComments = findViewById(R.id.etAdditionalComments);

        rgSecurityQuality = findViewById(R.id.rgSecurityQuality);
        rgResponseTime = findViewById(R.id.rgResponseTime);
        rgSafetyPerception = findViewById(R.id.rgSafetyPerception);
        rgEmergencyServices = findViewById(R.id.rgEmergencyServices);

        btnSubmit = findViewById(R.id.btnSubmit);
        tvComplaintCount = findViewById(R.id.tvComplaintCountsc);

        // Load complaint count when activity starts
        loadComplaintCount();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSurvey();
            }
        });
    }

    private void submitSurvey() {
        // Validate input
        if (validateInputs()) {
            String name = etName.getText().toString().trim();
            String area = etArea.getText().toString().trim();
            String securityImprovements = etSecurityImprovements.getText().toString().trim();
            String additionalComments = etAdditionalComments.getText().toString().trim();

            int securityQualityId = rgSecurityQuality.getCheckedRadioButtonId();
            int responseTimeId = rgResponseTime.getCheckedRadioButtonId();
            int safetyPerceptionId = rgSafetyPerception.getCheckedRadioButtonId();
            int emergencyServicesId = rgEmergencyServices.getCheckedRadioButtonId();

            RadioButton rbSecurityQuality = findViewById(securityQualityId);
            RadioButton rbResponseTime = findViewById(responseTimeId);
            RadioButton rbSafetyPerception = findViewById(safetyPerceptionId);
            RadioButton rbEmergencyServices = findViewById(emergencyServicesId);

            // Create a survey object
            Survey survey = new Survey(
                    name,
                    area,
                    rbSecurityQuality != null ? rbSecurityQuality.getText().toString() : "",
                    rbResponseTime != null ? rbResponseTime.getText().toString() : "",
                    rbSafetyPerception != null ? rbSafetyPerception.getText().toString() : "",
                    rbEmergencyServices != null ? rbEmergencyServices.getText().toString() : "",
                    securityImprovements,
                    additionalComments
            );

            // Push the survey to Firebase
            databaseReference.push().setValue(survey)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Toast.makeText(Security_Survey.this, "تم إرسال الاستبيان بنجاح", Toast.LENGTH_SHORT).show();
                                findViewById(R.id.E_W).setOnClickListener(e->{
                                    Intent intent = new Intent(getApplicationContext(), home.class);
                                    startActivity(intent);
                                });
                                clearFields();
                                loadComplaintCount(); // Update complaint count after submission
                            } else {
                                Toast.makeText(Security_Survey.this, "فشل إرسال الاستبيان، يرجى المحاولة لاحقاً", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private boolean validateInputs() {
        if (rgSecurityQuality.getCheckedRadioButtonId() == -1) {

            showCustomToast("يرجى تقييم جودة الخدمات الأمنية");
            return false;
        }
        if (rgResponseTime.getCheckedRadioButtonId() == -1) {

            showCustomToast("يرجى تقييم سرعة الاستجابة");
            return false;
        }
        if (rgSafetyPerception.getCheckedRadioButtonId() == -1) {

            showCustomToast("يرجى تقييم شعور الأمان");
            return false;
        }
        if (rgEmergencyServices.getCheckedRadioButtonId() == -1) {

            showCustomToast("يرجى تقييم جودة خدمات الطوارئ");
            return false;
        }
        return true;
    }

    private void clearFields() {
        etName.setText("");
        etArea.setText("");
        etSecurityImprovements.setText("");
        etAdditionalComments.setText("");
        rgSecurityQuality.clearCheck();
        rgResponseTime.clearCheck();
        rgSafetyPerception.clearCheck();
        rgEmergencyServices.clearCheck();
    }

    private void loadComplaintCount() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int complaintCount = (int) dataSnapshot.getChildrenCount();
                tvComplaintCount.setText("عدد الشكاوى: " + complaintCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Security_Survey.this, "فشل في تحميل عدد الشكاوى", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Survey class for Firebase
    public static class Survey {
        public String name;
        public String area;
        public String securityQuality;
        public String responseTime;
        public String safetyPerception;
        public String emergencyServices;
        public String securityImprovements;
        public String additionalComments;

        public Survey() {
            // Default constructor required for calls to DataSnapshot.getValue(Survey.class)
        }

        public Survey(String name, String area, String securityQuality, String responseTime, String safetyPerception, String emergencyServices, String securityImprovements, String additionalComments) {
            this.name = name;
            this.area = area;
            this.securityQuality = securityQuality;
            this.responseTime = responseTime;
            this.safetyPerception = safetyPerception;
            this.emergencyServices = emergencyServices;
            this.securityImprovements = securityImprovements;
            this.additionalComments = additionalComments;
        }
    }
    void showCustomToast(String message) {
        Toast toast = new Toast(getApplicationContext());
        View view = getLayoutInflater().inflate(R.layout.custom_toast, null);

        TextView text = view.findViewById(R.id.toast_message);
        text.setText(message);

        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
