package com.chitranjank.apps.socialchats;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chitranjank.apps.socialchats.Fragments.Adapters.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {
    private ListView allUsers;
    List<User> userArrayList;
    List<User> searchList;
    UserAdapter adapter;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        EditText editText = findViewById(R.id.search_bar);
        allUsers = findViewById(R.id.allUsers);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        userArrayList = new ArrayList<>();
        searchList = new ArrayList<>();

        allUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!editText.getText().toString().trim().isEmpty()) {
                    Intent intent = new Intent(UsersActivity.this, ChatActivity.class);
                    intent.putExtra("userId", searchList.get(position).getId());
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(UsersActivity.this, ChatActivity.class);
                    intent.putExtra("userId", userArrayList.get(position).getId());
                    startActivity(intent);
                    finish();
                }
            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchList.clear();
                for (User user : userArrayList) {
                    if (user.getUsername().toLowerCase().contains(s.toString().toLowerCase())) {
                        searchList.add(user);
                    }
                }
                adapter.searchUsers(searchList);
            }
        });

        read_users();
    }

    private void read_users() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    if (!user.getId().equals(firebaseUser.getUid())) {
                        userArrayList.add(user);
                    }
                }
                adapter = new UserAdapter(userArrayList, UsersActivity.this);
                allUsers.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}