package com.chitranjank.apps.socialchats.Fragments.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chitranjank.apps.socialchats.Chat;
import com.chitranjank.apps.socialchats.Fragments.Options.ImageActivity;
import com.chitranjank.apps.socialchats.Fragments.Options.MusicActivity;
import com.chitranjank.apps.socialchats.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    ArrayList<Chat> arrayList;
    Context context;
    FirebaseUser firebaseUser;

    boolean send;

    public MessageAdapter(ArrayList<Chat> arrayList, Context context, Boolean send) {
        this.arrayList = arrayList;
        this.context = context;
        this.send = send;
    }

    public void searchUsers(ArrayList<Chat> userList) {
        this.arrayList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.right, parent, false);
            send = true;
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.left, parent, false);
            send = false;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = arrayList.get(position);
        if (!chat.getMessage().equals("")) {
            holder.textView.setText(chat.getMessage());
        }

        holder.textViewTime.setText(chat.getTimeStamp());

        if (send) {
            if (chat.isIsSeen()) {
                Glide.with(context).load(R.drawable.viewed).into(holder.seenMsg);
            }
        }

        if (!chat.getImgUrl().equals("None")) {
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(chat.getImgUrl()).centerCrop().into(holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE);

        }

        if (!chat.getMp3File().equals("None")) {
            holder.mp3_file.setVisibility(View.VISIBLE);
        } else {
            holder.mp3_file.setVisibility(View.GONE);
        }

        if (!chat.getPdfFile().equals("None")) {
            holder.imageViewPdf.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewPdf.setVisibility(View.GONE);
        }

        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!chat.getImgUrl().equals("None")) {
                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.putExtra("IMG", chat.getImgUrl()).putExtra("Title", chat.getMessage());
                    context.startActivity(intent);
                }
                if (!chat.getMp3File().equals("None")) {
                    Intent intent = new Intent(context, MusicActivity.class);
                    intent.putExtra("MUSIC", chat.getMp3File()).putExtra("Title", chat.getMessage());
                    context.startActivity(intent);
                }

            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!chat.getImgUrl().equals("None")) {
                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.putExtra("IMG", chat.getImgUrl()).putExtra("Title", chat.getMessage());
                    context.startActivity(intent);
                }
            }
        });

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
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
                        delete_message(chat.getSender(), chat.getReceiver(), chat.getMessage(), chat.getTimeStamp());
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
                return true;
            }
        });

        //deleting dialog
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
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
                        delete_message(chat.getSender(), chat.getReceiver(), chat.getMessage(), chat.getTimeStamp());
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
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView textViewTime;
        CircleImageView seenMsg;
        ImageView imageView;
        CircleImageView mp3_file;
        CircleImageView imageViewPdf;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_msg);
            textViewTime = itemView.findViewById(R.id.time_text);
            seenMsg = itemView.findViewById(R.id.seen_msg);
            imageView = itemView.findViewById(R.id.image_send);
            mp3_file = itemView.findViewById(R.id.mp3_file);
            imageViewPdf = itemView.findViewById(R.id.pdf_file);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (arrayList.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    private void delete_message(String sender, String receiver, String message, String timeStamp) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getSender().equals(sender) && chat.getReceiver().equals(receiver) && chat.getTimeStamp().equals(timeStamp)) {

                        if (!chat.getImgUrl().equals("None")) {
                            delete_attachments_also(chat.getImgUrl());
                        } else if (!chat.getMp3File().equals("None")) {
                            delete_attachments_also(chat.getMp3File());
                        } else if (!chat.getPdfFile().equals("None")) {
                            delete_attachments_also(chat.getPdfFile());
                        }

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

    private void delete_attachments_also(String url) {
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
    }

}
