package com.chitranjank.apps.socialchats.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chitranjank.apps.socialchats.Chat;
import com.chitranjank.apps.socialchats.Fragments.Options.PublicPostContent;
import com.chitranjank.apps.socialchats.Fragments.Options.WritePostsActivity;
import com.chitranjank.apps.socialchats.ProfileActivity;
import com.chitranjank.apps.socialchats.R;
import com.chitranjank.apps.socialchats.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class Profile extends Fragment {
    CircleImageView circleImageView;
    ImageButton editPIMG;
    TextView textViewUserName;
    TextView textViewEmail;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    FirebaseStorage storage;

    private Uri imageUri;
    private static final int IMAGE_REQUEST = 1;

    private List<PublicPostContent> myPosts;
    private MyGridAdapter adapter;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        circleImageView = view.findViewById(R.id.profile_image_homePage);
        editPIMG = view.findViewById(R.id.edit_pImg);
        textViewEmail = view.findViewById(R.id.user_email_profile);
        textViewUserName = view.findViewById(R.id.user_name_profile);

        GridView gridView = view.findViewById(R.id.gride_view);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storage = FirebaseStorage.getInstance();


        databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                Glide.with(requireContext()).load(user.getImageURL()).into(circleImageView);
                textViewEmail.setText(user.getEmail());
                textViewUserName.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().startActivity(new Intent(requireContext(), WritePostsActivity.class));
            }
        });

        editPIMG.setOnClickListener(v -> {
            String[] permissionList = {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

            Dexter.withContext(requireContext())
                    .withPermissions(permissionList)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                openImage();
                            } else if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                                Toast.makeText(requireContext(), "Permission denied",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    }).check();
        });

        myPosts = new ArrayList<>();
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("Public");
        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myPosts.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PublicPostContent publicPostContent = dataSnapshot.getValue(PublicPostContent.class);

                    assert publicPostContent != null;
                    if (publicPostContent.getUserId().equals(firebaseUser.getUid())) {
                        myPosts.add(publicPostContent);
                    }

                }

                adapter = new MyGridAdapter(myPosts, getContext());
                gridView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = requireContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(requireContext());
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
                Toast.makeText(requireContext(), e.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(requireContext(), "No image selected",
                    Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
    }

    public static class MyGridAdapter extends BaseAdapter {

        List<PublicPostContent> list;
        Context context;

        public MyGridAdapter(List<PublicPostContent> list, Context context) {
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gride_view_content, parent, false);

            TextView textViewCaption = convertView.findViewById(R.id.grid_caption);
            ImageView imageView = convertView.findViewById(R.id.image_gride);

            Glide.with(context).load(list.get(position).getImageUrl()).into(imageView);
            textViewCaption.setText(list.get(position).getCaptionMessage());

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.delete_dialog);
                    TextView btnYes = dialog.findViewById(R.id.yes);
                    TextView btnNo = dialog.findViewById(R.id.no);
                    dialog.show();

                    btnYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            delete_message(list.get(position).getImageUrl());
                            dialog.dismiss();
                            notifyDataSetChanged();
                        }
                    });
                    btnNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }
                    });

                    return false;
                }
            });

            return convertView;
        }

        private void delete_message(String url) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Public");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        PublicPostContent publicPostContent = dataSnapshot.getValue(PublicPostContent.class);

                        assert publicPostContent != null;
                        if (publicPostContent.getImageUrl().equals(url)) {
                            StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                            reference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

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

    }

}