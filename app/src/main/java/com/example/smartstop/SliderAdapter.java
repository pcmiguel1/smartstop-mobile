package com.example.smartstop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context) {
        this.context = context;
    }

    public int[] slide_images = {

            R.drawable.mockupmap,
            R.drawable.mockuppark2,
            R.drawable.mockuppark1,
            R.drawable.mockupreservation,
            R.drawable.mockuphistory

    };

    public String[] slide_headings = {

            "Parking just got easier",
            "Park information",
            "Make a reservation",
            "Booking information",
            "All reservations made"

    };

    public String[] slide_descs = {

            "Simpler, safer and complete. Without wasting time find a place, book now parking place.",
            "See all the information about the park and also the route from your location to the selected park.",
            "To make the reservation you only need to select the duration and the starting date.",
            "After making the payment, you will be able to see al the information of the reservation made.",
            "See all reservations that are active, sheduled and expired and also extend or interrupt your parking time at any time."

    };

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (RelativeLayout) object;
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slideImageView = view.findViewById(R.id.slide_image);
        TextView slideHeading = view.findViewById(R.id.slide_heading);
        TextView slideDescription = view.findViewById(R.id.slide_desc);

        slideImageView.setImageResource(slide_images[position]);
        slideHeading.setText(slide_headings[position]);
        slideDescription.setText(slide_descs[position]);

        container.addView(view);

        return view;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((RelativeLayout)object);

    }
}
