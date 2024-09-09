package com.example.lnabin;
import android.annotation.SuppressLint;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class organizationSurvey extends AppCompatActivity {

    private EditText etName, etArea, etSpecificArea, etServiceImprovements, etAdditionalComments;
    private RadioGroup rgLocation, rgServiceQuality, rgAccessEase, rgWaitTime;
    private Button btnSubmit;
    private TextView tvComplaintCount;
    private ProgressBar progressBar; // Add ProgressBar
    private LottieAnimationView successAnimation; // Add Lottie Animation View
    private Handler handler = new Handler();
    private Runnable timeoutRunnable;
    private static final long TIMEOUT_DURATION = 5000; // 5 seconds timeout duration

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_survey);

        // ربط المعرفات من ملف XML
        etName = findViewById(R.id.etName);
        etArea = findViewById(R.id.etArea);
        etSpecificArea = findViewById(R.id.etSpecificArea);
        rgLocation = findViewById(R.id.rgLocation);
        rgServiceQuality = findViewById(R.id.rgServiceQuality);
        rgAccessEase = findViewById(R.id.rgAccessEase);
        rgWaitTime = findViewById(R.id.rgWaitTime);
        etServiceImprovements = findViewById(R.id.etServiceImprovements);
        etAdditionalComments = findViewById(R.id.etAdditionalComments);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvComplaintCount = findViewById(R.id.tvComplaintCountorg);
        progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar
        successAnimation = findViewById(R.id.successAnimation); // Initialize Lottie Animation View

        // Hide ProgressBar and success animation initially
        progressBar.setVisibility(View.GONE);
        successAnimation.setVisibility(View.GONE);

        // إعداد مرجع Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference surveyRef = database.getReference("organizationSurvey");

        // تحميل عدد الشكاوى عند بدء النشاط
        loadComplaintCount();

        // عند النقر على زر "إرسال"
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
    }

    private void submitData() {
        progressBar.setVisibility(View.VISIBLE); // Show ProgressBar
        startTimeout(); // Start timeout handler

        // جمع البيانات المدخلة
        String name = etName.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        int selectedLocationId = rgLocation.getCheckedRadioButtonId();
        String specificArea = etSpecificArea.getText().toString().trim();
        int selectedServiceQualityId = rgServiceQuality.getCheckedRadioButtonId();
        int selectedAccessEaseId = rgAccessEase.getCheckedRadioButtonId();
        int selectedWaitTimeId = rgWaitTime.getCheckedRadioButtonId();
        String serviceImprovements = etServiceImprovements.getText().toString().trim();
        String additionalComments = etAdditionalComments.getText().toString().trim();

        // التحقق من تعبئة الحقول المطلوبة
        if (TextUtils.isEmpty(area) || TextUtils.isEmpty(specificArea) || selectedLocationId == -1 || selectedServiceQualityId == -1 || selectedAccessEaseId == -1 || selectedWaitTimeId == -1) {
            showCustomToast("الرجاء ملء جميع الحقول المطلوبة.");
            progressBar.setVisibility(View.GONE); // Hide ProgressBar
            return;
        }

        // الحصول على النصوص من RadioButtons
        String location = ((RadioButton) findViewById(selectedLocationId)).getText().toString();
        String serviceQuality = ((RadioButton) findViewById(selectedServiceQualityId)).getText().toString();
        String accessEase = ((RadioButton) findViewById(selectedAccessEaseId)).getText().toString();
        String waitTime = ((RadioButton) findViewById(selectedWaitTimeId)).getText().toString();

        // إنشاء كائن للبيانات
        Map<String, String> surveyData = new HashMap<>();
        surveyData.put("Name", name);
        surveyData.put("Area", area);
        surveyData.put("Location", location);
        surveyData.put("SpecificArea", specificArea);
        surveyData.put("ServiceQuality", serviceQuality);
        surveyData.put("AccessEase", accessEase);
        surveyData.put("WaitTime", waitTime);
        surveyData.put("ServiceImprovements", serviceImprovements);
        surveyData.put("AdditionalComments", additionalComments);

        // إرسال البيانات إلى Firebase
        DatabaseReference surveyRef = FirebaseDatabase.getInstance().getReference("organizationSurvey");
        surveyRef.push().setValue(surveyData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
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
                    }, 2000);

                    loadComplaintCount(); // تحديث عدد الشكاوى بعد الإرسال
                } else {
                    Toast.makeText(organizationSurvey.this, "حدث خطأ أثناء إرسال الاستبيان.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startTimeout() {
        timeoutRunnable = () -> {
            progressBar.setVisibility(View.GONE);
            successAnimation.setVisibility(View.GONE);

            showCustomToast("تأكد من اتصال الشبكة، يبدو أنه غير متصل.");

            showCustomToast("أكد من اتصال الشبكة، يبدو أنه غير متصل.");

        };
        handler.postDelayed(timeoutRunnable, TIMEOUT_DURATION);
    }

    private void loadComplaintCount() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference surveyRef = database.getReference("organizationSurvey");

        surveyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int complaintCount = (int) dataSnapshot.getChildrenCount();
                tvComplaintCount.setText("عدد الشكاوى و المقترحات : " + complaintCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(organizationSurvey.this, "فشل في تحميل عدد الشكاوى", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(organizationSurvey.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
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
