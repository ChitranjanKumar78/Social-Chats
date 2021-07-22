package com.chitranjank.apps.socialchats.Fragments.Options;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.chitranjank.apps.socialchats.R;
import com.chitranjank.apps.socialchats.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class WritePostsActivity extends AppCompatActivity {
    private ImageView photoView;
    private Uri imageUri;
    String userName;
    String profilePicUrl;
    String id;

    ProgressDialog progressDialog;
    private FirebaseStorage firebaseStorage;
    CircleImageView sendPost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_posts);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Write Public Posts");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(this);

        CircleImageView addImage = findViewById(R.id.add_pic);
        sendPost = findViewById(R.id.send_post);
        EditText editText = findViewById(R.id.caption);

        photoView = findViewById(R.id.photo_public);


        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 11);
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                userName = user.getUsername();
                profilePicUrl = user.getImageURL();
                id = user.getId();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    String caption = editText.getText().toString();

                    if (caption.trim().isEmpty()) {
                        caption = "None";
                    }

                    upload_post_and_send(imageUri, id, caption, userName, profilePicUrl);
                    Glide.with(WritePostsActivity.this).load(R.drawable.ic_baseline_send_desable).into(sendPost);
                }
            }
        });
    }

    private void upload_post_and_send(Uri imageUri, String uId, String caption, String user_name, String profilePic) {
        progressDialog.setMessage("Uploading....");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.show();

        String postFileName = System.currentTimeMillis() + "." + getFileExtension(imageUri);

        StorageReference storageReference = firebaseStorage.getReference().child("Public Posts").child(postFileName);
        storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("imageUrl", url);
                            hashMap.put("captionMessage", caption);
                            hashMap.put("userNamePerson", user_name);
                            hashMap.put("profileUrl", profilePic);
                            hashMap.put("userId", uId);
                            hashMap.put("likeCount", 0);

                            databaseReference.child("Public").push().setValue(hashMap);
                            progressDialog.dismiss();

                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            progressDialog.setMessage(e.getLocalizedMessage());
                            progressDialog.setCancelable(true);
                            progressDialog.show();

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                progressDialog.setMessage(e.getLocalizedMessage());
                progressDialog.setCancelable(true);
                progressDialog.show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int progress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setProgress(progress);
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Glide.with(WritePostsActivity.this).load(R.drawable.ic_baseline_send_24).into(sendPost);
            switch (getFileExtension(data.getData())) {
                case "jpg":
                case "png":
                    imageUri = data.getData();
                    Glide.with(this).load(data.getData()).into(photoView);
                    break;
                default:
                    progressDialog.setMessage("Please choose JPG or PNG file only!");
                    progressDialog.show();
            }
        }
    }
}