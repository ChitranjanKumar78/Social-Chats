package com.chitranjank.apps.socialchats.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chitranjank.apps.socialchats.Fragments.Options.PublicPostContent;
import com.chitranjank.apps.socialchats.Fragments.Options.WritePostsActivity;
import com.chitranjank.apps.socialchats.R;
import com.chitranjank.apps.socialchats.Utilities.HomePage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Posts extends Fragment {
    private ListView listView;
    private List<PublicPostContent> contentList;

    FloatingActionButton fab;
    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;

    public Posts() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        //((AppCompatActivity) requireActivity()).getSupportActionBar().show();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        listView = view.findViewById(R.id.public_posts);

        contentList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Public");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contentList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PublicPostContent postContent = dataSnapshot.getValue(PublicPostContent.class);
                    contentList.add(postContent);
                }

                Collections.reverse(contentList);
                PublicPostAdapter adapter = new PublicPostAdapter(contentList, getContext());
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (contentList.get(position).getUserId().equals(firebaseUser.getUid())) {
                    String url = contentList.get(position).getImageUrl();

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                PublicPostContent postContent = dataSnapshot.getValue(PublicPostContent.class);
                                if (postContent.getImageUrl().equals(url)) {
                                    dataSnapshot.getRef().removeValue();
                                    return;
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                return false;
            }
        });
        return view;
    }

    static class PublicPostAdapter extends BaseAdapter {
        private final List<PublicPostContent> postContentList;
        Context context;

        public PublicPostAdapter(List<PublicPostContent> postContentList, Context context) {
            this.postContentList = postContentList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return postContentList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(context).inflate(R.layout.posts_content, parent, false);

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Public");

            TextView userName = convertView.findViewById(R.id.user_name_posted);
            TextView textViewCaption = convertView.findViewById(R.id.posted_caption);
            CircleImageView profilePic = convertView.findViewById(R.id.posted_person);
            ImageView postedImage = convertView.findViewById(R.id.posted_image);

            userName.setText(postContentList.get(position).getUserNamePerson());
            if (!postContentList.get(position).getProfileUrl().equals("default")) {
                Glide.with(context).load(postContentList.get(position).getProfileUrl()).into(profilePic);
            } else {
                Glide.with(context).load(R.drawable.person).into(profilePic);
            }

            if (!postContentList.get(position).getCaptionMessage().equals("None")) {
                textViewCaption.setText(postContentList.get(position).getCaptionMessage());
            }

            if (!postContentList.get(position).getImageUrl().trim().isEmpty()) {
                postedImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(postContentList.get(position).getImageUrl()).centerCrop().into(postedImage);
            }

            return convertView;
        }

    }

}