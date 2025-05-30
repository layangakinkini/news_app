package com.layanga.tech_pulse;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class signup extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        //Set up password visibility toggle
        setupPasswordToggle(etPassword, true);
        setupPasswordToggle(etConfirmPassword, false);

        //Handle Sign Up button click
        Button btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> {

            //UPDATED: Get user input
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            //Validate input
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(signup.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            //Email format check
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(signup.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(signup.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            //Store user in Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("users");

            ref.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(signup.this, "Username already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        HelperClass helperClass = new HelperClass(username, email, password);
                        ref.child(username).setValue(helperClass)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(signup.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(signup.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(signup.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(signup.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        //Handle "Already have an account? Login" click
        TextView tvLogin = findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(signup.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional
        });
    }

    private void setupPasswordToggle(EditText editText, boolean isPrimaryPassword) {
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawableEnd = editText.getCompoundDrawables()[2];
                if (drawableEnd != null) {
                    int drawableWidth = drawableEnd.getBounds().width();
                    int touchX = (int) event.getX();
                    int width = editText.getWidth() - editText.getPaddingEnd();

                    if (touchX >= (width - drawableWidth)) {
                        if (isPrimaryPassword) {
                            isPasswordVisible = !isPasswordVisible;
                            updateInputType(editText, isPasswordVisible);
                            updateDrawable(editText, isPasswordVisible);
                        } else {
                            isConfirmPasswordVisible = !isConfirmPasswordVisible;
                            updateInputType(editText, isConfirmPasswordVisible);
                            updateDrawable(editText, isConfirmPasswordVisible);
                        }
                        editText.setSelection(editText.getText().length());
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void updateInputType(EditText editText, boolean visible) {
        if (visible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    private void updateDrawable(EditText editText, boolean visible) {
        int icon = visible ? R.drawable.visibility_24dp_b7b7b7_fill0_wght400_grad0_opsz24
                : R.drawable.visibility_off_24dp_b7b7b7_fill0_wght400_grad0_opsz24;
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0);
    }
}
