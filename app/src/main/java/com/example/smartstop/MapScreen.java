package com.example.smartstop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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

    // Variables needed to listen to location updates
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_map_screen);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

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

                        markerCoordinates.add(Feature.fromGeometry(
                                Point.fromLngLat(-9.1212317,38.722098)));
                        markerCoordinates.add(Feature.fromGeometry(
                                Point.fromLngLat(-9.0210615,38.5417719)));
                        markerCoordinates.add(Feature.fromGeometry(
                                Point.fromLngLat(-8.6760867,38.4652694)));

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
                                        textField("2€"),
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
                openParkInfo();

                //Calcular rota até ao destino
                Point origin = Point.fromLngLat(userLocation.getLongitude(), userLocation.getLatitude());
                Point destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
                System.out.println("PointO: " + origin);
                System.out.println("PointD: " + destination);
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

    private void openParkInfo() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                MapScreen.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_sheet_park_info,
                        (LinearLayout) findViewById(R.id.bottomSheetParkInfo)
                );

        bottomSheetView.findViewById(R.id.btn_openBookDetails).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                openBookDetails();
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
     * Add the route and marker sources to the map
     */
    private void initSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID));
    }

    /**
     * Add the route and marker icon layers to the map
     */
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
     * @param mapboxMap the Mapbox map object that the route will be drawn on
     * @param origin      the starting point of the route
     * @param destination the desired finish point of the route
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
                System.out.println("Route: " + currentRoute);

                // Make a toast which displays the route's distance
                /*Toast.makeText(MapScreen.this, String.format(
                        "Distancia ",
                        currentRoute.distance()), Toast.LENGTH_SHORT).show();*/

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

    private void openBookDetails() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                MapScreen.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_sheet_book_info,
                        (LinearLayout) findViewById(R.id.bottomSheetBookDetails)
                );

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
                    .build();

            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));

        }

    }
}