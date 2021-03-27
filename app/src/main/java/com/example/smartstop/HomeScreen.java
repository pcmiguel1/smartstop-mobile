package com.example.smartstop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeScreen extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout linearDotsLayout;

    private TextView[] dots;

    private SliderAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        viewPager = findViewById(R.id.sideViewPaper);
        linearDotsLayout = findViewById(R.id.dotsLayout);

        sliderAdapter = new SliderAdapter(this);

        viewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);

        viewPager.addOnPageChangeListener(viewListener);

    }

    public void addDotsIndicator(int position) {

        dots = new TextView[3];
        linearDotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {

            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.colorGray));

            linearDotsLayout.addView(dots[i]);

        }

        if (dots.length > 0) {

            dots[position].setTextColor(getResources().getColor(R.color.colorPrimaryDark));

        }

    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public void login(View view) {

        Intent intent = new Intent(HomeScreen.this, MapScreen.class);
        startActivity(intent);
        finish();

    }
}