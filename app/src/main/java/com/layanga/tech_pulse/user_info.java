package com.layanga.tech_pulse;

import android.os.Bundle;
import android.Manifest;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.ImageView;
import androidx.annotation.NonNull;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class user_info extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 100;

    private View profileLayout;

    //Declare TextViews
    private TextView nameTextView, emailTextView;
    private ImageView profileIcon;

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

        //Initialize TextViews (make sure these IDs match your layout XML)
        nameTextView = findViewById(R.id.userName);
        emailTextView = findViewById(R.id.email);
        profileIcon = findViewById(R.id.profileIcon);

        // Set click listener on profile icon to pick image
        profileIcon.setOnClickListener(v -> checkPermissionAndOpenImageChooser());

        //Fetch and display user info
        loadUserInfo();
    }

    // Check permission and open image chooser
    private void checkPermissionAndOpenImageChooser() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            openImageChooser();
        }
    }

    // Open the system image picker
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            } else {
                Toast.makeText(this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle image chooser result                                 // **ADDED**
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // Display the selected image in the ImageView          // **ADDED**
            profileIcon.setImageURI(imageUri);
        }
    }


    //Function to load user data from Firebase
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

        EditText editUsername = dialog.findViewById(R.id.edit_username);
        EditText editEmail = dialog.findViewById(R.id.edit_email);
        Button ok = dialog.findViewById(R.id.btn_ok);
        Button cancel = dialog.findViewById(R.id.btn_cancel);

        TextView warningText = dialog.findViewById(R.id.username_warning);
        warningText.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String currentUsername = prefs.getString("username", null);

        if (currentUsername != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUsername);

            // Pre-fill EditTexts with current values
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String existingUsername = snapshot.child("username").getValue(String.class);
                        String existingEmail = snapshot.child("email").getValue(String.class);

                        editUsername.setText(existingUsername);
                        editEmail.setText(existingEmail);
                        editUsername.setTextColor(Color.GRAY);
                        editEmail.setTextColor(Color.GRAY);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(user_info.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        cancel.setOnClickListener(v -> dialog.dismiss());

        ok.setOnClickListener(v -> {
            String newUsername = editUsername.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            warningText.setVisibility(View.VISIBLE);

            new android.os.Handler().postDelayed(() -> {
                if (currentUsername != null) {
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users");

                    dbRef.child(currentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Copy data to new username key
                                dbRef.child(newUsername).setValue(snapshot.getValue(), (error, ref) -> {
                                    if (error == null) {
                                        // Update email and username fields
                                        dbRef.child(newUsername).child("username").setValue(newUsername);
                                        dbRef.child(newUsername).child("email").setValue(newEmail);

                                        // Delete old username key
                                        dbRef.child(currentUsername).removeValue();

                                        // Update SharedPreferences
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("username", newUsername); // Updated username
                                        editor.apply();

                                        // Update UI
                                        nameTextView.setText("Username : " + newUsername);
                                        emailTextView.setText("Email : " + newEmail);

                                        Toast.makeText(user_info.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(user_info.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(user_info.this, "Database error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }, 3000); // 3-second delay
        });

        dialog.show();
    }

    //Sign Out Popup
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
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            Intent intent = new Intent(user_info.this, login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    //Background Dimming Function
    private void dimBackground(boolean dim) {
        if (profileLayout != null) {
            profileLayout.setAlpha(dim ? 0.3f : 1.0f);
        }
    }
}
