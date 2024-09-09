package com.example.lnabin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
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

public class sign_up extends AppCompatActivity {

    private EditText emailEditText, nameEditText, usernameEditText, passwordEditText, mobileEditText;
    private TextView signupTextView;
    private LottieAnimationView lottieAnimationView;

    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private Handler handler;
    private Runnable timeoutRunnable;
    private static final long TIMEOUT_DURATION = 10000; // 10 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Link UI elements
        emailEditText = findViewById(R.id.mail);
        nameEditText = findViewById(R.id.name);
        usernameEditText = findViewById(R.id.username_u);
        passwordEditText = findViewById(R.id.password_u);
        mobileEditText = findViewById(R.id.mobphone);
        signupTextView = findViewById(R.id.sup);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Signup");

        handler = new Handler();

        // Handle "Signup" button click
        signupTextView.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String mobile = mobileEditText.getText().toString().trim();

            // Check if any fields are empty
            if (email.isEmpty() || name.isEmpty() || username.isEmpty() || password.isEmpty() || mobile.isEmpty()) {
                showCustomToast("يرجى ملء جميع الحقول");
                return;
            }

            // Check if username length is less than 3 characters
            if (username.length() < 3) {
                showCustomToast("اسم المستخدم يجب أن يتكون من ثلاث حروف على الأقل");
                return;
            }

            // Check internet connection
            if (!isNetworkAvailable()) {
                showCustomToast("الرجاء التحقق من اتصال الإنترنت");
                return;
            }

            // Check if username already exists
            usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Username already exists
                        showCustomToast("اسم المستخدم موجود بالفعل، يرجى اختيار اسم مستخدم آخر");
                    } else {
                        // Username does not exist, proceed with signup
                        showLoadingAnimation(true);
                        startTimeout();

                        // Create a user object
                        signup signup = new signup(email, name, username, password, mobile);

                        // Send data to Firebase
                        usersRef.child(username).setValue(signup).addOnCompleteListener(task -> {
                            stopTimeout();
                            showLoadingAnimation(false); // Hide loading animation
                            if (task.isSuccessful()) {
                                showCustomToast("تم التسجيل بنجاح");
                                Intent intent = new Intent(getApplicationContext(), sign_in.class);
                                startActivity(intent);
                                finish();
                            } else {
                                showCustomToast("فشل التسجيل، حاول مرة أخرى");
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showCustomToast("حدث خطأ أثناء التحقق من اسم المستخدم");
                }
            });
        });

        // Handle "Already have an account?" click
        findViewById(R.id.lin).setOnClickListener(e -> {
            Intent intent = new Intent(getApplicationContext(), sign_in.class);
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

    private void startTimeout() {
        timeoutRunnable = () -> {
            showLoadingAnimation(false);

            showCustomToast("تأكد من اتصال الشبكة، يبدو أنه غير متصل.");

            showCustomToast("أكد من اتصال الشبكة، يبدو أنه غير متصل.");

        };
        handler.postDelayed(timeoutRunnable, TIMEOUT_DURATION);
    }

    private void stopTimeout() {
        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable);
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    public static class signup {
        public String email;
        public String name;
        public String username;
        public String password;
        public String mobile;

        public signup() {
            // Required for Firebase
        }

        public signup(String email, String name, String username, String password, String mobile) {
            this.email = email;
            this.name = name;
            this.username = username;
            this.password = password;
            this.mobile = mobile;
        }
    }
}




