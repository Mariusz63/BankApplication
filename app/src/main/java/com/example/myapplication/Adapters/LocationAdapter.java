package com.example.myapplication.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private ArrayList<String> loginHistory;

    public LocationAdapter(ArrayList<String> loginHistory) {
        this.loginHistory = loginHistory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_login_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String location = loginHistory.get(position);
        holder.tvLoginLocation.setText(location);
    }

    @Override
    public int getItemCount() {
        return loginHistory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLoginLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLoginLocation = itemView.findViewById(R.id.tvLoginLocation);
        }
    }
}
