package com.example.lnabin;

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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class Education_Survey extends AppCompatActivity {

    private EditText etName, etArea, etEducationImprovements, etAdditionalComments;
    private RadioGroup rgEducationQuality, rgFacilityCondition, rgTeacherQuality, rgServiceAvailability, rgOverallSatisfactionEducation;
    private Button btnSubmit;
    private TextView tvRatingCount;

    private DatabaseReference databaseReference;
    private DatabaseReference ratingCountRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education_survey);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("EducationSurveys");
        ratingCountRef = FirebaseDatabase.getInstance().getReference("EducationSurveys/ratingCount");

        // Initialize UI elements
        etName = findViewById(R.id.etName);
        etArea = findViewById(R.id.etArea);
        etEducationImprovements = findViewById(R.id.etEducationImprovements);
        etAdditionalComments = findViewById(R.id.etAdditionalComments);

        rgEducationQuality = findViewById(R.id.rgEducationQuality);
        rgFacilityCondition = findViewById(R.id.rgFacilityCondition);
        rgTeacherQuality = findViewById(R.id.rgTeacherQuality);
        rgServiceAvailability = findViewById(R.id.rgServiceAvailability);
        rgOverallSatisfactionEducation = findViewById(R.id.rgOverallSatisfactionEducation);

        btnSubmit = findViewById(R.id.btnSubmit);
        tvRatingCount = findViewById(R.id.tvEducationComplaintsCountEdu);

        // Fetch and display rating count
        fetchAndDisplayRatingCount();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSurvey();
            }
        });
    }

    private void fetchAndDisplayRatingCount() {
        ratingCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long ratingCount = dataSnapshot.getValue(Long.class);
                    tvRatingCount.setText("عدد الشكاوى و المقترحات:" + ratingCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(Education_Survey.this, "فشل في جلب عدد الشكاوى و المقترحات:", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitSurvey() {
        // Validate inputs
        if (isAnyFieldEmpty()) {
            showCustomToast("يرجى ملء جميع الحقول");
            return;
        }

        // Get user inputs
        String name = etName.getText().toString();
        String area = etArea.getText().toString();
        String educationImprovements = etEducationImprovements.getText().toString();
        String additionalComments = etAdditionalComments.getText().toString();

        int selectedEducationQualityId = rgEducationQuality.getCheckedRadioButtonId();
        int selectedFacilityConditionId = rgFacilityCondition.getCheckedRadioButtonId();
        int selectedTeacherQualityId = rgTeacherQuality.getCheckedRadioButtonId();
        int selectedServiceAvailabilityId = rgServiceAvailability.getCheckedRadioButtonId();
        int selectedOverallSatisfactionEducationId = rgOverallSatisfactionEducation.getCheckedRadioButtonId();

        RadioButton rbEducationQuality = findViewById(selectedEducationQualityId);
        RadioButton rbFacilityCondition = findViewById(selectedFacilityConditionId);
        RadioButton rbTeacherQuality = findViewById(selectedTeacherQualityId);
        RadioButton rbServiceAvailability = findViewById(selectedServiceAvailabilityId);
        RadioButton rbOverallSatisfactionEducation = findViewById(selectedOverallSatisfactionEducationId);

        // Create a Survey object
        Survey survey = new Survey(
                name,
                area,
                rbEducationQuality.getText().toString(),
                rbFacilityCondition.getText().toString(),
                rbTeacherQuality.getText().toString(),
                rbServiceAvailability.getText().toString(),
                rbOverallSatisfactionEducation.getText().toString(),
                educationImprovements,
                additionalComments
        );

        // Save to Firebase
        databaseReference.push().setValue(survey)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update the rating count
                        ratingCountRef.setValue(ServerValue.increment(1)); // Increment rating count by 1

                        showCustomToast("تم إرسال الاستبيان بنجاح");
                        // Navigate to home screen
                        Intent intent = new Intent(getApplicationContext(), home.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Education_Survey.this, "فشل إرسال الاستبيان", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isAnyFieldEmpty() {
        return rgEducationQuality.getCheckedRadioButtonId() == -1 ||
                rgFacilityCondition.getCheckedRadioButtonId() == -1 ||
                rgTeacherQuality.getCheckedRadioButtonId() == -1 ||
                rgServiceAvailability.getCheckedRadioButtonId() == -1 ||
                rgOverallSatisfactionEducation.getCheckedRadioButtonId() == -1;
    }

    public static class Survey {
        public String name;
        public String area;
        public String educationQuality;
        public String facilityCondition;
        public String teacherQuality;
        public String serviceAvailability;
        public String overallSatisfactionEducation;
        public String educationImprovements;
        public String additionalComments;

        public Survey() {
            // Default constructor required for calls to DataSnapshot.getValue(Survey.class)
        }

        public Survey(String name, String area, String educationQuality, String facilityCondition,
                      String teacherQuality, String serviceAvailability, String overallSatisfactionEducation,
                      String educationImprovements, String additionalComments) {
            this.name = name;
            this.area = area;
            this.educationQuality = educationQuality;
            this.facilityCondition = facilityCondition;
            this.teacherQuality = teacherQuality;
            this.serviceAvailability = serviceAvailability;
            this.overallSatisfactionEducation = overallSatisfactionEducation;
            this.educationImprovements = educationImprovements;
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
