package com.example.smartstop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FeatureParkingAdapter extends RecyclerView.Adapter<FeatureParkingAdapter.FeatureParkingViewHolder> {

    private Context context;
    private ArrayList<Integer> features;

    public FeatureParkingAdapter(Context context, ArrayList<Integer> features) {
        this.context = context;
        this.features = features;
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

    public class FeatureParkingViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {

        ImageView image;
        TextView title;
        RelativeLayout relativeLayout;

        public FeatureParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.feature_image);
            title = itemView.findViewById(R.id.feature_title);
            relativeLayout = itemView.findViewById(R.id.rec_feature_park);

            relativeLayout.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            if (!features.contains(getAdapterPosition()+1)) {
                image.setColorFilter(v.getResources().getColor(R.color.colorAccent));
                relativeLayout.setBackgroundTintList(v.getResources().getColorStateList(R.color.colorPrimary));
                title.setTextColor(v.getResources().getColor(R.color.colorAccent));
                features.add(getAdapterPosition()+1);
            } else {
                features.remove(features.indexOf(getAdapterPosition()+1));
                image.setColorFilter(v.getResources().getColor(R.color.colorPrimaryDark));
                relativeLayout.setBackgroundTintList(v.getResources().getColorStateList(R.color.colorG));
                title.setTextColor(v.getResources().getColor(R.color.colorPrimaryDark));
            }

        }
    }

    @Override
    public int getItemCount() {
        return imagesList.length;
    }

}
