package com.layanga.tech_pulse;

import android.os.Bundle;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView; // 🔹 Added for displaying name and email
import android.content.SharedPreferences; // 🔹 Added for retrieving saved username

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot; // 🔹 Firebase imports
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class user_info extends AppCompatActivity {

    private View profileLayout;

    // 🔹 Declare TextViews
    private TextView nameTextView, emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user_info), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileLayout = findViewById(R.id.profile_layout);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnSignOut = findViewById(R.id.btnSignOut);

        btnEdit.setOnClickListener(v -> showEditPopup());
        btnSignOut.setOnClickListener(v -> showSignOutPopup());

        // 🔹 Initialize TextViews (make sure these IDs match your layout XML)
        nameTextView = findViewById(R.id.userName);
        emailTextView = findViewById(R.id.email);

        // 🔹 Fetch and display user info
        loadUserInfo();
    }

    // 🔹 Function to load user data from Firebase
    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);

                        nameTextView.setText("Username : " + name);
                        emailTextView.setText("Email : " + email);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Optional: Show error message
                }
            });
        }
    }

    private void showEditPopup() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_edit);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dimBackground(true);
        dialog.setOnDismissListener(d -> dimBackground(false));

        Button ok = dialog.findViewById(R.id.btn_ok);
        Button cancel = dialog.findViewById(R.id.btn_cancel);

        cancel.setOnClickListener(v -> dialog.dismiss());
        ok.setOnClickListener(v -> {
            // TODO: Handle form submission
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showSignOutPopup() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_signout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dimBackground(true);
        dialog.setOnDismissListener(d -> dimBackground(false));

        Button ok = dialog.findViewById(R.id.btn_ok);
        Button cancel = dialog.findViewById(R.id.btn_cancel);

        cancel.setOnClickListener(v -> dialog.dismiss());
        ok.setOnClickListener(v -> {
            // TODO: Handle sign out logic
            dialog.dismiss();
        });

        dialog.show();
    }

    private void dimBackground(boolean dim) {
        if (profileLayout != null) {
            profileLayout.setAlpha(dim ? 0.3f : 1.0f);
        }
    }
}
