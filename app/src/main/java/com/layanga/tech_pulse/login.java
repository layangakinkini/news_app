package com.layanga.tech_pulse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.text.InputType;


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

public class login extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvLogin;
    private boolean isPasswordVisible = false;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        //Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnlogin);
        tvLogin = findViewById(R.id.tvLogin);

        // *** Setup password visibility toggle ***
        setupPasswordToggle(etPassword);

        //Handle login button click
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(username, password);
        });

        //Redirect to signup page if user taps "sign up"
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, signup.class);
            startActivity(intent);
            finish();
        });
    }

    // Toggle password visibility icon and behavior
    private void setupPasswordToggle(EditText passwordEditText) {
        // Set initial icon (eye closed)
        updatePasswordDrawable(passwordEditText, isPasswordVisible);

        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawableEnd = passwordEditText.getCompoundDrawables()[2]; // Right drawable
                if (drawableEnd != null) {
                    int drawableWidth = drawableEnd.getBounds().width();
                    int touchX = (int) event.getX();
                    int width = passwordEditText.getWidth() - passwordEditText.getPaddingEnd();

                    if (touchX >= (width - drawableWidth)) {
                        // Toggle visibility state
                        isPasswordVisible = !isPasswordVisible;

                        updateInputType(passwordEditText, isPasswordVisible);
                        updatePasswordDrawable(passwordEditText, isPasswordVisible);

                        // Move cursor to end
                        passwordEditText.setSelection(passwordEditText.getText().length());
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

    private void updatePasswordDrawable(EditText editText, boolean visible) {
        int icon = visible
                ? R.drawable.visibility_24dp_b7b7b7_fill0_wght400_grad0_opsz24
                : R.drawable.visibility_off_24dp_b7b7b7_fill0_wght400_grad0_opsz24;
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0);
    }

    //Check login credentials
    private void loginUser(String username, String password) {
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String storedPassword = snapshot.child("password").getValue(String.class);
                    if (password.equals(storedPassword)) {
                        Toast.makeText(login.this, "Login successful", Toast.LENGTH_SHORT).show();

                        //Save username to SharedPreferences
                        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                .edit()
                                .putString("username", username)
                                .apply();

                        startActivity(new Intent(login.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(login.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(login.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(login.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}