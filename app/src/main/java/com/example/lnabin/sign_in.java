package com.example.lnabin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class sign_in extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private LottieAnimationView lottieAnimationView;
    private Handler handler;
    private Runnable timeoutRunnable;
    private static final long TIMEOUT_DURATION = 10000; // 10 seconds
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_LAST_USERNAME = "lastUsername";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        // Initialize EditText fields and Lottie animation view
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);

        handler = new Handler();

        // Retrieve and set the last username
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastUsername = prefs.getString(KEY_LAST_USERNAME, "");
        usernameEditText.setText(lastUsername);

        // Example button to trigger login check
        findViewById(R.id.login).setOnClickListener(view -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.length() < 3) {
                showCustomToast("اسم المستخدم يجب أن يتكون من ثلاث حروف على الأقل");
                return;
            }

            if (!username.isEmpty() && !password.isEmpty()) {

                if (isNetworkAvailable()) {
                    showLoadingAnimation(true);
                    startTimeout();
                    checkLogin(username, password);
                } else {
                    showCustomToast("الرجاء التحقق من اتصال الإنترنت");
                }
            } else {
                showCustomToast("من فضلك أدخل اسم المستخدم وكلمة المرور");
            }
        });

        // "Sign up" button to go to the sign-up activity
        findViewById(R.id.sinup_l).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), sign_up.class);
            startActivity(intent);
            finish();
        });
    }

    private void showLoadingAnimation(boolean show) {
        if (show) {
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        } else {
            lottieAnimationView.setVisibility(View.GONE);
            lottieAnimationView.cancelAnimation();
        }
    }

    private void checkLogin(String username, String password) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Signup");

        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                showLoadingAnimation(false);
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String dbPassword = userSnapshot.child("password").getValue(String.class);
                        if (dbPassword != null && dbPassword.equals(password)) {

                            showCustomToast(username + " مرحبا ");
                            // Save the username in SharedPreferences
                            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(KEY_LAST_USERNAME, username);
                            editor.apply();

                            Intent intent = new Intent(getApplicationContext(), home.class);
                            startActivity(intent);
                            finish();
                        } else {
                            showCustomToast("كلمة المرور غير صحيحة");
                        }
                    }
                } else {
                    showCustomToast("اسم المستخدم غير موجود");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                stopTimeout();
                showLoadingAnimation(false);
                showCustomToast("حدث خطأ");
            }
        });
    }

    private void startTimeout() {
        timeoutRunnable = () -> {
            showLoadingAnimation(false);
            showCustomToast("انتهت مهلة الاتصال");
        };
        handler.postDelayed(timeoutRunnable, TIMEOUT_DURATION);
    }

    private void stopTimeout() {
        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable);
        }
    }

    private boolean isNetworkAvailable() {
        // Here you should implement a method to check network availability
        // This is a placeholder method and should be modified accordingly
        return true;
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
