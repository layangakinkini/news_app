package com.layanga.tech_pulse;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class signup extends AppCompatActivity {

    private EditText etPassword, etConfirmPassword;
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

        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // 🔥 Set up password visibility toggle
        setupPasswordToggle(etPassword, true);
        setupPasswordToggle(etConfirmPassword, false);

        // ✅ Handle Sign Up button click
        Button btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(signup.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional: close signup activity
        });

        // ✅ Handle "Already have an account? Login" click
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
