package com.example.myapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.Location;
import com.example.myapplication.R;

import java.util.ArrayList;

public class LoginHistoryAdapter extends RecyclerView.Adapter<LoginHistoryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Location> loginHistoryList;

    public LoginHistoryAdapter(Context context, ArrayList<Location> loginHistoryList) {
        this.context = context;
        this.loginHistoryList = loginHistoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_stats_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Location location = loginHistoryList.get(position);

        holder.loginLocationTextView.setText(location.getLoginLocation());
        holder.loginTimeTextView.setText(location.getLoginTime());
    }

    @Override
    public int getItemCount() {
        return loginHistoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView loginLocationTextView;
        TextView loginTimeTextView;
        TextView loginAddressTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            loginLocationTextView = itemView.findViewById(R.id.textview_location);
            loginAddressTextView = itemView.findViewById(R.id.textview_address);
            loginTimeTextView = itemView.findViewById(R.id.textview_time);
        }
    }
}
