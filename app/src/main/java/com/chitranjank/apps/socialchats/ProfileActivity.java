package com.chitranjank.apps.socialchats;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;

    FirebaseStorage storage;

    private Uri imageUri;
    private static final int IMAGE_REQUEST = 1;

    private List<User> infoList;
    private UserProfileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView pgImg = findViewById(R.id.profile_img);
        ImageView changePic = findViewById(R.id.edit_pic);

        TextView editInfo = findViewById(R.id.tv_edit);
        CircleImageView editInfoBtn = findViewById(R.id.edit_info_btn);
        TextView logOut = findViewById(R.id.log_out);

        ListView listView = findViewById(R.id.info);

        infoList = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        storage = FirebaseStorage.getInstance();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                infoList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    assert user != null;
                    if (user.getId().equals(firebaseUser.getUid())) {
                        infoList.add(user);
                        if (!user.getImageURL().equals("None")) {
                            Glide.with(getApplicationContext()).load(user.getImageURL()).centerCrop().into(pgImg);
                        } else {
                            Glide.with(getApplicationContext()).load(R.drawable.person).centerCrop().into(pgImg);
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



        Dialog dialog = new Dialog(ProfileActivity.this);
        dialog.setContentView(R.layout.dialog_edit_info);
        dialog.setTitle("Write about...");

        EditText editText = dialog.findViewById(R.id.editText_about);
        TextView textViewBtn = dialog.findViewById(R.id.done_btn);

        editInfo.setOnClickListener(v -> dialog.show());
        editInfoBtn.setOnClickListener(v -> dialog.show());

        textViewBtn.setOnClickListener(v -> {
            if (!editText.getText().toString().trim().isEmpty()) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                String aBout = editText.getText().toString();
                HashMap<String, Object> map = new HashMap<>();
                map.put("about", aBout);
                databaseReference.updateChildren(map);
                dialog.dismiss();
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage("Updating");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (imageUri != null) {
            StorageReference reference = storage.getReference().child("Profile pic").child(System.currentTimeMillis() +
                    "." + getFileExtension(imageUri));

            reference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String mUrl = uri.toString();

                                databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                                        .child(firebaseUser.getUid());

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("imageURL", mUrl);

                                databaseReference.updateChildren(map);
                                progressDialog.dismiss();
                            }
                        });
                    }
                }
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, e.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(ProfileActivity.this, "No image selected",
                    Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
    }

    public static class UserProfileAdapter extends BaseAdapter {
        private final List<User> list;
        private final Context context;

        public UserProfileAdapter(List<User> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public int getCount() {
            return list.size();
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
            convertView = LayoutInflater.from(context).inflate(R.layout.user_info, parent, false);

            TextView textViewName, textViewEmail, textViewPhone;

            textViewName = convertView.findViewById(R.id.text_view_name);
            textViewEmail = convertView.findViewById(R.id.text_view_email);
            textViewPhone = convertView.findViewById(R.id.text_view_phone);

            textViewName.setText(list.get(position).getUsername());
            textViewEmail.setText(list.get(position).getEmail());
            textViewPhone.setText(list.get(position).getAbout());

            return convertView;
        }
    }
}