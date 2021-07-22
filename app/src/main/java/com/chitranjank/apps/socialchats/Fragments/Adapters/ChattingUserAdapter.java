package com.chitranjank.apps.socialchats.Fragments.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.chitranjank.apps.socialchats.Chat;
import com.chitranjank.apps.socialchats.R;
import com.chitranjank.apps.socialchats.User;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChattingUserAdapter extends BaseAdapter {
    List<User> arrayList;
    Context context;

    String lastMessage, lastMessageTime;
    private final boolean isChat;

    public ChattingUserAdapter(List<User> arrayList, Context context, boolean isChat) {
        this.arrayList = arrayList;
        this.context = context;
        this.isChat = isChat;
    }

    @Override
    public int getCount() {
        return arrayList.size();
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
        @SuppressLint("ViewHolder")
        View view = LayoutInflater.from(context).inflate(R.layout.users_item_chats, parent, false);

        TextView userNameTv = view.findViewById(R.id.user_name);
        TextView textViewLastMsg = view.findViewById(R.id.last_msg);
        TextView textViewLastMsgTime = view.findViewById(R.id.last_msg_time);

        CircleImageView on_off = view.findViewById(R.id.on_off);
        CircleImageView seen_or_not = view.findViewById(R.id.seen_or_not);

        CircleImageView circleImageView = view.findViewById(R.id.profile_img);

        userNameTv.setText(arrayList.get(position).getUsername());
        if (!arrayList.get(position).getImageURL().trim().equals("default")) {
            Glide.with(context)
                    .load(arrayList.get(position).getImageURL())
                    .into(circleImageView);
        } else {
            Glide.with(context)
                    .load(R.drawable.person)
                    .into(circleImageView);
        }

        if (arrayList.get(position).getStatus().equals("Online")) {
            Glide.with(context).load(R.drawable.online).into(on_off);
        } else {
            Glide.with(context).load(R.drawable.offline).into(on_off);
        }

        last_message_get(arrayList.get(position).getId(), textViewLastMsg, textViewLastMsgTime, seen_or_not);
        return view;
    }

    private void last_message_get(String userId, TextView textViewMsg, TextView lastMsgTime, CircleImageView seen_or_not) {
        lastMessage = "None";
        lastMessageTime = "";
        final boolean[] seenOrNot = {false};
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat != null && firebaseUser != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)
                                || chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) {
                            lastMessage = chat.getMessage();
                            lastMessageTime = chat.getTimeStamp();
                            seenOrNot[0] = chat.isIsSeen();
                        }
                    }
                }

                if (lastMessage.equals("None")) {
                    textViewMsg.setVisibility(View.GONE);
                } else {
                    textViewMsg.setVisibility(View.VISIBLE);
                    textViewMsg.setText(lastMessage);
                    lastMsgTime.setText(lastMessageTime);
                }

                if (seenOrNot[0]) {
                    Glide.with(context).load(R.drawable.viewed).into(seen_or_not);
                }

                lastMessage = "None";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
