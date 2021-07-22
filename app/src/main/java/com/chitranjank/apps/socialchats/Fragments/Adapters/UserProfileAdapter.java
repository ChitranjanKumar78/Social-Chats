package com.chitranjank.apps.socialchats.Fragments.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chitranjank.apps.socialchats.R;
import com.chitranjank.apps.socialchats.User;

import java.util.List;

public class UserProfileAdapter extends BaseAdapter {
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

