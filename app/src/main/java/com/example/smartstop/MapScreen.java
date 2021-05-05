package com.example.smartstop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textFont;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

public class MapScreen extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;

    private boolean markerSelected = false;

    private LocationRequest locationRequest;
    public static final int REQUEST_CHECK_SETTING = 1001;

    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private LatLng userLocation;

    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private DirectionsRoute currentRoute;
    private MapboxDirections client;

    private RequestQueue requestQueue;

    private TextView inputSearchPark;

    public static JSONObject USER_JSON_OBJECT;

    // Variables needed to listen to location updates
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Saber as informações do utilizador que fez o login
        if(getIntent().hasExtra("user")) {
            try {
                USER_JSON_OBJECT = new JSONObject(getIntent().getStringExtra("user"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        requestQueue = Volley.newRequestQueue(MapScreen.this);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_map_screen);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        inputSearchPark = findViewById(R.id.input_search_park);

        inputSearchPark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchPark();
            }
        });

        turnOnGPS();

    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/pcmiguel/ckhsyjp813gxb19qq3eqydsmu"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        mapboxMap.getUiSettings().setCompassEnabled(false);
                        mapboxMap.getUiSettings().setAttributionEnabled(false);
                        mapboxMap.getUiSettings().setLogoEnabled(false);
                        enableLocationComponent(style);

                        List<Feature> markerCoordinates = new ArrayList<>();

                        String url = "http://10.72.120.186:3000/api/parks";

                        StringRequest request = new StringRequest(Request.Method.GET, url,
                                new com.android.volley.Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        if (response != null) {
                                            try {
                                                JSONArray jsonArray = new JSONArray(response);
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    JSONObject data = jsonArray.getJSONObject(i);

                                                    Feature feature = Feature.fromGeometry(Point.fromLngLat(data.getDouble("park_longitude"), data.getDouble("park_latitude")));
                                                    feature.addStringProperty("PARK_PRICE", data.getDouble("park_price_hour") + "€");
                                                    feature.addNumberProperty("PARK_ID", data.getInt("park_id"));
                                                    feature.addStringProperty("PARK_LAT", data.getString("park_latitude"));
                                                    feature.addStringProperty("PARK_LNT", data.getString("park_longitude"));
                                                    markerCoordinates.add(feature);

                                                }

                                                style.addSource(new GeoJsonSource("marker-source",
                                                        FeatureCollection.fromFeatures(markerCoordinates)));

                                                //Add the marker image to map
                                                style.addImage("marker-park-price", BitmapFactory.decodeResource(
                                                        MapScreen.this.getResources(), R.drawable.marker));

                                                // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
                                                // middle of the icon being fixed to the coordinate point.
                                                style.addLayer(new SymbolLayer("marker-layer", "marker-source")
                                                        .withProperties(PropertyFactory.iconImage("marker-park-price"),
                                                                iconAllowOverlap(true),
                                                                textField(Expression.get("PARK_PRICE")),
                                                                textColor("#FFF"),
                                                                textFont(new String[] {"Montserrat Regular"}),
                                                                textSize(15f)));

                                                style.addSource(new GeoJsonSource("selected-marker"));

                                                //Add the marker image to map
                                                style.addImage("marker-park-selected", BitmapFactory.decodeResource(
                                                        MapScreen.this.getResources(), R.drawable.marker2));

                                                // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
                                                // middle of the icon being fixed to the coordinate point.
                                                style.addLayer(new SymbolLayer("selected-marker-layer", "selected-marker")
                                                        .withProperties(PropertyFactory.iconImage("marker-park-selected"),
                                                                iconAllowOverlap(true),
                                                                textField("P"),
                                                                textColor("#000"),
                                                                textFont(new String[] {"Montserrat ExtraBold"}),
                                                                textSize(29f)));

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }

                                    }
                                }, new com.android.volley.Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MapScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        requestQueue.add(request);

                        mapboxMap.addOnMapClickListener(MapScreen.this);

                        initSource(style);

                        initLayers(style);

                    }
                });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        Style style = mapboxMap.getStyle();

        if (style != null) {

            final SymbolLayer selectedMarkerSymbolLayer =
                    (SymbolLayer) style.getLayer("selected-marker-layer");

            final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);

            List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "marker-layer");

            List<Feature> selectedFeature = mapboxMap.queryRenderedFeatures(
                    pixel, "selected-marker-layer");

            if (selectedFeature.size() > 0 && markerSelected) {
                return false;
            }

            if (features.isEmpty()) {
                if (markerSelected) {
                    markerSelected = false;
                }
                return false;
            }

            GeoJsonSource source = style.getSourceAs("selected-marker");
            if (source != null) {
                source.setGeoJson(FeatureCollection.fromFeatures(
                        new Feature[]{Feature.fromGeometry(features.get(0).geometry())}));
            }

            if (markerSelected) {
                markerSelected = false;
            }

            if (features.size() > 0) {
                markerSelected = true;

                double lat = Double.parseDouble(features.get(0).getStringProperty("PARK_LAT"));
                double lnt = Double.parseDouble(features.get(0).getStringProperty("PARK_LNT"));

                //Open bottom sheet with park information
                openParkInfo(features.get(0).getNumberProperty("PARK_ID").intValue(), lat, lnt);

                //Calcular rota até ao destino
                Point origin = Point.fromLngLat(userLocation.getLongitude(), userLocation.getLatitude());
                Point destination = Point.fromLngLat(lnt, lat);
                getRoute(mapboxMap, origin, destination);
            }

        }
        return true;

    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            LocationComponentOptions locationComponentOptions =
                    LocationComponentOptions.builder(this)
                            .pulseEnabled(true)
                            .pulseColor(getResources().getColor(R.color.colorPrimary))
                            .pulseAlpha(.4f)
                            .pulseInterpolator(new BounceInterpolator())
                            .build();

            // Activate with options
            LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions
                    .builder(this, style)
                    .locationComponentOptions(locationComponentOptions)
                    .build();

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();


        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, "Please give location permissions!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private static class MainActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MapScreen> activityWeakReference;

        MainActivityLocationCallback(MapScreen activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            MapScreen activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                // save the user location
                if (result != null) {
                    activity.userLocation = new LatLng(result.getLastLocation().getLatitude(), result.getLastLocation().getLongitude());
                }

                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            MapScreen activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openSearchPark() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                MapScreen.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_search_park,
                        (LinearLayout) findViewById(R.id.bottomSheetParkInfo)
                );

        //List type parking
        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.list_type_parking);
        TypeParkingAdapter typeParkingAdapter = new TypeParkingAdapter(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(typeParkingAdapter);

        //List Parking features
        RecyclerView recyclerView2 = bottomSheetView.findViewById(R.id.list_features_parking);
        FeatureParkingAdapter featureParkingAdapter = new FeatureParkingAdapter(this);

        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView2.setLayoutManager(layoutManager2);
        recyclerView2.setItemAnimator(new DefaultItemAnimator());
        recyclerView2.setAdapter(featureParkingAdapter);

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    private void openParkInfo(int id, double lat, double lnt) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                MapScreen.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_sheet_park_info,
                        (LinearLayout) findViewById(R.id.bottomSheetParkInfo)
                );

        TextView name, address, total_spots, price, hours, hours_status, contact, full_address;

        name = bottomSheetView.findViewById(R.id.park_name);
        address = bottomSheetView.findViewById(R.id.park_address);
        total_spots = bottomSheetView.findViewById(R.id.park_totalSpots);
        price = bottomSheetView.findViewById(R.id.park_price);
        hours = bottomSheetView.findViewById(R.id.park_hours);
        hours_status = bottomSheetView.findViewById(R.id.park_hoursStatus);
        contact = bottomSheetView.findViewById(R.id.park_contact);
        full_address = bottomSheetView.findViewById(R.id.park_fullAddress);

        String url = "http://10.72.120.186:3000/api/parks/"+id;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                String close_hour = jsonObject.getString("park_hour_close");
                                String open_hour = jsonObject.getString("park_hour_open");

                                name.setText(jsonObject.getString("park_name"));
                                total_spots.setText(jsonObject.getString("park_spots")+" spots");
                                price.setText(jsonObject.getString("park_price_hour")+"/h");
                                hours.setText(open_hour + " - " + close_hour);

                                contact.setText(jsonObject.getString("park_contact"));

                                //Verificar se o parque está fechado ou aberto
                                SimpleDateFormat formatter = new SimpleDateFormat("HHmm", Locale.getDefault());

                                String now = formatter.format(new Date());
                                String date_close = close_hour.replace(":", "");
                                String date_open = open_hour.replace(":", "");

                                boolean isBetween = Integer.parseInt(now) > Integer.parseInt(date_open)
                                        && Integer.parseInt(now) < Integer.parseInt(date_close);

                                if (isBetween) {
                                    hours_status.setText("Now is open");
                                    hours_status.setTextColor(Color.parseColor("#4EB84A"));
                                } else {
                                    hours_status.setText("Now is close");
                                    hours_status.setTextColor(Color.parseColor("#F04444"));
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(request);

        String url2 = "https://api.mapbox.com/geocoding/v5/mapbox.places/"+lnt+","+lat+".json?limit=1&access_token=pk.eyJ1IjoicGNtaWd1ZWwiLCJhIjoiY2toc3lncG1zMGllajJxcGkxYnNjanVieCJ9.yfUra6VpwwsP4dGk9badRA";

        StringRequest request2 = new StringRequest(Request.Method.GET, url2,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                JSONArray jsonArray = jsonObject.getJSONArray("features");

                                JSONObject parkObject = jsonArray.getJSONObject(0);

                                address.setText(parkObject.getString("text"));
                                full_address.setText(parkObject.getString("place_name"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapScreen.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(request2);


        bottomSheetView.findViewById(R.id.btn_openBookDetails).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();

                //Enviar as informações do parque para o outro dialog
                JSONObject info = new JSONObject();
                try {
                    info.put("name", name.getText().toString());
                    info.put("spots", total_spots.getText().toString());
                    info.put("address", address.getText().toString());
                    info.put("full_address", full_address.getText().toString());
                    info.put("price", price.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                openBookDetails(info);
            }
        });


        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    private void turnOnGPS() {

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        //Verificar se o GPS está ativo ou nao
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try { //GPS ON
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException e) { //GPS OFF
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MapScreen.this, REQUEST_CHECK_SETTING);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: //Quando o dispositivo do utilizador não tem localização
                            break;
                    }
                }

            }
        });

    }

    /**
     * Add the route to the map
     */
    private void initSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID));
    }

    private void initLayers(@NonNull Style loadedMapStyle) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(getResources().getColor(R.color.colorPrimary))
        );
        loadedMapStyle.addLayer(routeLayer);
    }

    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     */
    private void getRoute(MapboxMap mapboxMap, Point origin, Point destination) {
        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.mapbox_access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                if (response.body() == null) {
                    return;
                } else if (response.body().routes().size() < 1) {
                    return;
                }

                // Get the directions route
                currentRoute = response.body().routes().get(0);

                if (mapboxMap != null) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {

                            // Retrieve and update the source designated for showing the directions route
                            GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);

                            // Create a LineString with the directions route's geometry and
                            // reset the GeoJSON source for the route LineLayer source
                            if (source != null) {
                                source.setGeoJson(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6));
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Toast.makeText(MapScreen.this, "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openBookDetails(JSONObject info) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                MapScreen.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_sheet_book_info,
                        (LinearLayout) findViewById(R.id.bottomSheetBookDetails)
                );

        TextView startDate, startHour, d, price, p_name, p_address, p_spots, p_price;

        startDate = bottomSheetView.findViewById(R.id.start_date);
        startHour = bottomSheetView.findViewById(R.id.start_hour);
        d = bottomSheetView.findViewById(R.id.duration);
        price = bottomSheetView.findViewById(R.id.total_price);

        p_name = bottomSheetView.findViewById(R.id.park_name);
        p_address = bottomSheetView.findViewById(R.id.park_address);
        p_spots = bottomSheetView.findViewById(R.id.park_totalSpots);
        p_price = bottomSheetView.findViewById(R.id.park_price);

        String fullAddress = "";

        try {
            p_name.setText(info.getString("name"));
            p_address.setText(info.getString("address"));
            p_spots.setText(info.getString("spots"));
            p_price.setText(info.getString("price"));
            fullAddress = info.getString("full_address");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        bottomSheetView.findViewById(R.id.btn_start_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showStartDayDialog(startDate, startHour);

            }
        });

        bottomSheetView.findViewById(R.id.btn_duration).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDurationDialog(d, price, p_price);

            }
        });

        final String fullAddressFinal = fullAddress;
        bottomSheetView.findViewById(R.id.btn_pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Verificar se está tudo preenchido
                if (startDate.getText() != "" && startHour.getText() != "" && d.getText() != "") {

                    bottomSheetDialog.dismiss();
                    Intent intent = new Intent(MapScreen.this, ParkingCodeScreen.class);
                    intent.putExtra("DateFrom", startDate.getText() + " " + startHour.getText());
                    intent.putExtra("Duration", d.getText());
                    intent.putExtra("Address", fullAddressFinal);
                    startActivity(intent);

                } else {
                    Toast.makeText(MapScreen.this, "Please select a date and duration!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    public void openPerfil(View view) {

        Intent intent = new Intent(MapScreen.this, PerfilScreen.class);
        startActivity(intent);

    }

    public void openVehicles(View view) {
        Intent intent = new Intent(MapScreen.this, VehiclesScreen.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    public void myLocation(View view) {

        if (userLocation != null) {

            CameraPosition pos = new CameraPosition.Builder()
                    .target(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()))
                    .zoom(10)
                    .build();

            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));

        }

    }

    private void showDurationDialog(final TextView hour, final TextView price, final  TextView p) {

        final Calendar calendar = Calendar.getInstance();

        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                hour.setText(calendar.get(Calendar.HOUR_OF_DAY)+"h " + calendar.get(Calendar.MINUTE)+"m");


                //Calcular o preço de acordo com o valor por hora e a duração
                String hourPrice = p.getText().toString().replace("/h", "");
                double hourPriceDouble = Double.parseDouble(hourPrice);
                double dateToHour = calendar.get(Calendar.HOUR_OF_DAY) + (calendar.get(Calendar.MINUTE) / 60.0);
                double total = (dateToHour * hourPriceDouble);

                price.setText("Pay €"+ String.format("%.2f", total).replace(",", "."));


            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(MapScreen.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true);
        timePickerDialog.show();


    }

    private void showStartDayDialog(final TextView date, final TextView hour) {

        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                date.setText(simpleDateFormat.format(calendar.getTime()));

                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        SimpleDateFormat simpleHourFormat = new SimpleDateFormat("HH:mm");
                        hour.setText(simpleHourFormat.format(calendar.getTime()));

                    }
                };

                new TimePickerDialog(MapScreen.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true).show();

            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(MapScreen.this, dateSetListener, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMinDate(calendar.getTimeInMillis() + (1000 * 60 * 60 * 24));
        datePickerDialog.show();

    }
}