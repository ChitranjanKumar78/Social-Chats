package com.chitranjank.apps.socialchats;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chitranjank.apps.socialchats.Fragments.Adapters.MessageAdapter;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatMessages;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    FirebaseStorage storageReference;

    private String user_image;
    private static final int SELECTED_REQUEST_CODE = 100;

    Intent intent;

    MessageAdapter adapter;
    ArrayList<Chat> chatArrayList;

    String userid;

    ValueEventListener listener;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        progressDialog = new ProgressDialog(ChatActivity.this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        CircleImageView sendMsg = findViewById(R.id.sendMsg);
        CircleImageView attachments = findViewById(R.id.attach_ments);
        TextView uName = findViewById(R.id.u_name);
        ImageButton backHome = findViewById(R.id.backHome);
        EditText editTextMsg = findViewById(R.id.editText_msg);
        CircleImageView tPg = findViewById(R.id.t_pg);

        chatArrayList = new ArrayList<>();
        chatMessages = findViewById(R.id.chat_messages);
        chatMessages.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chatMessages.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        userid = intent.getStringExtra("userId");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        storageReference = FirebaseStorage.getInstance();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;

                uName.setText(user.getUsername());
                user_image = user.getImageURL();

                if (user.getImageURL().equals("default")) {
                    user_image = "None";
                    Glide.with(getApplicationContext())
                            .load(R.drawable.person)
                            .into(tPg);
                } else {
                    Glide.with(getApplicationContext())
                            .load(user_image)
                            .into(tPg);

                }

                read_messages(firebaseUser.getUid(), userid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendMsg.setOnClickListener(v -> {
            String msg = editTextMsg.getText().toString();

            if (!msg.trim().isEmpty()) {
                send_text_message(firebaseUser.getUid(), userid, msg, get_time_stamp());
            } else {
                Toast.makeText(getApplicationContext(), "Please type some message",
                        Toast.LENGTH_SHORT).show();
            }
            editTextMsg.setText("");
        });


        backHome.setOnClickListener(v -> {
            finish();
        });

        attachments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_chooser();
            }
        });

        seenMessage(userid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.user_profile) {
            Intent pIntent = new Intent(getApplicationContext(), UserProfile.class);
            pIntent.putExtra("user_id", userid);
            startActivity(pIntent);
            return true;
        }

        return false;
    }

    private void open_chooser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, SELECTED_REQUEST_CODE);
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(listener);
        status("Offline");
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECTED_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            switch (getFileExtension(data.getData())) {

                case "jpg":
                case "png":
                    send_image_file(data.getData());
                    break;

                case "mp3":
                    send_mp3_file(data.getData());
                    break;

                case "pdf":
                    pdf_send_dialog(data.getData());
                    break;

                default:
                    Toast.makeText(this, "Something went wrong! ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ChatActivity.this, "Invalid selection, please select any JPG, PNG, Mp3 or PDF file", Toast.LENGTH_SHORT).show();
        }
    }

    private void send_image_file(Uri uri) {
        Dialog dialogAttach = new Dialog(this);
        dialogAttach.setContentView(R.layout.send_image_dialog);
        dialogAttach.show();
        dialogAttach.setCancelable(false);
        dialogAttach.setTitle("Send image");

        ImageView imageView = dialogAttach.findViewById(R.id.d_image);
        EditText editText = dialogAttach.findViewById(R.id.title_d);
        TextView yesBtn = dialogAttach.findViewById(R.id.yes);
        TextView noBtn = dialogAttach.findViewById(R.id.no);

        Glide.with(ChatActivity.this).load(uri).centerCrop().into(imageView);

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAttach.dismiss();
                String dialogTitle = editText.getText().toString();
                if (dialogTitle.trim().isEmpty()) {
                    dialogTitle = "IMAGE" + System.currentTimeMillis();
                }

                dialogTitle = dialogTitle + "." + getFileExtension(uri);

                uploadImage(uri, dialogTitle);
            }
        });
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAttach.dismiss();
            }
        });
    }

    private void send_mp3_file(Uri uri) {
        Dialog dialogAttach = new Dialog(this);
        dialogAttach.setContentView(R.layout.send_file_dialog);
        dialogAttach.show();
        dialogAttach.setCancelable(false);
        dialogAttach.setTitle("Add some title..");

        EditText editText = dialogAttach.findViewById(R.id.title_music);
        TextView sendBtn = dialogAttach.findViewById(R.id.sendBtn);
        TextView cancelBtn = dialogAttach.findViewById(R.id.cancelBtn);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAttach.dismiss();
                String dialogTitle = editText.getText().toString();
                if (dialogTitle.trim().isEmpty()) {
                    dialogTitle = "AUDIO" + System.currentTimeMillis();
                }

                dialogTitle = dialogTitle + "." + getFileExtension(uri);
                upload_audio(uri, dialogTitle);
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAttach.dismiss();
            }
        });
    }

    private void pdf_send_dialog(Uri uri) {
        Dialog dialogAttach = new Dialog(this);
        dialogAttach.setContentView(R.layout.send_file_dialog);
        dialogAttach.show();
        dialogAttach.setCancelable(false);
        dialogAttach.setTitle("Add some title..");

        EditText editText = dialogAttach.findViewById(R.id.title_music);
        TextView sendBtn = dialogAttach.findViewById(R.id.sendBtn);
        TextView cancelBtn = dialogAttach.findViewById(R.id.cancelBtn);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAttach.dismiss();
                String title = editText.getText().toString();
                if (title.trim().isEmpty()) {
                    title = "PDF" + System.currentTimeMillis();
                }

                title = title + "." + getFileExtension(uri);
                upload_pdf(uri, title);
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAttach.dismiss();
            }
        });
    }

    private void uploadImage(Uri uri, String msg) {
        progressDialog.show();

        if (uri != null) {
            StorageReference reference = storageReference.getReference().child("Image").child(msg);

            reference.putFile(uri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            progressDialog.dismiss();
                            String imageUrl = downloadUri.toString();
                            if (!imageUrl.isEmpty()) {
                                send_image_file(firebaseUser.getUid(), userid, msg, get_time_stamp(), imageUrl);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(ChatActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ChatActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    int progress = (int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                    progressDialog.setProgress(progress);
                }
            });

        } else {
            progressDialog.dismiss();
            Toast.makeText(ChatActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void upload_audio(Uri data, String dialogTitle) {
        progressDialog.show();

        if (data != null) {
            StorageReference st = storageReference.getReference().child("Mp3Files").child(dialogTitle);
            st.putFile(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        st.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.dismiss();
                                String mp3_url = uri.toString();
                                if (!mp3_url.isEmpty()) {
                                    send_mp3_message(firebaseUser.getUid(), userid, dialogTitle, get_time_stamp(), mp3_url);
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(ChatActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ChatActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    int progress = (int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                    progressDialog.setProgress(progress);
                }
            });
        } else {
            progressDialog.dismiss();
            Toast.makeText(ChatActivity.this, "File not uploaded. Try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void upload_pdf(Uri data, String title) {
        progressDialog.show();

        if (data != null) {
            StorageReference st = storageReference.getReference().child("PDF Files").child(title);
            st.putFile(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        st.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.dismiss();
                                String pdfUrl = uri.toString();
                                if (!pdfUrl.isEmpty()) {
                                    send_pdf_file(firebaseUser.getUid(), userid, title, get_time_stamp(), pdfUrl);
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(ChatActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ChatActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    int progress = (int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                    progressDialog.setProgress(progress);
                }
            });
        } else {
            progressDialog.dismiss();
            Toast.makeText(ChatActivity.this, "File not uploaded. Try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void seenMessage(String string_id) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        listener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) &&
                            chat.getSender().equals(string_id)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void read_messages(String myId, String userId) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        chatArrayList.add(chat);
                    }
                    adapter = new MessageAdapter(chatArrayList, ChatActivity.this, true);
                    chatMessages.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String get_time_stamp() {
        Date date = new Date();
        int hours = date.getHours() > 12 ? date.getHours() - 12 : date.getHours();
        String am_pm = date.getHours() >= 12 ? "PM" : "AM";
        hours = hours < 10 ? Integer.parseInt("0" + hours) : hours;
        int minutes = date.getMinutes() < 10 ? Integer.parseInt("0" + date.getMinutes()) : date.getMinutes();
        return hours + ":" + minutes + " " + am_pm;
    }

    private void send_mp3_message(String uid, String userid, String dialogMessage, String time_stamp, String mp3_url) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", uid);
        hashMap.put("receiver", userid);
        hashMap.put("message", dialogMessage);
        hashMap.put("imgUrl", "None");
        hashMap.put("timeStamp", time_stamp);
        hashMap.put("isSeen", false);
        hashMap.put("mp3File", mp3_url);
        hashMap.put("pdfFile", "None");
        databaseReference.child("Chats").push().setValue(hashMap);
    }

    private void send_pdf_file(String uid, String userid, String dialogMessage, String time_stamp, String pdfUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", uid);
        hashMap.put("receiver", userid);
        hashMap.put("message", dialogMessage);
        hashMap.put("imgUrl", "None");
        hashMap.put("timeStamp", time_stamp);
        hashMap.put("isSeen", false);
        hashMap.put("mp3File", "None");
        hashMap.put("pdfFile", pdfUrl);
        databaseReference.child("Chats").push().setValue(hashMap);
    }

    private void send_text_message(String uid, String userid, String msg, String time_stamp) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", uid);
        hashMap.put("receiver", userid);
        hashMap.put("message", msg);
        hashMap.put("imgUrl", "None");
        hashMap.put("timeStamp", time_stamp);
        hashMap.put("isSeen", false);
        hashMap.put("mp3File", "None");
        hashMap.put("pdfFile", "None");
        databaseReference.child("Chats").push().setValue(hashMap);
    }

    private void send_image_file(String sender, String receiver, String message, String timeStamp, String url) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("imgUrl", url);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("isSeen", false);
        hashMap.put("mp3File", "None");
        hashMap.put("pdfFile", "None");
        databaseReference.child("Chats").push().setValue(hashMap);
    }
}