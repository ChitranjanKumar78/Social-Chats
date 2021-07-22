package com.chitranjank.apps.socialchats.Fragments.Options;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.chitranjank.apps.socialchats.R;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class ImageActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    String url;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbar = findViewById(R.id.image_tool);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ImageView imageView = findViewById(R.id.user_img);

        Intent intent = getIntent();
        url = intent.getStringExtra("IMG");
        title = intent.getStringExtra("Title");

        Glide.with(getApplicationContext()).load(url).into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.set_profile) {

            if (!url.trim().isEmpty()) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                        .child(firebaseUser.getUid());

                HashMap<String, Object> map = new HashMap<>();
                map.put("imageURL", url);

                databaseReference.updateChildren(map);

                Toast.makeText(ImageActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
            }
        }

        if (item.getItemId() == R.id.save_image) {
            if (!url.trim().isEmpty() && !title.trim().isEmpty()) {
                downloadImageFile(url, title);
            }
        }

        return false;
    }

    private void downloadImageFile(String url, String downloadTitle) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(url);

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Downloading....");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.setMax(100);
        pd.show();

        final File rootPath = new File(Environment.getExternalStorageDirectory(), "ChatNow");

        final File localFile = new File(rootPath, downloadTitle);

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                if (localFile.canRead()) {
                    pd.dismiss();
                    Toast.makeText(ImageActivity.this, "Download completed", Toast.LENGTH_LONG).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                pd.dismiss();
                Toast.makeText(ImageActivity.this, "Download failed", Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                    int progress = (int) (100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    pd.setProgress(progress);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}