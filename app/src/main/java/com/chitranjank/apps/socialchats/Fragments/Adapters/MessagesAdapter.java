package com.chitranjank.apps.socialchats.Fragments.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chitranjank.apps.socialchats.Chat;
import com.chitranjank.apps.socialchats.Fragments.Options.ImageActivity;
import com.chitranjank.apps.socialchats.Fragments.Options.MusicActivity;
import com.chitranjank.apps.socialchats.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends BaseAdapter {
    private final ArrayList<Chat> chatArrayList;
    private final Context context;

    final static int SENT_ITEM = 1;
    final static int RECEIVE_ITEM = 2;

    FirebaseUser firebaseUser;
    boolean send;

    public MessagesAdapter(ArrayList<Chat> chatArrayList, Context context, boolean send) {
        this.chatArrayList = chatArrayList;
        this.context = context;
        this.send = send;
    }

    @Override
    public int getCount() {
        return chatArrayList.size();
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

        if (SENT_ITEM == 1){
            convertView = LayoutInflater.from(context).inflate(R.layout.right,parent,false);
            send = true;
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.left,parent,false);
            send = false;
        }
        TextView textView;
        TextView textViewTime;
        CircleImageView seenMsg;
        ImageView imageView;
        CircleImageView mp3_file;
        CircleImageView imageViewPdf;

        textView = convertView.findViewById(R.id.text_msg);
        textViewTime = convertView.findViewById(R.id.time_text);
        seenMsg = convertView.findViewById(R.id.seen_msg);
        imageView = convertView.findViewById(R.id.image_send);
        mp3_file = convertView.findViewById(R.id.mp3_file);
        imageViewPdf = convertView.findViewById(R.id.pdf_file);

        Chat chat = chatArrayList.get(position);
        if (!chat.getMessage().equals("")) {
            textView.setText(chat.getMessage());
        }

        textViewTime.setText(chat.getTimeStamp());

        if (send) {
            if (chat.isIsSeen()) {
                Glide.with(context).load(R.drawable.viewed).into(seenMsg);
            }
        }

        if (!chat.getImgUrl().equals("None")) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(chat.getImgUrl()).centerCrop().into(imageView);
        } else {
            imageView.setVisibility(View.GONE);

        }

        if (!chat.getMp3File().equals("None")) {
            mp3_file.setVisibility(View.VISIBLE);
        } else {
            mp3_file.setVisibility(View.GONE);
        }

        if (!chat.getPdfFile().equals("None")) {
            imageViewPdf.setVisibility(View.VISIBLE);
        } else {
            imageViewPdf.setVisibility(View.GONE);
        }

        textView.setOnClickListener(new View.OnClickListener() {
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

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!chat.getImgUrl().equals("None")) {
                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.putExtra("IMG", chat.getImgUrl()).putExtra("Title", chat.getMessage());
                    context.startActivity(intent);
                }
            }
        });

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (chatArrayList.get(position).getSender().equals(firebaseUser.getUid())) {
            return SENT_ITEM;
        } else {
            return RECEIVE_ITEM;
        }
    }
}
