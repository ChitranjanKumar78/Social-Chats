package com.chitranjank.apps.socialchats.Fragments.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chitranjank.apps.socialchats.R;
import com.chitranjank.apps.socialchats.User;


import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends BaseAdapter {
    List<User> arrayList;
    Context context;

    public UserAdapter(List<User> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    public void searchUsers(List<User> userList){
        this.arrayList = userList;
        notifyDataSetChanged();
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
        View view = LayoutInflater.from(context).inflate(R.layout.users_items, parent, false);

        TextView userNameTv = view.findViewById(R.id.user_name);

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

        return view;
    }

}
