package com.chitranjank.apps.socialchats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.chitranjank.apps.socialchats.Fragments.Adapters.UserProfileAdapter;
import com.chitranjank.apps.socialchats.Fragments.Options.ImageActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfile extends AppCompatActivity {
    private ListView listView;
    private List<User> infoList;
    private UserProfileAdapter adapter;
    private ImageView imageView;
    String imgUrl = "None";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        imageView = findViewById(R.id.ur_profile_img);
        listView = findViewById(R.id.user_info);

        infoList = new ArrayList<>();

        Intent intent = getIntent();
        String uId = intent.getStringExtra("user_id");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                infoList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    if (user.getId().equals(uId)) {
                        infoList.add(user);

                        if (!user.getImageURL().equals("None")) {
                            Glide.with(getApplicationContext()).load(user.getImageURL()).centerCrop().into(imageView);
                        } else {
                            Glide.with(getApplicationContext()).load(R.drawable.person).into(imageView);
                        }
                    }
                }
                adapter = new UserProfileAdapter(infoList, getApplicationContext());
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!imgUrl.equals("None")) {
                    startActivity(new Intent(UserProfile.this, ImageActivity.class)
                            .putExtra("IMG", imgUrl));
                }
            }
        });
    }
}