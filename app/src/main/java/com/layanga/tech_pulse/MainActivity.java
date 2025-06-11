package com.layanga.tech_pulse;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private String currentCategory = "all";

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private LinearLayout newsCardsContainer;
    private ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        newsCardsContainer = findViewById(R.id.news_cards_container);
        loadingSpinner = findViewById(R.id.loading_spinner);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navView      = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        ImageView hamburger = findViewById(R.id.news_menu_icon);
        hamburger.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        fetchNewsFromFirebase();

        ImageButton sportBtn = findViewById(R.id.sport_button);
        ImageButton educationBtn = findViewById(R.id.education_button);
        ImageButton globalBtn = findViewById(R.id.global_button);

        sportBtn.setOnClickListener(v -> fetchNewsFromCategory("sport"));
        educationBtn.setOnClickListener(v -> fetchNewsFromCategory("education"));
        globalBtn.setOnClickListener(v -> fetchNewsFromCategory("event"));

        fetchNewsFromFirebase();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }

                if (!currentCategory.equals("all")) {
                    currentCategory = "all"; // Reset the category
                    fetchNewsFromFirebase(); // Load all news
                    Toast.makeText(MainActivity.this, "Showing all news", Toast.LENGTH_SHORT).show();
                } else {
                    finish(); // Default back behavior
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dev_info) { // Make sure this ID matches the one in your nav menu XML
            startActivity(new Intent(MainActivity.this, dev_info.class));
        } else if (id == R.id.nav_user_info) {
            startActivity(new Intent(MainActivity.this, user_info.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }



    private void fetchNewsFromFirebase() {
        currentCategory = "all";
        loadingSpinner.setVisibility(View.VISIBLE);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("news");
        List<NewsItem> newsList = new ArrayList<>();

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot categorySnap : snapshot.getChildren()) {
                    for (DataSnapshot newsSnap : categorySnap.getChildren()) {
                        NewsItem item = newsSnap.getValue(NewsItem.class);
                        if (item != null) {
                            newsList.add(item);
                        }
                    }
                }

                if (!newsList.isEmpty()) {
                    displayNews(newsList);
                } else {
                    Toast.makeText(MainActivity.this, "No news available", Toast.LENGTH_SHORT).show();
                    newsCardsContainer.removeAllViews();
                }

                loadingSpinner.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Data load failed", error.toException());
                loadingSpinner.setVisibility(View.GONE);
            }
        });
    }

    private void fetchNewsFromCategory(String category) {
        currentCategory = category;
        loadingSpinner.setVisibility(View.VISIBLE);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("news").child(category);
        List<NewsItem> newsList = new ArrayList<>();

        dbRef.limitToFirst(6).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot newsSnap : snapshot.getChildren()) {
                    NewsItem item = newsSnap.getValue(NewsItem.class);
                    if (item != null) newsList.add(item);
                }

                displayNews(newsList);
                loadingSpinner.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Error loading " + category, error.toException());
                loadingSpinner.setVisibility(View.GONE);
            }
        });
    }

    private void displayNews(List<NewsItem> newsList) {
        newsCardsContainer.removeAllViews();

        for (NewsItem item : newsList) {

            View card = getLayoutInflater().inflate(R.layout.news_card, newsCardsContainer, false);

            ImageView imageView = card.findViewById(R.id.news_image);
            TextView titleView = card.findViewById(R.id.news_title);
            TextView dateView = card.findViewById(R.id.news_date);
            TextView contentView = card.findViewById(R.id.news_content);
            Button readMoreButton = card.findViewById(R.id.read_more_button);

            if (titleView != null && item.title != null) {
                titleView.setText(item.title);
            }
            if (dateView != null && item.date != null) {
                dateView.setText(item.date);
            }

            if (imageView != null && item.imageurl != null) {
                Picasso.get().load(item.imageurl).into(imageView);
            }

            if (contentView != null && item.content != null) {
                contentView.setText(item.content);
                contentView.setVisibility(View.GONE);
            }

            if (readMoreButton != null && contentView != null) {
                readMoreButton.setOnClickListener(v -> {
                    if (contentView.getVisibility() == View.GONE) {
                        contentView.setVisibility(View.VISIBLE);
                        readMoreButton.setText("Read Less");
                    } else {
                        contentView.setVisibility(View.GONE);
                        readMoreButton.setText("Read More");
                    }
                });
            }

            newsCardsContainer.addView(card);
        }
    }
}
