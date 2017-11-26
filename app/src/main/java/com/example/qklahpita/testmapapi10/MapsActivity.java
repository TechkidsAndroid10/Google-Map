package com.example.qklahpita.testmapapi10;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.qklahpita.testmapapi10.map_direction.DirectionHandler;
import com.example.qklahpita.testmapapi10.map_direction.DirectionResponse;
import com.example.qklahpita.testmapapi10.map_direction.RetrofitInstance;
import com.example.qklahpita.testmapapi10.map_direction.RetrofitService;
import com.example.qklahpita.testmapapi10.map_direction.RouteModel;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.security.Permission;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";

    private static final int REQUEST_PERMISSION = 0;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private LatLng currentLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    private Button btFind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btFind = findViewById(R.id.bt_find);
        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findPlace();
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            setupPermission();
        } else {
            setupMap();
        }

        getCurrentLocation();
    }

    private void getCurrentLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 13);
                            mMap.animateCamera(cameraUpdate);

                        }
                    }
                });
    }

    private void findPlace() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: " + PlaceAutocomplete.getPlace(this, data));

            mMap.clear();
            RetrofitService retrofitService = RetrofitInstance.getInstance().create(RetrofitService.class);
            retrofitService.getDirection(
                    getLatLng(currentLocation),
                    getLatLng(PlaceAutocomplete.getPlace(this, data).getLatLng()),
                    getResources().getString(R.string.google_maps_key)
            ).enqueue(new Callback<DirectionResponse>() {
                @Override
                public void onResponse(Call<DirectionResponse> call, Response<DirectionResponse> response) {
                    RouteModel routeModel = DirectionHandler.getListRoute(response.body()).get(0);
                    mMap.addMarker(new MarkerOptions().position(routeModel.endLocation));

                    Log.d(TAG, "onResponse: " + routeModel.distance);

                    PolylineOptions polylineOptions = new PolylineOptions()
                            .color(Color.CYAN)
                            .width(8);

                    for (int i = 0; i < routeModel.points.size(); i++) {
                        polylineOptions.add(routeModel.points.get(i));
                    }

                    mMap.addPolyline(polylineOptions);
                    DirectionHandler.zoomRoute(mMap, routeModel.points);
                }

                @Override
                public void onFailure(Call<DirectionResponse> call, Throwable t) {

                }
            });
        }
    }

    public String getLatLng(LatLng latLng) {
        double lat = latLng.latitude;
        double lng = latLng.longitude;
        return lat + "," + lng;
    }

    private void setupMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: " + location.getLongitude() + " " + location.getLatitude());

                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 13);
                mMap.animateCamera(cameraUpdate);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1000, locationListener);
    }

    private void setupPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
        } else {
            setupMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0) {
                setupMap();
            } else {
                Toast.makeText(this, "Wtf?", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
    }
}
