package com.example.smartstop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TypeParkingAdapter extends RecyclerView.Adapter<TypeParkingAdapter.TypeParkingViewHolder> {

    private Context context;

    public TypeParkingAdapter(Context context) {
        this.context = context;
    }

    public int[] imagesList = {R.drawable.people_80px, R.drawable.unfriend_80px};

    public String[] titlesList = {"Public", "Private"};

    public String[] subtitlesList = {"230 parking slots", "230 parking slots"};

    @NonNull
    @Override
    public TypeParkingAdapter.TypeParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_type_parking, parent, false);
        return new TypeParkingAdapter.TypeParkingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeParkingViewHolder holder, int position) {
        holder.image.setImageResource(imagesList[position]);
        holder.title.setText(titlesList[position]);
        holder.subtitle.setText(subtitlesList[position]);




    }

    public class TypeParkingViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title, subtitle;

        public TypeParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.type_image);
            title = itemView.findViewById(R.id.type_title);
            subtitle = itemView.findViewById(R.id.type_subtitle);
        }

    }

    @Override
    public int getItemCount() {
        return imagesList.length;
    }

}
