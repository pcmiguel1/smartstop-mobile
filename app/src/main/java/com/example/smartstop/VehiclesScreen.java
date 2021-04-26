package com.example.smartstop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class VehiclesScreen extends AppCompatActivity {

    private ListView listViewVehicles;
    String matriculas[] = {"87-PL-43", "87-PL-43", "87-PL-43"};
    String modelos[] = {"BMW 520 GT", "BMW 520 GT", "BMW 520 GT"};
    int tipos[] = {R.drawable.categorybasic, R.drawable.categorybolt, R.drawable.categoryminivan, R.drawable.categorypremium};

    private int selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles_screen);

        listViewVehicles = findViewById(R.id.listView_vehicles);

        MyAdapter adapter = new MyAdapter(this, matriculas, modelos, tipos);
        listViewVehicles.setAdapter(adapter);

        listViewVehicles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectedItem = position;
                adapter.notifyDataSetChanged();
                openEditVehicle();

            }
        });

    }

    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        String rMatriculas[];
        String rModelos[];
        int rTipos[];

        MyAdapter (Context c, String matricula[], String modelo[], int tipo[]) {
            super(c, R.layout.row_vehicle, R.id.matricula, matricula);
            this.context = c;
            this.rMatriculas = matricula;
            this.rModelos = modelo;
            this.rTipos = tipo;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = layoutInflater.inflate(R.layout.row_vehicle, parent, false);
            ImageView images = row.findViewById(R.id.car_type);
            TextView myMatricula = row.findViewById(R.id.matricula);
            TextView myModelo = row.findViewById(R.id.modelo_car);
            CardView myCardVehicle = row.findViewById(R.id.card_vehicle);

            images.setImageResource(rTipos[position]);
            myMatricula.setText(rMatriculas[position]);
            myModelo.setText(rModelos[position]);

            if (position == selectedItem) {

                myCardVehicle.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                myModelo.setTextColor(getResources().getColor(R.color.colorAccent));

            }

            return row;
        }
    }

    public void closeVehicles(View view) {
        finish();
    }

    private void openEditVehicle() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                VehiclesScreen.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_edit_vehicle,
                        (LinearLayout) findViewById(R.id.bottomSheetParkInfo)
                );

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }
}