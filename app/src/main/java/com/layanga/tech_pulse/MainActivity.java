package com.layanga.tech_pulse;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

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
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // View IDs for the 6 cards
    int[] cardIds = {
            R.id.news_card_1,
            R.id.news_card_2,
            R.id.news_card_3,
            R.id.news_card_4,
            R.id.news_card_5,
            R.id.news_card_6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fetchNewsFromFirebase();
    }

    private void fetchNewsFromFirebase() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("news");
        List<NewsItem> newsList = new ArrayList<>();

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot categorySnap : snapshot.getChildren()) {
                    for (DataSnapshot newsSnap : categorySnap.getChildren()) {
                        NewsItem item = newsSnap.getValue(NewsItem.class);
                        if (newsList.size() < 6) {
                            newsList.add(item);
                        } else {
                            break;
                        }
                    }
                    if (newsList.size() >= 6) break;
                }

                displayNews(newsList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Data load failed", error.toException());
            }
        });
    }

    private void displayNews(List<NewsItem> newsList) {
        for (int i = 0; i < newsList.size(); i++) {
            NewsItem item = newsList.get(i);
            View card = findViewById(cardIds[i]);

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
                contentView.setVisibility(View.GONE); // initially hidden
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
        }
    }
}
