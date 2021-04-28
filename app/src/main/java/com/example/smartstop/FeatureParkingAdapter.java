package com.example.smartstop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FeatureParkingAdapter extends RecyclerView.Adapter<FeatureParkingAdapter.FeatureParkingViewHolder> {

    private Context context;

    public FeatureParkingAdapter(Context context) {
        this.context = context;
    }

    public int[] imagesList = {R.drawable.assistive_technology_80px, R.drawable.electrical_80px, R.drawable.defense_80px, R.drawable.umbrella_80px};

    public String[] titlesList = {"Handicapped", "Charger", "Guard", "Covered"};

    @NonNull
    @Override
    public FeatureParkingAdapter.FeatureParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_feature_parking, parent, false);
        return new FeatureParkingAdapter.FeatureParkingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureParkingViewHolder holder, int position) {
        holder.image.setImageResource(imagesList[position]);
        holder.title.setText(titlesList[position]);

    }

    public class FeatureParkingViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;

        public FeatureParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.feature_image);
            title = itemView.findViewById(R.id.feature_title);
        }

    }

    @Override
    public int getItemCount() {
        return imagesList.length;
    }

}
