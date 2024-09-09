package com.example.lnabin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class Tangible_Service_Survey extends AppCompatActivity {

    private EditText etServiceName, etName, etArea, etServiceImprovements, etAdditionalComments;
    private RadioGroup rgServiceQuality, rgServiceInterruptions, rgCustomerService, rgOverallSatisfaction;
    private Button btnSubmit;
    private TextView tvComplaintCount;

    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tangible_service_survey);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Tangible_Service_Survey");

        // Find views
        etServiceName = findViewById(R.id.etServiceName);
        etName = findViewById(R.id.etName);
        etArea = findViewById(R.id.etArea);
        etServiceImprovements = findViewById(R.id.etServiceImprovements);
        etAdditionalComments = findViewById(R.id.etAdditionalComments);
        rgServiceQuality = findViewById(R.id.rgServiceQuality);
        rgServiceInterruptions = findViewById(R.id.rgServiceInterruptions);
        rgCustomerService = findViewById(R.id.rgCustomerService);
        rgOverallSatisfaction = findViewById(R.id.rgOverallSatisfaction);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvComplaintCount = findViewById(R.id.tvComplaintCountotng);

        // Load complaint count when activity starts
        loadComplaintCount();

        btnSubmit.setOnClickListener(v -> submitSurvey());
    }

    private void submitSurvey() {
        String serviceName = etServiceName.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String serviceImprovements = etServiceImprovements.getText().toString().trim();
        String additionalComments = etAdditionalComments.getText().toString().trim();

        int serviceQualityId = rgServiceQuality.getCheckedRadioButtonId();
        int serviceInterruptionsId = rgServiceInterruptions.getCheckedRadioButtonId();
        int customerServiceId = rgCustomerService.getCheckedRadioButtonId();
        int overallSatisfactionId = rgOverallSatisfaction.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(serviceName) ||
                serviceQualityId == -1 ||
                serviceInterruptionsId == -1 ||
                customerServiceId == -1 ||
                overallSatisfactionId == -1) {

            showCustomToast("يرجى ملء جميع الحقول المطلوبة!");
            return;
        }

        // Get selected RadioButton text
        RadioButton rbServiceQuality = findViewById(serviceQualityId);
        RadioButton rbServiceInterruptions = findViewById(serviceInterruptionsId);
        RadioButton rbCustomerService = findViewById(customerServiceId);
        RadioButton rbOverallSatisfaction = findViewById(overallSatisfactionId);

        // Create a survey object
        Survey survey = new Survey(
                serviceName,
                name,
                area,
                rbServiceQuality.getText().toString(),
                rbServiceInterruptions.getText().toString(),
                rbCustomerService.getText().toString(),
                rbOverallSatisfaction.getText().toString(),
                serviceImprovements,
                additionalComments
        );

        // Save to Firebase
        databaseReference.push().setValue(survey).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Tangible_Service_Survey.this, "تم إرسال الاستبيان بنجاح!", Toast.LENGTH_SHORT).show();

                findViewById(R.id.E_W).setOnClickListener(e -> {
                    Intent intent = new Intent(getApplicationContext(), home.class);
                    startActivity(intent);
                });
                clearFields();
                loadComplaintCount(); // Update complaint count after submission
            } else {

                showCustomToast("فشل في إرسال الاستبيان يرجى المحاولة مرة أخرى.!");
            }
        });
    }

    private void clearFields() {
        etServiceName.setText("");
        etName.setText("");
        etArea.setText("");
        etServiceImprovements.setText("");
        etAdditionalComments.setText("");
        rgServiceQuality.clearCheck();
        rgServiceInterruptions.clearCheck();
        rgCustomerService.clearCheck();
        rgOverallSatisfaction.clearCheck();
    }

    private void loadComplaintCount() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int complaintCount = (int) dataSnapshot.getChildrenCount();
                tvComplaintCount.setText("عدد الشكاوى و المقترحات :" + complaintCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Tangible_Service_Survey.this, "فشل في تحميل عدد الشكاوى", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Define Survey class
    public static class Survey {
        public String serviceName, name, area, serviceQuality, serviceInterruptions, customerService, overallSatisfaction, serviceImprovements, additionalComments;

        public Survey() {
            // Default constructor required for calls to DataSnapshot.getValue(Survey.class)
        }

        public Survey(String serviceName, String name, String area, String serviceQuality, String serviceInterruptions,
                      String customerService, String overallSatisfaction, String serviceImprovements, String additionalComments) {
            this.serviceName = serviceName;
            this.name = name;
            this.area = area;
            this.serviceQuality = serviceQuality;
            this.serviceInterruptions = serviceInterruptions;
            this.customerService = customerService;
            this.overallSatisfaction = overallSatisfaction;
            this.serviceImprovements = serviceImprovements;
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
