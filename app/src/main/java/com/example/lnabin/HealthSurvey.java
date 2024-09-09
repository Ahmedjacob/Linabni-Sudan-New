package com.example.lnabin;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HealthSurvey extends AppCompatActivity {

    private EditText etName, etArea, etHealthImprovements, etAdditionalComments;
    private RadioGroup rgHealthQuality, rgAccessEase, rgWaitTime, rgMedicalStaffEfficiency,
            rgComfortAndCleanliness, rgMedicineAvailability, rgOverallServiceQuality;
    private RadioGroup rgLocation;
    private Button btnSubmit;
    private TextView tvComplaintCount;
    private ProgressBar progressBar; // Add ProgressBar
    private LottieAnimationView successAnimation; // Add Lottie Animation View

    private DatabaseReference databaseReference;
    private Handler handler = new Handler();
    private Runnable timeoutRunnable;
    private static final long TIMEOUT_DURATION = 9000; // 5 seconds timeout duration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_survey);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("HealthSurvey");

        // Initialize views
        etName = findViewById(R.id.etName);
        etArea = findViewById(R.id.etArea);
        rgHealthQuality = findViewById(R.id.rgHealthQuality);
        rgAccessEase = findViewById(R.id.rgAccessEase);
        rgWaitTime = findViewById(R.id.rgWaitTime);
        rgMedicalStaffEfficiency = findViewById(R.id.rgMedicalStaffEfficiency);
        rgComfortAndCleanliness = findViewById(R.id.rgComfortAndCleanliness);
        rgMedicineAvailability = findViewById(R.id.rgMedicineAvailability);
        rgOverallServiceQuality = findViewById(R.id.rgOverallServiceQuality);
        etHealthImprovements = findViewById(R.id.etHealthImprovements);
        etAdditionalComments = findViewById(R.id.etAdditionalComments);
        rgLocation = findViewById(R.id.rgLocation);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvComplaintCount = findViewById(R.id.tvComplaintCount);
        progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar
        successAnimation = findViewById(R.id.successAnimation); // Initialize Lottie Animation View

        // Hide ProgressBar and success animation initially
        progressBar.setVisibility(View.GONE);
        successAnimation.setVisibility(View.GONE);

        // Set up the submit button click listener
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    submitData();
                } else {
                    showCustomToast("لا يوجد اتصال بالشبكة");
                }
            }
        });

        // Load the complaint count when the activity is created
        loadComplaintCount();
    }

    private void submitData() {
        progressBar.setVisibility(View.VISIBLE); // Show ProgressBar
        startTimeout(); // Start timeout handler

        String name = etName.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String healthQuality = getSelectedRadioButtonText(rgHealthQuality);
        String accessEase = getSelectedRadioButtonText(rgAccessEase);
        String waitTime = getSelectedRadioButtonText(rgWaitTime);
        String medicalStaffEfficiency = getSelectedRadioButtonText(rgMedicalStaffEfficiency);
        String comfortAndCleanliness = getSelectedRadioButtonText(rgComfortAndCleanliness);
        String medicineAvailability = getSelectedRadioButtonText(rgMedicineAvailability);
        String overallServiceQuality = getSelectedRadioButtonText(rgOverallServiceQuality);
        String location = getSelectedRadioButtonText(rgLocation);
        String healthImprovements = etHealthImprovements.getText().toString().trim();
        String additionalComments = etAdditionalComments.getText().toString().trim();

        // Validate required fields
        if (TextUtils.isEmpty(name)) {
            etName.setError("الاسم مطلوب");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(area)) {
            etArea.setError("المنطقة السكنية مطلوبة");
            etArea.requestFocus();
            return;
        }

        // Check if at least one of the optional fields is filled
        if (TextUtils.isEmpty(healthQuality) && TextUtils.isEmpty(accessEase) && TextUtils.isEmpty(waitTime) &&
                TextUtils.isEmpty(medicalStaffEfficiency) && TextUtils.isEmpty(comfortAndCleanliness) &&
                TextUtils.isEmpty(medicineAvailability) && TextUtils.isEmpty(overallServiceQuality) &&
                TextUtils.isEmpty(healthImprovements) && TextUtils.isEmpty(additionalComments)) {

            showCustomToast("يرجى ملء حقل واحد على الأقل في الاستبيان");
            progressBar.setVisibility(View.GONE); // Hide ProgressBar
            return;
        }

        // Create SurveyResponse object
        SurveyResponse surveyResponse = new SurveyResponse(name, area, healthQuality, accessEase, waitTime,
                medicalStaffEfficiency, comfortAndCleanliness, medicineAvailability, overallServiceQuality,
                healthImprovements, additionalComments);

        // Send data to Firebase
        String surveyId = databaseReference.push().getKey();
        if (surveyId != null) {
            databaseReference.child(surveyId).setValue(surveyResponse)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE); // Hide ProgressBar
                        handler.removeCallbacks(timeoutRunnable); // Remove timeout callback

                        if (task.isSuccessful()) {
                            // Show success animation
                            successAnimation.setVisibility(View.VISIBLE);
                            successAnimation.playAnimation();

                            // Hide success animation and navigate to home after 2 seconds
                            new Handler().postDelayed(() -> {
                                successAnimation.setVisibility(View.GONE);
                                Intent intent = new Intent(getApplicationContext(), home.class);
                                startActivity(intent);
                            }, 2000); showCustomToast("تم إرسال الاستبيان بنجاح");

                            clearForm();
                            loadComplaintCount(); // Update the complaint count after submission
                        } else {
                            Toast.makeText(HealthSurvey.this, "فشل في إرسال البيانات", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void startTimeout() {
        timeoutRunnable = () -> {
            progressBar.setVisibility(View.GONE);
            successAnimation.setVisibility(View.GONE);
            showCustomToast("تأكد من اتصال الشبكة، يبدو أنه غير متصل.");
        };
        handler.postDelayed(timeoutRunnable, TIMEOUT_DURATION);
    }

    private void loadComplaintCount() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int complaintCount = (int) dataSnapshot.getChildrenCount();
                tvComplaintCount.setText("عدد الشكاوى و المقترحات: " + complaintCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HealthSurvey.this, "فشل في تحميل عدد الشكاوى", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedRadioButtonText(RadioGroup radioGroup) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        return selectedRadioButton != null ? selectedRadioButton.getText().toString() : "";
    }

    private void clearForm() {
        etName.setText("");
        etArea.setText("");
        rgHealthQuality.clearCheck();
        rgAccessEase.clearCheck();
        rgWaitTime.clearCheck();
        rgMedicalStaffEfficiency.clearCheck();
        rgComfortAndCleanliness.clearCheck();
        rgMedicineAvailability.clearCheck();
        rgOverallServiceQuality.clearCheck();
        rgLocation.clearCheck();
        etHealthImprovements.setText("");
        etAdditionalComments.setText("");
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(HealthSurvey.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // SurveyResponse class to hold survey data
    public static class SurveyResponse {
        public String name, location, area, healthQuality, accessEase, waitTime;
        public String medicalStaffEfficiency, comfortAndCleanliness, medicineAvailability, overallServiceQuality;
        public String healthImprovements, additionalComments;

        public SurveyResponse() {
            // Default constructor required for calls to DataSnapshot.getValue(SurveyResponse.class)
        }

        public SurveyResponse(String name, String area, String healthQuality, String accessEase, String waitTime,
                              String medicalStaffEfficiency, String comfortAndCleanliness, String medicineAvailability,
                              String overallServiceQuality, String healthImprovements, String additionalComments) {
            this.name = name;
            this.area = area;
            this.healthQuality = healthQuality;
            this.accessEase = accessEase;
            this.waitTime = waitTime;
            this.medicalStaffEfficiency = medicalStaffEfficiency;
            this.comfortAndCleanliness = comfortAndCleanliness;
            this.medicineAvailability = medicineAvailability;
            this.overallServiceQuality = overallServiceQuality;
            this.healthImprovements = healthImprovements;
            this.additionalComments = additionalComments;
            this.location = location;
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
