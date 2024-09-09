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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Water_Electricity extends AppCompatActivity {

    private EditText etName, etArea, etWaterImprovements, etElectricityImprovements, etAdditionalComments;
    private RadioGroup rgWaterQuality, rgElectricityQuality, rgWaterInterruptions, rgElectricityInterruptions,
            rgWaterCustomerService, rgElectricityCustomerService, rgOverallSatisfactionWater, rgOverallSatisfactionElectricity;
    private Button btnSubmit;
    private DatabaseReference databaseReference;
    private TextView tvComplaintCount;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.water_electricity);

        databaseReference = FirebaseDatabase.getInstance().getReference("Water_Electricity Survey");

        // Initializing views
        etName = findViewById(R.id.etName);
        etArea = findViewById(R.id.etArea);
        rgWaterQuality = findViewById(R.id.rgWaterQuality);
        rgElectricityQuality = findViewById(R.id.rgElectricityQuality);
        rgWaterInterruptions = findViewById(R.id.rgWaterInterruptions);
        rgElectricityInterruptions = findViewById(R.id.rgElectricityInterruptions);
        rgWaterCustomerService = findViewById(R.id.rgWaterCustomerService);
        rgElectricityCustomerService = findViewById(R.id.rgElectricityCustomerService);
        rgOverallSatisfactionWater = findViewById(R.id.rgOverallSatisfactionWater);
        rgOverallSatisfactionElectricity = findViewById(R.id.rgOverallSatisfactionElectricity);
        etWaterImprovements = findViewById(R.id.etWaterImprovements);
        etElectricityImprovements = findViewById(R.id.etElectricityImprovements);
        etAdditionalComments = findViewById(R.id.etAdditionalComments);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvComplaintCount = findViewById(R.id.tvComplaintCountwe);

        // Load complaint count when activity starts
        loadComplaintCount();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitData();
            }
        });
    }

    private void submitData() {
        String name = etName.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String waterQuality = getSelectedRadioButtonText(rgWaterQuality);
        String electricityQuality = getSelectedRadioButtonText(rgElectricityQuality);
        String waterInterruptions = getSelectedRadioButtonText(rgWaterInterruptions);
        String electricityInterruptions = getSelectedRadioButtonText(rgElectricityInterruptions);
        String waterCustomerService = getSelectedRadioButtonText(rgWaterCustomerService);
        String electricityCustomerService = getSelectedRadioButtonText(rgElectricityCustomerService);
        String overallSatisfactionWater = getSelectedRadioButtonText(rgOverallSatisfactionWater);
        String overallSatisfactionElectricity = getSelectedRadioButtonText(rgOverallSatisfactionElectricity);
        String waterImprovements = etWaterImprovements.getText().toString().trim();
        String electricityImprovements = etElectricityImprovements.getText().toString().trim();
        String additionalComments = etAdditionalComments.getText().toString().trim();

        // Validate required fields
        if (TextUtils.isEmpty(name)) {
            etName.setError("الاسم مطلوب");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(area)) {
            etArea.setError("المنطقة مطلوبة");
            etArea.requestFocus();
            return;
        }

        // Check if at least one optional field is filled
        if (TextUtils.isEmpty(waterQuality) && TextUtils.isEmpty(electricityQuality) && TextUtils.isEmpty(waterInterruptions) &&
                TextUtils.isEmpty(electricityInterruptions) && TextUtils.isEmpty(waterCustomerService) &&
                TextUtils.isEmpty(electricityCustomerService) && TextUtils.isEmpty(overallSatisfactionWater) &&
                TextUtils.isEmpty(overallSatisfactionElectricity) && TextUtils.isEmpty(waterImprovements) &&
                TextUtils.isEmpty(electricityImprovements) && TextUtils.isEmpty(additionalComments)) {


            showCustomToast("يرجى ملء حقل واحد على الأقل في الاستبيان");
            return;
        }

        // Create SurveyResponse object
        SurveyResponse surveyResponse = new SurveyResponse(name, area, waterQuality, electricityQuality, waterInterruptions,
                electricityInterruptions, waterCustomerService, electricityCustomerService, overallSatisfactionWater,
                overallSatisfactionElectricity, waterImprovements, electricityImprovements, additionalComments);

        // Send data to Firebase
        String surveyId = databaseReference.push().getKey();
        if (surveyId != null) {
            databaseReference.child(surveyId).setValue(surveyResponse)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Water_Electricity.this, "تم إرسال البيانات بنجاح", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.E_W).setOnClickListener(e -> {
                                Intent intent = new Intent(getApplicationContext(), home.class);
                                startActivity(intent);
                            });
                            clearForm();
                            loadComplaintCount(); // Update complaint count after submission
                        } else {
                            Toast.makeText(Water_Electricity.this, "فشل في إرسال البيانات", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private String getSelectedRadioButtonText(RadioGroup radioGroup) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        return selectedRadioButton != null ? selectedRadioButton.getText().toString() : "";
    }

    private void clearForm() {
        etName.setText("");
        etArea.setText("");
        rgWaterQuality.clearCheck();
        rgElectricityQuality.clearCheck();
        rgWaterInterruptions.clearCheck();
        rgElectricityInterruptions.clearCheck();
        rgWaterCustomerService.clearCheck();
        rgElectricityCustomerService.clearCheck();
        rgOverallSatisfactionWater.clearCheck();
        rgOverallSatisfactionElectricity.clearCheck();
        etWaterImprovements.setText("");
        etElectricityImprovements.setText("");
        etAdditionalComments.setText("");
    }

    private void loadComplaintCount() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int complaintCount = (int) dataSnapshot.getChildrenCount();

                tvComplaintCount.setText(" عدد الشكاوى و المقترحات: " + complaintCount);

                tvComplaintCount.setText("عدد الشكاوى: " + complaintCount);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Water_Electricity.this, "فشل في تحميل عدد الشكاوى", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // SurveyResponse class to hold survey data
    public static class SurveyResponse {
        public String name, area, waterQuality, electricityQuality, waterInterruptions, electricityInterruptions;
        public String waterCustomerService, electricityCustomerService, overallSatisfactionWater, overallSatisfactionElectricity;
        public String waterImprovements, electricityImprovements, additionalComments;

        public SurveyResponse() {
            // Default constructor required for calls to DataSnapshot.getValue(SurveyResponse.class)
        }

        public SurveyResponse(String name, String area, String waterQuality, String electricityQuality,
                              String waterInterruptions, String electricityInterruptions, String waterCustomerService,
                              String electricityCustomerService, String overallSatisfactionWater, String overallSatisfactionElectricity,
                              String waterImprovements, String electricityImprovements, String additionalComments) {
            this.name = name;
            this.area = area;
            this.waterQuality = waterQuality;
            this.electricityQuality = electricityQuality;
            this.waterInterruptions = waterInterruptions;
            this.electricityInterruptions = electricityInterruptions;
            this.waterCustomerService = waterCustomerService;
            this.electricityCustomerService = electricityCustomerService;
            this.overallSatisfactionWater = overallSatisfactionWater;
            this.overallSatisfactionElectricity = overallSatisfactionElectricity;
            this.waterImprovements = waterImprovements;
            this.electricityImprovements = electricityImprovements;
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
